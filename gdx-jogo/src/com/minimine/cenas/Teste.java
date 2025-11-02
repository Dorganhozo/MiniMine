package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.minimine.utils.Net;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.utils.BiomasUtil;
import com.minimine.utils.ruidos.PerlinNoise2D;
import com.minimine.utils.ChunkUtil;
import com.minimine.utils.EstruturaUtil;
import com.minimine.mods.LuaAPI;

public class Teste implements Screen {
	public UI ui;
	public Mundo mundo;
	public Jogador jogador = new Jogador();
	public Net net;

    @Override
	public void show() {
        mundo = new Mundo();
		ui = new UI(jogador);
		net = new Net(Net.SERVIDOR_MODO);
		
		LuaAPI.iniciar(this);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_BLEND);
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		mundo.att(delta, jogador);
		if(mundo.carregado) jogador.att(delta);
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		ui.att(delta, mundo);
    }

    @Override
    public void dispose() {
		mundo.liberar();
		ui.liberar();
		net.liberar();
    }
	
	@Override
	public void resize(int v, int h) {
		ui.ajustar(v, h);
		Gdx.gl.glViewport(0, 0, v, h);
	}

	@Override public void hide() {}
	@Override
	public void pause() {
		mundo.carregado = false;
	}
	@Override public void resume() {}
}
