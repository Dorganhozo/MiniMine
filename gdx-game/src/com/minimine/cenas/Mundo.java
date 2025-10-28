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
import com.minimine.PerlinNoise3D;
import com.minimine.PerlinNoise2D;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.minimine.utils.FloatArrayUtil;
import com.minimine.utils.IntArrayUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import com.badlogic.gdx.math.Vector2;

public class Mundo {
	public Texture atlasGeral;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
	public final List<String> texturas = new ArrayList<>();
    public static final Map<Integer, float[]> atlasUVs = new HashMap<>();
	public final Map<ChunkUtil.Chave, Chunk> chunks = new ConcurrentHashMap<>();
	
	public static final int TAM_CHUNK = 16, Y_CHUNK = 255, RAIO_CHUNKS = 10;

	public ShaderProgram shader;

	public final ExecutorService exec = Executors.newFixedThreadPool(4);
	public Iterator<Map.Entry<ChunkUtil.Chave, Chunk>> iterator;

	public int maxVerts, maxIndices, maxFaces;
	public VertexAttribute[] atriburs = new VertexAttribute[] {
		new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_posicao"),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
	};

	public Mundo() {
		texturas.add("blocos/grama_topo.png");
		texturas.add("blocos/grama_lado.png");
		texturas.add("blocos/terra.png");
		texturas.add("blocos/pedra.png");

        criarAtlas();

		maxFaces = TAM_CHUNK * Y_CHUNK * TAM_CHUNK * 6;
		maxVerts = maxFaces * 4;
		maxIndices = maxFaces * 6;
		
		String vert =
			"attribute vec3 a_posicao;\n"+
			"attribute vec2 a_texCoord0;\n"+
			"uniform mat4 u_projTrans;\n"+
			"varying vec2 v_texCoord;\n"+
			"void main() {\n"+
			"  v_texCoord = a_texCoord0;\n"+
			"  gl_Position = u_projTrans * vec4(a_posicao, 1.0);\n"+
			"}";

		String frag =
			"#ifdef GL_ES\n"+
			"precision mediump float;\n"+
			"#endif\n"+
			"varying vec2 v_texCoord;\n"+
			"uniform sampler2D u_textura;\n"+
			"void main() {\n"+
			"  gl_FragColor = texture2D(u_textura, v_texCoord);\n"+
			"}";
		
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(vert, frag);
	}
	// chamado render:
	public void att(float delta, PerspectiveCamera camera) {
		if(shader == null) return;

		attChunks((int) camera.position.x, (int) camera.position.z);

		shader.begin();
		shader.setUniformMatrix("u_projTrans", camera.combined);

		atlasGeral.bind(0);
		shader.setUniformi("u_textura", 0);

		for(final Chunk chunk : chunks.values()) {    
			if(!frustrum(chunk, camera)) continue;
			if(chunk.att) chunk.att = false;
			if(chunk.mesh != null) chunk.mesh.render(shader, GL20.GL_TRIANGLES);
		}
		shader.end();
	}
	
	public boolean frustrum(Chunk chunk, PerspectiveCamera camera) {
		float globalX = chunk.chunkX << 4;
		float globalZ = chunk.chunkZ << 4;
		float dis = Vector2.dst(globalX, globalZ, camera.position.x, camera.position.z);
		return dis < ((RAIO_CHUNKS << 4) * 1.5f);
	}
	
	// chamado em dispose:
	public void liberar() {
        atlasGeral.dispose();
		for(Chunk chunk : chunks.values()) chunk.mesh.dispose();
		shader.dispose();
		texturas.clear();
		chunks.clear();
		atlasUVs.clear();
		exec.shutdown();
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
	}
	// GERAÇÃO DE DADOS:
	public void attChunks(float playerX, float playerZ) {
		final int chunkX = (int) playerX / TAM_CHUNK;
		final int chunkZ = (int) playerZ / TAM_CHUNK;

		limparChunks(chunkX, chunkZ);

		for(int raios = 0; raios <= RAIO_CHUNKS; raios++) {
			if(raios == 0) {
				tentarGerarChunk(chunkX, chunkZ);
				continue;
			}
			final int raio = raios;
			exec.submit(new Runnable() {
					@Override
					public void run() {
						for(int i = -raio; i <= raio; i++) {
							tentarGerarChunk(chunkX + i, chunkZ - raio); // Topo
							tentarGerarChunk(chunkX + i, chunkZ + raio); // Baixo
							tentarGerarChunk(chunkX - raio, chunkZ + i); // Esquerda  
							tentarGerarChunk(chunkX + raio, chunkZ + i); // Direita
						}
					}
				});
		}
	}
	
	public void tentarGerarChunk(int x, int z) {
		final ChunkUtil.Chave chave = new ChunkUtil.Chave(x, z);
		Chunk chunkExistente = chunks.get(chave);

		if(chunkExistente != null && chunkExistente.mesh != null) {
			return;
		}

		if(chunkExistente == null) {
			chunks.put(chave, new Chunk());
			gerarChunk(chave);
		}
	}

