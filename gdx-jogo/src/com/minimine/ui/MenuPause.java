package com.minimine.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.minimine.graficos.Texturas;
import com.minimine.Inicio;
import com.minimine.utils.ArquivosUtil;
import com.minimine.cenas.Jogo;
import com.minimine.Cenas;

public class MenuPause {
    public static EstanteVertical menuPause;
    public static boolean menuAberto = false;
    public static ShapeRenderer shapeRenderer;
    public static Botao botaoVoltar, botaoSalvar, botaoSair;

    public static void iniciar() {
        float larguraTela = Gdx.graphics.getWidth();
        float alturaTela = Gdx.graphics.getHeight();

        // tamanho dos botões
        float larguraBotao = 200f;
        float alturaBotao = 60f;
        float espacoBotoes = 20f;

        // posição central
        float xCentral = (larguraTela - larguraBotao) / 2f;
        float yCentral = alturaTela / 2f + alturaBotao;

        Gdx.app.log("MenuPause", String.format("Inicializando menu - Tela: %.0fx%.0f, Centro: %.0f,%.0f", 
		larguraTela, alturaTela, xCentral, yCentral));

        // criar o menu vertical
        menuPause = new EstanteVertical("menuPause", (int)xCentral, (int)yCentral, espacoBotoes);

        // botão voltar
        botaoVoltar = new Botao(
            Texturas.texs.get("botao_opcao"), 
            xCentral, 
            yCentral, 
            larguraBotao, 
            alturaBotao, 
            "voltar") {
            @Override
            public void aoTocar(int telaX, int telaY, int p) {
                sprite.setAlpha(0.7f);
                Gdx.app.log("MenuPause", "Tocou VOLTAR em " + telaX + "," + telaY);
				fecharMenu();
            }
            @Override
            public void aoSoltar(int telaX, int telaY, int p) {
                sprite.setAlpha(1f);
                Gdx.app.log("MenuPause", "Soltou VOLTAR - Fechando");
            }
        };
        // botão salvar
        botaoSalvar = new Botao(
            Texturas.texs.get("botao_opcao"), 
            xCentral, 
            yCentral - alturaBotao - espacoBotoes, 
            larguraBotao, 
            alturaBotao, 
            "salvar") {
            @Override
            public void aoTocar(int telaX, int telaY, int p) {
                sprite.setAlpha(0.7f);
				fecharMenu();
				salvarJogo();
				UI.abrirDialogo("Jogo salvo com sucesso!");
                Gdx.app.log("MenuPause", "Tocou SALVAR em " + telaX + "," + telaY);
            }
            @Override
            public void aoSoltar(int telaX, int telaY, int p) {
                sprite.setAlpha(1f);
                Gdx.app.log("MenuPause", "Soltou SALVAR");
            }
        };
        // botão sair
        botaoSair = new Botao(
            Texturas.texs.get("botao_opcao"), 
            xCentral, 
            yCentral - (alturaBotao + espacoBotoes) * 2, 
            larguraBotao, 
            alturaBotao, 
            "sair") {
            @Override
            public void aoTocar(int telaX, int telaY, int p) {
                sprite.setAlpha(0.7f);
				salvarJogo();
				Inicio.defTela(Cenas.menu);
				fecharMenu();
				Cenas.jogo.dispose();
                Gdx.app.log("MenuPause", "Tocou SAIR em " + telaX + "," + telaY);
            }
            @Override
            public void aoSoltar(int telaX, int telaY, int p) {
                sprite.setAlpha(1f);
                Gdx.app.log("MenuPause", "Soltou SAIR");
				fecharMenu();
            }
        };
        // adiciona na ordem
        menuPause.add(botaoVoltar);
        menuPause.add(botaoSalvar);
        menuPause.add(botaoSair);

        // log das hitboxes
        Gdx.app.log("MenuPause", String.format("Voltar hitbox: %.0f,%.0f %.0fx%.0f", 
		botaoVoltar.hitbox.x, botaoVoltar.hitbox.y, botaoVoltar.hitbox.width, botaoVoltar.hitbox.height));
        Gdx.app.log("MenuPause", String.format("Salvar hitbox: %.0f,%.0f %.0fx%.0f", 
		botaoSalvar.hitbox.x, botaoSalvar.hitbox.y, botaoSalvar.hitbox.width, botaoSalvar.hitbox.height));
        Gdx.app.log("MenuPause", String.format("Sair hitbox: %.0f,%.0f %.0fx%.0f", 
		botaoSair.hitbox.x, botaoSair.hitbox.y, botaoSair.hitbox.width, botaoSair.hitbox.height));
        Gdx.app.log("MenuPause", "Menu inicializado - " + menuPause.filhos.size() + " botões");
    }

    public static void alternarMenu() {
        if(menuAberto) fecharMenu();
        else abrirMenu();
    }

    public static void abrirMenu() {
        if(menuPause == null) iniciar();
        
        menuAberto = true;
        Gdx.input.setCursorCatched(false);
        Gdx.app.log("MenuPause", "===== MENU ABERTO =====");
    }

    public static void fecharMenu() {
        menuAberto = false;
        Gdx.input.setCursorCatched(true);
        Gdx.app.log("MenuPause", "===== MENU FECHADO =====");
    }

    public static void salvarJogo() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					ArquivosUtil.svMundo(Jogo.mundo, Jogo.render.ui.jg);
				}
			}).start();
        Gdx.app.log("MenuPause", "Salvando o jogo...");
        fecharMenu();
    }

    public static void renderizar(SpriteBatch sb, BitmapFont fonte) {
        if(!menuAberto || menuPause == null) return;

        sb.end();

        shapeRenderer.setProjectionMatrix(sb.getProjectionMatrix());

        // desenha fundo semi-transparente escuro
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        sb.begin();

        // renderiza os botões
        menuPause.porFrame(0, sb, fonte);

        // desenha texto nos botões
        float larguraTela = Gdx.graphics.getWidth();
        float alturaTela = Gdx.graphics.getHeight();
        float alturaBotao = 60f;
        float espacoBotoes = 20f;

        // textos centralizados nos botões
        String[] textos = {"Voltar", "Salvar", "Sair"};
        float yCentral = alturaTela / 2f + alturaBotao;

        for(int i = 0; i < textos.length; i++) {
            float yTexto = (yCentral - (alturaBotao + espacoBotoes) * i - alturaBotao / 2f) + 50f;
            float xTexto = larguraTela / 2f - 30f;
            fonte.draw(sb, textos[i], xTexto, yTexto);
        }
    }

    public static void aoAjustar(int largura, int altura) {
        if(menuPause == null) return;

        float larguraBotao = 200f;
        float alturaBotao = 60f;
        
        float xCentral = (largura - larguraBotao) / 2f;
        float yCentral = altura / 2f + alturaBotao;

        menuPause.x = (int)xCentral;
        menuPause.y = (int)yCentral;
        menuPause.att();

        Gdx.app.log("MenuPause", "Tela ajustada para " + largura + "x" + altura);
    }

    public static void liberar() {
        if(shapeRenderer != null) shapeRenderer.dispose();
    }
}

