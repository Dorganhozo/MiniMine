package com.minimine.cenas;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.util.List;
import com.minimine.ui.Botao;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Gdx;
import com.minimine.ui.Texto;
import com.minimine.Cenas;
import com.minimine.Inicio;
import com.badlogic.gdx.graphics.GL20;
import com.minimine.utils.Texturas;

public class Menu implements Screen, InputProcessor {
	public static SpriteBatch sb;
    public static BitmapFont fonte;
	public List<Texto> textos = new ArrayList<>();
	public List<Botao> botoes = new ArrayList<>();
	public float botaoTam = 130;
	
	@Override
	public void show() {
		sb = new SpriteBatch(); 

		fonte = new BitmapFont();
		fonte.getData().setScale(1.5f);
		Gdx.input.setInputProcessor(this);
		
		botoes.add(new Botao(Texturas.texs.get("botao_opcao"), 0, 0, 260*2, 130, "irJogo") {
			@Override
			public void aoTocar(int tx, int ty, int p) {
				Inicio.defTela(Cenas.jogo);
			}
			@Override
			public void aoAjustar(int v, int h) {
				defPos((v - tamX) / 2, (h - tamY) / 2);
			}
		});
		textos.add(new Texto("teste", 100, 500));
	}
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		sb.begin();
		for(Botao b : botoes) {
			b.porFrame(delta, sb, fonte);
		}
		for(Texto t : textos) {
			t.porFrame(delta, sb, fonte);
		}
		sb.end();
	}
	@Override
	public void resize(int v, int h) {
		for(int i = 0; i < botoes.size(); i++) {
			if(botoes.get(i) != null) botoes.get(i).aoAjustar(v, h);
		}
	}
	@Override
	public void dispose() {
		sb.dispose();
		fonte.dispose();
	}
	
	@Override
	public boolean touchDown(int telaX, int telaY, int p, int b) {
		int y = Gdx.graphics.getHeight() - telaY;
		for(Botao bt : botoes) {
			if(bt.hitbox.contains(telaX, y)) {
				bt.aoTocar(telaX, y, p);
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int p, int p1, int p2) {

		return false;
	}

	@Override
	public boolean touchUp(int p, int p1, int p2, int p3) {

		return false;
	}
	
	@Override public void hide(){}
	@Override public void pause(){}
	@Override public void resume(){}
	@Override public boolean keyDown(int p){return false;}
	@Override public boolean keyTyped(char p){return false;}
	@Override public boolean keyUp(int p){return false;}
	@Override public boolean mouseMoved(int p, int p1){return false;}
	@Override public boolean scrolled(float p, float p1){return false;}
}
