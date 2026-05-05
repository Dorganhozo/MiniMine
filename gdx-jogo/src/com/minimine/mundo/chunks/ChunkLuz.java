package com.minimine.mundo.chunks;

import com.minimine.mundo.blocos.Bloco;
import com.minimine.graficos.TipoRender;
import java.util.Arrays;
import com.minimine.mundo.Mundo;

public class ChunkLuz implements GeradorLuz {
    public static final int Y_MAX = Mundo.Y_CHUNK - 1;
    public static final int[] POS_X = {1, -1, 0, 0, 0, 0};
    public static final int[] POS_Y = {0, 0, 1, -1, 0, 0};
    public static final int[] POS_Z = {0, 0, 0, 0, 1, -1};

    public static final int TOTAL_BLOCOS = 16 * Mundo.Y_CHUNK * 16;

    // reuso de arrays por thread, sem alocação e sem GC por chunk
    public static final ThreadLocal<byte[]> LUZ_TEMP_REUSO = new ThreadLocal<byte[]>() {
        @Override protected byte[] initialValue() { return new byte[TOTAL_BLOCOS]; }
    };
    public static final ThreadLocal<int[]> FILA_LUZ_REUSO = new ThreadLocal<int[]>() {
        @Override protected int[] initialValue() { return new int[TOTAL_BLOCOS * 6]; }
    };

    @Override
    public void calcularLuz(Chunk chunk) {
        execProcesso(chunk, true);
    }

    @Override
    public void attLuz(Chunk chunk) {
        if(!chunk.luzSuja) return;
        chunk.luzSuja = false;
        execProcesso(chunk, false);
    }

    @Override
    public void recalcularLuz(Chunk chunk) {
        zerarLuzBlocoChunk(chunk);

        final Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        final Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        final Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        final Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);
        final Chunk chunkNE = obterChunk(chunk.x + 1, chunk.z - 1);
        final Chunk chunkNO = obterChunk(chunk.x - 1, chunk.z - 1);
        final Chunk chunkSE = obterChunk(chunk.x + 1, chunk.z + 1);
        final Chunk chunkSO = obterChunk(chunk.x - 1, chunk.z + 1);

