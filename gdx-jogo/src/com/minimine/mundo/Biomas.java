package com.minimine.mundo;

import com.minimine.mundo.geracao.GeradorTerreno;
import com.minimine.mundo.geracao.GeradorTerreno.TipoBioma;

// interface entre o gerador de terreno e o sistema de chunks
public class Biomas {
    public static GeradorTerreno gerador;

    public static void iniciar() {
        gerador = new GeradorTerreno(Mundo.semente);
    }

    public static void escolher(Chunk chunk) {
        int chunkX = chunk.x << 4;
        int chunkZ = chunk.z << 4;

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int mundoX = chunkX + x;
                int mundoZ = chunkZ + z;

                // recebe altura e bioma em uma unica chamada otimizada
                int[] dados = gerador.calcularDadosColuna(mundoX, mundoZ);
                int altura = dados[0];
                TipoBioma bioma = TipoBioma.values()[dados[1]];

                gerarColuna(chunk, x, z, altura, bioma, mundoX, mundoZ);
            }
        }
        chunk.dadosProntos = true;
    }

    private static void gerarColuna(Chunk chunk, int x, int z, int altura, TipoBioma bioma, int mundoX, int mundoZ) {
        // fim do mundo
        ChunkUtil.defBloco(x, 0, z, "pedra", chunk);

        // base de pedra
        for(int y = 1; y < altura - 4; y++) {
            ChunkUtil.defBloco(x, y, z, "pedra", chunk);
        }
        // camadas superiores por bioma
        switch(bioma) {
            case OCEANO:
            case OCEANO_QUENTE:
            case OCEANO_PROFUNDO:
                // areia ou pedra
                int profundidade = 62 - altura;
                for(int y = altura - 4; y < altura; y++) {
                    if(profundidade > 20) {
                        ChunkUtil.defBloco(x, y, z, "pedra", chunk);
                    } else {
                        ChunkUtil.defBloco(x, y, z, "areia", chunk);
                    }
                }
                // água
                for(int y = altura; y <= 62; y++) {
                    ChunkUtil.defBloco(x, y, z, "agua", chunk);
                }
            break;
            case OCEANO_COSTEIRO:
                // areia
                for(int y = altura - 3; y < altura; y++) {
                    ChunkUtil.defBloco(x, y, z, "areia", chunk);
                }
                // água rasa
                for(int y = altura; y <= 62; y++) {
                    ChunkUtil.defBloco(x, y, z, "agua", chunk);
                }
            break;
            case DESERTO:
            case COLINAS_DESERTO:
                // areia profunda
                for(int y = altura - 5; y < altura; y++) {
                    ChunkUtil.defBloco(x, y, z, "areia", chunk);
                }
            break;
            case PLANICIES:
            case PLANICIES_MONTANHOSAS:
                // terra
                for(int y = altura - 4; y < altura - 1; y++) {
                    ChunkUtil.defBloco(x, y, z, "terra", chunk);
                }
                // grama
                ChunkUtil.defBloco(x, altura - 1, z, "grama", chunk);
            break;
            case PLANICIES_AGUADAS:
                // terra
                for(int y = altura - 4; y < altura - 1; y++) {
                    ChunkUtil.defBloco(x, y, z, "terra", chunk);
                }
                // verifica se é lago
                double lago = Mundo.s2D.ruido(mundoX * 0.015, mundoZ * 0.015);
                if(lago < -0.5) {
                    ChunkUtil.defBloco(x, altura - 1, z, "areia", chunk);
                    ChunkUtil.defBloco(x, altura, z, "agua", chunk);
                    ChunkUtil.defBloco(x, altura + 1, z, "agua", chunk);
                } else {
                    ChunkUtil.defBloco(x, altura - 1, z, "grama", chunk);
                }
            break;
            case FLORESTA:
            case FLORESTA_COSTEIRA:
            case FLORESTA_MONTANHOSA:
                // terra
                int profTerra = bioma == TipoBioma.FLORESTA_MONTANHOSA ? 3 : 4;
                for(int y = altura - profTerra; y < altura - 1; y++) {
                    ChunkUtil.defBloco(x, y, z, "terra", chunk);
                }
                // grama
                ChunkUtil.defBloco(x, altura - 1, z, "grama", chunk);
            break;
            case FLORESTA_COM_RIOS:
                // terra
                for(int y = altura - 4; y < altura - 1; y++) {
                    ChunkUtil.defBloco(x, y, z, "terra", chunk);
                }
                // verifica se é rio
                double rio = Math.abs(Mundo.s2D.ruido(mundoX * 0.008, mundoZ * 0.008));
                if(rio < 0.08) {
                    ChunkUtil.defBloco(x, altura - 1, z, "areia", chunk);
                    ChunkUtil.defBloco(x, altura, z, "agua", chunk);
                } else {
                    ChunkUtil.defBloco(x, altura - 1, z, "grama", chunk);
                }
            break;
        }
    }
}

