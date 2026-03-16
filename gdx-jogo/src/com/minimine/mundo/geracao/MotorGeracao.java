package com.minimine.mundo.geracao;

import com.minimine.mundo.Chunk;
import com.minimine.mundo.ChunkUtil;
import com.minimine.mundo.Chave;
import com.minimine.mundo.Mundo;
import com.minimine.mundo.FluxoAgua;
import com.minimine.utils.ruidos.OpenSimplex2;
import com.minimine.mundo.blocos.Bloco;
/*
 * orquestrador de geração de chunk
 * thread-segura: toda a geração opera sobre ContextoGeracao local por thread

 * MotorGeracao, TerranoBase e GeradorRios são imutaveis após construção
 * contem apenas parametros e sementes
 * multiplas threads podem chamar
 * gerarChunk() simultaneamente sem concorrencia porque cada chamada usa seu proprio
 * ContextoGeracao via ThreadLocal

 *  fase 1: pré-calculo de ruido arrays
 *     persist -> base -> alt -> aSele -> subaquat -> crista -> calor -> umidade -> preenchimento
 *     todos os arrays populados antes de qualquer loop de blocos

 *   fase 2:
 *     for(z) for(x) for(y): pedra | escava canal de rio

 *   fase 3:
 *     passagem separada, por coluna: bioma por calor/umidade/altura, topo/subtopo/interior
 *     popula ctx.biomaMapa[] para uso futuro por GeradorDecoracoes

 *   fase 4:
 *     preenche água em blocos vazios abaixo do nível do mar

 *   fase 5: (antiga fase 3 de tuneis)
 *     escava tuneis

 *   fase 6: decoração
 *     por coluna acima do topo sólido:
 *       vegetacao: PseudoAleatorio por coluna, chance, ChunkUtil.defBloco direto
 *       estruturas: idem, coloca blocos com ids pré-resolvidos, apenas dentro do chunk

 * parametros de bioma
 *   aquecer: pos=0, escala=1, espalhar=1000, oct=3, persist=0.5, lac=2.0
 *   umidade: pos=0, escala=1, espalhar=1000, oct=3, persist=0.5, lac=2.0
 *   preenchimento_profundidade: pos=0, escala=1.2, espalhar=150, oct=3, persist=0.7, lac=2.0
 */
public final class MotorGeracao {
    public static final int NIVEL_MAR = 62;

    public final long semente;
    public final TerranoBase terreno;
    public final GeradorRios rios;
    public final GeradorTuneis tuneis;
    public final RegistroBiomas registro;

    // parametros de ruidos de bioma, imutaveis, compartilhaveis
    public final long semCalor, semUmidade, sempreenchimento;
    public final float espalharCalor, espalharUmidade, espalharPreen;
    public final int octCalor, octUmidade, octPreen;
    public final float perCalor, perUmidade, perPreen;
    public final float escalaPreen;

    // ContextoGeracao por thread, elimina alocação por chunk e race conditions
    public final ThreadLocal<ContextoGeracao> ctxLocal = new ThreadLocal<ContextoGeracao>() {
        @Override protected ContextoGeracao initialValue() {return new ContextoGeracao(Mundo.Y_CHUNK);}
    };

    public static int PEDRA, AGUA;

    public MotorGeracao(long semente, RegistroBiomas registro) {
        this.semente  = semente;
        this.registro = registro;
        this.terreno  = new TerranoBase(semente, NIVEL_MAR);
        this.rios = new GeradorRios(semente, NIVEL_MAR);
        this.tuneis = new GeradorTuneis(semente);

        semCalor = semente ^ 0xCAFEBABE87654321L;
        semUmidade = semente ^ 0x4F3C2B1A9E8D7C6BL;
        sempreenchimento = semente ^ 0xB3A4C5D6E7F80192L;
        espalharCalor = 1000f;
		octCalor = 3;
		perCalor = 0.5f;
        espalharUmidade = 1000f;
		octUmidade = 3;
		perUmidade = 0.5f;
        espalharPreen = 150f; 
		octPreen = 3;
		perPreen = 0.7f;
        escalaPreen = 1.2f;

        PEDRA = Bloco.texIds.get("pedra").tipo;
        AGUA = Bloco.texIds.get("agua").tipo;
    }

