package com.minimine.utils;  

import com.badlogic.gdx.graphics.Mesh;  
import com.badlogic.gdx.graphics.VertexAttribute;  
import com.badlogic.gdx.graphics.VertexAttributes;  
import com.badlogic.gdx.math.Vector3;  
import com.badlogic.gdx.math.Matrix4;  
import com.minimine.cenas.Mundo;  
import com.badlogic.gdx.graphics.glutils.ShaderProgram;  
import com.badlogic.gdx.Gdx;  
import com.badlogic.gdx.graphics.GL20;  
import com.badlogic.gdx.graphics.Color;
import com.minimine.utils.blocos.BlocoModelo;

public class NuvensUtil {  
    public static boolean ativo = true;  
    private static Mesh meshNuvens;  
    private static float tempo = 0;  

    private static Matrix4 matrizMovimento = new Matrix4();  
    private static ShaderProgram shaderNuvens;  

    // Configurações das nuvens  
    private static final int NUM_NUVENS_CLUSTER = 30;
    private static final int BLOCOS_POR_NUVEM = 25;
    private static final float TAMANHO_CLUSTER_XZ = 8f;
    private static final float TAMANHO_CLUSTER_Y = 3f;
    private static final float ALTURA_NUVENS = 80f;
    private static final float RAIO_NUVENS = 60f;

    private static final int ID_TEXTURA_NUVEM = 1;

    public static final String vert =  
	"attribute vec3 a_pos;\n" +
	"attribute vec2 a_texCoord0;\n" +  
	"attribute vec4 a_cor;\n" +  
	"\n" +  
	"uniform mat4 u_projPos;\n" +  
	"uniform mat4 u_mundoPos;\n" +  
	"\n" +  
	"varying vec2 v_texCoord;\n" +  
	"varying vec4 v_cor;\n" +  
	"\n" +  
	"void main() {\n" +  
	"    v_texCoord = a_texCoord0;\n" +  
	"    v_cor = a_cor;\n" +  
	"    gl_Position = u_projPos * u_mundoPos * vec4(a_pos, 1.0);\n" +  
	"}\n";  

    public static final String frag =  
	"#ifdef GL_ES\n" +  
	"precision mediump float;\n" +  
	"#endif\n" +  
	"\n" +  
	"varying vec2 v_texCoord;\n" +  
	"varying vec4 v_cor;\n" +  
	"\n" +  
	"uniform sampler2D u_textura;\n" +  
	"\n" +  
	"void main() {\n" +  
	"    vec4 texCor = texture2D(u_textura, v_texCoord);\n" +  
	"    vec4 corFinal = texCor * v_cor;\n" +  
	"    \n" +  
	"    if(corFinal.a < 0.1) {\n" +  
	"        discard;\n" +  
	"    }\n" +  
	"    \n" +  
	"    gl_FragColor = corFinal;\n" +  
	"}\n";

    public static void iniciar() {  
        if(!ativo) return;  

        shaderNuvens = new ShaderProgram(vert, frag);  
        if(!shaderNuvens.isCompiled()) {  
            Gdx.app.error("NuvensUtil", "Erro ao compilar shader: " + shaderNuvens.getLog());  
            ativo = false;  
            return;  
        }  
        VertexAttribute[] atributos = new VertexAttribute[] {  
            new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),  
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),  
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, GL20.GL_UNSIGNED_BYTE, true, ShaderProgram.COLOR_ATTRIBUTE)  
        };  

        int maxVertices = NUM_NUVENS_CLUSTER * BLOCOS_POR_NUVEM * 24;
        int maxIndices = NUM_NUVENS_CLUSTER * BLOCOS_POR_NUVEM * 36;

        meshNuvens = new Mesh(true, maxVertices, maxIndices, atributos);  
        gerarNuvens();  
    }  

    private static void gerarNuvens() {  
        FloatArrayUtil vertices = new FloatArrayUtil();  
        IntArrayUtil indices = new IntArrayUtil();  

        for(int i = 0; i < NUM_NUVENS_CLUSTER; i++) {  
            float clusterX = (float) (Math.random() - 0.5) * 2 * RAIO_NUVENS;  
            float clusterZ = (float) (Math.random() - 0.5) * 2 * RAIO_NUVENS;  
            float clusterY = ALTURA_NUVENS + ((float) Math.random() - 0.5f) * 10f;  

            for(int j = 0; j < BLOCOS_POR_NUVEM; j++) {  
                float posX = clusterX + (float) Math.floor((Math.random() - 0.5) * TAMANHO_CLUSTER_XZ);  
                float posY = clusterY + (float) Math.floor((Math.random() - 0.5) * TAMANHO_CLUSTER_Y);  
                float posZ = clusterZ + (float) Math.floor((Math.random() - 0.5) * TAMANHO_CLUSTER_XZ);  

                for(int face = 0; face < 6; face++) {
                    BlocoModelo.addFace(face, ID_TEXTURA_NUVEM, posX, posY, posZ, 1.0f, vertices, indices);
                }
            }  
        }  

        if(vertices.tam > 0) {  
            meshNuvens.setVertices(vertices.praArray());  
            int[] indArray = indices.praArray();  
            short[] indShortArray = new short[indArray.length];  
            for (int i = 0; i < indArray.length; i++) {  
                indShortArray[i] = (short) indArray[i];  
            }  
            meshNuvens.setIndices(indShortArray);  
        }  
    }  

    public static void atualizar(float delta, Vector3 posJogador) {  
        if (!ativo) return;  
        tempo += delta * 0.05f;   
        matrizMovimento.idt();  
        float offsetX = (float)Math.cos(tempo) * 5f;  
        float offsetZ = (float)Math.sin(tempo) * 5f;  
        matrizMovimento.translate(posJogador.x + offsetX, 0, posJogador.z + offsetZ);  
    }  

    public static void render(Matrix4 projViewMatrix) {  
        if(!ativo || meshNuvens == null || shaderNuvens == null || meshNuvens.getNumIndices() == 0) return;  

        Gdx.gl.glEnable(GL20.GL_BLEND);  
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);  
        Gdx.gl.glDepthMask(false); 

        shaderNuvens.begin();  
        shaderNuvens.setUniformMatrix("u_projPos", projViewMatrix);  
        shaderNuvens.setUniformMatrix("u_mundoPos", matrizMovimento);  
        shaderNuvens.setUniformi("u_textura", 0); 
        meshNuvens.render(shaderNuvens, GL20.GL_TRIANGLES);  
        shaderNuvens.end();  

        Gdx.gl.glDepthMask(true);  
        Gdx.gl.glDisable(GL20.GL_BLEND);  
    }  

    public static void liberar() {  
        if (meshNuvens != null) {  
            meshNuvens.dispose();  
            meshNuvens = null;  
        }  
        if (shaderNuvens != null) {  
            shaderNuvens.dispose();  
            shaderNuvens = null;  
        }  
    }    
}
