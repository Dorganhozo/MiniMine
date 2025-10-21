package com.minimine;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Jogo implements ApplicationListener, InputProcessor {
    public PerspectiveCamera camera;
    public ModelBatch render;
    public Environment ambiente;
    public Mundo mundo;
    public Model chunkModelo;
    public ModelInstance chunk;

    public int pontoEsq = -1;
    public int pontoDir = -1;
    public Vector2 esqCentro = new Vector2();
    public Vector2 esqPos = new Vector2();
    public Vector2 ultimaDir = new Vector2();

    public Vector3 velo = new Vector3();
    public float veloM = 8f;      // m/s
    public float sensi = 0.25f; // degrees per pixel
    public float grav = -30f;      // m/s^2
    
    public boolean noChao = false;
    // camera
    public float yaw = 180f;
    public float tom = -20f;
	
    public int telaV;
    public int telaH;

    @Override
    public void create() {
        telaV = Gdx.graphics.getWidth();
        telaH = Gdx.graphics.getHeight();

        render = new ModelBatch();

        camera = new PerspectiveCamera(67, telaV, telaH);
        camera.position.set(Mundo.CHUNK_TAM / 2f, 70f, Mundo.CHUNK_TAM * 3f);
        attCamera();

        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        ambiente = new Environment();
        ambiente.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        ambiente.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        mundo = new Mundo(1337);
        ModelBuilder mb = new ModelBuilder();
        chunkModelo = mundo.criarChunk(0, 0, mb);
        chunk = new ModelInstance(chunkModelo);

        Gdx.input.setInputProcessor(this);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
    }

    @Override
    public void render() {
        float delta = Math.min(1/30f, Gdx.graphics.getDeltaTime());

        if(!noChao) velo.y += grav * delta;

        Vector2 joy = obterEsquerda(); // -1..1
        if(joy.len2() > 0.0001f) {
            Vector3 propagar = new Vector3(camera.direction.x, 0f, camera.direction.z);
            propagar.nor();
            Vector3 direita = propagar.cpy().crs(camera.up).nor();

            Vector3 move = new Vector3();
            move.add(propagar.scl(joy.y));
            move.add(direita.scl(joy.x));
            if(move.len2() > 0.00001f) {
                move.nor().scl(veloM);
                velo.x = move.x;
                velo.z = move.z;
            } else {
                velo.x = 0f;
                velo.z = 0f;
            }
        } else {
            velo.x = 0f;
            velo.z = 0f;
        }
        Vector3 deltaPos = new Vector3(velo).scl(delta);
        camera.position.add(deltaPos);
        // colisqo
        float chao = obterAlturaMundo(camera.position.x, camera.position.z);
        if(camera.position.y <= chao + 1.7f) {
            camera.position.y = chao + 1.7f;
            velo.y = 0f;
            noChao = true;
        } else {
            noChao = false;
        }
        attCamera();
        // render
        Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        camera.update();
        render.begin(camera);
        render.render(chunk, ambiente);
        render.end();
    }

    @Override
    public void dispose() {
        render.dispose();
        if(chunkModelo != null) chunkModelo.dispose();
    }

    @Override public void resize(int v, int h) {
        telaV = v;
        telaH = h;
        camera.viewportWidth = v;
        camera.viewportHeight = h;
        camera.update();
    }
    @Override public void pause() {}
    @Override public void resume() {}
	
	// CONTROLES
	
    @Override
    public boolean touchDown(int telaX, int telaY, int ponto, int botao) {
        int yInv = telaH - telaY;
        if(telaX < telaV / 2) {
            if(pontoEsq == -1) {
                pontoEsq = ponto;
				esqCentro.set(telaX, yInv);
                esqPos.set(telaX, yInv);
            }
        } else {
            if(pontoDir == -1) {
                pontoDir = ponto;
                ultimaDir.set(telaX, yInv);
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int telaX, int telaY, int ponto, int botao) {
        if(ponto == pontoEsq) {
            pontoEsq = -1;
            esqPos.set(esqCentro);
        }
        if(ponto == pontoDir) pontoDir = -1;
        return true;
    }

    @Override
    public boolean touchDragged(int telaX, int telaY, int ponto) {
        int yInv = telaH - telaY;
        if(ponto == pontoEsq) {
            esqPos.set(telaX, yInv);
        } else if(ponto == pontoDir) {
            float dx = telaX - ultimaDir.x;
            float dy = yInv - ultimaDir.y;
            yaw -= dx * sensi;
            tom += dy * sensi;
            if(tom > 89f) tom = 89f;
            if(tom < -89f) tom = -89f;
            ultimaDir.set(telaX, yInv);
        }
        return true;
    }

    public Vector2 obterEsquerda() {
        Vector2 s = new Vector2();
        if(pontoEsq == -1) return s;
        s.set(esqPos).sub(esqCentro);
        float maxRadianos = Math.min(telaV, telaH) * 0.20f;
        if(s.len() > maxRadianos) s.nor().scl(maxRadianos);
        s.scl(1f / maxRadianos); // -1..1
        s.y = s.y;
        return s;
    }
	
    public float obterAlturaMundo(float vx, float vz) {
        int xi = MathUtils.floor(vx);
        int zi = MathUtils.floor(vz);
        if(xi < 0 || xi >= Mundo.CHUNK_TAM || zi < 0 || zi >= Mundo.CHUNK_TAM) {
            return 0f; // fora do chunk: considera 0
        }
        for(int y = Mundo.CHUNK_ALTURA - 1; y >= 0; y--) {
            try {
                if(mundo.blocos[xi][y][zi] != 0) {
                    return (float) y + 1f; // topo do bloco
                }
            } catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("[ERRO]: "+e);
            }
        }
        return 0f;
    }

    public void attCamera() {
        float yawRad = yaw * MathUtils.degRad;
        float tomRad = tom * MathUtils.degRad;
        float cx = MathUtils.cos(tomRad) * MathUtils.sin(yawRad);
        float cy = MathUtils.sin(tomRad);
        float cz = MathUtils.cos(tomRad) * MathUtils.cos(yawRad);
        camera.direction.set(cx, cy, cz).nor();
        camera.up.set(0f, 1f, 0f);
        camera.lookAt(camera.position.x + camera.direction.x,
		camera.position.y + camera.direction.y,
		camera.position.z + camera.direction.z);
    }
	
	@Override public boolean keyDown(int c) { return false; }
    @Override public boolean keyUp(int c) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean mouseMoved(int tx, int ty) { return false; }
    @Override public boolean scrolled(float p, float p2) { return false; }
}
