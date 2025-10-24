package com.minimine.cenas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.minimine.Controles;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public class TesteChunk implements Screen {
	public Controles ctr;
	public MundoTeste mundo;

    @Override
	public void show() {
        mundo = new MundoTeste();
        mundo.gerarChunk();
		ctr = new Controles();
		Gdx.input.setInputProcessor(ctr);
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        mundo.att(delta, ctr.camera);
		ctr.att(delta);
    }

    @Override
    public void dispose() {
		mundo.liberar();
    }

	@Override public void hide() {}
	@Override public void pause() {}
	@Override public void resize(int v, int h) {
		ctr.ajustar(v, h);
	}
	@Override public void resume() {}
}

class MundoTeste {
	public Texture grama_topo, grama_lado, terra, pedra;
	public Mesh chunkMesh;
	public byte[][][] chunk = new byte[16][16][16];
	public ImmediateModeRenderer20 render;

	public MundoTeste() {
		grama_topo = new Texture(Gdx.files.internal("grama_topo.png"));
		grama_lado = new Texture(Gdx.files.internal("grama_lado.png"));
		terra = new Texture(Gdx.files.internal("terra.png"));
		pedra = new Texture(Gdx.files.internal("pedra.png"));

		render = new ImmediateModeRenderer20(16 * 16 * 16 * 6 * 6, false, true, 1); 
	}

