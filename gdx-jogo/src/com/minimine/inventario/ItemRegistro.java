package com.minimine.inventario;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemRegistro {
    public static class Item {
        public final CharSequence nome;
        public final CharSequence textura;

        public Item(CharSequence nome, CharSequence textura) {
            this.nome = nome;
            this.textura = textura;
        }
    }

    // LinkedHashMap pra manter ordem de inserção(PaginaItens usa isso)
    public static final LinkedHashMap<CharSequence, Item> itens = new LinkedHashMap<>();

    public static void registrar(CharSequence nome, CharSequence textura) {
        itens.put(nome, new Item(nome, textura));
    }

    public static Item obter(CharSequence nome) {
        return itens.get(nome);
    }

    public static Iterable<Item> todos() {
        return itens.values();
    }

    public static int total() {
        return itens.size();
    }
}
