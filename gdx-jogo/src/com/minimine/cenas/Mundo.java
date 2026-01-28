package com.minimine.cenas;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.minimine.utils.chunks.ChunkUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.Iterator;
import java.util.concurrent.Executors;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Pool;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.utils.BiomasUtil;
import com.minimine.utils.Mat;
import com.minimine.utils.NuvensUtil;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.Texturas;
import com.minimine.utils.CorposCelestes;
import com.minimine.utils.chunks.Chave;
import com.minimine.utils.chunks.ChunkMalha;
import com.minimine.utils.blocos.Bloco;
import com.minimine.utils.chunks.Chunk;
import com.minimine.utils.ruidos.SimplexNoise3D;
import com.minimine.utils.ruidos.Simplex2D;
import com.minimine.utils.graficos.Animacoes2D;

public class Mundo {
    public static String nome = "novo mundo";
    public static Texture atlasGeral = null;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
    public static final List<Object> texturas = new ArrayList<>();
	public static final List<Chunk> praLiberar = new ArrayList<>();
	public static final List<Chave> praRemover = new ArrayList<>();
    public static final Map<Integer, float[]> atlasUVs = new HashMap<>();
    public static Map<Chave, Chunk> chunks = new ConcurrentHashMap<>();
    public static Map<Chave, Chunk> chunksMod = new ConcurrentHashMap<>();
	// Estados: 0 = Vazia, 1 = Dados Prontos, 2 = Malha Pronta
	public static final Map<Chave, Integer> estados = new ConcurrentHashMap<>();

    public static final int TAM_CHUNK = 16, Y_CHUNK = 255;
    public static final int CHUNK_AREA = TAM_CHUNK * TAM_CHUNK;
    public static int semente = 0, RAIO_CHUNKS = 5;

    public static Simplex2D s2D;
	public static SimplexNoise3D s3D;

    public static ShaderProgram shader;
    public static boolean carregado = false, ciclo = true, nuvens = true, mod = false;
    public static float tick = 0.2f;

    public static ExecutorService exec;
    public static Iterator<Map.Entry<Chave, Chunk>> iterator;

    public static int maxFaces = TAM_CHUNK * Y_CHUNK * TAM_CHUNK * 6 / 6;
    public static int maxVerts = maxFaces * 4;
    public static int maxIndices = maxFaces * 6;
    public static final VertexAttribute[] atriburs = new VertexAttribute[] {
        new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"),
        new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_cor")
    };

    public static String vert = 
    "attribute vec3 a_pos;\n" +
    "attribute vec2 a_texCoord;\n" +
    "attribute vec4 a_cor;\n" +
    "uniform mat4 u_projPos;\n" +
    "varying vec2 v_texCoord;\n" +
    "varying vec4 v_cor;\n" +
    "void main() {\n" +
	"v_texCoord = a_texCoord;\n" +
	"v_cor = a_cor;\n" +
	"gl_Position = u_projPos * vec4(a_pos, 1.0);\n" +
    "}";

	public static String frag =
	"#ifdef GL_ES\n" +
	"precision mediump float;\n" +
	"#endif\n" +
	"varying vec2 v_texCoord;\n" +
	"varying vec4 v_cor;\n" +
	"uniform sampler2D u_textura;\n" +
	"uniform float u_luzCeu;\n" +
	"void main() {\n" +
	"vec4 texCor = texture2D(u_textura, v_texCoord);\n" +
	"if(texCor.a < 0.5) discard;\n" +
	// a luz final é o maximo entre a luz do bloco e a luz do céu
	"float luzFinal = max(v_cor.r, u_luzCeu);\n" + 
	"gl_FragColor = texCor * vec4(luzFinal, luzFinal, luzFinal, 1.0);\n" +
	"}";

    public static Matrix4 matrizTmp = new Matrix4();
	public static Chave chaveTmp = new Chave(0, 0);

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
        texturas.add(Texturas.texs.get("tabua_madeira"));
        texturas.add(Texturas.texs.get("cacto_topo"));
        texturas.add(Texturas.texs.get("cacto_lado"));
        texturas.add(Texturas.texs.get("vidro"));
        texturas.add(Texturas.texs.get("tocha"));
		
