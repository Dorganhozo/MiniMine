package com.minimine.graficos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.minimine.entidades.Jogador;
/*
 * uma malha dinamica com todos os quads em lote
 * atributos: 5 floats = 20 bytes por vertice
 */
public class GerenciadorParticulas implements RenderableProvider {
    public static final int MAX = 256;
    public static final int FLOATS_V = 5;   // x y z u v
    public static final int VERTS_PART = 4;
    public static final int IDC_PART = 6;

    // simulação: arrays paralelos, zero GC por frame
    public final float[] px = new float[MAX];
    public final float[] py = new float[MAX];
    public final float[] pz = new float[MAX];
    public final float[] vx = new float[MAX];
    public final float[] vy = new float[MAX];
    public final float[] vz = new float[MAX];
    public final float[] vida = new float[MAX];
    public final float[] tam = new float[MAX];
    public final float[] u0 = new float[MAX];
    public final float[] v0 = new float[MAX];
    public final float[] u1 = new float[MAX];
    public final float[] v1 = new float[MAX];
    public int conta = 0;

    // GPU
    public final Mesh malha;
    public final float[] vboCpu;
    public final short[] iboCpu;
    public final Material material;
    
    public final Jogador jogador;

    public GerenciadorParticulas(Jogador jogador) {
        this.jogador = jogador;

        vboCpu = new float[MAX * VERTS_PART * FLOATS_V];

        iboCpu = new short[MAX * IDC_PART];
        for(int i = 0; i < MAX; i++) {
            final int b = i * VERTS_PART;
            final int o = i * IDC_PART;
            iboCpu[o] = (short)(b);
            iboCpu[o+1] = (short)(b + 1);
            iboCpu[o+2] = (short)(b + 2);
            iboCpu[o+3] = (short)(b);
            iboCpu[o+4] = (short)(b + 2);
            iboCpu[o+5] = (short)(b + 3);
        }
        malha = new Mesh(false, MAX * VERTS_PART, MAX * IDC_PART,
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
		);
        malha.setIndices(iboCpu);

        material = new Material(TextureAttribute.createDiffuse(Texturas.blocos));
    }

    public void criar(float x, float y, float z, TextureRegion regiao) {
        if(regiao == null) return;

        final float tileU0 = regiao.getU();
        final float tileV0 = regiao.getV();
        final float tileU1 = regiao.getU2();
        final float tileV1 = regiao.getV2();
        final float tileUW = tileU1 - tileU0;
        final float tileVH = tileV1 - tileV0;
        final float fragUV = 0.25f;

        final int quantidade = MathUtils.random(6, 14);
        for(int i = 0; i < quantidade && conta < MAX; i++) {
            final float fu = MathUtils.random(0f, 1f - fragUV);
            final float fv = MathUtils.random(0f, 1f - fragUV);

            u0[conta] = tileU0 + fu * tileUW;
            v0[conta] = tileV0 + fv * tileVH;
            u1[conta] = u0[conta] + fragUV * tileUW;
            v1[conta] = v0[conta] + fragUV * tileVH;

            px[conta] = x + MathUtils.random(0.1f, 0.9f);
            py[conta] = y + MathUtils.random(0.1f, 0.9f);
            pz[conta] = z + MathUtils.random(0.1f, 0.9f);

            vx[conta] = MathUtils.random(-1.5f, 1.5f);
            vy[conta] = MathUtils.random(0.5f, 4.0f);
            vz[conta] = MathUtils.random(-1.5f, 1.5f);

            tam[conta] = MathUtils.random(0.05f, 0.12f);
            vida[conta] = MathUtils.random(0.4f, 1.2f);

            conta++;
        }
    }

    // chamado antes de mb.begin() para atualizar simulação e VBO
    public void att(float delta) {
        int i = 0;
        while(i < conta) {
            vida[i] -= delta;
            if(vida[i] <= 0f) {
                final int ult = conta - 1;
                px[i]=px[ult];
				py[i]=py[ult];
				pz[i]=pz[ult];
                vx[i]=vx[ult];
				vy[i]=vy[ult];
				vz[i]=vz[ult];
                vida[i]=vida[ult];
				tam[i]=tam[ult];
                u0[i]=u0[ult];
				v0[i]=v0[ult];
                u1[i]=u1[ult];
				v1[i]=v1[ult];
                conta--;
                continue;
            }
            vy[i] -= 9.8f * delta;
            px[i] += vx[i] * delta;
            py[i] += vy[i] * delta;
            pz[i] += vz[i] * delta;
            i++;
        }
        if(conta == 0) return;

        final Camera cam = jogador.camera;

        float rx = cam.up.y * cam.direction.z - cam.up.z * cam.direction.y;
        float ry = cam.up.z * cam.direction.x - cam.up.x * cam.direction.z;
        float rz = cam.up.x * cam.direction.y - cam.up.y * cam.direction.x;
        final float rtam = (float)Math.sqrt(rx*rx + ry*ry + rz*rz);
        if(rtam > 0f) {
			rx/=rtam;
			ry/=rtam;
			rz/=rtam;
		}
        final float ux = cam.up.x, uy = cam.up.y, uz = cam.up.z;

        int pos = 0;
        for(int j = 0; j < conta; j++) {
            final float cx = px[j], cy = py[j], cz = pz[j];
            final float s = tam[j] * (vida[j] < 0.2f ? vida[j] / 0.2f : 1f);

            final float rsx = rx*s, rsy = ry*s, rsz = rz*s;
            final float usx = ux*s, usy = uy*s, usz = uz*s;

            vboCpu[pos++]=cx-rsx-usx;
			vboCpu[pos++]=cy-rsy-usy;
			vboCpu[pos++]=cz-rsz-usz;
            vboCpu[pos++]=u0[j];
			vboCpu[pos++]=v1[j];

            vboCpu[pos++]=cx+rsx-usx;
			vboCpu[pos++]=cy+rsy-usy;
			vboCpu[pos++]=cz+rsz-usz;
            vboCpu[pos++]=u1[j];
			vboCpu[pos++]=v1[j];

            vboCpu[pos++]=cx+rsx+usx;
			vboCpu[pos++]=cy+rsy+usy;
			vboCpu[pos++]=cz+rsz+usz;
            vboCpu[pos++]=u1[j];
			vboCpu[pos++]=v0[j];

            vboCpu[pos++]=cx-rsx+usx;
			vboCpu[pos++]=cy-rsy+usy;
			vboCpu[pos++]=cz-rsz+usz;
            vboCpu[pos++]=u0[j];
			vboCpu[pos++]=v0[j];
        }
        malha.setVertices(vboCpu, 0, conta * VERTS_PART * FLOATS_V);
    }

    @Override
    public void getRenderables(Array<Renderable> rs, Pool<Renderable> reuso) {
        if(conta == 0) return;

        final Renderable r = reuso.obtain();
        r.material = material;
        r.meshPart.mesh = malha;
        r.meshPart.offset = 0;
        r.meshPart.size = conta * IDC_PART;
        r.meshPart.primitiveType = GL20.GL_TRIANGLES;
        r.worldTransform.idt();
        r.userData = jogador.dadosLuz;
        rs.add(r);
    }

    public void liberar() {
        malha.dispose();
    }
}

