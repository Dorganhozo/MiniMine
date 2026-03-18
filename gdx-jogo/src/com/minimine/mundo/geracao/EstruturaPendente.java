package com.minimine.mundo.geracao;

import com.minimine.mundo.Chunk;
import com.minimine.mundo.ChunkUtil;
/*
 * um unico bloco de estrutura que extrapolou os limites da chunk de origem
 * e precisa ser escrito na chunk alvo quando ela atingir estado 1
 
 * os ids ja estão resolvidos(mesma resolução feita em EntradaEstrutura),
 * e as coordenadas ja estão em espaço local da chunk alvo
 */
public final class EstruturaPendente {
    public final int lx, ly, lz;
    public final int id;
    public final short meta;

    public EstruturaPendente(int lx, int ly, int lz, int id, short meta) {
        this.lx = lx;
        this.ly = ly;
        this.lz = lz;
        this.id = id;
        this.meta = meta;
    }

    public void aplicar(final Chunk alvo) {
        if(ly < 0 || ly >= 256) return;
        ChunkUtil.defBloco(lx, ly, lz, id, alvo);
        if(meta != 0) ChunkUtil.defMeta(lx, ly, lz, meta, alvo);
    }
}

