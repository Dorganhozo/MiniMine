package com.minimine.cenas;

import com.badlogic.gdx.graphics.Mesh;

public final class Chunk {
    public int bitsPorBloco = 4; // 1..8 (modo direto)
    public int blocosPorInt = 32 / this.bitsPorBloco;; // = 32 / bitsPorBloco ou 32 / paletaBits
    public int[] blocos; // buffer com dados empacotados(indices de paleta ou ids diretos)
    public byte[] luz = new byte[16*255*16/2];
    public Mesh mesh;
    public int x, z, maxIds = 8;
    public volatile boolean att = false;
    public boolean usaPaleta = true; // controla se estamos no modo paleta
    public int paletaTam = 0;    // quantos entries existem
    public int paletaBits = 1; // bits para indice da paleta(1..8)
	public int[] paleta = new int[1 << this.paletaBits]; // array de valores reais(ids de blocos)
	public volatile boolean dadosProntos = false;
	public volatile boolean meshEmConstrucao = false;
}
