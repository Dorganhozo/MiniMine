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
import com.minimine.utils.audio.AudioUtil;

public class Inicio extends Game {
	public static boolean ehArm64;
	public static String externo;
	public static boolean telaNova = false;
	public static Screen telaAtual;
	public static JS js;
	public static Logs log = new Logs();
	
	public Inicio(String externo, Debugador debugador, JS js) {
		Inicio.externo = externo;
		Inicio.js = js;
		UI.debugador = debugador;
		ehArm64 = debugador.ehArm64();
		ehArm64 = false;
	}

	@Override
	public void create() {
		Gdx.app.setApplicationLogger(log);
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
			Mundo.liberar();
			UI.liberar();
			NuvensUtil.liberar();
			CorposCelestes.liberar();
			AudioUtil.liberar();
		} catch(Exception e) {
			Gdx.app.log("Inicio", "[ERRO] ao liberar: "+e);
		}
	}
}
