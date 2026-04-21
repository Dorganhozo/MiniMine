package com.minimine.utils;

import java.io.*;

public final class MemNativa {
    static {
        carregarBiblioteca();
    }

    public static void carregarBiblioteca() {
        String os = System.getProperty("os.name").toLowerCase();
        String arq = System.getProperty("os.arch").toLowerCase();

        String nome;
        if(os.contains("win")) {
            nome = "memnativa.dll";
        } else if (os.contains("mac")) {
            nome = "libmemnativa.dylib";
        } else {
            nome = "libmemnativa.so";
        }
        // caminho dentro do jar: /libs/x86_64/libmemnativa.so etc
        String pasta;
        if(arq.contains("aarch64") || arq.contains("arm64")) {
            pasta = "arm64-v8a";
        } else if (arq.contains("amd64") || arq.contains("x86_64")) {
            pasta = "x86_64";
        } else {
            pasta = "x86";
        }
        String recurso = "/libs/" + pasta + "/" + nome;
        try {
            InputStream en = MemNativa.class.getResourceAsStream(recurso);
            if(en == null) {
                // em caso de erro pro carregamento normal
                System.loadLibrary("memnativa");
                return;
            }
            File temp = File.createTempFile("memnativa", nome.substring(nome.lastIndexOf('.')));
            temp.deleteOnExit();
            FileOutputStream saida = new FileOutputStream(temp);
            byte[] buf = new byte[4096];
            int n;
            while((n = en.read(buf)) != -1) saida.write(buf, 0, n);
            en.close();
            saida.close();
            System.load(temp.getAbsolutePath());
        } catch(Exception e) {
            throw new RuntimeException("Falha ao carregar memnativa: " + recurso, e);
        }
    }

    public static native long alocar(int quantidade);
    public static native long alocarZerado(int quantidade);
    public static native long realocar(long fim, int quantidade);
    public static native void liberar(long fim);
    public static native int lerInt(long fim, int indice);
    public static native void gravarInt(long fim, int indice, int valor);
}
