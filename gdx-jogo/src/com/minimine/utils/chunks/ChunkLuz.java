package com.minimine.utils.chunks;

import com.minimine.cenas.Mundo;
import com.minimine.utils.DiaNoiteUtil;

public class ChunkLuz {
    public static float LUZ_AMBIENTE = 0.25f;
    public static final float[] FACE_LUZ = {
        1.0f, // topo, luz max
        0.4f, // baixo, mais escuro
        0.7f, // lado +X
        0.7f, // lado -X  
        0.8f, // lado +Z
        0.8f  // lado -Z
    };
	
	public static void attLuz(Chunk chunk) {
        for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
            for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
                boolean bloqueado = false;
                for(int y = Mundo.Y_CHUNK - 1; y >= 0; y--) {
                    if(!bloqueado) {
                        if(ChunkUtil.ehSolido(x, y, z, chunk)) {
                            bloqueado = true;
                            defLuz(x, y, z, (byte)10, chunk);
                        } else {
                            defLuz(x, y, z, (byte)15, chunk);
                        }
                    } else {
                        defLuz(x, y, z, (byte)2, chunk);
                    }
                }
            }
        }
    }
	
    public static float calcularNivelLuz(int x, int y, int z, int faceId, Chunk chunk) {
        float luzCeu = calcularLuzCeu(x, y, z, chunk);
        float luzFinal = Math.max(LUZ_AMBIENTE, luzCeu);

        luzFinal *= FACE_LUZ[faceId];

        return Math.max(0.1f, Math.min(1.0f, luzFinal));
    }

    public static float calcularLuzCeu(int x, int y, int z, Chunk chunk) {
        if(y >= Mundo.Y_CHUNK - 1)  return DiaNoiteUtil.luz;
        int blocosAcima = 0;
        for(int cy = y + 1; cy < Mundo.Y_CHUNK; cy++) {
            if(ChunkUtil.ehSolido(x, cy, z, chunk)) blocosAcima++;
        }
        if(blocosAcima == 0) return DiaNoiteUtil.luz;
        float atenuacao = 1.0f - (blocosAcima * 0.15f);
        return Math.max(LUZ_AMBIENTE, DiaNoiteUtil.luz * atenuacao);
    }
	
	public static byte obterLuz(int x, int y, int z, Chunk chunk) {
        int i = x + (z * 16) + (y * 16 * 16);
		int byteIdc = i / 2;
		int bitPos = (i % 2) * 4;
        return (byte)((chunk.luz[byteIdc] >> bitPos) & 0b1111);
    }

    public static void defLuz(int x, int y, int z, byte valor, Chunk chunk) {
		int i = x + (z * 16) + (y * 16 * 16);
		int byteIdc = i / 2;
		int bitPos = (i % 2) * 4;
		byte mascara = (byte) ~(0b1111 << bitPos);
		chunk.luz[byteIdc] = (byte)((chunk.luz[byteIdc] & mascara) | ((valor & 0b1111) << bitPos));
    }
}
