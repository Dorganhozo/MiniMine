package com.minimine.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Gdx;
import com.minimine.cenas.Mundo;

public class NuvensUtil {
    public static Mesh meshNuvens;
    public static ShaderProgram shaderNuvens;
    public static float tempo = 0f;
    public static float[] nuvensPos; // [x, y, z, tam] pra cada nuvem
    public static final int NUM_NUVENS = 50;
    public static final float RAIO_VISIVEL = 100f;
    public static final float VELOCIDADE = 10f;
	
    public static Vector3 centro= new Vector3();
    public static boolean primeiraVez = true;
	public static float p = 1f;
	
    public static String vert = 
    "attribute vec3 a_pos;\n" +
    "uniform mat4 u_projPos;\n" +
    "varying float v_altura;\n" +
    "void main() {\n" +
    "  v_altura = a_pos.y;\n" +
    "  gl_Position = u_projPos * vec4(a_pos, 1.0);\n" +
    "}";

    public static String frag =
    "#ifdef GL_ES\n" +
    "precision mediump float;\n" +
    "#endif\n" +
    "varying float v_altura;\n" +
    "void main() {\n" +
    "  vec3 cor = mix(vec3(1.0, 1.0, 1.0), vec3(0.9, 0.9, 0.95), v_altura * 0.05);\n" +
    "  gl_FragColor = vec4(cor, 0.8);\n" +
    "}";
	
	public static VertexAttribute atribus = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos");

    public static void iniciar() {
        nuvensPos = new float[NUM_NUVENS * 4];
        shaderNuvens = new ShaderProgram(vert, frag);

        if(!shaderNuvens.isCompiled()) {
            Gdx.app.log("Shader", "[ERRO]: " + shaderNuvens.getLog());
        }
    }

    public static void gerarNuvem(int idc, Vector3 centro) {
        int base = idc * 4;
        // gera ao redor do centro(360 graus)
        float angulo = (float)Math.random() * 360f;
        float distancia = (float)Math.random() * RAIO_VISIVEL * 0.8f; // 80% do raio

        nuvensPos[base] = centro.x + (float)Math.cos(Math.toRadians(angulo)) * distancia;
        nuvensPos[base + 1] = 80f + (float)Math.random() * 20f; // altura
        nuvensPos[base + 2] = centro.z + (float)Math.sin(Math.toRadians(angulo)) * distancia;
        nuvensPos[base + 3] = 6f + (float)Math.random() * 10f; // tamanho
    }

    public static void attMesh() {
        int VERT_NUVEM = 8;
        int TOTAL_VERTICES = NUM_NUVENS * VERT_NUVEM;
        int TOTAL_INDICES = NUM_NUVENS * 36;

        float[] vertices = new float[TOTAL_VERTICES * 3];
        short[] indices = new short[TOTAL_INDICES];

        int vertIdc = 0;
        int indicesIdc = 0;
        short vertPos = 0;

        for(int i = 0; i < NUM_NUVENS; i++) {
			int base = i * 4;
			float x = nuvensPos[base];
			float y = nuvensPos[base + 1];
			float z = nuvensPos[base + 2];
			float tam = nuvensPos[base + 3];

			// cria cubo pra nuvem
			vertices[vertIdc++] = x; vertices[vertIdc++] = y; vertices[vertIdc++] = z; // 0
			vertices[vertIdc++] = x + tam; vertices[vertIdc++] = y; vertices[vertIdc++] = z; // 1
			vertices[vertIdc++] = x + tam; vertices[vertIdc++] = y; vertices[vertIdc++] = z + tam; // 2
			vertices[vertIdc++] = x; vertices[vertIdc++] = y; vertices[vertIdc++] = z + tam; // 3
			vertices[vertIdc++] = x; vertices[vertIdc++] = y + tam * 0.3f; vertices[vertIdc++] = z; // 4
			vertices[vertIdc++] = x + tam; vertices[vertIdc++] = y + tam * 0.3f; vertices[vertIdc++] = z; // 5
			vertices[vertIdc++] = x + tam; vertices[vertIdc++] = y + tam * 0.3f; vertices[vertIdc++] = z + tam; // 6
			vertices[vertIdc++] = x; vertices[vertIdc++] = y + tam * 0.3f; vertices[vertIdc++] = z + tam; // 7
			// indices:
			short baseIdc = vertPos;

			// baixo
			indices[indicesIdc++] = (short)(baseIdc + 2); indices[indicesIdc++] = (short)(baseIdc + 3); indices[indicesIdc++] = (short)(baseIdc + 0);
			indices[indicesIdc++] = (short)(baseIdc + 0); indices[indicesIdc++] = (short)(baseIdc + 1); indices[indicesIdc++] = (short)(baseIdc + 2);
			// topo
			indices[indicesIdc++] = (short)(baseIdc + 4); indices[indicesIdc++] = (short)(baseIdc + 7); indices[indicesIdc++] = (short)(baseIdc + 6);
			indices[indicesIdc++] = (short)(baseIdc + 6); indices[indicesIdc++] = (short)(baseIdc + 5); indices[indicesIdc++] = (short)(baseIdc + 4);
			// lados:
			// frente(-Z)
			indices[indicesIdc++] = (short)(baseIdc + 0); indices[indicesIdc++] = (short)(baseIdc + 4); indices[indicesIdc++] = (short)(baseIdc + 5);
			indices[indicesIdc++] = (short)(baseIdc + 5); indices[indicesIdc++] = (short)(baseIdc + 1); indices[indicesIdc++] = (short)(baseIdc + 0);
			// direita(+X)
			indices[indicesIdc++] = (short)(baseIdc + 1); indices[indicesIdc++] = (short)(baseIdc + 5); indices[indicesIdc++] = (short)(baseIdc + 6);
			indices[indicesIdc++] = (short)(baseIdc + 6); indices[indicesIdc++] = (short)(baseIdc + 2); indices[indicesIdc++] = (short)(baseIdc + 1);
			// tras(+Z)
			indices[indicesIdc++] = (short)(baseIdc + 2); indices[indicesIdc++] = (short)(baseIdc + 6); indices[indicesIdc++] = (short)(baseIdc + 7);
			indices[indicesIdc++] = (short)(baseIdc + 7); indices[indicesIdc++] = (short)(baseIdc + 3); indices[indicesIdc++] = (short)(baseIdc + 2);
			// esquerda(-X)
			indices[indicesIdc++] = (short)(baseIdc + 3); indices[indicesIdc++] = (short)(baseIdc + 7); indices[indicesIdc++] = (short)(baseIdc + 4);
			indices[indicesIdc++] = (short)(baseIdc + 4); indices[indicesIdc++] = (short)(baseIdc + 0); indices[indicesIdc++] = (short)(baseIdc + 3);
			vertPos += 8;
		}
        if(meshNuvens != null) meshNuvens.dispose();
        
        meshNuvens = new Mesh(true, TOTAL_VERTICES, TOTAL_INDICES, atribus);
        meshNuvens.setVertices(vertices);
        meshNuvens.setIndices(indices);
    }