        final Chunk[] vizinhas = {
			chunkNorte, chunkSul, chunkLeste, chunkOeste,
			chunkNE, chunkNO, chunkSE, chunkSO
		};
        for(Chunk v : vizinhas) {
            if(v != null) zerarLuzBlocoChunk(v);
        }
        chunk.luzSuja = true;
        chunk.att = true;
        for(Chunk v : vizinhas) {
            if(v != null) {
				v.luzSuja = true;
				v.att = true;
			}
        }
    }

    // processo principal
    public static void execProcesso(Chunk chunk, boolean soVizinhasProntas) {
        chunk.luzFazendo = true;

        final byte[] luzTemp = LUZ_TEMP_REUSO.get();
        final int[] filaLuz = FILA_LUZ_REUSO.get();
        Arrays.fill(luzTemp, (byte) 0);
        int inicioFila = 0;
        int fimFila = 0;

        // 1. inicia luz solar e fontes de luz de bloco
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int luzSolarAtual = 15;
                final int posXZ = x + (z << 4);

                for(int y = Y_MAX; y >= 0; y--) {
                    final int idc = posXZ + (y << 8);
                    final int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
                    final Bloco b = Bloco.numIds.get(blocoId);

                    if(b != null && b.render == TipoRender.OPACO) luzSolarAtual = 0;

                    luzTemp[idc] = (byte) (luzSolarAtual << 4);

                    if(luzSolarAtual > 0) filaLuz[fimFila++] = idc;
                    
                    if(b != null && b.luz > 0) {
                        luzTemp[idc] |= (byte) (b.luz & 0x0F);
						
                        if(luzSolarAtual <= 0) filaLuz[fimFila++] = idc;
                    }
                }
            }
        }
        // 2. importa luz das chunks vizinhas
        fimFila = importarLuzVizinhas(chunk, luzTemp, filaLuz, fimFila, soVizinhasProntas);

        // 3. propagação BFS dentro da chunk
        fimFila = propagarBFS(chunk, luzTemp, filaLuz, inicioFila, fimFila);

        // 4. copia resultado pro array definitivo da chunk
        System.arraycopy(luzTemp, 0, chunk.luz, 0, TOTAL_BLOCOS);

        chunk.luzSuja = false;
        chunk.luzFazendo = false;
    }
	
    // BFS de propagação de luz
    public static int propagarBFS(Chunk chunk, byte[] luzTemp, int[] filaLuz,
	int inicioFila, int fimFila) {
        while (inicioFila < fimFila) {
            final int idcAtual = filaLuz[inicioFila++];
            final int luzTotal = luzTemp[idcAtual] & 0xFF;

            final int cx = idcAtual & 0xF;
            final int cz = (idcAtual >> 4) & 0xF;
            final int cy = idcAtual >> 8;

            final int lb = luzTotal & 0x0F;
            final int ls = luzTotal >> 4;

            for(int i = 0; i < 6; i++) {
                final int nx = cx + POS_X[i];
                final int ny = cy + POS_Y[i];
                final int nz = cz + POS_Z[i];

                if(nx < 0 || nx >= 16 || ny < 0 || ny >= Mundo.Y_CHUNK || nz < 0 || nz >= 16) continue;

                final int idcVizinho = nx + (nz << 4) + (ny << 8);
                final int luzVizinha = luzTemp[idcVizinho] & 0xFF;

                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                boolean mudou = false;
                if(lb > 0 && lbV < lb - 1) {
					lbV = lb - 1;
					mudou = true;
				}
                if(ls > 0 && lsV < ls - 1) {
					lsV = ls - 1;
					mudou = true;
				}
                if(mudou) {
                    luzTemp[idcVizinho] = (byte) ((lsV << 4) | lbV);

                    final Bloco bV = Bloco.numIds.get(ChunkUtil.obterBloco(nx, ny, nz, chunk));
                    if((bV == null || bV.render != TipoRender.OPACO) && fimFila < filaLuz.length) {
                        filaLuz[fimFila++] = idcVizinho;
                    }
                }
            }
        }
        return fimFila;
    }
    /*
     * importa a luz das 4 chunks vizinhas cardinais para as bordas desta chunk
     * true -> so importa se a vizinha ja tem dadosProntos e não ta calculando(calcularLuz)
     * false -> importa mesmo que a vizinha ainda não esteja 100% pronta(attLuz)
    */
    public static int importarLuzVizinhas(Chunk chunk, byte[] luzTemp, int[] filaLuz,
	int fimFila, boolean apenasVizinhasProntas) {
        // norte: z-1, borda z=15 da vizinha -> z=0 da nossa
        final Chunk norte = filtrarVizinha(obterChunk(chunk.x, chunk.z - 1), apenasVizinhasProntas);
        // sul: z+1, borda z=0 da vizinha -> z=15 da nossa
        final Chunk sul = filtrarVizinha(obterChunk(chunk.x, chunk.z + 1), apenasVizinhasProntas);
        // leste: x+1, borda x=0 da vizinha -> x=15 da nossa
        final Chunk leste = filtrarVizinha(obterChunk(chunk.x + 1, chunk.z), apenasVizinhasProntas);
        // oeste: x-1, borda x=15 da vizinha -> x=0  da nossa
        final Chunk oeste = filtrarVizinha(obterChunk(chunk.x - 1, chunk.z), apenasVizinhasProntas);

        if(norte != null) fimFila = importarBorda(chunk, norte, luzTemp, filaLuz, fimFila, true, 15, 0);
        if(sul != null) fimFila = importarBorda(chunk, sul, luzTemp, filaLuz, fimFila, true, 0, 15);
        if(leste != null) fimFila = importarBorda(chunk, leste, luzTemp, filaLuz, fimFila, false, 0, 15);
        if(oeste != null) fimFila = importarBorda(chunk, oeste, luzTemp, filaLuz, fimFila, false, 15, 0);
        return fimFila;
    }

    /*
     * retorna a chunk vizinha se ela for valida para importação, ou null caso contrario
     * quando apenasVizinhasProntas=true, exige dadosProntos e que não esteja calculando luz
     */
    public static Chunk filtrarVizinha(Chunk vizinha, boolean apenasVizinhasProntas) {
        if(vizinha == null) return null;
        if(apenasVizinhasProntas && (!vizinha.dadosProntos || vizinha.luzFazendo)) return null;
        if(!apenasVizinhasProntas && (!vizinha.dadosProntos || vizinha.luzFazendo)) return null;
        return vizinha;
    }

    public static int importarBorda(Chunk chunk, Chunk vizinha, byte[] luzTemp, int[] filaLuz,
	int fimFila, boolean iteraX, int bordaViz, int bordaNossa) {
        for(int a = 0; a < 16; a++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                // calcula indices dependendo de qual eixo é o "livre"(a) e qual é fixo
                final int idcViz = iteraX
					? a + (bordaViz << 4) + (y << 8) // norte/sul: a=x, fixo=z
					: bordaViz + (a << 4) + (y << 8); // leste/oeste: a=z, fixo=x

                final int luzVizinha = vizinha.luz[idcViz] & 0xFF;
                final int lbV = luzVizinha & 0x0F;
                final int lsV = luzVizinha >> 4;

                if(lbV <= 1 && lsV <= 1) continue; // não vai propagar nada util

                // verifica se o bloco de entrada(na borda) é transparente
                final int bxNossa = iteraX ? a : bordaNossa;
                final int bzNossa = iteraX ? bordaNossa : a;
                final Bloco b = Bloco.numIds.get(ChunkUtil.obterBloco(bxNossa, y, bzNossa, chunk));
                if(b != null && b.render == TipoRender.OPACO) continue;

                final int idcNossa = iteraX
					? a + (bordaNossa << 4) + (y << 8)
					: bordaNossa + (a << 4) + (y << 8);

                final int lbNova = lbV - 1;
                final int lsNova = lsV - 1;

                final int luzAtual = luzTemp[idcNossa] & 0xFF;
                final int lbAtual  = luzAtual & 0x0F;
                final int lsAtual  = luzAtual >> 4;

                if(lbNova > lbAtual || lsNova > lsAtual) {
                    luzTemp[idcNossa] = (byte) ((Math.max(lsNova, lsAtual) << 4)
						|  Math.max(lbNova, lbAtual));
                    if(fimFila < filaLuz.length) {
                        filaLuz[fimFila++] = idcNossa;
                    }
                }
            }
        }
        return fimFila;
    }

    // utils
    public static void zerarLuzBlocoChunk(Chunk chunk) {
        for(int i = 0; i < TOTAL_BLOCOS; i++) {
            final int luzAtual = chunk.luz[i] & 0xFF;
            final int luzSolar = (luzAtual >> 4) & 0x0F;

            final int x = i & 0xF;
            final int z = (i >> 4) & 0xF;
            final int y = i >> 8;

            final Bloco b = Bloco.numIds.get(ChunkUtil.obterBloco(x, y, z, chunk));

            // mantem a luz solar; reinicia a luz de bloco para o valor intrinseco do bloco(ou 0)
            chunk.luz[i] = (b != null && b.luz > 0)
				? (byte) ((luzSolar << 4) | (b.luz & 0x0F))
				: (byte) (luzSolar << 4);
        }
    }

    public static Chunk obterChunk(final int cx, final int cz) {
        return Mundo.obterChunk(cx, cz);
    }
}

