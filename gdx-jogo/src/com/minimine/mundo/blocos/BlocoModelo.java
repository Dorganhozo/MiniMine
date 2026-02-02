package com.minimine.mundo.blocos;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.badlogic.gdx.graphics.Color;
import com.minimine.mundo.ChunkLuz;
import com.minimine.graficos.Render;

public class BlocoModelo {
    public static final float TAM = 1f; // tamanho
    // definições das faces(vertices + UVs)
    public static final float[][][] FACE_VERTICES = {
        // topo
        {
            {TAM, TAM, 0},
            {0, TAM, 0},
            {0, TAM, TAM},
            {TAM, TAM, TAM}
        },
        // baixo
        {
            {TAM, 0, TAM},
            {0, 0, TAM},
            {0, 0, 0},
            {TAM, 0, 0}
        },
        // +X
        {
            {TAM, 0, TAM},
            {TAM, 0, 0},
            {TAM, TAM, 0},
            {TAM, TAM, TAM}
        },
        // -X
        {
            {0, 0, 0},
            {0, 0, TAM},
            {0, TAM, TAM},
            {0, TAM, 0}
        },
        // +Z
        {
            {0, TAM, TAM},
            {0, 0, TAM},
            {TAM, 0, TAM},
            {TAM, TAM, TAM}
        },
        // -Z
        {
            {0, 0, 0},
            {0, TAM, 0},
            {TAM, TAM, 0},
            {TAM, 0, 0}
        }
    };
    public static final float[][][] FACE_UVS = {
        {{1,1}, {0,1}, {0,0}, {1,0}}, // topo
        {{1,0}, {0,0}, {0,1}, {1,1}}, // baixo
        {{1,1}, {0,1}, {0,0}, {1,0}}, // +X
        {{1,1}, {0,1}, {0,0}, {1,0}}, // -X
        {{0,0}, {0,1}, {1,1}, {1,0}}, // +Z
        {{0,1}, {0,0}, {1,0}, {1,1}}  // -Z
    };

    	public static void addFace(int faceId, int atlasId, float x, float y, float z, 
	float w, float h, float luzBloco, float luzSol, FloatArrayUtil verts, ShortArrayUtil idc) {

		float[] atlasCoords = Render.atlasUVs.get(atlasId);
		if(atlasCoords == null) return;

		final float uMin = atlasCoords[0];
		final float vMin = atlasCoords[1];
		final float uMax = atlasCoords[2];
		final float vMax = atlasCoords[3];

		// pre-calculo da cor pra evitar chamar Color.toFloatBits
		float multFace = ChunkLuz.FACE_LUZ[faceId];
		int r = (int)(luzBloco * multFace * 255);
		int g = (int)(luzSol * multFace * 255);
		int b = (int)(multFace * 255); 
		float corFinal = Color.toFloatBits(r, g, b, 255);

		short indiceBase = (short)(verts.tam / 10); // Agora sao 10 floats por vertice

        // Define escalas baseadas na face
        float sx = 1f, sy = 1f, sz = 1f; // escalas de posicao
        float uw = 1f, vh = 1f; // escalas de UV

        // Mapeamento:
        // Topo/Baixo (Faces 0, 1): w -> X, h -> Z
        // Lados X (Faces 2, 3): w -> Z, h -> Y
        // Lados Z (Faces 4, 5): w -> X, h -> Y
        
        switch(faceId) {
            case 0: case 1: sx = w; sz = h; uw = w; vh = h; break;
            case 2: case 3: sz = w; sy = h; uw = w; vh = h; break; // Check orientation
            case 4: case 5: sx = w; sy = h; uw = w; vh = h; break;
        }

        // Loop pelos 4 vertices
        for(int i = 0; i < 4; i++) {
            float[] v = FACE_VERTICES[faceId][i];
            float[] uv = FACE_UVS[faceId][i];

            // Posicao
            // Logica: Se o componente for TAM (1.0), multiplicamos pela escala daquela dimensao?
            // Nao exatamente. Faces deslocadas (ex: Topo Y=1) devem manter Y=1, nao Y=h.
            // Porem, faces "planas" tem 0 ou 1 nas coordenadas variaveis.
            // Ex: Topo varia X e Z. Y é fixo em 1.
            // Se v[0] (X) for 1, deve virar w. Se 0, vira 0. -> v[0] * sx da certo?
            // E o eixo fixo? Y=1. sy=1 (default). v[1]*sy = 1*1 = 1. Correto.
            // Entao basta multiplicar.
            
            verts.add(x + v[0] * sx);
            verts.add(y + v[1] * sy);
            verts.add(z + v[2] * sz);

            // UV Local (para tiling)
            // Multiplicamos o 0..1 original pelo tamanho (w ou h)
            verts.add(uv[0] * uw); 
            verts.add(uv[1] * vh);

            // Atlas Limits (uMin, vMin, uMax, vMax)
            verts.add(uMin); verts.add(vMin); verts.add(uMax); verts.add(vMax);

            // Cor
            verts.add(corFinal);
        }

		// indices(ordem dos triangulos)
		idc.add(indiceBase);
		idc.add((short)(indiceBase + 1));
		idc.add((short)(indiceBase + 2));
		idc.add((short)(indiceBase + 2));
		idc.add((short)(indiceBase + 3));
		idc.add(indiceBase);
	}
}
