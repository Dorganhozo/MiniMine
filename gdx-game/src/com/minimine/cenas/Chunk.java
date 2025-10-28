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
    public int chunkX = 0;
    public int chunkZ = 0;
	public boolean att = true;
}
