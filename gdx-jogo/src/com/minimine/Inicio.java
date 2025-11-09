package com.minimine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.cenas.Menu;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.Gdx;
import com.minimine.cenas.UI;
import com.minimine.utils.NuvensUtil;
import com.minimine.utils.CorposCelestes;
import com.minimine.cenas.Mundo;

public class Inicio extends Game {
	public static int versao = 00001; // 0.0.0.0.1
	public static String externo;
	public static boolean telaNova = false;
	public static Screen telaAtual;

	public Inicio(String externo) {
		Inicio.externo = externo;
	}

	@Override
	public void create() {
		Gdx.app.setApplicationLogger(UI.logs);
		defTela(Cenas.intro);
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
		try {
			for(Texture tex : Texturas.texs.values()) {
				tex.dispose();
			}
			UI.liberar();
			NuvensUtil.liberar();
			CorposCelestes.liberar();
		} catch(Exception e) {
			Gdx.app.log("Inicio", "[ERRO] ao liberar: "+e);
		}
	}
}
