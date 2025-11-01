package com.minimine.cenas;

import com.badlogic.gdx.graphics.Mesh;

public class Chunk {
	// largura * altura * largura / 2 divisao = 8 blocos por byte
    public byte[] blocos = new byte[16*255*16/2]; // 16×256×16 / 4 = 16384
    public byte[] luz = new byte[16*255*16/4];
    public Mesh mesh;
    public int chunkX, chunkZ;
    public boolean att = false;
}
