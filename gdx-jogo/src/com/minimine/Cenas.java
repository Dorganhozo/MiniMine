package com.minimine;

import com.badlogic.gdx.Screen;
import com.minimine.cenas.Menu;
import com.minimine.cenas.Jogo;
import com.minimine.cenas.MundoMenu;
import com.minimine.cenas.Intro;
import com.minimine.cenas.Config;

public class Cenas {
	public static Screen menu, jogo, selecao, intro, configuracoes;
	
	static {
		intro = new Intro();
		menu = new Menu();
		jogo = new Jogo();
		selecao = new MundoMenu();
		configuracoes = new Config();
	}
	
	public static void mudarCena(Screen cena) {
		Inicio.defTela(cena);
	}
}
