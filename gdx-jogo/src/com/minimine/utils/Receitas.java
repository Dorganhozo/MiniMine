package com.minimine.utils;
import com.minimine.entidades.Inventario;
import com.badlogic.gdx.Gdx;

public class Receitas {
	public static void fabricar(Inventario inv) {
		String nome = null;
		int quant = 1;
		if(inv.itens[inv.slotSelecionado] == null) return;
		if(inv.itens[inv.slotSelecionado].nome.equals("tronco")) {
			nome = "tabua_madeira";
			quant = 4;
		} else if(inv.itens[inv.slotSelecionado].nome.equals("areia")) {
			nome = "vidro";
		} else if(inv.itens[inv.slotSelecionado].nome.equals("folha")) {
			nome = "tocha";
		}
		if(nome != null) {
			inv.rmItem(inv.slotSelecionado, 1);
			inv.addItem(nome, quant);
			Gdx.app.log("[Receitas]", "feito: "+nome);
		}
	}
}
