package com.minimine.cenas;

public class Bloco {
	public CharSequence nome;
	public byte tipo;
	public int topo_id, lado_id = topo_id, baixo_id = topo_id;
	
	public Bloco(CharSequence nome, byte tipo, int topo_id) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo_id = topo_id;
	}
}
