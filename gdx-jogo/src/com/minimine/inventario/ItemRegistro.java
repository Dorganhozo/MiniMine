package com.minimine.inventario;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.LinkedHashMap;
import com.minimine.graficos.Texturas;

public final class ItemRegistro {
    // LinkedHashMap pra manter ordem de inserção(PaginaItens usa isso)
    public static final LinkedHashMap<CharSequence, Item> itens = new LinkedHashMap<>();

    public static Item registrar(CharSequence nome, CharSequence textura) {
		final Item item = new Item(nome, Texturas.atlas.get(textura));
        itens.put(nome, item);
		return item;
    }

    public static final Item obter(CharSequence nome) {
        return itens.get(nome);
    }

    public static final Iterable<Item> todos() {
        return itens.values();
    }

    public static final int total() {
        return itens.size();
    }
}
