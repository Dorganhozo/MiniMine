package com.minimine.utils;

import com.minimine.utils.FloatArrayUtil;
import com.minimine.utils.IntArrayUtil;
import com.badlogic.gdx.graphics.Mesh;
import java.util.Map;
import java.util.HashMap;
import com.minimine.cenas.Chunk;

public class ChunkUtil {
	public static Map<Integer, float[]> atlasUVs = new HashMap<>();

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
					if(y == chunk.TAM_CHUNKY - 1 || !chunk.ehSolido(x, y + 1, z)) addFace(x,y,z,0, idTopo, vertsGeral, idcGeral, chunk);
					if(y == 0 || !chunk.ehSolido(x, y - 1, z)) addFace(x,y,z,1, idBaixo, vertsGeral, idcGeral, chunk);
					if(x == chunk.TAM_CHUNKX - 1 || !chunk.ehSolido(x + 1, y, z)) addFace(x,y,z,2, idLado, vertsGeral, idcGeral, chunk);
					if(x == 0 || !chunk.ehSolido(x - 1, y, z)) addFace(x,y,z,3, idLado, vertsGeral, idcGeral, chunk);
					if(z == chunk.TAM_CHUNKZ - 1 || !chunk.ehSolido(x, y, z + 1)) addFace(x,y,z,4, idLado, vertsGeral, idcGeral, chunk);
					if(z == 0 || !chunk.ehSolido(x, y, z - 1)) addFace(x,y,z,5, idLado, vertsGeral, idcGeral, chunk);
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
		float[] normal = new float[3];
		float[][] uvPadrao = new float[4][2]; 

		switch(faceId) {
			case 0: // topo
				faceVertices[0] = new float[]{X+tam, Y+tam, Z}; faceVertices[1] = new float[]{X, Y+tam, Z}; faceVertices[2] = new float[]{X, Y+tam, Z+tam}; faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
				normal = new float[]{0,1,0};
				uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 1: // baixo
				faceVertices[0] = new float[]{X+tam, Y, Z+tam}; faceVertices[1] = new float[]{X, Y, Z+tam}; faceVertices[2] = new float[]{X, Y, Z}; faceVertices[3] = new float[]{X+tam, Y, Z};
				normal = new float[]{0,-1,0};
				uvPadrao = new float[][]{{1,0},{0,0},{0,1},{1,1}};
				break;
			case 2: // +X
				faceVertices[0] = new float[]{X+tam, Y, Z+tam}; faceVertices[1] = new float[]{X+tam, Y, Z}; faceVertices[2] = new float[]{X+tam, Y+tam, Z}; faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
				normal = new float[]{1,0,0};
				uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 3: // -X
				faceVertices[0] = new float[]{X, Y, Z}; faceVertices[1] = new float[]{X, Y, Z+tam}; faceVertices[2] = new float[]{X, Y+tam, Z+tam}; faceVertices[3] = new float[]{X, Y+tam, Z};
				normal = new float[]{-1,0,0};
				uvPadrao = new float[][]{{1,1},{0,1},{0,0},{1,0}};
				break;
			case 4: // +Z
				faceVertices[0] = new float[]{X, Y+tam, Z+tam}; faceVertices[1] = new float[]{X, Y, Z+tam}; faceVertices[2] = new float[]{X+tam, Y, Z+tam}; faceVertices[3] = new float[]{X+tam, Y+tam, Z+tam};
				normal = new float[]{0,0,1};
				uvPadrao = new float[][]{{0,0},{0,1},{1,1},{1,0}};
				break;
			case 5: // -Z
				faceVertices[0] = new float[]{X, Y, Z}; faceVertices[1] = new float[]{X, Y+tam, Z}; faceVertices[2] = new float[]{X+tam, Y+tam, Z}; faceVertices[3] = new float[]{X+tam, Y, Z};
				normal = new float[]{0,0,-1};
				uvPadrao = new float[][]{{0,1},{0,0},{1,0},{1,1}};
				break;
		}
        // mapeamento:
        float[] atlasCoords = atlasUVs.get(atlasId);

        if(atlasCoords == null) return;

        float u_min = atlasCoords[0];
        float v_min = atlasCoords[1];
        float u_max = atlasCoords[2];
        float v_max = atlasCoords[3];

        int vertConta = verts.tam / 10; 

		float uv_luz_u = (x + 0.5f) / chunk.TAM_CHUNKX;
		float uv_luz_v = (z + 0.5f) / chunk.TAM_CHUNKZ;

		for(int i=0;i<4;i++) {
			float vx=faceVertices[i][0], vy=faceVertices[i][1], vz=faceVertices[i][2];
			float[] n = normal;

            float u = u_min + uvPadrao[i][0] * (u_max - u_min);
            float v = v_min + uvPadrao[i][1] * (v_max - v_min);

			verts.add(vx); verts.add(vy); verts.add(vz); 
			verts.add(n[0]); verts.add(n[1]); verts.add(n[2]); 
			verts.add(u); verts.add(v); 
			verts.add(uv_luz_u); verts.add(uv_luz_v);
		}
        // adição de indices
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
}
