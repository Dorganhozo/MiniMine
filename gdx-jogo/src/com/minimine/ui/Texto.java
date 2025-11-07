package com.minimine.ui;

import com.minimine.cenas.UI;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.minimine.utils.InterUtil;

public class Texto extends InterUtil.Objeto {
	public String texto;
	public float x, y;
	
	public Texto(String texto, float x, float y) {
		super(texto);
		this.texto = texto;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void porFrame(float delta, SpriteBatch sb, BitmapFont fonte) {
		fonte.draw(sb, texto, x, y);
	}
}
