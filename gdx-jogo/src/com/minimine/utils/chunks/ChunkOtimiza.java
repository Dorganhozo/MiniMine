package com.minimine.utils.chunks;

import com.minimine.utils.blocos.BlocoModelo;
import com.minimine.utils.blocos.Bloco;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;

public class ChunkOtimiza {
    // lida com a logica de limite da chunk e troca para a chunk vizinha
    public static Bloco obterVizinho(int x, int y, int z, Chunk chunk, Chunk vizinhoChunk) {
        // 1. checagem de limites do mundo(eixo Y)
        if(y < 0 || y >= Mundo.Y_CHUNK) return null; // força a renderização da face no limite do mundo
        // 2. checagem se o vizinho ta no chunk atual
        // se as coordenadas(x, z) estão dentro dos limites [0, TAM_CHUNK), é na chunk atual
        if(x >= 0 && x < Mundo.TAM_CHUNK && z >= 0 && z < Mundo.TAM_CHUNK) {
            // o arg "null'"pra vizinhoChunk é usado quando busca internamente na chunk
            return ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
        }
        // 3. checagem de vizinhos(eixos X e Z)
        if(vizinhoChunk == null) return null; // se deveria tiver uma chunk vizinha, mas ela não ta carregada
        // calcular coordenadas locais(nx, nz) na chunk vizinha
        int nx = x;
        int nz = z;

        if(x >= Mundo.TAM_CHUNK) nx = 0; // x+1 no limite da chunk atual -> (0, y, z) na chunk vizinha
        else if(x < 0) nx = Mundo.TAM_CHUNK - 1; // x-1 no limite da chunk atual -> (TAM_CHUNK - 1, y, z) na chunk vizinha
        
        if(z >= Mundo.TAM_CHUNK) nz = 0; // z+1 no limite da chunk atual -> (x, y, 0) na chunk vizinha
        else if(z < 0) nz = Mundo.TAM_CHUNK - 1; // z-1 no limite da chunk atual -> (x, y, TAM_CHUNK - 1) na chunk vizinha
        // usamos a chunk vizinha pra buscar o bloco
        return ChunkUtil.obterblocoTipo(nx, y, nz, vizinhoChunk, null);
    }
	
	public static void lidarFacesDoBloco(int x, int y, int z, Bloco blocoTipo,
	Chunk chunk, Chunk chunkXP, Chunk chunkXN,
	Chunk chunkZP, Chunk chunkZN, FloatArrayUtil verts, ShortArrayUtil idc) {
        // pega todos os 6 vizinhos
        Bloco adjTopo = obterVizinho(x, y + 1, z, chunk, null); 
        Bloco adjBaixo = obterVizinho(x, y - 1, z, chunk, null);
        Bloco adjXP = obterVizinho(x + 1, y, z, chunk, chunkXP);
        Bloco adjXN = obterVizinho(x - 1, y, z, chunk, chunkXN);
        Bloco adjZP = obterVizinho(x, y, z + 1, chunk, chunkZP);
        Bloco adjZN = obterVizinho(x, y, z - 1, chunk, chunkZN);

		if(!deveOcultarFace(blocoTipo, adjTopo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 0, chunk);
			int textureId = blocoTipo.texturaId(0);
			BlocoModelo.addFace(0, textureId, x, y, z, luz, verts, idc);
		}
		if(!deveOcultarFace(blocoTipo, adjBaixo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 1, chunk);
			int textureId = blocoTipo.texturaId(1);
			BlocoModelo.addFace(1, textureId, x, y, z, luz, verts, idc);
		}
		if(!deveOcultarFace(blocoTipo, adjXP)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 2, chunk);
			int textureId = blocoTipo.texturaId(2);
			BlocoModelo.addFace(2, textureId, x, y, z, luz, verts, idc);
		}
		if(!deveOcultarFace(blocoTipo, adjXN)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 3, chunk);
			int textureId = blocoTipo.texturaId(3);
			BlocoModelo.addFace(3, textureId, x, y, z, luz, verts, idc);
		}
		if(!deveOcultarFace(blocoTipo, adjZP)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 4, chunk);
			int textureId = blocoTipo.texturaId(4);
			BlocoModelo.addFace(4, textureId, x, y, z, luz, verts, idc);
		}
		if(!deveOcultarFace(blocoTipo, adjZN)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 5, chunk);
			int textureId = blocoTipo.texturaId(5);
			BlocoModelo.addFace(5, textureId, x, y, z, luz, verts, idc);
		}
	}

	public static boolean deveOcultarFace(Bloco blocoAtual, Bloco blocoAdjacente) {
		if(blocoAdjacente == null) return false;
		// normais se ocultam
		if(blocoAtual.culling && blocoAdjacente.culling) return true;
		// outros transparentes não se ocultam entre tipos diferentes
		if(blocoAtual.transparente && blocoAdjacente.transparente) {
			return blocoAtual.tipo == blocoAdjacente.tipo;
		}
		return false;
	}
}
