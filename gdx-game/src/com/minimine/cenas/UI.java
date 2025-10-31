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

public class UI implements InputProcessor {
	public PerspectiveCamera camera;
	
    public static SpriteBatch sb;
    public static BitmapFont fonte;

    public Texture texEsquerda, texDireita, texCima, texBaixo, texMira, texAcao;
    public Sprite spriteEsquerda, spriteDireita, spriteFrente, spriteTras, spriteCima, spriteBaixo, spriteMira, spriteAcao;
    public Rectangle rectEsquerda, rectDireita,rectFrente, rectTras, rectCima, rectBaixo, rectAcao;

    public boolean esquerda = false, frente = false, tras = false, direita = false, cima = false, baixo = false, acao = false;
	
	public int pontoEsq = -1;
    public int pontoDir = -1;
    public Vector2 esqCentro = new Vector2();
    public Vector2 esqPos = new Vector2();
    public Vector2 ultimaDir = new Vector2();
	
	public float sensi = 0.25f;
	
	public final HashMap<Integer, CharSequence> toques = new HashMap<>();
    // camera
    public float yaw = 180f;
    public float tom = -20f;

    public int telaV;
    public int telaH;
	public Runtime rt;
	
	public Logs logs;
	
	public Jogador jogador;
	
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

        carregarTexturasDPad();
		
		sb = new SpriteBatch(); 

		fonte = new BitmapFont();
		fonte.getData().setScale(1.5f);

