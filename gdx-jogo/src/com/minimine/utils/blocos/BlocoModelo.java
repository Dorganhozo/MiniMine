package com.minimine.utils.blocos;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.minimine.cenas.Mundo;
import com.badlogic.gdx.graphics.Color;

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

    public static void addFace(int faceId, int atlasId, float x, float y, float z, float nivelLuz, FloatArrayUtil verts, ShortArrayUtil idc) {
        float[] atlasCoords = Mundo.atlasUVs.get(atlasId);
        if(atlasCoords == null) return;

        float u_min = atlasCoords[0];
        float v_min = atlasCoords[1];
        float u_max = atlasCoords[2];
        float v_max = atlasCoords[3];
        // calcula cor baseada na luz
        int r = (int)(nivelLuz * 255);
        int g = (int)(nivelLuz * 255);
        int b = (int)(nivelLuz * 255);
        int a = 255;
        float cor = Color.toFloatBits(r, g, b, a);
		
        short vertConta = (short)(verts.tam / 6);
        // add vertices:
        for(int i = 0; i < 4; i++) {
            float[] vert = FACE_VERTICES[faceId][i];
            float vx = x + vert[0];
            float vy = y + vert[1];
            float vz = z + vert[2];

            float[] uv = FACE_UVS[faceId][i];
            float u = u_min + uv[0] * (u_max - u_min);
            float v = v_min + uv[1] * (v_max - v_min);

            verts.add(vx); verts.add(vy); verts.add(vz);
            verts.add(u); verts.add(v);
            verts.add(cor);
        }
        // add indices(triangulos)
        idc.add((short)(vertConta + 0));
        idc.add((short)(vertConta + 1));
        idc.add((short)(vertConta + 2));
        idc.add((short)(vertConta + 2));
        idc.add((short)(vertConta + 3));
        idc.add((short)(vertConta + 0));
    }
}
