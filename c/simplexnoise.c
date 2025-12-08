#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
#include <math.h>

// gradientes
static const int8_t GRAD2_X[8] = {1, -1, 1, -1, 1, -1, 0, 0};
static const int8_t GRAD2_Y[8] = {1, 1, -1, -1, 0, 0, 1, -1};

// constantes
static const float F2 = 0.366025403f;
static const float G2 = 0.211324865f;
static const float G2x2 = 0.577350269f;

typedef struct {
    uint8_t perm[512];
    uint8_t mod8[512];
} RuidoDados;

static uint32_t xorshift32(uint32_t* s) {
    uint32_t x = *s;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    *s = x;
    return x;
}

static inline int floorRapido(float x) {
    int i = (int)x;
    return i - (i > x);
}

// Cria estado:
JNIEXPORT jlong JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_iniciarC(
    JNIEnv* amb, jclass classe, jint seed) {
    
    RuidoDados* dados = (RuidoDados*)malloc(sizeof(RuidoDados));
    if(!dados) return 0;
    
    // base 0-255
    uint8_t base[256];
    for (int i = 0; i < 256; i++) {
        base[i] = (uint8_t)i;
    }
    // embaralha
    uint32_t rng = (seed == 0) ? 0x9E3779B9 : (uint32_t)seed;
    for(int i = 255; i > 0; i--) {
        uint32_t r = xorshift32(&rng);
        int j = (int)(r % (i + 1));
        uint8_t tmp = base[i];
        base[i] = base[j];
        base[j] = tmp;
    }
    // duplica
    for(int i = 0; i < 512; i++) {
        uint8_t v = base[i & 255];
        dados->perm[i] = v;
        dados->mod8[i] = v & 7;
    }
    return (jlong)(uintptr_t)dados;
}

// ruido:
JNIEXPORT jfloat JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_ruidoC(
    JNIEnv* amb, jclass classe, jlong ptr, jfloat xin, jfloat yin) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    if(!dados) return 0.0f;
    
    float s = (xin + yin) * F2;
    int i = floorRapido(xin + s);
    int j = floorRapido(yin + s);
    
    float t = (i + j) * G2;
    float X0 = (float)i - t;
    float Y0 = (float)j - t;
    float x0 = xin - X0;
    float y0 = yin - Y0;
    
    int i1, j1;
    if(x0 > y0) {
        i1 = 1;
        j1 = 0;
    } else {
        i1 = 0;
        j1 = 1;
    }
    float x1 = x0 - (float)i1 + G2;
    float y1 = y0 - (float)j1 + G2;
    float x2 = x0 - G2x2;
    float y2 = y0 - G2x2;
    
    int ii = i & 255;
    int jj = j & 255;
    
    const uint8_t* p = dados->perm;
    const uint8_t* g = dados->mod8;
    
    int gi0 = g[ii + p[jj]];
    int gi1 = g[ii + i1 + p[jj + j1]];
    int gi2 = g[ii + 1 + p[jj + 1]];
    
    float n0 = 0.0f, n1 = 0.0f, n2 = 0.0f;
    
    float t0 = 0.5f - x0*x0 - y0*y0;
    if(t0 > 0.0f) {
        t0 *= t0;
        n0 = t0 * t0 * (GRAD2_X[gi0]*x0 + GRAD2_Y[gi0]*y0);
    }
    float t1 = 0.5f - x1*x1 - y1*y1;
    if(t1 > 0.0f) {
        t1 *= t1;
        n1 = t1 * t1 * (GRAD2_X[gi1]*x1 + GRAD2_Y[gi1]*y1);
    }
    float t2 = 0.5f - x2*x2 - y2*y2;
    if(t2 > 0.0f) {
        t2 *= t2;
        n2 = t2 * t2 * (GRAD2_X[gi2]*x2 + GRAD2_Y[gi2]*y2);
    }
    return 70.0f * (n0 + n1 + n2);
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
    
    const uint8_t* p = dados->perm;
    const uint8_t* g = dados->mod8;
    
    for(int oct = 0; oct < octaves; oct++) {
        float xin = x * escalaAtual;
        float yin = z * escalaAtual;
        
        float s = (xin + yin) * F2;
        int i = floorRapido(xin + s);
        int j = floorRapido(yin + s);
        
        float t = (i + j) * G2;
        float X0 = (float)i - t;
        float Y0 = (float)j - t;
        float x0 = xin - X0;
        float y0 = yin - Y0;
        
        int i1, j1;
        if(x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        float x1 = x0 - (float)i1 + G2;
        float y1 = y0 - (float)j1 + G2;
        float x2 = x0 - G2x2;
        float y2 = y0 - G2x2;
        
        int ii = i & 255;
        int jj = j & 255;
        
        int gi0 = g[ii + p[jj]];
        int gi1 = g[ii + i1 + p[jj + j1]];
        int gi2 = g[ii + 1 + p[jj + 1]];
        
        float n0 = 0.0f, n1 = 0.0f, n2 = 0.0f;
        
        float t0 = 0.5f - x0*x0 - y0*y0;
        if(t0 > 0.0f) {
            t0 *= t0;
            n0 = t0 * t0 * (GRAD2_X[gi0]*x0 + GRAD2_Y[gi0]*y0);
        }
        float t1 = 0.5f - x1*x1 - y1*y1;
        if(t1 > 0.0f) {
            t1 *= t1;
            n1 = t1 * t1 * (GRAD2_X[gi1]*x1 + GRAD2_Y[gi1]*y1);
        }
        float t2 = 0.5f - x2*x2 - y2*y2;
        if(t2 > 0.0f) {
            t2 *= t2;
            n2 = t2 * t2 * (GRAD2_X[gi2]*x2 + GRAD2_Y[gi2]*y2);
        }
        float ruidoV = 70.0f * (n0 + n1 + n2);
        total += ruidoV * amplitude;
        maxValor += amplitude;
        amplitude *= persis;
        escalaAtual *= 2.0f;
    }
    if(maxValor > 0.0f) return total / maxValor;
    
    return total;
}

JNIEXPORT void JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise2D_liberarC(
    JNIEnv* amb, jclass classe, jlong ptr) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    if(dados) free(dados);
}