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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.badlogic.gdx.utils.Pool;
import com.minimine.utils.ruidos.SimplexNoise2D;
import com.minimine.utils.BiomasUtil;
import com.minimine.utils.Mat;
import com.minimine.utils.NuvensUtil;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.Texturas;
import com.minimine.utils.CorposCelestes;

public class Mundo {
	public static String nome = "novo mundo";
	public Texture atlasGeral;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
	public static final List<Object> texturas = new ArrayList<>();
    public static Map<Integer, float[]> atlasUVs = new HashMap<>();
	public static final Map<ChunkUtil.Chave, Chunk> chunks = new ConcurrentHashMap<>();
	public static final Map<ChunkUtil.Chave, Chunk> chunksMod = new ConcurrentHashMap<>();

	public static final int TAM_CHUNK = 16, Y_CHUNK = 255;
	public static int seed = 0, RAIO_CHUNKS = 5;
	public static int chunksTotais = (RAIO_CHUNKS *2 + 1) * (RAIO_CHUNKS *2 + 1);
	public static SimplexNoise2D s2D;
	
	public ShaderProgram shader;
	public static boolean neblina = false, carregado = false, ciclo = true, nuvens = true;

	public static final ExecutorService exec = Executors.newFixedThreadPool(4);
	public static Iterator<Map.Entry<ChunkUtil.Chave, Chunk>> iterator;

