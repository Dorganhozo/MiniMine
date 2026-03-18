package com.minimine.utils;

public final class MemNativa {
    static {
        System.loadLibrary("memnativa");
    }
    public static native long alocar(int quantidade);
    public static native long alocarZerado(int quantidade);
    public static native long realocar(long fim, int quantidade);
    public static native void liberar(long fim);
    public static native int  lerInt(long fim, int indice);
    public static native void gravarInt(long fim, int indice, int valor);
}

