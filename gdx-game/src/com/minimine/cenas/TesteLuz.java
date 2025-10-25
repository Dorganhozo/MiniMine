package com.minimine.cenas;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import java.util.Arrays;
import com.badlogic.gdx.graphics.Pixmap;
import com.minimine.utils.FloatArrayUtil;
import com.minimine.utils.IntArrayUtil;
import com.minimine.cenas.blocos.Luz;
import com.badlogic.gdx.Screen;
import com.minimine.Controles;

public class TesteLuz implements Screen {
	public Controles ctr;
	public MundoLuz mundo;

    @Override
	public void show() {
        mundo = new MundoLuz();
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

class MundoLuz {
	public Texture grama_topo, grama_lado, terra, pedra;
	public Mesh meshGramaTopo, meshGramaLado, meshTerra, meshPedra;

	public static final int TAM_CHUNK = 16, Y_CHUNK = 16;
	public final byte[][][] chunk = new byte[TAM_CHUNK][Y_CHUNK][TAM_CHUNK];

	public final Vector3 solDir = new Vector3(0.4f, 0.8f, 0.2f).nor();
	public final float luzAmt = 0.25f;
	public final float sombraDensi = 0.15f;

	public ShaderProgram shader;

	public Luz player;

	public MundoLuz() {
		player = new Luz(0, 15, 0);
		// texturas:
		grama_topo = new Texture(Gdx.files.internal("blocos/grama_topo.png"));
		grama_lado = new Texture(Gdx.files.internal("blocos/grama_lado.png"));
		terra = new Texture(Gdx.files.internal("blocos/terra.png"));
		pedra = new Texture(Gdx.files.internal("blocos/pedra.png"));
		LuzUtil.luzPx = new Pixmap(TAM_CHUNK, TAM_CHUNK, Pixmap.Format.RGB888);
		LuzUtil.luzTextura = new Texture(LuzUtil.luzPx);

		int maxFaces = 16 * 16 * 16 * 6;
		int maxVerts = maxFaces * 4;
		int maxIndices = maxFaces * 6;

		VertexAttribute[] atribus = new VertexAttribute[] {
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_posicao"),
			new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord1")
		};
		meshGramaTopo = new Mesh(true, maxVerts, maxIndices, atribus);
		meshGramaLado = new Mesh(true, maxVerts, maxIndices, atribus);
		meshTerra = new Mesh(true, maxVerts, maxIndices, atribus);
		meshPedra = new Mesh(true, maxVerts, maxIndices, atribus);

		String vert =
			"attribute vec3 a_posicao;\n"+
			"attribute vec3 a_normal;\n"+
			"attribute vec2 a_texCoord0;\n"+
			"attribute vec2 a_texCoord1;\n"+
			"uniform mat4 u_projTrans;\n"+
			"uniform vec3 u_solDir;\n"+
			"uniform float u_sombraDensi;\n"+
			"varying vec4 v_cor;\n"+
			"varying vec2 v_texCoord;\n"+
			"varying vec2 v_luzTexCoord;\n"+
			"void main() {\n"+
			"  float difuso = max(0.0, dot(a_normal, u_solDir)) * u_sombraDensi;\n"+
			"  v_cor = vec4(difuso, difuso, difuso, 1.0);\n"+ 
			"  v_texCoord = a_texCoord0;\n"+
			"  v_luzTexCoord = a_texCoord1;\n"+
			"  gl_Position = u_projTrans * vec4(a_posicao, 1.0);\n"+
			"}";

		String frag =
			"#ifdef GL_ES\n"+
			"precision mediump float;\n"+
			"#endif\n"+
			"varying vec4 v_cor;\n"+ 
			"varying vec2 v_texCoord;\n"+
			"varying vec2 v_luzTexCoord;\n"+
			"uniform sampler2D u_textura;\n"+
			"uniform sampler2D u_mapaLuz;\n"+
			"uniform float u_luzAmt;\n"+
			"void main() {\n"+
			"  vec3 blocoLuz = texture2D(u_mapaLuz, v_luzTexCoord).rgb;\n"+
			"  vec3 luzFinal = vec3(u_luzAmt) + blocoLuz + v_cor.rgb;\n"+
			"  vec4 tex = texture2D(u_textura, v_texCoord);\n"+
			"  gl_FragColor = tex * vec4(clamp(luzFinal, 0.0, 1.0), 1.0);\n"+
			"}";

		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(vert, frag);
		if(!shader.isCompiled()) {
			Gdx.app.log("Mundo", "Shader compile error: " + shader.getLog());
		}
	}
	// GERAÇÃO DE DADOS:
	// chamado em show:
	public void gerarChunk() {
		for(int x = 0; x < TAM_CHUNK; x++) {
			for(int z = 0; z < TAM_CHUNK; z++) {
				chunk[x][15][z] = 1;
				for(int y = 12; y < 15; y++) chunk[x][y][z] = 2;
				for(int y = 0; y < 12; y++) chunk[x][y][z] = 3;
			}
		}
		attMesh();

		LuzUtil.addLuz(new Luz(7, 15, 7, new Color(1.0f, 0.0f, 0.0f, 1.0f)), chunk);
		LuzUtil.addLuz(player, chunk);
	}

	public boolean ehSolido(int x, int y, int z) {
		if(x < 0 || x >= TAM_CHUNK || y < 0 || y >= TAM_CHUNK || z < 0 || z >= TAM_CHUNK) return false;
		return chunk[x][y][z] != 0;
	}

	public void att(float delta, PerspectiveCamera camera) {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);

		if(shader == null) return;

		player.x = (int)camera.position.x;
		player.z = (int)camera.position.z;
		player.y = (int)camera.position.y;

		LuzUtil.att(chunk);

		shader.begin();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		// textura padrao
		grama_topo.bind(0);
		shader.setUniformi("u_textura", 0);
		// mapa de luz
		LuzUtil.luzTextura.bind(1);
		shader.setUniformi("u_mapaLuz", 1); 
		// iluminacao dimanica
		shader.setUniformf("u_solDir", solDir); 
		shader.setUniformf("u_luzAmt", luzAmt);
		shader.setUniformf("u_sombraDensi", sombraDensi);
		// render dos meshes
		if(meshGramaTopo.getNumIndices() > 0) meshGramaTopo.render(shader, GL20.GL_TRIANGLES);
		grama_lado.bind(0);
		if(meshGramaLado.getNumIndices() > 0) meshGramaLado.render(shader, GL20.GL_TRIANGLES);
		terra.bind(0);
		if(meshTerra.getNumIndices() > 0) meshTerra.render(shader, GL20.GL_TRIANGLES);
		pedra.bind(0);
		if(meshPedra.getNumIndices() > 0) meshPedra.render(shader, GL20.GL_TRIANGLES);

		shader.end();
	}
	// chamado em dispose:
	public void liberar() {
		grama_topo.dispose();
		grama_lado.dispose();
		terra.dispose();
		pedra.dispose();
		meshGramaTopo.dispose();
		meshGramaLado.dispose();
		meshTerra.dispose();
		meshPedra.dispose();
		shader.dispose();
		LuzUtil.liberar();
	}
	// CALCULOS PESADOS:
	public void attMesh() {
		FloatArrayUtil gramaTopoVerts = new FloatArrayUtil();
		IntArrayUtil gramaTopoIdc = new IntArrayUtil();
		FloatArrayUtil gramaLadoVerts = new FloatArrayUtil();
		IntArrayUtil gramaLadoIdc = new IntArrayUtil();
		FloatArrayUtil terraVerts = new FloatArrayUtil();
		IntArrayUtil terraIdc = new IntArrayUtil();
		FloatArrayUtil pedraVerts = new FloatArrayUtil();
		IntArrayUtil pedraIdc = new IntArrayUtil();

		for(int x = 0; x < TAM_CHUNK; x++) {
			for(int y = 0; y < TAM_CHUNK; y++) {
				for(int z = 0; z < TAM_CHUNK; z++) {
					int bloco = chunk[x][y][z];
					if(bloco == 0 || bloco == 4) continue;

					Texture topo = null, lado = null, baixo = null;
					switch(bloco) {
						case 1: topo = grama_topo; lado = grama_lado; baixo = terra; break;
						case 2: topo = terra; lado = terra; baixo = terra; break;
						case 3: topo = pedra; lado = pedra; baixo = pedra; break;
					}
					FloatArrayUtil vertsTopo = gramaTopoVerts;
					IntArrayUtil idcTopo = gramaTopoIdc;
					FloatArrayUtil vertsLado = obterVertsTex(lado, gramaLadoVerts, terraVerts, pedraVerts);
					IntArrayUtil idcLado = obterIndicerTex(lado, gramaLadoIdc, terraIdc, pedraIdc);
					FloatArrayUtil vertsBaixo = obterVertsTex(baixo, gramaLadoVerts, terraVerts, pedraVerts);
					IntArrayUtil idcBaixo = obterIndicerTex(baixo, gramaLadoIdc, terraIdc, pedraIdc);

					if(y == TAM_CHUNK - 1 || !ehSolido(x, y + 1, z)) addFace(x,y,z,0, topo, vertsTopo, idcTopo);
					if(y == 0 || !ehSolido(x, y - 1, z)) addFace(x,y,z,1, baixo, vertsBaixo, idcBaixo);
					if(x == TAM_CHUNK - 1 || !ehSolido(x + 1, y, z)) addFace(x,y,z,2, lado, vertsLado, idcLado);
					if(x == 0 || !ehSolido(x - 1, y, z)) addFace(x,y,z,3, lado, vertsLado, idcLado);
					if(z == TAM_CHUNK - 1 || !ehSolido(x, y, z + 1)) addFace(x,y,z,4, lado, vertsLado, idcLado);
					if(z == 0 || !ehSolido(x, y, z - 1)) addFace(x,y,z,5, lado, vertsLado, idcLado);
				}
			}
		}
		defMesh(meshGramaTopo, gramaTopoVerts, gramaTopoIdc);
		defMesh(meshGramaLado, gramaLadoVerts, gramaLadoIdc);
		defMesh(meshTerra, terraVerts, terraIdc);
		defMesh(meshPedra, pedraVerts, pedraIdc);
	}

	public void addFace(int x, int y, int z, int faceId, Texture tex, FloatArrayUtil verts, IntArrayUtil idc) {
		float tam = 1f;
		float X = x * tam;
		float Y = y * tam;
		float Z = z * tam;

		float[][] v = new float[4][3];
		float[] normal = new float[3];
		float[][] uv = new float[4][2];

		switch(faceId) {
			case 0: // topo
				v[0] = new float[]{X+tam, Y+tam, Z}; v[1] = new float[]{X, Y+tam, Z}; v[2] = new float[]{X, Y+tam, Z+tam}; v[3] = new float[]{X+tam, Y+tam, Z+tam};
				normal = new float[]{0,1,0};
				uv = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 1: // baixo
				v[0] = new float[]{X+tam, Y, Z+tam}; v[1] = new float[]{X, Y, Z+tam}; v[2] = new float[]{X, Y, Z}; v[3] = new float[]{X+tam, Y, Z};
				normal = new float[]{0,-1,0};
				uv = new float[][]{{1,0},{0,0},{0,1},{1,1}};
				break;
			case 2: // +X
				v[0] = new float[]{X+tam, Y, Z+tam}; v[1] = new float[]{X+tam, Y, Z}; v[2] = new float[]{X+tam, Y+tam, Z}; v[3] = new float[]{X+tam, Y+tam, Z+tam};
				normal = new float[]{1,0,0};
				uv = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 3: // -X
				v[0] = new float[]{X, Y, Z}; v[1] = new float[]{X, Y, Z+tam}; v[2] = new float[]{X, Y+tam, Z+tam}; v[3] = new float[]{X, Y+tam, Z};
				normal = new float[]{-1,0,0};
				uv = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 4: // +Z
				v[0] = new float[]{X, Y+tam, Z+tam}; v[1] = new float[]{X, Y, Z+tam}; v[2] = new float[]{X+tam, Y, Z+tam}; v[3] = new float[]{X+tam, Y+tam, Z+tam};
				normal = new float[]{0,0,1};
				uv = new float[][]{{0,0},{0,1},{1,1},{1,0}};
				break;
			case 5: // -Z
				v[0] = new float[]{X, Y, Z}; v[1] = new float[]{X, Y+tam, Z}; v[2] = new float[]{X+tam, Y+tam, Z}; v[3] = new float[]{X+tam, Y, Z};
				normal = new float[]{0,0,-1};
				uv = new float[][]{{0,1},{0,0},{1,0},{1,1}};
				break;
		}
		int vertConta = verts.tam / 10; 

		float uv_luz_u = (x + 0.5f) / TAM_CHUNK;
		float uv_luz_v = (z + 0.5f) / TAM_CHUNK;

		for(int i=0;i<4;i++) {
			float vx=v[i][0], vy=v[i][1], vz=v[i][2];
			float[] n = normal;

			verts.add(vx); verts.add(vy); verts.add(vz); 
			verts.add(n[0]); verts.add(n[1]); verts.add(n[2]); 
			verts.add(uv[i][0]); verts.add(uv[i][1]); 
			verts.add(uv_luz_u); verts.add(uv_luz_v);
		}
		idc.add(vertConta + 0); idc.add(vertConta + 1); idc.add(vertConta + 2);
		idc.add(vertConta + 2); idc.add(vertConta + 3); idc.add(vertConta + 0);
	}

	public void defMesh(Mesh mesh, FloatArrayUtil verts, IntArrayUtil idc) {
		if(verts.tam == 0) {
			mesh.setVertices(new float[0]);
			mesh.setIndices(new short[0]);
			return;
		}
		float[] vArr = verts.praArray();
		int[] iArr = idc.praArray();
		short[] sIdc = new short[iArr.length];
		for(int i = 0; i < iArr.length; i++) sIdc[i] = (short) iArr[i];
		mesh.setVertices(vArr);
		mesh.setIndices(sIdc);
	}

	public FloatArrayUtil obterVertsTex(Texture t, FloatArrayUtil g, FloatArrayUtil ter, FloatArrayUtil p) {
		if(t == grama_lado) return g;
		if(t == terra) return ter;
		return p;
	}
	public IntArrayUtil obterIndicerTex(Texture t, IntArrayUtil g, IntArrayUtil ter, IntArrayUtil p) {
		if(t == grama_lado) return g;
		if(t == terra) return ter;
		return p;
	}
}
