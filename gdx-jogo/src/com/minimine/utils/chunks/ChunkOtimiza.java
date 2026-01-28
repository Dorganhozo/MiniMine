package com.minimine.utils.chunks;

import com.minimine.utils.blocos.Bloco;
import com.minimine.utils.blocos.BlocoModelo;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;

public class ChunkOtimiza {
    public static void lidarFacesDoBloco(
		int x, int y, int z, Bloco bloco,
		Chunk c, Chunk cXP, Chunk cXN, Chunk cZP, Chunk cZN,
		FloatArrayUtil verts, ShortArrayUtil idc) {

		// define as direções pra facilitar a amostragem da luz
		int[] dx = {0, 0, 1, -1, 0, 0};
		int[] dy = {1, -1, 0, 0, 0, 0};
		int[] dz = {0, 0, 0, 0, 1, -1};

		for(int face = 0; face < 6; face++) {
			int nx = x + dx[face];
			int ny = y + dy[face];
			int nz = z + dz[face];

			// decide qual chunk usar para ler a luz do vizinho
			Chunk alvo = c;
			if(nx < 0) {
				alvo = cXN;
				nx = 15;
			} else if(nx > 15) {
				alvo = cXP;
				nx = 0;
			}
			if(nz < 0) {
				alvo = cZN;
				nz = 15;
			} else if(nz > 15) {
				alvo = cZP;
				nz = 0;
			}
			// so renderiza e calcula luz se a face tiver exposta
			if(deveRenderFace(bloco, ChunkUtil.obterblocoTipo(x + dx[face], y + dy[face], z + dz[face], c, alvo))) {
				// pega a luz do vizinho
				byte luzTotal = (ny >= 0 && ny < Mundo.Y_CHUNK && alvo != null) 
					? ChunkUtil.obterLuzCompleta(nx, ny, nz, alvo) 
					: (byte) 0;

				float lb = (luzTotal & 0x0F) / 15f;
				float ls = ((luzTotal >> 4) & 0x0F) / 15f;

				// faceId, textura, x, y, z, luzTocha, luzSol, verts, idc
				BlocoModelo.addFace(face, bloco.texturaId(face), x, y, z, lb, ls, verts, idc);
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
