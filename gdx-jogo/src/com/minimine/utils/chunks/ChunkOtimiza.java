package com.minimine.utils.chunks;

import com.minimine.utils.blocos.BlocoModelo;
import com.minimine.utils.blocos.Bloco;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;

public class ChunkOtimiza {
	public static void lidarFacesDoBloco(int x, int y, int z, Bloco blocoTipo,
										 Chunk chunk, Chunk chunkXP, Chunk chunkXN, 
										 Chunk chunkZP, Chunk chunkZN, FloatArrayUtil verts, ShortArrayUtil idc) {
		float posX = x * 1f;
		float posY = y * 1f;
		float posZ = z * 1f;
		if(deveRenderFaceTopo(x, y + 1, z, chunk, blocoTipo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 0, chunk);
			int textureId = blocoTipo.texturaId(0);
			BlocoModelo.addFace(0, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceBaixo(x, y - 1, z, chunk, blocoTipo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 1, chunk);
			int textureId = blocoTipo.texturaId(1);
			BlocoModelo.addFace(1, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceXPositivo(x + 1, y, z, chunk, chunkXP, blocoTipo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 2, chunk);
			int textureId = blocoTipo.texturaId(2);
			BlocoModelo.addFace(2, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceXNegativo(x - 1, y, z, chunk, chunkXN, blocoTipo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 3, chunk);
			int textureId = blocoTipo.texturaId(3);
			BlocoModelo.addFace(3, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceZPositivo(x, y, z + 1, chunk, chunkZP, blocoTipo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 4, chunk);
			int textureId = blocoTipo.texturaId(4);
			BlocoModelo.addFace(4, textureId, posX, posY, posZ, luz, verts, idc);
		}
		if(deveRenderFaceZNegativo(x, y, z - 1, chunk, chunkZN, blocoTipo)) {
			float luz = ChunkLuz.calcularNivelLuz(x, y, z, 5, chunk);
			int textureId = blocoTipo.texturaId(5);
			BlocoModelo.addFace(5, textureId, posX, posY, posZ, luz, verts, idc);
		}
	}

	public static boolean deveRenderFaceTopo(int x, int y, int z, Chunk chunk, Bloco blocoAtual) {
		if(y >= Mundo.Y_CHUNK) return true;
		Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
		return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
	}

	public static boolean deveRenderFaceBaixo(int x, int y, int z, Chunk chunk, Bloco blocoAtual) {
		if(y < 0) return true;
		Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
		return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
	}

	public static boolean deveRenderFaceXPositivo(int x, int y, int z, Chunk chunk, Chunk chunkXP, Bloco blocoAtual) {
		if(x >= Mundo.TAM_CHUNK) {
			if(chunkXP == null) return true;
			Bloco adjacente = ChunkUtil.obterblocoTipo(0, y, z, chunkXP, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveRenderFaceXNegativo(int x, int y, int z, Chunk chunk, Chunk chunkXN, Bloco blocoAtual) {
		if(x < 0) {
			if(chunkXN == null) return true;
			Bloco adjacente = ChunkUtil.obterblocoTipo(Mundo.TAM_CHUNK - 1, y, z, chunkXN, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveRenderFaceZPositivo(int x, int y, int z, Chunk chunk, Chunk chunkZP, Bloco blocoAtual) {
		if(z >= Mundo.TAM_CHUNK) {
			if (chunkZP == null) return true;
			Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, 0, chunkZP, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveRenderFaceZNegativo(int x, int y, int z, Chunk chunk, Chunk chunkZN, Bloco blocoAtual) {
		if(z < 0) {
			if(chunkZN == null) return true;
			Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, Mundo.TAM_CHUNK - 1, chunkZN, null);
			return adjacente == null || !adjacente.solido || blocoAtual.transparente;
		} else {
			Bloco adjacente = ChunkUtil.obterblocoTipo(x, y, z, chunk, null);
			return adjacente == null || !deveOcultarFace(blocoAtual, adjacente);
		}
	}

	public static boolean deveOcultarFace(Bloco blocoAtual, Bloco blocoAdjacente) {
		if(blocoAdjacente == null) return false;
		// ocultam faces entre si
		if(blocoAtual.cullingAlto && blocoAdjacente.cullingAlto) {
			return true;
		}
		// normais se ocultam
		if(blocoAtual.solido && blocoAdjacente.solido) {
			return true;
		}
		// outros transparentes não se ocultam entre tipos diferentes
		if(blocoAtual.transparente && blocoAdjacente.transparente) {
			return blocoAtual.tipo == blocoAdjacente.tipo;
		}
		return false;
	}
}
