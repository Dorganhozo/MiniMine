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
import com.minimine.utils.NuvensUtil;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.Texturas;
import com.minimine.utils.CorposCelestes;

public class Jogo implements Screen {
	public UI ui;
	public Mundo mundo = new Mundo();
	public Jogador jogador = new Jogador();
	public Net net;
	public static boolean pronto = false;
	public Environment ambiente;
	public ModelBatch mb;
	public List<Jogador> jgs = new ArrayList<>();
	
    @Override
	public void show() {
		mundo.ciclo = true;
		ui = new UI(jogador);
		// net = new Net(Net.SERVIDOR_MODO);
		
		ArquivosUtil.crMundo(mundo, jogador);
		
		LuaAPI.iniciar(this);
		
		mundo.iniciar();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		mb = new ModelBatch();
		ambiente = new Environment();
		
		pronto = true;
	}

    @Override
	public void render(float delta) {
		DiaNoiteUtil.att();

		float fator = DiaNoiteUtil.obterFatorTransicao();
		float[] corNoite = {0.05f, 0.05f, 0.15f};
		float[] corDia = {0.5f * DiaNoiteUtil.luz, 0.7f * DiaNoiteUtil.luz, 1.0f * DiaNoiteUtil.luz};

		float r = corNoite[0] * (1f - fator) + corDia[0] * fator;
		float g = corNoite[1] * (1f - fator) + corDia[1] * fator;
		float b = corNoite[2] * (1f - fator) + corDia[2] * fator;

		Gdx.gl.glClearColor(r, g, b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		if(pronto) mundo.att(delta, jogador);
		
		if(jgs.size() > 1) {
			for(Jogador jo : jgs) {
				mb.begin(ui.camera);
				if(jo.modelo == null) jo.criarModelo3D();

				jo.modelo.transform.setToTranslation(
					jo.camera.position.x + jo.camera.direction.x * 0.5f + 0.3f,
					jo.camera.position.y + jo.camera.direction.y * 0.5f + 1.2f, 
					jo.camera.position.z + jo.camera.direction.z * 0.5f + 0.3f
				);
				jo.modelo.transform.rotate(0, 1, 0, -jogador.yaw);
				jo.modelo.transform.rotate(1, 0, 0, -jogador.tom);

				mb.render(jo.modelo, ambiente);
				mb.end();
			}
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		}
		if(mundo.carregado) jogador.att(delta);
		if(pronto) LuaAPI.att(delta);
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		ui.att(delta, mundo);
    }

    @Override
    public void dispose() {
		mundo.carregado = false;
		ArquivosUtil.svMundo(mundo, jogador);
		mundo.liberar();
		net.liberar();
    }
	
	@Override
	public void resize(int v, int h) {
		ui.ajustar(v, h);
		Gdx.gl.glViewport(0, 0, v, h);
		LuaAPI.ajustar(v, h);
	}

	@Override public void hide() {
		mundo.carregado = false;
		ArquivosUtil.svMundo(mundo, jogador);
	}
	@Override
	public void pause() {
		LuaAPI.iniciar(this);
		mundo.carregado = false;
		ArquivosUtil.svMundo(mundo, jogador);
	}
	@Override public void resume() {}
}