        Gdx.input.setInputProcessor(this);
		rt = Runtime.getRuntime();
		this.jogador = jogador;
		this.jogador.camera = camera;
		this.jogador.inv = new Inventario();
		// this.jogador.inv.aberto = true;
    }

	@Override
	public boolean touchDown(int telaX, int telaY, int p, int b) {
		int y = Gdx.graphics.getHeight() - telaY;
		
		if(rectEsquerda.contains(telaX, y)) { esquerda = true; spriteEsquerda.setAlpha(0.7f); toques.put(p, "esq"); }
		else if(rectDireita.contains(telaX, y)) { direita = true; spriteDireita.setAlpha(0.7f); toques.put(p, "dir"); }
		else if(rectFrente.contains(telaX, y)) { frente = true; spriteFrente.setAlpha(0.7f); toques.put(p, "frente"); }
		else if(rectTras.contains(telaX, y)) { tras = true; spriteTras.setAlpha(0.7f); toques.put(p, "tras"); }
		else if(rectCima.contains(telaX, y)) { cima = true; spriteCima.setAlpha(0.7f); toques.put(p, "cima"); }
		else if(rectBaixo.contains(telaX, y)) { baixo = true; spriteBaixo.setAlpha(0.7f); toques.put(p, "baixo"); }
		else if(rectAcao.contains(telaX, y)) { 
			acao = true; 
			spriteAcao.setAlpha(0.7f); 
			toques.put(p, "acao");
			jogador.interagirBloco();
		} else if(telaX >= telaV / 2 && pontoDir == -1) { 
			pontoDir = p; 
			ultimaDir.set(telaX, y); 
		}
		jogador.inv.toque(telaX, y, p, b, jogador);
		return true;
	}

	@Override
	public boolean touchUp(int telaX, int telaY, int p, int b) {
		CharSequence botao = toques.remove(p);
		if(botao != null) {
			if(botao.equals("esq")) { esquerda = false; spriteEsquerda.setAlpha(1f); }
			else if(botao.equals("dir")) { direita = false; spriteDireita.setAlpha(1f); }
			else if(botao.equals("frente")) { frente = false; spriteFrente.setAlpha(1f); }
			else if(botao.equals("tras")) { tras = false; spriteTras.setAlpha(1f); }
			else if(botao.equals("cima")) { cima = false; spriteCima.setAlpha(1f); }
			else if(botao.equals("baixo")) { baixo = false; spriteBaixo.setAlpha(1f); }
			else if(botao.equals("acao")) { acao = false; spriteAcao.setAlpha(1f); }
		}
		if(p == pontoDir) pontoDir = -1;
		return true;
	}

	@Override
	public boolean touchDragged(int telaX, int telaY, int p) {
		int y = Gdx.graphics.getHeight() - telaY;

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

			if(botaoAntigo != null) {
				if(botaoAntigo.equals("esq")) { esquerda = false; spriteEsquerda.setAlpha(1f); }
				else if(botaoAntigo.equals("dir")) { direita = false; spriteDireita.setAlpha(1f); }
				else if(botaoAntigo.equals("frente")) { frente = false; spriteFrente.setAlpha(1f); }
				else if(botaoAntigo.equals("tras")) { tras = false; spriteTras.setAlpha(1f); }
				else if(botaoAntigo.equals("cima")) { cima = false; spriteCima.setAlpha(1f); }
				else if(botaoAntigo.equals("baixo")) { baixo = false; spriteBaixo.setAlpha(1f); }
				else if(botaoAntigo.equals("acao")) { acao = false; spriteAcao.setAlpha(1f); }
			}
			if(rectEsquerda.contains(telaX, y)) { esquerda = true; spriteEsquerda.setAlpha(0.7f); toques.put(p, "esq"); }
			else if(rectDireita.contains(telaX, y)) { direita = true; spriteDireita.setAlpha(0.7f); toques.put(p, "dir"); }
			else if(rectFrente.contains(telaX, y)) { frente = true; spriteFrente.setAlpha(0.7f); toques.put(p, "frente"); }
			else if(rectTras.contains(telaX, y)) { tras = true; spriteTras.setAlpha(0.7f); toques.put(p, "tras"); }
			else if(rectCima.contains(telaX, y)) { cima = true; spriteCima.setAlpha(0.7f); toques.put(p, "cima"); }
			else if(rectBaixo.contains(telaX, y)) { baixo = true; spriteBaixo.setAlpha(0.7f); toques.put(p, "baixo"); }
			else if(rectAcao.contains(telaX, y)) { acao = true; spriteAcao.setAlpha(0.7f); toques.put(p, "acao"); }
			else toques.put(p, null);
		}
		return true;
	}

    public void carregarTexturasDPad() {
        texEsquerda = new Texture(Gdx.files.internal("ui/botao_e.png"));
        texDireita = new Texture(Gdx.files.internal("ui/botao_d.png"));
		texCima = new Texture(Gdx.files.internal("ui/botao_f.png"));
        texBaixo = new Texture(Gdx.files.internal("ui/botao_t.png"));
        texMira = new Texture(Gdx.files.internal("ui/mira.png"));
		texAcao = new Texture(Gdx.files.internal("ui/clique.png"));

        spriteEsquerda = new Sprite(texEsquerda);
        spriteDireita = new Sprite(texDireita);
		spriteFrente = new Sprite(texCima);
        spriteTras = new Sprite(texBaixo);
        spriteCima = new Sprite(texCima);
        spriteBaixo = new Sprite(texBaixo);
        spriteMira = new Sprite(texMira);
		spriteAcao = new Sprite(texAcao);

        float botaoTam = 140f;
        spriteEsquerda.setSize(botaoTam, botaoTam);
        spriteDireita.setSize(botaoTam, botaoTam);
		spriteFrente.setSize(botaoTam, botaoTam);
        spriteTras.setSize(botaoTam, botaoTam);
        spriteCima.setSize(botaoTam, botaoTam);
        spriteBaixo.setSize(botaoTam, botaoTam);
		spriteAcao.setSize(botaoTam, botaoTam);
        spriteMira.setSize(50f, 50f);
    }

	public void configurarAreasDPad(int v, int h) {
		float botaoTam = 140f;
		float espaco = 60f;

		final float centerX = espaco + botaoTam * 1.5f;
		final float centerY = espaco + botaoTam * 1.5f;

		spriteEsquerda.setPosition(centerX - botaoTam - espaco, centerY - botaoTam/2);
		spriteDireita.setPosition(centerX + espaco, centerY - botaoTam/2);
		spriteFrente.setPosition(centerX - botaoTam/2, centerY + espaco);
		spriteTras.setPosition(centerX - botaoTam/2, centerY - botaoTam - espaco);
		spriteCima.setPosition(v - botaoTam*1.5f, centerY + espaco);
		spriteBaixo.setPosition(v - botaoTam*1.5f, centerY - botaoTam - espaco);
		spriteAcao.setPosition(v - botaoTam*1.5f, centerY*2 + espaco);
		
		spriteMira.setPosition(
			v / 2 - spriteMira.getWidth() / 2, 
			h / 2 - spriteMira.getHeight() / 2 
		);
		rectEsquerda = new Rectangle(
			spriteEsquerda.getX(), 
			spriteEsquerda.getY(), 
			spriteEsquerda.getWidth(), 
			spriteEsquerda.getHeight()
		);
		rectDireita = new Rectangle(
			spriteDireita.getX(), 
			spriteDireita.getY(), 
			spriteDireita.getWidth(), 
			spriteDireita.getHeight()
		);
		rectFrente = new Rectangle(
			spriteFrente.getX(), 
			spriteFrente.getY(), 
			spriteFrente.getWidth(), 
			spriteFrente.getHeight()
		);
		rectTras = new Rectangle(
			spriteTras.getX(), 
			spriteTras.getY(), 
			spriteTras.getWidth(), 
			spriteTras.getHeight()
		);
		rectCima = new Rectangle(
			spriteCima.getX(), 
			spriteCima.getY(), 
			spriteCima.getWidth(), 
			spriteCima.getHeight()
		);
		rectBaixo = new Rectangle(
			spriteBaixo.getX(), 
			spriteBaixo.getY(), 
			spriteBaixo.getWidth(), 
			spriteBaixo.getHeight()
		);
		rectAcao = new Rectangle(
			spriteAcao.getX(), 
			spriteAcao.getY(), 
			spriteAcao.getWidth(), 
			spriteAcao.getHeight()
		);
	}

	public void att(float delta, Mundo mundo) {
		attCamera();

		float velocidade = 5f;  

		Vector3 frente = new Vector3(camera.direction.x, 0, camera.direction.z).nor();  
		Vector3 direita = new Vector3(frente.z, 0, -frente.x).nor();
		
		jogador.velocidade.x = 0;
		jogador.velocidade.z = 0;
		if(jogador.modo != 2) jogador.velocidade.y = 0;

		if(this.frente) jogador.velocidade.add(new Vector3(frente).scl(velocidade));
		if(this.tras)  jogador.velocidade.sub(new Vector3(frente).scl(velocidade));
		if(this.esquerda) jogador.velocidade.add(new Vector3(direita).scl(velocidade));
		if(this.direita) jogador.velocidade.sub(new Vector3(direita).scl(velocidade));
		if(this.cima) {
			if(jogador.modo != 2 || jogador.noChao) {
				jogador.velocidade.y = 15f; // pulo
				jogador.noChao = false;
			}
        }
        if(this.baixo) jogador.velocidade.y = -10f;
        
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0); 
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0); 

		camera.update();
		sb.begin();  
		
		spriteEsquerda.draw(sb);  
		spriteDireita.draw(sb);  
		spriteFrente.draw(sb);  
		spriteTras.draw(sb);  
		spriteCima.draw(sb);  
		spriteBaixo.draw(sb);  
		spriteMira.draw(sb);
		spriteAcao.draw(sb);
		
		this.jogador.inv.att();
		
		float livre = rt.freeMemory() >> 20;
		float total = rt.totalMemory() >> 20;
		float usado = total - livre;

		fonte.draw(sb, String.format("X: %.1f, Y: %.1f, Z: %.1f\nFPS: %d\n"+
		"Memória livre: %.1f MB\nMemória total: %.1f MB\nMemória usada: %.1f MB\nMemória nativa: %d\n"+
		"Controles:\nDireita: %b\nEsquerda: %b\nFrente: %b\nTrás: %b\nCima: %b\nBaixo: %b\nAção: %b\n\nItem: %s\n"+
		"Chunks ativos: %d\n"+
		"Logs:\n%s",
		camera.position.x, camera.position.y, camera.position.z, (int) Gdx.graphics.getFramesPerSecond(),
		livre, total, usado, Gdx.app.getNativeHeap(),
		this.direita, this.esquerda, this.frente, this.tras, this.cima, this.baixo, this.acao, this.jogador.item,
		mundo.chunks.size(),
		logs.logs), 50, Gdx.graphics.getHeight() - 100);
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
        camera.viewportWidth = v;
        camera.viewportHeight = h;
        camera.update();
		
		jogador.inv.ajustar(v, h);
        configurarAreasDPad(v, h);

        sb.getProjectionMatrix().setToOrtho2D(0, 0, v, h);
    }

    public void liberar() {
        sb.dispose();
        fonte.dispose();
        texEsquerda.dispose();
        texDireita.dispose();
        texCima.dispose();
        texBaixo.dispose();
        texMira.dispose();
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
}
