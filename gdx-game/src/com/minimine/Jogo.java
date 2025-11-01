package com.minimine;

import com.badlogic.gdx.Game;
import com.minimine.cenas.Teste;
import com.minimine.cenas.TesteUI;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.Texture;

public class Jogo extends Game {
	@Override
	public void create() {
		setScreen(new Teste());
	}

	@Override
	public void dispose() {
		super.dispose();
		for(Texture tex : Texturas.texs.values()) {
			tex.dispose();
		}
	}
}
