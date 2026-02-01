package com.minimine.utils.chunks;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;
import com.minimine.utils.blocos.Bloco;
import com.minimine.utils.blocos.BlocoModelo;

public class ChunkMalha {
    public static void attMalha(Chunk chunk, FloatArrayUtil vertsGeral, ShortArrayUtil idcSolidos, ShortArrayUtil idcTransp) {
        ChunkLuz.attLuz(chunk);

        Chunk chunkXP, chunkXN, chunkZP, chunkZN;
        Chave c = new Chave(chunk.x + 1, chunk.z);
        chunkXP = Mundo.chunks.get(c);
        c.x = chunk.x - 1; c.z = chunk.z;
        chunkXN = Mundo.chunks.get(c);
        c.x = chunk.x; c.z = chunk.z + 1;
        chunkZP = Mundo.chunks.get(c);
        c.x = chunk.x; c.z = chunk.z - 1;
        chunkZN = Mundo.chunks.get(c);

        for(int idc = 0; idc < Mundo.CHUNK_AREA * Mundo.Y_CHUNK; idc++) {
            int x = idc & 0xF;
            int z = (idc >> 4) & 0xF;
            int y = idc >> 8;

            int blocoId = ChunkUtil.obterBloco(x, y, z, chunk);
            if(blocoId == 0) continue;

            Bloco blocoTipo = Bloco.numIds.get(blocoId);
            if(blocoTipo == null) continue;

            // se o bloco for transparente vai pra lista transparente
            if(blocoTipo.transparente) {
                lidarFacesDoBloco(
                    x, y, z, blocoTipo,
                    chunk, chunkXP, chunkXN, chunkZP, chunkZN,
                    vertsGeral, idcTransp);
            } else {
                lidarFacesDoBloco(
                    x, y, z, blocoTipo,
                    chunk, chunkXP, chunkXN, chunkZP, chunkZN,
                    vertsGeral, idcSolidos);
            }
        }
    }
	
	public static void lidarFacesDoBloco(
		int x, int y, int z, Bloco bloco,
		Chunk c, Chunk cXP, Chunk cXN, Chunk cZP, Chunk cZN,
		FloatArrayUtil verts, ShortArrayUtil idc) {

		int vizId;
		byte luzTotal;
		float lb, ls;
		Bloco bViz;

		// face 0: cima(Y + 1)
		int ny = y + 1;
		if(ny >= Mundo.Y_CHUNK) {
			// limite do mundo(céu), sempre desenha
			BlocoModelo.addFace(0, bloco.texturaId(0), x, y, z, 1.0f, 1.0f, verts, idc);
		} else {
			// ta dentro do mesmo chunk(Y não muda chunk vizinho horizontal)
			vizId = ChunkUtil.obterBloco(x, ny, z, c);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);

			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x, ny, z, c);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(0, bloco.texturaId(0), x, y, z, lb, ls, verts, idc);
			}
		}
		// face 1: baixo(Y - 1)
		ny = y - 1;
		if(ny < 0) {
			// desenha com luz zero ou ambiente
			BlocoModelo.addFace(1, bloco.texturaId(1), x, y, z, 0.0f, 0.0f, verts, idc);
		} else {
			vizId = ChunkUtil.obterBloco(x, ny, z, c);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);

			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x, ny, z, c);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(1, bloco.texturaId(1), x, y, z, lb, ls, verts, idc);
			}
		}
		// face 2: leste(X + 1)
		// se x < 15, vizinho ta em C; se x == 15, vizinho ta em cXP na posição 0
		if(x < 15) {
			vizId = ChunkUtil.obterBloco(x + 1, y, z, c);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x + 1, y, z, c);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(2, bloco.texturaId(2), x, y, z, lb, ls, verts, idc);
			}
		} else if(cXP != null) {
			// Borda: Lê do vizinho cXP na coordenada 0
			vizId = ChunkUtil.obterBloco(0, y, z, cXP);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(0, y, z, cXP);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(2, bloco.texturaId(2), x, y, z, lb, ls, verts, idc);
			}
		}
		// face 3: oeste(X - 1)
		// se x > 0, vizinho ta em C; se x == 0, vizinho ta em cXN na posição 15
		if(x > 0) {
			vizId = ChunkUtil.obterBloco(x - 1, y, z, c);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x - 1, y, z, c);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(3, bloco.texturaId(3), x, y, z, lb, ls, verts, idc);
			}
		} else if(cXN != null) {
			// borda: le do vizinho cXN na coordenada 15
			vizId = ChunkUtil.obterBloco(15, y, z, cXN);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(15, y, z, cXN);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(3, bloco.texturaId(3), x, y, z, lb, ls, verts, idc);
			}
		}
		// face 4: sul(Z + 1)
		// se z < 15, vizinho tá em C; se z == 15, vizinho ta em cZP na posição 0
		if(z < 15) {
			vizId = ChunkUtil.obterBloco(x, y, z + 1, c);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x, y, z + 1, c);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(4, bloco.texturaId(4), x, y, z, lb, ls, verts, idc);
			}
		} else if(cZP != null) {
			// borda: le do vizinho cZP na coordenada 0
			vizId = ChunkUtil.obterBloco(x, y, 0, cZP);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x, y, 0, cZP);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(4, bloco.texturaId(4), x, y, z, lb, ls, verts, idc);
			}
		}
		// face 5: norte(Z - 1)
		// se z > 0, vizinho tá em C; se z == 0, vizinho tá em cZN na posição 15
		if(z > 0) {
			vizId = ChunkUtil.obterBloco(x, y, z - 1, c);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x, y, z - 1, c);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(5, bloco.texturaId(5), x, y, z, lb, ls, verts, idc);
			}
		} else if(cZN != null) {
			// borda: le do vizinho cZN na coordenada 15
			vizId = ChunkUtil.obterBloco(x, y, 15, cZN);
			bViz = (vizId == 0) ? null : Bloco.numIds.get(vizId);
			if(deveRenderFace(bloco, bViz)) {
				luzTotal = ChunkUtil.obterLuzCompleta(x, y, 15, cZN);
				lb = (luzTotal & 0x0F) / 15f;
				ls = ((luzTotal >> 4) & 0x0F) / 15f;
				BlocoModelo.addFace(5, bloco.texturaId(5), x, y, z, lb, ls, verts, idc);
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
