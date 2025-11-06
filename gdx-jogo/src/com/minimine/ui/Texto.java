package com.minimine.ui;

import com.minimine.cenas.UI;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Texto implements UI.Evento {
	public String texto;
	public float x, y;
	
	public Texto(String texto, float x, float y) {
		this.texto = texto;
		this.x = x;
		this.y = y;
	}

	@Override
	public void porFrame(float delta, SpriteBatch sb, BitmapFont fonte) {
		fonte.draw(sb, texto, x, y);
	}
}
