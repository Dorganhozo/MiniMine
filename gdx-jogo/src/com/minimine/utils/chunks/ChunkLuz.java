package com.minimine.utils.chunks;

import com.minimine.cenas.Mundo;
import com.minimine.utils.blocos.Bloco;

public class ChunkLuz {
    public static float LUZ_AMBIENTE = 0.25f;
    public static final float[] FACE_LUZ = {1.0f, 0.4f, 0.7f, 0.7f, 0.8f, 0.8f};
    public static final int Y_MAX = Mundo.Y_CHUNK - 1;
    public static final int[] POS_X = {1, -1, 0, 0, 0, 0};
    public static final int[] POS_Y = {0, 0, 1, -1, 0, 0};
    public static final int[] POS_Z = {0, 0, 0, 0, 1, -1};

    public static void attLuz(Chunk chunk) {
        if(!chunk.luzSuja) return;
        chunk.luzSuja = false;
        attLuzCompleta(chunk);
    }

    public static void attLuzCompleta(Chunk chunk) {
		final int area = Mundo.CHUNK_AREA;
		final int totalBlocos = area * Mundo.Y_CHUNK;

		final byte[] luzTemp = new byte[totalBlocos];
		int[] filaLuz = new int[totalBlocos * 2];
		int inicioFila = 0;
		int fimFila = 0;

		// passo 1: luz solar
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				int luzSolarAtual = 15;
				for(int y = Y_MAX; y >= 0; y--) {
					int idc = x + (z << 4) + (y * area);
					int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
					Bloco b = Bloco.numIds.get(blocoId);

					// se não for ar e não for transparente, o sol pra aqui
					if(b != null && !b.transparente) luzSolarAtual = 0;

					// mascaras pra garantir que o byte não corrompa os bits
					luzTemp[idc] = (byte) ((luzSolarAtual << 4) & 0xF0);

					if(luzSolarAtual > 0) filaLuz[fimFila++] = idc;

					// pasao 2: fontes de luz
					if(b != null && b.luz > 0) {
						// mescla preservando os bits do sol
						int luzAtual = (luzTemp[idc] & 0xFF);
						luzTemp[idc] = (byte) (luzAtual | (b.luz & 0x0F));

						// se for tocha no escuro, entra na fila
						if(luzSolarAtual <= 0) filaLuz[fimFila++] = idc;
					}
				}
			}
		}
		// passo 3: espalhamento de luz
		while(inicioFila < fimFila) {
			int idcAtual = filaLuz[inicioFila++];
			// & 0xFF transforma o byte "assinado" em int "limpo"(0-255)
			int luzTotal = luzTemp[idcAtual] & 0xFF; 

			int lb = luzTotal & 0x0F; // brilho
			int ls = (luzTotal >> 4) & 0x0F; // Sol

			int cy = idcAtual / area;
			int resto = idcAtual % area;
			int cz = resto >> 4;
			int cx = resto & 0xF;

			for(int i = 0; i < 6; i++) {
				int nx = cx + POS_X[i];
				int ny = cy + POS_Y[i];
				int nz = cz + POS_Z[i];

				if(nx >= 0 && nx < 16 && ny >= 0 && ny < Mundo.Y_CHUNK && nz >= 0 && nz < 16) {
					int idcVizinho = nx + (nz << 4) + (ny * area);

					// pega a luz do vizinho(usando & 0xFF pra não dar erro de sinal)
					int luzVizinha = luzTemp[idcVizinho] & 0xFF;
					int lbV = luzVizinha & 0x0F;
					int lsV = (luzVizinha >> 4) & 0x0F;

					boolean mudou = false;

					// se o vizinho recebe luz mais forte do que ja tem
					if(lbV < lb - 1 && lb > 1) {
						lbV = lb - 1;
						mudou = true;
					}
					if(lsV < ls - 1 && ls > 1) {
						lsV = ls - 1;
						mudou = true;
					}
					if(mudou) {
						// grava a nova luz no vizinho
						luzTemp[idcVizinho] = (byte) (((lsV << 4) & 0xF0) | (lbV & 0x0F));

						// a luz so continua se espalhando se o vizinho for transparente
						int blocoIdV = ChunkUtil.obterBloco(nx, ny, nz, chunk);
						Bloco bV = Bloco.numIds.get(blocoIdV);

						if(bV == null || bV.transparente) filaLuz[fimFila++] = idcVizinho;
					}
				}
			}
		}
		System.arraycopy(luzTemp, 0, chunk.luz, 0, totalBlocos);
	}
    
    public static float[] obterLuzesNormais(int x, int y, int z, Chunk chunk) {
        byte luzTotal = ChunkUtil.obterLuzCompleta(x, y, z, chunk);
        return new float[] {
            (luzTotal & 0x0F) / 15f, // luz bloco(0.0 a 1.0)
            ((luzTotal >> 4) & 0x0F) / 15f // luz Sol(0.0 a 1.0)
        };
    }
}
