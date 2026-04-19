package com.minimine.graficos;

import com.badlogic.gdx.graphics.g3d.Model;
import java.util.HashMap;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import com.badlogic.gdx.Gdx;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.minimine.inventario.ItemRegistro;

public class Modelos {
	public static final HashMap<String, SceneAsset> modelosGltf = new HashMap<>();
	public static final HashMap<CharSequence, Model> modelosItens = new HashMap<>();
	
	public static final float TAM_ITEM = 0.35f;
	public static final int COLUNAS_TEX = 16;
	public static final float ESPESSURA = TAM_ITEM / COLUNAS_TEX;

	public static final float ITEM_OX = 0.10f;
	public static final float ITEM_OY = -0.45f;
	public static final float ITEM_OZ = -0.55f;
	
	public static Model obterModelo(String caminho, boolean interno) {
		if(modelosGltf.containsKey(caminho)) return modelosGltf.get(caminho).scene.model;
		
		SceneAsset ativoCena = new GLTFLoader().load(interno ? Gdx.files.internal(caminho) : Gdx.files.absolute(caminho));
		 modelosGltf.put(caminho, ativoCena);
		 
		 return modelosGltf.get(caminho).scene.model;
	}
	
	public static Model obterModelo(String caminho) {
		return obterModelo(caminho, true);
	}
	
	// reconstroi o modelo do item quando muda
	// geometria: 16 quads frontais (uma fatia por coluna de pixel) + 16 quads laterais
	// cada quad frontal cobre uma coluna da textura
	// cada quad lateral da a espessura daquela coluna
	// ordem dos vertices: BL, BR, TR, TL (anti-horario visto de frente, normal apontando pra camera)
	// igual face +Z do BlocoModelo: {0,0,TAM},{TAM,0,TAM},{TAM,TAM,TAM},{0,TAM,TAM}
	public static ModelInstance modeloItem(CharSequence item) {
		if(modelosItens.containsKey(item)) return new ModelInstance(modelosItens.get(item));
		
		if(item.equals("ar")) return null;

		ItemRegistro.Item reg = ItemRegistro.obter(item);
		if(reg == null) return null;

		TextureRegion tex = Texturas.atlas.get(reg.textura);
		if(tex == null) return null;

		float uMin = tex.getU();
		float uMax = tex.getU2();
		float vMin = tex.getV();
		float vMax = tex.getV2();
		float uPasso = (uMax - uMin) / COLUNAS_TEX;

		// inclinacao 45 graus no eixo Y
		float cos = 0.7071f;
		float sin = 0.7071f;

		// eixo direita do item em espaco de camera
		float rdx = cos;
		float rdz = sin;
		// eixo de profundidade da fatia (perpendicular ao eixo direita, no plano XZ)
		float fdx = -sin;
		float fdz = cos;

		int totalQuads = COLUNAS_TEX * 2;
		int totalVertices = totalQuads * 4;
		int totalIndices = totalQuads * 6;

		float[] verts = new float[totalVertices * 5];
		short[] indices = new short[totalIndices];

		int vi = 0;
		int ii = 0;

		for(int col = 0; col < COLUNAS_TEX; col++) {
			float t0 = (float)col / COLUNAS_TEX;
			float t1 = (float)(col + 1) / COLUNAS_TEX;

			float px0 = ITEM_OX + rdx * TAM_ITEM * t0;
			float pz0 = ITEM_OZ + rdz * TAM_ITEM * t0;
			float px1 = ITEM_OX + rdx * TAM_ITEM * t1;
			float pz1 = ITEM_OZ + rdz * TAM_ITEM * t1;

			float uCol0 = uMin + uPasso * col;
			float uCol1 = uMin + uPasso * (col + 1);
			float uColMeio = (uCol0 + uCol1) * 0.5f;

			// quad frontal
			short base = (short)(vi / 5);
			verts[vi++] = px0;
			verts[vi++] = ITEM_OY;
			verts[vi++] = pz0;
			verts[vi++] = uCol0;
			verts[vi++] = vMax;
			verts[vi++] = px0;
			verts[vi++] = ITEM_OY + TAM_ITEM;
			verts[vi++] = pz0;
			verts[vi++] = uCol0;
			verts[vi++] = vMin;
			
			verts[vi++] = px1;
			verts[vi++] = ITEM_OY + TAM_ITEM;
			verts[vi++] = pz1;
			verts[vi++] = uCol1;
			verts[vi++] = vMin;
			verts[vi++] = px1;
			verts[vi++] = ITEM_OY;
			verts[vi++] = pz1;
			verts[vi++] = uCol1;
			verts[vi++] = vMax;

			indices[ii++] = base;
			indices[ii++] = (short)(base + 1);
			indices[ii++] = (short)(base + 2);
			indices[ii++] = (short)(base + 2);
			indices[ii++] = (short)(base + 3);
			indices[ii++] = base;

			// quad lateral: espessura da fatia
			float lx1 = px0 + fdx * ESPESSURA;
			float lz1 = pz0 + fdz * ESPESSURA;

			base = (short)(vi / 5);
			verts[vi++] = lx1;
			verts[vi++] = ITEM_OY;
			verts[vi++] = lz1;
			verts[vi++] = uColMeio;
			verts[vi++] = vMax;
			verts[vi++] = px0;
			verts[vi++] = ITEM_OY;
			verts[vi++] = pz0;
			verts[vi++] = uColMeio;
			verts[vi++] = vMax;
			
			verts[vi++] = px0;
			verts[vi++] = ITEM_OY + TAM_ITEM;
			verts[vi++] = pz0;
			verts[vi++] = uColMeio;
			verts[vi++] = vMin;
			verts[vi++] = lx1;
			verts[vi++] = ITEM_OY + TAM_ITEM;
			verts[vi++] = lz1;
			verts[vi++] = uColMeio;
			verts[vi++] = vMin;

			indices[ii++] = base;
			indices[ii++] = (short)(base + 1);
			indices[ii++] = (short)(base + 2);
			indices[ii++] = (short)(base + 2);
			indices[ii++] = (short)(base + 3);
			indices[ii++] = base;
		}

		Mesh mesh = new Mesh(true, totalVertices, totalIndices,
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
		);
		mesh.setVertices(verts);
		mesh.setIndices(indices);

		Material mat = new Material(TextureAttribute.createDiffuse(tex.getTexture()));

		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		mb.part("item", mesh, GL20.GL_TRIANGLES, 0, totalIndices, mat);
		Model modelo = mb.end();
		ModelInstance modeloItem = new ModelInstance(modelo);
		
		modelosItens.put(item, modelo);
		return modeloItem;
	}
	
	public static void liberar() {
		for(SceneAsset m : modelosGltf.values()) {
			m.scene.model.dispose();
			m.dispose();
		}
		modelosGltf.clear();
		for(Model m : modelosItens.values()) {
			m.dispose();
		}
		modelosItens.clear();
	}
}
