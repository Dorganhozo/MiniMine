package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.minimine.Controles;
import com.minimine.cenas.blocos.Luz;

public class Teste implements Screen {
	public UI ui;
	public Mundo mundo;

    @Override
	public void show() {
        mundo = new Mundo();
		
		mundo.attChunks(0, 0);
		
		mundo.chunks.get("1,1").addLuz(new Luz(7, 15, 7, new Color(1.0f, 0.0f, 0.0f, 1.0f)));
		mundo.chunks.get("0,0").addLuz(mundo.player);
		
		mundo.attChunks(0, 0);
		ui = new UI();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		mundo.att(delta, ui.camera);
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		ui.att(delta);
    }

    @Override
    public void dispose() {
		mundo.liberar();
		ui.liberar();
    }
	
	@Override
	public void resize(int v, int h) {
		ui.ajustar(v, h);
	}

	@Override public void hide() {}
	@Override public void pause() {}
	@Override public void resume() {}
}
