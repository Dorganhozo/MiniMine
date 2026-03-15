package com.minimine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.minimine.graficos.Texturas;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;

public class DiaNoiteUtil {
    // cor do horizonte lida pelo Render para nevoa dos blocos
    public float corCeuR, corCeuG, corCeuB;

    // cupula(abobada celeste)
    public Mesh malhaDomo;
    public ShaderProgram shaderDomo;

    public static final String DOMO_VERT =
	"attribute vec3 a_pos;\n" +
	"uniform mat4 u_proj;\n" +
	"uniform vec3 u_posCamera;\n" +
	"varying vec3 v_dir;\n" +
	
	"void main() {\n" +
	"  v_dir = a_pos;\n" +
	"  gl_Position = u_proj * vec4(a_pos + u_posCamera, 1.0);\n" +
	"}";

    public static final String DOMO_FRAG =
	"#ifdef GL_ES\n" +
	"precision mediump float;\n" +
	"#endif\n" +
	"varying vec3 v_dir;\n" +
	"uniform float u_luzCeu;\n" +
	"uniform float u_alturaSol;\n" +
	"uniform vec3 u_dirSol;\n" +
	"uniform vec3 u_dirLua;\n" +
	"uniform float u_visibiLua;\n" +
	"uniform float u_tempo;\n"+

	"float hash3(vec3 p) {\n" +
	"  return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);\n" +
	"}\n" +

	"float passoBorrado2(float e0, float e1, float x) {\n" +
	"  float t = clamp((x - e0) / (e1 - e0), 0.0, 1.0);\n" +
	"  return t * t * (3.0 - 2.0 * t);\n" +
	"}\n" +

	"void main() {\n" +
	"  vec3 dir = normalize(v_dir);\n" +
	"  float altFrag = max(0.0, dir.y);\n" +

	"  vec3 corNoiteAlto = vec3(0.02, 0.04, 0.14);\n" +
	"  vec3 corNoiteBaix = vec3(0.04, 0.06, 0.18);\n" +
	"  vec3 corDiaAlto = vec3(0.26, 0.50, 0.95);\n" +
	"  vec3 corDiaBaix = vec3(0.52, 0.74, 1.00);\n" +
	"  vec3 corPoenteH = vec3(1.00, 0.32, 0.02);\n" +
	"  vec3 corPoenteTopo= vec3(0.55, 0.20, 0.45);\n" +

	"  float diaFator = passoBorrado2(0.40, 0.60, u_alturaSol);\n" +
	"  float noiteFator = 1.0 - diaFator;\n" +

	"  float gradV = passoBorrado2(-0.05, 0.65, altFrag);\n" +
	"  vec3 corDia = mix(corDiaBaix, corDiaAlto, gradV) * u_luzCeu;\n" +
	"  vec3 corNoite = mix(corNoiteBaix, corNoiteAlto, gradV);\n" +
	"  corNoite += vec3(0.03, 0.04, 0.08) * u_visibiLua;\n" +

	"  vec3 corBase = mix(corNoite, corDia, diaFator);\n" +

	"  float janelaCrp = passoBorrado2(0.34, 0.48, u_alturaSol) * (1.0 - passoBorrado2(0.52, 0.66, u_alturaSol));\n" +
	"  float dotSol = max(0.0, dot(dir, u_dirSol));\n" +
	"  float dotSolH = max(0.0, dot(vec3(dir.x, 0.0, dir.z), vec3(u_dirSol.x, 0.0, u_dirSol.z)));\n" +
	"  float haloHoriz = pow(dotSolH, 2.5) * (1.0 - altFrag * 1.8);\n" +
	"  float haloDisc  = pow(dotSol, 6.0);\n" +
	"  vec3 corCrp = mix(corPoenteTopo, corPoenteH, (1.0 - altFrag));\n" +

	"  corBase += corCrp * clamp(haloHoriz, 0.0, 1.0) * janelaCrp * 1.1;\n" +
	"  corBase += corCrp * haloDisc  * janelaCrp * 0.7;\n" +
	"  corBase += vec3(1.0, 0.90, 0.70) * pow(dotSol, 28.0) * diaFator * 0.5;\n" +

	"  float dotLua = max(0.0, dot(dir, u_dirLua));\n" +
	"  float haloLua = pow(dotLua, 8.0) * u_visibiLua * noiteFator;\n" +
	"  corBase += vec3(0.15, 0.18, 0.30) * haloLua;\n" +

