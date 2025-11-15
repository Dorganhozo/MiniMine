package com.minimine.utils.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;

public class AudioUtil {
	public static HashMap<CharSequence, Sound> sons = new HashMap<>();
	
	public static Sound addSom(String nome, String caminho) {
		Sound som = Gdx.audio.newSound(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		sons.put(nome, som);
		return som;
	}
	
	public static void liberar() {
		for(Sound s : sons.values()) s.dispose();
	}
}
