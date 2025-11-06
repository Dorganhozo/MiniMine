package com.minimine;

import com.badlogic.gdx.Screen;
import com.minimine.cenas.Menu;
import com.minimine.cenas.Jogo;

public class Cenas {
	public static Screen menu, jogo;
	
	static {
		menu = new Menu();
		jogo = new Jogo();
	}
}
