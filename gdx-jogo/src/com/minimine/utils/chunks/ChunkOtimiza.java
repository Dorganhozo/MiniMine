package com.minimine.utils.chunks;

import com.minimine.utils.blocos.Bloco;
import com.minimine.utils.blocos.BlocoModelo;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;

public class ChunkOtimiza {
    public static void lidarFacesDoBloco(
        int x, int y, int z, Bloco bloco,
        Chunk c, Chunk cXP, Chunk cXN, Chunk cZP, Chunk cZN,
        FloatArrayUtil verts, ShortArrayUtil idc) {
		
        float luz = (float) ChunkUtil.obterLuz(x, y, z, c) / 15f;

        // verifica as 6 direções
        // topo(face 0)
        if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x, y + 1, z, c, null))) {
            BlocoModelo.addFace(0, bloco.topo, x, y, z, luz, verts, idc);
        }
        // baixo(face 1)
        if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x, y - 1, z, c, null))) {
            BlocoModelo.addFace(1, bloco.baixo, x, y, z, luz, verts, idc);
        }
        // +X(face 2)
        if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x + 1, y, z, c, cXP))) {
            BlocoModelo.addFace(2, bloco.lados, x, y, z, luz, verts, idc);
        }
        // -X(face 3)
        if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x - 1, y, z, c, cXN))) {
            BlocoModelo.addFace(3, bloco.lados, x, y, z, luz, verts, idc);
        }
        // +Z(face 4)
        if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x, y, z + 1, c, cZP))) {
            BlocoModelo.addFace(4, bloco.lados, x, y, z, luz, verts, idc);
        }
        // -Z(face 5)
        if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x, y, z - 1, c, cZN))) {
            BlocoModelo.addFace(5, bloco.lados, x, y, z, luz, verts, idc);
        }
    }

    public static boolean deveRenderFace(Bloco atual, Bloco vizinho) {
        // se não tem vizinho(ar), desenha a face
        if(vizinho == null) return true;
		
        // se o vizinho for do mesmo tipo, oculta
        if(atual.tipo == vizinho.tipo) return false;

        // se o vizinho for opaco(não transparente), ele esconde a face do atual
        if(!vizinho.transparente) return false;

        // se o atual é opaco e o vizinho é transparente, renderiza
        return true;
    }
}
