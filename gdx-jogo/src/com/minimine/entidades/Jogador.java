package com.minimine.entidades;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.collision.Ray;
import com.minimine.utils.Mat;
import com.minimine.mundo.blocos.Bloco;
import com.minimine.audio.Audio;
import com.minimine.mundo.Mundo;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Quaternion;

public class Jogador extends Entidade {
	public ModeloJogador modelo;
	
	public int modo = 0; // 0 = espectador, 1 = criativo, 2 = sobrevivencia
	public PerspectiveCamera camera;
	
	public final Vector3 frenteV = new Vector3(0, 0, 0), direitaV = new Vector3(0, 0, 0);
	public float pulo = 10f;

	public CharSequence item = "ar";
	public int ALCANCE = 7;
	public Inventario inv = new Inventario(this);

	public Jogador() {
		super();
	}
	
	public void criarModelo3D() {
		modelo = new ModeloJogador();
		
		// deixa o braço reto pra frente
		modelo.bracoDir.rotation.set(modelo.rotBracoDir);
		// rotaciona 90 graus no eixo X
		modelo.bracoDir.rotation.mul(new Quaternion(Vector3.X, 90f));
		modelo.instancia.calculateTransforms();
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

		for(float t = 0; t < ALCANCE; t += 0.10f) { // passo menor = mais preciso
			int x = Mat.floor(olhoX + dirX * t);
			int y = Mat.floor(olhoY + dirY * t);
			int z = Mat.floor(olhoZ + dirZ * t);

			Bloco bloco = Bloco.numIds.get(Mundo.obterBlocoMundo(x, y, z));

			if(bloco != null) {
				if(item.equals("ar")) {
					if(modo == 2) inv.addItem(bloco.nome, 1);
					Mundo.defBlocoMundo(x, y, z, "ar");
					Bloco.tocarSom(bloco.nome);
				} else {
					int xAnt = Mat.floor(olhoX + dirX * (t - 0.25f));
					int yAnt = Mat.floor(olhoY + dirY * (t - 0.25f));
					int zAnt = Mat.floor(olhoZ + dirZ * (t - 0.25f));

					if(Mundo.obterBlocoMundo(xAnt, yAnt, zAnt) == 0) {
						blocoHitbox.set(minVec.set(xAnt, yAnt, zAnt), maxVec.set(xAnt + 1, yAnt + 1, zAnt + 1));
						attHitbox();
						if(blocoHitbox.intersects(hitbox)) return;
						Mundo.defBlocoMundo(xAnt, yAnt, zAnt, item);
						Bloco.tocarSom(item);

						if(modo == 2) inv.rmItem(inv.slotSelecionado, 1);
					}
				}
				return;
			}
		}
	}

	@Override
	public void att(float delta) {
		frenteV.x = camera.direction.x;
		frenteV.z = camera.direction.z;
		frenteV.nor();  
		direitaV.x = frenteV.z;
		direitaV.z = -frenteV.x;

		velocidade.x = 0;
		velocidade.z = 0;
		if(modo != 2) velocidade.y = 0;

		if(this.frente) velocidade.add(frenteV.cpy().scl(velo));
		if(this.tras)  velocidade.sub(frenteV.cpy().scl(velo));
		if(this.esquerda) velocidade.add(direitaV.cpy().scl(velo));
		if(this.direita) velocidade.sub(direitaV.cpy().scl(velo));
		if(this.cima) {
			if(modo != 2 || noChao || naAgua) {
				velocidade.y = pulo; // pulo
				noChao = false;
			}
        }
        if(this.baixo) velocidade.y = -10f;

		// gravidade no sobrevivencia
		if(naAgua) GRAVIDADE = -10;
		else GRAVIDADE = -30;

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
		// primeiro verifica colisão vertical
		posicao.y += dy;
		attHitbox();

		if(colideMundo()) {
			posicao.y -= dy;
			attHitbox(); // atualiza hitbox apos corrigir posição
			// verifica se ta colidindo por baixo(pé no chão)
			if(dy < 0) {
				noChao = true;
			} else if(dy > 0) {
				// colisão por cima(cabeça)
				noChao = false;
			}
			velocidade.y = 0;
		} else {
			// se não ha colisão vertical, verifica se ta no chão usando uma verificação mais precisa
			noChao = ehChao();
		}
		// agora processa movimento horizontal
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
		camera.position.set(posicao.x, posicao.y + altura * 0.9f, posicao.z);

		if(posicao.y < -100f) {
			posicao.y = 80f;
		}
	}

	public boolean ehChao() {
		// verifica se ha blocos solidos logo abaixo dos pes do jogador
		float epsilon = 0.05f; // margem pra evitar flutuação
		float yCheque = posicao.y - epsilon;

		int minX = Mat.floor(posicao.x - largura / 2);
		int maxX = Mat.floor(posicao.x + largura / 2);
		int y = Mat.floor(yCheque);
		int minZ = Mat.floor(posicao.z - profundidade / 2);
		int maxZ = Mat.floor(posicao.z + profundidade / 2);

		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				int id = Mundo.obterBlocoMundo(x, y, z);
				if(id != 0) {
					Bloco b = Bloco.numIds.get(id);
					if(b != null && b.solido) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void render(ModelBatch sb) {
		if(modelo == null) return;

		// sincroniza o modelo visual com a logica do jogador
		modelo.instancia.transform.setToTranslation(posicao);
		
		// aplica a rotação(yaw) da camera ao corpo
		float anguloRotacao = ((float)Math.toDegrees(-Math.atan2(camera.direction.z, camera.direction.x)) - 90);
		modelo.instancia.transform.rotate(Vector3.Y, anguloRotacao);
		
		// renderiza
		sb.begin(camera);
		modelo.render(sb);
		sb.end();
	}
	@Override
	public void liberar() {
		if(modelo != null) modelo.liberar();
	}
}
