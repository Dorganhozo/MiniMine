package com.minimine.utils.arrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
/*
 * buffer dinamico de shorts em memoria nativa(fora do heap Java)
 * cresce 1.5x quando cheio
 * tam = quantidade de shorts escritos
 */
public class ShortArrayUtil {
    public static final int TAM_INICIAL = 256; // shorts

    public ByteBuffer buf;
    public ShortBuffer shortBuf; // visão sobre buf
    public int tam = 0; // shorts escritos

    public ShortArrayUtil() {
        buf = ByteBuffer.allocateDirect(TAM_INICIAL * 2).order(ByteOrder.nativeOrder());
        shortBuf = buf.asShortBuffer();
    }

    public ShortArrayUtil(int tamInicial) {
        int cap = Math.max(tamInicial, TAM_INICIAL);
        buf = ByteBuffer.allocateDirect(cap * 2).order(ByteOrder.nativeOrder());
        shortBuf = buf.asShortBuffer();
    }

    public void add(short s) {
        if(tam == shortBuf.capacity()) crescer();
        shortBuf.put(tam, s);
        tam++;
    }

    private void crescer() {
        int novoTam = shortBuf.capacity() + (shortBuf.capacity() >> 1);
        ByteBuffer novo = ByteBuffer.allocateDirect(novoTam * 2).order(ByteOrder.nativeOrder());
        buf.position(0);
        buf.limit(tam * 2);
        novo.put(buf);
        novo.position(0);
        buf.limit(buf.capacity());
        buf = novo;
        shortBuf = buf.asShortBuffer();
    }

    // prepara o buf para leitura do inicio ate tam shorts
    public ByteBuffer bufPronto() {
        buf.position(0);
        buf.limit(tam * 2);
        return buf;
    }

    public float memoriaMB() {
        return (shortBuf.capacity() * 2f) / (1024f * 1024f);
    }
}

