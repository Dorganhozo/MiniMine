package com.minimine.cenas;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.Texture;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.minimine.utils.ChunkUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.Iterator;
import java.util.concurrent.Executors;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.Gdx;
import com.minimine.utils.PerlinNoise2D;
import com.minimine.utils.PerlinNoise3D;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.minimine.utils.FloatArrayUtil;
import com.minimine.utils.IntArrayUtil;
import com.badlogic.gdx.utils.Pool;
import com.minimine.utils.EstruturaUtil;

public class Mundo {
	public Texture atlasGeral;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
	public static final List<String> texturas = new ArrayList<>();
    public static final Map<Integer, float[]> atlasUVs = new HashMap<>();
	public static final Map<ChunkUtil.Chave, Chunk> chunks = new ConcurrentHashMap<>();
	public static List<Evento> eventos = new ArrayList<>();
	
	public static final int TAM_CHUNK = 16, Y_CHUNK = 255, RAIO_CHUNKS = 5, seed = 458384435;

	public final ShaderProgram shader;
	public boolean neblina = false;
	public boolean carregado = false;

	public static final ExecutorService exec = Executors.newFixedThreadPool(4);
	public static Iterator<Map.Entry<ChunkUtil.Chave, Chunk>> iterator;

	public static final int maxFaces = TAM_CHUNK * Y_CHUNK * TAM_CHUNK * 6, maxVerts = maxFaces * 4, maxIndices = maxFaces * 6;
	public static final VertexAttribute[] atriburs = new VertexAttribute[] {
		new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_posicao"),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"),
		new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_cor")
	};
	public static final Pool<Mesh> meshReuso = new Pool<Mesh>() {
		@Override
		protected Mesh newObject() {
			return new Mesh(true, maxVerts, maxIndices, atriburs);
		}
	};
	public static final Pool<ChunkUtil.Chave> chaveReuso = new Pool<ChunkUtil.Chave>() {
		@Override
		protected ChunkUtil.Chave newObject() {
			return new ChunkUtil.Chave(0, 0);
		}
	};
	
	public Matrix4 matrizTmp = new Matrix4();
	
	public Mundo() {
		texturas.add("blocos/grama_topo.png");
		texturas.add("blocos/grama_lado.png");
		texturas.add("blocos/terra.png");
		texturas.add("blocos/pedra.png");
		texturas.add("blocos/agua_fixa.png");
		texturas.add("blocos/areia.png");
		texturas.add("blocos/tronco_topo.png");
		texturas.add("blocos/tronco_lado.png");
		texturas.add("blocos/folha.png");
		
        criarAtlas();

		String vert =
			"attribute vec3 a_posicao;\n"+
			"attribute vec2 a_texCoord;\n"+
			"attribute vec4 a_cor;\n"+
			"uniform mat4 u_projTrans;\n"+
			"varying vec2 v_texCoord;\n"+
			"varying vec4 v_cor;\n"+
			"void main() {\n"+
			"  v_texCoord = a_texCoord;\n"+
			"  v_cor = a_cor;\n"+
			"  gl_Position = u_projTrans * vec4(a_posicao, 1.0);\n"+
			"}";

		String frag =
			"#ifdef GL_ES\n"+
			"precision mediump float;\n"+
			"#endif\n"+
			"varying vec2 v_texCoord;\n"+
			"varying vec4 v_cor;\n"+
			"uniform sampler2D u_textura;\n"+
			"void main() {\n"+
			"  vec4 texCor = texture2D(u_textura, v_texCoord);\n"+
			"if(texCor.a < 0.5) discard;\n"+
			"  gl_FragColor = texCor * v_cor;\n"+
			"}";
		
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(vert, frag);
		if(!shader.isCompiled()) {
			Gdx.app.log("shader", "[ERRO]: "+shader.getLog());
		}
		for(int i = 0; i < RAIO_CHUNKS; i++) {
			meshReuso.obtain();
		}
		for(Evento e : eventos) {
			e.aoIniciar();
		}
	}
	
	public void criarAtlas() {
		int texTam = new Pixmap(Gdx.files.internal(texturas.get(0))).getWidth();
		int colunas = (int)Math.ceil(Math.sqrt(texturas.size()));
		int linhas = (int)Math.ceil((float)texturas.size() / colunas);
		int atlasTam = texTam * colunas;

		Pixmap atlasPx = new Pixmap(atlasTam, texTam * linhas, Pixmap.Format.RGBA8888);

		for(int i = 0; i < texturas.size(); i++) {
			int x = (i % colunas) * texTam;
			int y = (i / colunas) * texTam;

			Pixmap px = new Pixmap(Gdx.files.internal(texturas.get(i)));
			atlasPx.drawPixmap(px, x, y);

			float u1 = (float)x / atlasTam;
			float v1 = (float)y / (texTam * linhas);
			float u2 = (float)(x + texTam) / atlasTam;
			float v2 = (float)(y + texTam) / (texTam * linhas);

			atlasUVs.put(i, new float[]{u1, v1, u2, v2});
			px.dispose();
		}
		atlasGeral = new Texture(atlasPx);
		atlasPx.dispose();
	}
	
	// chamado render:
	public void att(float delta, Jogador jogador) {
		for(Evento e : eventos) {
			e.porFrame(delta);
		}
		if(shader == null) return;
		if(!carregado) {
			final Chunk chunk = new Chunk();
			for(int lx = 0; lx < TAM_CHUNK; lx++) {
				for(int lz = 0; lz < TAM_CHUNK; lz++) {
					float px = jogador.posicao.x * TAM_CHUNK + lx;
					float pz = jogador.posicao.z * TAM_CHUNK + lz;

					float alturaRuido = PerlinNoise2D.ruido(px * 0.1f, pz * 0.1f, seed);
					int altura = 45 + (int)(alturaRuido * 5);

					if(lx == (int) jogador.posicao.x && lz == jogador.posicao.z) {
						jogador.posicao.y = altura*2;
						Gdx.app.log("Mundo", "jogador posicionado á "+altura+" blocos de altura");
						Gdx.app.log("Mundo", "o ruido de altura é:  "+alturaRuido+"f");
					}
					for(int y = 0; y < Y_CHUNK; y++) {
						byte bloco = 0; // ar

						if(y < altura) {
							float cavernaRuido = PerlinNoise3D.ruidoFractal3D(px * 0.05f, y * 0.1f, pz * 0.05f, seed, 2, 0.6f);
							if(cavernaRuido > -0.1f) {
								if(y < altura - 3) {
									bloco = 3; // pedra
								} else if(y < altura - 1) {
									bloco = 2; // terra
								} else {
									bloco = 1; // grama
								}
							}
							if(bloco==0) bloco = 4;
						}
						ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
					}
				}
			}
			chunk.chunkX = (int) jogador.posicao.x;
			chunk.chunkZ = (int) jogador.posicao.z;
			// prepara a malha
			final FloatArrayUtil vertsGeral = new FloatArrayUtil(); 
			final IntArrayUtil idcGeral = new IntArrayUtil();

			ChunkUtil.attMesh(chunk, vertsGeral, idcGeral);

			chunk.mesh = meshReuso.obtain();
			ChunkUtil.defMesh(chunk.mesh, vertsGeral, idcGeral);
			matrizTmp.setToTranslation(chunk.chunkX * TAM_CHUNK, 0, chunk.chunkZ * TAM_CHUNK);
			chunk.mesh.transform(matrizTmp);
			carregado = true;
		}
		attChunks((int) jogador.posicao.x, (int) jogador.posicao.z);

		shader.begin();
		shader.setUniformMatrix("u_projTrans", jogador.camera.combined);
		
		atlasGeral.bind(0);
		shader.setUniformi("u_textura", 0);

		for(final Chunk chunk : chunks.values()) {
			if(!frustrum(chunk, jogador)) continue; 
			if(chunk.mesh != null) chunk.mesh.render(shader, GL20.GL_TRIANGLES);
		}
		shader.end();
	}
	
	public boolean frustrum(Chunk chunk, Jogador jogador) {
		float globalX = chunk.chunkX << 4;
		float globalZ = chunk.chunkZ << 4;

		float dis = Vector2.dst(globalX, globalZ, jogador.posicao.x, jogador.posicao.z); 

		if(!(dis < ((RAIO_CHUNKS << 4)))) return false;
		return jogador.camera.frustum.boundsInFrustum(globalX, 0, globalZ, TAM_CHUNK, Y_CHUNK, TAM_CHUNK);
	}
	
	// chamado em dispose:
	public void liberar() {
		for(Evento e : eventos) {
			e.aoFim();
		}
        atlasGeral.dispose();
		for(Chunk chunk : chunks.values()) chunk.mesh.dispose();
		shader.dispose();
		texturas.clear();
		chunks.clear();
		atlasUVs.clear();
		exec.shutdown();
	}
	
	// MANIPULAÇÃO DE BLOCOS:
	public static byte obterBlocoMundo(int x, int y, int z) {
		if(y < 0 || y >= Y_CHUNK) return 0; // ar(fora dos limites)

		int chunkX = Math.floorDiv(x, TAM_CHUNK);
		int chunkZ = Math.floorDiv(z, TAM_CHUNK);

		int localX = Math.floorMod(x, TAM_CHUNK);
		int localZ = Math.floorMod(z, TAM_CHUNK);
		
		ChunkUtil.Chave chave = chaveReuso.obtain();
		if(chave == null) chave = new ChunkUtil.Chave(0, 0);
		chave.x = chunkX; chave.z = chunkZ;
		Chunk chunk = chunks.get(chave);
		chaveReuso.free(chave);
		if(chunk == null) return 0; 

		return ChunkUtil.obterBloco(localX, y, localZ, chunk);
	}
	
	public static void defBlocoMundo(int x, int y, int z, byte bloco) {
		if(y < 0 || y >= Y_CHUNK) return; // fora dos limites

		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		int localX = x & 15;
		int localZ = z & 15;
		
		ChunkUtil.Chave chave = chaveReuso.obtain();
		if(chave == null) chave = new ChunkUtil.Chave(0, 0);
		chave.x = chunkX; chave.z = chunkZ;
		Chunk chunk = chunks.get(chave);
		if(chunk == null) return;

		ChunkUtil.defBloco(localX, y, localZ, bloco, chunk);
		if(bloco == 2) ChunkUtil.defLuz(localX, y, localZ, (byte) 5, chunk);

		chunk.att = true;
		// chunks perto pra atualização se o bloco for na borda
		if(localX == 0) {
			chave.x = chunkX - 1; chave.z = chunkZ;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		if(localX == TAM_CHUNK - 1) {
			chave.x = chunkX + 1; chave.z = chunkZ;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		if(localZ == 0) {
			chave.x = chunkX; chave.z = chunkZ - 1;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		if(localZ == TAM_CHUNK - 1) {
			chave.x = chunkX; chave.z = chunkZ + 1;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		chaveReuso.free(chave);
	}
	
	// GERAÇÃO DE DADOS:
	public void attChunks(int playerX, int playerZ) {
		final int chunkX = playerX / TAM_CHUNK;
		final int chunkZ = playerZ / TAM_CHUNK;
		
		limparChunks(chunkX, chunkZ);

		for(int raios = 0; raios <= RAIO_CHUNKS; raios++) {
			if(raios == 0) {
				tentarGerarChunk(chunkX, chunkZ);
				continue;
			}
			final int raio = raios;

			for(int i = -raio; i <= raio; i++) {
				tentarGerarChunk(chunkX + i, chunkZ - raio); // topo
				tentarGerarChunk(chunkX + i, chunkZ + raio); // baixo
				tentarGerarChunk(chunkX - raio, chunkZ + i); // esquerda  
				tentarGerarChunk(chunkX + raio, chunkZ + i); // direita
			}
		}
	}
	
	public void tentarGerarChunk(int x, int z) {
		final ChunkUtil.Chave chave = new ChunkUtil.Chave(x, z);
		final Chunk chunkExistente = chunks.get(chave);
		
		if(chunkExistente != null && chunkExistente.att) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					final FloatArrayUtil vertsGeral = new FloatArrayUtil(); 
					final IntArrayUtil idcGeral = new IntArrayUtil();

					ChunkUtil.attMesh(chunkExistente, vertsGeral, idcGeral);

					Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								ChunkUtil.defMesh(chunkExistente.mesh, vertsGeral, idcGeral);
								matrizTmp.setToTranslation(chunkExistente.chunkX * TAM_CHUNK, 0, chunkExistente.chunkZ * TAM_CHUNK);
								chunkExistente.mesh.transform(matrizTmp);
								chunkExistente.att = false;
							}
						});
				}
			});
		}
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
					meshReuso.free(chunk.mesh);
				}
				iterator.remove();
				chunks.remove(chave);
			}
		}
	}

	public void gerarChunk(final ChunkUtil.Chave chave) {
		exec.submit(new Runnable() {
				@Override
				public void run() {
					final Chunk chunk = chunks.get(chave);

					for(int lx = 0; lx < TAM_CHUNK; lx++) {
						for(int lz = 0; lz < TAM_CHUNK; lz++) {
							float px = chave.x * TAM_CHUNK + lx;
							float pz = chave.z * TAM_CHUNK + lz;
							byte bloco = 0;
							// ruido continental: define onde ha oceanos e terra
							float continente = PerlinNoise2D.ruidoFractal2D(px * 0.0003f, pz * 0.0003f, 1.0f, seed, 5, 0.5f);
							// ruido de detalhe local
							float detalhe = PerlinNoise2D.ruidoFractal2D(px * 0.015f, pz * 0.015f, 2.0f, seed + 99, 3, 0.5f);
							// ruido de escala intermediaria para morros
							float relevo = PerlinNoise2D.ruidoFractal2D(px * 0.003f, pz * 0.003f, 1.0f, seed + 300, 4, 0.55f);
							// continente controla a intensidade do relevo: regiões "oceanicas" ficam planas
							float base = continente;
							if(base < -0.15f) base = -0.15f; // evita profundidades exageradas
							float intensi = (base + 0.3f) * 1.6f;
							if(intensi < 0f) intensi = 0f;
							// elevação final misturando tudo
							float alturaNormalizada = (relevo * intensi) + (detalhe * 0.2f);
							// curva exponencial para picos altos e vales suaves
							if(alturaNormalizada > 0) alturaNormalizada = (float)Math.pow(alturaNormalizada, 1.8f);
							else alturaNormalizada = -((float)Math.pow(-alturaNormalizada, 0.8f));

							int altura = (int)(50 + alturaNormalizada * 180f);
							if(altura < 5) altura = 5;
							if(altura > Y_CHUNK - 2) altura = Y_CHUNK - 2;
							
							for(int y = 0; y < altura; y++) {
								if(y < altura - 8) bloco = 3;       // pedra
								else if(y < altura - 1) bloco = 2;  // terra
								else if(y == altura - 1) bloco = 1; // grama
								else bloco = 0;
								ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
							}
							if(Math.random() < 0.05) EstruturaUtil.gerarArvore(lx, altura, lz, chunk);
						}
					}
					chunk.chunkX = chave.x;
					chunk.chunkZ = chave.z;
					
					for(Evento e : eventos) {
						e.aoGerarChunk(chave.x, chave.z, "padrao");
					}
					final FloatArrayUtil vertsGeral = new FloatArrayUtil();
					final IntArrayUtil idcGeral = new IntArrayUtil();
					ChunkUtil.attMesh(chunk, vertsGeral, idcGeral);

					Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								chunk.mesh = meshReuso.obtain();
								ChunkUtil.defMesh(chunk.mesh, vertsGeral, idcGeral);
								matrizTmp.setToTranslation(chunk.chunkX * TAM_CHUNK, 0, chunk.chunkZ * TAM_CHUNK);
								chunk.mesh.transform(matrizTmp);
							}
						});
				}
			});
	}
	
	public static interface Evento {
		public void aoCarregar();
		public void aoGerarChunk(int x, int z, String bioma);
		public void aoSair();
		public void aoGerarEntidade(String id);
		
		public void aoIniciar();
		public void porFrame(float delta);
		public void aoFim();
	}
}
