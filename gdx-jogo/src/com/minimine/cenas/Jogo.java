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
import com.minimine.utils.chunks.ChunkUtil;
import com.minimine.utils.NuvensUtil;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.Texturas;
import com.minimine.utils.CorposCelestes;
import com.minimine.Inicio;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.minimine.mods.Util;
import com.minimine.utils.BiomasUtil;
import com.minimine.Logs;
import com.minimine.JS;
import com.minimine.utils.audio.AudioUtil;
import com.minimine.utils.chunks.Chunk;
import com.badlogic.gdx.graphics.Mesh;

public class Jogo implements Screen {
	public UI ui;
	public static Mundo mundo = new Mundo();
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
		
		LuaAPI.iniciar(this);
		/*
		try {
			MainActivity.ISSO.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Inicio.js.config();
					Inicio.js.API(mundo, "Mundo");
					Inicio.js.API(ui.jogador, "Jogador");
					Inicio.js.API(ui, "Ui");
					Inicio.js.API(new Util(), "Util");
					Inicio.js.API(new BiomasUtil(), "Biomas");
					Inicio.js.API(new ChunkUtil(), "ChunkUtil");
					Inicio.js.API(new Texturas(), "TexUtil");
					Inicio.js.API(new NuvensUtil(), "Nuvens");
					Inicio.js.API(new DiaNoiteUtil(), "Ciclo");
					Inicio.js.API(new Gdx(), "Gdx");
					Inicio.js.API(new LuaAPI(), "Lua");
					Inicio.js.API(new ArquivosUtil(), "Arquivos");

					Inicio.js.iniciar(Inicio.externo+"/MiniMine/mods/arquivos.html");
				}
			});
		} catch(Exception e) {
			Logs.log("JAVASCRIPT API: [ERRO]: "+e.getMessage());
		}
		*/
		if(ArquivosUtil.existe(Inicio.externo+"/MiniMine/mundos/"+mundo.nome+".mini")) ArquivosUtil.crMundo(mundo, jogador);
		
		mundo.iniciar();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		mb = new ModelBatch();
		ambiente = new Environment();
		ambiente.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
		ambiente.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));
		
		AudioUtil.sons.put("grama_1", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/grama_1.mp3")));
		AudioUtil.sons.put("terra_1", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/terra_1.mp3")));
		AudioUtil.sons.put("terra_2", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/terra_2.mp3")));
		AudioUtil.sons.put("terra_3", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/terra_3.mp3")));
		AudioUtil.sons.put("pedra_1", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/pedra_1.mp3")));
		AudioUtil.sons.put("pedra_2", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/pedra_2.mp3")));
		AudioUtil.sons.put("madeira_1", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/madeira_1.mp3")));
		AudioUtil.sons.put("madeira_2", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/madeira_2.mp3")));
		AudioUtil.sons.put("madeira_3", Gdx.audio.newMusic(Gdx.files.internal("audio/blocos/madeira_3.mp3")));
		
		pronto = true;
		
		new java.util.Timer().schedule(
			new java.util.TimerTask() {
				@Override
				public void run() {
					if(mundo.ciclo) DiaNoiteUtil.att();
				}
			},
			0, 120
		);
	}

    @Override
	public void render(float delta) {
		float fator = DiaNoiteUtil.obterFatorTransicao();
		float[] corNoite = {0.05f, 0.05f, 0.15f};
		float[] corDia = {0.5f * DiaNoiteUtil.luz, 0.7f * DiaNoiteUtil.luz, 1.0f * DiaNoiteUtil.luz};

		float r = corNoite[0] * (1f - fator) + corDia[0] * fator;
		float g = corNoite[1] * (1f - fator) + corDia[1] * fator;
		float b = corNoite[2] * (1f - fator) + corDia[2] * fator;

		Gdx.gl.glClearColor(r, g, b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		if(pronto) mundo.att(delta, jogador);
		
		if(jgs.size() >= 1) {
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
		mundo.liberar();
		net.liberar();
		CorposCelestes.liberar();
    }
	
	@Override
	public void resize(int v, int h) {
		ui.ajustar(v, h);
		Gdx.gl.glViewport(0, 0, v, h);
		LuaAPI.ajustar(v, h);
	}

	@Override public void hide() {
		mundo.carregado = false;
		for(Chunk c : mundo.chunks.values()) {
			if(c.mesh != null) c.mesh.dispose();
			c.mesh = null;
		}
		mundo.chunks.clear();
		ArquivosUtil.svMundo(mundo, jogador);
		mundo.carregado = true;
	}
	@Override
	public void pause() {
		LuaAPI.iniciar(this);	
		mundo.carregado = false;
		for(Chunk c : mundo.chunks.values()) {
			if(c.mesh != null) c.mesh.dispose();
			c.mesh = null;
		}
		mundo.chunks.clear();
		ArquivosUtil.svMundo(mundo, jogador);
		mundo.carregado = true;
	}
	@Override public void resume() {}
}
