#include "jni.h"

JNIEXPORT jlong JNICALL
Java_com_minimine_utils_MemNativa_alocar(JNIEnv *env, jclass cls, jint quantidade) {
    return (jlong) __builtin_malloc((unsigned long) quantidade * 4);
}

JNIEXPORT jlong JNICALL
Java_com_minimine_utils_MemNativa_alocarZerado(JNIEnv *env, jclass cls, jint quantidade) {
    return (jlong) __builtin_calloc((unsigned long) quantidade, 4);
}

JNIEXPORT jlong JNICALL
Java_com_minimine_utils_MemNativa_realocar(JNIEnv *env, jclass cls, jlong fim, jint quantidade) {
    return (jlong) __builtin_realloc((void*) fim, (unsigned long) quantidade * 4);
}

JNIEXPORT void JNICALL
Java_com_minimine_utils_MemNativa_liberar(JNIEnv *env, jclass cls, jlong fim) {
    if(fim == 0) return;
    __builtin_free((void*) fim);
}

JNIEXPORT jint JNICALL
Java_com_minimine_utils_MemNativa_lerInt(JNIEnv *env, jclass cls, jlong fim, jint indice) {
    if(fim == 0) return 0;
    return ((jint*) fim)[indice];
}

JNIEXPORT void JNICALL
Java_com_minimine_utils_MemNativa_gravarInt(JNIEnv *env, jclass cls, jlong fim, jint indice, jint valor) {
    if(fim == 0) return;
    ((jint*) fim)[indice] = valor;
}
