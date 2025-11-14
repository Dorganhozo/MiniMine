package com.minimine.utils.blocos;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Bloco {
	public static List<Bloco> blocos = new ArrayList<>();
	public static HashMap<CharSequence, Bloco> texIds = new HashMap<>();
	public static HashMap<Integer, Bloco> numIds = new HashMap<>();
	public static Bloco[] blocosId;
	
	public CharSequence nome;
	public int tipo;
	public int topo, lados, baixo;
	public boolean transparente;
	public boolean solido;
	public boolean cullingAlto;

	public Bloco(CharSequence nome, int topo) {this(nome, topo, topo, topo, false, true, false);}
	public Bloco(CharSequence nome, int topo, int lados) {this(nome, topo, lados, topo, false, true, false);}
	public Bloco(CharSequence nome, int topo, int lados, int baixo) {this(nome,topo, lados, baixo, false, true, false);}
	public Bloco(CharSequence nome, int topo, boolean transparente) {this(nome, topo, topo, topo, transparente, true, false);}
	public Bloco(CharSequence nome, int topo, boolean transparente, boolean solido) {this(nome, topo, topo, topo, transparente, solido, false);}
	public Bloco(CharSequence nome, int topo, int lados, int baixo, boolean transparente) {this(nome, topo, lados, baixo, transparente, true, false);}
	public Bloco(CharSequence nome, int topo, int lados, int baixo, boolean transparente, boolean solido) {this(nome, topo, lados, baixo, transparente, solido, false);}
	public Bloco(CharSequence nome, int topo, boolean transparente, boolean solido, boolean cullingAlto) {this(nome, topo, topo, topo, transparente, solido, cullingAlto);}

	public Bloco(CharSequence nome, int topo, int lados, int baixo, boolean transparente, boolean solido, boolean cullingAlto) {
		this.nome = nome;
		this.tipo = blocos.size()+1;
		this.topo = topo; this.lados = lados; this.baixo = baixo;
		this.transparente = transparente;
		this.solido = solido;
		this.cullingAlto = cullingAlto;
		
		blocosId = new Bloco[blocos.size()];
		for(int i = 0; i < blocosId.length; i++) {
			blocosId[i] = blocos.get(i);
		}
		numIds.put(this.tipo, this);
		texIds.put(this.nome, this);
	}

	public int texturaId(int faceId) {
        switch(faceId) {
            case 0: return topo;
            case 1: return baixo;
            default: return lados;
        }
    }

	public static Bloco existe(int id) {
		for(int i = 0; i < blocosId.length; i++) if(blocosId[i].tipo == id) return blocosId[i];
		return null;
	}
}
