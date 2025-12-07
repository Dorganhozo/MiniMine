package com.minimine.utils.chunks;

import com.minimine.utils.blocos.Bloco;
import com.minimine.cenas.Mundo;

public class ChunkUtil {
	public static Bloco obterblocoTipo(int x, int y, int z, Chunk chunk, Chunk chunkAdj) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			int blocoId = obterBloco(x, y, z, chunk);
			return blocoId == 0 ? null : Bloco.numIds.get(blocoId);
		}
		if(chunkAdj != null) {
			if(y < 0 || y >= Mundo.Y_CHUNK) return null;

			int adjX = x;
			int adjZ = z;

			if(x < 0) adjX = Mundo.TAM_CHUNK - 1;
			else if(x >= Mundo.TAM_CHUNK) adjX = 0;

			if(z < 0) adjZ = Mundo.TAM_CHUNK - 1;
			else if(z >= Mundo.TAM_CHUNK) adjZ = 0;

			int blocoId = obterBloco(adjX, y, adjZ, chunkAdj);
			return blocoId == 0 ? null : Bloco.numIds.get(blocoId);
		}
		return null;
	}

	public static boolean ehSolido(int x, int y, int z, Chunk chunk) {
		if(x < 0 || x >= 16 || y < 0 || y >= Mundo.Y_CHUNK || z < 0 || z >= 16) {
			return false; // ou delega ao chunk adjacente antes
		}
		int total = x + (z << 4) + (y * 256);
		int[] arr = chunk.blocos;

		int bloco;
		if(chunk.usaPaleta) {
			int idc = (arr[total / chunk.blocosPorInt] >>> 
				((total % chunk.blocosPorInt) * chunk.paletaBits))
				& ((1 << chunk.paletaBits) - 1);
			bloco = chunk.paleta[idc];
		} else {
			bloco = (arr[total / chunk.blocosPorInt] >>> 
				((total % chunk.blocosPorInt) * chunk.bitsPorBloco))
				& ((1 << chunk.bitsPorBloco) - 1);
		}
		return bloco != 0 && bloco != 7;
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
		int bitPos = (indiceGlobal % blocosPorInt) * bits;
		int mascara = (1 << bits) - 1;
		return (arr[idc] >>> bitPos) & mascara;
	}

	public static void gravarPacote(int indiceGlobal, int valor, int bits, int[] arr, int blocosPorInt) {
		int idc = indiceGlobal / blocosPorInt;
		int bitPos = (indiceGlobal % blocosPorInt) * bits;
		int mascara = ((1 << bits) - 1) << bitPos;
		arr[idc] = (arr[idc] & ~mascara) | ((valor & ((1 << bits) - 1)) << bitPos);
	}

	public static int obterBloco(int x, int y, int z, Chunk chunk) {
		int total = x + (z << 4) + (y << 8); 

		if(chunk.blocos == null) return 0;

		if(chunk.usaPaleta) {
			int idc = lerPacote(total, chunk.paletaBits, chunk.blocos, chunk.blocosPorInt);
			if(idc < 0 || idc >= chunk.paletaTam) return 0;
			return chunk.paleta[idc];
		} else {
			return lerPacote(total, chunk.bitsPorBloco, chunk.blocos, chunk.blocosPorInt);
		}
	}

	public static void defBloco(int x, int y, int z, CharSequence nome, Chunk chunk) {
		int bloco = nome.equals("ar") ? 0 : Bloco.texIds.get(nome).tipo;
		int totalTam = Mundo.TAM_CHUNK; // local pra JIT
		int total = x + (z * totalTam) + (y * totalTam * totalTam);
		// se ta em modo paleta, tenta usar/expandir paleta
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
						// aumenta array se necessario
						int[] novo = new int[Math.max(chunk.paleta.length * 2, capacidade)];
						System.arraycopy(chunk.paleta, 0, novo, 0, chunk.paleta.length);
						chunk.paleta = novo;
					}
					chunk.paleta[chunk.paletaTam] = bloco;
					idc = chunk.paletaTam++;
				} else {
					// paleta cheia para paletaBits atual
					if(chunk.paletaBits < 8) {
						// aumenta paletaBits(repacota indices)
						refazerPaleta(chunk.paletaBits + 1, chunk);
						// inserir agora(deve caber)
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
			// se ainda estamos em paleta(idc valido), grava ndice
			if(chunk.usaPaleta) {
				gravarPacote(total, idc, chunk.paletaBits, chunk.blocos, chunk.blocosPorInt);
				return;
			}
			// caso contrario, conversão ocorreu e prossegue pra modo direto
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
				// idAntigo é indice de paleta -> mantem mesmo indice no novo pacote
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
}
