package com.minimine.utils.chunks;

import com.minimine.cenas.Mundo;
import com.minimine.utils.DiaNoiteUtil;

public class ChunkLuz {
    public static float LUZ_AMBIENTE = 0.25f;
    public static float[] FACE_LUZ = {1.0f, 0.4f, 0.7f, 0.7f, 0.8f, 0.8f};
    public static final int Y_MAX = Mundo.Y_CHUNK - 1;

    public static void attLuz(Chunk chunk) {
        byte[] luzBytes = chunk.luz;
        int area = Mundo.CHUNK_AREA; // local para JIT
        int yMax = Y_MAX; // local para JIT

        for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
            int zArea = z << 4;

            for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
                int xzIdc = x + zArea;
                // encontra topo(comeca do ultimo solido conhecido se existir)
                int topo = -1;
                for(int y = yMax; y >= 0; y--) {
                    if(ChunkUtil.ehSolido(x, y, z, chunk)) {
                        topo = y;
                        break;
                    }
                }
                // processa toda a coluna y de uma vez
                int byteIdc = xzIdc >> 1;
                int bitShift = (xzIdc & 1) << 2;

                for(int y = yMax; y >= 0; y--) {
                    byte valorLuz;

                    if(topo == -1) valorLuz = 15;
                    else if(y > topo) valorLuz = 15;
                    else if(y == topo) valorLuz = 10;
                    else valorLuz = 2;

                    int idc = byteIdc + (y * (area >> 1));
					
                    luzBytes[idc] = (byte)(
                        (luzBytes[idc] & ~(15 << bitShift)) | 
                        ((valorLuz & 15) << bitShift)
						);
                }
            }
        }
    }

    public static float calcularNivelLuz(int x, int y, int z, int idFace, Chunk chunk) {
        float c = calcularLuzCeu(x, y, z, chunk);
        if(c < LUZ_AMBIENTE) c = LUZ_AMBIENTE;

        float f = c * FACE_LUZ[idFace];

        return Math.min(Math.max(f, 0.1f), 1.0f);
    }

    public static float calcularLuzCeu(int x, int y, int z, Chunk chunk) {
        if(y >= Y_MAX) return DiaNoiteUtil.luz;

        int bloqueios = 0;
        // loop invertido e pre verificado
        for(int cy = Y_MAX; cy > y; cy--) {
            if(ChunkUtil.ehSolido(x, cy, z, chunk)) {
                bloqueios++;
                // se ja tem muitos blocos, retorna cedo
                if(bloqueios >= 7) return LUZ_AMBIENTE;
            }
        }
        if(bloqueios == 0) return DiaNoiteUtil.luz;
		
        float atenuacao = 1.0f - (bloqueios * 0.15f);
        float resultado = DiaNoiteUtil.luz * atenuacao;

        return resultado > LUZ_AMBIENTE ? resultado : LUZ_AMBIENTE;
    }

    public static byte obterLuz(int x, int y, int z, Chunk chunk) {
        int idc = x + (z * Mundo.TAM_CHUNK) + (y * Mundo.CHUNK_AREA);
        return (byte)((chunk.luz[idc >> 1] >> ((idc & 1) << 2)) & 15);
    }

    public static void defLuz(int x, int y, int z, byte valor, Chunk chunk) {
        int idc = x + (z * Mundo.TAM_CHUNK) + (y * Mundo.CHUNK_AREA);
        int byteIdc = idc >> 1;
        int shift = (idc & 1) << 2;
        chunk.luz[byteIdc] = (byte)(
            (chunk.luz[byteIdc] & ~(15 << shift)) | 
            ((valor & 15) << shift)
			);
    }
}