        Bloco.blocos.add(null);
        Bloco.blocos.add(new Bloco("grama", 0, 1, 2));
        Bloco.blocos.add(new Bloco("terra", 2));
        Bloco.blocos.add(new Bloco("pedra", 3));
        Bloco.blocos.add(new Bloco("agua", 4, true, false, false));
        Bloco.blocos.add(new Bloco("areia", 5));
        Bloco.blocos.add(new Bloco("tronco", 6, 7));
        Bloco.blocos.add(new Bloco("folha", 8, true, true, false));
        Bloco.blocos.add(new Bloco("tabua_madeira", 9));
        Bloco.blocos.add(new Bloco("cacto", 10, 11));
        Bloco.blocos.add(new Bloco("vidro", 12, true, true, false));
        Bloco.blocos.add(new Bloco("tocha", 13, 13, 13, false, true, true, 15));

		Bloco.addSom("grama", "grama_1", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("terra", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("pedra", "pedra_1", "pedra_2");
		Bloco.addSom("folha", "terra_1", "terra_2", "terra_3");
		Bloco.addSom("tabua_madeira", "madeira_1", "madeira_2", "madeira_3");
		Bloco.addSom("tocha", "madeira_1", "madeira_2", "madeira_3");
    }

    public void iniciar() {
        semente = semente == 0 ? Mat.floor((float)Math.random()*1000000) : semente;
        s2D = new Simplex2D(semente);
		s3D = new SimplexNoise3D(semente >> 1);

        criarAtlas();

		Texture[] framesAgua = {
			Texturas.texs.get("agua"),
			Texturas.texs.get("agua_a1"),
			Texturas.texs.get("agua_a2")
		};
		Animacoes2D.add(4, framesAgua, 3f); // 8 fps

		Animacoes2D.config();

        ShaderProgram.pedantic = false;
        if(!mod) shader = new ShaderProgram(vert, frag);
        if(!shader.isCompiled()) Gdx.app.log("shader", "[ERRO]: "+shader.getLog());

        if(nuvens && NuvensUtil.primeiraVez) NuvensUtil.iniciar();
        if(ciclo) CorposCelestes.iniciar();

        exec = Executors.newFixedThreadPool(8);
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

		attChunks((int) jogador.posicao.x, (int) jogador.posicao.z);

        if(nuvens) NuvensUtil.att(delta, jogador.posicao);

        shader.begin();

		Animacoes2D.att(delta);

        shader.setUniformMatrix("u_projPos", jogador.camera.combined);

        shader.setUniformf("u_luzCeu", DiaNoiteUtil.luz); 

		atlasGeral.bind(0);
        shader.setUniformi("u_textura", 0);
		
		// 1. solidos:
        for(final Chunk chunk : chunks.values()) {
			if(frustrum(chunk, jogador) && chunk.malha != null && chunk.contaSolida > 0) {
				// renderiza apenas do indice 0 até o final dos solidos
				chunk.malha.render(shader, GL20.GL_TRIANGLES, 0, chunk.contaSolida);
			}
		}
		// 2: transparentes:
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		for(final Chunk chunk : chunks.values()) {
			if(frustrum(chunk, jogador) && chunk.malha != null && chunk.contaTransp > 0) {
				// renderiza começando de onde o solido parou
				chunk.malha.render(shader, GL20.GL_TRIANGLES, chunk.contaSolida, chunk.contaTransp);
			}
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);

		shader.end();
        if(nuvens) NuvensUtil.att(jogador.camera.combined);
        if(ciclo) {
			CorposCelestes.att(jogador.camera.combined, jogador.posicao);
			// ciclo de dia e noite:
			if(tick > 0.03f) {

				tick = 0;
			}
		}
    }

    public boolean frustrum(Chunk chunk, Jogador jogador) {
        float globalX = chunk.x << 4;
        float globalZ = chunk.z << 4;

        float dis = Vector2.dst(globalX, globalZ, jogador.posicao.x, jogador.posicao.z);

        if(!(dis < ((RAIO_CHUNKS << 4)))) return false;

        return jogador.camera.frustum.boundsInFrustum(globalX, 0, globalZ, TAM_CHUNK, Y_CHUNK, TAM_CHUNK);
    }
    // chamado em dispose:
    public static void liberar() {
		atlasGeral.dispose();
        for(Chunk chunk : chunks.values()) {
            if(chunk.malha != null) {
                chunk.malha.dispose();
				chunk.malha = null;
            }
        }
		chunks.clear();
        shader.dispose();
        chunks.clear();
        atlasUVs.clear();
		estados.clear();
        exec.shutdown();
		// s2D.liberar();
		s3D.liberar();
		Animacoes2D.liberar();
    }

    public static int obterBlocoMundo(int x, int y, int z) {
        if(y < 0 || y >= Y_CHUNK) return 0; // ar(fora dos limites)

		chaveTmp.x = x >> 4;
		chaveTmp.z = z >> 4;
        Chunk chunk = chunks.get(chaveTmp);

        if(chunk == null) return 0;

        int localX = x & 0xF;
        int localZ = z & 0xF;

        return ChunkUtil.obterBloco(localX, y, localZ, chunk);
    }

    public static void defBlocoMundo(int x, int y, int z, CharSequence bloco) {
        if(y < 0 || y >= Y_CHUNK) return; // fora dos limites

        final int chunkX = x >> 4;
        final int chunkZ = z >> 4;

		chaveTmp.x = chunkX; chaveTmp.z = chunkZ;

        Chunk chunk = chunks.get(chaveTmp);
        if(chunk == null) {
			Gdx.app.log("Mundo", "chunk null na posição X: "+chunkX+", Z: "+chunkZ);
			return;
		}
        int localX = x & 0xF;
        int localZ = z & 0xF;

        ChunkUtil.defBloco(localX, y, localZ, bloco, chunk);
		chunk.luzSuja = true;
        chunk.att = true;
        // chunks perto pra atualizar se o bloco for na borda
        if(localX == 0) {
            chaveTmp.x = chunkX - 1; chaveTmp.z = chunkZ;
            Chunk chunkAdj = chunks.get(chaveTmp);
            if(chunkAdj != null) chunkAdj.att = true;
        }
        if(localX == TAM_CHUNK - 1) {
            chaveTmp.x = chunkX + 1; chaveTmp.z = chunkZ;
            Chunk chunkAdj = chunks.get(chaveTmp);
            if(chunkAdj != null) chunkAdj.att = true;
        }
        if(localZ == 0) {
            chaveTmp.x = chunkX; chaveTmp.z = chunkZ - 1;
            Chunk chunkAdj = chunks.get(chaveTmp);
            if(chunkAdj != null) chunkAdj.att = true;
        }
        if(localZ == TAM_CHUNK - 1) {
            chaveTmp.x = chunkX; chaveTmp.z = chunkZ + 1;
            Chunk chunkAdj = chunks.get(chaveTmp);
            if(chunkAdj != null) chunkAdj.att = true;
        }
        chunksMod.put(new Chave(chunkX, chunkZ), chunk);
    }
    // GERAÇÃO DE DADOS:
    public boolean deveAttChunk(int chunkX, int chunkZ, int x, int z) {
        int distX = Mat.abs(chunkX - x);
        int distZ = Mat.abs(chunkZ - z);
        // prioriza chunks mais proximos
        if(distX <= 1 && distZ <= 1) return true; // alta prioridade
        if(distX <= RAIO_CHUNKS/2 && distZ <= RAIO_CHUNKS/2) return true; // media prioridade
        return distX <= RAIO_CHUNKS && distZ <= RAIO_CHUNKS; // baixa prioridade
    }

    public void attChunks(int x, int z) {
		final int cx = x >> 4;
		final int cz = z >> 4;
		limparChunks(cx, cz);

		int dx = 0;
		int dz = 0;
		int passo = 1;

		tentarGerarChunk(cx, cz);

		while(passo <= RAIO_CHUNKS << 1) {
			int i;
			for(i = 0; i < passo; i++) {
				dx++;
				int px = cx + dx;
				int pz = cz + dz;
				if(deveAttChunk(px, pz, cx, cz)) tentarGerarChunk(px, pz);
			}
			for(i = 0; i < passo; i++) {
				dz++;
				int px = cx + dx;
				int pz = cz + dz;
				if(deveAttChunk(px, pz, cx, cz)) tentarGerarChunk(px, pz);
			}
			passo++;

			for(i = 0; i < passo; i++) {
				dx--;
				int px = cx + dx;
				int pz = cz + dz;
				if(deveAttChunk(px, pz, cx, cz)) tentarGerarChunk(px, pz);
			}
			for(i = 0; i < passo; i++) {
				dz--;
				int px = cx + dx;
				int pz = cz + dz;
				if(deveAttChunk(px, pz, cx, cz)) tentarGerarChunk(px, pz);
			}
			passo++;
		}
	}

    public void limparChunks(int chunkX, int chunkZ) {
		praLiberar.clear();
		praRemover.clear();

		for(Map.Entry<Chave, Chunk> e : chunks.entrySet()) {
			Chave chave = e.getKey();
			int distX = Mat.abs(chave.x - chunkX);
			int distZ = Mat.abs(chave.z - chunkZ);
			Chunk chunk = e.getValue();
			
			if(distX > RAIO_CHUNKS || distZ > RAIO_CHUNKS) {
				if(!chunksMod.containsKey(chave)) praLiberar.add(chunk);
				if(chunk != null) {
					if(chunk.malha != null) praLiberar.add(chunk);
				}
				praRemover.add(chave);
			} else if(chunk.att && !chunk.fazendo) {
				if(vizinhosProntos(chunk.x, chunk.z)) {
					gerarMalha(new Chave(chunk.x, chunk.z));
				}
			}
		}
		if(!praLiberar.isEmpty() || !praRemover.isEmpty()) {
			Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						for(Chunk c : praLiberar) {
							if(c.malha != null) {
								c.malha.dispose();
								c.malha = null;
							}
						}
						for(Chave k : praRemover) chunks.remove(k);
					}
				});
		}
	}
	
	public void tentarGerarChunk(int x, int z) {
		final Chave chave = new Chave(x, z);
		// 1. tenta pegar do mapa de modificadas ou do mapa geral
		Chunk chunk = chunksMod.get(chave);
		if(chunk == null) chunk = chunks.get(chave);

		// 2. se não existe em lugar nenhum, cria do zero(geração procedural)
		if(chunk == null) {
			chunk = new Chunk();
			chunk.x = x; chunk.z = z;
			ChunkUtil.compactar(ChunkUtil.bitsPraMaxId(chunk.maxIds), chunk);
			chunks.put(chave, chunk);
			estados.put(chave, 0);
			gerarDados(chave);
			return; 
		}
		// 3. se ela existe(veio do disco ou ja foi gerada) e ta no estado 1, 
		// gera a malha assim que os vizinhos permitirem
		int estadoAtual = estados.getOrDefault(chave, 0);
		if(estadoAtual == 1 && !chunk.fazendo) {
			if(vizinhosProntos(x, z)) {
				gerarMalha(chave);
			}
		}
	}
	// verifica se as 8 vizinhas ao redor ja tem dados de blocos
	public boolean vizinhosProntos(int cx, int cz) {
		for(int x = cx - 1; x <= cx + 1; x++) {
			for(int z = cz - 1; z <= cz + 1; z++) {
				if(x == cx && z == cz) continue;
				Chave vizinha = new Chave(x, z);
				// se a vizinha não tem estado ou ainda ta no estado 0(sem dados)
				if(estados.getOrDefault(vizinha, 0) < 1) return false;
			}
		}
		return true;
	}
	
	public static void gerarDados(final Chave chave) {
		final Chunk chunk = chunks.get(chave);
		exec.submit(new Runnable() {
				@Override
				public void run() {
					for(int lx = 0; lx < TAM_CHUNK; lx++) {
						for(int lz = 0; lz < TAM_CHUNK; lz++) {
							BiomasUtil.escolher(lx, lz, chunk);
						}
					}
					estados.put(chave, 1); // agora ta pronta pra que as vizinhas gerem malha
				}
			});
	}

	public static void gerarMalha(final Chave chave) {
		final Chunk chunk = chunks.get(chave);
		chunk.fazendo = true;

		exec.submit(new Runnable() {
				@Override
				public void run() {
					final FloatArrayUtil vertsGeral = new FloatArrayUtil();
					final ShortArrayUtil idcSolidos = new ShortArrayUtil();
					final ShortArrayUtil idcTransp = new ShortArrayUtil();

					ChunkMalha.attMalha(chunk, vertsGeral, idcSolidos, idcTransp);

					// junta os indices: primeiro solidos depois transparentes
					final short[] idcFinal = new short[idcSolidos.tam + idcTransp.tam];
					System.arraycopy(idcSolidos.praArray(), 0, idcFinal, 0, idcSolidos.tam);
					System.arraycopy(idcTransp.praArray(), 0, idcFinal, idcSolidos.tam, idcTransp.tam);

					chunk.contaSolida = idcSolidos.tam;
					chunk.contaTransp = idcTransp.tam;

					Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								if(chunk.malha == null) {
									chunk.malha = new Mesh(true, maxVerts, maxIndices, atriburs);
								}
								chunk.malha.setVertices(vertsGeral.praArray());
								chunk.malha.setIndices(idcFinal);

								matrizTmp.setToTranslation(chunk.x << 4, 0, chunk.z << 4);
								chunk.malha.transform(matrizTmp);

								chunk.fazendo = false;
								chunk.att = false;
								estados.put(chave, 2);
							}
						});
				}
			});
	}
	
	public static void gerarChunk(final Chave chave) {
		final Chunk chunk = chunks.get(chave);
		chunk.x = chave.x;
		chunk.z = chave.z;
		chunk.fazendo = true;

		exec.submit(new Runnable() {
				@Override
				public void run() {
					// 1: gera apenas os dados dos blocos(se ainda não existirem)
					// garante que a chunk central e suas 4 vizinhas diretas tenham dados
					prepararDadosVizinhos(chave.x, chave.z);

					// 2: gera a malha
					final FloatArrayUtil vertsGeral = new FloatArrayUtil();
					final ShortArrayUtil idcSolido = new ShortArrayUtil();
					final ShortArrayUtil idcTransp = new ShortArrayUtil();

					// agora a attmalha pode checar os vizinhos com segurança
					ChunkMalha.attMalha(chunk, vertsGeral, idcSolido, idcTransp);

					Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								if(chunk.malha != null) {
									chunk.malha.dispose();
								}
								chunk.malha = new Mesh(true, maxVerts, maxIndices, atriburs);
								chunk.malha.setVertices(vertsGeral.praArray());
								chunk.malha.setIndices(idcSolido.praArray());

								matrizTmp.setToTranslation(chunk.x << 4, 0, chunk.z << 4);
								chunk.malha.transform(matrizTmp);

								chunk.fazendo = false;
								chunk.att = false;
							}
						});
				}
			});
	}
	
	public static void prepararDadosVizinhos(int cx, int cz) {
		// define um raio de vizinhos necessarios(pelo menos as 4 direções)
		for(int x = cx - 1; x <= cx + 1; x++) {
			for(int z = cz - 1; z <= cz + 1; z++) {
				Chave vizinhaChave = new Chave(x, z);
				Chunk v = chunks.get(vizinhaChave);

				// se a chunk vizinha não existe, cria ela apenas com dados(sem malha ainda)
				if(v == null) {
					v = new Chunk();
					v.x = x; v.z = z;
					ChunkUtil.compactar(ChunkUtil.bitsPraMaxId(v.maxIds), v);
					chunks.put(vizinhaChave, v);
				}
				// se os blocos ainda não foram gerados(biomas não escolhidos)
				if(!v.dadosProntos) {
					for(int lx = 0; lx < TAM_CHUNK; lx++) {
						for(int lz = 0; lz < TAM_CHUNK; lz++) {
							BiomasUtil.escolher(lx, lz, v);
						}
					}
					v.dadosProntos = true;
				}
			}
		}
	}
    // API:
    public static Bloco addBloco(String nome, int topo) {
        return addBloco(nome, topo, topo, topo, false, true);
    }

    public static Bloco addBloco(String nome, int topo, int lados) {
        return addBloco(nome, topo, lados, topo, false, true);
    }

    public static Bloco addBloco(String nome, int topo, int lados, int baixo) {
        return addBloco(nome, topo, lados, baixo, false, true);
    }

    public static Bloco addBloco(String nome, int topo, int lados, int baixo, boolean alfa, boolean solido) {
        return addBloco(nome, topo, lados, baixo, alfa, solido, 0);
    }

	public static Bloco addBloco(String nome, int topo, int lados, int baixo, boolean alfa, boolean solido, int luz) {
        Bloco.blocos.add(new Bloco(nome, topo, lados, baixo, alfa, solido, true, luz));
        return Bloco.blocos.get(Bloco.blocos.size()-1);
    }
}
