package com.minimine.utils.chunks;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;
import com.minimine.utils.blocos.Bloco;
import com.badlogic.gdx.graphics.Mesh;

public class ChunkMesh {
	public static Chave c = new Chave(0, 0);
	
	public static void attMesh(Chunk chunk, FloatArrayUtil vertsGeral, ShortArrayUtil idcGeral) {
		ChunkLuz.attLuz(chunk);
		
		Chunk chunkXP, chunkXN, chunkZP, chunkZN;
		c.x = chunk.x + 1; c.z = chunk.z;
		chunkXP = Mundo.chunks.get(c);
		c.x = chunk.x - 1; c.z = chunk.z;
		chunkXN = Mundo.chunks.get(c);
		c.x = chunk.x; c.z = chunk.z + 1;
		chunkZP = Mundo.chunks.get(c);
		c.x = chunk.x; c.z = chunk.z - 1;
		chunkZN = Mundo.chunks.get(c);

		for(int idc = 0; idc < Mundo.CHUNK_AREA * Mundo.Y_CHUNK; idc++) {
			int x = idc & 0xF;
			int z = (idc >> 4) & 0xF;
			int y = idc >> 8;

			int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
			if(blocoId == 0) continue;

			Bloco blocoTipo = Bloco.numIds.get(blocoId);
			if(blocoTipo == null) continue;

			ChunkOtimiza.lidarFacesDoBloco(
				x, y, z, blocoTipo,
				chunk, chunkXP, chunkXN, chunkZP, chunkZN,
				vertsGeral, idcGeral);
		}
	}
}
