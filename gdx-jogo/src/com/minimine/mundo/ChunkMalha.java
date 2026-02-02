package com.minimine.mundo;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.mundo.blocos.Bloco;
import com.minimine.mundo.blocos.BlocoModelo;

public class ChunkMalha {
    public static void attMalha(Chunk chunk, FloatArrayUtil verts, ShortArrayUtil idcSolidos, ShortArrayUtil idcTransp) {
        synchronized(chunk.debugRects) {
            chunk.debugRects.clear();
        }
        ChunkLuz.attLuz(chunk);

        Chunk cXP, cXN, cZP, cZN;
        Chave chave = new Chave(chunk.x + 1, chunk.z);
        cXP = Mundo.chunks.get(chave);
        chave.x = chunk.x - 1; chave.z = chunk.z;
        cXN = Mundo.chunks.get(chave);
        chave.x = chunk.x; chave.z = chunk.z + 1;
        cZP = Mundo.chunks.get(chave);
        chave.x = chunk.x; chave.z = chunk.z - 1;
        cZN = Mundo.chunks.get(chave);

        // --- Greedy Meshing Visual (Gera Malha de Renderização) ---
        
        // 1. Eixo Y (Faces Cima/Baixo)
        int[] mask = new int[16 * 16];
        for (boolean cima : new boolean[]{true, false}) { // Passada para Cima, depois Baixo
            for (int y = 0; y < Mundo.Y_CHUNK; y++) {
                int n = 0;
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int id = ChunkUtil.obterBloco(x, y, z, chunk);
                        int val = 0;
                        if (id != 0) {
                            Bloco b = Bloco.numIds.get(id);
                            if (b != null) {
                                int ny = cima ? y + 1 : y - 1;
                                int vizId = 0;
                                if (ny >= 0 && ny < Mundo.Y_CHUNK) {
                                    vizId = ChunkUtil.obterBloco(x, ny, z, chunk);
                                } else { 
                                    vizId = 0;
                                }
                                Bloco bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
                                
                                if (deveRenderFace(b, bViz)) {
                                    byte luz = ChunkUtil.obterLuzCompleta(x, (ny < 0 || ny >= Mundo.Y_CHUNK) ? y : ny, z, chunk);
                                    val = (id << 8) | (luz & 0xFF);
                                }
                            }
                        }
                        mask[n++] = val;
                    }
                }
                meshingPlano(mask, 16, 16, y, cima ? 0 : 1, chunk, verts, idcSolidos, idcTransp);
            }
        }

        // 2. Eixo X (Faces Leste/Oeste)
        mask = new int[16 * Mundo.Y_CHUNK];
        for (boolean leste : new boolean[]{true, false}) {
            for (int x = 0; x < 16; x++) {
                int n = 0;
                for (int y = 0; y < Mundo.Y_CHUNK; y++) {
                    for (int z = 0; z < 16; z++) {
                        int id = ChunkUtil.obterBloco(x, y, z, chunk);
                        int val = 0;
                        if (id != 0) {
                            Bloco b = Bloco.numIds.get(id);
                            if (b != null) {
                                int nx = leste ? x + 1 : x - 1;
                                int vizId = 0;
                                Chunk tC = chunk;
                                int tx = nx;
                                
                                if (nx >= 16) { tC = cXP; tx = 0; }
                                else if (nx < 0) { tC = cXN; tx = 15; }
                                
                                if (tC != null) vizId = ChunkUtil.obterBloco(tx, y, z, tC);
                                
                                Bloco bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
                                if (deveRenderFace(b, bViz)) {
                                    byte luz = (tC != null) ? ChunkUtil.obterLuzCompleta(tx, y, z, tC) : 15;
                                    val = (id << 8) | (luz & 0xFF);
                                }
                            }
                        }
                        mask[n++] = val;
                    }
                }
                meshingPlano(mask, 16, Mundo.Y_CHUNK, x, leste ? 2 : 3, chunk, verts, idcSolidos, idcTransp);
            }
        }

        // 3. Eixo Z (Faces Sul/Norte)
        for (boolean sul : new boolean[]{true, false}) {
            for (int z = 0; z < 16; z++) {
                int n = 0;
                for (int y = 0; y < Mundo.Y_CHUNK; y++) {
                    for (int x = 0; x < 16; x++) {
                        int id = ChunkUtil.obterBloco(x, y, z, chunk);
                        int val = 0;
                        if (id != 0) {
                            Bloco b = Bloco.numIds.get(id);
                            if (b != null) {
                                int nz = sul ? z + 1 : z - 1;
                                int vizId = 0;
                                Chunk tC = chunk;
                                int tz = nz;
                                
                                if (nz >= 16) { tC = cZP; tz = 0; }
                                else if (nz < 0) { tC = cZN; tz = 15; }
                                
                                if (tC != null) vizId = ChunkUtil.obterBloco(x, y, tz, tC);
                                
                                Bloco bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
                                if (deveRenderFace(b, bViz)) {
                                    byte luz = (tC != null) ? ChunkUtil.obterLuzCompleta(x, y, tz, tC) : 15;
                                    val = (id << 8) | (luz & 0xFF);
                                }
                            }
                        }
                        mask[n++] = val;
                    }
                }
                meshingPlano(mask, 16, Mundo.Y_CHUNK, z, sul ? 4 : 5, chunk, verts, idcSolidos, idcTransp);
            }
        }

        // --- Geração de Debug de Colisão (Greedy Physics - Independente da Malha Visual) ---
        // Foca apenas em geometria sólida para gerar retangulos unificados de colisão
        synchronized(chunk.debugRects) {
            chunk.debugRects.clear();
            
            // Vamos iterar por camadas Y e tentar mesclar retangulos XZ de blocos solidos.
            for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                // Mascara booleana de solidos nesta camada
                int[] solidMask = new int[16*16];
                int n = 0;
                for(int z = 0; z < 16; z++) {
                    for(int x = 0; x < 16; x++) {
                        int id = ChunkUtil.obterBloco(x, y, z, chunk);
                        boolean solido = false;
                        if(id != 0) {
                             Bloco b = Bloco.numIds.get(id);
                             if(b != null && b.solido) solido = true;
                        }
                        solidMask[n++] = solido ? 1 : 0;
                    }
                }
                
                // Greedy 2D Simples na camada (apenas XZ)
                n = 0; // reset index for greedy loop
                // NOTA: O loop abaixo percorre Z (outer) e X (inner) - correspondendo à ordem de preenchimento da mask acima
                for(int j = 0; j < 16; j++) { // Z
                     for(int i = 0; i < 16; ) { // X
                         if(solidMask[j * 16 + i] == 1) { // Usa indice calculado (j*16 + i) para seguranca
                             int idx = j * 16 + i;
                             
                             // Determinar largura (W) no eixo X
                             int w = 1; 
                             while(i + w < 16 && solidMask[j * 16 + (i + w)] == 1) w++;
                             
                             // Determinar altura (H) no eixo Z
                             int h = 1; 
                             boolean continua = true;
                             while(j + h < 16 && continua) {
                                 for(int k = 0; k < w; k++) {
                                     if(solidMask[(j + h) * 16 + (i + k)] != 1) {
                                         continua = false;
                                         break;
                                     }
                                 }
                                 if(continua) h++;
                             }
                             
                             // Limpa a área encontrada na máscara para não processar de novo
                             for (int l = 0; l < h; l++) {
                                for (int k = 0; k < w; k++) {
                                    solidMask[(j + l) * 16 + (i + k)] = 0;
                                }
                            }
                            
                            // Adiciona caixa de colisao altura 1 (y ate y+1)
                            chunk.debugRects.add(new com.badlogic.gdx.math.collision.BoundingBox(
                                new com.badlogic.gdx.math.Vector3(i, y, j),
                                new com.badlogic.gdx.math.Vector3(i + w, y + 1, j + h)
                            ));
                            
                            i += w;
                         } else {
                             i++;
                         }
                     }
                }
            }
        }
    }

    private static void meshingPlano(int[] mask, int largura, int altura, 
                                     int profundidade, int faceId, Chunk chunk,
                                     FloatArrayUtil verts, ShortArrayUtil idcSolidos, ShortArrayUtil idcTransp) {
        
        int n = 0;
        for (int j = 0; j < altura; j++) {
            for (int i = 0; i < largura; ) {
                int val = mask[n];
                if (val != 0) {
                    // Encontrou inicio de face
                    int w = 1;
                    while (i + w < largura && mask[n + w] == val) {
                        w++;
                    }

                    int h = 1;
                    boolean continua = true;
                    while (j + h < altura && continua) {
                        for (int k = 0; k < w; k++) {
                            if (mask[n + k + h * largura] != val) {
                                continua = false;
                                break;
                            }
                        }
                        if (continua) h++;
                    }

                    // Limpar mascara
                    for (int l = 0; l < h; l++) {
                        for (int k = 0; k < w; k++) {
                            mask[n + k + l * largura] = 0;
                        }
                    }

                    // Dados da face
                    int id = val >> 8;
                    int luzTotal = val & 0xFF;
                    float lb = (luzTotal & 0x0F) / 15f;
                    float ls = ((luzTotal >> 4) & 0x0F) / 15f;
                    
                    Bloco b = Bloco.numIds.get(id);
                    float x = 0, y = 0, z = 0;
                    float fw = 0, fh = 0;
                    
                    switch(faceId) {
                        case 0: case 1: // Y
                            x = i; z = j; y = profundidade;
                            fw = w; fh = h; 
                            break;
                        case 2: case 3: // X
                            z = i; y = j; x = profundidade;
                            fw = w; fh = h;
                            break;
                        case 4: case 5: // Z
                            x = i; y = j; z = profundidade;
                            fw = w; fh = h;
                            break;
                    }

                    ShortArrayUtil lista = b.transparente ? idcTransp : idcSolidos;
                    BlocoModelo.addFace(faceId, b.texturaId(faceId), x, y, z, fw, fh, lb, ls, verts, lista);

                    i += w;
                    n += w;
                } else {
                    i++;
                    n++;
                }
            }
        }
    }
    
    public static boolean deveRenderFace(Bloco atual, Bloco vizinho) {
        if(vizinho == null) return true;
        if(atual.tipo == vizinho.tipo) return false;
        if(!vizinho.transparente) return false;
        return true;
    }
}
