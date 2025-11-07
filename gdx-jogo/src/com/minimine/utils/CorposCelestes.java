package com.minimine.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Gdx;

public class CorposCelestes {
    public static Mesh meshSol, meshLua;
    public static ShaderProgram shaderCelestial;

    public static final String VERT = 
	"attribute vec3 a_pos;\n" +
	"uniform mat4 u_projPos;\n" +
	"uniform vec3 u_posCorpo;\n" +
	"uniform float u_tam;\n" +
	"void main() {\n" +
	"  vec4 posMundo = vec4(u_posCorpo + a_pos * u_tam, 1.0);\n" +
	"  gl_Position = u_projPos * posMundo;\n" +
	"}";

    public static final String FRAG =
	"#ifdef GL_ES\n" +
	"precision mediump float;\n" +
	"#endif\n" +
	"uniform vec4 u_cor;\n" +
	"void main() {\n" +
	"  gl_FragColor = u_cor;\n" +
	"}";

    public static void iniciar() {
        shaderCelestial = new ShaderProgram(VERT, FRAG);

        if(!shaderCelestial.isCompiled()) {
            Gdx.app.log("Shader", "[ERRO] no shader celestial: " + shaderCelestial.getLog());
        }
        meshSol = criarEsfera(12, 12);
        meshLua = criarEsfera(10, 10);
    }

    public static Mesh criarEsfera(int aneis, int setores) {
        int vertConta = aneis * setores * 3;
        int idcConta = (aneis - 1) * (setores - 1) * 6;

        float[] vertices = new float[vertConta];
        short[] indices = new short[idcConta];

        int v = 0, i = 0;

        for(int r = 0; r < aneis; r++) {
            float phi = (float)Math.PI * r / (aneis - 1);
            for(int s = 0; s < setores; s++) {
                float theta = 2.0f * (float)Math.PI * s / (setores - 1);

                vertices[v++] = (float)(Math.sin(phi) * Math.cos(theta));
                vertices[v++] = (float)(Math.cos(phi));
                vertices[v++] = (float)(Math.sin(phi) * Math.sin(theta));
            }
        }
        for(int r = 0; r < aneis - 1; r++) {
            for(int s = 0; s < setores - 1; s++) {
                int current = r * setores + s;
                int next = (r + 1) * setores + s;

                indices[i++] = (short)current;
                indices[i++] = (short)next;
                indices[i++] = (short)(next + 1);

                indices[i++] = (short)(next + 1);
                indices[i++] = (short)(current + 1);
                indices[i++] = (short)current;
            }
        }
        Mesh mesh = new Mesh(true, vertConta / 3, idcConta, NuvensUtil.atribus);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    public static void att(Matrix4 matrizCamera) {
        if(shaderCelestial == null || !shaderCelestial.isCompiled()) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shaderCelestial.begin();
        shaderCelestial.setUniformMatrix("u_projPos", matrizCamera);
        // renderizar sol(se visivel)
        if(DiaNoiteUtil.visibiSol > 0.01f) {
            shaderCelestial.setUniform3fv("u_posCorpo",
			new float[]{DiaNoiteUtil.posicaoSol.x, DiaNoiteUtil.posicaoSol.y, DiaNoiteUtil.posicaoSol.z}, 0, 3);
            shaderCelestial.setUniformf("u_tam", 8f); // sol maior
            shaderCelestial.setUniform4fv("u_cor", DiaNoiteUtil.corSol, 0, 4);
            meshSol.render(shaderCelestial, GL20.GL_TRIANGLES);
        }
        // renderizar lua(se visivel)
        if(DiaNoiteUtil.visibiLua > 0.01f) {
            shaderCelestial.setUniform3fv("u_posCorpo", 
			new float[]{DiaNoiteUtil.posicaoLua.x, DiaNoiteUtil.posicaoLua.y, DiaNoiteUtil.posicaoLua.z}, 0, 3);
            shaderCelestial.setUniformf("u_tam", 6f); // lua menor
            shaderCelestial.setUniform4fv("u_cor", DiaNoiteUtil.corLua, 0, 4);
            meshLua.render(shaderCelestial, GL20.GL_TRIANGLES);
        }
        shaderCelestial.end();
        // restaura estado do blend
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public static void liberar() {
        meshSol.dispose();
        meshLua.dispose();
        shaderCelestial.dispose();
    }
}
