package com.minimine.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;
import com.badlogic.gdx.audio.Music;

public class Audio {
	public static HashMap<CharSequence, Music> sons = new HashMap<>();
	
	public static Music addSom(String nome, String caminho) {
		Music s = null;
		if(caminho.startsWith("/")) s = Gdx.audio.newMusic(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods"+caminho));
		else s = Gdx.audio.newMusic(Gdx.files.internal(caminho));
		sons.put(nome, s);
		return s;
	}

	public static void liberar() {
		for(Music s : sons.values()) s.dispose();
	}
}