    public static void att(float delta, Vector3 pos) {
        tempo += delta;
        if(primeiraVez) {
            for(int i = 0; i < NUM_NUVENS; i++) {
                gerarNuvem(i, pos);
            }
            attMesh();
            centro.set(pos);
            primeiraVez = false;
            return;
        }
        boolean precisaAtualizarMesh = false;
        // movimento
        for(int i = 0; i < NUM_NUVENS; i++) {
            int base = i * 4;

            // movimento continuo em direção fixa(pra OESTE -X)
            nuvensPos[base] -= VELOCIDADE;
            // verifica se saiu do raio visivel em relação do jogador
            float distanciaX = nuvensPos[base] - pos.x;
            float distanciaZ = nuvensPos[base + 2] - pos.z;
            float distancia = (float)Math.sqrt(distanciaX * distanciaX + distanciaZ * distanciaZ);

            if(distancia > RAIO_VISIVEL) {
                // nuvem espalhada ao eedor do jogador
                attNuvemEspalhada(i, pos);
                precisaAtualizarMesh = true;
            }
        }
        centro.set(pos);
        // atualiza a mesh a cada frame
        if(precisaAtualizarMesh || true) attMesh();
    }

    public static void attNuvemEspalhada(int idc, Vector3 posJogador) {
        int base = idc * 4;
		
        float anguloBase = 0f; // começa a direita(0 graus)
        float variacaoAngulo = 120f; // varia de -60 a +60 graus em relação a direita
        float angulo = anguloBase + (float)Math.random() * variacaoAngulo - variacaoAngulo/2;

        float dis = RAIO_VISIVEL * 0.7f + (float)Math.random() * RAIO_VISIVEL * 0.3f; // 70-100% do raio

        nuvensPos[base] = posJogador.x + (float)Math.cos(Math.toRadians(angulo)) * dis;
        nuvensPos[base + 1] = 80f + (float)Math.random() * 20f; // altura
        nuvensPos[base + 2] = posJogador.z + (float)Math.sin(Math.toRadians(angulo)) * dis;
        nuvensPos[base + 3] = 6f + (float)Math.random() * 10f; // tamanho
    }

    public static void att(Matrix4 matrizCamera) {
        if(!Mundo.nuvens || meshNuvens == null) return;

        shaderNuvens.begin();
        shaderNuvens.setUniformMatrix("u_projPos", matrizCamera);
        meshNuvens.render(shaderNuvens, GL20.GL_TRIANGLES);
        shaderNuvens.end();
    }

    public static void liberar() {
		meshNuvens.dispose();
        shaderNuvens.dispose();
    }
}
