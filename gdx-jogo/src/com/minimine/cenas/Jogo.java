package com.minimine.cenas;

import com.badlogic.gdx.Screen;
import com.minimine.utils.Net;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import java.util.List;
import com.minimine.utils.ArquivosUtil;
import java.util.ArrayList;
import com.minimine.mods.LuaAPI;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.minimine.utils.ChunkUtil;

public class Jogo implements Screen {
	public UI ui;
	public Mundo mundo;
	public Jogador jogador = new Jogador();
	public Net net;
	public static boolean pronto = false;
	public Environment ambiente;
	public ModelBatch mb;
	public List<Jogador> jgs = new ArrayList<>();

    @Override
	public void show() {
        mundo = new Mundo();
		ui = new UI(jogador);
		net = new Net(Net.SERVIDOR_MODO);
		
		ArquivosUtil.crMundo(mundo);
		LuaAPI.iniciar(this);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		mb = new ModelBatch();
		ambiente = new Environment();
		jgs.add(jogador);
		pronto = true;
	}

    @Override
    public void render(float delta) {
		float luz = ChunkUtil.LUZ_SOL;
		if(luz < 0f) luz = 0f;
		if(luz > 1f) luz = 1f;

		float r = 0.5f * luz;
		float g = 0.7f * luz;
		float b = 1.0f * luz;

		Gdx.gl.glClearColor(r, g, b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		if(pronto) mundo.att(delta, jogador);
		for(Jogador jo : jgs) {
			if(jogador.modelo != null) {
				jo.criarModelo3D();
			}
			mb.begin(ui.camera);

			jo.modelo.transform.setToTranslation(
				jo.camera.position.x + jo.camera.direction.x * 0.5f + 0.3f,
				jo.camera.position.y + jo.camera.direction.y * 0.5f + 1.2f, 
				jo.camera.position.z + jo.camera.direction.z * 0.5f + 0.3f
			);
			jo.modelo.transform.rotate(0, 1, 0, -ui.yaw);
			jo.modelo.transform.rotate(1, 0, 0, -ui.tom);

			mb.render(jo.modelo, ambiente);
			mb.end();
		}
		if(mundo.carregado) jogador.att(delta);
		if(pronto) LuaAPI.att(delta);
		
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
		ArquivosUtil.svMundo(mundo);
	}
	@Override public void resume() {}
}
