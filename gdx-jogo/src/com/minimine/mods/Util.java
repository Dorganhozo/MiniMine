package com.minimine.mods;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.minimine.cenas.Jogo;
import com.minimine.Inicio;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.cenas.Bloco;
import com.minimine.utils.ChunkUtil;
import com.minimine.utils.InterUtil;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Util {
	public static ModelInstance obterModeloGLTF(String caminho) {
		SceneAsset asset = new GLTFLoader().load(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		return new ModelInstance(asset.scene.model);
	}
	
	public static Texture carregarTextura(String nome, String caminho) {
		Texturas.texs.put(nome, new Texture(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho)));
		return Texturas.texs.get(nome);
	}
	
	public static BitmapFont carregarFonte(String caminho, int tam) {
		return InterUtil.carregarFonte(caminho, tam);
	}
	
	public static Sprite criarSprite(Texture textura) {
		return new Sprite(textura);
	}
	
	public static Sprite criarSprite(String textura) {
		return new Sprite(Texturas.texs.get(textura));
	}
}
