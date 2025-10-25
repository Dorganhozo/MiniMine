package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class UI implements InputProcessor {
	public PerspectiveCamera camera;
	
    public SpriteBatch sb;
    public BitmapFont fonte;

    public Texture texEsquerda, texDireita, texCima, texBaixo, texMira;
    public Sprite spriteEsquerda, spriteDireita, spriteCima, spriteBaixo, spriteMira;
    public Rectangle rectEsquerda, rectDireita, rectCima, rectBaixo;

    public boolean esquerda = false, direita = false, cima = false, baixo = false;
    public float cuboX = 0;
    public float cuboY = 0;

	public int pontoEsq = -1;
    public int pontoDir = -1;
    public Vector2 esqCentro = new Vector2();
    public Vector2 esqPos = new Vector2();
    public Vector2 ultimaDir = new Vector2();

    public Vector3 velo = new Vector3();
    public float veloM = 9f;      // m/s
    public float sensi = 0.25f;
    public float grav = -30f;      // m/s^2

    public boolean noChao = false;
    // camera
    public float yaw = 180f;
    public float tom = -20f;

    public int telaV;
    public int telaH;

    public UI() {
		telaV = Gdx.graphics.getWidth();
		telaH = Gdx.graphics.getHeight();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 18f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        carregarTexturasDPad();
		
		sb = new SpriteBatch(); 

		fonte = new BitmapFont();
		fonte.getData().setScale(2f);

        Gdx.input.setInputProcessor(this);
    }

	@Override
	public boolean touchDown(int telaX, int telaY, int p, int b) {
		int y = Gdx.graphics.getHeight() - telaY;

		if(telaX < telaV / 2 && pontoEsq == -1) {
			pontoEsq = p;
			esqCentro.set(telaX, y);
			esqPos.set(telaX, y);

			if(rectEsquerda.contains(telaX, y)) { esquerda = true; spriteEsquerda.setAlpha(0.7f); }
			if(rectDireita.contains(telaX, y)) { direita = true; spriteDireita.setAlpha(0.7f); }
			if(rectCima.contains(telaX, y)) { cima = true; spriteCima.setAlpha(0.7f); }
			if(rectBaixo.contains(telaX, y)) { baixo = true; spriteBaixo.setAlpha(0.7f); }
		} else if(telaX >= telaV / 2 && pontoDir == -1) {
			pontoDir = p;
			ultimaDir.set(telaX, y);
		}
		return true;
	}

	@Override
	public boolean touchUp(int telaX, int telaY, int p, int b) {
		if(p == pontoEsq) {
			pontoEsq = -1;
			esquerda = direita = cima = baixo = false;
			spriteEsquerda.setAlpha(1f);
			spriteDireita.setAlpha(1f);
			spriteCima.setAlpha(1f);
			spriteBaixo.setAlpha(1f);
		}
		if(p == pontoDir) pontoDir = -1;
		return true;
	}

	@Override
	public boolean touchDragged(int telaX, int telaY, int p) {
		int y = Gdx.graphics.getHeight() - telaY;

		if(p == pontoEsq) {
			if(rectEsquerda.contains(telaX, y)) esquerda = true; else esquerda = false;
			if(rectDireita.contains(telaX, y)) direita = true; else direita = false;
			if(rectCima.contains(telaX, y)) cima = true; else cima = false;
			if(rectBaixo.contains(telaX, y)) baixo = true; else baixo = false;
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
		return true;
	}

    public void carregarTexturasDPad() {
        texEsquerda = new Texture(Gdx.files.internal("ui/botao_e.png"));
        texDireita = new Texture(Gdx.files.internal("ui/botao_d.png"));
        texCima = new Texture(Gdx.files.internal("ui/botao_f.png"));
        texBaixo = new Texture(Gdx.files.internal("ui/botao_t.png"));
        texMira = new Texture(Gdx.files.internal("ui/mira.png"));

        spriteEsquerda = new Sprite(texEsquerda);
        spriteDireita = new Sprite(texDireita);
        spriteCima = new Sprite(texCima);
        spriteBaixo = new Sprite(texBaixo);
        spriteMira = new Sprite(texMira);

        float botaoTam = 140f;
        spriteEsquerda.setSize(botaoTam, botaoTam);
        spriteDireita.setSize(botaoTam, botaoTam);
        spriteCima.setSize(botaoTam, botaoTam);
        spriteBaixo.setSize(botaoTam, botaoTam);
        spriteMira.setSize(60f, 60f);
    }

	public void configurarAreasDPad(int v, int h) {
		float botaoTam = 140f;
		float espaco = 60f;

		float centerX = espaco + botaoTam * 1.5f;
		float centerY = espaco + botaoTam * 1.5f;

		spriteEsquerda.setPosition(centerX - botaoTam - espaco, centerY - botaoTam/2);
		spriteDireita.setPosition(centerX + espaco, centerY - botaoTam/2);
		spriteCima.setPosition(centerX - botaoTam/2, centerY + espaco);
		spriteBaixo.setPosition(centerX - botaoTam/2, centerY - botaoTam - espaco);

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
	}

	public void att(float delta) {
		attCamera();

		float velocidade = 10f * delta;  

		Vector3 frente = new Vector3(camera.direction.x, 0, camera.direction.z).nor();  
		Vector3 direita2 = new Vector3(frente.z, 0, -frente.x).nor();  

		if(cima) camera.position.add(new Vector3(frente).scl(velocidade));
		if(baixo) camera.position.sub(new Vector3(frente).scl(velocidade));
		if(esquerda) camera.position.add(new Vector3(direita2).scl(velocidade));
		if(direita) camera.position.sub(new Vector3(direita2).scl(velocidade));

		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0); 
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0); 

		camera.update();

		sb.begin();  
		
		spriteEsquerda.draw(sb);  
		spriteDireita.draw(sb);  
		spriteCima.draw(sb);  
		spriteBaixo.draw(sb);  
		spriteMira.draw(sb);  

		fonte.draw(sb, String.format("X: %.1f, Y: %.1f, Z: %.1f\nFPS: %d", camera.position.x, camera.position.y, camera.position.z, (int) Gdx.graphics.getFramesPerSecond()), 50, Gdx.graphics.getHeight() - 100);  

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

	@Override public boolean keyDown(int p){return false;}
	@Override public boolean keyTyped(char p){return false;}
	@Override public boolean keyUp(int p){return false;}
	@Override public boolean mouseMoved(int p, int p1){return false;}
	@Override public boolean scrolled(float p, float p1){return false;}
}
