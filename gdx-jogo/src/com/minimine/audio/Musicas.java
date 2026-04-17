package com.minimine.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class Musicas {
	public static HashMap<CharSequence, String> musicas = new HashMap<>();
	public static ArrayList<CharSequence> nomes = new ArrayList<>();
	public static Music tocando;
	
	public static void iniciar() {
		Musicas.addMusica("igor-2", "audio/musicas/igor-2.ogg");
		Musicas.addMusica("caminho-sombreado", "audio/musicas/caminho-sombreado.ogg");
		Musicas.addMusica("gatitos", "audio/musicas/gatitos.ogg");
		Musicas.addMusica("noite-estrelada", "audio/musicas/noite-estrelada.ogg");
		Musicas.addMusica("flor", "audio/musicas/flor.ogg");
		Musicas.addMusica("liminar", "audio/musicas/liminar.ogg");
		Musicas.addMusica("esperança", "audio/musicas/esperança.ogg");
	}
	
	public static void addMusica(String nome, String caminho) {
		musicas.put(nome, caminho);
		nomes.add(nome);
	}
	
	public static void tocarAleatorio() {
		if(tocando == null || !tocando.isPlaying()) {
			CharSequence nome = nomes.get(MathUtils.random(0, nomes.size() - 1));
			
			Music m = null;
			m = Gdx.audio.newMusic(Gdx.files.internal(musicas.get(nome)));
			tocando = m;
			m.play();
			Gdx.app.log("Musicas", "Tocando "+nome.toString());
		}
	}
	
	public static void pausar() {
		if(tocando == null) return;
		tocando.pause();
		tocando.dispose();
		tocando = null;
	}

	public static void liberar() {
		tocando.dispose();
		tocando = null;
		nomes.clear();
	}
}
