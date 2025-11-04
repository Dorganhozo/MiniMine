package com.minimine.utils;

import com.minimine.utils.FloatArrayUtil;
import com.minimine.utils.IntArrayUtil;
import com.badlogic.gdx.graphics.Mesh;
import java.util.Map;
import java.util.HashMap;
import com.minimine.cenas.Chunk;
import java.util.Objects;
import com.minimine.cenas.Mundo;
import com.badlogic.gdx.utils.Pool;
import java.util.Arrays;
import com.minimine.utils.blocos.BlocoTipo;
import com.minimine.utils.blocos.BlocoModelo;

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
	
    public static float calcularNivelLuz(int x, int y, int z, int faceId, Chunk chunk) {
        float luzCeu = calcularLuzCeu(x, y, z, chunk);
        float luzFinal = Math.max(LUZ_AMBIENTE, luzCeu);

        luzFinal *= FACE_LUZ[faceId];

        return Math.max(0.1f, Math.min(1.0f, luzFinal));
    }

    public static float calcularLuzCeu(int x, int y, int z, Chunk chunk) {
        // se estiver no topo do mundo, recebe luz maxima
        if(y >= Mundo.Y_CHUNK - 1)  return LUZ_SOL;
        // se tem bloco solido em cima:
        int blocosAcima = 0;
        for(int cy = y + 1; cy < Mundo.Y_CHUNK; cy++) {
            if(ehSolido(x, cy, z, chunk)) blocosAcima++;
        }
        if(blocosAcima == 0) return LUZ_SOL;
        // luz badeada no num de blocos em cima
        float atenuacao = 1.0f - (blocosAcima * 0.15f); // 15% de redução por bloco
        return Math.max(LUZ_AMBIENTE, LUZ_SOL * atenuacao);
    }
	
	public static void attMesh(Chunk chunk, FloatArrayUtil vertsGeral, IntArrayUtil idcGeral) {
		attLuz(chunk);
		// chunks adkentiv3s, sei la comk escreve, cê entendeu
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

					BlocoTipo blocoTipo = BlocoTipo.criar(blocoId);
					if(blocoTipo == null) continue;

					lidarFacesDoBloco(x, y, z, blocoTipo,
					chunk, chunkXP, chunkXN, chunkZP, chunkZN,
					vertsGeral, idcGeral);
				}
			}
		}
		Mundo.chaveReuso.free(chave);
	}

	public static void lidarFacesDoBloco(int x, int y, int z, BlocoTipo blocoTipo,
	Chunk chunk, Chunk chunkXP, Chunk chunkXN, 
	Chunk chunkZP, Chunk chunkZN, FloatArrayUtil verts, IntArrayUtil idc) {
		float posX = x * 1f;
		float posY = y * 1f;
		float posZ = z * 1f;
		// face topo(Y+)
		if(deveRenderFaceTopo(x, y + 1, z, chunk, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 0, chunk);
			int textureId = blocoTipo.textureId(0);
			BlocoModelo.addFace(0, textureId, posX, posY, posZ, luz, verts, idc);
		}
		// face baixo(Y-)
		if(deveRenderFaceBaixo(x, y - 1, z, chunk, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 1, chunk);
			int textureId = blocoTipo.textureId(1);
			BlocoModelo.addFace(1, textureId, posX, posY, posZ, luz, verts, idc);
		}
		// face +X(leste)
		if(deveRenderFaceXPositivo(x + 1, y, z, chunk, chunkXP, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 2, chunk);
			int textureId = blocoTipo.textureId(2);
			BlocoModelo.addFace(2, textureId, posX, posY, posZ, luz, verts, idc);
		}
		// face -X (oeste)
		if(deveRenderFaceXNegativo(x - 1, y, z, chunk, chunkXN, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 3, chunk);
			int textureId = blocoTipo.textureId(3);
			BlocoModelo.addFace(3, textureId, posX, posY, posZ, luz, verts, idc);
		}
		// face +Z(norte)
		if(deveRenderFaceZPositivo(x, y, z + 1, chunk, chunkZP, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 4, chunk);
			int textureId = blocoTipo.textureId(4);
			BlocoModelo.addFace(4, textureId, posX, posY, posZ, luz, verts, idc);
		}
		// face -Z(sul)
		if(deveRenderFaceZNegativo(x, y, z - 1, chunk, chunkZN, blocoTipo)) {
			float luz = calcularNivelLuz(x, y, z, 5, chunk);
			int textureId = blocoTipo.textureId(5);
			BlocoModelo.addFace(5, textureId, posX, posY, posZ, luz, verts, idc);
		}
	}
	// metodos especificos pra cada face que lidam com chunks adjacentes
	public static boolean deveRenderFaceTopo(int x, int y, int z, Chunk chunk, BlocoTipo blocoAtual) {
		if(y >= Mundo.Y_CHUNK) return true;
		BlocoTipo adjacente = obterblocoTipo(x, y, z, chunk, null);
		return adjacente == null || !adjacente.solido || blocoAtual.transparente;
	}

	public static boolean deveRenderFaceBaixo(int x, int y, int z, Chunk chunk, BlocoTipo blocoAtual) {
		if(y < 0) return true; // fora do fundo - sempre renderiza
		BlocoTipo adjacente = obterblocoTipo(x, y, z, chunk, null);
		return adjacente == null || !adjacente.solido || blocoAtual.transparente;
	}

	public static boolean deveRenderFaceXPositivo(int x, int y, int z, Chunk chunk, Chunk chunkXP, BlocoTipo blocoAtual) {
		if(x >= Mundo.TAM_CHUNK) {
			// ta na borda direita, verificar chunk adjacente
			if(chunkXP == null) return true;
			BlocoTipo adjacente = obterblocoTipo(0, y, z, chunkXP, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			BlocoTipo adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		}
	}

	public static boolean deveRenderFaceXNegativo(int x, int y, int z, Chunk chunk, Chunk chunkXN, BlocoTipo blocoAtual) {
		if(x < 0) {
			// ta na borda esquerda, verificar chunk adjacente
			if(chunkXN == null) return true;
			BlocoTipo adjacente = obterblocoTipo(Mundo.TAM_CHUNK - 1, y, z, chunkXN, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			BlocoTipo adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		}
	}

	public static boolean deveRenderFaceZPositivo(int x, int y, int z, Chunk chunk, Chunk chunkZP, BlocoTipo blocoAtual) {
		if(z >= Mundo.TAM_CHUNK) {
			// tana borda frontal, verificar chunk adjacente
			if (chunkZP == null) return true;
			BlocoTipo adjacente = obterblocoTipo(x, y, 0, chunkZP, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			BlocoTipo adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		}
	}

	public static boolean deveRenderFaceZNegativo(int x, int y, int z, Chunk chunk, Chunk chunkZN, BlocoTipo blocoAtual) {
		if(z < 0) {
			// ta na borda traseira, verificar chunk adjacente
			if(chunkZN == null) return true;
			BlocoTipo adjacente = obterblocoTipo(x, y, Mundo.TAM_CHUNK - 1, chunkZN, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			BlocoTipo adjacente = obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		}
	}

	public static BlocoTipo obterblocoTipo(int x, int y, int z, Chunk chunk, Chunk chunkAdj) {
		// se as coordenadas estão dentro do chunk atual
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			byte blocoId = obterBloco(x, y, z, chunk);
			return blocoId == 0 ? null : BlocoTipo.criar(blocoId);
		}
		// se ta fora dos limites e temos um chunk adjacente especifico
		if(chunkAdj != null) {
			// ajusta coordenadas pra o sistema do chunk adjacente
			int adjX = x;
			int adjZ = z;

			if(x < 0) adjX = Mundo.TAM_CHUNK - 1;
			else if(x >= Mundo.TAM_CHUNK) adjX = 0;

			if(z < 0) adjZ = Mundo.TAM_CHUNK - 1;
			else if(z >= Mundo.TAM_CHUNK) adjZ = 0;

			byte blocoId = obterBloco(adjX, y, adjZ, chunkAdj);
			return blocoId == 0 ? null : BlocoTipo.criar(blocoId);
		}
		// fora dos limites e sem chunk adjacente = considerar como ar
		return null;
	}

    public static void attLuz(Chunk chunk) {
        for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
            for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
                boolean bloqueado = false;
                // do topo pra baixo
                for(int y = Mundo.Y_CHUNK - 1; y >= 0; y--) {
                    if(!bloqueado) {
                        // se encontrou um bloco solido, começa a bloquear
                        if(ehSolido(x, y, z, chunk)) {
                            bloqueado = true;
                            // armazena luz reduzida pra esse bloco
                            defLuz(x, y, z, (byte)10, chunk); // 10/15 de luz
                        } else {
                            // ar, luz maxima
                            defLuz(x, y, z, (byte)15, chunk);
                        }
                    } else {
                        // ja ta bloqueado, luz ambiente min
                        defLuz(x, y, z, (byte)2, chunk); // 2/15 de luz
                    }
                }
            }
        }
    }
	
	public static void defMesh(Mesh mesh, FloatArrayUtil verts, IntArrayUtil idc) {
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
	
	public static boolean ehSolido(int x, int y, int z, Chunk chunk) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			byte b = obterBloco(x, y, z, chunk);
			return  b != 0 && b != 7;
		}
		// se ta fora dos limites, verifica no mundo
		int mundoX = chunk.x * Mundo.TAM_CHUNK + x;
		int mundoZ = chunk.z * Mundo.TAM_CHUNK + z;
		
		byte b = Mundo.obterBlocoMundo(mundoX, y, mundoZ);

		return b != 0 && b != 7;
	}
	
	public static boolean ehSolidoComChunk(int x, int y, int z, Chunk chunk, Chunk chunkAdjacente) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			byte b = obterBloco(x, y, z, chunk);
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
		chunk.luz[byteIdc] = (byte)((chunk.blocos[byteIdc] & mascara) | ((valor & 0b1111) << bitPos));
    }
	
	public static byte obterBloco(int x, int y, int z, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
		int byteIdc = i / 2;
		int bitPos = (i % 2) * 4;
        return (byte)((chunk.blocos[byteIdc] >> bitPos) & 0b1111);
    }

    public static void defBloco(int x, int y, int z, byte valor, Chunk chunk) {
		int i = x + (z * 16) + (y * 16 * 16);
		int byteIdc = i / 2;
		int bitPos = (i % 2) * 4;

		byte mascara = (byte) ~(0b1111 << bitPos);
		chunk.blocos[byteIdc] = (byte)((chunk.blocos[byteIdc] & mascara) | ((valor & 0b1111) << bitPos));
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
}
