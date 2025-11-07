package com.minimine.cenas;

import com.badlogic.gdx.graphics.Mesh;
import com.minimine.utils.ChunkUtil;
import java.util.Arrays;

public final class Chunk {
    public int bitsPorBloco; // 1..8 (definir ao criar o chunk)
    public int blocosPorInt; // = 32 / bitsPorBloco
    public int[] blocos;
    public byte[] luz = new byte[16*255*16/2];
    public Mesh mesh;
    public int x, z, maxIds = 8;
    public boolean att = false;
}
