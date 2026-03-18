package com.minimine.utils.arrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
/*
 * buffer dinamico de floats em memoria nativa(fora do heap Java)
 * cresce 1.5x quando cheio, igual ao anterior
 * tam = quantidade de floats escritos
 */
public class FloatArrayUtil {
    public static final int TAM_INICIAL = 256; // floats

    public ByteBuffer buf;
    public FloatBuffer floatBuf; // visão sobre buf, pra putFloat rapido
    public int tam = 0; // floats escritos

    public FloatArrayUtil() {
        buf = ByteBuffer.allocateDirect(TAM_INICIAL * 4).order(ByteOrder.nativeOrder());
        floatBuf = buf.asFloatBuffer();
    }

    public FloatArrayUtil(int tamInicial) {
        int cap = Math.max(tamInicial, TAM_INICIAL);
        buf = ByteBuffer.allocateDirect(cap * 4).order(ByteOrder.nativeOrder());
        floatBuf = buf.asFloatBuffer();
    }

    public void add(float f) {
        if(tam == floatBuf.capacity()) crescer();
        floatBuf.put(tam, f);
        tam++;
    }

    private void crescer() {
        int novoTam = floatBuf.capacity() + (floatBuf.capacity() >> 1);
        ByteBuffer novo = ByteBuffer.allocateDirect(novoTam * 4).order(ByteOrder.nativeOrder());
        // copia conteudo nativo para o novo buffer
        buf.position(0);
        buf.limit(tam * 4);
        novo.put(buf);
        novo.position(0);
        buf.limit(buf.capacity());
        buf = novo;
        floatBuf = buf.asFloatBuffer();
    }

    // prepara o buf para leitura do inicio até tam floats
    public ByteBuffer bufPronto() {
        buf.position(0);
        buf.limit(tam * 4);
        return buf;
    }

    public float memoriaMB() {
        return (floatBuf.capacity() * 4f) / (1024f * 1024f);
    }
}

