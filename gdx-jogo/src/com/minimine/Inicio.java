package com.minimine;

import com.badlogic.gdx.Game;
import com.minimine.cenas.Jogo;
import com.minimine.cenas.TesteUI;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.Texture;

public class Inicio extends Game {
	public static String externo;
	
	public Inicio(String externo) {
		this.externo = externo;
	}
	@Override
	public void create() {
		setScreen(new Jogo());
	}

	@Override
	public void dispose() {
		super.dispose();
		for(Texture tex : Texturas.texs.values()) {
			tex.dispose();
		}
	}
}
