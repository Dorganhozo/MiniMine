package com.minimine.mundo.blocos;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.minimine.mundo.ChunkLuz;
import com.minimine.graficos.Texturas;

public class BlocoModelo {
    public static final float TAM = 1f;
    // X, Y, Z (3) + U, V (2) + TexID (1) + Cor (1)
    public static final int FLOATS_VERTICE = 7; 
    // cache pra mapear nomes -> IDs numericos pro Shader
    public static final ObjectIntMap<String> mapaTexturas = new ObjectIntMap<>();
    // array plano pra enviar como Uniform(4 floats por textura: u, v, u2, v2)
    // suporta até 256 texturas unicas por enquanto.
    public static final float[] dadosAtlas = new float[256 * 4]; 
    public static int contaTexturas = 0;

    public static final float[][][] FACE_VERTICES = {
        {{TAM, TAM, 0}, {0, TAM, 0}, {0, TAM, TAM}, {TAM, TAM, TAM}}, // topo
        {{TAM, 0, TAM}, {0, 0, TAM}, {0, 0, 0}, {TAM, 0, 0}},         // baixo
        {{TAM, 0, TAM}, {TAM, 0, 0}, {TAM, TAM, 0}, {TAM, TAM, TAM}}, // +X
        {{0, 0, 0}, {0, 0, TAM}, {0, TAM, TAM}, {0, TAM, 0}},         // -X
        {{0, TAM, TAM}, {0, 0, TAM}, {TAM, 0, TAM}, {TAM, TAM, TAM}}, // +Z
        {{0, 0, 0}, {0, TAM, 0}, {TAM, TAM, 0}, {TAM, 0, 0}}          // -Z
    };
    public static final float[][][] FACE_UVS = {
        {{1,1}, {0,1}, {0,0}, {1,0}}, 
        {{1,0}, {0,0}, {0,1}, {1,1}}, 
        {{1,1}, {0,1}, {0,0}, {1,0}}, 
        {{1,1}, {0,1}, {0,0}, {1,0}}, 
        {{0,0}, {0,1}, {1,1}, {1,0}}, 
        {{0,1}, {0,0}, {1,0}, {1,1}}  
    };

    // obtem ou cria um ID para a textura e preenche o buffer de dados do atlas
    public static float obterIdTextura(String nome) {
        if(mapaTexturas.containsKey(nome)) {
            return (float) mapaTexturas.get(nome, 0);
        }
        TextureRegion region = Texturas.atlas.get(nome);
        if(region == null) return 0f; // textura faltando, usa ID 0 ou trata erro

        int id = contaTexturas;
        if(id >= 256) return 0f; // limite de segurança do array/shader

        mapaTexturas.put(nome, id);

        // preenche os dados que o shader vai ler(uMin, vMin, uMax, vMax)
        int idc = id * 4;
        dadosAtlas[idc] = region.getU();
        dadosAtlas[idc + 1] = region.getV();
        dadosAtlas[idc + 2] = region.getU2();
        dadosAtlas[idc + 3] = region.getV2();

        contaTexturas++;
        return (float) id;
    }

    public static void addFace(int faceId, String texturaNome, float x, float y, float z, 
	float h, float v, float luzBloco, float luzSol, FloatArrayUtil verts, ShortArrayUtil idc) {
        // pega o ID numerico(0 a 255) em vez de passar coordenadas brutas
        float texId = obterIdTextura(texturaNome);

        float multFace = ChunkLuz.FACE_LUZ[faceId];
        int r = (int)(luzBloco * multFace * 255);
        int g = (int)(luzSol * multFace * 255);
        int b = (int)(multFace * 255); 
        float corFinal = Color.toFloatBits(r, g, b, 255);

        // atualizado para usar a constante correta
        short idcBase = (short)(verts.tam / FLOATS_VERTICE); 

        float sx = 1f, sy = 1f, sz = 1f; 
        float uh = 1f, vv = 1f; 

        switch(faceId) {
            case 0: case 1: sx = h; sz = v; uh = h; vv = v; break;
            case 2: case 3: sz = h; sy = v; uh = h; vv = v; break;
            case 4: case 5: sx = h; sy = v; uh = h; vv = v; break;
        }
        for(int i = 0; i < 4; i++) {
            float[] vert = FACE_VERTICES[faceId][i];
            float[] uv = FACE_UVS[faceId][i];
            // posicao (3 floats)
            verts.add(x + vert[0] * sx);
            verts.add(y + vert[1] * sy);
            verts.add(z + vert[2] * sz);
            // UV Local com Tiling do Guloso(2 floats)
            // o shader vai usar fract() nisso aqui
            verts.add(uv[0] * uh); 
            verts.add(uv[1] * vv);
            // textura ID(1 float)
            verts.add(texId);
            // cor(1 float)
            verts.add(corFinal);
        }
        idc.add(idcBase);
        idc.add((short)(idcBase + 1));
        idc.add((short)(idcBase + 2));
        idc.add((short)(idcBase + 2));
        idc.add((short)(idcBase + 3));
        idc.add(idcBase);
    }
}