	public void renderFace(float x, float y, float z, int faceId, float luz) {
		float tam = 1.0f;
		float X = x * tam;
		float Y = y * tam;
		float Z = z * tam;

		float r = luz, g = luz, b = luz, a = 1.0f;

		float u0=0, v0=0, u1=1, v1=1;

		switch(faceId) {
			case 0: // topo +Y (u=X, v=Z)
				// triangulo 1
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(0, 1, 0); render.vertex(X+tam, Y+tam, Z+tam); // P1 (1,1,1)
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(0, 1, 0); render.vertex(X+tam, Y+tam, Z);    // P4 (1,1,0)
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(0, 1, 0); render.vertex(X, Y+tam, Z);       // P3 (0,1,0)
				// trangulo 2
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(0, 1, 0); render.vertex(X, Y+tam, Z);       // P3 (0,1,0)
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(0, 1, 0); render.vertex(X, Y+tam, Z+tam);    // P2 (0,1,1)
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(0, 1, 0); render.vertex(X+tam, Y+tam, Z+tam); // P1 (1,1,1)
				break;
			case 1: // baixo -Y
				r = 0.5f; g = 0.5f; b = 0.5f;
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(0, -1, 0); render.vertex(X+tam, Y, Z);      // P1 (1,0,0)
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(0, -1, 0); render.vertex(X+tam, Y, Z+tam);  // P4 (1,0,1)
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(0, -1, 0); render.vertex(X, Y, Z+tam);       // P3 (0,0,1)

				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(0, -1, 0); render.vertex(X, Y, Z+tam);       // P3 (0,0,1)
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(0, -1, 0); render.vertex(X, Y, Z);           // P2 (0,0,0)
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(0, -1, 0); render.vertex(X+tam, Y, Z);      // P1 (1,0,0)
				break;
			case 2: // lado leste +X
				r = 0.8f; g = 0.8f; b = 0.8f;
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(1, 0, 0); render.vertex(X+tam, Y+tam, Z+tam);
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(1, 0, 0); render.vertex(X+tam, Y, Z+tam);
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(1, 0, 0); render.vertex(X+tam, Y, Z);

				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(1, 0, 0); render.vertex(X+tam, Y, Z);
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(1, 0, 0); render.vertex(X+tam, Y+tam, Z);
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(1, 0, 0); render.vertex(X+tam, Y+tam, Z+tam);
				break;
			case 3: // lado oeste -X
				r = 0.7f; g = 0.7f; b = 0.7f;
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(-1, 0, 0); render.vertex(X, Y+tam, Z);
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(-1, 0, 0); render.vertex(X, Y, Z);
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(-1, 0, 0); render.vertex(X, Y, Z+tam);

				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(-1, 0, 0); render.vertex(X, Y, Z+tam);
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(-1, 0, 0); render.vertex(X, Y+tam, Z+tam);
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(-1, 0, 0); render.vertex(X, Y+tam, Z);
				break;
			case 4: // lado norte +Z
				r = 0.9f; g = 0.9f; b = 0.9f;
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(0, 0, 1); render.vertex(X+tam, Y+tam, Z+tam);
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(0, 0, 1); render.vertex(X, Y+tam, Z+tam);
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(0, 0, 1); render.vertex(X, Y, Z+tam);

				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(0, 0, 1); render.vertex(X, Y, Z+tam);
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(0, 0, 1); render.vertex(X+tam, Y, Z+tam);
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(0, 0, 1); render.vertex(X+tam, Y+tam, Z+tam);
				break;
			case 5: // lado sul -Z
				r = 0.75f; g = 0.75f; b = 0.75f;
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(0, 0, -1); render.vertex(X+tam, Y, Z);
				render.color(r, g, b, a); render.texCoord(u1, v1); render.normal(0, 0, -1); render.vertex(X, Y, Z);
				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(0, 0, -1); render.vertex(X, Y+tam, Z);

				render.color(r, g, b, a); render.texCoord(u1, v0); render.normal(0, 0, -1); render.vertex(X, Y+tam, Z);
				render.color(r, g, b, a); render.texCoord(u0, v0); render.normal(0, 0, -1); render.vertex(X+tam, Y+tam, Z);
				render.color(r, g, b, a); render.texCoord(u0, v1); render.normal(0, 0, -1); render.vertex(X+tam, Y, Z);
				break;
		}
	}
	// chamado uma vez
	public void gerarChunk() {
		// 1 = grama, 2 = terra, 3 = pedra
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				chunk[x][15][z] = 1; 

				for(int y = 12; y < 15; y++) chunk[x][y][z] = 2; 
				for(int y = 0; y < 12; y++) chunk[x][y][z] = 3; 
			}
		}
	}
	// chamado em render
	public void att(float delta, PerspectiveCamera camera) {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);

		Texture textura = null;

		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				for(int z = 0; z < 16; z++) {
					int bloco = chunk[x][y][z];
					if(bloco != 0) { 
						Texture topo = null, lado = null, baixo = null;

						switch(bloco) {
							case 1: 
								topo = grama_topo; lado = grama_lado; baixo = terra; 
								break;
							case 2:
								topo = terra; lado = terra; baixo = terra; 
								break;
							case 3:
								topo = pedra; lado = pedra; baixo = pedra; 
								break;
							default: 
								continue;
						}
						// topo (+Y)
						if(y == 15 || chunk[x][y + 1][z] == 0) {
							if(textura != topo) {
								if(textura != null) render.end();
								topo.bind();
								render.begin(camera.combined, GL20.GL_TRIANGLES);
								textura = topo;
							}
							renderFace(x, y, z, 0, 1.0f); 
						}
						// baixo (-Y)
						if(y == 0 || chunk[x][y - 1][z] == 0) {
							if(textura != baixo) {
								if(textura != null) render.end();
								baixo.bind();
								render.begin(camera.combined, GL20.GL_TRIANGLES);
								textura = baixo;
							}
							renderFace(x, y, z, 1, 1.0f);
						} 
						// leste (+X)
						if(x == 15 || chunk[x + 1][y][z] == 0) {
							if(textura != lado) {
								if(textura != null) render.end();
								lado.bind();
								render.begin(camera.combined, GL20.GL_TRIANGLES);
								textura = lado;
							}
							renderFace(x, y, z, 2, 1.0f); 
						}
						// oeste (-X)
						if(x == 0 || chunk[x - 1][y][z] == 0) {
							if(textura != lado) {
								if(textura != null) render.end();
								lado.bind();
								render.begin(camera.combined, GL20.GL_TRIANGLES);
								textura = lado;
							}
							renderFace(x, y, z, 3, 1.0f); 
						}
						// norte (+Z)
						if(z == 15 || chunk[x][y][z + 1] == 0) {
							if(textura != lado) {
								if(textura != null) render.end();
								lado.bind();
								render.begin(camera.combined, GL20.GL_TRIANGLES);
								textura = lado;
							}
							renderFace(x, y, z, 4, 1.0f); 
						}
						// sul (-Z)
						if(z == 0 || chunk[x][y][z - 1] == 0) {
							if(textura != lado) {
								if(textura != null) render.end();
								lado.bind();
								render.begin(camera.combined, GL20.GL_TRIANGLES);
								textura = lado;
							}
							renderFace(x, y, z, 5, 1.0f); 
						}
					}
				}
			}
		}
		if(textura != null) render.end();
	}
	// chamado em dispose
	public void liberar() {
		grama_topo.dispose();
		grama_lado.dispose();
		terra.dispose();
		pedra.dispose();
		chunkMesh.dispose();
		render.dispose();
	}
}
