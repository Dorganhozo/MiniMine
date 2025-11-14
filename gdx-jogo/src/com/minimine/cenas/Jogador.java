package com.minimine.cenas;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.minimine.utils.ruidos.PerlinNoise3D;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.collision.Ray;
import com.minimine.utils.Mat;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.utils.JsonReader;
import com.minimine.utils.Texturas;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Jogador {
	public ModelInstance modelo;
	public int modo = 2; // 0 = espectador, 1 = criativo, 2 = sobrevivencia
	public PerspectiveCamera camera;
	public Vector3 posicao = new Vector3(1, 80, 1), velocidade = new Vector3();

	public float largura = 0.6f, altura = 1.8f, profundidade = 0.6f;
	public boolean noChao = true, naAgua = false;
	public BoundingBox hitbox = new BoundingBox();
	public static final BoundingBox blocoBox = new BoundingBox();
	public static final Vector3 minVec = new Vector3(), maxVec = new Vector3();
	
	public static float GRAVIDADE = -30f, VELO_MAX_QUEDA = -50f, velo = 8f, pulo = 10f;
	
	public int blocoSele = 0;
	public CharSequence item = "ar";
	public static int ALCANCE = 6;
	public Inventario inv = new Inventario();
	
	public float yaw = 180f, tom = -20f;
	
	public void criarModelo3D() {
		SceneAsset asset = new GLTFLoader().load(Gdx.files.internal("modelos/jogador.gltf"));
		this.modelo = new ModelInstance(asset.scene.model);
	}
	
	public Jogador() {
		if(modo != 2) {
			// itens iniciais:
			inv.itens[0] = new Inventario.Item(1, "Grama", Texturas.texs.get("grama_lado"), 1);
			inv.itens[1] = new Inventario.Item(2, "Terra", Texturas.texs.get("terra"), 1);
			inv.itens[2] = new Inventario.Item(3, "Pedra", Texturas.texs.get("pedra"), 1);
			inv.itens[3] = new Inventario.Item(4, "Agua", Texturas.texs.get("agua"), 1);
		}
	}
	
	public void interagirBloco() {
		Ray raio = camera.getPickRay(
			Gdx.graphics.getWidth() / 2f,
			Gdx.graphics.getHeight() / 2f);
		float olhoX = raio.origin.x;
		float olhoY = raio.origin.y;
		float olhoZ = raio.origin.z;
		float dirX = raio.direction.x;
		float dirY = raio.direction.y;
		float dirZ = raio.direction.z;

		for(float t = 0; t < ALCANCE; t += 0.25f) { // passo menor = mais preciso
			int x = Mat.floor(olhoX + dirX * t);
			int y = Mat.floor(olhoY + dirY * t);
			int z = Mat.floor(olhoZ + dirZ * t);

			int bloco = Mundo.obterBlocoMundo(x, y, z);
			if(bloco > 0) {
				if(blocoSele == 0) {
					if(modo == 2) inv.addItem(bloco, 1);
					Mundo.defBlocoMundo(x, y, z, 0);
				} else {
					int xAnt = Mat.floor(olhoX + dirX * (t - 0.25f));
					int yAnt = Mat.floor(olhoY + dirY * (t - 0.25f));
					int zAnt = Mat.floor(olhoZ + dirZ * (t - 0.25f));

					if(Mundo.obterBlocoMundo(xAnt, yAnt, zAnt) == 0) {
						blocoBox.set(minVec.set(xAnt, yAnt, zAnt), maxVec.set(xAnt + 1, yAnt + 1, zAnt + 1));
						attHitbox();
						if(blocoBox.intersects(hitbox)) return;
						Mundo.defBlocoMundo(xAnt, yAnt, zAnt, blocoSele);
						
						if(modo == 2) inv.rmItem(inv.slotSelecionado, 1);
					}
				}
				return;
			}
		}
	}

	public void attHitbox() {
		float x = posicao.x;
		float y = posicao.y;
		float z = posicao.z;

		hitbox.set(minVec.set(x - largura / 2, y, z - profundidade / 2), maxVec.set(x + largura / 2, y + altura, z + profundidade / 2));
	}

	public boolean colideComMundo() {
		int minX = Mat.floor(hitbox.min.x);
		int maxX = Mat.floor(hitbox.max.x);
		int minY = Mat.floor(hitbox.min.y);
		int maxY = Mat.floor(hitbox.max.y);
		int minZ = Mat.floor(hitbox.min.z);
		int maxZ = Mat.floor(hitbox.max.z);

		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					int bloco = Mundo.obterBlocoMundo(x, y, z);
					
					naAgua = false;
					
					if(bloco > 0) {
						blocoBox.set(minVec.set(x, y, z), maxVec.set(x + 1, y + 1, z + 1));
						if(bloco == 4) {
							naAgua = true;
							return false;
						}
						if(hitbox.intersects(blocoBox)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void att(float delta) {
		// gravidade no sobrevivencia
		if(naAgua) {
			GRAVIDADE = -10;
		} else {
			GRAVIDADE = -30;
		}
		if(modo == 2 && !noChao || naAgua) { 
            this.velocidade.y += GRAVIDADE * delta;

			if(this.velocidade.y < VELO_MAX_QUEDA) {
				this.velocidade.y = VELO_MAX_QUEDA;
			}
        }
        if(modo == 0) {
			posicao.add(velocidade.x * delta, velocidade.y * delta, velocidade.z * delta);
			attHitbox();

            camera.position.set(posicao.x, posicao.y + altura * 0.95f, posicao.z);
            return;
        }
        float dx = velocidade.x * delta;
        float dy = velocidade.y * delta;
		float dz = velocidade.z * delta;

        attHitbox();
		
        noChao = false;

		posicao.x += dx;
		attHitbox();
		if(colideComMundo()) {
			posicao.x -= dx;
			velocidade.x = 0;
		}
        posicao.z += dz;
        attHitbox();
        if(colideComMundo()) {
            posicao.z -= dz; 
            velocidade.z = 0;
        }
        posicao.y += dy;
        attHitbox();
        if(colideComMundo()) {
            posicao.y -= dy;
            if(dy < 0) {
                noChao = true;
            }
            velocidade.y = 0;
        }
        camera.position.set(posicao.x, posicao.y + altura * 0.9f, posicao.z);
		
		if(posicao.y < -100f) {
			posicao.y = 80f;
		}
    }
	
	public static interface Evento {
		public void aoAndar();
		public void aoVoar();
		public void aoInteragir();
		public void aoMorrer();
		public void aoColidir(byte bloco);
		public void aoLevarDano(int dano, String motivo);
		public void blocoAbaixo(byte bloco, int acao);
		public void slotAtual(Inventario.Item slot, int indice);
		
		public void aoIniciar();
		public void porFrame(float delta);
		public void aoFim();
	}
}
