package com.minimine.entidades;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.collision.Ray;
import com.minimine.utils.Mat;
import com.minimine.mundo.blocos.Bloco;
import com.minimine.graficos.TipoRender;
import com.minimine.audio.Audio;
import com.minimine.mundo.Mundo;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.minimine.cenas.Jogo;
import com.minimine.graficos.Modelos;
import com.minimine.mundo.blocos.InterfaceBloco;
import com.minimine.inventario.Inventario;
import com.badlogic.gdx.math.MathUtils;
import com.minimine.inventario.ItemRegistro;
import com.minimine.graficos.Texturas;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

public class Jogador extends Entidade {
	public int modo = 2;
	public PerspectiveCamera camera;
	public float forcaMov = 0;

	public CharSequence item = "ar";
	public CharSequence itemCache = "ar";
	public int ALCANCE = 7;
	public Inventario inv;

	public ModelInstance instancia;

	public float tempoAnimacao = 0;
	public float tempoDuploPulo = 0f;
	public static final float JANELA_DUPLO_PULO = 0.3f;

	public Quaternion rotTemp = new Quaternion();
	public Vector3 eulerTemp = new Vector3();

	public Node cabeca, tronco, bracoDir, bracoEsq, pernaDir, pernaEsq, itemPos;

	public Quaternion rotCabeca = new Quaternion();
	public Quaternion rotTronco = new Quaternion();
	public Quaternion rotBracoDir = new Quaternion();
	public Quaternion rotBracoEsq = new Quaternion();
	public Quaternion rotPernaDir = new Quaternion();
	public Quaternion rotPernaEsq = new Quaternion();

	public ModelInstance modeloItem;
	
	public Jogador() {
		super();
		vida = 20;
		vidaMax = 20;
		this.inv = new Inventario(this);
		Jogo.relogio.schedule(
			new java.util.TimerTask() {
				@Override
				public void run() {
					if(!Mundo.carregado) return;
					bioma = Mundo.motor.obterBioma((int)posicao.x, (int)posicao.z);
				}
			}, 0, 500);
		try {
			instancia = new ModelInstance(Modelos.obterModelo("modelos/jogador.gltf"));
			pegarNos();
			salvarRotacoes();
		} catch(Exception e) {
			Gdx.app.error("[Jogador]", "Erro no GLTF: " + e.getMessage());
		}
		bracoDir.rotation.set(rotBracoDir);
		bracoDir.rotation.mul(new Quaternion(Vector3.X, 100f));
		instancia.calculateTransforms();
	}

	@Override
	public void morreu() {
		posicao = new Vector3(0f, 0f, 0f);
		posicao.y = Mundo.obterAlturaChao((int)posicao.x, (int)posicao.z);
		Mundo.limparChunks(0, 0);
		Mundo.carregado = false;
		velocidade.set(0, 0, 0);
		vida = vidaMax;
		tempoInvulneravel = 3f;
	}

	public void interagirBloco() {
		final Ray raio = camera.getPickRay(
			Gdx.graphics.getWidth() / 2f,
			Gdx.graphics.getHeight() / 2f
		);
		float olhoX = raio.origin.x;
		float olhoY = raio.origin.y;
		float olhoZ = raio.origin.z;
		float dirX = raio.direction.x;
		float dirY = raio.direction.y;
		float dirZ = raio.direction.z;

		for(float t = 0; t < ALCANCE; t += 0.10f) {
			final int x = Mat.floor(olhoX + dirX * t);
			final int y = Mat.floor(olhoY + dirY * t);
			final int z = Mat.floor(olhoZ + dirZ * t);

			final Bloco bloco = Bloco.numIds.get(Mundo.obterBlocoMundo(x, y, z));

			if(bloco != null) {
				if(item.equals("ar") || bloco.render == TipoRender.LIQUIDO) {
					if(modo == 2) inv.addItem(bloco.nome, 1);
					Mundo.defBlocoMundo(x, y, z, item);
					Bloco.tocarSom(bloco.nome);
					if(bloco.evento != null) bloco.evento.aoDestruir(x, y, z);
				} else {
					if(bloco.ui != null) {
						bloco.ui.abrir(x, y, z);
						return;
					}
					int xAnt = Mat.floor(olhoX + dirX * (t - 0.25f));
					int yAnt = Mat.floor(olhoY + dirY * (t - 0.25f));
					int zAnt = Mat.floor(olhoZ + dirZ * (t - 0.25f));

					if(Mundo.obterBlocoMundo(xAnt, yAnt, zAnt) == 0) {
						blocoHitbox.set(minVec.set(xAnt, yAnt, zAnt), maxVec.set(xAnt + 1, yAnt + 1, zAnt + 1));
						attHitbox();
						if(blocoHitbox.intersects(hitbox)) return;

						Mundo.defBlocoMundo(xAnt, yAnt, zAnt, inv.itens[inv.slotSelecionado].nome);
						Bloco.tocarSom(item);
						Bloco blocoColocado = Bloco.texIds.get(item);
						if(blocoColocado != null && blocoColocado.evento != null)
							blocoColocado.evento.aoColocar(xAnt, yAnt, zAnt);

						if(modo == 2) inv.rmItem(inv.slotSelecionado, 1);
					}
				}
				return;
			}
		}
	}

