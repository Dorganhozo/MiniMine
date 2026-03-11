package com.minimine.mundo.blocos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.micro.CaixaDialogo;
import com.micro.CampoTexto;
import com.micro.PainelFatiado;
import com.micro.Acao;
import com.micro.Ancora;
import com.minimine.utils.ArquivosUtil;
import java.util.ArrayList;
import java.util.List;
/*
 * classe auxiliar que monta a InterfaceBloco do bloco_estrutura
 * chamada por Bloco.iniciarInterfaces()

 * interface:
 *   - campo "Nome": nome do arquivo .minies a salvar
 *   - campos Larg/Alt/Prof: dimensões da bounding box (padrão 16x16x16)
 *   - campos AncX/Y/Z: âncora relativa ao canto da bbox (padrão 0,0,0)
 *   - botão "Salvar": varre a região, descarta ar e bloco_nulo, salva .minies
 *   - botão "Carregar": lê o .minies pelo nome e coloca no mundo 1 bloco à frente do bloco_estrutura
 *   - botão "Fechar": fecha sem salvar

 * bloco_nulo:
 *   - marca "ar explícito" dentro da estrutura
 *   - transparente=true, solido=false, culling=false
 *   - não é gravado no .minies(descartado igual ao ar real)

 * formato .minies(binario simples, sem ZIP):
 *   [int]versão do formato(= 1)
 *   [utf]nome
 *   [int]largura
 *   [int]altura
 *   [int]profundidade
 *   [int]ancora X
 *   [int]ancora Y
 *   [int]ancora Z
 *   [int]total de blocos salvos
 *   para cada bloco:
 *     [int] x local(0..largura-1)
 *     [int] y local(0..altura-1)
 *     [int] z local(0..profundidade-1)
 *     [utf] id do bloco
 *     [int] meta do bloco

 * a area visivel começa 1 bloco à frente(+X) do bloco_estrutura

 * OrigemEstrutura.x/y/z é preenchido pelo abrir() e lido pelo salvar()
 */
public class BlocoEstrutura {
    // guarda a posição do bloco_estrutura com interface aberta
    public static final class OrigemEstrutura {
        public static int x, y, z;
    }
    /*
     * Bcaixas
     * lista de bounding boxes ativas, uma por bloco_estrutura colocado no mundo
     * cada entrada: float[6] = { baseX, baseY, baseZ, larg, alt, prof }
     * alimentada pelo EventoBloco(aoColocar/aoDestruir)
     * lida pelo Render a cada frame, normalmente 0~5 entradas
     */
    public static final List<float[]> bcaixas = new ArrayList<float[]>();

    public static void addBcaixa(int x, int y, int z) {
        // bcaixa começa 1 bloco a frente (+X), dimensões padrão 16x16x16
        bcaixas.add(new float[]{ x + 1, y, z, 16, 16, 16 });
    }

    public static void rmBcaixa(int x, int y, int z) {
        float bx = x + 1, by = y, bz = z;
        for(int i = bcaixas.size() - 1; i >= 0; i--) {
            float[] b = bcaixas.get(i);
            if(b[0] == bx && b[1] == by && b[2] == bz) {
                bcaixas.remove(i);
                return;
            }
        }
    }

    // iniciar(), chamado por Bloco.iniciarInterfaces()
    public static void iniciar(
		final Bloco blocoEstrutura,
		final PainelFatiado base,
		final BitmapFont fonte) {

        if(blocoEstrutura == null) return;

        final CaixaDialogo dialogo = new CaixaDialogo(
			base, fonte, 3f, new ShapeRenderer());
        dialogo.largura = 440;
        dialogo.altura  = 320;

        // nome da estrutura
        final CampoTexto campoNome = new CampoTexto(base, fonte, 20, 240, 400, 44, 3f);
        campoNome.padrao = "Nome da estrutura";
        campoNome.limiteCaracteres = 48;
        dialogo.add(campoNome);

        // dimensões
        final CampoTexto campoLarg = new CampoTexto(base, fonte,  20, 188, 80, 40, 3f);
        campoLarg.padrao = "Larg"; campoLarg.limiteCaracteres = 4;
        dialogo.add(campoLarg);

        final CampoTexto campoAlt = new CampoTexto(base, fonte, 110, 188, 80, 40, 3f);
        campoAlt.padrao  = "Alt";  campoAlt.limiteCaracteres  = 4;
        dialogo.add(campoAlt);

        final CampoTexto campoProf = new CampoTexto(base, fonte, 200, 188, 80, 40, 3f);
        campoProf.padrao = "Prof"; campoProf.limiteCaracteres = 4;
        dialogo.add(campoProf);

        // ancora
        final CampoTexto campoAncX = new CampoTexto(base, fonte,  20, 136, 80, 40, 3f);
        campoAncX.padrao = "AncX"; campoAncX.limiteCaracteres = 4;
        dialogo.add(campoAncX);

        final CampoTexto campoAncY = new CampoTexto(base, fonte, 110, 136, 80, 40, 3f);
        campoAncY.padrao = "AncY"; campoAncY.limiteCaracteres = 4;
        dialogo.add(campoAncY);

        final CampoTexto campoAncZ = new CampoTexto(base, fonte, 200, 136, 80, 40, 3f);
        campoAncZ.padrao = "AncZ"; campoAncZ.limiteCaracteres = 4;
        dialogo.add(campoAncZ);

        com.minimine.ui.UI.gerenciador.addDialogo(dialogo);

        blocoEstrutura.ui = new InterfaceBloco() {
            boolean aberta = false;

            @Override
            public void abrir(int x, int y, int z) {
                Bloco.ABERTO = true;
                if(aberta) return;
                aberta = true;
                OrigemEstrutura.x = x;
                OrigemEstrutura.y = y;
                OrigemEstrutura.z = z;
                com.minimine.ui.UI.modoTexto = true;
                Gdx.input.setCursorCatched(false);

                dialogo.x = Gdx.graphics.getWidth()  / 2f - dialogo.largura / 2f;
                dialogo.y = Gdx.graphics.getHeight() / 2f - dialogo.altura  / 2f;

                dialogo.mostrar(
                    "Bloco de Estrutura (" + x + ", " + y + ", " + z + ")",
                    "",
                    new CaixaDialogo.Fechar() {
                        @Override public void aoFechar(boolean confirmou) { fechar(); }
                    });
            }

            @Override
            public void fechar() {
                if(!aberta) return;
                aberta = false;
                com.minimine.ui.UI.modoTexto = false;
                Gdx.input.setCursorCatched(true);
                dialogo.fechar(false);
                Bloco.ABERTO = false;
            }

            @Override
            public void renderizar(SpriteBatch sb, BitmapFont f, float delta) {
                // dialogo desenhado pelo GerenciadorUI
            }
            @Override public boolean aberta() { return aberta; }

            @Override
            public boolean processarToque(int x, int y, boolean pressionado) {
                return com.minimine.ui.UI.gerenciador.processarToque(x, y, pressionado);
            }

            @Override public void liberar() { dialogo.liberar(); }
        };
        // botão salvar
        dialogo.addBotao("Salvar", base, Ancora.CENTRO_DIREITO, -10, new Acao() {
				@Override public void exec() {
					salvar(blocoEstrutura.ui,
						   campoNome, campoLarg, campoAlt, campoProf,
						   campoAncX, campoAncY, campoAncZ);
				}
			});
        // botão carregar
        dialogo.addBotao("Carregar", base, Ancora.CENTRO, 0, new Acao() {
				@Override public void exec() {
					carregar(blocoEstrutura.ui, campoNome);
				}
			});
        // botão fechar
        dialogo.addBotao("Fechar", base, Ancora.CENTRO_ESQUERDO, 10, new Acao() {
				@Override public void exec() { blocoEstrutura.ui.fechar(); }
			});
    }

