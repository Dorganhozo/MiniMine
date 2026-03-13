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

public class CorposCelestes {
    // cor do horizonte lida pelo Render para nevoa dos blocos
    public static float corCeuR, corCeuG, corCeuB;

    // cupula(abobada celeste)
    public static Mesh malhaDomo;
    public static ShaderProgram shaderDomo;

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
	"uniform float u_tempo;\n" +

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
    public static Mesh malhaBillboard;
    public static ShaderProgram shaderBillboard;

    // vetores temporarios reutilizaveis, evita alocação por frame
    public static final Vector3 tmpDireita = new Vector3();
    public static final Vector3 tmpCimaMundo = new Vector3();
    public static final Vector3 tmpCima = new Vector3();

    // raio da esfera celeste(unidades de cena)
    public static final float RAIO_VISUAL = 120f;

    // tamanho angular dos corpos. Valores menores = corpos menores
    // RAIO_VISUAL * FATOR_TAMANHO_SOL = tamanho real no espaço 3D
    public static final float FATOR_TAM_SOL = 0.055f; // ~6.6f unidades
    public static final float FATOR_TAM_LUA = 0.048f; // ~5.8f unidades

    public static final String BILL_VERT =
	"attribute vec3 a_pos;\n" +
	"attribute vec2 a_uv;\n" +
	"uniform mat4 u_proj;\n" +
	"varying vec2 v_uv;\n" +
	
	"void main() {\n" +
	"  v_uv = a_uv;\n" +
	"  gl_Position = u_proj * vec4(a_pos, 1.0);\n" +
	"}";

    // billboard do sol e da lua com brilho aditivo para a lua
    public static final String BILL_FRAG =
	"#ifdef GL_ES\n" +
	"precision mediump float;\n" +
	"#endif\n" +
	"varying vec2 v_uv;\n" +
	"uniform sampler2D u_tex;\n" +
	"uniform float u_alfa;\n" +
	"uniform float u_luzCeu;\n" +
	"uniform float u_ehLua;\n" +
	
	"void main() {\n" +
	"  vec4 cor = texture2D(u_tex, v_uv);\n" +
	"  if(cor.a < 0.05) discard;\n" +
	"  vec3 corFinal;\n" +
	
	"  if(u_ehLua < 0.5) {\n" +
	// sol: cor original, leve aquecimento no pôr do sol(vem pelo alfa)
	"    corFinal = cor.rgb;\n" +
	"  } else {\n" +
	// lua: branca-prateada fria com destaque proprio
	// não escurece com u_luzCeu, a lua brilha na escuridão
	"    vec2 uv = v_uv * 2.0 - 1.0;\n" + // uv centrado em [-1,1]
	"    float dist = length(uv);\n" +
	// brilho suave ao redor: anel de brilho fora do disco
	"    float brilho = smoothstep(1.0, 0.4, dist) * 0.25;\n" +
	"    corFinal = cor.rgb * vec3(0.92, 0.96, 1.00) + vec3(0.10, 0.12, 0.20) * brilho;\n" +
	"  }\n" +
	"  gl_FragColor = vec4(corFinal, cor.a * u_alfa);\n" +
	"}";

