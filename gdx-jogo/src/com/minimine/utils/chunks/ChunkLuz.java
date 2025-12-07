package com.minimine.utils.chunks;

import com.minimine.cenas.Mundo;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.utils.blocos.Bloco;

public class ChunkLuz {
    public static float LUZ_AMBIENTE = 0.25f; // Mantive seu valor base
    public static final float[] FACE_LUZ = {1.0f, 0.4f, 0.7f, 0.7f, 0.8f, 0.8f};
    public static final int Y_MAX = Mundo.Y_CHUNK - 1;
    // se coloca 1: A luz entra 15 blocos terra adentro
    // se coloca 3: A luz entra 5 blocos escurece mais rapido(pra cavernas))
    public static final int DENSIDADE_SOLIDO = 2; 
    // posiçãp pros 6 vizinhos (X+, X-, Y+, Y-, Z+, Z-)
    public static final int[] POS_X = {1, -1, 0, 0, 0, 0};
    public static final int[] POS_Y = {0, 0, 1, -1, 0, 0};
    public static final int[] POS_Z = {0, 0, 0, 0, 1, -1};
	
    public static void attLuz(Chunk chunk) {
        // pega a luz atual do ciclo dia/noite(0 a 15)
        int luzCeuBase = (int)(DiaNoiteUtil.luz * 15);
		// variaveis locais pra JIT
        final int area = Mundo.CHUNK_AREA;
        final int altura = Mundo.Y_CHUNK;
        final int totalBlocos = area * altura;
        // array temporario e fila
        final byte[] luzTemp = new byte[totalBlocos];
        int[] filaLuz = new int[totalBlocos];
        int inicioFila = 0;
        int fimFila = 0;
        // passo 1: sol e fontes de luz
        for(int x = 0; x < Mundo.TAM_CHUNK; x++) {
            for(int z = 0; z < Mundo.TAM_CHUNK; z++) {
                // começa com a luz total do sol no topo
                int luzSolarAtual = luzCeuBase;
                // desce do topo até o final(0)
                for(int y = Y_MAX; y >= 0; y--) {
                    int idc = x + (z * Mundo.TAM_CHUNK) + (y * area);
                    // 1. aplica o degrade da luz solar
                    boolean solido = ChunkUtil.ehSolido(x, y, z, chunk);
                    if(solido) {
                        // se é solido, a luz do sol perde força(degrade)
                        luzSolarAtual -= DENSIDADE_SOLIDO;
                        if(luzSolarAtual < 0) luzSolarAtual = 0;
                    } 
                    // se é ar, a luzSolarAtual mantem o valor que veio de cima
                    // define a luz inicial do bloco como a luz solar que restou
                    int luzFinal = luzSolarAtual;
                    // 2. verifica se o bloco é uma fonte de luz
                    int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
                    if(blocoId > 0) {
                        Bloco b = Bloco.numIds.get(blocoId);
                        if(b != null && b.luz > luzFinal) {
                            luzFinal = b.luz; // a luz ganha do sol se for mais forte
                        }
                    }
                    // 3. salva e prepara pra espalhar
                    if(luzFinal > 0) {
                        luzTemp[idc] = (byte) luzFinal;
                        // adiciona na fila pra espalhar para os lados
                        filaLuz[fimFila++] = idc;
                    } else luzTemp[idc] = 0;
                }
            }
        }
        // passo 2: espalhamento(BFS)
        while(inicioFila < fimFila) {
            int idxAtual = filaLuz[inicioFila++];
            byte valorLuz = luzTemp[idxAtual];

            if(valorLuz <= 1) continue; // luz 1 vira 0
            // coordenadas atuais
            int cy = idxAtual / area;
            int resto = idxAtual % area;
            int cz = resto >> 4;
            int cx = resto & 0xF;
            // espalha pros 6 vizinhos
            for(int i = 0; i < 6; i++) {
                int nx = cx + POS_X[i];
                int ny = cy + POS_Y[i];
                int nz = cz + POS_Z[i];
                // verifica limites do array
                if(nx >= 0 && nx < Mundo.TAM_CHUNK &&
                    ny >= 0 && ny < Mundo.Y_CHUNK &&
                    nz >= 0 && nz < Mundo.TAM_CHUNK) {

                    int idcVizinho = nx + (nz * Mundo.TAM_CHUNK) + (ny * area);
                    // luz vizinha = Luz Atual - 1
                    byte novaLuz = (byte)(valorLuz - 1);
                    // se a nova luz for mais forte do que o que ja tem la, atualiza
                    if(luzTemp[idcVizinho] < novaLuz) {
                        luzTemp[idcVizinho] = novaLuz;
                        filaLuz[fimFila++] = idcVizinho;
                    }
                }
            }
        }
        // passo 3: salva na chunk
        for(int i = 0; i < totalBlocos; i++) {
            // recalculo reverso de indice pra coordenadas
            int cy = i / area;
            int resto = i % area;
            int cz = resto >> 4;
            int cx = resto & 0xF;

            defLuz(cx, cy, cz, luzTemp[i], chunk);
        }
    }

    public static float calcularNivelLuz(int x, int y, int z, int idFace, Chunk chunk) {
        byte luzVal = obterLuz(x, y, z, chunk);
        float c = (luzVal & 0xFF) / 15.0f;
        if(c < LUZ_AMBIENTE) c = LUZ_AMBIENTE;
        float f = c * FACE_LUZ[idFace];
        return Math.min(Math.max(f, 0.1f), 1.0f);
    }

    public static byte obterLuz(int x, int y, int z, Chunk chunk) {
        int idc = x + (z << 4) + (y * Mundo.CHUNK_AREA);
        return (byte)((chunk.luz[idc >> 1] >> ((idc & 1) << 2)) & 15);
    }

    public static void defLuz(int x, int y, int z, byte valor, Chunk chunk) {
        int idc = x + (z << 4) + (y * Mundo.CHUNK_AREA);
        int byteIdc = idc >> 1;
        int shift = (idc & 1) << 2;
        chunk.luz[byteIdc] = (byte)(
            (chunk.luz[byteIdc] & ~(15 << shift)) | 
            ((valor & 15) << shift));
    }
}