	// === ESTRELAS ===
	"  float estrelasVis = noiteFator * passoBorrado2(0.0, 0.10, altFrag);\n" +
	"  estrelasVis *= (1.0 - clamp(pow(dotLua, 4.0) * u_visibiLua * 1.5, 0.0, 1.0));\n" +
	"  if(estrelasVis > 0.01) {\n" +
	"    vec3 p = dir * 60.0;\n"+ // escala do espaço das estrelas
	"    vec3 id = floor(p);\n" +
	"    float n = hash3(id);\n" +
	"    if(n > 0.98) {\n" +
	"      vec3 centro = id + 0.5;\n" +
	"      float dist = length(p - centro);\n" +
	"      // Esfera de brilho radial pura\n" +
	"      float brilho = smoothstep(0.4, 0.0, dist);\n" +
	"      float cintila = 0.7 + 0.3 * sin(u_tempo * 3.0 + n * 20.0);\n" +
	"      corBase += vec3(0.95, 0.98, 1.0) * brilho * cintila * estrelasVis * 1.8;\n" +
	"    }\n" +
	"  }\n" +
	"  gl_FragColor = vec4(corBase, 1.0);\n" +
	"}";
	
    // billboards(sol + lua)
    public Mesh malhaBillboard;
    public ShaderProgram shaderBillboard;

    // vetores temporarios reutilizaveis, evita alocação por frame
    public final Vector3 tmpDireita = new Vector3();
    public final Vector3 tmpCimaMundo = new Vector3();
    public final Vector3 tmpCima = new Vector3();
	public final Vector3 padraoVec = new Vector3(0f, 0f, 1f);

    // raio da esfera celeste(unidades de cena)
    public final float RAIO_VISUAL = 120f;

    // tamanho angular dos corpos. Valores menores = corpos menores
    // RAIO_VISUAL * FATOR_TAMANHO_SOL = tamanho real no espaço 3D
    public final float FATOR_TAM_SOL = 0.040f;
    public final float FATOR_TAM_LUA = 0.020f;

    public static final String BILL_VERT = 
    "attribute vec3 a_pos;\n"+
	"attribute vec2 a_uv; " +
    "uniform mat4 u_proj;\n"+
	"uniform vec3 u_centro;\n"+
	"uniform vec2 u_tam;\n" +
    "uniform vec3 u_camD;\n"+
	"uniform vec3 u_camU;\n"+
	"uniform vec4 u_uvRegiao;\n" +
    "varying vec2 v_uv; " +
	
    "void main() { " +
    "  v_uv = mix(u_uvRegiao.xy, u_uvRegiao.zw, a_uv); " +
    "  vec3 r = normalize(cross(u_camD, u_camU)); " +
    "  vec3 u = cross(r, u_camD); " +
    "  vec3 p = u_centro + (r * a_pos.x * u_tam.x) + (u * a_pos.y * u_tam.y); " +
    "  gl_Position = u_proj * vec4(p, 1.0); " +
    "}";

	public static final String BILL_FRAG = 
    "#ifdef GL_ES\n"+
	"precision mediump float;\n"+
	"#endif\n" +
    "varying vec2 v_uv;\n"+
	"uniform sampler2D u_tex;\n"+
	"uniform float u_alfa;\n"+
	"uniform float u_ehLua;\n" +
	
    "void main() { " +
    "  vec4 c = texture2D(u_tex, v_uv);\n"+
	"  if(c.a < 0.05) discard;\n" +
    "  vec3 f = (u_ehLua < 0.5) ? c.rgb : (c.rgb * vec3(0.92, 0.96, 1.0) + vec3(0.1, 0.12, 0.2) * smoothstep(1.0, 0.4, length(v_uv * 2.0 - 1.0)) * 0.25);\n" +
    "  gl_FragColor = vec4(f, c.a * u_alfa);\n" +
    "}";
	
	public float tempo = 0.0f;
    public float tempo_velo = 1f / 20f;
    public float luz = 1f;
    public long ultimaAtt  = 0;

    public final Vector3 dirSol = new Vector3();
    public final Vector3 dirLua = new Vector3();

    public final Vector3 posicaoSol = new Vector3();
    public final Vector3 posicaoLua = new Vector3();

    public final float[] corSol = { 1.0f, 0.9f, 0.1f, 1.0f };
    public final float[] corLua = { 0.9f, 0.95f, 1.0f, 1.0f };

    public float visibiSol = 1.0f;
    public float visibiLua = 0.0f;

    public final float[] posTmp = new float[3];

    public void calcularPosicoesCorposCelestes() {
        float anguloSolRad = tempo;

        dirSol.set(
            MathUtils.cos(anguloSolRad),
            MathUtils.sin(anguloSolRad),
            0f
        );
        posicaoSol.set(dirSol);

        dirLua.set(-dirSol.x, -dirSol.y, 0f);
        posicaoLua.set(dirLua);
    }

