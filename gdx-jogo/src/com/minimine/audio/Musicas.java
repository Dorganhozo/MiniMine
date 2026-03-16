package com.minimine.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;
import com.badlogic.gdx.audio.Music;

public class Musicas {
	public static HashMap<CharSequence, String> musicas = new HashMap<>();
	public static Music tocando;
	
	public static void iniciar() {
		Musicas.addMusica("igor", "audio/musicas/igor.ogg");
		Musicas.addMusica("igor-2", "audio/musicas/igor-2.ogg");
		Musicas.addMusica("caminho-sombreado", "audio/musicas/caminho-sombreado.ogg");
		Musicas.addMusica("gatitos", "audio/musicas/gatitos.ogg");
		Musicas.addMusica("noite-estrelada", "audio/musicas/noite-estrelada.ogg");
	}
	
	public static void addMusica(String nome, String caminho) {
		musicas.put(nome, caminho);
	}
	
	public static void tocarAleatorio() {
		if(tocando == null || !tocando.isPlaying()) {
			for(Object nome : musicas.keySet()) {
				if(Math.random() > 0.5) {
					Music m = null;
					m = Gdx.audio.newMusic(Gdx.files.internal(musicas.get(nome)));
					tocando = m;
					m.play();
					Gdx.app.log("Musicas", "Tocando "+nome.toString());
					return;
				}
			}
		}
	}
	
	public static void pausar() {
		if(tocando == null) return;
		tocando.pause();
		liberar();
	}

	public static void liberar() {
		tocando.dispose();
		tocando = null;
	}
}
