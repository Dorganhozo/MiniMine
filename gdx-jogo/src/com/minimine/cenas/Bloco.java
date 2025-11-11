package com.minimine.cenas;

import com.minimine.utils.ChunkUtil;

public class Bloco {
	public CharSequence nome;
	public int tipo;
	public int topo, lados, baixo;
	public boolean transparente = false;
	public boolean solido = true;
	public boolean cullingAlto = false;
	
	public Bloco(CharSequence nome, int tipo, int topo) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = topo; this.baixo = topo;
	}
	
	public Bloco(CharSequence nome, int tipo, int topo, boolean transparente) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = topo; this.baixo = topo;
		this.transparente = transparente;
	}
	
	public Bloco(CharSequence nome, int tipo, int topo, boolean transparente, boolean solido) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = topo; this.baixo = topo;
		this.transparente = transparente;
		this.solido = solido;
	}
	
	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo, boolean transparente) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = lados; this.baixo = baixo;
		this.transparente = transparente;
	}
	
	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo, boolean transparente, boolean solido) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = lados; this.baixo = baixo;
		this.transparente = transparente;
		this.solido = solido;
	}
	
	public Bloco(CharSequence nome, int tipo, int topo, int lados) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo;
		this.lados = lados; this.baixo = topo;
	}
	
	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo;
		this.lados = lados;
		this.baixo = baixo;
	}
	
	public Bloco(String nome, int tipo, int topo, boolean transparente, boolean solido, boolean cullingAlto) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = topo; this.baixo = topo;
		this.transparente = transparente;
		this.solido = solido;
		this.cullingAlto = cullingAlto;
		// this.emissorLuz = (tipo == ChunkUtil.LUZ_TOCHA); // Adicione esta propriedade
	}

// Adicione esta propriedade na classe Bloco
	public boolean emissorLuz = false;
	
	public int textureId(int faceId) {
        switch(faceId) {
            case 0: return topo;   // topo
            case 1: return baixo;  // baixo
            default: return lados;  // lados
        }
    }
	
	public static Bloco criar(int id) {
		for(Bloco b : ChunkUtil.blocos) {
			if(b.tipo == id) return b;
		}
		return null;
	}
}