    // === ENTRADA PRINCIPAL ===
    public void gerarChunk(Chunk chunk) {
        final int chunkX = chunk.x << 4;
        final int chunkZ = chunk.z << 4;
        final ContextoGeracao ctx = ctxLocal.get();

        // === FASE 1: pré-calculo de todos os ruidos arrays ===
        terreno.calcularChunk(chunkX, chunkZ, ctx);
        rios.calcularChunk(chunkX, 0, chunkZ, Mundo.Y_CHUNK, ctx);

        calcular2D(semCalor, espalharCalor, octCalor, perCalor, 2.0f, chunkX, chunkZ, ctx.calorMapa);
        calcular2D(semUmidade, espalharUmidade,  octUmidade, perUmidade, 2.0f, chunkX, chunkZ, ctx.umidadeMapa);
        calcular2Dpreenchimento(chunkX, chunkZ, ctx.preenProfMapa);

        // normaliza calor e umidade para [0,1]
        for(int i = 0; i < 16 * 16; i++) {
            ctx.calorMapa[i] = Math.max(0f, Math.min(1f, ctx.calorMapa[i] * 0.5f + 0.5f));
            ctx.umidadeMapa[i] = Math.max(0f, Math.min(1f, ctx.umidadeMapa[i] * 0.5f + 0.5f));
        }

        // === FASE 2: gerar terreno ===
        int pedraSuperficieMaxY = 0;
        for(int z = 0; z < 16; z++) {
            for(int x = 0; x < 16; x++) {
                int superficieY = terreno.obterAltura(x, z, ctx);
                if(superficieY > pedraSuperficieMaxY) pedraSuperficieMaxY = superficieY;
                for(int y = 0; y < Mundo.Y_CHUNK; y++) {
                    if(y <= superficieY && !rios.eCanal(x, y, z, y, superficieY, ctx)) {
                        ChunkUtil.defBloco(x, y, z, PEDRA, chunk);
                    }
                }
            }
        }
        // === FASE 3: gerar vazios ===
        tuneis.escavar(chunk, chunkX, chunkZ);

        // === FASE 4: gerar biomas ===
        for(int z = 0; z < 16; z++) {
            for(int x = 0; x < 16; x++) {
                int idc2d = z * 16 + x;

                int topoColuna = terreno.obterAltura(x, z, ctx);
                while(topoColuna > 0 && ChunkUtil.obterBloco(x, topoColuna, z, chunk) == 0) topoColuna--;

                float calor = ctx.calorMapa[idc2d];
                float umidade = ctx.umidadeMapa[idc2d];
                calor = Math.max(0f, Math.min(1f, calor - (float)((topoColuna - NIVEL_MAR) * 0.004)));

                DadosBioma bioma = registro.selecionar(calor, umidade, topoColuna);
                ctx.biomaMapa[idc2d] = bioma;

                final DadosBioma.Superficie s = bioma.superficie;
                float preenchimentoVal = Math.max(0f, ctx.preenProfMapa[idc2d]);
                int profpreenchimento = s.profTopo + s.profSubtopo + (int)preenchimentoVal;

                int profAtual = 0;
                for(int y = topoColuna; y >= 1; y--) {
                    if(ChunkUtil.obterBloco(x, y, z, chunk) == 0) {
                        if(profAtual > 0) break;
                        continue;
                    }
                    if(profAtual < s.profTopo) {
                        ChunkUtil.defBloco(x, y, z, s.topo, chunk);
                    } else if(profAtual < profpreenchimento) {
                        ChunkUtil.defBloco(x, y, z, s.subtopo, chunk);
                    } else {
                        ChunkUtil.defBloco(x, y, z, s.interior, chunk);
                        break;
                    }
                    profAtual++;
                }
            }
        }
        // === FASE 5: preencher água ===
        for(int z = 0; z < 16; z++) {
            for(int x = 0; x < 16; x++) {
                for(int y = NIVEL_MAR; y >= 0; y--) {
                    if(ChunkUtil.obterBloco(x, y, z, chunk) == 0) {
                        ChunkUtil.defBloco(x, y, z, AGUA, chunk);
                        ChunkUtil.defMeta(x, y, z, (short)FluxoAgua.NIVEL_FONTE, chunk);
                    }
                }
            }
        }
        // === FASE 6: decoração executada externamente por Mundo.decorarDados()
        // após vizinhos atingirem estado >= 1, permitindo escrita em chunks vizinhas
        chunk.dadosProntos = true;
    }
    /*
     * decorar: coloca vegetação e estruturas sobre a superficie do chunk
     * opera APENAS dentro dos limites locais [0..15] x [0..Y_CHUNK-1] x [0..15]
     * sem Mundo.defBlocoMundo: nenhum efeito colateral fora do chunk
     * semente por coluna derivada deterministicamente de(semente ^ x ^ z*31)
     */
    public void decorar(Chunk chunk, int chunkX, int chunkZ, ContextoGeracao ctx) {
        for(int z = 0; z < 16; z++) {
            for(int x = 0; x < 16; x++) {
                int idc2d = z * 16 + x;
                DadosBioma bioma = ctx.biomaMapa[idc2d];
                if(bioma == null) continue;

                // topo sólido da coluna
                int topo = Mundo.Y_CHUNK - 1;
                while(topo > 0 && ChunkUtil.obterBloco(x, topo, z, chunk) == 0) topo--;

                // não decora sob água
                if(topo <= NIVEL_MAR) continue;
                // posição de colocação: bloco acima do topo solido
                int yDec = topo + 1;
                if(yDec >= Mundo.Y_CHUNK) continue;

                // seed determinística por coluna
                long semCol = semente ^ ((chunkX + x) * 374761393L) ^ ((chunkZ + z) * 668265263L);

                // === vegetação ===
                DadosBioma.EntradaVegetacao[] veg = bioma.vegetacao;
                for(int i = 0; i < veg.length; i++) {
                    semCol = lcg(semCol);
                    float r = (semCol >>> 1) / (float)(Long.MAX_VALUE);
                    if(r < veg[i].chance) {
                        ChunkUtil.defBloco(x, yDec, z, veg[i].id, chunk);
                        break; // so uma vegetação por coluna
                    }
                }
                // === estruturas ===
                DadosBioma.EntradaEstrutura[] estr = bioma.estruturas;
                for(int i = 0; i < estr.length; i++) {
                    semCol = lcg(semCol);
                    float r = (semCol >>> 1) / (float)(Long.MAX_VALUE);
                    if(r < estr[i].chance) {
                        // bloco de superficie deve ser o esperado pela estrutura
                        if(estr[i].blocoBaixo >= 0 && ChunkUtil.obterBloco(x, topo, z, chunk) != estr[i].blocoBaixo) break;
                        // yDec deve estar livre (galhos de outra arvore vizinha podem ja ocupar)
                        if(ChunkUtil.obterBloco(x, yDec, z, chunk) != 0) break;
                        colocarEstrutura(estr[i], x, topo, z, chunk);
                        break;
                    }
                }
            }
        }
    }
    /*
     * coloca uma estrutura a partir da ancora(ox, oy, oz) em coordenadas locais do chunk
     * blocos dentro do chunk são escritos diretamente
     * blocos fora são escritos na chunk vizinha se ela existir e tiver dadosProntos
     */
    private void colocarEstrutura(DadosBioma.EntradaEstrutura e, int ox, int oy, int oz, Chunk chunk) {
        for(int i = 0; i < e.lx.length; i++) {
            int id = e.blocoIds[i];
            if(id < 0) continue;
            int bx = ox + (e.lx[i] - e.ancX);
            int by = oy + (e.ly[i] - e.ancY);
            int bz = oz + (e.lz[i] - e.ancZ);
            if(by < 0 || by >= Mundo.Y_CHUNK) continue;
            if(bx >= 0 && bx <= 15 && bz >= 0 && bz <= 15) {
                ChunkUtil.defBloco(bx, by, bz, id, chunk);
                if(e.blocoMeta[i] != 0) ChunkUtil.defMeta(bx, by, bz, e.blocoMeta[i], chunk);
            } else {
                int gx = (chunk.x << 4) + bx;
                int gz = (chunk.z << 4) + bz;
                Chunk alvo = Mundo.chunks.get(Chave.calcularChave(gx >> 4, gz >> 4));
                if(alvo == null || !alvo.dadosProntos) continue;
                ChunkUtil.defBloco(gx & 0xF, by, gz & 0xF, id, alvo);
                if(e.blocoMeta[i] != 0) ChunkUtil.defMeta(gx & 0xF, by, gz & 0xF, e.blocoMeta[i], alvo);
            }
        }
    }