	@Override
	public void att(float delta) {
		if(modo == 0) voando = true;
		if(tempoDuploPulo > 0f) tempoDuploPulo -= delta;
		super.att(delta);

		Inventario.Item itemInv = inv.itens[inv.slotSelecionado];
		if(itemInv != null && itemInv.nome != item) item = itemInv.nome;
		else if(itemInv == null) item = "ar";

		frenteV.x = camera.direction.x;
		frenteV.z = camera.direction.z;
		frenteV.nor();
		direitaV.x = frenteV.z;
		direitaV.z = -frenteV.x;

		if(naAgua) Mundo.GRAVIDADE = -10;
		else Mundo.GRAVIDADE = -30;

		if(!voando && !noChao || naAgua) {
			this.velocidade.y += Mundo.GRAVIDADE * delta;
			if(this.velocidade.y < VELO_MAX_QUEDA) {
				this.velocidade.y = VELO_MAX_QUEDA;
			}
		}
		if(modo == 0) {
			posicao.add(velocidade.x * delta, velocidade.y * delta, velocidade.z * delta);
			attHitbox();
			camera.position.set(posicao.x, posicao.y + altura * 0.95f, posicao.z);
			camera.update();
			return;
		}
		float dx = velocidade.x * delta;
		final float dy = velocidade.y * delta;
		float dz = velocidade.z * delta;

		posicao.y += dy;
		attHitbox();

		if(colideMundo()) {
			posicao.y -= dy;
			attHitbox();
			if(dy < 0) {
				noChao = true;
			} else if(dy > 0) {
				noChao = false;
			}
			velocidade.y = 0;
		} else {
			noChao = ehChao();
		}
		if(agachado && noChao && dx != 0 && !temSuporte(posicao.x + dx, posicao.z)) {
			dx = 0;
		}
		posicao.x += dx;
		attHitbox();
		if(colideMundo()) {
			posicao.x -= dx;
			velocidade.x = 0;
			attHitbox();
		}
		if(agachado && noChao && dz != 0 && !temSuporte(posicao.x, posicao.z + dz)) {
			dz = 0;
		}
		posicao.z += dz;
		attHitbox();
		if(colideMundo()) {
			posicao.z -= dz;
			velocidade.z = 0;
			attHitbox();
		}
		if(posicao.y < -100f) posicao.y = Mundo.obterAlturaChao((int)posicao.x, (int)posicao.z);

		if(movendo) {
			tempoAnimacao += delta * 8f;
			forcaMov = Math.min(1f, forcaMov + delta * 5f);
		} else {
			forcaMov = Math.max(0f, forcaMov - delta * 5f);
			if(forcaMov == 0) tempoAnimacao = 0;
		}
		camera.position.set(posicao.x, posicao.y + altura * 0.9f, posicao.z);
		camera.update();

		instancia.userData = dadosLuz;
		if(modeloItem != null) modeloItem.userData = dadosLuz;
	}

	public void render(ModelBatch mb) {
		if(!item.equals(itemCache)) {
			itemCache = item;
			modeloItem = Modelos.modeloItem(item);
		}
		instancia.transform.set(camera.view);

		if(Math.abs(instancia.transform.det()) > 1e-6f) {
			instancia.transform.inv();
		} else {
			camera.update();
			return;
		}
		float balancoX = MathUtils.sin(tempoAnimacao * 0.5f) * 0.05f;
		float balancoY = Math.abs(MathUtils.cos(tempoAnimacao)) * 0.05f;

		instancia.transform.translate(0.5f + balancoX, -2.15f + balancoY, -1f);
		instancia.transform.rotate(Vector3.Y, 15);

		instancia.calculateTransforms();

		mb.flush();
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

		if(modeloItem == null) mb.render(instancia);
		else {
			// mesma base: view invertida, sem o offset do braço
			modeloItem.transform.set(camera.view);
			modeloItem.transform.inv();
			mb.render(modeloItem);
		}
	}

	public void pegarNos() {
		cabeca = instancia.getNode("cabeca", true);
		tronco = instancia.getNode("tronco", true);
		bracoDir = instancia.getNode("braco_dir", true);
		bracoEsq = instancia.getNode("braco_esq", true);
		pernaDir = instancia.getNode("perna_dir", true);
		pernaEsq = instancia.getNode("perna_esq", true);
		itemPos = instancia.getNode("item", true);

		instancia.nodes.clear();
		instancia.nodes.add(bracoDir);
	}

	public void salvarRotacoes() {
		if(cabeca != null) rotCabeca.set(cabeca.rotation);
		if(tronco != null) rotTronco.set(tronco.rotation);
		if(bracoDir != null) rotBracoDir.set(bracoDir.rotation);
		if(bracoEsq != null) rotBracoEsq.set(bracoEsq.rotation);
		if(pernaDir != null) rotPernaDir.set(pernaDir.rotation);
		if(pernaEsq != null) rotPernaEsq.set(pernaEsq.rotation);
	}
}
