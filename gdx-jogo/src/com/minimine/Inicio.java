package com.minimine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.cenas.Menu;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.Gdx;

public class Inicio extends Game {
	public static String externo;
	public static boolean telaNova = false;
	public static Screen telaAtual;

	public Inicio(String externo) {
		Inicio.externo = externo;
	}

	@Override
	public void create() {
		defTela(Cenas.menu);
	}

	public static void defTela(Screen tela) {
		telaAtual = tela;
		telaNova = true;
	}

	@Override
	public void render() {
		if(telaNova) {
			setScreen(telaAtual);
			telaNova = false;
		}
		if(telaAtual != null) telaAtual.render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void dispose() {
		super.dispose();
		for(Texture tex : Texturas.texs.values()) {
			tex.dispose();
		}
	}
}
