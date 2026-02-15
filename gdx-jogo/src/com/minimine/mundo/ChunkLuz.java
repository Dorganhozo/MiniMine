package com.minimine.mundo;

import com.minimine.mundo.blocos.Bloco;

public class ChunkLuz {
    public static final float[] FACE_LUZ = {1.0f, 0.4f, 0.7f, 0.7f, 0.8f, 0.8f};
    public static final int Y_MAX = Mundo.Y_CHUNK - 1;
    public static final int[] POS_X = {1, -1, 0, 0, 0, 0};
    public static final int[] POS_Y = {0, 0, 1, -1, 0, 0};
    public static final int[] POS_Z = {0, 0, 0, 0, 1, -1};

    public static void calcularLuz(Chunk chunk) {
        final int area = Mundo.CHUNK_AREA;
        final int totalBlocos = area * Mundo.Y_CHUNK;

        // arrays temporarios
        final byte[] luzTemp = new byte[totalBlocos];
        int[] filaLuz = new int[totalBlocos * 4];
        int inicioFila = 0;
        int fimFila = 0;

        // 1. inicialização: luz solar e fontes de luz
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
                        if(luzSolarAtual <= 0) {
                            filaLuz[fimFila++] = idc;
                        }
                    }
                }
            }
        }
        // 2. importa a luz das vizinhas (se existirem e tiverem dados prontos)
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
                    if(lbV < lb - 1 && lb > 0) { lbV = lb - 1; mudou = true; }
                    if(lsV < ls - 1 && ls > 0) { lsV = ls - 1; mudou = true; }

                    if(mudou) {
                        luzTemp[idcVizinho] = (byte) ((lsV << 4) | lbV);

                        int blocoIdV = ChunkUtil.obterBloco(nx, ny, nz, chunk);
                        Bloco bV = Bloco.numIds.get(blocoIdV);

                        if(bV == null || bV.transparente) {
                            if(fimFila < filaLuz.length) {
                                filaLuz[fimFila++] = idcVizinho;
                            }
                        }
                    }
                }
            }
        }
        // 4. copia resultado
        System.arraycopy(luzTemp, 0, chunk.luz, 0, totalBlocos);

        // 5. marca luz como não suja
        chunk.luzSuja = false;
    }

    public static void attLuz(Chunk chunk) {
        if(!chunk.luzSuja) return;
        chunk.luzSuja = false;
        attLuzCompleta(chunk);
    }

    public static void attLuzCompleta(Chunk chunk) {
        final int area = Mundo.CHUNK_AREA;
        final int totalBlocos = area * Mundo.Y_CHUNK;

        // arrays temporarios
        final byte[] luzTemp = new byte[totalBlocos];
        int[] filaLuz = new int[totalBlocos * 4];
        int inicioFila = 0;
        int fimFila = 0;

        // 1. inicialização: luz solar e fontes de luz
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

                        if(bV == null || bV.transparente) {
                            if(fimFila < filaLuz.length) {
                                filaLuz[fimFila++] = idcVizinho;
                            }
                        }
                    }
                }
            }
        }
        // 4. copia resultado
        System.arraycopy(luzTemp, 0, chunk.luz, 0, totalBlocos);
    }

    // apenas le os dados das vizinhas se elas ja tiverem seus dados prontos
    private static int importarLuzVizinhas(Chunk chunk, byte[] luzTemp, int[] filaLuz, int fimFila) {
        // pega chunks vizinhas
        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);

        // importa da borda norte(z=0 recebe de z=15 da vizinha norte)
        if(chunkNorte != null && chunkNorte.dadosProntos) {
            fimFila = importarBordaNorte(chunk, chunkNorte, luzTemp, filaLuz, fimFila);
        }
        // importa da borda sul(z=15 recebe de z=0 da vizinha sul)
        if(chunkSul != null && chunkSul.dadosProntos) {
            fimFila = importarBordaSul(chunk, chunkSul, luzTemp, filaLuz, fimFila);
        }
        // importa da borda leste(x=15 recebe de x=0 da vizinha leste)
        if(chunkLeste != null && chunkLeste.dadosProntos) {
            fimFila = importarBordaLeste(chunk, chunkLeste, luzTemp, filaLuz, fimFila);
        }
        // importa da borda oeste(x=0 recebe de x=15 da vizinha oeste)
        if(chunkOeste != null && chunkOeste.dadosProntos) {
            fimFila = importarBordaOeste(chunk, chunkOeste, luzTemp, filaLuz, fimFila);
        }
        return fimFila;
    }

    // funções auxiliares pra importar luz de cada borda
    private static int importarBordaNorte(Chunk chunk, Chunk chunkNorte, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = x + (15 << 4) + (y << 8);
                int luzVizinha = chunkNorte.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(x, y, 0, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.transparente) {
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

    private static int importarBordaSul(Chunk chunk, Chunk chunkSul, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = x + (0 << 4) + (y << 8);
                int luzVizinha = chunkSul.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(x, y, 15, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.transparente) {
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

    private static int importarBordaLeste(Chunk chunk, Chunk chunkLeste, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int z = 0; z < 16; z++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = 0 + (z << 4) + (y << 8);
                int luzVizinha = chunkLeste.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(15, y, z, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.transparente) {
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

    private static int importarBordaOeste(Chunk chunk, Chunk chunkOeste, byte[] luzTemp, int[] filaLuz, int fimFila) {
        for(int z = 0; z < 16; z++) {
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                int idcVizinha = 15 + (z << 4) + (y << 8);
                int luzVizinha = chunkOeste.luz[idcVizinha] & 0xFF;
                int lbV = luzVizinha & 0x0F;
                int lsV = luzVizinha >> 4;

                if(lbV > 1 || lsV > 1) {
                    int blocoId = ChunkUtil.obterBloco(0, y, z, chunk);
                    Bloco b = Bloco.numIds.get(blocoId);
                    if(b == null || b.transparente) {
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

    // importa luz das bordas das chunks vizinhas pra dentro dessa chunk
    public static int obterLuzVizinhas(Chunk chunk, byte[] luzTemp, int[] filaLuz, int fimFila) {
        // pega chunks vizinhas
        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);

        // importa da borda norte(z=0 recebe de z=15 da vizinha norte)
        if(chunkNorte != null) {
            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    // luz da borda sul da chunk norte(z=15)
                    int idcVizinha = x + (15 << 4) + (y << 8);
                    int luzVizinha = chunkNorte.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        // verifica se bloco na nossa borda aceita luz
                        int blocoId = ChunkUtil.obterBloco(x, y, 0, chunk);
                        Bloco b = Bloco.numIds.get(blocoId);
                        if(b == null || b.transparente) {
                            // propaga luz atenuada pra nossa borda
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
        // importa da borda sul(z=15 recebe de z=0 da vizinha sul)
        if(chunkSul != null) {
            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = x + (0 << 4) + (y << 8);
                    int luzVizinha = chunkSul.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        int blocoId = ChunkUtil.obterBloco(x, y, 15, chunk);
                        Bloco b = Bloco.numIds.get(blocoId);
                        if(b == null || b.transparente) {
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
        // importa da borda leste(x=15 recebe de x=0 da vizinha leste)
        if(chunkLeste != null) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = 0 + (z << 4) + (y << 8);
                    int luzVizinha = chunkLeste.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        int blocoId = ChunkUtil.obterBloco(15, y, z, chunk);
                        Bloco b = Bloco.numIds.get(blocoId);
                        if(b == null || b.transparente) {
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
        // importa da borda oeste(x=0 recebe de x=15 da vizinha oeste)
        if(chunkOeste != null) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    int idcVizinha = 15 + (z << 4) + (y << 8);
                    int luzVizinha = chunkOeste.luz[idcVizinha] & 0xFF;
                    int lbV = luzVizinha & 0x0F;
                    int lsV = luzVizinha >> 4;

                    if(lbV > 1 || lsV > 1) {
                        int blocoId = ChunkUtil.obterBloco(0, y, z, chunk);
                        Bloco b = Bloco.numIds.get(blocoId);
                        if(b == null || b.transparente) {
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

    public static void attVizinhas(Chunk chunk) {
        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);

        if(chunkNorte != null) {
            chunkNorte.luzSuja = true;
            chunkNorte.att = true;
        }
        if(chunkSul != null) {
            chunkSul.luzSuja = true;
            chunkSul.att = true;
        }
        if(chunkLeste != null) {
            chunkLeste.luzSuja = true;
            chunkLeste.att = true;
        }
        if(chunkOeste != null) {
            chunkOeste.luzSuja = true;
            chunkOeste.att = true;
        }
    }
    /*
     * zera a luz de bloco na chunk e vizinhas quando emissor é removido
     * isso evita que chunks importem luz antiga umas das outras durante recalculo
     */
    public static void zerarLuzAoRemoverEmissor(Chunk chunk) {
        // zera luz de bloco na chunk atual
        zerarLuzBlocoChunk(chunk);

        // zera nas vizinhas também
        Chunk chunkNorte = obterChunk(chunk.x, chunk.z - 1);
        Chunk chunkSul = obterChunk(chunk.x, chunk.z + 1);
        Chunk chunkLeste = obterChunk(chunk.x + 1, chunk.z);
        Chunk chunkOeste = obterChunk(chunk.x - 1, chunk.z);

        if(chunkNorte != null) zerarLuzBlocoChunk(chunkNorte);
        if(chunkSul != null) zerarLuzBlocoChunk(chunkSul);
        if(chunkLeste != null) zerarLuzBlocoChunk(chunkLeste);
        if(chunkOeste != null) zerarLuzBlocoChunk(chunkOeste);

        // agora marca todas como sujas pra recalcular
        chunk.luzSuja = true;
        chunk.att = true;
        if(chunkNorte != null) { chunkNorte.luzSuja = true; chunkNorte.att = true; }
        if(chunkSul != null) { chunkSul.luzSuja = true; chunkSul.att = true; }
        if(chunkLeste != null) { chunkLeste.luzSuja = true; chunkLeste.att = true; }
        if(chunkOeste != null) { chunkOeste.luzSuja = true; chunkOeste.att = true; }
    }

    // zera apenas luz de bloco (mantém luz solar e emissores diretos)
    private static void zerarLuzBlocoChunk(Chunk chunk) {
        final int totalBlocos = Mundo.CHUNK_AREA * Mundo.Y_CHUNK;

        for(int i = 0; i < totalBlocos; i++) {
            int luzAtual = chunk.luz[i] & 0xFF;
            int luzSolar = (luzAtual >> 4) & 0x0F; // mantém bits altos(luz solar)

            // verifica se este bloco é um emissor direto
            int x = i & 0xF;
            int z = (i >> 4) & 0xF;
            int y = i >> 8;

            int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
            Bloco b = Bloco.numIds.get(blocoId);

            if(b != null && b.luz > 0) {
                // é um emissor, mantem sua luz
                chunk.luz[i] = (byte)((luzSolar << 4) | (b.luz & 0x0F));
            } else {
                // não é emissor, zera luz de bloco
                chunk.luz[i] = (byte)(luzSolar << 4);
            }
        }
    }

    public static Chunk obterChunk(int cx, int cz) {
        long chave = Chave.calcularChave(cx, cz);
        return Mundo.chunks.get(chave);
    }
}

