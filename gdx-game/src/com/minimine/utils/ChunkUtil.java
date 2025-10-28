package com.minimine.utils;

import com.minimine.utils.FloatArrayUtil;
import com.minimine.utils.IntArrayUtil;
import com.badlogic.gdx.graphics.Mesh;
import java.util.Map;
import java.util.HashMap;
import com.minimine.cenas.Chunk;
import java.util.Objects;
import com.minimine.cenas.Mundo;

public class ChunkUtil {
	public static void attMesh(Chunk chunk, FloatArrayUtil vertsGeral, IntArrayUtil idcGeral) {
		for(int x = 0; x < chunk.TAM_CHUNKX; x++) {
			for(int y = 0; y < chunk.TAM_CHUNKY; y++) {
				for(int z = 0; z < chunk.TAM_CHUNKZ; z++) {
					int bloco = chunk.chunk[x][y][z];
					if(bloco == 0 || bloco == 4) continue;

					int idTopo = 0, idLado = 0, idBaixo = 0;

					switch(bloco) {
						case 1: idTopo = 0; idLado = 1; idBaixo = 2; break; 
						case 2: idTopo = 2; idLado = 2; idBaixo = 2; break; 
						case 3: idTopo = 3; idLado = 3; idBaixo = 3; break; 
                        default: continue; 
					}
                    // adiciona faces
					if(y == chunk.TAM_CHUNKY - 1 || !ehSolido(x, y + 1, z, chunk)) addFace(x,y,z,0, idTopo, vertsGeral, idcGeral, chunk);
					if(y == 0 || !ehSolido(x, y - 1, z, chunk)) addFace(x,y,z,1, idBaixo, vertsGeral, idcGeral, chunk);
					if(x == chunk.TAM_CHUNKX - 1 || !ehSolido(x + 1, y, z, chunk)) addFace(x,y,z,2, idLado, vertsGeral, idcGeral, chunk);
					if(x == 0 || !ehSolido(x - 1, y, z, chunk)) addFace(x,y,z,3, idLado, vertsGeral, idcGeral, chunk);
					if(z == chunk.TAM_CHUNKZ - 1 || !ehSolido(x, y, z + 1, chunk)) addFace(x,y,z,4, idLado, vertsGeral, idcGeral, chunk);
					if(z == 0 || !ehSolido(x, y, z - 1, chunk)) addFace(x,y,z,5, idLado, vertsGeral, idcGeral, chunk);
				}
			}
		}
	}

	public static void attMesh(Chunk chunk) {
		FloatArrayUtil vertsGeral = new FloatArrayUtil(); 
		IntArrayUtil idcGeral = new IntArrayUtil(); 

		for(int x = 0; x < chunk.TAM_CHUNKX; x++) {
			for(int y = 0; y < chunk.TAM_CHUNKY; y++) {
				for(int z = 0; z < chunk.TAM_CHUNKZ; z++) {
					int bloco = chunk.chunk[x][y][z];
					if(bloco == 0 || bloco == 4) continue;

					int idTopo = 0, idLado = 0, idBaixo = 0;

					switch(bloco) {
						case 1: idTopo = 0; idLado = 1; idBaixo = 2; break; 
						case 2: idTopo = 2; idLado = 2; idBaixo = 2; break; 
						case 3: idTopo = 3; idLado = 3; idBaixo = 3; break; 
                        default: continue; 
					}
                    // adiciona faces
					if(y == chunk.TAM_CHUNKY - 1 || !ehSolido(x, y + 1, z, chunk)) addFace(x,y,z,0, idTopo, vertsGeral, idcGeral, chunk);
					if(y == 0 || !ehSolido(x, y - 1, z, chunk)) addFace(x,y,z,1, idBaixo, vertsGeral, idcGeral, chunk);
					if(x == chunk.TAM_CHUNKX - 1 || !ehSolido(x + 1, y, z, chunk)) addFace(x,y,z,2, idLado, vertsGeral, idcGeral, chunk);
					if(x == 0 || !ehSolido(x - 1, y, z, chunk)) addFace(x,y,z,3, idLado, vertsGeral, idcGeral, chunk);
					if(z == chunk.TAM_CHUNKZ - 1 || !ehSolido(x, y, z + 1, chunk)) addFace(x,y,z,4, idLado, vertsGeral, idcGeral, chunk);
					if(z == 0 || !ehSolido(x, y, z - 1, chunk)) addFace(x,y,z,5, idLado, vertsGeral, idcGeral, chunk);
				}
			}
		}
		defMesh(chunk.mesh, vertsGeral, idcGeral);
	}
	
