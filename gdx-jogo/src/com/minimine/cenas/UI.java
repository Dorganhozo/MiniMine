package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.InputProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.minimine.utils.Texturas;
import com.minimine.ui.Botao;
import com.minimine.Debugador;
import org.luaj.vm2.LuaFunction;
import java.util.Map;
import com.minimine.ui.Texto;
import org.luaj.vm2.LuaValue;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.InterUtil;
import com.badlogic.gdx.Input;
import com.minimine.utils.ArquivosUtil;
import com.minimine.Inicio;
import com.minimine.Logs;
import android.webkit.JavascriptInterface;

public class UI implements InputProcessor {
	public static PerspectiveCamera camera;
	public static Map<CharSequence, Botao> botoes = new HashMap<>();
	public static Map<CharSequence, Texto> textos = new HashMap<>();
    public static SpriteBatch sb;
    public static BitmapFont fonte;
    
    public boolean esquerda = false, frente = false, tras = false, direita = false, cima = false, baixo = false, acao = false;
	public Sprite spriteMira;
	public int pontoEsq = -1;
    public int pontoDir = -1;
    public final Vector2 esqCentro = new Vector2();
    public final Vector2 esqPos = new Vector2();
    public final Vector2 ultimaDir = new Vector2();

	public float sensi = 0.25f;
	public static final HashMap<Integer, CharSequence> toques = new HashMap<>();
	public static Runtime rt = Runtime.getRuntime();

	public float botaoTam = 140f;
	public float espaco = 60f;

	public static Jogador jogador;
	public static boolean debug = false;
	public int fps = 0;
	public static Debugador debugador;
	
	public final Vector3 frenteV = new Vector3(0, 0, 0), direitaV = new Vector3(0, 0, 0);
	
