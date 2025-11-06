package com.minimine.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.minimine.cenas.UI;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Botao implements UI.Evento {
	public Sprite sprite;
	public Rectangle hitbox;
	public String nome;
	public float x, y;
	public float tamX, tamY;
	
	public Botao(Texture tex, float x, float y, float tamX, float tamY, String nome) {
		sprite = new Sprite(tex);
		this.x = x; this.y = y;
		this.tamX = tamX; this.tamY = tamY;
		sprite.setPosition(x, y);
		sprite.setSize(tamX, tamY);
		
		hitbox = new Rectangle(x, y, tamX, tamY);
		
		this.nome = nome;
	}
	
	@Override
	public void porFrame(float delta, SpriteBatch sb, BitmapFont fonte) {
		sprite.draw(sb);
	}
	
	public void defTextura(String nomeId) {
		sprite.setTexture(Texturas.texs.get(nomeId));
	}
	
	public void defPos(float x, float y) {
		sprite.setPosition(x, y);
		hitbox.setPosition(x, y);
	}
	
	public void defTam(float x, float y) {
		sprite.setSize(x, y);
		hitbox.setSize(x, y);
	}
}
