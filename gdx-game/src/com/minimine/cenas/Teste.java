package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;

public class Teste implements Screen {
	public UI ui;
	public Mundo mundo;
	public static boolean rodando = true;
	public Jogador jogador = new Jogador();

    @Override
	public void show() {
        mundo = new Mundo();
		ui = new UI(jogador);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_BLEND);
	}

    @Override
    public void render(float delta) {
		if(!rodando) return;
        Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		mundo.att(delta, jogador);
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		ui.att(delta, mundo);
    }

    @Override
    public void dispose() {
		mundo.liberar();
		ui.liberar();
    }
	
	@Override
	public void resize(int v, int h) {
		ui.ajustar(v, h);
		Gdx.gl.glViewport(0, 0, v, h);
	}

	@Override public void hide() {}
	@Override
	public void pause() {
		rodando = false;
		for(Chunk chunk : mundo.chunks.values()) {
			if(chunk != null) mundo.meshReuso.free(chunk.mesh);
		}
		mundo.chunks.clear();
		for(int i = 0; i < mundo.RAIO_CHUNKS+5; i++) {
			mundo.meshReuso.obtain();
		}
		rodando = true;
	}
	@Override public void resume() {}
}