    public static void iniciar() {
        shaderDomo = new ShaderProgram(DOMO_VERT, DOMO_FRAG);
        if(!shaderDomo.isCompiled())
            Gdx.app.log("CorposCelestes", "[ERRO] domo: " + shaderDomo.getLog());

        shaderBillboard = new ShaderProgram(BILL_VERT, BILL_FRAG);
        if(!shaderBillboard.isCompiled())
            Gdx.app.log("CorposCelestes", "[ERRO] billboard: " + shaderBillboard.getLog());

        malhaDomo = criarDomo(32, 16);

        malhaBillboard = new Mesh(false, 4, 6,
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_pos"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_uv")
		);
        malhaBillboard.setIndices(new short[]{0, 1, 2, 2, 3, 0});
    }

    public static void att(PerspectiveCamera camera) {
        if(shaderDomo == null || !shaderDomo.isCompiled()) return;

        float luzCeu = DiaNoiteUtil.luz;
        float alturaSol = DiaNoiteUtil.obterFatorTransicao();

        Vector3 dirSol = DiaNoiteUtil.dirSol;
        Vector3 dirLua = DiaNoiteUtil.dirLua;

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
        shaderDomo.setUniformf("u_visibiLua", DiaNoiteUtil.visibiLua);
        shaderDomo.setUniformf("u_tempo", DiaNoiteUtil.tempo);
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

        if(DiaNoiteUtil.visibiSol > 0.01f) {
            shaderBillboard.setUniformf("u_ehLua", 0f);
            renderBillboard(
                DiaNoiteUtil.dirSol, camera,
                RAIO_VISUAL * FATOR_TAM_SOL,
                DiaNoiteUtil.visibiSol,
                Texturas.atlas.get("sol")
            );
        }
        if (DiaNoiteUtil.visibiLua > 0.01f) {
            shaderBillboard.setUniformf("u_ehLua", 1f);
            renderBillboard(
                DiaNoiteUtil.dirLua, camera,
                RAIO_VISUAL * FATOR_TAM_LUA,
                DiaNoiteUtil.visibiLua,
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
    public static final Vector3 Y_MUNDO = new Vector3(0f, 1f, 0f);

    public static void renderBillboard(
		Vector3 dirCorpo, PerspectiveCamera camera,
		float tam, float alfa,
		TextureRegion regiao) {

        // Centro do billboard na esfera celeste
        float cx = camera.position.x + dirCorpo.x * RAIO_VISUAL;
        float cy = camera.position.y + dirCorpo.y * RAIO_VISUAL;
        float cz = camera.position.z + dirCorpo.z * RAIO_VISUAL;

        // vetor da camera até o centro do billboard
        tmpDireita.set(cx - camera.position.x,
		cy - camera.position.y,
		cz - camera.position.z).nor();

        // se o corpo está quase no zenite/nadir, o cross com Y degeneraria
        // usa o eixo Z do mundo como padrão
        float absDotY = Math.abs(tmpDireita.dot(Y_MUNDO));
        Vector3 refCima = (absDotY > 0.98f)
            ? new Vector3(0f, 0f, 1f)
            : Y_MUNDO;

        // right = toCentro * refCima (perpendicular ao plano camera-Y)
        tmpCimaMundo.set(tmpDireita).crs(refCima).nor();
        // up = direita * toCentro(perpendicular a direita, ainda na face do quad)
        tmpCima.set(tmpCimaMundo).crs(tmpDireita).nor();

        float dx = tmpCimaMundo.x * tam;
        float dy = tmpCimaMundo.y * tam;
        float dz = tmpCimaMundo.z * tam;
        float ux = tmpCima.x * tam;
        float uy = tmpCima.y * tam;
        float uz = tmpCima.z * tam;

        float u1 = regiao.getU(),  v1 = regiao.getV();
        float u2 = regiao.getU2(), v2 = regiao.getV2();

        float[] verts = {
            cx - dx - ux,  cy - dy - uy,  cz - dz - uz,  u1, v2,
            cx + dx - ux,  cy + dy - uy,  cz + dz - uz,  u2, v2,
            cx + dx + ux,  cy + dy + uy,  cz + dz + uz,  u2, v1,
            cx - dx + ux,  cy - dy + uy,  cz - dz + uz,  u1, v1,
        };
        malhaBillboard.setVertices(verts);
        shaderBillboard.setUniformf("u_alfa", alfa);
        malhaBillboard.render(shaderBillboard, GL20.GL_TRIANGLES);
		
		camera.update();
    }

    public static Mesh criarDomo(int setores, int aneis) {
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
                idc[i++] = (short) curr;
                idc[i++] = (short) prox;
                idc[i++] = (short) (prox + 1);
                idc[i++] = (short) (prox + 1);
                idc[i++] = (short) (curr + 1);
                idc[i++] = (short) curr;
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

    public static void liberar() {
        if(malhaDomo != null) malhaDomo.dispose();
        if(malhaBillboard != null) malhaBillboard.dispose();
        if(shaderDomo != null) shaderDomo.dispose();
        if(shaderBillboard != null) shaderBillboard.dispose();
    }
}

