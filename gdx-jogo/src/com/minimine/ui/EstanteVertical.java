package com.minimine.ui;

import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EstanteVertical extends InterUtil.Objeto {
	public int x, y;
    public List<InterUtil.Objeto> filhos = new ArrayList<>();
    public float espaco = 10f;
	
	public EstanteVertical(String nome, int x, int y) {
		this(nome, x, y, 10f);
	}
	public EstanteVertical(String nome, int x, int y, float espaco) {
		super(nome);
		this.x = x;
		this.y = y;
		this.espaco = espaco;
	}
	
    public void add(InterUtil.Objeto obj) {
        filhos.add(obj);
        att();
	}
	
	public void rm(String nome) {
		for(InterUtil.Objeto o : filhos) {
			if(o.nome.equals(nome)) filhos.remove(o);
		}
        att();
	}
	
	@Override
	public void porFrame(float delta, SpriteBatch sb, BitmapFont fonte) {
		for(InterUtil.Objeto o : filhos) {
			o.porFrame(delta, sb, fonte);
		}
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
