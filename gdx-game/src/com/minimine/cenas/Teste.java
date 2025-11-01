package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.minimine.utils.Net;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.Texture;

public class Teste implements Screen {
	public UI ui;
	public Mundo mundo;
	public Jogador jogador = new Jogador();
	public Net net;

    @Override
	public void show() {
		Texturas.texs.put("grama_topo", new Texture(Gdx.files.internal("blocos/grama_topo.png")));
		Texturas.texs.put("grama_lado", new Texture(Gdx.files.internal("blocos/grama_lado.png")));
		Texturas.texs.put("terra", new Texture(Gdx.files.internal("blocos/terra.png")));
		Texturas.texs.put("pedra", new Texture(Gdx.files.internal("blocos/pedra.png")));
		Texturas.texs.put("agua", new Texture(Gdx.files.internal("blocos/agua_fixa.png")));
		Texturas.texs.put("areia", new Texture(Gdx.files.internal("blocos/areia.png")));
		Texturas.texs.put("tronco_topo", new Texture(Gdx.files.internal("blocos/tronco_topo.png")));
		Texturas.texs.put("tronco_lado", new Texture(Gdx.files.internal("blocos/tronco_lado.png")));
		Texturas.texs.put("folha", new Texture(Gdx.files.internal("blocos/folha.png")));
		
		Texturas.texs.put("botao_f", new Texture(Gdx.files.internal("ui/botao_f.png")));
		Texturas.texs.put("botao_t", new Texture(Gdx.files.internal("ui/botao_t.png")));
		Texturas.texs.put("botao_d", new Texture(Gdx.files.internal("ui/botao_d.png")));
		Texturas.texs.put("botao_e", new Texture(Gdx.files.internal("ui/botao_e.png")));
		Texturas.texs.put("mira", new Texture(Gdx.files.internal("ui/mira.png")));
		Texturas.texs.put("clique", new Texture(Gdx.files.internal("ui/clique.png")));
		Texturas.texs.put("slot", new Texture(Gdx.files.internal("ui/slot.png")));
		
        mundo = new Mundo();
		ui = new UI(jogador);
		net = new Net(Net.SERVIDOR_MODO);
		
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
