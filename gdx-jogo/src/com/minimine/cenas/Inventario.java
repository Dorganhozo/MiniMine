package com.minimine.cenas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.minimine.utils.Texturas;

public class Inventario implements UI.Evento {
    public int quantSlots = 25;
    public int slotsV = 5, slotsH = 5;
    public int tamSlot = 64+16;
    public Texture texSlot;
    public Sprite[] sprites;
    public Rectangle[] rects;
    public int invX, invY;

    public Item[] itens = new Item[quantSlots];
    public int slotSelecionado = 0;
    public boolean aberto = false;
    // hotbar
    public int hotbarSlots = 5;
    public Rectangle[] rectsHotbar;
    public Sprite[] spritesHotbar;
    public int hotbarY = 20;

    public Inventario() {
        texSlot = new Texture(Gdx.files.internal("ui/slot.png"));
        aoAjustar(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    
	@Override
    public void aoAjustar(int v, int h) {
        invX = v / 2 - (slotsH * tamSlot) / 2;
        invY = h / 2 - (slotsV * tamSlot) / 2;
        // slots do inventario completo:
        sprites = new Sprite[quantSlots];
        rects = new Rectangle[quantSlots];

        int i = 0;
        for(int y = 0; y < slotsV; y++) {
            for(int x = 0; x < slotsH; x++) {
                if(i >= quantSlots) break;
                Sprite s = new Sprite(texSlot);
                s.setSize(tamSlot, tamSlot);
                s.setPosition(invX + (x * tamSlot), invY + (y * tamSlot));
                Rectangle rect = new Rectangle(s.getX(), s.getY(), s.getWidth(), s.getHeight());
                sprites[i] = s;
                rects[i] = rect;
                i++;
            }
        }
        //  slots da hotbar:
        spritesHotbar = new Sprite[hotbarSlots];
        rectsHotbar = new Rectangle[hotbarSlots];

        int hotbarX = v / 2 - (hotbarSlots * tamSlot) / 2;
        for(int x = 0; x < hotbarSlots; x++) {
            Sprite s = new Sprite(texSlot);
            s.setSize(tamSlot, tamSlot);
            s.setPosition(hotbarX + (x * tamSlot), hotbarY);
            Rectangle rect = new Rectangle(s.getX(), s.getY(), s.getWidth(), s.getHeight());
            spritesHotbar[x] = s;
            rectsHotbar[x] = rect;
        }
    }
	@Override
    public void aoTocar(int telaX, int telaY, int p) {
        if(aberto) {
            // no inventario:
            for(int i = 0; i < rects.length; i++) {
                if(rects[i].contains(telaX, telaY)) {
                    selecionarSlot(i, UI.jogador);
                    return;
                }
            }
        } else {
            // na hotbar:
            for(int i = 0; i < rectsHotbar.length; i++) {
                if(rectsHotbar[i].contains(telaX, telaY)) {
                    selecionarSlot(i, UI.jogador);
                    return;
                }
            }
        }
    }

    public void selecionarSlot(int slot, Jogador jogador) {
        slotSelecionado = slot;
        if(itens[slot] != null) {
            jogador.blocoSele = itens[slot].tipo;
            jogador.item = itens[slot].nome;
        } else {
            jogador.blocoSele = 0;
            jogador.item = "Ar";
        }
    }

    public void addItem(int tipo, int quantidade) {
		// se o slot tem o mesmo item:
		if(itens[slotSelecionado] != null && itens[slotSelecionado].tipo == tipo) {
			itens[slotSelecionado].quantidade += quantidade;
			return;
		}
		// se o slot atual é diferente, coloca o item onde tiver espaço:
		for(int i = 0; i < itens.length; i++) {
			if(itens[i] != null && itens[i].tipo == tipo) {
				itens[i].quantidade += quantidade;
				return;
			}
		}
		// senão, colocar no primeiro slot vazio:
		for(int i = 0; i < itens.length; i++) {
			if(itens[i] == null) {
				String nome = "Ar";
				Texture textura = texSlot;

				if(tipo == 1) {
					nome = "Grama";
					textura = Texturas.texs.get("grama_lado");
				} else if(tipo == 2) {
					nome = "Terra";
					textura = Texturas.texs.get("terra");
				} else if(tipo == 3) {
					nome = "Pedra";
					textura = Texturas.texs.get("pedra");
				} else if(tipo == 4) {
					nome = "Agua";
					textura = Texturas.texs.get("agua");
				} else if(tipo == 5) {
					nome = "Areia";
					textura = Texturas.texs.get("areia");
				} else if(tipo == 6) {
					nome = "Tronco";
					textura = Texturas.texs.get("tronco_lado");
				} else if(tipo == 7) {
					nome = "Folha";
					textura = Texturas.texs.get("folha");
				}
				itens[i] = new Item(tipo, nome, textura, quantidade);
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

    public void att() {
        // hotbar:
        for(int i = 0; i < spritesHotbar.length; i++) {
            spritesHotbar[i].draw(UI.sb);

            if(i == slotSelecionado) {
                UI.sb.setColor(1, 1, 1, 0.5f);
                spritesHotbar[i].draw(UI.sb);
                UI.sb.setColor(1, 1, 1, 1);
            }
            if(itens[i] != null) {
                Sprite itemSprite = new Sprite(itens[i].textura);
                itemSprite.setSize(tamSlot - 10, tamSlot - 10);
                itemSprite.setPosition(spritesHotbar[i].getX() + 5, spritesHotbar[i].getY() + 5);
                itemSprite.draw(UI.sb);

                if(itens[i].quantidade > 1) {
                    UI.fonte.getData().setScale(1f);
                    UI.fonte.draw(UI.sb, String.valueOf(itens[i].quantidade), 
					spritesHotbar[i].getX() + tamSlot - 15, 
					spritesHotbar[i].getY() + 15);
                    UI.fonte.getData().setScale(1.5f);
                }
            }
        }
        // inventario:
        if(aberto) {
            for(int i = 0; i < sprites.length; i++) {
                sprites[i].draw(UI.sb);
                if(itens[i] != null) {
                    Sprite itemSprite = new Sprite(itens[i].textura);
                    itemSprite.setSize(tamSlot - 5, tamSlot - 5);
                    itemSprite.setPosition(sprites[i].getX() + 5, sprites[i].getY() + 5);
                    itemSprite.draw(UI.sb);

                    if(itens[i].quantidade > 1) {
                        UI.fonte.getData().setScale(0.8f);
                        UI.fonte.draw(UI.sb, String.valueOf(itens[i].quantidade), 
						sprites[i].getX() + tamSlot - 15, 
						sprites[i].getY() + 15);
                        UI.fonte.getData().setScale(1.5f);
                    }
                }
            }
        }
    }
    public static class Item {
        public int tipo;
        public String nome;
        public Texture textura;
        public int quantidade;

        public Item(int tipo, String nome, Texture textura, int quantidade) {
            this.tipo = tipo;
            this.nome = nome;
            this.textura = textura;
            this.quantidade = quantidade;
        }
    }
}
