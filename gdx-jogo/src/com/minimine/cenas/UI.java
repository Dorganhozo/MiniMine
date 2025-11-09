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
import java.util.Map;
import com.minimine.ui.Texto;
import org.luaj.vm2.LuaValue;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.InterUtil;
import com.badlogic.gdx.Input;
import com.minimine.utils.ArquivosUtil;
import com.minimine.Inicio;

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

	public static Logs logs = new Logs();
	
	public float botaoTam = 140f;
	public float espaco = 60f;

	public static Jogador jogador;
	public static boolean debug = false;
	public int fps = 0;
	
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
		
		// abrirChat();
		configDpad();
    }
	
	public boolean chatAberto = false;
	public String ultimaMensagem = "";
	public List<String> mensagens = new ArrayList<String>();

	public void abrirChat() {
		if(chatAberto) return;
		chatAberto = true;

		Gdx.input.getTextInput(new Input.TextInputListener() {
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
	
	public void configDpad() {
		configDpad(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void criarBotoesPadrao() {
		if(!botoes.isEmpty()) return;

		spriteMira = new Sprite(Texturas.texs.get("mira"));
		spriteMira.setSize(50f, 50f);

		botoes.put("direita", new Botao(Texturas.texs.get("botao_d"), 0, 0, botaoTam, botaoTam, "direita") {
				public void aoTocar(int t, int t2, int p){ direita = true; }
				public void aoSoltar(int t, int t2, int p){ direita = false; }
			});
		botoes.put("esquerda", new Botao(Texturas.texs.get("botao_e"), 0, 0, botaoTam, botaoTam, "esquerda") {
				public void aoTocar(int t, int t2, int p){ esquerda = true; }
				public void aoSoltar(int t, int t2, int p){ esquerda = false; }
			});
		botoes.put("frente", new Botao(Texturas.texs.get("botao_f"), 0, 0, botaoTam, botaoTam, "frente") {
				public void aoTocar(int t, int t2, int p){ frente = true; }
				public void aoSoltar(int t, int t2, int p){ frente = false; }
			});
		botoes.put("tras", new Botao(Texturas.texs.get("botao_t"), 0, 0, botaoTam, botaoTam, "tras") {
				public void aoTocar(int t, int t2, int p){ tras = true; }
				public void aoSoltar(int t, int t2, int p){ tras = false; }
			});
		botoes.put("cima", new Botao(Texturas.texs.get("botao_f"), 0, 0, botaoTam, botaoTam, "cima") {
				public void aoTocar(int t, int t2, int p){ cima = true; }
				public void aoSoltar(int t, int t2, int p){ cima = false; }
			});
		botoes.put("baixo", new Botao(Texturas.texs.get("botao_t"), 0, 0, botaoTam, botaoTam, "baixo") {
				public void aoTocar(int t, int t2, int p){ baixo = true; }
				public void aoSoltar(int t, int t2, int p){ baixo = false; }
			});
		botoes.put("acao", new Botao(Texturas.texs.get("clique"), 0, 0, botaoTam, botaoTam, "acao") {
				public void aoTocar(int t, int t2, int p){
					acao = true;
					toques.put(p, "acao");
					jogador.interagirBloco();
				}
				public void aoSoltar(int t, int t2, int p){ acao = false; }
			});
		botoes.put("inv", new Botao(Texturas.texs.get("clique"), 0, 0, jogador.inv.tamSlot, jogador.inv.tamSlot, "inv") {
				public void aoTocar(int t, int t2, int p){
					if(jogador.inv.itens[jogador.inv.slotSelecionado] != null) jogador.blocoSele = jogador.inv.itens[jogador.inv.slotSelecionado].tipo;
					else jogador.blocoSele = 0;
					jogador.inv.alternar();
					toques.put(p, "inv");
				}
				public void aoSoltar(int t, int t2, int p){}
			});
	}

	public void configDpad(int v, int h) {
		criarBotoesPadrao();

		final float centroX = espaco + botaoTam * 1.5f;
		final float centroY = espaco + botaoTam * 1.5f;

		for(Botao b : botoes.values()) {
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
			else if(b.nome.equals("inv")) {
				int hotbarX = v / 2 - (jogador.inv.hotbarSlots * jogador.inv.tamSlot) / 2;
				int invX = hotbarX + ((jogador.inv.hotbarSlots) * jogador.inv.tamSlot);
				b.sprite.setPosition(invX, jogador.inv.hotbarY);
			}
			b.hitbox.setPosition(b.sprite.getX(), b.sprite.getY());
		}
		spriteMira.setPosition(v / 2 - spriteMira.getWidth() / 2, h / 2 - spriteMira.getHeight() / 2);
	}

	public void att(float delta, Mundo mundo) {
		attCamera();

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
			float nativaLivre = Debug.getNativeHeapFreeSize() >> 20;
			float nativaTotal = Debug.getNativeHeapSize() >> 20;
			
			fps = Gdx.graphics.getFramesPerSecond();
			
			String[] logsArr = logs.logs.split("\n");
			StringBuilder construtorLogs = new StringBuilder();

// Pegar os últimos 10 registros de log (ou menos se não houver muitos)
			int inicio = Math.max(0, logsArr.length - 10);
			for (int i = inicio; i < logsArr.length; i++) {
				construtorLogs.append(logsArr[i]).append("\n");
			}
			String logsTexto = construtorLogs.toString();

// Desenhar informações do jogador e mundo
			fonte.draw(sb, String.format(
						   "X: %.1f, Y: %.1f, Z: %.1f\n" +
						   "Mundo: %s\nJogador:\nModo: %s\nSlot: %d\nItem: %s\n\n" +
						   "Controles:\nDireita: %b\nEsquerda: %b\nFrente: %b\nTrás: %b\nCima: %b\nBaixo: %b\nAção: %b\n\n" +
						   "Mundo:\nRaio Chunks: %d\nChunks ativos: %d\nChunks Alteradas: %d\nSeed: %d\nTempo: %.1f\nTick: %.3f",
						   camera.position.x, camera.position.y, camera.position.z,
						   mundo.nome, 
						   (this.jogador.modo == 0 ? "espectador" : this.jogador.modo == 1 ? "criativo" : "sobrevivencia"), 
						   this.jogador.inv.slotSelecionado, this.jogador.item,
						   this.direita, this.esquerda, this.frente, this.tras, this.cima, this.baixo, this.acao,
						   mundo.RAIO_CHUNKS, mundo.chunks.size(), mundo.chunksMod.size(), mundo.seed, DiaNoiteUtil.tempo, mundo.tick), 
					   50, Gdx.graphics.getHeight() - 100);

// Desenhar informações de sistema
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
	
	public void attCamera() {
		attCamera(this.camera, jogador.yaw, jogador.tom);
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
			ArquivosUtil.escrever(Inicio.externo+"/MiniMine/debug/logs.txt", logs);
		}

		@Override
		public void log(String string, String string1, Throwable throwable) {
			logs += string + ": " + string1 + throwable.getMessage() + "\n";
		}
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
	@Override public boolean keyDown(int p){return false;}
	@Override public boolean keyTyped(char p){return false;}
	@Override public boolean keyUp(int p){return false;}
	@Override public boolean mouseMoved(int p, int p1){return false;}
	@Override public boolean scrolled(float p, float p1){return false;}
}
