package com.minimine;

import com.badlogic.gdx.Screen;
import com.minimine.cenas.Menu;
import com.minimine.cenas.Jogo;
import com.minimine.cenas.MundoMenu;
import com.minimine.cenas.Intro;

public class Cenas {
	public static Screen menu, jogo, selecao, intro;
	
	static {
		intro = new Intro();
		menu = new Menu();
		jogo = new Jogo();
		selecao = new MundoMenu();
	}
}