	// cada bloco pode ter até 6 faces visíveis, mas na prática ~15% são visíveis
	public static int maxFaces = TAM_CHUNK * Y_CHUNK * TAM_CHUNK * 6 / 6;
	public static int maxVerts = maxFaces * 4;
	public static int maxIndices = maxFaces * 6;
	public static final VertexAttribute[] atriburs = new VertexAttribute[] {
		new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
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
	
	public static String vert = 
	"attribute vec3 a_pos;\n" +
	"attribute vec2 a_texCoord;\n" + 
	"attribute vec4 a_cor;\n" +
	"uniform mat4 u_projPos;\n" +
	"uniform float u_luzGlobal;\n" +
	"uniform float u_tempoDia;\n" +
	"varying vec2 v_texCoord;\n" +
	"varying vec4 v_cor;\n" +
	"void main() {\n" +
	"  v_texCoord = a_texCoord;\n" +
	"  float inten = u_luzGlobal;\n" +
	"  \n" +
	"  if(u_luzGlobal < 0.3) {\n" +
	"    inten *= 0.7;\n" +
	"  }\n" +
	"  vec4 corFinal = a_cor * inten;\n" +
	"  v_cor = corFinal;\n" +
	"  gl_Position = u_projPos * vec4(a_pos, 1.0);\n" +
	"}";

	public static String frag =
	"#ifdef GL_ES\n" +
	"precision mediump float;\n" +
	"#endif\n" +
	"varying vec2 v_texCoord;\n" +
	"varying vec4 v_cor;\n" +
	"uniform sampler2D u_textura;\n" +
	"void main() {\n" +
	"  vec4 texCor = texture2D(u_textura, v_texCoord);\n" +
	"  \n" +
	"  if(texCor.a < 0.1) discard;\n" +
	"  gl_FragColor = texCor * v_cor;\n" +
	"}";

	public static Matrix4 matrizTmp = new Matrix4();
	
	static {
		texturas.add(Texturas.texs.get("grama_topo"));
		texturas.add(Texturas.texs.get("grama_lado"));
		texturas.add(Texturas.texs.get("terra"));
		texturas.add(Texturas.texs.get("pedra"));
		texturas.add(Texturas.texs.get("agua"));
		texturas.add(Texturas.texs.get("areia"));
		texturas.add(Texturas.texs.get("tronco_topo"));
		texturas.add(Texturas.texs.get("tronco_lado"));
		texturas.add(Texturas.texs.get("folha"));
		
		ChunkUtil.blocos.add(new Bloco("grama", 1, 0, 1, 2));
		ChunkUtil.blocos.add(new Bloco("terra", 2, 2));
		ChunkUtil.blocos.add(new Bloco("pedra", 3, 3));
		ChunkUtil.blocos.add(new Bloco("agua", 4, 4, false));
		ChunkUtil.blocos.add(new Bloco("areia", 5, 5));
		ChunkUtil.blocos.add(new Bloco("tronco", 6, 6, 7));
		ChunkUtil.blocos.add(new Bloco("folha", 7, 8, true, false));
	}
	
	public void iniciar() {
		seed = seed == 0 ? Mat.floor((float)Math.random()*1000000) : seed;
		s2D = new SimplexNoise2D(seed);

        criarAtlas();
	
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(vert, frag);
		if(!shader.isCompiled()) Gdx.app.log("shader", "[ERRO]: "+shader.getLog());
		
		if(nuvens) NuvensUtil.iniciar();
		if(ciclo) CorposCelestes.iniciar();
		Gdx.app.log("Debug", "chunks: "+chunksTotais);
	}
		
	public void criarAtlas() {
		Pixmap primeiroPx = null;
		if(texturas.get(0) instanceof String) {
			primeiroPx = new Pixmap(Gdx.files.internal((String) texturas.get(0)));
		} else if(texturas.get(0) instanceof Texture) {
			Texture t = (Texture) texturas.get(0);
			t.getTextureData().prepare();
			primeiroPx = t.getTextureData().consumePixmap();
		}
		int texTam = primeiroPx.getWidth();
		int colunas = (int)Math.ceil(Math.sqrt(texturas.size()));
		int linhas = (int)Math.ceil((float)texturas.size() / colunas);
		int atlasLarg = texTam * colunas;
		int atlasAlt = texTam * linhas;

		Pixmap atlasPx = new Pixmap(atlasLarg, atlasAlt, Pixmap.Format.RGBA8888);

		for(int i = 0; i < texturas.size(); i++) {
			int x = (i % colunas) * texTam;
			int y = (i / colunas) * texTam;

			Pixmap px = null;
			if(texturas.get(i) instanceof String) {
				px = new Pixmap(Gdx.files.internal((String) texturas.get(i)));
			} else if(texturas.get(i) instanceof Texture) {
				Texture t = (Texture) texturas.get(i);
				t.getTextureData().prepare();
				Pixmap tmp = t.getTextureData().consumePixmap();
				px = new Pixmap(tmp.getWidth(), tmp.getHeight(), tmp.getFormat());
				px.drawPixmap(tmp, 0, 0);
				tmp.dispose();
			}

			atlasPx.drawPixmap(px, x, y);

			float u1 = (float)x / atlasLarg;
			float v1 = (float)y / atlasAlt;
			float u2 = (float)(x + texTam) / atlasLarg;
			float v2 = (float)(y + texTam) / atlasAlt;
			atlasUVs.put(i, new float[]{u1, v1, u2, v2});
			px.dispose();
		}
		atlasGeral = new Texture(atlasPx);
		atlasPx.dispose();
		primeiroPx.dispose();
	}
	// chamado em render:
	public void att(float delta, Jogador jogador) {
		if(shader == null) return;
		
		if(chunks.size() >= chunksTotais) {
			System.gc();
			if(!carregado) {
				carregado = true;
			}
		}
		attChunks((int) jogador.posicao.x, (int) jogador.posicao.z);
		
		if(ciclo) DiaNoiteUtil.att();
		
		if(nuvens) NuvensUtil.att(delta, jogador.posicao);
		
		shader.begin();
		shader.setUniformMatrix("u_projPos", jogador.camera.combined);
		
		if(ciclo) DiaNoiteUtil.aplicarShader(shader);

		atlasGeral.bind(0);
		shader.setUniformi("u_textura", 0);

		for(final Chunk chunk : chunks.values()) {
			if(frustrum(chunk, jogador)) {
				if(chunk.mesh != null) chunk.mesh.render(shader, GL20.GL_TRIANGLES);
			}
		}	
		shader.end();
		if(nuvens) NuvensUtil.att(jogador.camera.combined);
		if(ciclo) CorposCelestes.att(jogador.camera.combined);
	}

	public boolean frustrum(Chunk chunk, Jogador jogador) {
		float globalX = chunk.x << 4;
		float globalZ = chunk.z << 4;

		float dis = Vector2.dst(globalX, globalZ, jogador.posicao.x, jogador.posicao.z); 

		if(!(dis < ((RAIO_CHUNKS << 4)))) return false;
		return jogador.camera.frustum.boundsInFrustum(globalX, 0, globalZ, TAM_CHUNK, Y_CHUNK, TAM_CHUNK);
	}
	// chamado em dispose:
	public void liberar() {
        this.atlasGeral.dispose();
		for(Chunk chunk : chunks.values()) {
			if(chunk.mesh != null) {
				chunk.mesh.dispose();
				meshReuso.free(chunk.mesh);
			}
		}
		meshReuso.clear();
		this.shader.dispose();
		this.chunks.clear();
		this.atlasUVs.clear();
	}
	// MANIPULAÇÃO DE BLOCOS:
	public static int obterBlocoMundo(int x, int y, int z) {
		if(y < 0 || y >= Y_CHUNK) return 0; // ar(fora dos limites)

		final int chunkX = Math.floorDiv(x, TAM_CHUNK);
		final int chunkZ = Math.floorDiv(z, TAM_CHUNK);

		ChunkUtil.Chave chave = chaveReuso.obtain();
		if(chave == null) chave = new ChunkUtil.Chave(0, 0);
		chave.x = chunkX; chave.z = chunkZ;
		Chunk chunk = chunks.get(chave);
		chaveReuso.free(chave);
		if(chunk == null) return 0; 
		
		int localX = Math.floorMod(x, TAM_CHUNK);
		int localZ = Math.floorMod(z, TAM_CHUNK);

		return ChunkUtil.obterBloco(localX, y, localZ, chunk);
	}

	public static void defBlocoMundo(int x, int y, int z, int bloco) {
		if(y < 0 || y >= Y_CHUNK) return; // fora dos limites

		final int chunkX = x >> 4;
		final int chunkZ = z >> 4;

		ChunkUtil.Chave chave = chaveReuso.obtain();
		if(chave == null) chave = new ChunkUtil.Chave(0, 0);
		chave.x = chunkX; chave.z = chunkZ;
		Chunk chunk = chunks.get(chave);
		if(chunk == null) return;
		
		int localX = Math.floorMod(x, TAM_CHUNK);
		int localZ = Math.floorMod(z, TAM_CHUNK);

		ChunkUtil.defBloco(localX, y, localZ, bloco, chunk);
		if(bloco == 2) ChunkUtil.defLuz(chunkX, y, chunkZ, (byte) 5, chunk);

		chunk.att = true;
		// chunks perto pra atualização se o bloco for na borda
		if(chunkX == 0) {
			chave.x = chunkX - 1; chave.z = chunkZ;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		if(chunkX == TAM_CHUNK - 1) {
			chave.x = chunkX + 1; chave.z = chunkZ;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		if(chunkZ == 0) {
			chave.x = chunkX; chave.z = chunkZ - 1;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		if(chunkZ == TAM_CHUNK - 1) {
			chave.x = chunkX; chave.z = chunkZ + 1;
			Chunk chunkAdj = chunks.get(chave);
			if(chunkAdj != null) chunkAdj.att = true;
		}
		chunksMod.put(new ChunkUtil.Chave(chunkX, chunkZ), chunk);
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
			chunkExistente.mesh.dispose();
			exec.submit(new Runnable() {
					@Override
					public void run() {
						final FloatArrayUtil vertsGeral = new FloatArrayUtil(); 
						final ShortArrayUtil idcGeral = new ShortArrayUtil();

						ChunkUtil.attMesh(chunkExistente, vertsGeral, idcGeral);

						Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									chunkExistente.mesh = new Mesh(true, maxVerts, maxIndices, atriburs);
									ChunkUtil.defMesh(chunkExistente.mesh, vertsGeral, idcGeral);
									matrizTmp.setToTranslation(chunkExistente.x * TAM_CHUNK, 0, chunkExistente.z * TAM_CHUNK);
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
			Chunk chunkMod = chunks.get(chave);
			if(chunkMod != null) {
				chunks.put(chave, chunkMod);
				gerarChunk(chave);
				return;
			} else {
				Chunk chunk = new Chunk();
				ChunkUtil.compactar(ChunkUtil.bitsPraMaxId(chunk.maxIds), chunk);
				chunks.put(chave, chunk);
				gerarChunk(chave);
			}
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
					chunk.x = chave.x;
					chunk.z = chave.z;

					for(int lx = 0; lx < TAM_CHUNK; lx++) {
						for(int lz = 0; lz < TAM_CHUNK; lz++) {

							float v = (Mundo.s2D.ruidoFractal(
								(chave.x * TAM_CHUNK + lx) * 0.0005f,
								(chave.z * TAM_CHUNK + lz) * 0.0005f,
								1.0f, 4, 0.5f) + 1f) * 0.5f;
/*
							float temp = (Mundo.s2D.ruidoFractal(
								(chave.x * TAM_CHUNK + lx) * 0.001f,
								(chave.z * TAM_CHUNK + lz) * 0.001f,
								1.0f, 3, 0.5f) + 1f) * 0.5f;

							float umid = (Mundo.s2D.ruidoFractal(
								(chave.x * TAM_CHUNK + lx) * 0.002f,
								(chave.z * TAM_CHUNK + lz) * 0.002f,
								1.0f, 3, 0.5f) + 1f) * 0.5f;
*/
							float somaPesos = 0f;
							for(int i = 0; i < BiomasUtil.biomas.size(); i++) {
								somaPesos += 1f - BiomasUtil.biomas.get(i).raridade[0];
							}
							float acumulado = 0f;
							BiomasUtil.Bioma escolhido = null;
							for(int i = 0; i < BiomasUtil.biomas.size(); i++) {
								BiomasUtil.Bioma b = BiomasUtil.biomas.get(i);
								float peso = (1f - b.raridade[0]) / somaPesos;
								acumulado += peso;
								if(v <= acumulado) {
									escolhido = b;
									break;
								}
							}
							if(escolhido == null) {
								escolhido = BiomasUtil.biomas.get(BiomasUtil.biomas.size() - 1);
							}
							if(escolhido != null) {
								escolhido.gerarColuna(lx, lz, chunk);
							}
						}
					}

					final FloatArrayUtil vertsGeral = new FloatArrayUtil();
					final ShortArrayUtil idcGeral = new ShortArrayUtil();
					ChunkUtil.attMesh(chunk, vertsGeral, idcGeral);

					Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								chunk.mesh = meshReuso.obtain();
								ChunkUtil.defMesh(chunk.mesh, vertsGeral, idcGeral);
								matrizTmp.setToTranslation(chunk.x * TAM_CHUNK, 0, chunk.z * TAM_CHUNK);
								chunk.mesh.transform(matrizTmp);
							}
						});
				}
			});
	}
	// API:
	public static Bloco addBloco(String nome, int tipo, int topo) {
		return addBloco(nome, tipo, topo, topo, topo, false, true);
	}

	public static Bloco addBloco(String nome, int tipo, int topo, int lados) {
		return addBloco(nome, tipo, topo, lados, topo, false, true);
	}

	public static Bloco addBloco(String nome, int tipo, int topo, int lados, int baixo) {
		return addBloco(nome, tipo, topo, lados, baixo, false, true);
	}

	public static Bloco addBloco(String nome, int tipo, int topo, int lados, int baixo, boolean alfa, boolean solido) {
		ChunkUtil.blocos.add(new Bloco(nome, tipo, topo, lados, baixo, alfa, solido));
		return ChunkUtil.blocos.get(tipo-1);
	}
}
