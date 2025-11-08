package com.minimine;

import com.badlogic.gdx.Screen;
import com.minimine.cenas.Menu;
import com.minimine.cenas.Jogo;
import com.minimine.cenas.MundoMenu;

public class Cenas {
	public static Screen menu, jogo, selecao;
	
	static {
		menu = new Menu();
		jogo = new Jogo();
		selecao = new MundoMenu();
	}
}
