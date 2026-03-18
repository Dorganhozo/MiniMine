#pragma once

typedef unsigned char      jboolean;
typedef signed char        jbyte;
typedef unsigned short     jchar;
typedef short              jshort;
typedef int                jint;
typedef long long          jlong;
typedef float              jfloat;
typedef double             jdouble;
typedef void*              jobject;
typedef jobject            jclass;
typedef void*              JNIEnv;

#define JNIEXPORT __attribute__((visibility("default")))
#define JNICALL
