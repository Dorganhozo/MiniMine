package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class TesteUI implements Screen {
    public PerspectiveCamera camera;
    public ModelBatch mb;
    public Environment ambiente;
    public Model modelo;
    public ModelInstance clone;

    public SpriteBatch sb;
    public BitmapFont fonte;
    
    public boolean esquerda = false;
    public boolean direita = false;
    public boolean cima = false;
    public boolean baixo = false;
    public float cuboX = 0;
    public float cuboY = 0;

    @Override
    public void show() {
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        mb = new ModelBatch();
        ambiente = new Environment();
        ambiente.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        ambiente.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();
        modelo = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(0.5f, 0.5f, 0.8f, 1f)), Usage.Position | Usage.Normal);

        clone = new ModelInstance(modelo);

        sb = new SpriteBatch();
        fonte = new BitmapFont();
        fonte.getData().setScale(2f);

        Gdx.input.setInputProcessor(new InputAdapter() {
				@Override
				public boolean touchDown(int x, int y, int p, int b) {
					y = Gdx.graphics.getHeight() - y;
					// BOTÃO ESQUERDA
					if(x < 100 && y < 100) {
						esquerda = true;
					}
					// BOTÃO DIREITA
					else if(x > 200 && x < 300 && y < 100) {
						direita = true;
					}
					// BOTÃO CIMA
					else if(x >= 100 && x <= 200 && y < 100) {
						cima = true;
					}
					// BOTÃO BAIXO
					else if(x >= 100 && x <= 200 && y > 100 && y < 200) {
						baixo = true;
					}
					return true;
				}

				@Override
				public boolean touchUp(int x, int y, int pointer, int button) {
					esquerda = false;
					direita = false;
					cima = false;
					baixo = false;
					return true;
				}
			});
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if(esquerda) cuboX -= 5f * delta;
        if(direita) cuboX += 5f * delta;
        if(cima) cuboY += 5f * delta;
        if(baixo) cuboY -= 5f * delta;
		
		clone.transform.setTranslation(cuboX, cuboY, 0);
        clone.transform.rotate(Vector3.Y, 50 * delta);

        mb.begin(camera);
        mb.render(clone, ambiente);
        mb.end();

        sb.begin();
        fonte.draw(sb, "CONTROLES:", 50, Gdx.graphics.getHeight() - 50);
        fonte.draw(sb, "ESQUERDA", 10, 80);
        fonte.draw(sb, "CIMA", 110, 110);
        fonte.draw(sb, "DIREITA", 210, 80);
        fonte.draw(sb, "BAIXO", 110, 30);
        fonte.draw(sb, "POS: " + cuboX + ", " + cuboY, 50, Gdx.graphics.getHeight() - 100);
		fonte.draw(sb, "FPS: " + Gdx.graphics.getFramesPerSecond(), 50, Gdx.graphics.getHeight() - 150);
        sb.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        sb.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void dispose() {
        mb.dispose();
        modelo.dispose();
        sb.dispose();
        fonte.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
