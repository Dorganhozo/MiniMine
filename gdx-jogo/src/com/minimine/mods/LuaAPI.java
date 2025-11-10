package com.minimine.mods;

import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import com.minimine.cenas.Jogo;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.LuaFunction;
import java.io.File;
import java.io.IOException;
import com.minimine.utils.ArquivosUtil;
import com.minimine.utils.BiomasUtil;
import com.minimine.utils.ChunkUtil;
import com.minimine.utils.Texturas;
import com.minimine.utils.NuvensUtil;
import com.minimine.utils.DiaNoiteUtil;

public class LuaAPI {
	public static Globals globais;
	public static Jogo tela;
	public static String att;
	public static LuaFunction aoAjustar, ajuste;
	public static int v, h;
	
	public static void iniciar(Jogo principal) {
		tela = principal;
		String script = "";
		
		globais = JsePlatform.standardGlobals();
		
		globais.set("mundo", CoerceJavaToLua.coerce(tela.mundo));
		globais.set("jogador", CoerceJavaToLua.coerce(tela.ui.jogador));
		globais.set("ui", CoerceJavaToLua.coerce(tela.ui));
		globais.set("util", CoerceJavaToLua.coerce(new Util()));
		globais.set("biomas", CoerceJavaToLua.coerce(new BiomasUtil()));
		globais.set("chunkutil", CoerceJavaToLua.coerce(new ChunkUtil()));
		globais.set("texutil", CoerceJavaToLua.coerce(new Texturas()));
		globais.set("nuvens", CoerceJavaToLua.coerce(new NuvensUtil()));
		globais.set("ciclo", CoerceJavaToLua.coerce(new DiaNoiteUtil()));
		globais.set("gdx", CoerceJavaToLua.coerce(new Gdx()));
		
		aoAjustar = new LuaFunction() {
			public LuaValue call(LuaValue arg) {
				ajuste = (LuaFunction) arg;
				arg.call(LuaValue.valueOf(v), LuaValue.valueOf(h));
				return LuaValue.NIL;
			}
		};
		globais.set("log", new LuaFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					tela.ui.logs.logs += arg.tojstring() + "\n";
					return LuaValue.NIL;
				}
			});
		globais.set("rescripts", new LuaFunction() {
				@Override
				public LuaValue call() {
					att = "";
					ajuste = null;
					iniciar(tela);
					return LuaValue.NIL;
				}
			});
		globais.set("aoAjustar", aoAjustar);
		File dir = new File(Inicio.externo+"/MiniMine/mods/");
		if(!dir.exists()) {
			dir.mkdirs();
			ArquivosUtil.criar(dir.getAbsolutePath()+"/arquivos.mini");
		}
		String[] str = Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/arquivos.mini").readString().split("\n");
		for(int i = 0; i < str.length; i++) {
			if(str == null || str[i].equals("")) continue;
			script += ArquivosUtil.ler(Inicio.externo+"/MiniMine/mods/"+str[i]);
		}
		globais.load(script, "script").call();
		att = ArquivosUtil.ler(Inicio.externo+"/MiniMine/mods/att.lua");
	}
	
	public static void att(float delta) {
		globais.load(att, "script").call();
	}
	
	public static void ajustar(int ve, int ho) {
		v = ve;
		h = ho;
		if(ajuste != null) aoAjustar.call(ajuste);
	}
}