	public void limparChunks(int chunkXPlayer, int chunkZPlayer) {
		iterator = chunks.entrySet().iterator();

		while(iterator.hasNext()) {
			Map.Entry<ChunkUtil.Chave, Chunk> c = iterator.next();
			ChunkUtil.Chave chave = c.getKey();
			Chunk chunk = c.getValue();

			int distX = Math.abs(chave.x - chunkXPlayer);
			int distZ = Math.abs(chave.z - chunkZPlayer);

			if(distX > RAIO_CHUNKS || distZ > RAIO_CHUNKS) {
				if(chunk.mesh != null) {
					chunk.mesh.dispose();
				}
				iterator.remove();
				chunks.remove(chave);
			}
		}
	}

	public void gerarChunk(final ChunkUtil.Chave chave) {
		final Chunk chunk = chunks.get(chave); // pega o vazio
		// gera os blocos
		for(int lx = 0; lx < TAM_CHUNK; lx++) {
			for(int lz = 0; lz < TAM_CHUNK; lz++) {
				float px = chave.x * TAM_CHUNK + lx;
				float pz = chave.z * TAM_CHUNK + lz;

				float alturaRuido = PerlinNoise2D.ruidoFractal2D(px * 0.01f, pz * 0.01f, 1.0f, 12345, 4, 0.5f);
				int altura = 45 + (int)(alturaRuido * 5);

				for(int y = 0; y < Y_CHUNK; y++) {
					byte bloco = 0; // ar

					if(y < altura) {
						float cavernaRuido = PerlinNoise3D.ruidoFractal3D(
							px * 0.05f, y * 0.1f, pz * 0.05f, 67890, 3, 0.6f
						);
						if(cavernaRuido > -0.1f) {
							if(y < altura - 3) {
								bloco = 3; // pedra
							} else if(y < altura - 1) {
								bloco = 2; // terra
							} else {
								bloco = 1; // grama
							}
						}
					}
					chunk.chunk[lx][y][lz] = bloco;
				}
			}
		}
		chunk.chunkX = chave.x;
		chunk.chunkZ = chave.z;
		// prepara a malha
		final FloatArrayUtil vertsGeral = new FloatArrayUtil(); 
		final IntArrayUtil idcGeral = new IntArrayUtil();
		ChunkUtil.attMesh(chunk, vertsGeral, idcGeral);
		
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				chunk.mesh = new Mesh(true, maxVerts, maxIndices, atriburs);
				ChunkUtil.defMesh(chunk.mesh, vertsGeral, idcGeral);
				Matrix4 m = new Matrix4();
				m.setToTranslation(chunk.chunkX * TAM_CHUNK, 0, chunk.chunkZ * TAM_CHUNK);
				chunk.mesh.transform(m);
			}
		});
	}
}
/*
package com.minimine.cenas;

import com.badlogic.gdx.graphics.Texture;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import java.util.Objects;
import com.minimine.PerlinNoise2D;

public class Mundo {
	public static Texture atlasGeral;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
	public final List<String> texturas = new ArrayList<>();
    public static final Map<Integer, float[]> atlasUVs = new HashMap<>();
	public final Map<Chave, Chunk> chunks = new HashMap<>();
	public final Map<Chave, Chunk> chunksAtivos = new HashMap<>();

	public static final int TAM_CHUNK = 16, Y_CHUNK = 255, RAIO_CHUNKS = 3;
	// zero é ar
	public static final byte B_GRAMA = 1, B_TERRA = 2, B_PEDRA = 3;
	
	public static final ExecutorService exec = Executors.newFixedThreadPool(4);
	
	public Mundo() {
		texturas.add("blocos/grama_topo.png");
		texturas.add("blocos/grama_lado.png");
		texturas.add("blocos/terra.png");
		texturas.add("blocos/pedra.png");
		
        criarAtlas();
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
	}
	// GERAÇÃO DE DADOS:
	public void gerarBlocos(Chunk chunk) {
		float escala = 0.01f; 
		int octaves = 4;

		for(int x = 0; x < TAM_CHUNK; x++) {
			for(int z = 0; z < TAM_CHUNK; z++) {
				float mundoX = (chunk.chunkX * TAM_CHUNK) + x;
				float mundoZ = (chunk.chunkZ * TAM_CHUNK) + z;

				float noise = PerlinNoise2D.ruidoFractal2D(mundoX, mundoZ, escala, 12345, octaves, 0.5f);

				int altura = (int)((noise + 1) * 15 + 40);

				for(int y = 0; y <= altura && y < Y_CHUNK; y++) {
					if(y == altura) {
						chunk.blocos[x][y][z] = B_GRAMA;
					} else if(y > altura - 4) {
						chunk.blocos[x][y][z] = B_TERRA;
					} else {
						chunk.blocos[x][y][z] = B_PEDRA;
					}
				}
			}
		}
	}
	// chamado render:
	public void att(float delta, PerspectiveCamera camera) {
		
	}
	// chamado em dispose:
	public void liberar() {
        atlasGeral.dispose();
		texturas.clear();
		chunks.clear();
		chunksAtivos.clear();
		atlasUVs.clear();
		exec.shutdown();
	}
	
	public static class Chunk {
		public byte[][][] blocos = new byte[TAM_CHUNK][Y_CHUNK][TAM_CHUNK];
		public int chunkX, chunkZ;
	}
	
	public static class Chave {
		public int x, z;
		public Chave(int x, int z) {
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o.getClass() != getClass()) return false;
			Chave chave = (Chave) o;
			return  x == chave.x && z == chave.z;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, z);
		}
	}
} */
