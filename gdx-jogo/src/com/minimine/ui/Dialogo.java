package com.minimine.ui;

import com.minimine.utils.InterUtil;
import com.minimine.utils.Texturas;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.Gdx;

public class Dialogo extends InterUtil.Objeto {
    public String titulo;
    public String texto = "";
    public boolean visivel = false;
    public GlyphLayout layout;
	public Acao acao;
	public int limite = 15;

    public Dialogo() {
        super("caixa_dialogo");
        this.layout = new GlyphLayout();
    }

    public void abrir(String titulo, Acao acao) {
        this.titulo = titulo;
        this.texto = "";
        this.visivel = true;
		this.acao = acao;
		
		Gdx.input.setOnscreenKeyboardVisible(true); 
    }

    // recebe as letras digitadas no teclado
    public void digitando(char letra) {
        if(!visivel) return;
		if(acao == null) acao.aoDigitar(letra);

        if(letra == '\b') { // apaga
            if(texto.length() > 0) {
                texto = texto.substring(0, texto.length() - 1);
            }
        } else if(letra == '\n' || letra == '\r') {
			visivel = false;
			if(acao != null) acao.aoConfirmar();
		} else if(texto.length() < limite) {
            texto += letra;
        }
    }

    @Override
    public void porFrame(float delta, SpriteBatch sb, BitmapFont fonte) {
        if(!visivel) return;

        // desenha o fundo(centralizado)
        float larg = 500;
        float alt = 200;
        float x = (Gdx.graphics.getWidth() - larg) / 2;
        float y = (Gdx.graphics.getHeight() - alt) / 2;

        sb.draw(Texturas.texs.get("botao_opcao"), x, y, larg, alt);

        // desenha o titulo
        layout.setText(fonte, titulo);
        fonte.draw(sb, titulo, (Gdx.graphics.getWidth() - layout.width) / 2, y + alt - 30);

        // desenha o texto que ta sendo escrito
        layout.setText(fonte, texto);
        fonte.draw(sb, texto, (Gdx.graphics.getWidth() - layout.width) / 2, y + (alt / 2) + 10);
    }
	
	public static interface Acao {
		public void aoConfirmar();
		public void aoDigitar(char letra);
	}
}
