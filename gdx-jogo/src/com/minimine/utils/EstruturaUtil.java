package com.minimine.utils;

import com.minimine.cenas.Chunk;
import com.minimine.cenas.Mundo;

public class EstruturaUtil {
	public static void gerarArvore(int x, int y, int z, Chunk chunk) {
		// tronco
		for(int i = 0; i < 5; i++) {
			if(dentroLimite(x, y + i, z)) {
				ChunkUtil.defBloco(x, y + i, z, (byte)6, chunk);
			}
		}
		// copa(duas camadas e topo)
		for(int dy = 4; dy <= 6; dy++) {
			int raio = dy == 4 ? 2 : (dy == 5 ? 1 : 0);
			for(int dx = -raio; dx <= raio; dx++) {
				for(int dz = -raio; dz <= raio; dz++) {
					if(Mat.abs(dx) + Mat.abs(dz) <= raio + 1) {
						int xx = x + dx;
						int yy = y + dy;
						int zz = z + dz;
						if(dentroLimite(xx, yy, zz)) {
							ChunkUtil.defBloco(xx, yy, zz, (byte)7, chunk);
						}
					}
				}
			}
		}
	}
	
	public static boolean dentroLimite(int x, int y, int z) {
		return x >= 0 && x < Mundo.TAM_CHUNK &&
			z >= 0 && z < Mundo.TAM_CHUNK &&
			y >= 0 && y < Mundo.Y_CHUNK;
	}
}
