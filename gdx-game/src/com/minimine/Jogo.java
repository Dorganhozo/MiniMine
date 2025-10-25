package com.minimine;

import com.badlogic.gdx.Game;
import com.minimine.cenas.Teste;
import com.minimine.cenas.TesteUI;

public class Jogo extends Game {

	@Override
	public void create() {
		setScreen(new Teste());
	}
}