    // gerador congruente linear
    public static final long lcg(long s) {
        return s * 6364136223846793005L + 1442695040888963407L;
    }

    // === UTILITARIOS ===
    public void calcular2D(long sem, float espalhar, int oct, float persist, float lac,
	int origemX, int origemZ, float[] saida) {
        float freq = 1.0f / espalhar;
        for(int z = 0; z < 16; z++) {
            for(int x = 0; x < 16; x++) {
                saida[(z << 4) * x] = OpenSimplex2.ruido2Fractal(
                    sem, (origemX + x) * freq, (origemZ + z) * freq, oct, persist, lac);
            }
        }
    }

    public void calcular2Dpreenchimento(int origemX, int origemZ, float[] saida) {
        float freq = 1.0f / espalharPreen;
        for(int z = 0; z < 16; z++) {
            for(int x = 0; x < 16; x++) {
                saida[(z << 4) + x] = escalaPreen * OpenSimplex2.ruido2Fractal(
                    sempreenchimento, (origemX + x) * freq, (origemZ + z) * freq, octPreen, perPreen, 2.0f);
            }
        }
    }

    // ponto a ponto, uso esporadico
    public String obterBioma(int mx, int mz) {
        int alt = terreno.calcularAlturaPonto(mx, mz);
        float cal = Math.max(0f, Math.min(1f,
		OpenSimplex2.ruido2Fractal(semCalor, mx / espalharCalor, mz / espalharCalor,
		octCalor, perCalor, 2.0f) * 0.5f + 0.5f - ((alt - NIVEL_MAR) * 0.004f)));
        float umi = Math.max(0f, Math.min(1f,
		OpenSimplex2.ruido2Fractal(semUmidade, mx / espalharUmidade, mz / espalharUmidade,
		octUmidade, perUmidade, 2.0f) * 0.5f + 0.5f));
        return registro.selecionar(cal, umi, alt).nome;
    }

