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
	float luzBloco, float luzSol, FloatArrayUtil verts, ShortArrayUtil idc) {

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

		short indiceBase = (short)(verts.tam / 6);

		// vertice 0
		float[] v0 = FACE_VERTICES[faceId][0];
		float[] uv0 = FACE_UVS[faceId][0];
		verts.add(x + v0[0]); verts.add(y + v0[1]); verts.add(z + v0[2]);
		verts.add(uMin + uv0[0] * (uMax - uMin)); verts.add(vMin + uv0[1] * (vMax - vMin));
		verts.add(corFinal);

		// vertice 1
		float[] v1 = FACE_VERTICES[faceId][1];
		float[] uv1 = FACE_UVS[faceId][1];
		verts.add(x + v1[0]); verts.add(y + v1[1]); verts.add(z + v1[2]);
		verts.add(uMin + uv1[0] * (uMax - uMin)); verts.add(vMin + uv1[1] * (vMax - vMin));
		verts.add(corFinal);

		// vertice 2
		float[] v2 = FACE_VERTICES[faceId][2];
		float[] uv2 = FACE_UVS[faceId][2];
		verts.add(x + v2[0]); verts.add(y + v2[1]); verts.add(z + v2[2]);
		verts.add(uMin + uv2[0] * (uMax - uMin)); verts.add(vMin + uv2[1] * (vMax - vMin));
		verts.add(corFinal);

		// vertice 3
		float[] v3 = FACE_VERTICES[faceId][3];
		float[] uv3 = FACE_UVS[faceId][3];
		verts.add(x + v3[0]); verts.add(y + v3[1]); verts.add(z + v3[2]);
		verts.add(uMin + uv3[0] * (uMax - uMin)); verts.add(vMin + uv3[1] * (vMax - vMin));
		verts.add(corFinal);

		// indices(ordem dos triangulos)
		idc.add(indiceBase);
		idc.add((short)(indiceBase + 1));
		idc.add((short)(indiceBase + 2));
		idc.add((short)(indiceBase + 2));
		idc.add((short)(indiceBase + 3));
		idc.add(indiceBase);
	}
}
