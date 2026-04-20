package com.minimine.audio;

import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import java.util.HashMap;
import com.badlogic.gdx.audio.Sound;

public class Audio {
	public static HashMap<CharSequence, Sound> sons = new HashMap<>();
	
	public static void iniciar() {
		// blocos:
		Audio.addSom("grama_1", "audio/blocos/grama_1.mp3");
		Audio.addSom("terra_1", "audio/blocos/terra_1.mp3");
		Audio.addSom("terra_2", "audio/blocos/terra_2.mp3");
		Audio.addSom("terra_3", "audio/blocos/terra_3.mp3");
		Audio.addSom("pedra_1", "audio/blocos/pedra_1.mp3");
		Audio.addSom("pedra_2", "audio/blocos/pedra_2.mp3");
		Audio.addSom("madeira_1", "audio/blocos/madeira_1.mp3");
		Audio.addSom("madeira_2", "audio/blocos/madeira_2.mp3");
		Audio.addSom("madeira_3", "audio/blocos/madeira_3.mp3");
	}
	
	public static Sound addSom(String nome, String caminho) {
		Sound s = null;
		if(caminho.startsWith("/")) s = Gdx.audio.newSound(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods"+caminho));
		else s = Gdx.audio.newSound(Gdx.files.internal(caminho));
		sons.put(nome, s);
		return s;
	}

	public static void liberar() {
		for(Sound s : sons.values()) s.dispose();
	}
}
