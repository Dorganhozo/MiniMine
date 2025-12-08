package com.minimine.utils.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;
import com.badlogic.gdx.audio.Music;

public class AudioUtil {
	public static HashMap<CharSequence, Music> sons = new HashMap<>();
	public static float volume = 0f;
	
	public static Music addSom(String nome, String caminho) {
		Music som = Gdx.audio.newMusic(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		sons.put(nome, som);
		som.setVolume(volume);
		return som;
	}
	
	public static void liberar() {
		for(Music s : sons.values()) s.dispose();
	}
}
