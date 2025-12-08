package com.minimine.utils;

import java.util.HashMap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

public class Texturas {
	public static TexLista<CharSequence, Texture> texs = new TexLista<CharSequence, Texture>();

	static {
		try {
			// modelos:
			texs.put("grama_topo", new Texture(Gdx.files.internal("blocos/grama_topo.png")));
			texs.put("grama_lado", new Texture(Gdx.files.internal("blocos/grama_lado.png")));
			texs.put("terra", new Texture(Gdx.files.internal("blocos/terra.png")));
			texs.put("pedra", new Texture(Gdx.files.internal("blocos/pedra.png")));
			texs.put("agua", new Texture(Gdx.files.internal("blocos/agua_fixa.png")));
			texs.put("areia", new Texture(Gdx.files.internal("blocos/areia.png")));
			texs.put("tronco_topo", new Texture(Gdx.files.internal("blocos/tronco_topo.png")));
			texs.put("tronco_lado", new Texture(Gdx.files.internal("blocos/tronco_lado.png")));
			texs.put("folha", new Texture(Gdx.files.internal("blocos/folha.png")));
			texs.put("tabua_madeira", new Texture(Gdx.files.internal("blocos/tabua_madeira.png")));
			texs.put("cacto_topo", new Texture(Gdx.files.internal("blocos/cacto_topo.png")));
			texs.put("cacto_lado", new Texture(Gdx.files.internal("blocos/cacto_lado.png")));
			texs.put("vidro", new Texture(Gdx.files.internal("blocos/vidro.png")));
			texs.put("tocha", new Texture(Gdx.files.internal("blocos/tocha.png")));
			// interface
			texs.put("botao_f", new Texture(Gdx.files.internal("ui/botao_f.png")));
			texs.put("botao_t", new Texture(Gdx.files.internal("ui/botao_t.png")));
			texs.put("botao_d", new Texture(Gdx.files.internal("ui/botao_d.png")));
			texs.put("botao_e", new Texture(Gdx.files.internal("ui/botao_e.png")));
			texs.put("mira", new Texture(Gdx.files.internal("ui/mira.png")));
			texs.put("clique", new Texture(Gdx.files.internal("ui/clique.png")));
			texs.put("ataque", new Texture(Gdx.files.internal("ui/ataque.png")));
			texs.put("slot", new Texture(Gdx.files.internal("ui/slot.png")));
			texs.put("receita", new Texture(Gdx.files.internal("ui/receita.png")));
			texs.put("botao_opcao", new Texture(Gdx.files.internal("ui/botao_opcao.png")));
			texs.put("botao_ld", new Texture(Gdx.files.internal("ui/botao_ld.png")));
			texs.put("botao_le", new Texture(Gdx.files.internal("ui/botao_le.png")));
			texs.put("salvar", new Texture(Gdx.files.internal("ui/salvar.png")));
			texs.put("botao_ativado", new Texture(Gdx.files.internal("ui/botao_ativado.png")));
			texs.put("botao_desativado", new Texture(Gdx.files.internal("ui/botao_desativado.png")));
			texs.put("botao_aviso", new Texture(Gdx.files.internal("ui/botao_aviso.png")));
			texs.put("botao_servidor", new Texture(Gdx.files.internal("ui/servidor.png")));
			texs.put("botao_cliente", new Texture(Gdx.files.internal("ui/cliente.png")));
		} catch(Exception e) {
			Gdx.app.log("Texturas", "[ERRO]: " + e);
		}
	}

	public static class TexLista<K, V> extends HashMap<K, V> {
		public V obter(Object chave) {
			V o = super.get(chave);
			if(o == null) {
				Gdx.app.log("Texturas", "[ERRO] em: " + chave);
				if(containsKey("slot")) return super.get("slot");
			}
			return o;
		}
	}
}