    public int[] localizarBioma(String chave, int origemX, int origemZ) {
        if(!registro.existe(chave)) return new int[]{0, 0};
        int passo = 64, raioMax = 100_000;
        for(int raio = passo; raio <= raioMax; raio += passo) {
            for(int dx = -raio; dx <= raio; dx += passo) {
                for(int dz = -raio; dz <= raio; dz += passo) {
                    if(Math.abs(dx) != raio && Math.abs(dz) != raio) continue;
                    int mx = origemX + dx, mz = origemZ + dz;
                    int alt = terreno.calcularAlturaPonto(mx, mz);
                    float cal = Math.max(0f, Math.min(1f,
					OpenSimplex2.ruido2Fractal(semCalor, mx / espalharCalor, mz / espalharCalor,
					octCalor, perCalor, 2.0f) * 0.5f + 0.5f - ((alt - NIVEL_MAR) * 0.004f)));
                    float umi = Math.max(0f, Math.min(1f,
					OpenSimplex2.ruido2Fractal(semUmidade, mx / espalharUmidade, mz / espalharUmidade,
					octUmidade, perUmidade, 2.0f) * 0.5f + 0.5f));
                    if(registro.selecionar(cal, umi, alt).chave.equals(chave)) return new int[]{mx, mz};
                }
            }
        }
        return new int[]{0, 0};
    }
}


