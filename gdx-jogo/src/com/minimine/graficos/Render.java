package com.minimine.graficos;

import com.minimine.ui.UI;
import com.minimine.cenas.Jogador;
import com.minimine.mundo.Mundo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.minimine.utils.DiaNoiteUtil;
import com.minimine.mundo.Chunk;
import com.minimine.utils.NuvensUtil;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.utils.CorposCelestes;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Render {
	public UI ui;
	public Mundo mundo;
	
	public static Texture atlasGeral = null;
    // mapa de UVs:
    // (atlas ID -> [u_min, v_min, u_max, v_max])
    public static final Map<Integer, float[]> atlasUVs = new HashMap<>();
	
	public static ShaderProgram shader;
	
	public static int maxFaces = Mundo.TAM_CHUNK * Mundo.Y_CHUNK * Mundo.TAM_CHUNK * 6 / 6;
    public static int maxVerts = maxFaces * 4;
    public static int maxIndices = maxFaces * 6;
    public static final VertexAttribute[] atriburs = new VertexAttribute[] {
        new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"),
        new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_cor")
    };
	
	public static String vert = 
    "attribute vec3 a_pos;\n" +
    "attribute vec2 a_texCoord;\n" +
    "attribute vec4 a_cor;\n" +
    "uniform mat4 u_projPos;\n" +
    "varying vec2 v_texCoord;\n" +
    "varying vec4 v_cor;\n" +
    "void main() {\n" +
	"v_texCoord = a_texCoord;\n" +
	"v_cor = a_cor;\n" +
	"gl_Position = u_projPos * vec4(a_pos, 1.0);\n" +
    "}";

	public static String frag =
    "#ifdef GL_ES\n" +
    "precision mediump float;\n" +
    "#endif\n" +
    "varying vec2 v_texCoord;\n" +
    "varying vec4 v_cor;\n" + 
    "uniform sampler2D u_textura;\n" +
    "uniform float u_luzCeu;\n" + 
    "void main() {\n" +
    // v_cor.r = Luz da Tocha (0.0 a 1.0)\n" +
    // v_cor.g = Luz do Sol (0.0 a 1.0)\n" +
    // v_cor.b = multiplicador de face(falso AO pra profundidade)
	"   float solDinamico = v_cor.g * u_luzCeu;\n" + 
	"   float brilhoBruto = max(v_cor.r, solDinamico);\n" +
	"   float iluminacaoFinal = brilhoBruto * v_cor.b;\n" +
	// transparencia:
	"   vec4 texCor = texture2D(u_textura, v_texCoord);\n" +
    "   if(texCor.a < 0.5) discard;\n" +
	// neblina baseada na distancia
	"   float dist = length(gl_FragCoord.z / gl_FragCoord.w);\n" +
	"   float inicio = 16.0;\n" + // começa a 1 chunk de distância
	"   float fim = 64.0;\n" + // termina no limite de 5 * 16
	"   float fator = clamp((dist - inicio) / (fim - inicio), 0.0, 1.0);\n" +
	// cor da neblina mudando com o céu
	"   vec3 corNevoa = vec3(0.4, 0.6, 0.9) * u_luzCeu;\n" + 
	"   gl_FragColor = vec4(mix(texCor.rgb * iluminacaoFinal, corNevoa, fator), texCor.a);\n" +
	"}";
	
	public Render(Jogador jogador, Mundo mundo) {
		this.ui = new UI(jogador);
		this.mundo = mundo;
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());  
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		shader = new ShaderProgram(vert, frag);
        if(!shader.isCompiled()) Gdx.app.log("shader", "[ERRO]: "+shader.getLog());
		
		criarAtlas();

		Texture[] framesAgua = {
			Texturas.texs.get("agua"),
			Texturas.texs.get("agua_a1"),
			Texturas.texs.get("agua_a2")
		};
		Animacoes2D.add(4, framesAgua, 3f); // 8 fps

		Animacoes2D.config();
		EmissorParticulas.iniciar();

        ShaderProgram.pedantic = false;

        if(mundo.nuvens && NuvensUtil.primeiraVez) NuvensUtil.iniciar();
        if(mundo.ciclo) CorposCelestes.iniciar();
	}
	
	public void att(float delta) {
		float fator = DiaNoiteUtil.obterFatorTransicao();
		float[] corNoite = {0.05f, 0.05f, 0.15f};
		float[] corDia = {0.5f * DiaNoiteUtil.luz, 0.7f * DiaNoiteUtil.luz, 1.0f * DiaNoiteUtil.luz};

		float r = corNoite[0] * (1f - fator) + corDia[0] * fator;
		float g = corNoite[1] * (1f - fator) + corDia[1] * fator;
		float b = corNoite[2] * (1f - fator) + corDia[2] * fator;

		Gdx.gl.glClearColor(r, g, b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		if(mundo.nuvens) NuvensUtil.att(delta, ui.jogador.posicao);

        shader.begin();

        shader.setUniformMatrix("u_projPos", ui.jogador.camera.combined);

        shader.setUniformf("u_luzCeu", DiaNoiteUtil.luz); 
		shader.setUniformf("u_alturaSol", DiaNoiteUtil.obterFatorTransicao());

		DiaNoiteUtil.aplicarShader(shader);

		atlasGeral.bind(0);
        shader.setUniformi("u_textura", 0);
		Gdx.gl.glDisable(GL20.GL_BLEND);

		// 1. solidos:
        for(final Chunk chunk : mundo.chunks.values()) {
			if(mundo.frustrum(chunk, ui.jogador) && chunk.malha != null && chunk.contaSolida > 0) {
				// renderiza apenas do indice 0 até o final dos solidos
				chunk.malha.render(shader, GL20.GL_TRIANGLES, 0, chunk.contaSolida);
			}
		}
		// 2. transparentes:
		Gdx.gl.glEnable(GL20.GL_BLEND);

		for(final Chunk chunk : mundo.chunks.values()) {
			if(mundo.frustrum(chunk, ui.jogador) && chunk.malha != null && chunk.contaTransp > 0) {
				// renderiza começando de onde o solido parou
				chunk.malha.render(shader, GL20.GL_TRIANGLES, chunk.contaSolida, chunk.contaTransp);
			}
		}
		Animacoes2D.att(delta);
		EmissorParticulas.att(shader, delta, ui.jogador);
		
		shader.end();
        if(mundo.nuvens) NuvensUtil.att(ui.jogador.camera.combined);

		mundo.att(delta, ui.jogador);
		
		if(mundo.carregado) {
			if(!ui.jogador.nasceu) {
				// tenta encontrar o chão, se o obterBlocoMundo retornar algo diferente de 0, 
				// significa que os dados daquela parte do mapa ja chegaram
				int yTeste = Mundo.obterAlturaChao((int)ui.jogador.posicao.x, (int)ui.jogador.posicao.z);
				if(yTeste > 1) { // se encontrou algo acima do fundo do mundo
					ui.jogador.posicao.y = yTeste;
					ui.jogador.nasceu = true;
					Gdx.app.log("[Jogo]", "jogador nasceu a "+yTeste+" blocos de altura");
				} else Gdx.app.log("[Jogo]", "não nasceu, altura recebida: "+yTeste);
			}
			ui.jogador.att(delta);
		}
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);

		ui.att(delta, mundo);
	}
	
	public void criarAtlas() {
        Pixmap primeiroPx = null;
        if(mundo.texturas.get(0) instanceof String) {
            primeiroPx = new Pixmap(Gdx.files.internal((String)mundo.texturas.get(0)));
        } else if(mundo.texturas.get(0) instanceof Texture) {
            Texture t = (Texture)mundo.texturas.get(0);
            t.getTextureData().prepare();
            primeiroPx = t.getTextureData().consumePixmap();
        }
        int texTam = primeiroPx.getWidth();
        int colunas = (int)Math.ceil(Math.sqrt(mundo.texturas.size()));
        int linhas = (int)Math.ceil((float)mundo.texturas.size() / colunas);
        int atlasLarg = texTam * colunas;
        int atlasAlt = texTam * linhas;

        Pixmap atlasPx = new Pixmap(atlasLarg, atlasAlt, Pixmap.Format.RGBA8888);

        for(int i = 0; i < mundo.texturas.size(); i++) {
            int x = (i % colunas) * texTam;
            int y = (i / colunas) * texTam;

            Pixmap px = null;
            if(mundo.texturas.get(i) instanceof String) {
                px = new Pixmap(Gdx.files.internal((String)mundo.texturas.get(i)));
            } else if(mundo.texturas.get(i) instanceof Texture) {
                Texture t = (Texture)mundo.texturas.get(i);
                t.getTextureData().prepare();
                Pixmap tmp = t.getTextureData().consumePixmap();
                px = new Pixmap(tmp.getWidth(), tmp.getHeight(), tmp.getFormat());
                px.drawPixmap(tmp, 0, 0);
                tmp.dispose();
            }
            atlasPx.drawPixmap(px, x, y);

            float u1 = (float)x / atlasLarg;
            float v1 = (float)y / atlasAlt;
            float u2 = (float)(x + texTam) / atlasLarg;
            float v2 = (float)(y + texTam) / atlasAlt;
            atlasUVs.put(i, new float[]{u1, v1, u2, v2});
            px.dispose();
        }
        atlasGeral = new Texture(atlasPx);
        atlasPx.dispose();
        primeiroPx.dispose();
    }
	
	public void liberar() {
		shader.dispose();
		atlasGeral.dispose();
		atlasUVs.clear();
		ui.liberar();
		mundo.liberar();
	}
}
