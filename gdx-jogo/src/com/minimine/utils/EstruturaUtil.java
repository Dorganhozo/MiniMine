package com.minimine.utils;

import com.minimine.cenas.Chunk;

public class EstruturaUtil {
	public static void gerarArvore(int x, int y, int z, Chunk chunk) {
		// tronco:
		ChunkUtil.defBloco(x, y, z, (byte)6, chunk);
		ChunkUtil.defBloco(x, y+1, z, (byte)6, chunk);
		ChunkUtil.defBloco(x, y+2, z, (byte)6, chunk);
		ChunkUtil.defBloco(x, y+3, z, (byte)6, chunk);
		ChunkUtil.defBloco(x, y+4, z, (byte)6, chunk);
		// folhas:
		ChunkUtil.defBloco(x+1, y+4, z, (byte)7, chunk);
		ChunkUtil.defBloco(x+1, y+4, z+1, (byte)7, chunk);
		ChunkUtil.defBloco(x-1, y+4, z, (byte)7, chunk);
		ChunkUtil.defBloco(x-1, y+4, z-1, (byte)7, chunk);
		ChunkUtil.defBloco(x, y, z+4+1, (byte)7, chunk);
		ChunkUtil.defBloco(x, y+4, z-1, (byte)7, chunk);
		// folhas de lado:
		ChunkUtil.defBloco(x+2, y+4, z, (byte)7, chunk);
		ChunkUtil.defBloco(x+2, y+4, z+2, (byte)7, chunk);
		ChunkUtil.defBloco(x-2, y+4, z, (byte)7, chunk);
		ChunkUtil.defBloco(x-2, y+4, z-2, (byte)7, chunk);
		ChunkUtil.defBloco(x, y+4, z+2, (byte)7, chunk);
		ChunkUtil.defBloco(x, y+4, z-2, (byte)7, chunk);
		// folhas de cima
		ChunkUtil.defBloco(x+1, y+5, z, (byte)7, chunk);
		ChunkUtil.defBloco(x+1, y+5, z+1, (byte)7, chunk);
		ChunkUtil.defBloco(x-1, y+5, z, (byte)7, chunk);
		ChunkUtil.defBloco(x-1, y+5, z-1, (byte)7, chunk);
		ChunkUtil.defBloco(x, y+5, z+1, (byte)7, chunk);
		ChunkUtil.defBloco(x, y+5, z-1, (byte)7, chunk);
	}
}