    public float obterFatorTransicao() {
        return Math.min(1.0f, Math.max(0.0f, (dirSol.y + 1.0f) * 0.5f));
    }

    public final boolean ehNoite() { return dirSol.y < -0.15f; }
    public final boolean ehDia() { return dirSol.y > 0.15f; }

    public void aplicarShader(ShaderProgram shader) {
        posTmp[0] = dirSol.x;
		posTmp[1] = dirSol.y;
		posTmp[2] = dirSol.z;
        shader.setUniform3fv("u_posSol", posTmp, 0, 3);
        posTmp[0] = dirLua.x;
		posTmp[1] = dirLua.y;
		posTmp[2] = dirLua.z;
        shader.setUniform3fv("u_posLua", posTmp, 0, 3);
        shader.setUniform4fv("u_corSol", corSol, 0, 4);
        shader.setUniform4fv("u_corLua", corLua, 0, 4);
    }

    public void iniciar() {
        shaderDomo = new ShaderProgram(DOMO_VERT, DOMO_FRAG);
        if(!shaderDomo.isCompiled())
            Gdx.app.log("CorposCelestes", "[ERRO] domo: " + shaderDomo.getLog());

        shaderBillboard = new ShaderProgram(BILL_VERT, BILL_FRAG);
        if(!shaderBillboard.isCompiled())
            Gdx.app.log("CorposCelestes", "[ERRO] billboard: " + shaderBillboard.getLog());

        malhaDomo = criarDomo(32, 16);

		malhaBillboard = new Mesh(true, 4, 6, 
		new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_uv")
		);

		malhaBillboard.setVertices(new float[]{
			-0.5f, -0.5f, 0, 0, 1,
			0.5f, -0.5f, 0, 1, 1,
			0.5f,  0.5f, 0, 1, 0,
			-0.5f,  0.5f, 0, 0, 0
		});
		malhaBillboard.setIndices(new short[]{0, 1, 2, 2, 3, 0});
    }

    public void att(PerspectiveCamera camera, float delta) {
		tempo += tempo_velo * delta;
        if(tempo > MathUtils.PI2) tempo -= MathUtils.PI2;

        float ciclo = MathUtils.sin(tempo);
        luz = Math.max(0.05f, (ciclo + 1.0f) * 0.475f + 0.05f);

        calcularPosicoesCorposCelestes();

        // sol: aparece suavemente conforme sobe acima do horizonte
        visibiSol = passoBorrado(-0.08f, 0.10f, dirSol.y);

        // lua: so aparece depois que o sol já está bem abaixo do horizonte,
        // e some antes do sol voltar, nunca coexistem visiveis no céu
        // solAbaixo=1 quando o sol está suficientemente submerso
        // luaAcima=1 quando a lua ja subiu acima do horizonte
        float solAbaixo = passoBorrado(-0.08f, -0.28f, dirSol.y);
        float luaAcima  = passoBorrado(-0.08f,  0.10f, dirLua.y);
        visibiLua = solAbaixo * luaAcima;

        corSol[3] = visibiSol;
        corLua[3] = visibiLua;

        ultimaAtt = System.currentTimeMillis();
		
        if(shaderDomo == null || !shaderDomo.isCompiled()) return;

        float luzCeu = luz;
        float alturaSol = obterFatorTransicao();

        // cor do horizonte para nevoa dos blocos
        float diaFator = passoBorrado(0.40f, 0.60f, alturaSol);
        float janelaCrp = passoBorrado(0.34f, 0.48f, alturaSol) *
		(1f - passoBorrado(0.52f, 0.66f, alturaSol));
		
        corCeuR = mix(0.04f, 0.52f * luzCeu, diaFator) + 1.00f * janelaCrp * 1.1f;
        corCeuG = mix(0.06f, 0.74f * luzCeu, diaFator) + 0.32f * janelaCrp * 1.1f;
        corCeuB = mix(0.18f, 1.00f * luzCeu, diaFator) + 0.02f * janelaCrp * 1.1f;

        // === domo ===
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        shaderDomo.begin();
        shaderDomo.setUniformMatrix("u_proj", camera.combined);
        shaderDomo.setUniformf("u_posCamera", camera.position.x, camera.position.y, camera.position.z);
        shaderDomo.setUniformf("u_luzCeu", luzCeu);
        shaderDomo.setUniformf("u_alturaSol", alturaSol);
        shaderDomo.setUniformf("u_dirSol", dirSol.x, dirSol.y, dirSol.z);
        shaderDomo.setUniformf("u_dirLua", dirLua.x, dirLua.y, dirLua.z);
        shaderDomo.setUniformf("u_visibiLua", visibiLua);
        shaderDomo.setUniformf("u_tempo", tempo);
        malhaDomo.render(shaderDomo, GL20.GL_TRIANGLES);
        shaderDomo.end();

        // === billboards ===
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shaderBillboard.begin();
        shaderBillboard.setUniformMatrix("u_proj", camera.combined);
        shaderBillboard.setUniformi("u_tex", 0);
        shaderBillboard.setUniformf("u_luzCeu", luzCeu);

        Texturas.ceu.bind(0);

        if(visibiSol > 0.01f) {
            shaderBillboard.setUniformf("u_ehLua", 0f);
            renderBillboard(
                dirSol, camera,
                RAIO_VISUAL * FATOR_TAM_SOL,
                visibiSol,
                Texturas.atlas.get("sol")
            );
        }
        if(visibiLua > 0.01f) {
            shaderBillboard.setUniformf("u_ehLua", 1f);
            renderBillboard(
                dirLua, camera,
                RAIO_VISUAL * FATOR_TAM_LUA,
                visibiLua,
                Texturas.atlas.get("lua_completa")
            );
        }
        shaderBillboard.end();
		
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }
    // billboard esferico: a face sempre aponta para a camera usando o eixo Y
    // do mundo como referencia, evita distorção ao mover a camera
    
