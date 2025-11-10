package com.minimine.utils;

import java.util.Map;
import java.util.HashMap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.minimine.cenas.Mundo;

public class Texturas {
	public static Map<CharSequence, Texture> texs = new TexLista<>();
	
	static {
		try {
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
			Texturas.texs.put("botao_opcao", new Texture(Gdx.files.internal("ui/botao_opcao.png")));
		} catch(Exception e) {
			Gdx.app.log("Texturas", "[ERRO]: "+e);
		}
	}
	
	public static class TexLista extends HashMap {

		@Override
		public Object get(Object chave) {
			Object o = super.get(chave);
			if(o == null) {
				Gdx.app.log("Texturas", "[ERRO] em: "+chave);
				return super.get("slot");
			}
			return o;
		}
	}
}
