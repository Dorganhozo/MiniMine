package com.minimine.mundo;

import com.minimine.mundo.blocos.Bloco;
import com.minimine.graficos.TipoRender;
import java.util.Arrays;

public class ChunkLuz {
    public static final float[] FACE_LUZ = {1.0f, 0.4f, 0.7f, 0.7f, 0.8f, 0.8f};
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

    public static void calcularLuz(Chunk chunk) {
        chunk.luzFazendo = true;
        // reutiliza arrays do reuso da thread atual, zero alocação
        final byte[] luzTemp = LUZ_TEMP_REUSO.get();
        final int[] filaLuz = FILA_LUZ_REUSO.get();
        Arrays.fill(luzTemp, (byte)0);
        int inicioFila = 0;
        int fimFila = 0;

        // 1. inicia: luz solar e fontes de luz
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int luzSolarAtual = 15;
                final int posXZ = x + (z << 4);

                for(int y = Y_MAX; y >= 0; y--) {
                    final int idc = posXZ + (y << 8);

                    final int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);

                    if(b != null && b.render == TipoRender.OPACO) luzSolarAtual = 0;

                    luzTemp[idc] = (byte)(luzSolarAtual << 4);

                    if(luzSolarAtual > 0) {
                        filaLuz[fimFila++] = idc;
                    }
                    if(b != null && b.luz > 0) {
                        luzTemp[idc] |= (byte) (b.luz & 0x0F);
                        if(luzSolarAtual <= 0) {
                            filaLuz[fimFila++] = idc;
                        }
                    }
                }
            }
        }
        // 2. importa a luz das vizinhas(se existirem e tiverem dados prontos)
        fimFila = importarLuzVizinhas(chunk, luzTemp, filaLuz, fimFila);

        // 3. propagação BFS dentro da chunk
        while(inicioFila < fimFila) {
            int idcAtual = filaLuz[inicioFila++];
            int luzTotal = luzTemp[idcAtual] & 0xFF;

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
                    if(lbV < lb - 1 && lb > 0) {
						lbV = lb - 1;
						mudou = true;
					}
                    if(lsV < ls - 1 && ls > 0) {
						lsV = ls - 1;
						mudou = true;
					}
                    if(mudou) {
                        luzTemp[idcVizinho] = (byte)((lsV << 4) | lbV);

                        final int blocoIdV = ChunkUtil.obterBloco(nx, ny, nz, chunk);
                        final Bloco bV = Bloco.numIds.get(blocoIdV);

                        if(bV == null || bV.render != TipoRender.OPACO) {
                            if(fimFila < filaLuz.length) {
                                filaLuz[fimFila++] = idcVizinho;
                            }
                        }
                    }
                }
            }
        }
        // 4. copia resultado
        System.arraycopy(luzTemp, 0, chunk.luz, 0, TOTAL_BLOCOS);

        // 5. marca luz como não suja
        chunk.luzSuja = false;
        chunk.luzFazendo = false;
    }

    public static void attLuz(Chunk chunk) {
        if(!chunk.luzSuja) return;
        chunk.luzSuja = false;
        attLuzCompleta(chunk);
    }

    public static void attLuzCompleta(Chunk chunk) {
        chunk.luzFazendo = true;
        // reutiliza arrays do reuso da thread atual, zero alocação
        final byte[] luzTemp = LUZ_TEMP_REUSO.get();
        final int[] filaLuz = FILA_LUZ_REUSO.get();
        Arrays.fill(luzTemp, (byte)0);
        int inicioFila = 0;
        int fimFila = 0;

        // 1. inicia: luz solar e fontes de luz
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int luzSolarAtual = 15;
                int posXZ = x + (z << 4);

                for(int y = Y_MAX; y >= 0; y--) {
                    int idc = posXZ + (y << 8);

                    final int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
                    final Bloco b = Bloco.numIds.get(blocoId);

                    if(b != null && b.render == TipoRender.OPACO) luzSolarAtual = 0;

                    luzTemp[idc] = (byte)(luzSolarAtual << 4);

                    if(luzSolarAtual > 0) {
                        filaLuz[fimFila++] = idc;
                    }
                    if(b != null && b.luz > 0) {
                        luzTemp[idc] |= (byte)(b.luz & 0x0F);
                        if(luzSolarAtual <= 0) {
                            filaLuz[fimFila++] = idc;
                        }
                    }
                }
            }
        }
        // 2. importa a luz das vizinhas
        fimFila = obterLuzVizinhas(chunk, luzTemp, filaLuz, fimFila);

        // 3. propagação BFS dentro da chunk
        while(inicioFila < fimFila) {
            int idcAtual = filaLuz[inicioFila++];
            int luzTotal = luzTemp[idcAtual] & 0xFF;

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

                        if(bV == null || bV.render != TipoRender.OPACO) {
                            if(fimFila < filaLuz.length) {
                                filaLuz[fimFila++] = idcVizinho;
                            }
                        }
                    }
                }
            }
        }
        // 4. copia resultado
        System.arraycopy(luzTemp, 0, chunk.luz, 0, TOTAL_BLOCOS);
        chunk.luzFazendo = false;
    }

    // recalcula a chunk central e em seguida cada vizinha em ordem garantida,
    // cada vizinha rele a borda ja atualizada da chunk central sem depender
    // da ordem de iteração do mapa e sem marcar luzSuja pras vizinhas
    public static void attLuzVizinhas(Chunk chunk) {
        attLuzCompleta(chunk);
        chunk.luzSuja = false;

        Chunk cn = obterChunk(chunk.x, chunk.z - 1);
        Chunk cs = obterChunk(chunk.x, chunk.z + 1);
        Chunk cl = obterChunk(chunk.x + 1, chunk.z);
        Chunk co = obterChunk(chunk.x - 1, chunk.z);

        if(cn != null && cn.dadosProntos && !cn.luzFazendo) { attLuzCompleta(cn); cn.luzSuja = false; cn.att = true; }
        if(cs != null && cs.dadosProntos && !cs.luzFazendo) { attLuzCompleta(cs); cs.luzSuja = false; cs.att = true; }
        if(cl != null && cl.dadosProntos && !cl.luzFazendo) { attLuzCompleta(cl); cl.luzSuja = false; cl.att = true; }
        if(co != null && co.dadosProntos && !co.luzFazendo) { attLuzCompleta(co); co.luzSuja = false; co.att = true; }

        // diagonais: depois das cardinais para que ja leiam bordas atualizadas
        Chunk cne = obterChunk(chunk.x + 1, chunk.z - 1);
        Chunk cno = obterChunk(chunk.x - 1, chunk.z - 1);
        Chunk cse = obterChunk(chunk.x + 1, chunk.z + 1);
        Chunk cso = obterChunk(chunk.x - 1, chunk.z + 1);

        if(cne != null && cne.dadosProntos && !cne.luzFazendo) { attLuzCompleta(cne); cne.luzSuja = false; cne.att = true; }
        if(cno != null && cno.dadosProntos && !cno.luzFazendo) { attLuzCompleta(cno); cno.luzSuja = false; cno.att = true; }
        if(cse != null && cse.dadosProntos && !cse.luzFazendo) { attLuzCompleta(cse); cse.luzSuja = false; cse.att = true; }
        if(cso != null && cso.dadosProntos && !cso.luzFazendo) { attLuzCompleta(cso); cso.luzSuja = false; cso.att = true; }

        chunk.att = true;
    }

    // apenas le os dados das vizinhas se elas ja tiverem seus dados prontos
    public static int importarLuzVizinhas(Chunk chunk, byte[] luzTemp, int[] filaLuz, int fimFila) {
        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);

        if(chunkNorte != null && chunkNorte.dadosProntos && !chunkNorte.luzFazendo) {
            fimFila = importarBordaNorte(chunk, chunkNorte, luzTemp, filaLuz, fimFila);
        }
        if(chunkSul != null && chunkSul.dadosProntos && !chunkSul.luzFazendo) {
            fimFila = importarBordaSul(chunk, chunkSul, luzTemp, filaLuz, fimFila);
        }
        if(chunkLeste != null && chunkLeste.dadosProntos && !chunkLeste.luzFazendo) {
            fimFila = importarBordaLeste(chunk, chunkLeste, luzTemp, filaLuz, fimFila);
        }
        if(chunkOeste != null && chunkOeste.dadosProntos && !chunkOeste.luzFazendo) {
            fimFila = importarBordaOeste(chunk, chunkOeste, luzTemp, filaLuz, fimFila);
        }
        return fimFila;
    }

    public static int importarBordaNorte(Chunk chunk, Chunk chunkNorte, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = x + (15 << 4) + (y << 8);
                int luzVizinha = chunkNorte.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(x, y, 0, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.render != TipoRender.OPACO) {
                        int idcNossa = x + (0 << 4) + (y << 8);
                        int lbNova = Math.max(0, lbV - 1);
                        int lsNova = Math.max(0, lsV - 1);

                        int luzAtual = luzTemp[idcNossa] & 0xFF;
                        int lbAtual = luzAtual & 0x0F;
                        int lsAtual = luzAtual >> 4;

                        if(lbNova > lbAtual || lsNova > lsAtual) {
                            luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                            filaLuz[fimFila++] = idcNossa;
                        }
                    }
                }
            }
        }
        return fimFila;
    }

    public static int importarBordaSul(Chunk chunk, Chunk chunkSul, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = x + (0 << 4) + (y << 8);
                int luzVizinha = chunkSul.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(x, y, 15, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.render != TipoRender.OPACO) {
                        int idcNossa = x + (15 << 4) + (y << 8);
                        int lbNova = Math.max(0, lbV - 1);
                        int lsNova = Math.max(0, lsV - 1);

                        int luzAtual = luzTemp[idcNossa] & 0xFF;
                        int lbAtual = luzAtual & 0x0F;
                        int lsAtual = luzAtual >> 4;

                        if(lbNova > lbAtual || lsNova > lsAtual) {
                            luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                            filaLuz[fimFila++] = idcNossa;
                        }
                    }
                }
            }
        }
        return fimFila;
    }

    public static int importarBordaLeste(Chunk chunk, Chunk chunkLeste, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int z = 0; z < 16; z++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = 0 + (z << 4) + (y << 8);
                int luzVizinha = chunkLeste.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(15, y, z, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.render != TipoRender.OPACO) {
                        int idcNossa = 15 + (z << 4) + (y << 8);
                        int lbNova = Math.max(0, lbV - 1);
                        int lsNova = Math.max(0, lsV - 1);

                        int luzAtual = luzTemp[idcNossa] & 0xFF;
                        int lbAtual = luzAtual & 0x0F;
                        int lsAtual = luzAtual >> 4;

                        if(lbNova > lbAtual || lsNova > lsAtual) {
                            luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                            filaLuz[fimFila++] = idcNossa;
                        }
                    }
                }
            }
        }
        return fimFila;
    }

    public static int importarBordaOeste(Chunk chunk, Chunk chunkOeste, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int z = 0; z < 16; z++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = 15 + (z << 4) + (y << 8);
                int luzVizinha = chunkOeste.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(0, y, z, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.render != TipoRender.OPACO) {
                        int idcNossa = 0 + (z << 4) + (y << 8);
                        int lbNova = Math.max(0, lbV - 1);
                        int lsNova = Math.max(0, lsV - 1);

                        int luzAtual = luzTemp[idcNossa] & 0xFF;
                        int lbAtual = luzAtual & 0x0F;
                        int lsAtual = luzAtual >> 4;

                        if(lbNova > lbAtual || lsNova > lsAtual) {
                            luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                            filaLuz[fimFila++] = idcNossa;
                        }
                    }
                }
            }
        }
        return fimFila;
    }

    public static int obterLuzVizinhas(Chunk chunk, byte[] luzTemp, int[] filaLuz, int fimFila) {
        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);
        if(chunkNorte != null && (!chunkNorte.dadosProntos || chunkNorte.luzFazendo)) chunkNorte = null;
        if(chunkSul != null && (!chunkSul.dadosProntos || chunkSul.luzFazendo)) chunkSul = null;
        if(chunkLeste != null && (!chunkLeste.dadosProntos || chunkLeste.luzFazendo)) chunkLeste = null;
        if(chunkOeste != null && (!chunkOeste.dadosProntos || chunkOeste.luzFazendo)) chunkOeste = null;

        if(chunkNorte != null) {
            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = x + (15 << 4) + (y << 8);
                    int luzVizinha = chunkNorte.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        int blocoId = ChunkUtil.obterBloco(x, y, 0, chunk);
                        Bloco b = Bloco.numIds.get(blocoId);
                        if(b == null || b.render != TipoRender.OPACO) {
                            int idcNossa = x + (0 << 4) + (y << 8);
                            int lbNova = Math.max(0, lbV - 1);
                            int lsNova = Math.max(0, lsV - 1);

                            int luzAtual = luzTemp[idcNossa] & 0xFF;
                            int lbAtual = luzAtual & 0x0F;
                            int lsAtual = luzAtual >> 4;

                            if(lbNova > lbAtual || lsNova > lsAtual) {
                                luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                                filaLuz[fimFila++] = idcNossa;
                            }
                        }
                    }
                }
            }
        }
        if(chunkSul != null) {
            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = x + (0 << 4) + (y << 8);
                    int luzVizinha = chunkSul.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        Bloco b = Bloco.numIds.get(ChunkUtil.obterBloco(x, y, 15, chunk));
                        if(b == null || b.render != TipoRender.OPACO) {
                            int idcNossa = x + (15 << 4) + (y << 8);
                            int lbNova = Math.max(0, lbV - 1);
                            int lsNova = Math.max(0, lsV - 1);

                            int luzAtual = luzTemp[idcNossa] & 0xFF;
                            int lbAtual = luzAtual & 0x0F;
                            int lsAtual = luzAtual >> 4;

                            if(lbNova > lbAtual || lsNova > lsAtual) {
                                luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                                filaLuz[fimFila++] = idcNossa;
                            }
                        }
                    }
                }
            }
        }
        if(chunkLeste != null) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = 0 + (z << 4) + (y << 8);
                    int luzVizinha = chunkLeste.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        Bloco b = Bloco.numIds.get(ChunkUtil.obterBloco(15, y, z, chunk));
                        if(b == null || b.render != TipoRender.OPACO) {
                            int idcNossa = 15 + (z << 4) + (y << 8);
                            int lbNova = Math.max(0, lbV - 1);
                            int lsNova = Math.max(0, lsV - 1);

                            int luzAtual = luzTemp[idcNossa] & 0xFF;
                            int lbAtual = luzAtual & 0x0F;
                            int lsAtual = luzAtual >> 4;

                            if(lbNova > lbAtual || lsNova > lsAtual) {
                                luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                                filaLuz[fimFila++] = idcNossa;
                            }
                        }
                    }
                }
            }
        }
        if(chunkOeste != null) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = 15 + (z << 4) + (y << 8);
                    int luzVizinha = chunkOeste.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        Bloco b = Bloco.numIds.get(ChunkUtil.obterBloco(0, y, z, chunk));
                        if(b == null || b.render != TipoRender.OPACO) {
                            int idcNossa = 0 + (z << 4) + (y << 8);
                            int lbNova = Math.max(0, lbV - 1);
                            int lsNova = Math.max(0, lsV - 1);

                            int luzAtual = luzTemp[idcNossa] & 0xFF;
                            int lbAtual = luzAtual & 0x0F;
                            int lsAtual = luzAtual >> 4;

                            if(lbNova > lbAtual || lsNova > lsAtual) {
                                luzTemp[idcNossa] = (byte)((Math.max(lsNova, lsAtual) << 4) | Math.max(lbNova, lbAtual));
                                filaLuz[fimFila++] = idcNossa;
                            }
                        }
                    }
                }
            }
        }
        return fimFila;
    }

    public static void zerarLuz(Chunk chunk) {
        zerarLuzBlocoChunk(chunk);

        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);
        Chunk chunkNE = obterChunk(chunk.x + 1, chunk.z - 1);
        Chunk chunkNO = obterChunk(chunk.x - 1, chunk.z - 1);
        Chunk chunkSE = obterChunk(chunk.x + 1, chunk.z + 1);
        Chunk chunkSO = obterChunk(chunk.x - 1, chunk.z + 1);

        if(chunkNorte != null) zerarLuzBlocoChunk(chunkNorte);
        if(chunkSul != null) zerarLuzBlocoChunk(chunkSul);
        if(chunkLeste != null) zerarLuzBlocoChunk(chunkLeste);
        if(chunkOeste != null) zerarLuzBlocoChunk(chunkOeste);
        if(chunkNE != null) zerarLuzBlocoChunk(chunkNE);
        if(chunkNO != null) zerarLuzBlocoChunk(chunkNO);
        if(chunkSE != null) zerarLuzBlocoChunk(chunkSE);
        if(chunkSO != null) zerarLuzBlocoChunk(chunkSO);

        chunk.luzSuja = true;
        chunk.att = true;
        if(chunkNorte != null) { chunkNorte.luzSuja = true; chunkNorte.att = true; }
        if(chunkSul != null)   { chunkSul.luzSuja = true; chunkSul.att = true; }
        if(chunkLeste != null) { chunkLeste.luzSuja = true; chunkLeste.att = true; }
        if(chunkOeste != null) { chunkOeste.luzSuja = true; chunkOeste.att = true; }
        if(chunkNE != null) { chunkNE.luzSuja = true; chunkNE.att = true; }
        if(chunkNO != null) { chunkNO.luzSuja = true; chunkNO.att = true; }
        if(chunkSE != null) { chunkSE.luzSuja = true; chunkSE.att = true; }
        if(chunkSO != null) { chunkSO.luzSuja = true; chunkSO.att = true; }
    }

    public static void zerarLuzBlocoChunk(Chunk chunk) {
        for(int i = 0; i < TOTAL_BLOCOS; i++) {
            int luzAtual = chunk.luz[i] & 0xFF;
            int luzSolar = (luzAtual >> 4) & 0x0F;

            int x = i & 0xF;
            int z = (i >> 4) & 0xF;
            int y = i >> 8;

            final int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
            final Bloco b = Bloco.numIds.get(blocoId);

            if(b != null && b.luz > 0) {
                chunk.luz[i] = (byte)((luzSolar << 4) | (b.luz & 0x0F));
            } else {
                chunk.luz[i] = (byte)(luzSolar << 4);
            }
        }
    }

    public static final Chunk obterChunk(final int cx, final int cz) {
        return Mundo.obterChunk(cx, cz);
    }
}

