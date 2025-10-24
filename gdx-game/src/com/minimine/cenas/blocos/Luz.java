package com.minimine.cenas.blocos;

import com.badlogic.gdx.graphics.Color;

public class Luz {
	public int x = 0, y = 0, z = 0, nivel = 1, raio = 5;
	public Color cor = new Color(1f, 0f, 0f, 1f);
	
	public Luz() {}
	public Luz(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Luz(int x, int y, int z, Color cor) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.cor = cor;
	}
	public Luz(int x, int y, int z, Color cor, int nivel) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.cor = cor;
		this.nivel = nivel;
	}
}
