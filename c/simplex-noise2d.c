#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
#include <math.h>

// gradientes pré computados como inteiros(melhor pra SIMD)
static const int8_t GRAD2_X[8] = {1, -1, 1, -1, 1, -1, 0, 0};
static const int8_t GRAD2_Y[8] = {1, 1, -1, -1, 0, 0, 1, -1};

// constantes otimizadas
static const float F2 = 0.366025403f;  // (sqrt(3)-1)/2
static const float G2 = 0.211324865f;  // (3-sqrt(3))/6
static const float G2x2 = 0.577350269f; // 1 - 2*G2
static const float MULT = 70.0f;       // fator de escala

typedef struct {
    uint8_t perm[512];
    uint8_t mod8[512];
} RuidoDados;

// RNG
static inline uint32_t xorshift32(uint32_t* s) {
    uint32_t x = *s;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    return *s = x;
}

static inline int floorRapido(float x) {
    int xi = (int)x;
    return xi - (xi > x);
}

// macro pra calculo de contribuição(evita duplicação de codigo)
#define CALC_CONTRIB(t, x, y, gi, n) \
    do { \
        float t_sq = t * t; \
        float t_quad = t_sq * t_sq; \
        n = t_quad * (GRAD2_X[gi] * x + GRAD2_Y[gi] * y); \
    } while(0)

static inline float simplexPrincipal(RuidoDados* dados, float xin, float yin) {
    // calculo de coordenadas simplex
    float s = (xin + yin) * F2;
    int i = floorRapido(xin + s);
    int j = floorRapido(yin + s);
    
    float t = (i + j) * G2;
    float X0 = i - t;
    float Y0 = j - t;
    float x0 = xin - X0;
    float y0 = yin - Y0;
    // determina ordem simplical
    int i1, j1;
    i1 = (x0 > y0);
    j1 = !i1;
    // coordenadas das vertices
    float x1 = x0 - i1 + G2;
    float y1 = y0 - j1 + G2;
    float x2 = x0 - G2x2;
    float y2 = y0 - G2x2;
    // indices de permutação
    int ii = i & 255;
    int jj = j & 255;
    
    const uint8_t* p = dados->perm;
    const uint8_t* g = dados->mod8;
    // gradientes
    int gi0 = g[ii + p[jj]];
    int gi1 = g[ii + i1 + p[jj + j1]];
    int gi2 = g[ii + 1 + p[jj + 1]];
    // contribuições
    float n0 = 0.0f, n1 = 0.0f, n2 = 0.0f;
    
    float t0 = 0.5f - x0*x0 - y0*y0;
    if(t0 > 0.0f) {
        CALC_CONTRIB(t0, x0, y0, gi0, n0);
    }
    float t1 = 0.5f - x1*x1 - y1*y1;
    if(t1 > 0.0f) {
        CALC_CONTRIB(t1, x1, y1, gi1, n1);
    }
    float t2 = 0.5f - x2*x2 - y2*y2;
    if(t2 > 0.0f) {
        CALC_CONTRIB(t2, x2, y2, gi2, n2);
    }
    return MULT * (n0 + n1 + n2);
}

JNIEXPORT jlong JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_iniciarC(
    JNIEnv* amb, jclass classe, jint semente) {
    
    RuidoDados* dados = (RuidoDados*)malloc(sizeof(RuidoDados));
    if(!dados) return 0;
    
    // embaralhamento usando LCG
    uint8_t base[256];
    for (int i = 0; i < 256; i++) {
        base[i] = i;
    }
    uint32_t rng = semente ? semente : 0x9E3779B9;
    // usa LCG pra embaralhar(mais rápido que xorshift pra isso)
    for(int i = 255; i > 0; i--) {
        rng = rng * 1103515245 + 12345;
        int j = (rng >> 16) % (i + 1);
        uint8_t tmp = base[i];
        base[i] = base[j];
        base[j] = tmp;
    }
    // duplica arrays, loop desenrolado parcialmente
    for(int i = 0; i < 256; i++) {
        uint8_t v = base[i];
        dados->perm[i] = v;
        dados->perm[i + 256] = v;
        dados->mod8[i] = v & 7;
        dados->mod8[i + 256] = v & 7;
    }
    return (jlong)(uintptr_t)dados;
}

// ruido
JNIEXPORT jfloat JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_ruidoC(
    JNIEnv* amb, jclass classe, jlong ptr, jfloat xin, jfloat yin) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    return dados ? simplexPrincipal(dados, xin, yin) : 0.0f;
}

// ruido fractal
JNIEXPORT jfloat JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_ruidoFractalC(
    JNIEnv* amb, jclass classe, jlong ptr,
    jfloat x, jfloat z, jfloat escala, jint octaves, jfloat persis) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    if(!dados || octaves <= 0) return 0.0f;
    
    float total = 0.0f;
    float amplitude = 1.0f;
    float maxValor = 0.0f;
    float escalaAtual = escala;
    
    // cache de amplitudes pra evitar multiplicações em loop
    float amplitudes[16]; // maximo 16 octaves
    float escalas[16];
    
    // pré calcula amplitudes e escalas
    for(int oct = 0; oct < octaves; oct++) {
        amplitudes[oct] = amplitude;
        escalas[oct] = escalaAtual;
        amplitude *= persis;
        escalaAtual *= 2.0f;
    }
    // loop principal desenrolado parcialmente
    int oct = 0;
    for(; oct + 3 < octaves; oct += 4) {
        // processa 4 octaves de uma vez
        float acc0 = simplexPrincipal(dados, x * escalas[oct], z * escalas[oct]) * amplitudes[oct];
        float acc1 = simplexPrincipal(dados, x * escalas[oct+1], z * escalas[oct+1]) * amplitudes[oct+1];
        float acc2 = simplexPrincipal(dados, x * escalas[oct+2], z * escalas[oct+2]) * amplitudes[oct+2];
        float acc3 = simplexPrincipal(dados, x * escalas[oct+3], z * escalas[oct+3]) * amplitudes[oct+3];
        
        total += acc0 + acc1 + acc2 + acc3;
        maxValor += amplitudes[oct] + amplitudes[oct+1] + amplitudes[oct+2] + amplitudes[oct+3];
    }
    // processa octaves restantes
    for(; oct < octaves; oct++) {
        float ruidoV = simplexPrincipal(dados, x * escalas[oct], z * escalas[oct]);
        total += ruidoV * amplitudes[oct];
        maxValor += amplitudes[oct];
    }
    return maxValor > 0.0f ? total / maxValor : total;
}

JNIEXPORT void JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_liberarC(
    JNIEnv* amb, jclass classe, jlong ptr) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    free(dados);
}