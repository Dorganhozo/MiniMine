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

public class Util {
	public static ModelInstance obterModeloGLTF(String caminho) {
		SceneAsset asset = new GLTFLoader().load(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		return new ModelInstance(asset.scene.model);
	}
	
	public static Texture carregarTextura(String nome, String caminho) {
		Texture tex = new Texture(Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		Texturas.texs.put(nome, tex);
		return tex;
	}
	
	public static Bloco addBloco(CharSequence nome, byte tipo, int topoId) {
		Bloco b = new Bloco(nome, tipo, topoId);
		ChunkUtil.blocos.add(b);
		return b;
	}
	
	public static Bloco addBloco(CharSequence nome, byte tipo, int topoId, boolean transp) {
		Bloco b = new Bloco(nome, tipo, topoId, transp);
		ChunkUtil.blocos.add(b);
		return b;
	}
	
	public static Bloco addBloco(CharSequence nome, byte tipo, int topoId, boolean transp, boolean solido) {
		Bloco b = new Bloco(nome, tipo, topoId, transp, solido);
		ChunkUtil.blocos.add(b);
		return b;
	}
}