	public static void addFace(int x, int y, int z, int faceId, int atlasId, FloatArrayUtil verts, IntArrayUtil idc, Chunk chunk) {
		float tam = 1f;
		float X = x * tam;
		float Y = y * tam;
		float Z = z * tam;

		float[][] faceVertices = new float[4][3];
		float[][] uvPadrao = new float[4][2]; 

		switch(faceId) {
			case 0: // topo
				faceVertices[0] = new float[]{X+tam, Y+tam, Z}; faceVertices[1] = new float[]{X, Y+tam, Z}; faceVertices[2] = new float[]{X, Y+tam, Z+tam}; faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
				uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 1: // baixo
				faceVertices[0] = new float[]{X+tam, Y, Z+tam}; faceVertices[1] = new float[]{X, Y, Z+tam}; faceVertices[2] = new float[]{X, Y, Z}; faceVertices[3] = new float[]{X+tam, Y, Z};
				uvPadrao = new float[][]{{1,0},{0,0},{0,1},{1,1}};
				break;
			case 2: // +X
				faceVertices[0] = new float[]{X+tam, Y, Z+tam}; faceVertices[1] = new float[]{X+tam, Y, Z}; faceVertices[2] = new float[]{X+tam, Y+tam, Z}; faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
				uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 3: // -X
				faceVertices[0] = new float[]{X, Y, Z}; faceVertices[1] = new float[]{X, Y, Z+tam}; faceVertices[2] = new float[]{X, Y+tam, Z+tam}; faceVertices[3] = new float[]{X, Y+tam, Z};
				uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 4: // +Z
				faceVertices[0] = new float[]{X, Y+tam, Z+tam}; faceVertices[1] = new float[]{X, Y, Z+tam}; faceVertices[2] = new float[]{X+tam, Y, Z+tam}; faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
				uvPadrao = new float[][]{{0,0},{0,1},{1,1},{1,0}};
				break;
			case 5: // -Z
				faceVertices[0] = new float[]{X, Y, Z}; faceVertices[1] = new float[]{X, Y+tam, Z}; faceVertices[2] = new float[]{X+tam, Y+tam, Z}; faceVertices[3] = new float[]{X+tam, Y, Z};
				uvPadrao = new float[][]{{0,1},{0,0},{1,0},{1,1}};
				break;
		}

		float[] atlasCoords = Mundo.atlasUVs.get(atlasId);
		if(atlasCoords == null) return;

		float u_min = atlasCoords[0];
		float v_min = atlasCoords[1];
		float u_max = atlasCoords[2];
		float v_max = atlasCoords[3];

		int vertConta = verts.tam / 5;

		for(int i = 0; i < 4; i++) {
			float vx = faceVertices[i][0], vy = faceVertices[i][1], vz = faceVertices[i][2];

			float u = u_min + uvPadrao[i][0] * (u_max - u_min);
			float v = v_min + uvPadrao[i][1] * (v_max - v_min);

			verts.add(vx); verts.add(vy); verts.add(vz); 
			verts.add(u); verts.add(v);
		}
		idc.add(vertConta + 0); idc.add(vertConta + 1); idc.add(vertConta + 2);
		idc.add(vertConta + 2); idc.add(vertConta + 3); idc.add(vertConta + 0);
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
        if(x < 0 || x >= chunk.TAM_CHUNKX || y < 0 || y >= chunk.TAM_CHUNKY || z < 0 || z >= chunk.TAM_CHUNKZ) return false;
        return chunk.chunk[x][y][z] != 0;
    }
	/*
	
	public static void criarMeshGuloso(Chunk chunk, FloatArrayUtil verts, IntArrayUtil indices) {
		for (int direcao = 0; direcao < 6; direcao++) {
			processarDirecao(chunk, direcao, verts, indices);
		}
	}
	
	public static void criarMeshGuloso(Chunk chunk) {
		FloatArrayUtil verts = new FloatArrayUtil();
		IntArrayUtil indices = new IntArrayUtil();

		for (int direcao = 0; direcao < 6; direcao++) {
			processarDirecao(chunk, direcao, verts, indices);
		}

		defMesh(chunk.mesh, verts, indices);
	}

	private static void processarDirecao(Chunk chunk, int direcao, FloatArrayUtil verts, IntArrayUtil indices) {
		boolean[][][] visitado = new boolean[chunk.TAM_CHUNKX][chunk.TAM_CHUNKY][chunk.TAM_CHUNKZ];

		for (int x = 0; x < chunk.TAM_CHUNKX; x++) {
			for (int y = 0; y < chunk.TAM_CHUNKY; y++) {
				for (int z = 0; z < chunk.TAM_CHUNKZ; z++) {
					if (visitado[x][y][z] || !faceVisivel(chunk, x, y, z, direcao)) continue;

					byte tipo = chunk.chunk[x][y][z];
					int atlasId = getIdTextura(tipo, direcao);

					int largura = 1;
					while (x + largura < chunk.TAM_CHUNKX && 
						   !visitado[x + largura][y][z] && 
						   chunk.chunk[x + largura][y][z] == tipo &&
						   faceVisivel(chunk, x + largura, y, z, direcao)) {
						largura++;
					}

					int altura = 1;
					boolean podeEstender = true;
					while (y + altura < chunk.TAM_CHUNKY && podeEstender) {
						for (int w = 0; w < largura; w++) {
							if (y + altura >= chunk.TAM_CHUNKY ||
								visitado[x + w][y + altura][z] ||
								chunk.chunk[x + w][y + altura][z] != tipo ||
								!faceVisivel(chunk, x + w, y + altura, z, direcao)) {
								podeEstender = false;
								break;
							}
						}
						if (podeEstender) altura++;
					}

					criarFaceGrande(chunk, x, y, z, largura, altura, direcao, atlasId, verts, indices);

					for (int w = 0; w < largura; w++) {
						for (int h = 0; h < altura; h++) {
							visitado[x + w][y + h][z] = true;
						}
					}
				}
			}
		}
	}

	private static boolean faceVisivel(Chunk chunk, int x, int y, int z, int direcao) {
		if (chunk.chunk[x][y][z] == 0) return false;

		int nx = x, ny = y, nz = z;
		switch (direcao) {
			case 0: ny++; break;
			case 1: ny--; break;
			case 2: nx++; break;
			case 3: nx--; break;
			case 4: nz++; break;
			case 5: nz--; break;
		}

		return !ehSolido(nx, ny, nz, chunk);
	}

	private static int getIdTextura(byte bloco, int direcao) {
		switch(bloco) {
			case 1: return direcao == 0 ? 0 : (direcao == 1 ? 2 : 1);
			case 2: return 2;
			case 3: return 3;
			default: return 0;
		}
	}

	private static void criarFaceGrande(Chunk chunk, int x, int y, int z, int largura, int altura, int direcao, 
										int atlasId, FloatArrayUtil verts, IntArrayUtil indices) {
		float X = x, Y = y, Z = z;
		float X2 = x + largura, Y2 = y + altura, Z2 = z + 1;

		float[][] vertices = new float[4][3];
		float[] normal = new float[3];
		float[][] uvs = new float[4][2];

		switch(direcao) {
			case 0:
				vertices[0] = new float[]{X2, Y2, Z}; vertices[1] = new float[]{X, Y2, Z};
				vertices[2] = new float[]{X, Y2, Z2}; vertices[3] = new float[]{X2, Y2, Z2};
				normal = new float[]{0,1,0};
				uvs = new float[][]{{largura,altura},{0,altura},{0,0},{largura,0}};
				break;
			case 1:
				vertices[0] = new float[]{X2, Y, Z2}; vertices[1] = new float[]{X, Y, Z2};
				vertices[2] = new float[]{X, Y, Z}; vertices[3] = new float[]{X2, Y, Z};
				normal = new float[]{0,-1,0};
				uvs = new float[][]{{largura,0},{0,0},{0,altura},{largura,altura}};
				break;
			case 2:
				vertices[0] = new float[]{X2, Y, Z2}; vertices[1] = new float[]{X2, Y, Z};
				vertices[2] = new float[]{X2, Y2, Z}; vertices[3] = new float[]{X2, Y2, Z2};
				normal = new float[]{1,0,0};
				uvs = new float[][]{{altura,largura},{0,largura},{0,0},{altura,0}};
				break;
			case 3:
				vertices[0] = new float[]{X, Y, Z}; vertices[1] = new float[]{X, Y, Z2};
				vertices[2] = new float[]{X, Y2, Z2}; vertices[3] = new float[]{X, Y2, Z};
				normal = new float[]{-1,0,0};
				uvs = new float[][]{{altura,largura},{0,largura},{0,0},{altura,0}};
				break;
			case 4:
				vertices[0] = new float[]{X, Y2, Z2}; vertices[1] = new float[]{X, Y, Z2};
				vertices[2] = new float[]{X2, Y, Z2}; vertices[3] = new float[]{X2, Y2, Z2};
				normal = new float[]{0,0,1};
				uvs = new float[][]{{0,0},{0,altura},{largura,altura},{largura,0}};
				break;
			case 5:
				vertices[0] = new float[]{X, Y, Z}; vertices[1] = new float[]{X, Y2, Z};
				vertices[2] = new float[]{X2, Y2, Z}; vertices[3] = new float[]{X2, Y, Z};
				normal = new float[]{0,0,-1};
				uvs = new float[][]{{0,altura},{0,0},{largura,0},{largura,altura}};
				break;
		}

		float[] atlas = atlasUVs.get(atlasId);
		if (atlas == null) return;

		int vertBase = verts.tam / 10;
		float luzU = (x + largura/2f) / chunk.TAM_CHUNKX;
		float luzV = (z + 0.5f) / chunk.TAM_CHUNKZ;

		for (int i = 0; i < 4; i++) {
			verts.add(vertices[i][0]); verts.add(vertices[i][1]); verts.add(vertices[i][2]);
			verts.add(normal[0]); verts.add(normal[1]); verts.add(normal[2]);
			float u = atlas[0] + uvs[i][0] * (atlas[2] - atlas[0]) / (direcao < 2 ? largura : altura);
			float v = atlas[1] + uvs[i][1] * (atlas[3] - atlas[1]) / (direcao < 2 ? altura : largura);
			verts.add(u); verts.add(v);
			verts.add(luzU); verts.add(luzV);
		}

		indices.add(vertBase); indices.add(vertBase + 1); indices.add(vertBase + 2);
		indices.add(vertBase + 2); indices.add(vertBase + 3); indices.add(vertBase);
	}
	*/
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
