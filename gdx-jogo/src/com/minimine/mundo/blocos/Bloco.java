package com.minimine.mundo.blocos;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import com.minimine.audio.Audio;
import com.badlogic.gdx.audio.Music;

public class Bloco {
	public static List<Bloco> blocos = new ArrayList<>();
	public static HashMap<CharSequence, Bloco> texIds = new HashMap<>();
	public static HashMap<Integer, Bloco> numIds = new HashMap<>();
	public static HashMap<String, String[]> sons = new HashMap<>();

	public CharSequence nome;
	public int tipo;
	public String topo, lados, baixo;
	public int luz;
	public boolean transparente;
	public boolean solido, liquido, culling, modeloX;
	public static boolean ABERTO = false;
	/*
	 * interface de UI associada a este bloco
	 * null = bloco sem interface(comportamento padrão: colocar/quebrar)
	 * Atribuída em Bloco.iniciar() para os blocos que precisarem
	 */
	public InterfaceBloco ui = null;
	public EventoBloco evento = null;

	public Bloco(CharSequence nome, String topo) {this(nome, topo, topo);}
	public Bloco(CharSequence nome, String topo, String lados) {this(nome, topo, lados, topo);}
	public Bloco(CharSequence nome, String topo, String lados, String baixo) {this(nome,topo, lados, baixo, false);}
	public Bloco(CharSequence nome, String topo, boolean transparente, boolean solido, boolean culling, int luz) {this(nome, topo, topo, topo, transparente, solido, culling, luz, false);}
	public Bloco(CharSequence nome, String topo, boolean transparente) {this(nome, topo, topo, topo, transparente);}
	public Bloco(CharSequence nome, String topo, boolean transparente, boolean solido) {this(nome, topo, topo, topo, transparente, solido);}
	public Bloco(CharSequence nome, String topo, String lados, String baixo, boolean transparente) {this(nome, topo, lados, baixo, transparente, true);}
	public Bloco(CharSequence nome, String topo, String lados, String baixo, boolean transparente, boolean solido) {this(nome, topo, lados, baixo, transparente, solido, true, 0, false);}
	public Bloco(CharSequence nome, String topo, boolean transparente, boolean solido, boolean culling) {this(nome, topo, topo, topo, transparente, solido, culling, 0, false);}
	public Bloco(CharSequence nome, String topo, boolean transparente, boolean solido, boolean culling, int luz, boolean formaX) {this(nome, topo, topo, topo, transparente, solido, culling, luz, formaX);}

	public Bloco(CharSequence nome, String topo, String lados, String baixo, boolean transparente, boolean solido, boolean culling, int luz, boolean formaX) {
		this.nome = nome;
		this.tipo = blocos.size();
		this.topo = topo; this.lados = lados; this.baixo = baixo;
		this.transparente = transparente;
		this.solido = solido;
		this.culling = culling;
		this.luz = luz;
		this.modeloX = formaX;
		numIds.put(this.tipo, this);
		texIds.put(this.nome, this);
	}

	public static void iniciar() {
		Bloco.add(null);
        Bloco.add(new Bloco("grama", "grama_topo", "grama_lado", "terra"));
        Bloco.add(new Bloco("terra", "terra"));
        Bloco.add(new Bloco("pedra", "pedra"));
        Bloco.add(new Bloco("agua", "agua", true, false, false)).liquido = true;
        Bloco.add(new Bloco("areia", "areia"));
        Bloco.add(new Bloco("tronco", "tronco_topo", "tronco_lado"));
        Bloco.add(new Bloco("folha", "folha", false, true, false));
        Bloco.add(new Bloco("tabua_madeira", "tabua_madeira"));
        Bloco.add(new Bloco("cacto", "cacto_topo", "cacto_lado"));
        Bloco.add(new Bloco("vidro", "vidro", true, true, false));
        Bloco.add(new Bloco("tocha", "tocha", false, true, true, 13));
		Bloco.add(new Bloco("pedregulho", "pedregulho"));
		Bloco.add(new Bloco("cascalho", "cascalho"));
		Bloco.add(new Bloco("gelo", "gelo"));
		Bloco.add(new Bloco("neve", "neve"));
		Bloco.add(new Bloco("coral_rosa", "coral_rosa"));
		Bloco.add(new Bloco("coral_azul", "coral_azul"));
		Bloco.add(new Bloco("coral_amarelo", "coral_amarelo"));
		Bloco.add(new Bloco("capim", "capim", true, false, false, 0, true));
		Bloco.add(new Bloco("tulipa", "tulipa", true, false, false, 0, true));
		Bloco.add(new Bloco("tulipa_luminosa", "tulipa", true, false, false, 5, true));
		Bloco.add(new Bloco("iris_azul", "iris_azul", true, false, false, 1, true));
		Bloco.add(new Bloco("arenito", "arenito"));
		Bloco.add(new Bloco("pilar_arenito", "pilar_arenito_topo", "pilar_arenito_lado"));
		/*
		 * bloco_nulo: marca "ar explícito" dentro de estruturas salvas
		 * transparente=true, solido=false, culling=false para não interferir no mundo
		 */
		Bloco.add(new Bloco("bloco_nulo", "nulo", true, false, false));
		/*
		 * bloco_estrutura: abre interface para definir e salvar estruturas .minies
		 * usa textura de pedra provisoriamente
		 * interface montada em iniciarInterfaces()
		 */
		Bloco.blocos.add(new Bloco("bloco_estrutura", "bloco_estrutura"));

		Bloco.addSom("grama", "grama_1", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("terra", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("areia", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("cascalho", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("pedra", "pedra_1", "pedra_2");
		Bloco.addSom("folha", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("tabua_madeira", "madeira_1", "madeira_2", "madeira_3");
		Bloco.addSom("tocha", "madeira_1", "madeira_2", "madeira_3");
	}
	/*
	 * chamado dentro do construtor de UI, apos visualBase e fonte estarem prontos
	 * cria as instâncias de InterfaceBloco e as injeta nos blocos correspondentes
	 */
	public static void iniciarInterfaces(final com.micro.PainelFatiado base, final com.badlogic.gdx.graphics.g2d.BitmapFont fonte) {
		// bloco_estrutura
		BlocoEstrutura.iniciar(texIds.get("bloco_estrutura"), base, fonte);
		BlocoEstrutura.iniciarEventos(texIds.get("bloco_estrutura"));
	}

	public String texturaId(int faceId) {
        switch(faceId) {
            case 0: return topo;
            case 1: return baixo;
            default: return lados;
        }
    }
	
	public static Bloco add(Bloco b) {
		blocos.add(b);
		return b;
	}

	public static void addSom(String bloco, String... sonoros) {
		sons.put(bloco, sonoros);
	}

	public static void tocarSom(Object bloco) {
		if(sons.containsKey(bloco)) {
			String[] sonoros = sons.get(bloco);
			for(int i = 0; i < sonoros.length; i++) {
				if(Math.random() > 0.6) {
					Music m = Audio.sons.get(sonoros[i]);
					m.play();
					return;
				}
			}
			Music m = Audio.sons.get(sonoros[0]);
			m.play();
		} else {
			tocarSom("pedra");
		}
	}

	public static void liberar() {
		// libera interfaces antes de limpar os mapas
		for(Bloco b : blocos) {
			if(b != null && b.ui != null) {
				b.ui.liberar();
				b.ui = null;
			}
		}
		Bloco.blocos.clear();
		Bloco.numIds.clear();
		Bloco.texIds.clear();
		Bloco.sons.clear();
	}
}
