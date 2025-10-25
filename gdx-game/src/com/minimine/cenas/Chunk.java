package com.minimine.cenas;

import com.badlogic.gdx.graphics.Mesh;
import com.minimine.cenas.blocos.Luz;
import java.util.ArrayList;
import java.util.List;

public class Chunk {
	public byte[][][] chunk = new byte[Mundo.TAM_CHUNK][Mundo.Y_CHUNK][Mundo.TAM_CHUNK];
	public Mesh mesh;
	public int TAM_CHUNKX = Mundo.TAM_CHUNK, TAM_CHUNKY = Mundo.Y_CHUNK, TAM_CHUNKZ = Mundo.TAM_CHUNK;
	public List<Luz> luzes = new ArrayList<>();
	
	public Chunk() {}
	
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
	
	public void addLuz(Luz luz) {
		if(luzes != null) luzes.add(luz);
	}
	
	public void attLuz() {
		LuzUtil.att(chunk, luzes);
	}
}
