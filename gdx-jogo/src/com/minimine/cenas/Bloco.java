package com.minimine.cenas;

public class Bloco {
	public CharSequence nome;
	public int tipo;
	public int topo, lados, baixo;
	public boolean transparente = false;
	public boolean solido = true;
	
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
}
