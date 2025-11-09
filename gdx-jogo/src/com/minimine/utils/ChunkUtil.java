package com.minimine.utils;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.badlogic.gdx.graphics.Mesh;
import java.util.Map;
import java.util.HashMap;
import com.minimine.cenas.Chunk;
import java.util.Objects;
import com.minimine.cenas.Mundo;
import com.badlogic.gdx.utils.Pool;
import java.util.Arrays;
import com.minimine.utils.blocos.BlocoModelo;
import java.util.List;
import com.minimine.cenas.Bloco;
import java.util.ArrayList;

public class ChunkUtil {
    public static float LUZ_SOL = 1.0f;
    public static float LUZ_AMBIENTE = 0.25f;
    public static final float[] FACE_LUZ = {
        1.0f, // topo, luz max
        0.4f, // baixo, mais escuro
        0.7f, // lado +X
        0.7f, // lado -X  
        0.8f, // lado +Z
        0.8f  // lado -Z
    };
	public static List<Bloco> blocos = new ArrayList<>();

    public static float calcularNivelLuz(int x, int y, int z, int faceId, Chunk chunk) {
        float luzCeu = calcularLuzCeu(x, y, z, chunk);
        float luzFinal = Math.max(LUZ_AMBIENTE, luzCeu);

        luzFinal *= FACE_LUZ[faceId];

        return Math.max(0.1f, Math.min(1.0f, luzFinal));
    }

    public static float calcularLuzCeu(int x, int y, int z, Chunk chunk) {
        if(y >= Mundo.Y_CHUNK - 1)  return LUZ_SOL;
        int blocosAcima = 0;
        for(int cy = y + 1; cy < Mundo.Y_CHUNK; cy++) {
            if(ehSolido(x, cy, z, chunk)) blocosAcima++;
        }
        if(blocosAcima == 0) return LUZ_SOL;
        float atenuacao = 1.0f - (blocosAcima * 0.15f);
        return Math.max(LUZ_AMBIENTE, LUZ_SOL * atenuacao);
    }

