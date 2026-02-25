package com.minimine.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.minimine.graficos.Texturas;
import com.badlogic.gdx.math.Vector2;
import com.minimine.mundo.ChunkUtil;
import com.minimine.mundo.blocos.Bloco;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Inventario {
    public Jogador jogador;
    public int quantSlots = 25;
    public int slotsV = 5, slotsH = 5;
    public int tamSlot = 64+16;
    public Texture texSlot;
    public Rectangle[] rects;
    public int invX, invY;

    public Item itemSendoArrastado = null;
    public int slotOrigem = -1;
    public int ponteiroArrastando = -1; // ID do toque que ta arrastando

    public Item[] itens = new Item[quantSlots];
    public int slotSelecionado = 0;
    public boolean aberto = false;

    public int hotbarSlots = 5;
    public Rectangle[] rectsHotbar;
    public int hotbarY = 20;

    public Item itemFlutuante = null;
    public int slotOrigemFlutuante = -1;
    public Vector2 posFlutuante = new Vector2(); // posicao visual do item flutuante

    public Inventario(Jogador jogador) {
        texSlot = Texturas.base;
        if(texSlot != null) aoAjustar(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        else Gdx.app.log("[Inventario]", "[ERRO]: Textura de slot nula");
        this.jogador = jogador;
    }

    public void aoAjustar(int v, int h) {
        invX = v / 2 - (slotsH * tamSlot) / 2;
        invY = h / 2 - (slotsV * tamSlot) / 2;

        rects = new Rectangle[quantSlots];
        int i = 0;
        for(int y = 0; y < slotsV; y++) {
            for(int x = 0; x < slotsH; x++) {
                if(i >= quantSlots) break;
                float sx = invX + (x * tamSlot);
                float sy = invY + (y * tamSlot);
                rects[i] = new Rectangle(sx, sy, tamSlot, tamSlot);
                i++;
            }
        }
        rectsHotbar = new Rectangle[hotbarSlots];
        int hotbarX = v / 2 - (hotbarSlots * tamSlot) / 2;
        for(int x = 0; x < hotbarSlots; x++) {
            float sx = hotbarX + (x * tamSlot);
            rectsHotbar[x] = new Rectangle(sx, hotbarY, tamSlot, tamSlot);
        }
    }

    public void aoSoltar(int telaX, int telaY, int p) {
        if(p != ponteiroArrastando || itemSendoArrastado == null) return;

        int slotDestino = -1;
        for(int i = 0; i < rectsHotbar.length; i++) {
            if(rectsHotbar[i].contains(telaX, telaY)) {
                slotDestino = i;
                break;
            }
        }
        if(slotDestino == -1 && aberto) {
            for(int i = 0; i < rects.length; i++) {
                if(rects[i].contains(telaX, telaY)) {
                    slotDestino = i;
                    break;
                }
            }
        }
        if(slotDestino != -1) {
            Item itemNoDestino = itens[slotDestino];
            itens[slotDestino] = itemSendoArrastado;
            itens[slotOrigem] = itemNoDestino;
        } else {
            itens[slotOrigem] = itemSendoArrastado;
        }
        itemSendoArrastado = null;
        slotOrigem = -1;
        ponteiroArrastando = -1;
    }

    public void selecionarSlot(int slot, Jogador jogador) {
        slotSelecionado = slot;
        if(itens[slot] != null) jogador.item = itens[slot].nome;
        else jogador.item = "ar";
    }

    public void addItem(CharSequence nome, int quantidade) {
        if(itens[slotSelecionado] != null && itens[slotSelecionado].nome.equals(nome)) {
            itens[slotSelecionado].quantidade += quantidade;
            return;
        }
        for(int i = 0; i < itens.length; i++) {
            if(itens[i] != null && itens[i].nome.equals(nome)) {
                itens[i].quantidade += quantidade;
                return;
            }
        }
        for(int i = 0; i < itens.length; i++) {
            if(itens[i] == null) {
                TextureRegion textura = null;
                for(Bloco b : Bloco.blocos) {
                    if(b == null) continue;
                    if(b.nome.equals(nome)) {
                        textura = Texturas.atlas.obter(b.lados);
                        break;
                    }
                }
                if(textura == null) {
                    Gdx.app.log("[Inventario]", "textura não encontrada para: " + nome);
                    textura = Texturas.atlas.obter("terra");
                }
                itens[i] = new Item(nome, textura, quantidade);
                return;
            }
        }
    }

    public void rmItem(int slot, int quantidade) {
        if(itens[slot] != null) {
            itens[slot].quantidade -= quantidade;
            if(itens[slot].quantidade <= 0) {
                itens[slot] = null;
            }
        }
    }

    public void aoTocar(int telaX, int telaY, int p) {
        if(!aberto) {
            for(int i = 0; i < rectsHotbar.length; i++) {
                if(rectsHotbar[i].contains(telaX, telaY)) {
                    selecionarSlot(i, jogador);
                    return;
                }
            }
            return;
        }
        int slotClicado = -1;
        for(int i = 0; i < rectsHotbar.length; i++) {
            if(rectsHotbar[i].contains(telaX, telaY)) {
                slotClicado = i;
                break;
            }
        }
        if(slotClicado == -1) {
            for(int i = 0; i < rects.length; i++) {
                if(rects[i].contains(telaX, telaY)) {
                    slotClicado = i;
                    break;
                }
            }
        }
        if(slotClicado == -1) return;

        posFlutuante.set(telaX, telaY);

        if(itemFlutuante == null) {
            if(itens[slotClicado] != null) {
                itemFlutuante = itens[slotClicado];
                slotOrigemFlutuante = slotClicado;
                itens[slotClicado] = null;
            }
        } else {
            Item itemNoSlot = itens[slotClicado];
            itens[slotClicado] = itemFlutuante;
            itemFlutuante = itemNoSlot;
            if(itemFlutuante == null) {
                slotOrigemFlutuante = -1;
            } else {
                slotOrigemFlutuante = slotClicado;
            }
        }
    }

    public void aoArrastar(int telaX, int telaY, int p) {
        if(itemFlutuante != null) posFlutuante.set(telaX, telaY);
    }

    public void alternar() {
        if(aberto) {
            aberto = false;
            if(itemFlutuante != null) {
                if(itens[slotOrigemFlutuante] == null) {
                    itens[slotOrigemFlutuante] = itemFlutuante;
                } else {
                    addItem(itemFlutuante.nome, itemFlutuante.quantidade);
                }
                itemFlutuante = null;
                slotOrigemFlutuante = -1;
            }
        } else {
            aberto = true;
        }
    }

    public static class Item {
        public CharSequence nome;
        public TextureRegion textura;
        public int quantidade;

        public Item(CharSequence nome, TextureRegion textura, int quantidade) {
            this.nome = nome;
            this.textura = textura;
            this.quantidade = quantidade;
        }
    }
}