    // tecnica: direita = normalize(cameraDir * Y_mundo), up = normalize(direita * cameraDir)
    // isso garante que o quad sempre fique de frente para a camera sem deformar
    public final Vector3 Y_MUNDO = new Vector3(0f, 1f, 0f);

    public void renderBillboard(
		Vector3 dirCorpo, PerspectiveCamera camera,
		float tam, float alfa,
		TextureRegion regiao) {

		// Centro do astro no mundo
		float cx = camera.position.x + dirCorpo.x * RAIO_VISUAL;
		float cy = camera.position.y + dirCorpo.y * RAIO_VISUAL;
		float cz = camera.position.z + dirCorpo.z * RAIO_VISUAL;

		shaderBillboard.setUniformf("u_centro", cx, cy, cz);
		shaderBillboard.setUniformf("u_tam", tam * 2, tam * 2);
		shaderBillboard.setUniformf("u_camD", camera.direction);
		shaderBillboard.setUniformf("u_camU", camera.up);
		shaderBillboard.setUniformf("u_alfa", alfa);

		// Passa os UVs da região para o shader se estiver usando atlas
		shaderBillboard.setUniformf("u_uvRegiao", regiao.getU(), regiao.getV(), regiao.getU2(), regiao.getV2());

		malhaBillboard.render(shaderBillboard, GL20.GL_TRIANGLES);
	}

    public Mesh criarDomo(int setores, int aneis) {
        int numVerts = (aneis + 1) * (setores + 1);
        int numidc   = aneis * setores * 6;
        float[] verts = new float[numVerts * 3];
        short[] idc   = new short[numidc];

        int v = 0;
        for(int a = 0; a <= aneis; a++) {
            double phi = Math.PI / 2.0 - a * (Math.PI * 1.2 / aneis);
            for(int s = 0; s <= setores; s++) {
                double theta = 2.0 * Math.PI * s / setores;
                verts[v++] = (float)(Math.cos(phi) * Math.cos(theta));
                verts[v++] = (float)Math.sin(phi);
                verts[v++] = (float)(Math.cos(phi) * Math.sin(theta));
            }
        }
        int i = 0;
        for(int a = 0; a < aneis; a++) {
            for(int s = 0; s < setores; s++) {
                int curr = a * (setores + 1) + s;
                int prox = curr + setores + 1;
                idc[i++] = (short)curr;
                idc[i++] = (short)prox;
                idc[i++] = (short)(prox + 1);
                idc[i++] = (short)(prox + 1);
                idc[i++] = (short)(curr + 1);
                idc[i++] = (short)curr;
            }
        }
        Mesh mesh = new Mesh(true, numVerts, numidc,
		new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"));
		
        mesh.setVertices(verts);
        mesh.setIndices(idc);
        return mesh;
    }

    public static final float passoBorrado(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }

    public static final float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public void liberar() {
        if(malhaDomo != null) malhaDomo.dispose();
        if(malhaBillboard != null) malhaBillboard.dispose();
        if(shaderDomo != null) shaderDomo.dispose();
        if(shaderBillboard != null) shaderBillboard.dispose();
    }
}

