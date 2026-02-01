package com.minimine.ui;

import com.minimine.graficos.Texturas;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

public class Dialogo extends InterUtil.Objeto {
    public String titulo;
    public String texto = "";
    public boolean visivel = false;
    public GlyphLayout layout;
    public Acao acao;
    public int limite = 30;
    public Rectangle hitbox;

    public Dialogo() {
        super("caixa_dialogo");
        this.layout = new GlyphLayout();
        this.hitbox = new Rectangle();
    }

    public void abrir(String titulo, Acao acao) {
        this.titulo = titulo;
        this.texto = "";
        this.visivel = true;
        this.acao = acao;

        Gdx.input.setOnscreenKeyboardVisible(true); 
        Gdx.input.setCursorCatched(false);
    }

    public void fechar() {
        this.visivel = false;
        Gdx.input.setOnscreenKeyboardVisible(false);
        if(acao != null) acao.aoFechar();
    }

    public void digitando(char letra) {
        if(!visivel) return;

        if(letra == '\b') { 
            if(texto.length() > 0) {
                texto = texto.substring(0, texto.length() - 1);
            }
        } else if(letra == '\n' || letra == '\r') {
            confirmar();
        } else if(texto.length() < limite) {
            texto += letra;
        }
    }

    public void confirmar() {
        visivel = false;
        Gdx.input.setOnscreenKeyboardVisible(false);
        if(acao != null) acao.aoConfirmar();
    }

    public boolean verificarToque(int tx, int ty) {
        if(!visivel) return false;

        if(hitbox.contains(tx, ty)) {
            // se clicou dentro, garante que o teclado abra
            Gdx.input.setOnscreenKeyboardVisible(true);
            return true;
        } else {
            // se clicou fora, fecha o diálogo
            fechar();
            return false;
        }
    }

    @Override
    public void porFrame(float delta, SpriteBatch sb, BitmapFont fonte) {
        if(!visivel) return;

        float larg = 500;
        float alt = 200;
        float x = (Gdx.graphics.getWidth() - larg) / 2;
        float y = (Gdx.graphics.getHeight() - alt) / 2;

        // atualiza a hitbox pra detecção de toque
        hitbox.set(x, y, larg, alt);

        sb.draw(Texturas.texs.get("botao_opcao"), x, y, larg, alt);

        layout.setText(fonte, titulo);
        fonte.draw(sb, titulo, (Gdx.graphics.getWidth() - layout.width) / 2, y + alt - 30);

        layout.setText(fonte, texto);
        fonte.draw(sb, texto, (Gdx.graphics.getWidth() - layout.width) / 2, y + (alt / 2) + 10);
    }

    public static interface Acao {
        public void aoConfirmar();
        public void aoDigitar(char letra);
        public void aoFechar();
    }
}
