package com.minimine.comandos;

import com.minimine.graficos.Render;
import com.minimine.cenas.Jogo;
import com.minimine.entidades.Jogador;
import com.badlogic.gdx.Gdx;

public class Comandos {
	public static void exec(String comando) {
		String[] partes = comando.split(" ");
		try {
			analisar(partes);
		} catch(Exception e) {
			Gdx.app.log("Comandos", "[ERRO] "+e);
		}
	}
	public static void analisar(String[] partes) {
		final Render render = Jogo.render;
		
		switch(partes[0]) {
			case "/jogador":
				Jogador jg = render.ui.jg;
				if(partes[1].equals("pos")) {
					if(partes[2].equals("def")) {
						jg.posicao.set(
						partes[3].equals("~") ? jg.posicao.x : Float.valueOf(partes[3]),
						partes[4].equals("~") ? jg.posicao.y : Float.valueOf(partes[4]),
						partes[5].equals("~") ? jg.posicao.z : Float.valueOf(partes[5])
						);
					} else if(partes[2].equals("add")) {
						jg.posicao.add(
						partes[3].equals("~") ? jg.posicao.x : Float.valueOf(partes[3]),
						partes[4].equals("~") ? jg.posicao.y : Float.valueOf(partes[4]),
						partes[5].equals("~") ? jg.posicao.z : Float.valueOf(partes[5])
						);
					} else if(partes[2].equals("sub")) {
						jg.posicao.sub(
							partes[3].equals("~") ? jg.posicao.x : Float.valueOf(partes[3]),
							partes[4].equals("~") ? jg.posicao.y : Float.valueOf(partes[4]),
							partes[5].equals("~") ? jg.posicao.z : Float.valueOf(partes[5])
						);
					}
				} else if(partes[1].equals("velo")) {
					if(partes[2].equals("def")) jg.velo = Float.valueOf(partes[3]);
					else if(partes[2].equals("add")) jg.velo += Float.valueOf(partes[3]);
					else if(partes[2].equals("sub")) jg.velo -= Float.valueOf(partes[3]);
				} else if(partes[1].equals("pulo")) {
					if(partes[2].equals("def")) jg.pulo = Float.valueOf(partes[3]);
					else if(partes[2].equals("add")) jg.pulo += Float.valueOf(partes[3]);
					else if(partes[2].equals("sub")) jg.pulo -= Float.valueOf(partes[3]);
				} else if(partes[1].equals("modo")) jg.modo = Integer.valueOf(partes[2]);
			break;
		}
	}
}
