package com.minimine.ui;

import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EstanteVertical {
	public int x, y;
    public List<InterUtil.Objeto> filhos = new ArrayList<>();
    public float espaco = 10f;
	
	public EstanteVertical(int x, int y) {
		this(x, y, 10f);
	}
	public EstanteVertical(int x, int y, float espaco) {
		this.x = x;
		this.y = y;
		this.espaco = espaco;
	}
	
    public void add(InterUtil.Objeto obj) {
        filhos.add(obj);
        att();
	}

    public void att() {
        float yAtual = this.y;
        for(InterUtil.Objeto obj : filhos) {
            if(obj instanceof Botao) {
                Botao bt = (Botao) obj;
                bt.defPos(this.x, yAtual);
                yAtual -= bt.tamY + espaco; // proximo vai pra baixo
            }
        }
    }
}
