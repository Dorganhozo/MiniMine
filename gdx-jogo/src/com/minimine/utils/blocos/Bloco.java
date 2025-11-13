package com.minimine.utils.blocos;

import com.minimine.utils.chunks.ChunkUtil;
import java.util.List;
import java.util.ArrayList;

public class Bloco {
	public static List<Bloco> blocos = new ArrayList<>();

	public CharSequence nome;
	public int tipo;
	public int topo, lados, baixo;
	public boolean transparente;
	public boolean solido;
	public boolean cullingAlto;

	public Bloco(CharSequence nome, int tipo, int topo) {this(nome, tipo, topo, topo, topo, false, true, false);}
	public Bloco(CharSequence nome, int tipo, int topo, int lados) {this(nome, tipo,topo, lados, topo, false, true, false);}
	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo) {this(nome, tipo,topo, lados, baixo, false, true, false);}
	public Bloco(CharSequence nome, int tipo, int topo, boolean transparente) {this(nome, tipo, topo, topo, topo, transparente, true, false);}
	public Bloco(CharSequence nome, int tipo, int topo, boolean transparente, boolean solido) {this(nome, tipo, topo, topo, topo, transparente, solido, false);}
	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo, boolean transparente) {this(nome, tipo, topo, lados, baixo, transparente, true, false);}
	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo, boolean transparente, boolean solido) {this(nome, tipo, topo, lados, baixo, transparente, solido, false);}
	public Bloco(String nome, int tipo, int topo, boolean transparente, boolean solido, boolean cullingAlto) {this(nome, tipo, topo, topo, topo, transparente, solido, cullingAlto);}

	public Bloco(CharSequence nome, int tipo, int topo, int lados, int baixo, boolean transparente, boolean solido, boolean cullingAlto) {
		this.nome = nome;
		this.tipo = tipo;
		this.topo = topo; this.lados = lados; this.baixo = baixo;
		this.transparente = transparente;
		this.solido = solido;
		this.cullingAlto = cullingAlto;
	}

	public int texturaId(int faceId) {
        switch(faceId) {
            case 0: return topo;
            case 1: return baixo;
            default: return lados;
        }
    }

	public static Bloco existe(int id) {
		for(Bloco b : blocos) if(b.tipo == id) return b;
		return null;
	}
}
