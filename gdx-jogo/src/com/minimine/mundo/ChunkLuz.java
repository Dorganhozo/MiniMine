package com.minimine.mundo;

import com.minimine.mundo.blocos.Bloco;

public class ChunkLuz {
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

		// reutiliza arrays
		final byte[] luzTemp = new byte[totalBlocos];
		int[] filaLuz = new int[totalBlocos]; 
		int inicioFila = 0;
		int fimFila = 0;

		// 1. otimização de varredura
		// inverte a ordem pra Y ser o laço interno mais rapido melhora o acesso a memória
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				int luzSolarAtual = 15;
				int posXZ = x + (z << 4);

				for(int y = Y_MAX; y >= 0; y--) {
					int idc = posXZ + (y << 8);

					int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
					Bloco b = Bloco.numIds.get(blocoId);

					if(b != null && !b.transparente) luzSolarAtual = 0;

					luzTemp[idc] = (byte) (luzSolarAtual << 4);

					if(luzSolarAtual > 0) {
						filaLuz[fimFila++] = idc;
					}
					if(b != null && b.luz > 0) {
						luzTemp[idc] |= (byte) (b.luz & 0x0F);
						// so adiciona se o sol ja não preencheu esse lugar
						if(luzSolarAtual <= 0) filaLuz[fimFila++] = idc;
					}
				}
			}
		}
		// 2. espalhamento com extração de coordenadas
		while(inicioFila < fimFila) {
			int idcAtual = filaLuz[inicioFila++];
			int luzTotal = luzTemp[idcAtual] & 0xFF;

			// extração de X, Z, Y sem usar divisão (/) ou resto (%)
			int cx = idcAtual & 0xF;
			int cz = (idcAtual >> 4) & 0xF;
			int cy = idcAtual >> 8;

			int lb = luzTotal & 0x0F;
			int ls = luzTotal >> 4;

			for(int i = 0; i < 6; i++) {
				int nx = cx + POS_X[i];
				int ny = cy + POS_Y[i];
				int nz = cz + POS_Z[i];

				if(nx >= 0 && nx < 16 && ny >= 0 && ny < Mundo.Y_CHUNK && nz >= 0 && nz < 16) {
					int idcVizinho = nx + (nz << 4) + (ny << 8);
					int luzVizinha = luzTemp[idcVizinho] & 0xFF;

					int lbV = luzVizinha & 0x0F;
					int lsV = luzVizinha >> 4;

					boolean mudou = false;
					if(lbV < lb - 1 && lb > 0) { lbV = lb - 1; mudou = true; }
					if(lsV < ls - 1 && ls > 0) { lsV = ls - 1; mudou = true; }

					if(mudou) {
						luzTemp[idcVizinho] = (byte) ((lsV << 4) | lbV);

						int blocoIdV = ChunkUtil.obterBloco(nx, ny, nz, chunk);
						Bloco bV = Bloco.numIds.get(blocoIdV);

						if(bV == null || bV.transparente) filaLuz[fimFila++] = idcVizinho;
					}
				}
			}
		}
		System.arraycopy(luzTemp, 0, chunk.luz, 0, totalBlocos);
	}
}
