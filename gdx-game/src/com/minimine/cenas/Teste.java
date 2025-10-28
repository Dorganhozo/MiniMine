package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.minimine.cenas.blocos.Luz;

public class Teste implements Screen {
	public UI ui;
	public Mundo mundo;

    @Override
	public void show() {
        mundo = new Mundo();
		ui = new UI();
		
		// Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		mundo.att(delta, ui.camera);
		
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
	@Override public void pause() {}
	@Override public void resume() {}
}
