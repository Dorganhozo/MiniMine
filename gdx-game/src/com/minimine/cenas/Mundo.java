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
import com.badlogic.gdx.math.Matrix4;

public class Mundo {
	public Texture atlasGeral;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
	public List<String> texturas = new ArrayList<>();
    public final Map<Integer, float[]> atlasUVs = new HashMap<>();
	public final Map<CharSequence, Chunk> chunks = new HashMap<>();
	public Map<CharSequence, Chunk> chunksAtivos = new HashMap<>();

	public static final int TAM_CHUNK = 16, Y_CHUNK = 16, RAIO_CHUNKS = 3;

	public final Vector3 solDir = new Vector3(0.4f, 0.8f, 0.2f).nor();
	public final float luzAmt = 0.25f;
	public final float sombraDensi = 0.15f;

	public ShaderProgram shader;

	public Luz player;
	
	public int maxVerts, maxIndices, maxFaces;
	public VertexAttribute[] atriburs;

	public Mundo() {
		player = new Luz(0, 15, 0);
		
		texturas.add("blocos/grama_topo.png");
		texturas.add("blocos/grama_lado.png");
		texturas.add("blocos/terra.png");
		texturas.add("blocos/pedra.png");
		
        criarAtlas();

		LuzUtil.luzPx = new Pixmap(TAM_CHUNK, TAM_CHUNK, Pixmap.Format.RGB888);
		LuzUtil.luzTextura = new Texture(LuzUtil.luzPx);

		maxFaces = TAM_CHUNK * Y_CHUNK * TAM_CHUNK * 6;
		maxVerts = maxFaces * 4;
		maxIndices = maxFaces * 6;

		atriburs = new VertexAttribute[] {
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_posicao"),
			new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"), // UV da textura
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord1") // UV da luz
		};

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

	public void attChunks(int playerX, int playerZ) {
		int chunkX = playerX / TAM_CHUNK;
		int chunkZ = playerZ / TAM_CHUNK;

		Map<CharSequence, Chunk> novosAtivos = new HashMap<>();

		for(int x = chunkX - RAIO_CHUNKS; x <= chunkX + RAIO_CHUNKS; x++) {
			for(int z = chunkZ - RAIO_CHUNKS; z <= chunkZ + RAIO_CHUNKS; z++) {
				String chave = x + "," + z;
				if(chunksAtivos.containsKey(chave)) {
					novosAtivos.put(chave, chunksAtivos.get(chave));
				} else {
					novosAtivos.put(chave, gerarChunk(x, z));
				}
			}
		}
		for(CharSequence chave : chunksAtivos.keySet()) {
			if(!novosAtivos.containsKey(chave)) {
				Chunk c = chunksAtivos.get(chave);
				c.mesh.dispose(); // libera mesh
			}
		}
		chunksAtivos = novosAtivos;
	}
	// GERAÇÃO DE DADOS:
	// chamado em show:
	public Chunk gerarChunk(int... coords) {
		Chunk chunk = new Chunk();
		chunk.mesh = new Mesh(true, maxVerts, maxIndices, atriburs);
		for(int x = 0; x < TAM_CHUNK; x++) {
			for(int z = 0; z < TAM_CHUNK; z++) {
				chunk.chunk[x][15][z] = 1; 
				for(int y = 12; y < 15; y++) chunk.chunk[x][y][z] = 2; 
				for(int y = 0; y < 12; y++) chunk.chunk[x][y][z] = 3; 
			}
		}
		ChunkUtil.attMesh(chunk);
		
		Matrix4 m = new Matrix4();
		float x = coords[0] * TAM_CHUNK, z = coords[1] * TAM_CHUNK;
		m.setToTranslation(x, 0, z);
		
		chunk.mesh.transform(m);
		chunks.put(coords[0]+","+coords[1], chunk);
		return chunk;
	}
	// chamado render:
	public void att(float delta, PerspectiveCamera camera) {
		if(shader == null) return;

		player.x = (int)camera.position.x;
		player.z = (int)camera.position.z;
		player.y = (int)camera.position.y;
		
		attChunks(player.x, player.z);

		shader.begin();
		shader.setUniformMatrix("u_projTrans", camera.combined);

		atlasGeral.bind(0);
		shader.setUniformi("u_textura", 0);

		LuzUtil.luzTextura.bind(1);
		shader.setUniformi("u_mapaLuz", 1); 

		shader.setUniformf("u_solDir", solDir); 
		shader.setUniformf("u_luzAmt", luzAmt);
		shader.setUniformf("u_sombraDensi", sombraDensi);
		for(Chunk chunk : chunksAtivos.values()) {	
			chunk.attLuz();
			if(chunk.mesh.getNumIndices() > 0) chunk.mesh.render(shader, GL20.GL_TRIANGLES);
		}
		shader.end();
	}
	// chamado em dispose:
	public void liberar() {
        atlasGeral.dispose();
		for(Chunk chunk : chunks.values()) chunk.mesh.dispose();
		shader.dispose();
		LuzUtil.liberar();
		texturas.clear();
		texturas = null;
		chunks.clear();
		chunksAtivos.clear();
		chunksAtivos = null;
		atlasUVs.clear();
	}
}
