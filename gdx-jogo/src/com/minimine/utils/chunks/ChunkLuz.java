package com.minimine.utils.chunks;

import com.minimine.cenas.Mundo;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.blocos.Bloco;

public class ChunkLuz {
    public static float LUZ_AMBIENTE = 0.25f;
    public static final float[] FACE_LUZ = {1.0f, 0.4f, 0.7f, 0.7f, 0.8f, 0.8f};
    public static final int Y_MAX = Mundo.Y_CHUNK - 1;
    public static final int DENSIDADE_SOLIDO = 2;
    public static final int[] POS_X = {1, -1, 0, 0, 0, 0};
    public static final int[] POS_Y = {0, 0, 1, -1, 0, 0};
    public static final int[] POS_Z = {0, 0, 0, 0, 1, -1};

    public static void attLuz(Chunk chunk) {
        // se a luz do ceu não mudou desde a ultima atualização dessa chunk
        // e não ha blocos que emitem luz que foram alterados
        // pode pular o recalculo completo
        if(!chunk.luzSuja) {
            return; // luz ja ta atualizada
        }
        // atualiza o cache dessa chunk
        chunk.luzSuja = false;
        // recalcula tudo
        attLuzCompleta(chunk);
    }

    public static void attLuzCompleta(Chunk chunk) {
        final int area = Mundo.CHUNK_AREA;
        final int altura = Mundo.Y_CHUNK;
        final int totalBlocos = area * altura;

        final byte[] luzTemp = new byte[totalBlocos];
        int[] filaLuz = new int[totalBlocos * 2];
        int inicioFila = 0;
        int fimFila = 0;
        // passo 1: fontes de luz
        for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
			for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
				for(int y = Y_MAX; y >= 0; y--) {
					int idc = x + (z * Mundo.TAM_CHUNK) + (y * area);
					int luzFinal = 0;

					int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
					if(blocoId > 0) {
						Bloco b = Bloco.numIds.get(blocoId);
						if(b != null && b.luz > 0) {
							luzFinal = b.luz;
						}
					}
					// preenche apenas com luz de blocos
					luzTemp[idc] = (byte) luzFinal;
					if(luzFinal > 0) filaLuz[fimFila++] = idc;
				}
			}
		}
        // passo 2: espalhamento
        while(inicioFila < fimFila) {
            int idcAtual = filaLuz[inicioFila++];
            byte valorLuz = luzTemp[idcAtual];

            if(valorLuz <= 1) continue;

            int cy = idcAtual / area;
            int resto = idcAtual % area;
            int cz = resto >> 4;
            int cx = resto & 0xF;

            for(int i = 0; i < 6; i++) {
                int nx = cx + POS_X[i];
                int ny = cy + POS_Y[i];
                int nz = cz + POS_Z[i];

                if(nx >= 0 && nx < Mundo.TAM_CHUNK &&
				   ny >= 0 && ny < Mundo.Y_CHUNK &&
				   nz >= 0 && nz < Mundo.TAM_CHUNK) {

                    int idcVizinho = nx + (nz * Mundo.TAM_CHUNK) + (ny * area);
                    byte novaLuz = (byte)(valorLuz - 1);

                    if(luzTemp[idcVizinho] < novaLuz) {
                        luzTemp[idcVizinho] = novaLuz;
                        filaLuz[fimFila++] = idcVizinho;
                    }
                }
            }
        }
        // passo 3: salva na chunk
        for(int i = 0; i < totalBlocos; i++) {
            defLuzPorIndice(i, luzTemp[i], chunk);
        }
    }
	
    public static byte obterLuzPorIndice(int idc, Chunk chunk) {
        return (byte)((chunk.luz[idc >> 1] >> ((idc & 1) << 2)) & 15);
    }

    public static void defLuzPorIndice(int idc, byte valor, Chunk chunk) {
        int byteIdc = idc >> 1;
        int shift = (idc & 1) << 2;
        chunk.luz[byteIdc] = (byte)(
            (chunk.luz[byteIdc] & ~(15 << shift)) | 
            ((valor & 15) << shift));
    }

    public static float calcularNivelLuz(int x, int y, int z, int idFace, Chunk chunk) {
        byte luzVal = ChunkUtil.obterLuz(x, y, z, chunk);
        float c = (luzVal & 0xFF) / 15.0f;
        if(c < LUZ_AMBIENTE) c = LUZ_AMBIENTE;
        float f = c * FACE_LUZ[idFace];
        return Math.min(Math.max(f, 0.1f), 1.0f);
    }
}
