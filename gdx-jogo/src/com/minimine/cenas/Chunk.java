package com.minimine.cenas;

import com.badlogic.gdx.graphics.Mesh;

public final class Chunk {
    public int bitsPorBloco; // 1..8 (modo direto)
    public int blocosPorInt; // = 32 / bitsPorBloco ou 32 / paletaBits
    public int[] blocos;     // buffer com dados empacotados (índices de paleta ou ids diretos)

    // luz (mantive do jeito antigo)
    public byte[] luz = new byte[16*255*16/2];
    public Mesh mesh;
    public int x, z, maxIds = 8;
    public boolean att = false;

    // --- campos da paleta ---
    public boolean usaPaleta = true; // controla se estamos no modo paleta
    public int[] paleta;             // array de valores reais (ids de blocos)
    public int paletaTamanho = 0;    // quantos entries existem
    public int paletaBits = 1;       // bits para index da paleta (1..8)

    public Chunk() {
        // valores iniciais simples — compactar(...) será chamado pelo mundo para configurar array 'blocos'
        this.bitsPorBloco = 4;
        this.blocosPorInt = 32 / this.bitsPorBloco;
        this.blocos = null;

        this.usaPaleta = true;
        this.paletaBits = 1;
        this.paleta = new int[1 << this.paletaBits];
        this.paletaTamanho = 0;

        this.maxIds = 8;
        this.att = false;
        this.mesh = null;
    }
}
