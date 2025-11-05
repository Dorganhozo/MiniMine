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
import com.badlogic.gdx.ApplicationLogger;
import java.util.List;
import java.util.ArrayList;
import com.minimine.utils.Texturas;
import com.minimine.ui.Botao;
import android.os.Debug;
import org.luaj.vm2.LuaFunction;

public class UI implements InputProcessor {
	public static PerspectiveCamera camera;
	public static List<Evento> eventos = new ArrayList<>();
	public static List<Botao> botoes = new ArrayList<>();
    public static SpriteBatch sb;
    public static BitmapFont fonte;
    
    public boolean esquerda = false, frente = false, tras = false, direita = false, cima = false, baixo = false, acao = false;
	public Sprite spriteMira;
	public int pontoEsq = -1;
    public int pontoDir = -1;
    public Vector2 esqCentro = new Vector2();
    public Vector2 esqPos = new Vector2();
    public Vector2 ultimaDir = new Vector2();

	public float sensi = 0.25f;

	public final HashMap<Integer, CharSequence> toques = new HashMap<>();
    // camera
    public static float yaw = 180f, tom = -20f;

    public int telaV;
    public int telaH;
	public Runtime rt;

	public Logs logs;
	
	public float botaoTam = 140f;
	public float espaco = 60f;

	public static Jogador jogador;
	public static boolean debug = false;
	public int fps = 0;
	
