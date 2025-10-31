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

public class ChunkUtil {
    public static float LUZ_SOL = 1.0f;
    public static final float LUZ_AMBIENTE = 0.25f;
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
		
		Chave chave = Mundo.chaveReuso.obtain();
		chave.x = chunk.chunkX + 1; chave.z = chunk.chunkZ;
		Chunk chunkXP = Mundo.chunks.get(chave);
		chave.x = chunk.chunkX - 1; chave.z = chunk.chunkZ;
		Chunk chunkXN = Mundo.chunks.get(chave);
		chave.x = chunk.chunkX; chave.z = chunk.chunkZ + 1;
		Chunk chunkZP = Mundo.chunks.get(chave);
		chave.x = chunk.chunkX; chave.z = chunk.chunkZ - 1;
		Chunk chunkZN = Mundo.chunks.get(chave);
		
		Mundo.chaveReuso.free(chave);

		for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
			for(int y = 0; y < Mundo.Y_CHUNK; y++) {
				for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
					int bloco = obterBloco(x, y, z, chunk);
					if(bloco == 0 || bloco == 4) continue;

					int idTopo = 0, idLado = 0, idBaixo = 0;

					switch(bloco) {
						case 1: idTopo = 0; idLado = 1; idBaixo = 2; break; 
						case 2: idTopo = 2; idLado = 2; idBaixo = 2; break; 
						case 3: idTopo = 3; idLado = 3; idBaixo = 3; break; 
						default: continue; 
					}
					// culling que consideram chunks ao redor
					if(y == Mundo.Y_CHUNK - 1 || !ehSolidoComChunk(x, y + 1, z, chunk, null)) 
						addFace(x,y,z,0, idTopo, vertsGeral, idcGeral, chunk);

					if(y == 0 || !ehSolidoComChunk(x, y - 1, z, chunk, null)) 
						addFace(x,y,z,1, idBaixo, vertsGeral, idcGeral, chunk);

					if(x == Mundo.TAM_CHUNK - 1) {
						if(chunkXP == null || !ehSolidoComChunk(0, y, z, chunkXP, null))
							addFace(x,y,z,2, idLado, vertsGeral, idcGeral, chunk);
					} else if(!ehSolidoComChunk(x + 1, y, z, chunk, null)) {
						addFace(x,y,z,2, idLado, vertsGeral, idcGeral, chunk);
					}
					if(x == 0) {
						if(chunkXN == null || !ehSolidoComChunk(Mundo.TAM_CHUNK - 1, y, z, chunkXN, null))
							addFace(x,y,z,3, idLado, vertsGeral, idcGeral, chunk);
					} else if(!ehSolidoComChunk(x - 1, y, z, chunk, null)) {
						addFace(x,y,z,3, idLado, vertsGeral, idcGeral, chunk);
					}
					if(z == Mundo.TAM_CHUNK - 1) {
						if(chunkZP == null || !ehSolidoComChunk(x, y, 0, chunkZP, null))
							addFace(x,y,z,4, idLado, vertsGeral, idcGeral, chunk);
					} else if(!ehSolidoComChunk(x, y, z + 1, chunk, null)) {
						addFace(x,y,z,4, idLado, vertsGeral, idcGeral, chunk);
					}
					if(z == 0) {
						if(chunkZN == null || !ehSolidoComChunk(x, y, Mundo.TAM_CHUNK - 1, chunkZN, null))
							addFace(x,y,z,5, idLado, vertsGeral, idcGeral, chunk);
					} else if(!ehSolidoComChunk(x, y, z - 1, chunk, null)) {
						addFace(x,y,z,5, idLado, vertsGeral, idcGeral, chunk);
					}
				}
			}
		}
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
	
    public static void addFace(int x, int y, int z, int faceId, int atlasId, FloatArrayUtil verts, IntArrayUtil idc, Chunk chunk) {
        float tam = 1f;
        float X = x * tam;
        float Y = y * tam;
        float Z = z * tam;

        float[][] faceVertices = new float[4][3];
        float[][] uvPadrao = new float[4][2]; 
		// nivel de luz pra face:
        float nivelLuz = calcularNivelLuz(x, y, z, faceId, chunk);
		int r = (int)(nivelLuz * 255);
        int g = (int)(nivelLuz * 255);
        int b = (int)(nivelLuz * 255);
        int a = 255;
		int cor = (a << 24) | (b << 16) | (g << 8) | r;

        switch(faceId) {
            case 0: // topo
                faceVertices[0] = new float[]{X+tam, Y+tam, Z};
                faceVertices[1] = new float[]{X, Y+tam, Z};
                faceVertices[2] = new float[]{X, Y+tam, Z+tam};
                faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
                uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
                break;
            case 1: // baixo
                faceVertices[0] = new float[]{X+tam, Y, Z+tam};
                faceVertices[1] = new float[]{X, Y, Z+tam};
                faceVertices[2] = new float[]{X, Y, Z};
                faceVertices[3] = new float[]{X+tam, Y, Z};
                uvPadrao = new float[][]{{1,0},{0,0},{0,1},{1,1}};
                break;
            case 2: // +X
                faceVertices[0] = new float[]{X+tam, Y, Z+tam};
                faceVertices[1] = new float[]{X+tam, Y, Z};
                faceVertices[2] = new float[]{X+tam, Y+tam, Z};
                faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
                uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
                break;
            case 3: // -X
                faceVertices[0] = new float[]{X, Y, Z};
                faceVertices[1] = new float[]{X, Y, Z+tam};
                faceVertices[2] = new float[]{X, Y+tam, Z+tam};
                faceVertices[3] = new float[]{X, Y+tam, Z};
                uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
                break;
            case 4: // +Z
                faceVertices[0] = new float[]{X, Y+tam, Z+tam};
                faceVertices[1] = new float[]{X, Y, Z+tam};
                faceVertices[2] = new float[]{X+tam, Y, Z+tam};
                faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
                uvPadrao = new float[][]{{0,0},{0,1},{1,1},{1,0}};
                break;
            case 5: // -Z
                faceVertices[0] = new float[]{X, Y, Z};
                faceVertices[1] = new float[]{X, Y+tam, Z};
                faceVertices[2] = new float[]{X+tam, Y+tam, Z};
                faceVertices[3] = new float[]{X+tam, Y, Z};
                uvPadrao = new float[][]{{0,1},{0,0},{1,0},{1,1}};
                break;
        }
        float[] atlasCoords = Mundo.atlasUVs.get(atlasId);
        if(atlasCoords == null) return;

        float u_min = atlasCoords[0];
        float v_min = atlasCoords[1];
        float u_max = atlasCoords[2];
        float v_max = atlasCoords[3];

        int vertConta = verts.tam / 6; // 6 floats por vertice

        for(int i = 0; i < 4; i++) {
            float vx = faceVertices[i][0], vy = faceVertices[i][1], vz = faceVertices[i][2];

            float u = u_min + uvPadrao[i][0] * (u_max - u_min);
            float v = v_min + uvPadrao[i][1] * (v_max - v_min);
            // luz:
            verts.add(vx); verts.add(vy); verts.add(vz); 
            verts.add(u); verts.add(v);
            verts.add(Float.intBitsToFloat(cor));
        }
        idc.add(vertConta + 0);
        idc.add(vertConta + 1);
        idc.add(vertConta + 2);
        idc.add(vertConta + 2);
        idc.add(vertConta + 3);
        idc.add(vertConta + 0);
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
			return obterBloco(x, y, z, chunk) != 0;
		}
		// se ta fora dos limites, verifica no mundo
		int worldX = chunk.chunkX * Mundo.TAM_CHUNK + x;
		int worldZ = chunk.chunkZ * Mundo.TAM_CHUNK + z;

		return Mundo.obterBlocoMundo(worldX, y, worldZ) != 0;
	}
	
	public static boolean ehSolidoComChunk(int x, int y, int z, Chunk chunk, Chunk chunkAdjacente) {
		if(x >= 0 && x < Mundo.TAM_CHUNK && y >= 0 && y < Mundo.Y_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
			return obterBloco(x, y, z, chunk) != 0;
		}
		return false;
	}
	
	public static byte obterLuz(int x, int y, int z, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
        int byteIdc = i / 4;
        int bitPos = (i % 4) * 2;
        return (byte)((chunk.luz[byteIdc] >> bitPos) & 0b11);
    }

    public static void defLuz(int x, int y, int z, byte valor, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
        int byteIdc = i / 4;
        int bitPos = (i % 4) * 2;

        byte mascaraLimpar = (byte) ~(0b11 << bitPos);
        byte mascaraDef = (byte)((valor & 0b11) << bitPos);

        chunk.luz[byteIdc] = (byte)((chunk.luz[byteIdc] & mascaraLimpar) | mascaraDef);
    }
	
	public static byte obterBloco(int x, int y, int z, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
        int byteIdc = i / 4;
        int bitPos = (i % 4) * 2;
        return (byte)((chunk.blocos[byteIdc] >> bitPos) & 0b11);
    }

    public static void defBloco(int x, int y, int z, byte valor, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
        int byteIdc = i / 4;
        int bitPos = (i % 4) * 2;

        byte mascaraLimpar = (byte) ~(0b11 << bitPos);
        byte mascaraDef= (byte)((valor & 0b11) << bitPos);

        chunk.blocos[byteIdc] = (byte)((chunk.blocos[byteIdc] & mascaraLimpar) | mascaraDef);
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
