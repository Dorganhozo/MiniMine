package com.minimine.mods;

import com.badlogic.gdx.Gdx;
import com.minimine.Jogo;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import com.minimine.cenas.Teste;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.LuaFunction;

public class LuaAPI {
	public static Globals globais;
	public static Teste tela;
	
	public static void iniciar(Teste principal) {
		tela = principal;
		
		globais = JsePlatform.standardGlobals();
		
		globais.set("mundo", CoerceJavaToLua.coerce(tela.mundo));
		globais.set("jogador", CoerceJavaToLua.coerce(tela.ui.jogador));
		globais.set("ui", CoerceJavaToLua.coerce(tela.ui));
		
		globais.set("log", new LuaFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					tela.ui.logs.log(arg.tojstring());
					return LuaValue.NIL;
				}
			});
		
		String[] str = Gdx.files.absolute(Jogo.externo+"/MiniMine/mods/arquivos.mini").readString().split("\n");
		for(int i = 0; i < str.length; i++) {
			globais.loadfile(Jogo.externo+"/MiniMine/mods/"+str[i]).call();
		}
		/*
		try {
			
		} catch(Exception e) {
			Gdx.app.log("LuaAPI", "[ERRO]: String: "+str[0]+", "+e);
		} */
	}
}
