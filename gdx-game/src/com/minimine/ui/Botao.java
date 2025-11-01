package com.minimine.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.minimine.cenas.UI;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Botao implements UI.Evento {
	public Sprite sprite;
	public Rectangle hitbox;
	public String nome;
	
	public Botao(Texture tex, float x, float y, float tamX, float tamY, String nome) {
		sprite = new Sprite(tex);
		sprite.setPosition(x, y);
		sprite.setSize(tamX, tamY);
		
		hitbox = new Rectangle(
		sprite.getX(), sprite.getY(),
		sprite.getWidth(), sprite.getHeight());
		
		this.nome = nome;
	}
	
	@Override
	public void porFrame(float delta, SpriteBatch sb) {
		sprite.draw(sb);
	}
}