	public static void attMesh(Chunk chunk, FloatArrayUtil vertsGeral, ShortArrayUtil idcGeral) {
		attLuz(chunk);
		Chave chave = Mundo.chaveReuso.obtain();
		chave.x = chunk.x + 1; chave.z = chunk.z;
		Chunk chunkXP = Mundo.chunks.get(chave);
		chave.x = chunk.x - 1; chave.z = chunk.z;
		Chunk chunkXN = Mundo.chunks.get(chave);
		chave.x = chunk.x; chave.z = chunk.z + 1;
		Chunk chunkZP = Mundo.chunks.get(chave);
		chave.x = chunk.x; chave.z = chunk.z - 1;
		Chunk chunkZN = Mundo.chunks.get(chave);

		for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
			for(int y = 0; y < Mundo.Y_CHUNK; y++) {
				for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
					int blocoId = obterBloco(x, y, z, chunk);
					if(blocoId == 0) continue;

					Bloco blocoTipo = Bloco.criar(blocoId);
					if(blocoTipo == null) continue;

					lidarFacesDoBloco(x, y, z, blocoTipo,
									  chunk, chunkXP, chunkXN, chunkZP, chunkZN,
									  vertsGeral, idcGeral);
				}
			}
		}
		Mundo.chaveReuso.free(chave);
	}

	public static void lidarFacesDoBloco(int x, int y, int z, Bloco blocoTipo,
										 Chunk chunk, Chunk chunkXP, Chunk chunkXN, 
										 Chunk chunkZP, Chunk chunkZN, FloatArrayUtil verts, ShortArrayUtil idc) {
		float posX = x * 1f;
		float posY = y * 1f;
		float posZ = z * 1f;
		if(deveRenderFaceTopo(x, y + 1, z, chunk, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 0, chunk);
			int textureId = blocoTipo.textureId(0);
			BlocoModelo.addFace(0, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceBaixo(x, y - 1, z, chunk, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 1, chunk);
			int textureId = blocoTipo.textureId(1);
			BlocoModelo.addFace(1, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceXPositivo(x + 1, y, z, chunk, chunkXP, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 2, chunk);
			int textureId = blocoTipo.textureId(2);
			BlocoModelo.addFace(2, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceXNegativo(x - 1, y, z, chunk, chunkXN, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 3, chunk);
			int textureId = blocoTipo.textureId(3);
			BlocoModelo.addFace(3, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceZPositivo(x, y, z + 1, chunk, chunkZP, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 4, chunk);
			int textureId = blocoTipo.textureId(4);
			BlocoModelo.addFace(4, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceZNegativo(x, y, z - 1, chunk, chunkZN, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 5, chunk);
			int textureId = blocoTipo.textureId(5);
			BlocoModelo.addFace(5, textureId, posX, posY, posZ, luz, verts, idc);
		}
	}

	public static boolean deveRenderFaceTopo(int x, int y, int z, Chunk chunk, Bloco blocoAtual) {
		if(y >= Mundo.Y_CHUNK) return true;
		Bloco adjacente = obterblocoTipo(x, y, z, chunk, null);
		return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
	}

	public static boolean deveRenderFaceBaixo(int x, int y, int z, Chunk chunk, Bloco blocoAtual) {
		if(y < 0) return true;
		Bloco adjacente = obterblocoTipo(x, y, z, chunk, null);
		return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
	}

	public static boolean deveRenderFaceXPositivo(int x, int y, int z, Chunk chunk, Chunk chunkXP, Bloco blocoAtual) {
		if(x >= Mundo.TAM_CHUNK) {
			if(chunkXP == null) return true;
			Bloco adjacente = obterblocoTipo(0, y, z, chunkXP, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveRenderFaceXNegativo(int x, int y, int z, Chunk chunk, Chunk chunkXN, Bloco blocoAtual) {
		if(x < 0) {
			if(chunkXN == null) return true;
			Bloco adjacente = obterblocoTipo(Mundo.TAM_CHUNK - 1, y, z, chunkXN, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveRenderFaceZPositivo(int x, int y, int z, Chunk chunk, Chunk chunkZP, Bloco blocoAtual) {
		if(z >= Mundo.TAM_CHUNK) {
			if (chunkZP == null) return true;
			Bloco adjacente = obterblocoTipo(x, y, 0, chunkZP, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveRenderFaceZNegativo(int x, int y, int z, Chunk chunk, Chunk chunkZN, Bloco blocoAtual) {
		if(z < 0) {
			if(chunkZN == null) return true;
			Bloco adjacente = obterblocoTipo(x, y, Mundo.TAM_CHUNK - 1, chunkZN, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}
	
	public static boolean deveOcultarFace(Bloco blocoAtual, Bloco blocoAdjacente) {
		// ocultam faces entre si
		if(blocoAtual.cullingAlto && blocoAdjacente.cullingAlto) {
			return true;
		}
		// normais se ocultam
		if(blocoAtual.solido && blocoAdjacente.solido) {
			return true;
		}
		// outros transparentes não se ocultam entre tipos diferentes
		if(blocoAtual.transparente && blocoAdjacente.transparente) {
			return blocoAtual.tipo == blocoAdjacente.tipo;
		}
		return false;
	}

	public static Bloco obterblocoTipo(int x, int y, int z, Chunk chunk, Chunk chunkAdj) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			int blocoId = obterBloco(x, y, z, chunk);
			return blocoId == 0 ? null : Bloco.criar(blocoId);
		}
		if(chunkAdj != null) {
			int adjX = x;
			int adjZ = z;

			if(x < 0) adjX = Mundo.TAM_CHUNK - 1;
			else if(x >= Mundo.TAM_CHUNK) adjX = 0;

			if(z < 0) adjZ = Mundo.TAM_CHUNK - 1;
			else if(z >= Mundo.TAM_CHUNK) adjZ = 0;

			int blocoId = obterBloco(adjX, y, adjZ, chunkAdj);
			return blocoId == 0 ? null : Bloco.criar(blocoId);
		}
		return null;
	}

    public static void attLuz(Chunk chunk) {
        for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
            for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
                boolean bloqueado = false;
                for(int y = Mundo.Y_CHUNK - 1; y >= 0; y--) {
                    if(!bloqueado) {
                        if(ehSolido(x, y, z, chunk)) {
                            bloqueado = true;
                            defLuz(x, y, z, (byte)10, chunk);
                        } else {
                            defLuz(x, y, z, (byte)15, chunk);
                        }
                    } else {
                        defLuz(x, y, z, (byte)2, chunk);
                    }
                }
            }
        }
    }

	public static void defMesh(Mesh mesh, FloatArrayUtil verts, ShortArrayUtil idc) {
		if(verts.tam == 0) {
			mesh.setVertices(new float[0]);
			mesh.setIndices(new short[0]);
			return;
		}
		float[] vArr = verts.praArray();
		short[] iArr = idc.praArray();
		short[] sIdc = new short[iArr.length];
		for(int i = 0; i < iArr.length; i++) sIdc[i] = iArr[i];
		mesh.setVertices(vArr);
		mesh.setIndices(sIdc);
	}

	public static boolean ehSolido(int x, int y, int z, Chunk chunk) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			int b = obterBloco(x, y, z, chunk);
			return  b != 0 && b != 7;
		}
		int mundoX = chunk.x * Mundo.TAM_CHUNK + x;
		int mundoZ = chunk.z * Mundo.TAM_CHUNK + z;

		int b = Mundo.obterBlocoMundo(mundoX, y, mundoZ);

		return b != 0 && b != 7;
	}

	public static boolean ehSolidoComChunk(int x, int y, int z, Chunk chunk, Chunk chunkAdjacente) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			int b = obterBloco(x, y, z, chunk);
			return b != 0 && b != 7;
		}
		return false;
	}

	public static byte obterLuz(int x, int y, int z, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
		int byteIdc = i / 2;
		int bitPos = (i % 2) * 4;
        return (byte)((chunk.luz[byteIdc] >> bitPos) & 0b1111);
    }

    public static void defLuz(int x, int y, int z, byte valor, Chunk chunk) {
		int i = x + (z * 16) + (y * 16 * 16);
		int byteIdc = i / 2;
		int bitPos = (i % 2) * 4;
		byte mascara = (byte) ~(0b1111 << bitPos);
		chunk.luz[byteIdc] = (byte)((chunk.luz[byteIdc] & mascara) | ((valor & 0b1111) << bitPos));
    }

	public static int bitsPraMaxId(int maxId) {
		int val = 1;
		int bits = 0;
		while(val <= maxId && bits < 8) { val <<= 1; bits++; }
		if(bits == 0) bits = 1;
		return Math.min(bits, 8);
	}

	public static int lerPacote(int indiceGlobal, int bits, int[] arr, int blocosPorInt) {
		int idc = indiceGlobal / blocosPorInt;
		int pos = indiceGlobal % blocosPorInt;
		int bitPos = pos * bits;
		int mascara = (1 << bits) - 1;
		return (arr[idc] >>> bitPos) & mascara;
	}

	public static void gravarPacote(int indiceGlobal, int valor, int bits, int[] arr, int blocosPorInt) {
		int idc = indiceGlobal / blocosPorInt;
		int pos = indiceGlobal % blocosPorInt;
		int bitPos = pos * bits;
		int mascara = ((1 << bits) - 1) << bitPos;
		arr[idc] = (arr[idc] & ~mascara) | ((valor & ((1 << bits) - 1)) << bitPos);
	}

	public static int obterBloco(int x, int y, int z, Chunk chunk) {
		int totalWidth = Mundo.TAM_CHUNK;
		int total = x + (z * totalWidth) + (y * totalWidth * totalWidth);
		if(chunk.blocos == null) return 0;
		if(chunk.usaPaleta) {
			int bits = chunk.paletaBits;
			int blocosPorInt = chunk.blocosPorInt;
			int idx = lerPacote(total, bits, chunk.blocos, blocosPorInt);
			if(idx < 0 || idx >= chunk.paletaTam) return 0;
			return chunk.paleta[idx];
		} else {
			int bits = chunk.bitsPorBloco;
			int blocosPorInt = chunk.blocosPorInt;
			int val = lerPacote(total, bits, chunk.blocos, blocosPorInt);
			return val;
		}
	}
	
	public static void defBloco(int x, int y, int z, int bloco, Chunk chunk) {
		int totalTam = Mundo.TAM_CHUNK;
		int total = x + (z * totalTam) + (y * totalTam * totalTam);
		// se estamos em modo paleta, tentamos usar/expandir paleta
		if(chunk.usaPaleta) {
			// procura na paleta
			int idc = -1;
			for(int i = 0; i < chunk.paletaTam; i++) {
				if(chunk.paleta[i] == bloco) { idc = i; break; }
			}
			if(idc == -1) {
				// não existe ainda na paleta -> tentar inserir
				int capacidade = 1 << chunk.paletaBits;
				if(chunk.paletaTam < capacidade) {
					// cabe na paleta atual
					if(chunk.paletaTam >= chunk.paleta.length) {
						// aumenta array se necessário
						int[] novo = new int[Math.max(chunk.paleta.length * 2, capacidade)];
						System.arraycopy(chunk.paleta, 0, novo, 0, chunk.paleta.length);
						chunk.paleta = novo;
					}
					chunk.paleta[chunk.paletaTam] = bloco;
					idc = chunk.paletaTam++;
				} else {
					// paleta cheia para paletaBits atual
					if(chunk.paletaBits < 8) {
						// aumenta paletaBits (repack indices)
						refazerPaleta(chunk.paletaBits + 1, chunk);
						// inserir agora (deve caber)
						int[] pal = chunk.paleta;
						for(int i = 0; i < chunk.paletaTam; i++) {
							if(pal[i] == bloco) { idc = i; break; }
						}
						if(idc == -1) {
							if(chunk.paletaTam >= chunk.paleta.length) {
								int[] novo = new int[Math.max(chunk.paleta.length * 2, 1 << chunk.paletaBits)];
								System.arraycopy(chunk.paleta, 0, novo, 0, chunk.paleta.length);
								chunk.paleta = novo;
							}
							chunk.paleta[chunk.paletaTam] = bloco;
							idc = chunk.paletaTam++;
						}
					} else {
						// paleta ja com 8 bits e cheia -> converte para modo direto
						convertPaletaDireto(chunk, bloco);
						// agora no modo direto
					}
				}
			}
			// se ainda estamos em paleta (idx válido), gravamos índice
			if(chunk.usaPaleta) {
				gravarPacote(total, idc, chunk.paletaBits, chunk.blocos, chunk.blocosPorInt);
				return;
			}
			// caso contrario, conversion ocorreu e prosseguimos para modo direto
		}
		// kodo direto: garantias de bits e compactação se necessario
		if(!chunk.usaPaleta) {
			if(bloco > chunk.maxIds) {
				ChunkUtil.compactar(ChunkUtil.bitsPraMaxId(bloco), chunk);
			}
			// escrever valor direto
			gravarPacote(total, bloco, chunk.bitsPorBloco, chunk.blocos, chunk.blocosPorInt);
		}
	}

	public static void refazerPaleta(int novosBits, Chunk chunk) {
		if(!chunk.usaPaleta) return;
		if(novosBits <= 0 || novosBits > 8) novosBits = 8;

		int bitsAntigo = chunk.paletaBits;
		int blocosPorIntAntigo = chunk.blocosPorInt;
		int[] antigos = chunk.blocos;

		chunk.paletaBits = novosBits;
		chunk.blocosPorInt = 32 / chunk.paletaBits;

		int totalBlocos = Mundo.TAM_CHUNK * Mundo.Y_CHUNK * Mundo.TAM_CHUNK;
		int tamNovo = (totalBlocos + chunk.blocosPorInt - 1) / chunk.blocosPorInt;
		int[] novos = new int[tamNovo];

		if(antigos != null && blocosPorIntAntigo > 0 && bitsAntigo > 0) {
			for(int i = 0; i < totalBlocos; i++) {
				int idAntigo = (antigos[i / blocosPorIntAntigo] >>> ((i % blocosPorIntAntigo) * bitsAntigo))
					& ((1 << bitsAntigo) - 1);
				// idAntigo é índice de paleta -> mantém mesmo índice no novo packing
				int idNovoIdc = i / chunk.blocosPorInt;
				int idNovoBit = (i % chunk.blocosPorInt) * chunk.paletaBits;
				novos[idNovoIdc] |= (idAntigo & ((1 << chunk.paletaBits) - 1)) << idNovoBit;
			}
		}
		chunk.blocos = novos;
	}

	// converte completamente do modo paleta para modo direto
	public static void convertPaletaDireto(Chunk chunk, int blocoExtra) {
		// encontra o maior id real que precisa ser representado
		int maxVal = blocoExtra;
		for(int i = 0; i < chunk.paletaTam; i++) {
			if(chunk.paleta[i] > maxVal) maxVal = chunk.paleta[i];
		}
		int bitsParaReal = bitsPraMaxId(maxVal);
		if(bitsParaReal < 1) bitsParaReal = 1;

		int bitsAntigo = chunk.paletaBits;
		int blocosPorIntAntigo = chunk.blocosPorInt;
		int[] antigos = chunk.blocos;

		chunk.usaPaleta = false;
		chunk.bitsPorBloco = bitsParaReal;
		chunk.blocosPorInt = 32 / chunk.bitsPorBloco;
		chunk.maxIds = (1 << chunk.bitsPorBloco) - 1;

		int totalBlocos = Mundo.TAM_CHUNK * Mundo.Y_CHUNK * Mundo.TAM_CHUNK;
		int tamNovo = (totalBlocos + chunk.blocosPorInt - 1) / chunk.blocosPorInt;
		int[] novos = new int[tamNovo];

		if(antigos != null && blocosPorIntAntigo > 0 && bitsAntigo > 0) {
			for(int i = 0; i < totalBlocos; i++) {
				int idxPal = (antigos[i / blocosPorIntAntigo] >>> ((i % blocosPorIntAntigo) * bitsAntigo))
					& ((1 << bitsAntigo) - 1);
				int real = 0;
				if(idxPal >= 0 && idxPal < chunk.paletaTam) real = chunk.paleta[idxPal];
				int idNovoIdc = i / chunk.blocosPorInt;
				int idNovoBit = (i % chunk.blocosPorInt) * chunk.bitsPorBloco;
				novos[idNovoIdc] |= (real & ((1 << chunk.bitsPorBloco) - 1)) << idNovoBit;
			}
		}
		chunk.blocos = novos;
		// limpa paleta pra liberar memoria
		chunk.paleta = null;
		chunk.paletaTam = 0;
		chunk.paletaBits = 0;
	}

	public static void compactar(int bitsPorBloco, Chunk chunk) {
		// se chunk estava em paleta e compactar é pedido pra modo direto
		// converte paleta->direto usando bitsPorBloco calculado
		if(chunk.usaPaleta && bitsPorBloco > 0) {
			// converter paleta para direto mantendi bitsPorBloco minimo requerido
			// descobre maior id na paleta
			int maxVal = 0;
			if(chunk.paleta != null) {
				for(int i = 0; i < chunk.paletaTam; i++) {
					if(chunk.paleta[i] > maxVal) maxVal = chunk.paleta[i];
				}
			}
			if(bitsPraMaxId(maxVal) > bitsPorBloco) {
				bitsPorBloco = bitsPraMaxId(maxVal);
			}
			convertPaletaDireto(chunk, 0);
			// agora talvez seja necessario reajustar bits se convertPaleta escolheu bits diferentes
			if(chunk.bitsPorBloco < bitsPorBloco) {
				// aumenta bitsPorBloco se solicitado(refaz direto)
				int bitsAntigo = chunk.bitsPorBloco;
				int blocosPorIntAntigo = chunk.blocosPorInt;
				int[] antigos = chunk.blocos;

				chunk.bitsPorBloco = bitsPorBloco;
				chunk.blocosPorInt = 32 / chunk.bitsPorBloco;
				chunk.maxIds = (1 << chunk.bitsPorBloco) - 1;

				int totalBlocos = Mundo.TAM_CHUNK * Mundo.Y_CHUNK * Mundo.TAM_CHUNK;
				int tamNovo = (totalBlocos + chunk.blocosPorInt - 1) / chunk.blocosPorInt;
				int[] novos = new int[tamNovo];

				if(antigos != null && blocosPorIntAntigo > 0 && bitsAntigo > 0) {
					for(int i = 0; i < totalBlocos; i++) {
						int idAntigo = (antigos[i / blocosPorIntAntigo] >>> ((i % blocosPorIntAntigo) * bitsAntigo))
							& ((1 << bitsAntigo) - 1);
						int idNovoIdc = i / chunk.blocosPorInt;
						int idNovoBit = (i % chunk.blocosPorInt) * chunk.bitsPorBloco;
						novos[idNovoIdc] |= (idAntigo & ((1 << chunk.bitsPorBloco) - 1)) << idNovoBit;
					}
				}
				chunk.blocos = novos;
			}
			return;
		}
		// comportamento antigo
		if(bitsPorBloco < 1) bitsPorBloco = 1;

		int bitsAntigo = chunk.bitsPorBloco;
		int blocosPorIntAntigo = chunk.blocosPorInt;
		int[] antigos = chunk.blocos;

		chunk.maxIds = (1 << bitsPorBloco) - 1;
		chunk.bitsPorBloco = bitsPorBloco;
		chunk.blocosPorInt = 32 / chunk.bitsPorBloco;

		int totalBlocos = Mundo.TAM_CHUNK * Mundo.Y_CHUNK * Mundo.TAM_CHUNK;
		int tamNovo = (totalBlocos + chunk.blocosPorInt - 1) / chunk.blocosPorInt;
		int[] novos = new int[tamNovo];

		if(antigos != null && blocosPorIntAntigo > 0 && bitsAntigo > 0) {
			for(int i = 0; i < totalBlocos; i++) {
				int idAntigo = (antigos[i / blocosPorIntAntigo] >>> ((i % blocosPorIntAntigo) * bitsAntigo))
					& ((1 << bitsAntigo) - 1);
				int idNovoIdc = i / chunk.blocosPorInt;
				int idNovoBit = (i % chunk.blocosPorInt) * chunk.bitsPorBloco;
				novos[idNovoIdc] |= (idAntigo & ((1 << chunk.bitsPorBloco) - 1)) << idNovoBit;
			}
		}
		chunk.blocos = novos;
	}

	public static class Chave {
		public int x, z;
		public Chave(int x, int z) {this.x = x; this.z = z;}
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o.getClass() != getClass()) return false;
			Chave chave = (Chave) o;
			return  x == chave.x && z == chave.z;
		}
		@Override public int hashCode() {return Objects.hash(x, z);}
	}
}
