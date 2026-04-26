package com.minimine.inventario;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.minimine.graficos.Texturas;
import com.badlogic.gdx.math.Vector2;
import com.minimine.mundo.ChunkUtil;
import com.minimine.mundo.blocos.Bloco;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.minimine.entidades.Jogador;

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
    public int ponteiroArrastando = -1;

    public Item[] itens = new Item[quantSlots];
    public int slotSelecionado = 0;
    public boolean aberto = false;

    public int hotbarSlots = 5;
    public Rectangle[] rectsHotbar;
    public int hotbarY = 20;

    public Item itemFlutuante = null;
    public boolean itemFlutuanteVeioDaGrade = false;
    public int slotOrigemFlutuante = -1; // -1 se veio da grade
    public int slotGradeOrigem = -1;     // slot da grade de origem, se veio dela
    public Vector2 posFlutuante = new Vector2();

    // === receita ===
    public int tamReceita = 56;
    public CharSequence[] gradeReceita = new CharSequence[9];
    public Item resultadoReceita = null;
    public Rectangle[] rectsGrade = new Rectangle[9];
    public Rectangle rectResultado;

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
                final float sx = invX + (x * tamSlot);
                final float sy = invY + (y * tamSlot);
                rects[i] = new Rectangle(sx, sy, tamSlot, tamSlot);
                i++;
            }
        }
        rectsHotbar = new Rectangle[hotbarSlots];
        final int hotbarX = v / 2 - (hotbarSlots * tamSlot) / 2;
        for(int x = 0; x < hotbarSlots; x++) {
            final float sx = hotbarX + (x * tamSlot);
            rectsHotbar[x] = new Rectangle(sx, hotbarY, tamSlot, tamSlot);
        }
        // grade de receita: a direita do inventario, centralizada verticalmente
        final int gradeX = invX + slotsH * tamSlot + 40;
        final int gradeY = invY + (slotsV * tamSlot) / 2 - (3 * tamReceita) / 2;
        for(int l = 0; l < 3; l++) {
            for(int c = 0; c < 3; c++) {
                final float sx = gradeX + c * tamReceita;
                final float sy = gradeY + (2 - l) * tamReceita;
                rectsGrade[l * 3 + c] = new Rectangle(sx, sy, tamReceita, tamReceita);
            }
        }
        // slot de resultado: a direita da grade, centralizado
        final int resX = gradeX + 3 * tamReceita + 24;
        final int resY = gradeY + tamReceita;
        rectResultado = new Rectangle(resX, resY, tamReceita, tamReceita);
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
            final Item itemNoDestino = itens[slotDestino];
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
                for(ItemRegistro.Item b : ItemRegistro.todos()) {
                    if(b == null) continue;
                    if(b.nome.equals(nome)) {
                        textura = Texturas.atlas.obter(b.textura);
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

    private TextureRegion texturaDoItem(CharSequence nome) {
        for(ItemRegistro.Item b : ItemRegistro.todos()) {
            if(b == null) continue;
            if(b.nome.equals(nome)) return Texturas.atlas.obter(b.textura);
        }
        return Texturas.atlas.obter("terra");
    }

    public void attReceita() {
        final ReceitaRegistro.Receita r = ReceitaRegistro.combinar(gradeReceita);
        if(r == null) {
            resultadoReceita = null;
            return;
        }
        final TextureRegion tex = texturaDoItem(r.resultado);
        resultadoReceita = new Item(r.resultado, tex, r.quantidade);
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
        posFlutuante.set(telaX, telaY);

        // === slot de resultado ===
        if(rectResultado.contains(telaX, telaY)) {
            if(resultadoReceita == null) return;
            if(itemFlutuante == null) {
                itemFlutuante = resultadoReceita;
                itemFlutuanteVeioDaGrade = false;
                slotOrigemFlutuante = -1;
                slotGradeOrigem = -1;
                // consome ingredientes
                for(int i = 0; i < 9; i++) gradeReceita[i] = null;
                resultadoReceita = null;
            }
            return;
        }
        // === slots da grade de receita ===
        for(int i = 0; i < 9; i++) {
            if(rectsGrade[i].contains(telaX, telaY)) {
                if(itemFlutuante == null) {
                    if(gradeReceita[i] != null && gradeReceita[i].length() > 0) {
                        final TextureRegion tex = texturaDoItem(gradeReceita[i]);
                        itemFlutuante = new Item(gradeReceita[i], tex, 1);
                        itemFlutuanteVeioDaGrade = true;
                        slotGradeOrigem = i;
                        slotOrigemFlutuante = -1;
                        gradeReceita[i] = null;
                        attReceita();
                    }
                } else {
                    // o que tava na grade volta pro flutuante
                    final CharSequence anteriorNaGrade = gradeReceita[i];
                    gradeReceita[i] = itemFlutuante.nome;
                    if(anteriorNaGrade != null && anteriorNaGrade.length() > 0) {
                        final TextureRegion tex = texturaDoItem(anteriorNaGrade);
                        itemFlutuante = new Item(anteriorNaGrade, tex, 1);
                        itemFlutuanteVeioDaGrade = true;
                        slotGradeOrigem = i;
                        slotOrigemFlutuante = -1;
                    } else {
                        itemFlutuante = null;
                        itemFlutuanteVeioDaGrade = false;
                        slotGradeOrigem = -1;
                    }
                    attReceita();
                }
                return;
            }
        }
        // === slots normais do inventario e hotbar ===
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

        if(itemFlutuante == null) {
            if(itens[slotClicado] != null) {
                itemFlutuante = itens[slotClicado];
                itemFlutuanteVeioDaGrade = false;
                slotOrigemFlutuante = slotClicado;
                slotGradeOrigem = -1;
                itens[slotClicado] = null;
            }
        } else {
            Item itemNoSlot = itens[slotClicado];
            itens[slotClicado] = itemFlutuante;
            itemFlutuante = itemNoSlot;
            itemFlutuanteVeioDaGrade = false;
            slotGradeOrigem = -1;
            if(itemFlutuante == null) {
                slotOrigemFlutuante = -1;
            } else {
                slotOrigemFlutuante = slotClicado;
            }
        }
    }

    public final void aoArrastar(final int telaX, final int telaY) {
        if(itemFlutuante != null) posFlutuante.set(telaX, telaY);
    }

    public void alternar() {
        if(aberto) {
            aberto = false;
            // devolve item flutuante
            if(itemFlutuante != null) {
                if(itemFlutuanteVeioDaGrade && slotGradeOrigem >= 0) {
                    gradeReceita[slotGradeOrigem] = itemFlutuante.nome;
                } else if(slotOrigemFlutuante >= 0 && itens[slotOrigemFlutuante] == null) {
                    itens[slotOrigemFlutuante] = itemFlutuante;
                } else {
                    addItem(itemFlutuante.nome, itemFlutuante.quantidade);
                }
                itemFlutuante = null;
                itemFlutuanteVeioDaGrade = false;
                slotOrigemFlutuante = -1;
                slotGradeOrigem = -1;
            }
            // devolve ingredientes da grade pro inventário
            for(int i = 0; i < 9; i++) {
                if(gradeReceita[i] != null && gradeReceita[i].length() > 0) {
                    addItem(gradeReceita[i], 1);
                    gradeReceita[i] = null;
                }
            }
            resultadoReceita = null;
            Gdx.input.setCursorCatched(true);
        } else {
            aberto = true;
            Gdx.input.setCursorCatched(false);
        }
    }
    // renderiza inventário + grade de receita
    public void renderizar(SpriteBatch sb, BitmapFont fonte) {
        if(!aberto) return;

        // slots do inventário
        for(int i = 0; i < rects.length; i++) {
            final Rectangle r = rects[i];
            sb.draw(texSlot, r.x, r.y, r.width, r.height);
            if(itens[i] != null) {
                sb.draw(itens[i].textura, r.x + 4, r.y + 4, r.width - 8, r.height - 8);
                if(itens[i].quantidade > 1)
                    fonte.draw(sb, String.valueOf(itens[i].quantidade), r.x + 4, r.y + 16);
            }
        }
        // grade de receita
        for(int i = 0; i < 9; i++) {
            final Rectangle r = rectsGrade[i];
            sb.draw(texSlot, r.x, r.y, r.width, r.height);
            if(gradeReceita[i] != null && gradeReceita[i].length() > 0) {
                final TextureRegion tex = texturaDoItem(gradeReceita[i]);
                if(tex != null) sb.draw(tex, r.x + 4, r.y + 4, r.width - 8, r.height - 8);
            }
        }
        // slot de resultado
        sb.draw(texSlot, rectResultado.x, rectResultado.y, rectResultado.width, rectResultado.height);
        if(resultadoReceita != null) {
            sb.draw(resultadoReceita.textura,
					rectResultado.x + 4, rectResultado.y + 4,
					rectResultado.width - 8, rectResultado.height - 8);
            if(resultadoReceita.quantidade > 1)
                fonte.draw(sb, String.valueOf(resultadoReceita.quantidade),
						   rectResultado.x + 4, rectResultado.y + 16);
        }

        // item flutuante
        if(itemFlutuante != null) {
            final float px = posFlutuante.x - tamReceita / 2f;
            final float py = posFlutuante.y - tamReceita / 2f;
            sb.draw(itemFlutuante.textura, px, py, tamReceita, tamReceita);
            if(itemFlutuante.quantidade > 1)
                fonte.draw(sb, String.valueOf(itemFlutuante.quantidade), px + 4, py + 16);
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

