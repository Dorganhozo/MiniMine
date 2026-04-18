package com.minimine.graficos;

import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.minimine.utils.DiaNoiteUtil;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.GL20;

public class ShaderBranco implements Shader {
	public static final String VERT =
	"attribute vec3 a_position;\n" +
	"attribute vec2 a_texCoord0;\n" +
	"uniform mat4 u_projVisao;\n" +
	"uniform mat4 u_mundoPos;\n" +
	"varying vec2 v_uv;\n" +
	"void main() {\n" +
	"  v_uv = a_texCoord0;\n" +
	"  gl_Position = u_projVisao * u_mundoPos * vec4(a_position, 1.0);\n" +
	"}";
	public static final String FRAG =
	"#ifdef GL_ES\n"+
	"precision mediump float;\n"+
	"#endif\n" +
	"varying vec2 v_uv;\n" +
	"uniform sampler2D u_difusa;\n" +
	"uniform float u_luzCeu;\n" +
	"uniform float u_luzSolar;\n" +
	"uniform float u_luzBloco;\n" +
	"void main() {\n" +
	"  vec4 c = texture2D(u_difusa, v_uv);\n" +
	"  if(c.a < 0.5) discard;\n" +
	"  float solDinamico = u_luzSolar * u_luzCeu;\n" +
	"  float brilho = max(u_luzBloco, solDinamico);\n" +
	"  gl_FragColor = vec4(c.rgb * brilho, c.a);\n" +
	"}";

	ShaderProgram prog;

	@Override public void init() {
		prog = new ShaderProgram(VERT, FRAG);
		ShaderProgram.pedantic = false;
		if(!prog.isCompiled()) {
			com.badlogic.gdx.Gdx.app.error("ShaderBraco", prog.getLog());
		}
	}

	@Override public int compareTo(Shader outro) { return 0; }
	@Override public boolean canRender(Renderable r) { return true; }

	@Override public void begin(com.badlogic.gdx.graphics.Camera cam, RenderContext ctx) {
		prog.begin();
		prog.setUniformMatrix("u_projVisao", cam.combined);
		ctx.setDepthTest(GL20.GL_LEQUAL);
		ctx.setDepthMask(true);
	}

	@Override public void render(Renderable r) {
		prog.setUniformMatrix("u_mundoPos", r.worldTransform);
		if(r.userData instanceof float[]) {
			float[] luz = (float[]) r.userData;
			prog.setUniformf("u_luzCeu", DiaNoiteUtil.luz);
			prog.setUniformf("u_luzSolar", luz[0] / 15f);
			prog.setUniformf("u_luzBloco", luz[1] / 15f);
		}
		TextureAttribute ta = (TextureAttribute)r.material.get(TextureAttribute.Diffuse);
		if(ta != null) {
			ta.textureDescription.texture.bind(0);
			prog.setUniformi("u_difusa", 0);
		}
		r.meshPart.render(prog, true);
	}

	@Override public void end() { prog.end(); }
	@Override public void dispose() { prog.dispose(); }
}
