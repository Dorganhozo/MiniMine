#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
#include <math.h>

// gradientes 3D
static const int8_t GRAD3_X[12] = {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0};
static const int8_t GRAD3_Y[12] = {1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1};
static const int8_t GRAD3_Z[12] = {0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1};
// constantes pra 3D
static const float F3 = 1.0f / 3.0f;
static const float G3 = 1.0f / 6.0f;
static const float G3x3 = 3.0f * G3;

typedef struct {
    uint8_t perm[512];
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

// cria estado pra 3D:
JNIEXPORT jlong JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise3D_iniciarC(
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
        dados->perm[i] = base[i & 255];
    }
    return (jlong)(uintptr_t)dados;
}

// ruido 3D:
JNIEXPORT jfloat JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise3D_ruidoC(
    JNIEnv* amb, jclass classe, jlong ptr, jfloat xin, jfloat yin, jfloat zin) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    if(!dados) return 0.0f;
    
    float s = (xin + yin + zin) * F3;
    int i = floorRapido(xin + s);
    int j = floorRapido(yin + s);
    int k = floorRapido(zin + s);
    
    float t = (i + j + k) * G3;
    float X0 = (float)i - t;
    float Y0 = (float)j - t;
    float Z0 = (float)k - t;
    float x0 = xin - X0;
    float y0 = yin - Y0;
    float z0 = zin - Z0;
    
    int i1, j1, k1;
    int i2, j2, k2;
    
    if(x0 >= y0) {
        if(y0 >= z0) { i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
        else if(x0 >= z0) { i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1; }
        else { i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1; }
    } else {
        if(y0 < z0) { i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1; }
        else if(x0 < z0) { i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1; }
        else { i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
    }
    
    float x1 = x0 - (float)i1 + G3;
    float y1 = y0 - (float)j1 + G3;
    float z1 = z0 - (float)k1 + G3;
    float x2 = x0 - (float)i2 + 2.0f * G3;
    float y2 = y0 - (float)j2 + 2.0f * G3;
    float z2 = z0 - (float)k2 + 2.0f * G3;
    float x3 = x0 - 1.0f + G3x3;
    float y3 = y0 - 1.0f + G3x3;
    float z3 = z0 - 1.0f + G3x3;
    
    int ii = i & 255;
    int jj = j & 255;
    int kk = k & 255;
    
    const uint8_t* p = dados->perm;
    
    int gi0 = p[ii + p[jj + p[kk]]] % 12;
    int gi1 = p[ii + i1 + p[jj + j1 + p[kk + k1]]] % 12;
    int gi2 = p[ii + i2 + p[jj + j2 + p[kk + k2]]] % 12;
    int gi3 = p[ii + 1 + p[jj + 1 + p[kk + 1]]] % 12;
    
    float n0 = 0.0f, n1 = 0.0f, n2 = 0.0f, n3 = 0.0f;
    
    float t0 = 0.6f - x0*x0 - y0*y0 - z0*z0;
    if(t0 > 0.0f) {
        t0 *= t0;
        n0 = t0 * t0 * (GRAD3_X[gi0]*x0 + GRAD3_Y[gi0]*y0 + GRAD3_Z[gi0]*z0);
    }
    float t1 = 0.6f - x1*x1 - y1*y1 - z1*z1;
    if(t1 > 0.0f) {
        t1 *= t1;
        n1 = t1 * t1 * (GRAD3_X[gi1]*x1 + GRAD3_Y[gi1]*y1 + GRAD3_Z[gi1]*z1);
    }
    float t2 = 0.6f - x2*x2 - y2*y2 - z2*z2;
    if(t2 > 0.0f) {
        t2 *= t2;
        n2 = t2 * t2 * (GRAD3_X[gi2]*x2 + GRAD3_Y[gi2]*y2 + GRAD3_Z[gi2]*z2);
    }
    float t3 = 0.6f - x3*x3 - y3*y3 - z3*z3;
    if(t3 > 0.0f) {
        t3 *= t3;
        n3 = t3 * t3 * (GRAD3_X[gi3]*x3 + GRAD3_Y[gi3]*y3 + GRAD3_Z[gi3]*z3);
    }
    return 32.0f * (n0 + n1 + n2 + n3);
}

// ruido fractal 3D
JNIEXPORT jfloat JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise3D_ruidoFractalC(
    JNIEnv* amb, jclass classe, jlong ptr,
    jfloat x, jfloat y, jfloat z, jfloat escala, jint octaves, jfloat persis) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    if(!dados || octaves <= 0) return 0.0f;
    
    float total = 0.0f;
    float amplitude = 1.0f;
    float maxValor = 0.0f;
    float escalaAtual = escala;
    
    const uint8_t* p = dados->perm;
    
    for(int oct = 0; oct < octaves; oct++) {
        float xin = x * escalaAtual;
        float yin = y * escalaAtual;
        float zin = z * escalaAtual;
        
        float s = (xin + yin + zin) * F3;
        int i = floorRapido(xin + s);
        int j = floorRapido(yin + s);
        int k = floorRapido(zin + s);
        
        float t = (i + j + k) * G3;
        float X0 = (float)i - t;
        float Y0 = (float)j - t;
        float Z0 = (float)k - t;
        float x0 = xin - X0;
        float y0 = yin - Y0;
        float z0 = zin - Z0;
        
        int i1, j1, k1;
        int i2, j2, k2;
        
        if(x0 >= y0) {
            if(y0 >= z0) { i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
            else if(x0 >= z0) { i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1; }
            else { i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1; }
        } else {
            if(y0 < z0) { i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1; }
            else if(x0 < z0) { i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1; }
            else { i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
        }
        float x1 = x0 - (float)i1 + G3;
        float y1 = y0 - (float)j1 + G3;
        float z1 = z0 - (float)k1 + G3;
        float x2 = x0 - (float)i2 + 2.0f * G3;
        float y2 = y0 - (float)j2 + 2.0f * G3;
        float z2 = z0 - (float)k2 + 2.0f * G3;
        float x3 = x0 - 1.0f + G3x3;
        float y3 = y0 - 1.0f + G3x3;
        float z3 = z0 - 1.0f + G3x3;
        
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        
        int gi0 = p[ii + p[jj + p[kk]]] % 12;
        int gi1 = p[ii + i1 + p[jj + j1 + p[kk + k1]]] % 12;
        int gi2 = p[ii + i2 + p[jj + j2 + p[kk + k2]]] % 12;
        int gi3 = p[ii + 1 + p[jj + 1 + p[kk + 1]]] % 12;
        
        float n0 = 0.0f, n1 = 0.0f, n2 = 0.0f, n3 = 0.0f;
        
        float t0 = 0.6f - x0*x0 - y0*y0 - z0*z0;
        if(t0 > 0.0f) {
            t0 *= t0;
            n0 = t0 * t0 * (GRAD3_X[gi0]*x0 + GRAD3_Y[gi0]*y0 + GRAD3_Z[gi0]*z0);
        }
        float t1 = 0.6f - x1*x1 - y1*y1 - z1*z1;
        if(t1 > 0.0f) {
            t1 *= t1;
            n1 = t1 * t1 * (GRAD3_X[gi1]*x1 + GRAD3_Y[gi1]*y1 + GRAD3_Z[gi1]*z1);
        }
        float t2 = 0.6f - x2*x2 - y2*y2 - z2*z2;
        if(t2 > 0.0f) {
            t2 *= t2;
            n2 = t2 * t2 * (GRAD3_X[gi2]*x2 + GRAD3_Y[gi2]*y2 + GRAD3_Z[gi2]*z2);
        }
        float t3 = 0.6f - x3*x3 - y3*y3 - z3*z3;
        if(t3 > 0.0f) {
            t3 *= t3;
            n3 = t3 * t3 * (GRAD3_X[gi3]*x3 + GRAD3_Y[gi3]*y3 + GRAD3_Z[gi3]*z3);
        }
        float ruidoV = 32.0f * (n0 + n1 + n2 + n3);
        total += ruidoV * amplitude;
        maxValor += amplitude;
        amplitude *= persis;
        escalaAtual *= 2.0f;
    }
    if(maxValor > 0.0f) return total / maxValor;
    
    return total;
}

JNIEXPORT void JNICALL
Java_com_minimine_utils_ruidos_SimplexNoise3D_liberarC(
    JNIEnv* amb, jclass classe, jlong ptr) {
    
    RuidoDados* dados = (RuidoDados*)(uintptr_t)ptr;
    if(dados) free(dados);
}