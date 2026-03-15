package com.minimine.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;
import com.badlogic.gdx.audio.Music;

public class Musicas {
	public static HashMap<CharSequence, Music> musicas = new HashMap<>();
	public static Music tocando;
	
	public static void iniciar() {
		Musicas.addMusica("igor", "audio/musicas/igor.ogg");
		Musicas.addMusica("igor-2", "audio/musicas/igor-2.ogg");
		Musicas.addMusica("caminho-sombreado", "audio/musicas/caminho-sombreado.ogg");
		Musicas.addMusica("gatitos", "audio/musicas/gatitos.ogg");
	}
	
	public static Music addMusica(String nome, String caminho) {
		Music m = null;
		if(caminho.startsWith("/")) m = Gdx.audio.newMusic(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods"+caminho));
		else m = Gdx.audio.newMusic(Gdx.files.internal(caminho));
		musicas.put(nome, m);
		return m;
	}
	
	public static void tocarAleatorio() {
		if(tocando == null || !tocando.isPlaying()) {
			for(Object nome : musicas.keySet()) {
				Music m = musicas.get(nome);
				if(Math.random() > 0.5) {
					tocando = m;
					m.play();
					Gdx.app.log("Musicas", "Tocando "+nome.toString());
					return;
				}
			}
		}
	}
	
	public static void pausarTodas() {
		for(Music m : musicas.values()) m.pause();
		tocando = null;
	}
	
	public static void defVolume(int volume) {
		for(Music m : musicas.values()) m.setVolume(volume);
	}

	public static void liberar() {
		for(Music m : musicas.values()) m.dispose();
	}
}
