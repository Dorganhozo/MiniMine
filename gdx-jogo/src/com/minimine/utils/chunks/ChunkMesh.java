package com.minimine.utils.chunks;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;
import com.minimine.utils.blocos.Bloco;
import com.badlogic.gdx.graphics.Mesh;

public class ChunkMesh {
	public static void attMesh(Chunk chunk, FloatArrayUtil vertsGeral, ShortArrayUtil idcGeral) {
		ChunkLuz.attLuz(chunk);

		Chunk chunkXP = null, chunkXN = null, chunkZP = null, chunkZN = null;
		Chave chave = null;

		synchronized(Mundo.chunks) {
			chave = new Chave(chunk.x + 1, chunk.z);
			chunkXP = Mundo.chunks.get(chave);

			chave = new Chave(chunk.x - 1, chunk.z);
			chunkXN = Mundo.chunks.get(chave);

			chave = new Chave(chunk.x, chunk.z + 1);
			chunkZP = Mundo.chunks.get(chave);

			chave = new Chave(chunk.x, chunk.z - 1);
			chunkZN = Mundo.chunks.get(chave);
		}
		for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
			for(int y = 0; y < Mundo.Y_CHUNK; y++) {
				for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
					int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
					if(blocoId == 0) continue;

					Bloco blocoTipo = Bloco.numIds.get(blocoId);
					if(blocoTipo == null) continue;

					ChunkOtimiza.lidarFacesDoBloco(x, y, z, blocoTipo,
									  chunk, chunkXP, chunkXN, chunkZP, chunkZN,
									  vertsGeral, idcGeral);
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
}
