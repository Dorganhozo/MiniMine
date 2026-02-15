package com.minimine.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;
import com.badlogic.gdx.audio.Music;

public class Musicas {
	public static HashMap<CharSequence, Music> musicas = new HashMap<>();
	public static Music tocando;
	
	public static Music addMusica(String nome, String caminho) {
		Music m = null;
		if(caminho.startsWith("/")) m = Gdx.audio.newMusic(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods"+caminho));
		else m = Gdx.audio.newMusic(Gdx.files.internal(caminho));
		musicas.put(nome, m);
		return m;
	}
	
	public static void tocarAleatorio() {
		if(tocando == null || !tocando.isPlaying()) {
			for(Music m : musicas.values()) {
				double chance = Math.random();
				if(chance > 0.5) {
					tocando = m;
					m.play();
					Gdx.app.log("[Musicas]", "Tocando com chance "+chance);
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