    public UI(Jogador jogador) {
		telaV = Gdx.graphics.getWidth();
		telaH = Gdx.graphics.getHeight();

		logs = new Logs();
		Gdx.app.setApplicationLogger(logs);

        camera = new PerspectiveCamera(120, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 18f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 400f;
        camera.update();

        configDpad(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		sb = new SpriteBatch(); 

		fonte = new BitmapFont();
		fonte.getData().setScale(1.5f);

        Gdx.input.setInputProcessor(this);
		rt = Runtime.getRuntime();
		this.jogador = jogador;
		this.jogador.camera = camera;
		this.jogador.inv = new Inventario();
		for(Evento e : eventos) {
			e.aoIniciar();
		}
		// this.jogador.inv.aberto = true;
		abrirChat();
    }
	
	private boolean chatAberto = false;
	private String ultimaMensagem = "";
	private List<String> mensagens = new ArrayList<String>();

	public void abrirChat() {
		if (chatAberto) return;
		chatAberto = true;

		Gdx.input.getTextInput(new com.badlogic.gdx.Input.TextInputListener() {
				public void input(String texto) {
					if (texto != null && texto.length() > 0) {
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
		for(Evento e : eventos) {
			e.aoTocar(telaX, y, p);
		}
		for(Botao e : botoes) {
			if(e.hitbox.contains(telaX, y)) {
				e.aoTocar(telaX, y, p);
				toques.put(p, e.nome);
				break;
			}
		}
		if(telaX >= telaV / 2 && pontoDir == -1) { 
			pontoDir = p; 
			ultimaDir.set(telaX, y); 
		}
		jogador.inv.aoTocar(telaX, y, p);
		return true;
	}

	@Override
	public boolean touchUp(int telaX, int telaY, int p, int b) {
		int y = Gdx.graphics.getHeight() - telaY;
		for(Evento e : eventos) {
			e.aoSoltar(telaX, y, p);
		}
		CharSequence botao = toques.remove(p);
		if(botao != null) {
			for(Botao e : botoes) {
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
		for(Evento e : eventos) {
			e.aoArrastar(telaX, y, p);
		}
		if(p == pontoDir) {
			float dx = telaX - ultimaDir.x;
			float dy = y - ultimaDir.y;
			yaw -= dx * sensi;
			tom += dy * sensi;
			if(tom > 89f) tom = 89f;
			if(tom < -89f) tom = -89f;
			ultimaDir.set(telaX, y);
		}
		if(toques.containsKey(p)) {
			CharSequence botaoAntigo = toques.get(p);
			// verifica se ainda ta sobre algum botão
			boolean sobreBotao = false;
			for(Botao e : botoes) {
				if(e.hitbox.contains(telaX, y)) {
					sobreBotao = true;
					// se mudou pra um botão diferente
					if(!e.nome.equals(botaoAntigo)) {
						// solta o botão antigo
						if(botaoAntigo != null) {
							for(Botao b : botoes) {
								if(b.nome.equals(botaoAntigo)) {
									b.aoSoltar(telaX, y, p);
									break;
								}
							}
						}
						// pressiona o novo botão
						e.aoTocar(telaX, y, p);
						toques.put(p, e.nome);
					}
					break;
				}
			}
			// se não ta sobre nenhum botão, solta o botão atual
			if(!sobreBotao && botaoAntigo != null) {
				for(Botao e : botoes) {
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
	
	public void configDpad() {
		configDpad(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void addBotao(String textura, float x, float y, float escalaX, float escalaY, String nome, final LuaFunction func, final LuaFunction func2) {
		botoes.add(new Botao(Texturas.texs.get(textura), x, y, escalaX, escalaY, nome) {
				@Override
				public void aoTocar(int telaX, int telaY, int p) {
					func.call();
				}
				@Override
				public void aoSoltar(int telaX, int telaY, int p) {
					func2.call();
				}
			});
	}

	public void criarBotoesPadrao() {
		if(!botoes.isEmpty()) return;

		spriteMira = new Sprite(Texturas.texs.get("mira"));
		spriteMira.setSize(50f, 50f);

		botoes.add(new Botao(Texturas.texs.get("botao_d"), 0, 0, botaoTam, botaoTam, "direita") {
				public void aoTocar(int t, int t2, int p){ direita = true; }
				public void aoSoltar(int t, int t2, int p){ direita = false; }
			});
		botoes.add(new Botao(Texturas.texs.get("botao_e"), 0, 0, botaoTam, botaoTam, "esquerda") {
				public void aoTocar(int t, int t2, int p){ esquerda = true; }
				public void aoSoltar(int t, int t2, int p){ esquerda = false; }
			});
		botoes.add(new Botao(Texturas.texs.get("botao_f"), 0, 0, botaoTam, botaoTam, "frente") {
				public void aoTocar(int t, int t2, int p){ frente = true; }
				public void aoSoltar(int t, int t2, int p){ frente = false; }
			});
		botoes.add(new Botao(Texturas.texs.get("botao_t"), 0, 0, botaoTam, botaoTam, "tras") {
				public void aoTocar(int t, int t2, int p){ tras = true; }
				public void aoSoltar(int t, int t2, int p){ tras = false; }
			});
		botoes.add(new Botao(Texturas.texs.get("botao_f"), 0, 0, botaoTam, botaoTam, "cima") {
				public void aoTocar(int t, int t2, int p){ cima = true; }
				public void aoSoltar(int t, int t2, int p){ cima = false; }
			});
		botoes.add(new Botao(Texturas.texs.get("botao_t"), 0, 0, botaoTam, botaoTam, "baixo") {
				public void aoTocar(int t, int t2, int p){ baixo = true; }
				public void aoSoltar(int t, int t2, int p){ baixo = false; }
			});
		botoes.add(new Botao(Texturas.texs.get("clique"), 0, 0, botaoTam, botaoTam, "acao") {
				public void aoTocar(int t, int t2, int p){
					acao = true;
					toques.put(p, "acao");
					jogador.interagirBloco();
				}
				public void aoSoltar(int t, int t2, int p){ acao = false; }
			});
	}

	public void configDpad(int v, int h) {
		criarBotoesPadrao();

		final float centroX = espaco + botaoTam * 1.5f;
		final float centroY = espaco + botaoTam * 1.5f;

		for(Botao b : botoes) {
			if(b.nome.equals("direita"))
				b.sprite.setPosition(centroX + espaco, centroY - botaoTam/2);
			else if(b.nome.equals("esquerda"))
				b.sprite.setPosition(centroX - botaoTam - espaco, centroY - botaoTam/2);
			else if(b.nome.equals("frente"))
				b.sprite.setPosition(centroX - botaoTam/2, centroY + espaco);
			else if(b.nome.equals("tras"))
				b.sprite.setPosition(centroX - botaoTam/2, centroY - botaoTam - espaco);
			else if(b.nome.equals("cima"))
				b.sprite.setPosition(v - botaoTam*1.5f, centroY + espaco);
			else if(b.nome.equals("baixo"))
				b.sprite.setPosition(v - botaoTam*1.5f, centroY - botaoTam - espaco);
			else if(b.nome.equals("acao"))
				b.sprite.setPosition(v - botaoTam*1.5f, centroY*2 + espaco);

			b.hitbox.setPosition(b.sprite.getX(), b.sprite.getY());
		}

		spriteMira.setPosition(v / 2 - spriteMira.getWidth() / 2, h / 2 - spriteMira.getHeight() / 2);
	}

	public void att(float delta, Mundo mundo) {
		attCamera();

		Vector3 frente = new Vector3(camera.direction.x, 0, camera.direction.z).nor();  
		Vector3 direita = new Vector3(frente.z, 0, -frente.x).nor();

		jogador.velocidade.x = 0;
		jogador.velocidade.z = 0;
		if(jogador.modo != 2) jogador.velocidade.y = 0;

		if(this.frente) jogador.velocidade.add(new Vector3(frente).scl(jogador.velo));
		if(this.tras)  jogador.velocidade.sub(new Vector3(frente).scl(jogador.velo));
		if(this.esquerda) jogador.velocidade.add(new Vector3(direita).scl(jogador.velo));
		if(this.direita) jogador.velocidade.sub(new Vector3(direita).scl(jogador.velo));
		if(this.cima) {
			if(jogador.modo != 2 || jogador.noChao) {
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
		
		for(Evento e : eventos) {
			e.porFrame(delta, sb);
		}
		for(Botao e : botoes) {
			e.porFrame(delta, sb);
		}

		this.jogador.inv.att();
		if(debug) {
			float livre = rt.freeMemory() >> 20;
			float total = rt.totalMemory() >> 20;
			float nativaLivre = Debug.getNativeHeapFreeSize() >> 20;
			float nativaTotal = Debug.getNativeHeapSize() >> 20;
			
			fps = Gdx.graphics.getFramesPerSecond();
			
			fonte.draw(sb, String.format("X: %.1f, Y: %.1f, Z: %.1f\nFPS: %d\n"+
			"Threads ativas: %d\nMemória livre: %.1f MB\nMemória total: %.1f MB\nMemória usada: %.1f MB\nMemória nativa livre: %.1f MB\nMemória nativa total: %.1f MB\nMemória nativa usada: %.1f MB\n"+
			"Jogador:\nModo: %s\nSlot: %d\nItem: %s\n\n"+
			"Controles:\nDireita: %b\nEsquerda: %b\nFrente: %b\nTrás: %b\nCima: %b\nBaixo: %b\nAção: %b\n"+
			"Raio Chunks: %d\nChunks ativos: %d\nChunks Alteradas: %d\nSeed: %d\nTempo: %.1f\n"+
			"Logs:\n%s",
			camera.position.x, camera.position.y, camera.position.z, fps,
			Thread.activeCount(), livre, total, total - livre, nativaLivre, nativaTotal, nativaTotal - nativaLivre,
			(this.jogador.modo == 0 ? "espectador" : this.jogador.modo == 1 ? "criativo" : "sobrevivencia"), this.jogador.inv.slotSelecionado, this.jogador.item,
			this.direita, this.esquerda, this.frente, this.tras, this.cima, this.baixo, this.acao,
			mundo.RAIO_CHUNKS, mundo.chunks.size(), mundo.chunksMod.size(), mundo.seed, Mundo.SistemaLuzGlobal.tempo,
			logs.logs), 50, Gdx.graphics.getHeight() - 100);
		}
		sb.end();  
	}

	public void attCamera() {
		float yawRad = yaw * MathUtils.degRad;
		float tomRad = tom * MathUtils.degRad;

		float cx = MathUtils.cos(tomRad) * MathUtils.sin(yawRad);
		float cy = MathUtils.sin(tomRad);
		float cz = MathUtils.cos(tomRad) * MathUtils.cos(yawRad);

		camera.direction.set(cx, cy, cz).nor();
		camera.up.set(0, 1, 0);
	}

    public void ajustar(int v, int h) {
		for(Evento e : eventos) {
			e.aoAjustar(v, h);
		}
        camera.viewportWidth = v;
        camera.viewportHeight = h;
        camera.update();

		jogador.inv.aoAjustar(v, h);
        configDpad(v, h);

        sb.getProjectionMatrix().setToOrtho2D(0, 0, v, h);
    }

    public void liberar() {
		for(Evento e : eventos) {
			e.aoFim();
		}
		for(Botao e : botoes) {
			e.aoFim();
		}
        sb.dispose();
        fonte.dispose();
    }

	public static class Logs implements ApplicationLogger {
		public String logs = "";

		@Override
		public void debug(String string, String string1) {
			logs += string + ": " + string1 + "\n";
		}

		@Override
		public void debug(String string, String string1, Throwable throwable) {
			logs += string + ": " + string1 + throwable.getMessage() + "\n";
		}

		@Override
		public void error(String string, String string1) {
			logs += string + ": " + string1 + "\n";
		}

		@Override
		public void error(String string, String string1, Throwable throwable) {
			logs += string + ": " + string1 + throwable.getMessage() + "\n";
		}
		
		public void log(String msg) {
			Gdx.app.log("", msg);
		}

		@Override
		public void log(String string, String string1) {
			logs += string + ": " + string1 + "\n";
		}

		@Override
		public void log(String string, String string1, Throwable throwable) {
			logs += string + ": " + string1 + throwable.getMessage() + "\n";
		}
	}

	@Override public boolean keyDown(int p){return false;}
	@Override public boolean keyTyped(char p){return false;}
	@Override public boolean keyUp(int p){return false;}
	@Override public boolean mouseMoved(int p, int p1){return false;}
	@Override public boolean scrolled(float p, float p1){return false;}

	public static interface Evento {
		default public void aoTocar(int telaX, int telaY, int ponto) {}
		default public void aoSoltar(int telaX, int telaY, int ponto) {}
		default public void aoArrastar(int telaX, int telaY, int ponto) {}
		default public void aoIniciar() {}
		default public void porFrame(float delta, SpriteBatch sb) {}
		default public void aoFim() {}
		default public void aoAjustar(int vertical, int horizontal) {}
	}
}