    // salvar()
    public static void salvar(
		final InterfaceBloco ui,
		final CampoTexto campoNome,
		final CampoTexto campoLarg,
		final CampoTexto campoAlt,
		final CampoTexto campoProf,
		final CampoTexto campoAncX,
		final CampoTexto campoAncY,
		final CampoTexto campoAncZ) {

        String nome = campoNome.texto.trim();
        if(nome.isEmpty()) {
            Gdx.app.log("[BlocoEstrutura]", "Nome vazio — salvamento cancelado.");
            return;
        }
        nome = nome.replaceAll("[^a-zA-Z0-9_\\-]", "_");

        int larg = clamp(praInt(campoLarg.texto, 16), 1,  64);
        int alt = clamp(praInt(campoAlt.texto,  16), 1, 256);
        int prof = clamp(praInt(campoProf.texto,  16), 1,  64);

        int ancX = clamp(praInt(campoAncX.texto, 0), 0, larg - 1);
        int ancY = clamp(praInt(campoAncY.texto, 0), 0, alt  - 1);
        int ancZ = clamp(praInt(campoAncZ.texto, 0), 0, prof - 1);

        // bcaixa começa 1 bloco a frente do bloco_estrutura no eixo +Z
        int baseX = OrigemEstrutura.x;
        int baseY = OrigemEstrutura.y;
        int baseZ = OrigemEstrutura.z + 1;

        try {
            ArquivosUtil.svEstrutura(
                nome, larg, alt, prof,
                ancX, ancY, ancZ,
                baseX, baseY, baseZ);
        } catch(Exception e) {
            Gdx.app.log("[BlocoEstrutura]", "[ERRO] ao salvar: " + e.getMessage());
        }
        ui.fechar();
    }

    // carregar()
    public static void carregar(
		final InterfaceBloco ui,
		final CampoTexto campoNome) {

        String nome = campoNome.texto.trim();
        if(nome.isEmpty()) {
            Gdx.app.log("[BlocoEstrutura]", "Nome vazio: carregamento cancelado.");
            return;
        }
        nome = nome.replaceAll("[^a-zA-Z0-9_\\-]", "_");

        ArquivosUtil.DadosEstrutura dados = ArquivosUtil.crEstrutura(nome);
        if(dados == null) {
            Gdx.app.log("[BlocoEstrutura]", "[ERRO] estrutura não encontrada: " + nome);
            return;
        }
        // coloca 1 bloco à frente do bloco_estrutura no eixo +Z
        int ox = OrigemEstrutura.x;
        int oy = OrigemEstrutura.y;
        int oz = OrigemEstrutura.z + 1;
        dados.colocarMundo(ox, oy, oz);
        Gdx.app.log("[BlocoEstrutura]", "estrutura carregada: " + nome);
        ui.fechar();
    }

    // iniciarEventos(), chamado por Bloco.iniciarInterfaces()
    public static void iniciarEventos(final Bloco blocoEstrutura) {
        if(blocoEstrutura == null) return;
        blocoEstrutura.evento = new EventoBloco() {
            @Override
            public void aoColocar(int x, int y, int z) {
                addBcaixa(x, y, z);
            }
            @Override
            public void aoDestruir(int x, int y, int z) {
                rmBcaixa(x, y, z);
            }
        };
    }

    public static int praInt(String s, int def) {
        if(s == null || s.trim().isEmpty()) return def;
        try {
			return Integer.parseInt(s.trim());
		} catch(NumberFormatException e) {
			return def;
		}
    }

    public static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }
}

