package com.minimine.entidades;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.minimine.mundo.Mundo;
import com.minimine.utils.Mat;
import com.minimine.mundo.blocos.Bloco;

public class Entidade {
	public int vida;
	public float velo = 8f; // tem uma velocidade
	public float peso = 65f; // 65 kg
	public boolean esquerda = false, frente = false, tras = false, direita = false, cima = false, baixo = false, acao = false;
	public boolean noChao = true, naAgua = false, agachado = false, nasceu = false;
	
	public Vector3 posicao = new Vector3(1, 80, 1), velocidade = new Vector3();
	public float largura = 0.6f, altura = 1.9f, profundidade = 0.6f;

	public BoundingBox hitbox = new BoundingBox();
	public final BoundingBox blocoHitbox = new BoundingBox(); // colisão dos blocos
	
	public final Vector3 minVec = new Vector3(), maxVec = new Vector3();
	
	public float yaw = 180f, tom = -20f; // pra ver onde ta olhando
	public float GRAVIDADE = -30f, VELO_MAX_QUEDA = -50f;
	
	public Entidade() {
		yaw = 180f;
		tom = -20f;
		GRAVIDADE = -30f;
		VELO_MAX_QUEDA = -50f;
		largura = 0.6f;
		altura = 1.9f;
		profundidade = 0.6f;
		hitbox = new BoundingBox();
	}
	
	public void attHitbox() {
		float x = posicao.x;
		float y = posicao.y;
		float z = posicao.z;

		hitbox.set(minVec.set(x - largura / 2, y, z - profundidade / 2), maxVec.set(x + largura / 2, y + altura, z + profundidade / 2));
	}
	
	public boolean colideMundo() {
		int minX = Mat.floor(hitbox.min.x);
		int maxX = Mat.floor(hitbox.max.x);
		int minY = Mat.floor(hitbox.min.y);
		int maxY = Mat.floor(hitbox.max.y);
		int minZ = Mat.floor(hitbox.min.z);
		int maxZ = Mat.floor(hitbox.max.z);

		naAgua = false;

		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {

					int id = Mundo.obterBlocoMundo(x, y, z);
					if(id == 0) continue;

					Bloco b = Bloco.numIds.get(id);

					blocoHitbox.set(
						minVec.set(x, y, z),
						maxVec.set(x + 1, y + 1, z + 1)
					);
					if(!b.solido) {
						naAgua = true;
						continue;
					} else {
						if(hitbox.intersects(blocoHitbox)) return true;
					}
				}
			}
		}
		return false;
	}
	
	// para verificar se tem chão embaixo dos pés da entidade
	public boolean temSuporte(float x, float z) {
		// 1. configura uma hitbox temporaria na nova posição(x, posicao.y, z)
		float yBase = posicao.y;
		// usa blocoBox temporariamente pra a verificação, configurando na nova posição
		blocoHitbox.set(
			minVec.set(x - largura / 2, yBase, z - profundidade / 2), 
			maxVec.set(x + largura / 2, yBase + altura, z + profundidade / 2)
		);
		// 2. define a area de busca: um pouco abaixo da base da hitbox
		int minX = Mat.floor(blocoHitbox.min.x);
		int maxX = Mat.floor(blocoHitbox.max.x);
		// checa o bloco imediatamente abaixo da base(yBase - 0.1f)
		int yCheque = Mat.floor(yBase - 0.1f); 
		int minZ = Mat.floor(blocoHitbox.min.z);
		int maxZ = Mat.floor(blocoHitbox.max.z);

		for(int atualX = minX; atualX <= maxX; atualX++) {
			for(int atualZ = minZ; atualZ <= maxZ; atualZ++) {
				int id = Mundo.obterBlocoMundo(atualX, yCheque, atualZ);
				if(id != 0) {
					Bloco b = Bloco.numIds.get(id);
					// se encontrar um bloco solido na camada de checagem, ha suporte
					if(b != null && b.solido) return true;
				}
			}
		}
		// não encontrou suporte solido em nenhuma parte da area debaixo
		return false;
	}
	
	public void att(float delta) {}
	
	public void liberar() {
		
	}
}
