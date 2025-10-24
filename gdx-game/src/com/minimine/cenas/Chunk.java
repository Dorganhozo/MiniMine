package com.minimine.cenas;

import com.badlogic.gdx.graphics.Mesh;

public class Chunk {
	public final byte[][][] chunk;
	public Mesh mesh;
	public int TAM_CHUNKX, TAM_CHUNKY, TAM_CHUNKZ;
	
	public Chunk(int TAM_CHUNKX, int TAM_CHUNKY, int TAM_CHUNKZ) {
		this.chunk = new byte[TAM_CHUNKX][TAM_CHUNKY][TAM_CHUNKZ];
		this.TAM_CHUNKX = TAM_CHUNKX;
		this.TAM_CHUNKY = TAM_CHUNKY;
		this.TAM_CHUNKZ = TAM_CHUNKZ;
	}
	
	public boolean ehSolido(int x, int y, int z) {
		if(x < 0 || x >= TAM_CHUNKX || y < 0 || y >= TAM_CHUNKY || z < 0 || z >= TAM_CHUNKZ) return false;
		return chunk[x][y][z] != 0;
	}
}
