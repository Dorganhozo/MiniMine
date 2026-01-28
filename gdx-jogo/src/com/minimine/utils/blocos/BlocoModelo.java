package com.minimine.utils.blocos;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;
import com.badlogic.gdx.graphics.Color;
import com.minimine.utils.chunks.ChunkLuz;

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
	float luzBloco, float luzSol, FloatArrayUtil verts, ShortArrayUtil idc) {
		float[] atlasCoords = Mundo.atlasUVs.get(atlasId);
		if(atlasCoords == null) return;

		float u_min = atlasCoords[0];
		float v_min = atlasCoords[1];
		float u_max = atlasCoords[2];
		float v_max = atlasCoords[3];

		// aplica sombra por face(falso AO)
		// usa o multiplicador da face pra os lados ficarem mais escuros que o topo
		float multFace = ChunkLuz.FACE_LUZ[faceId];

		// compressão nos canais de cor
		// R: luz de bloco
		// G: luz do ceu(exposição solar)
		// B: 1.0
		int r = (int)(luzBloco * multFace * 255);
		int g = (int)(luzSol * multFace * 255);
		int b = (int)(multFace * 255); 
		int a = 255;

		// converte pra o float que o OpenGL entende como cor(ABGR)
		float corEmpacotada = Color.toFloatBits(r, g, b, a);

		short vertConta = (short)(verts.tam / 6); // 6 é o passo(x, y, z, u, v, cor)

		for(int i = 0; i < 4; i++) {
			float[] vert = FACE_VERTICES[faceId][i];
			float[] uv = FACE_UVS[faceId][i];

			// posição
			verts.add(x + vert[0]); 
			verts.add(y + vert[1]); 
			verts.add(z + vert[2]);

			// textura
			verts.add(u_min + uv[0] * (u_max - u_min));
			verts.add(v_min + uv[1] * (v_max - v_min));

			// cor(contendo os dois canais de luz)
			verts.add(corEmpacotada);
		}
		// indices(triangulos)
		idc.add((short)(vertConta + 0));
		idc.add((short)(vertConta + 1));
		idc.add((short)(vertConta + 2));
		idc.add((short)(vertConta + 2));
		idc.add((short)(vertConta + 3));
		idc.add((short)(vertConta + 0));
	}
}
