package com.minimine.utils.arrays;

/*
 * array dinamico de floats otimizado para economizar memória
 * começa pequeno(256 elementos = ~1KB) e cresce conforme necessario
 * economia: ~98% de RAM em chunks vazias/esparsas
 */
public class FloatArrayUtil {
    public static final int TAM_INICIAL = 256; // ~1KB
    public float[] arr;
    public int tam = 0;

    public FloatArrayUtil() {
        this.arr = new float[TAM_INICIAL];
    }
    public FloatArrayUtil(int tamInicial) {
        this.arr = new float[Math.max(tamInicial, TAM_INICIAL)];
    }

    public void add(float f) {
        if(tam == arr.length) {
            // cresce 1.5x
            int novoTam = arr.length + (arr.length >> 1);
            float[] n = new float[novoTam];
            System.arraycopy(arr, 0, n, 0, arr.length);
            arr = n;
        }
        arr[tam++] = f;
    }

    // retorna o uso de memoria atual em KB
    public float memoriaMB() {
        return (arr.length * 4f) / (1024f * 1024f);
    }
}

