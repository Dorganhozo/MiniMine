package com.minimine.cenas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

public class Inventario {
	public int quantSlots = 25;
	public int slotsV = 5, slotsH = 5;
	public int tamSlot = 64;
	public Texture texSlot;
	public Sprite[] sprites;
	public Rectangle[] rects;
	public int invX = 500, invY = 500;
	
	public Inventario() {
		int v = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		invX = v / 2;
		invY = h / 2;
		ajustar(v, h);
	}

	public void ajustar(int v, int h) {
		texSlot = new Texture(Gdx.files.internal("ui/slot.png"));
		sprites = new Sprite[quantSlots];
		rects = new Rectangle[quantSlots];

		int i = 0;
		for(int y = 0; y < slotsV; y++) {
			for(int x = 0; x < slotsH; x++) {
				if(i >= quantSlots) break;
				Sprite s = new Sprite(texSlot);
				s.setSize(tamSlot, tamSlot);
				s.setPosition(invX + (x * tamSlot), invY + (y * tamSlot));
				Rectangle rect = new Rectangle(
					s.getX(), 
					s.getY(), 
					s.getWidth(), 
					s.getHeight()
				);
				sprites[i] = s;
				rects[i] = rect;
				i++;
			}
		}
	}
	
	public void toque(int telaX, int telaY, int p, int b, Jogador jogador) {
		for(int i = 0; i < rects.length; i++) {
			if(rects[i].contains(telaX, telaY)) {
				if(jogador.blocoSele == 0) jogador.blocoSele = 1;
				else if(jogador.blocoSele == 1) jogador.blocoSele = 2;
				else if(jogador.blocoSele == 2) jogador.blocoSele = 3;
				else if(jogador.blocoSele == 3) jogador.blocoSele = 0;
			}
		}
	}

	public void att() {
		for(int i = 0; i < sprites.length; i++) {
			sprites[i].draw(UI.sb);
		}
	}
}
