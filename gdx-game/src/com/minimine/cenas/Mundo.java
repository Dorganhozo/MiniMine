package com.minimine.cenas;

import com.minimine.utils.ChunkUtil;
import com.badlogic.gdx.graphics.Texture;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.minimine.cenas.blocos.Luz;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

public class Mundo {
	public Texture atlasGeral;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
	public List<String> texturas = new ArrayList<>();
    public Map<Integer, float[]> atlasUVs = new HashMap<>();

	public static final int TAM_CHUNK = 16, Y_CHUNK = 16;

	public final Vector3 solDir = new Vector3(0.4f, 0.8f, 0.2f).nor();
	public final float luzAmt = 0.25f;
	public final float sombraDensi = 0.15f;

	public ShaderProgram shader;

	public Luz player;
	
	public Chunk chunk = new Chunk(TAM_CHUNK, Y_CHUNK, TAM_CHUNK);

	public Mundo() {
		player = new Luz(0, 15, 0);
		
		texturas.add("grama_topo.png");
		texturas.add("grama_lado.png");
		texturas.add("terra.png");
		texturas.add("pedra.png");
		
        criarAtlas();

		LuzUtil.luzPx = new Pixmap(TAM_CHUNK, TAM_CHUNK, Pixmap.Format.RGB888);
		LuzUtil.luzTextura = new Texture(LuzUtil.luzPx);

		int maxFaces = 16 * 16 * 16 * 6;
		int maxVerts = maxFaces * 4;
		int maxIndices = maxFaces * 6;

		VertexAttribute[] attrs = new VertexAttribute[] {
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_posicao"),
			new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"), // UV da Textura
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord1") // UV da Luz
		};
		chunk.mesh = new Mesh(true, maxVerts, maxIndices, attrs);

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

    public void criarAtlas() {
		int texTam = new Pixmap(Gdx.files.internal(texturas.get(0))).getWidth(); 
		int atlasTam = texTam * 2; 

		Pixmap atlasPx = new Pixmap(atlasTam, atlasTam, Pixmap.Format.RGBA8888);

		for(int i = 0; i < texturas.size(); i++) {
			int x = (i % 2) * texTam;  // 0, texTam, 0, texTam
			int y = (i / 2) * texTam;  // 0, 0, texTam, texTam
			
			Pixmap px = new Pixmap(Gdx.files.internal(texturas.get(i)));

			atlasPx.drawPixmap(px, x, y);

			float u1 = (float)x / atlasTam;
			float v1 = (float)y / atlasTam;
			float u2 = (float)(x + texTam) / atlasTam;
			float v2 = (float)(y + texTam) / atlasTam;

			atlasUVs.put(i, new float[]{u1, v1, u2, v2});
			
			px.dispose();
		}
		atlasGeral = new Texture(atlasPx);
		atlasPx.dispose(); 
		ChunkUtil.atlasUVs = atlasUVs;
	}
	// GERAÇÃO DE DADOS:
	// chamado em show:
	public void gerarChunk() {
		for(int x = 0; x < TAM_CHUNK; x++) {
			for(int z = 0; z < TAM_CHUNK; z++) {
				chunk.chunk[x][15][z] = 1; 
				for(int y = 12; y < 15; y++) chunk.chunk[x][y][z] = 2; 
				for(int y = 0; y < 12; y++) chunk.chunk[x][y][z] = 3; 
			}
		}
		ChunkUtil.attMesh(chunk);

		LuzUtil.addLuz(new Luz(7, 15, 7, new Color(1.0f, 0.0f, 0.0f, 1.0f)), chunk.chunk);
		LuzUtil.addLuz(player, chunk.chunk);
	}

	// chamado render:
	public void att(float delta, PerspectiveCamera camera) {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);

		if(shader == null) return;

		player.x = (int)camera.position.x;
		player.z = (int)camera.position.z;
		player.y = (int)camera.position.y;

		LuzUtil.att(chunk.chunk);

		shader.begin();
		shader.setUniformMatrix("u_projTrans", camera.combined);

		atlasGeral.bind(0);
		shader.setUniformi("u_textura", 0);

		LuzUtil.luzTextura.bind(1);
		shader.setUniformi("u_mapaLuz", 1); 

		shader.setUniformf("u_solDir", solDir); 
		shader.setUniformf("u_luzAmt", luzAmt);
		shader.setUniformf("u_sombraDensi", sombraDensi);

		if(chunk.mesh.getNumIndices() > 0) chunk.mesh.render(shader, GL20.GL_TRIANGLES);

		shader.end();
	}
	// chamado em dispose:
	public void liberar() {
        atlasGeral.dispose();
		chunk.mesh.dispose();
		shader.dispose();
		LuzUtil.liberar();
		texturas.clear();
		texturas = null;
	}
}