    public UI(Jogador jogador) {
        camera = new PerspectiveCamera(120, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 18f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 400f;
        camera.update();

		sb = new SpriteBatch(); 

		fonte = InterUtil.carregarFonte("ui/fontes/pixel.ttf", 15);

        Gdx.input.setInputProcessor(this);
		this.jogador = jogador;
		this.jogador.camera = camera;
		this.jogador.inv = new Inventario();
		
		configDpad(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
	
	public boolean chatAberto = false;
	public String ultimaMensagem = "";
	public List<String> mensagens = new ArrayList<String>();

	public void abrirChat() {
		if(chatAberto) return;
		chatAberto = true;

		Gdx.input.getTextInput(new Input.TextInputListener() {
			@Override
				public void input(String texto) {
					if(texto != null && texto.length() > 0) {
						ultimaMensagem = texto;
						mensagens.add("> " + texto);
						Gdx.app.log("CHAT", texto);
					}
					chatAberto = false;
				}
				public void canceled() {
					chatAberto = false;
				}
			}, "Chat", "", "Digite e envie");
	}

	@Override
	public boolean touchDown(int telaX, int telaY, int p, int b) {
		int y = Gdx.graphics.getHeight() - telaY;

		for(Botao e : botoes.values()) {
			if(e.hitbox.contains(telaX, y)) {
				e.aoTocar(telaX, y, p);
				toques.put(p, e.nome);
				return true; // botão pressionado não faz mais nada
			}
		}
		jogador.inv.aoTocar(telaX, y, p);
		if(telaX >= Gdx.graphics.getWidth() / 2 && pontoDir == -1) { 
			pontoDir = p; 
			ultimaDir.set(telaX, y); 
		}
		return true;
	}

	@Override
	public boolean touchUp(int telaX, int telaY, int p, int b) {
		int y = Gdx.graphics.getHeight() - telaY;

		CharSequence botao = toques.remove(p);
		if(botao != null) {
			for(Botao e : botoes.values()) {
				if(botao.equals(e.nome)) {
					e.aoSoltar(telaX, y, p);
					break;
				}
			}
		}
		if(p == pontoDir) pontoDir = -1;
		return true;
	}

	@Override
	public boolean touchDragged(int telaX, int telaY, int p) {
		int y = Gdx.graphics.getHeight() - telaY;

        jogador.inv.aoArrastar(telaX, y, p);

		if(p == pontoDir) {
			float dx = telaX - ultimaDir.x;
			float dy = y - ultimaDir.y;
			jogador.yaw -= dx * sensi;
			jogador.tom += dy * sensi;
			if(jogador.tom > 89f) jogador.tom = 89f;
			if(jogador.tom < -89f) jogador.tom = -89f;
			ultimaDir.set(telaX, y);
		}
		if(toques.containsKey(p)) {
            CharSequence botaoAntigo = toques.get(p);
			boolean sobreBotao = false;
			for(Botao e : botoes.values()) {
				if(e.hitbox.contains(telaX, y)) {
					sobreBotao = true;
					if(!e.nome.equals(botaoAntigo)) {
						if(botaoAntigo != null) {
							for(Botao b : botoes.values()) {
								if(b.nome.equals(botaoAntigo)) {
									b.aoSoltar(telaX, y, p);
									break;
								}
							}
						}
						e.aoTocar(telaX, y, p);
						toques.put(p, e.nome);
					}
					break;
				}
			}
			if(!sobreBotao && botaoAntigo != null) {
				for(Botao e : botoes.values()) {
					if(e.nome.equals(botaoAntigo)) {
						e.aoSoltar(telaX, y, p);
						break;
					}
				}
				toques.put(p, null);
			}
		}
		return true;
	}

	public void criarBotoesPadrao() {
		if(!botoes.isEmpty()) return;

		spriteMira = new Sprite(Texturas.texs.get("mira"));
		spriteMira.setSize(50f, 50f);

		botoes.put("direita", new Botao(Texturas.texs.get("botao_d"), 0, 0, botaoTam, botaoTam, "direita") {
				public void aoTocar(int t, int t2, int p){ direita = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ direita = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("esquerda", new Botao(Texturas.texs.get("botao_e"), 0, 0, botaoTam, botaoTam, "esquerda") {
				public void aoTocar(int t, int t2, int p){ esquerda = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ esquerda = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("frente", new Botao(Texturas.texs.get("botao_f"), 0, 0, botaoTam, botaoTam, "frente") {
				public void aoTocar(int t, int t2, int p){ frente = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ frente = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("tras", new Botao(Texturas.texs.get("botao_t"), 0, 0, botaoTam, botaoTam, "tras") {
				public void aoTocar(int t, int t2, int p){ tras = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ tras = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("cima", new Botao(Texturas.texs.get("botao_f"), 0, 0, botaoTam, botaoTam, "cima") {
				public void aoTocar(int t, int t2, int p){ cima = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ cima = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("baixo", new Botao(Texturas.texs.get("botao_t"), 0, 0, botaoTam, botaoTam, "baixo") {
				public void aoTocar(int t, int t2, int p){ baixo = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ baixo = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("diagDireita", new Botao(Texturas.texs.get("botao_ld"), 0, 0, botaoTam, botaoTam, "diagDireita") {
				public void aoTocar(int t, int t2, int p){ frente = true; direita = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ frente = false; direita = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("diagEsquerda", new Botao(Texturas.texs.get("botao_le"), 0, 0, botaoTam, botaoTam, "diagEsquerda") {
				public void aoTocar(int t, int t2, int p){ frente = true; esquerda = true; sprite.setAlpha(0.5f); }
				public void aoSoltar(int t, int t2, int p){ frente = false; esquerda = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("acao", new Botao(Texturas.texs.get("clique"), 0, 0, botaoTam, botaoTam, "acao") {
				public void aoTocar(int t, int t2, int p){
					if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
					else jogador.item = "ar";
					acao = true;
					jogador.interagirBloco();
					if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
					else jogador.item = "ar";
					toques.put(p, "acao");
					sprite.setAlpha(0.5f);
				}
				public void aoSoltar(int t, int t2, int p){ acao = false; sprite.setAlpha(0.9f); }
			});
		botoes.put("ataque", new Botao(Texturas.texs.get("ataque"), 0, 0, botaoTam, botaoTam, "ataque") {
				public void aoTocar(int t, int t2, int p){
					jogador.item = "ar";
					jogador.interagirBloco();
					toques.put(p, "ataque");
					if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
					else jogador.item = "ar";
					sprite.setAlpha(0.5f);
				}
				public void aoSoltar(int t, int t2, int p){ acao = false; sprite.setAlpha(0.9f);}
			});
		botoes.put("inv", new Botao(Texturas.texs.get("clique"), 0, 0, jogador.inv.tamSlot, jogador.inv.tamSlot, "inv") {
				public void aoTocar(int t, int t2, int p){
					if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
					else jogador.item = "ar";
					jogador.inv.alternar();
					toques.put(p, "inv");
					sprite.setAlpha(0.5f);
				}
				public void aoSoltar(int t, int t2, int p){sprite.setAlpha(0.9f);}
			});
		botoes.put("receita", new Botao(Texturas.texs.get("receita"), 0, 0, jogador.inv.tamSlot, jogador.inv.tamSlot, "receita") {
				public void aoTocar(int t, int t2, int p){
					if(jogador.inv.itens[jogador.inv.slotSelecionado] == null) return;
					if(jogador.inv.itens[jogador.inv.slotSelecionado].nome.equals("tronco")) {
						jogador.inv.rmItem(jogador.inv.slotSelecionado, 1);
						jogador.inv.addItem("tabua_madeira", 4);
						if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
						else jogador.item = "ar";
						Logs.log("feito tabua");
					} else if(jogador.inv.itens[jogador.inv.slotSelecionado].nome.equals("areia")) {
						jogador.inv.rmItem(jogador.inv.slotSelecionado, 1);
						jogador.inv.addItem("vidro", 1);
						if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
						else jogador.item = "ar";
						Logs.log("feito vidro");
					} else if(jogador.inv.itens[jogador.inv.slotSelecionado].nome.equals("folhas")) {
						jogador.inv.rmItem(jogador.inv.slotSelecionado, 1);
						jogador.inv.addItem("tocha", 1);
						if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.item = jogador.inv.itens[jogador.inv.slotSelecionado].nome;
						else jogador.item = "ar";
						Logs.log("feito tocha");
					}
					toques.put(p, "receita");
					sprite.setAlpha(0.5f);
				}
				public void aoSoltar(int t, int t2, int p){sprite.setAlpha(0.9f);}
			});
		botoes.put("salvar", new Botao(Texturas.texs.get("salvar"), 0, 0, jogador.inv.tamSlot, jogador.inv.tamSlot, "salvar") {
				public void aoTocar(int t, int t2, int p){
					toques.put(p, "salvar");
					sprite.setAlpha(0.5f);
					Mundo.exec.submit(new Runnable() {
						@Override
						public void run() {
							ArquivosUtil.svMundo(Jogo.mundo, jogador);
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									abrirDialogo("seu jogo foi salvo");
									sprite.setAlpha(0.9f);
								}
							});
						}
					});
				}
				public void aoSoltar(int t, int t2, int p){}
			});
	}

	public void configDpad(int v, int h) {
		criarBotoesPadrao();

		final float centroX = espaco + botaoTam * 1.5f;
		final float centroY = espaco + botaoTam * 1.5f;

		for(Botao b : botoes.values()) {
			if(b.nome.equals("direita")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(centroX + espaco, centroY - botaoTam/2);
			} else if(b.nome.equals("esquerda")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(centroX - botaoTam - espaco, centroY - botaoTam/2);
			} else if(b.nome.equals("frente")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(centroX - botaoTam/2, centroY + espaco);
			} else if(b.nome.equals("tras")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(centroX - botaoTam/2, centroY - botaoTam - espaco);
			} else if(b.nome.equals("cima")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(v - botaoTam*1.5f, centroY + espaco);
			} else if(b.nome.equals("baixo")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(v - botaoTam*1.5f, centroY - botaoTam - espaco);
			} else if(b.nome.equals("diagDireita")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(centroX + espaco, centroY + espaco);
			} else if(b.nome.equals("diagEsquerda")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(centroX - botaoTam - espaco, centroY + espaco);
			} else if(b.nome.equals("acao")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(v - botaoTam*1.5f, centroY*2 + espaco);
			} else if(b.nome.equals("ataque")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(v - botaoTam*2.5f, centroY*2 + espaco);
			} else if(b.nome.equals("inv")) {
				b.sprite.setAlpha(0.9f);
				int hotbarX = v / 2 - (jogador.inv.hotbarSlots * jogador.inv.tamSlot) / 2;
				int invX = hotbarX + ((jogador.inv.hotbarSlots) * jogador.inv.tamSlot);
				b.sprite.setPosition(invX, jogador.inv.hotbarY);
			} else if(b.nome.equals("receita")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition(v - botaoTam, h - botaoTam);
			} else if(b.nome.equals("salvar")) {
				b.sprite.setAlpha(0.9f);
				b.sprite.setPosition((v - botaoTam)-botaoTam, h - botaoTam);
			}
			b.hitbox.setPosition(b.sprite.getX(), b.sprite.getY());
		}
		spriteMira.setPosition(v / 2 - spriteMira.getWidth() / 2, h / 2 - spriteMira.getHeight() / 2);
		spriteMira.setAlpha(0.9f);
	}

	public void att(float delta, Mundo mundo) {
		attCamera(camera, jogador.yaw, jogador.tom);

		frenteV.x = camera.direction.x;
		frenteV.z = camera.direction.z;
		frenteV.nor();  
		direitaV.x = frenteV.z;
		direitaV.z = -frenteV.x;

		jogador.velocidade.x = 0;
		jogador.velocidade.z = 0;
		if(jogador.modo != 2) jogador.velocidade.y = 0;

		if(this.frente) jogador.velocidade.add(frenteV.scl(jogador.velo));
		if(this.tras)  jogador.velocidade.sub(frenteV.scl(jogador.velo));
		if(this.esquerda) jogador.velocidade.add(direitaV.scl(jogador.velo));
		if(this.direita) jogador.velocidade.sub(direitaV.scl(jogador.velo));
		if(this.cima) {
			if(jogador.modo != 2 || jogador.noChao || jogador.naAgua) {
				jogador.velocidade.y = jogador.pulo; // pulo
				jogador.noChao = false;
			}
        }
        if(this.baixo) jogador.velocidade.y = -10f;

		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0); 
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0); 

		camera.update();
		sb.begin();
		
		spriteMira.draw(sb);
		
		for(Botao e : botoes.values()) {
			e.porFrame(delta, sb, fonte);
		}
		for(Texto e : textos.values()) {
			e.porFrame(delta, sb, fonte);
		}
		this.jogador.inv.att();
		if(debug) {
			float livre = rt.freeMemory() >> 20;
			float total = rt.totalMemory() >> 20;
			
			float nativaLivre = debugador.getNativeHeapFreeSize() >> 20;
			float nativaTotal = debugador.getNativeHeapSize() >> 20;
			
			fps = Gdx.graphics.getFramesPerSecond();
			
			String[] logsArr = Logs.logs.split("\n");
			StringBuilder construtorLogs = new StringBuilder();
			
			int inicio = Math.max(0, logsArr.length - 15);
			for(int i = inicio; i < logsArr.length; i++) {
				construtorLogs.append(logsArr[i]).append("\n");
			}
			String logsTexto = construtorLogs.toString();

			fonte.draw(sb, String.format(
						   "X: %.1f, Y: %.1f, Z: %.1f\n" +
						   "Mundo: %s\nJogador:\nModo: %s\nSlot: %d\nItem: %s\nNo chão: %b\nNa água: %b\n\n" +
						   "Controles:\nDireita: %b, Esquerda: %b\nFrente: %b, Trás: %b\nCima: %b\nBaixo: %b\nAção: %b\n\n" +
						   "Mundo:\nRaio Chunks: %d\nChunks ativos: %d\nChunks Alteradas: %d\nSeed: %d\nTempo: %.2f\nTick: %.3f\nVelocidade do tempo: %.3f",
						   jogador.posicao.x, jogador.posicao.y, jogador.posicao.z,
						   mundo.nome, 
						   (jogador.modo == 0 ? "espectador" : jogador.modo == 1 ? "criativo" : "sobrevivencia"), 
						   jogador.inv.slotSelecionado, jogador.item, jogador.noChao, jogador.naAgua,
						   this.direita, this.esquerda, this.frente, this.tras, this.cima, this.baixo, this.acao,
						   mundo.RAIO_CHUNKS, mundo.chunks.size(), mundo.chunksMod.size(), mundo.seed, DiaNoiteUtil.tempo, mundo.tick, DiaNoiteUtil.tempo_velo), 
					   50, Gdx.graphics.getHeight() - 100);

			fonte.draw(sb, String.format(
						   "FPS: %d\n" +
						   "Threads ativas: %d\nMemória livre: %.1f MB\nMemória total: %.1f MB\nMemória usada: %.1f MB\nMemória nativa livre: %.1f MB\nMemória nativa total: %.1f MB\nMemória nativa usada: %.1f MB\n" +
						   "Logs:\n%s",
						   fps,
						   Thread.activeCount(), livre, total, total - livre, nativaLivre, nativaTotal, nativaTotal - nativaLivre,
						   logsTexto), 
					   Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() - 100);
		}
		sb.end();
	}

	public static void attCamera(PerspectiveCamera camera, float yaw, float tom) {
		float yawRad = yaw * MathUtils.degRad;
		float tomRad = tom * MathUtils.degRad;

		float cx = MathUtils.cos(tomRad) * MathUtils.sin(yawRad);
		float cy = MathUtils.sin(tomRad);
		float cz = MathUtils.cos(tomRad) * MathUtils.cos(yawRad);

		camera.direction.set(cx, cy, cz).nor();
		camera.up.set(0, 1, 0);
	}

    public void ajustar(int v, int h) {
		for(Botao b : botoes.values()) {
			if(b != null) b.aoAjustar(v, h);
		}
		configDpad(v, h);
		
        camera.viewportWidth = v;
        camera.viewportHeight = h;
        camera.update();

		jogador.inv.aoAjustar(v, h);
        
        sb.getProjectionMatrix().setToOrtho2D(0, 0, v, h);
    }

    public static void liberar() {
		for(Botao e : botoes.values()) {
			e.aoFim();
		}
        sb.dispose();
        fonte.dispose();
    }
	
	public Texto addTexto(String nome, String texto, int x, int y) {
		textos.put(nome, new Texto(texto, x, y) {
				@Override
				public void aoAjustar(int v, int h) {}
		});
		return textos.get(nome);
	}
	
	public Texto addTexto(String nome, String texto, int x, int y, final LuaFunction func) {
		textos.put(nome, new Texto(texto, x, y) {
				@Override
				public void aoAjustar(int v, int h) {
					func.call(LuaValue.valueOf(v), LuaValue.valueOf(h));
				}
			});
		return textos.get(nome);
	}
	
	public void subsTexto(String nome, Texto textoObj) {
		textos.replace(nome, textoObj);
	}
	
	public void rmTexto(String nome) {
		textos.remove(nome);
	}
	
	public void defTextoTam(int escalaX, int escalaY) {
		fonte.getData().setScale(escalaX, escalaY);
	}
	
	public float[] obterTextoTam(int escalaX, int escalaY) {
		BitmapFont.BitmapFontData f = fonte.getData();
		return new float[]{f.scaleX, f.scaleY};
	}
	
	public void rmBotao(String nome) {
		botoes.remove(nome);
	}
	
	public void subsBotao(String nome, Botao botaoObj) {
		botoes.replace(nome, botaoObj);
	}
	
	public Botao addBotao(String textura, float x, float y, float escalaX, float escalaY, String nome, final LuaFunction func) {
		botoes.put(nome, new Botao(Texturas.texs.get(textura), x, y, escalaX, escalaY, nome) {
				@Override
				public void aoTocar(int telaX, int telaY, int p) {
					func.call();
				}
				@Override
				public void aoSoltar(int telaX, int telaY, int p) {}
			});
		return botoes.get(nome);
	}
	
	public Botao addBotao(String textura, float x, float y, float escalaX, float escalaY, String nome, final LuaFunction func, final LuaFunction func2) {
		botoes.put(nome, new Botao(Texturas.texs.get(textura), x, y, escalaX, escalaY, nome) {
				@Override
				public void aoTocar(int telaX, int telaY, int p) {
					func.call();
				}
				@Override
				public void aoSoltar(int telaX, int telaY, int p) {
					func2.call();
				}
			});
		return botoes.get(nome);
	}
	
	public Botao addBotao(String textura, float x, float y, float escalaX, float escalaY, String nome, final LuaFunction func, final LuaFunction func2, final LuaFunction func3) {
		botoes.put(nome, new Botao(Texturas.texs.get(textura), x, y, escalaX, escalaY, nome) {
				@Override
				public void aoTocar(int telaX, int telaY, int p) {
					func.call();
				}
				@Override
				public void aoSoltar(int telaX, int telaY, int p) {
					func2.call();
				}
				@Override
				public void aoAjustar(int v, int h) {
					func3.call(LuaValue.valueOf(v), LuaValue.valueOf(h));
				}
			});
		return botoes.get(nome);
	}
	
	public Botao addBotao(Sprite sprite, float x, float y, float escalaX, float escalaY, String nome, final LuaFunction func, final LuaFunction func2, final LuaFunction func3) {
		botoes.put(nome, new Botao(sprite, x, y, escalaX, escalaY, nome) {
				@Override
				public void aoTocar(int telaX, int telaY, int p) {
					func.call();
				}
				@Override
				public void aoSoltar(int telaX, int telaY, int p) {
					func2.call();
				}
				@Override
				public void aoAjustar(int v, int h) {
					func3.call(LuaValue.valueOf(v), LuaValue.valueOf(h));
				}
			});
		return botoes.get(nome);
	}
	
	public static void abrirDialogo(String titulo) {
		abrirDialogo(titulo, "", "", null, null);
	}
	
	public static void abrirDialogo(String titulo, String padrao, String msg, final LuaFunction func) {
		abrirDialogo(titulo, padrao, msg, func, null);
	}
	
	public static void abrirDialogo(String titulo, String padrao, String msg, final LuaFunction func, final LuaFunction func2) {
		Gdx.input.getTextInput(new Input.TextInputListener() {
				@Override
				public void input(String texto) {
					if(func != null) func.call(LuaValue.valueOf(texto));
				}
				public void canceled() {
					if(func2 != null) func2.call();
				}
			}, titulo, padrao, msg);
	}
	
	@JavascriptInterface
	public static void debug(boolean modo) {
		debug = modo;
	}
	@Override public boolean keyDown(int p){return false;}
	@Override public boolean keyTyped(char p){return false;}
	@Override public boolean keyUp(int p){return false;}
	@Override public boolean mouseMoved(int p, int p1){return false;}
	@Override public boolean scrolled(float p, float p1){return false;}
}
