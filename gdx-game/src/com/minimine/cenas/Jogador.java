package com.minimine.cenas;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.minimine.utils.PerlinNoise3D;

public class Jogador {
	public byte modo = 0; // 0 = espectador, 1 = criativo, 2 = sobrevivencia
	public PerspectiveCamera camera;
	public Vector3 posicao = new Vector3(0, 80, 0), velocidade = new Vector3();

	public float largura = 0.6f, altura = 1.8f, profundidade = 0.6f;
	public boolean noChao = true;
	public BoundingBox hitbox = new BoundingBox();
	public static final BoundingBox blocoBox = new BoundingBox();
	public static final Vector3 minVec = new Vector3(), maxVec = new Vector3();
	
	public static final float GRAVIDADE = -30f, VELO_MAX_QUEDA = -50f;
	
	public byte blocoSele = 1;
	public CharSequence item = "Grama";
	public static final float ALCANCE = 6f;
	public Inventario inv;

	public void interagirComBloco() {
		float dirX = camera.direction.x;
		float dirY = camera.direction.y;
		float dirZ = camera.direction.z;

		float olhoX = camera.position.x;
		float olhoY = camera.position.y;
		float olhoZ = camera.position.z;

		for(float t = 0; t < ALCANCE; t += 0.5f) {
			int x = PerlinNoise3D.floorRapido(olhoX + dirX * t);
			int y = PerlinNoise3D.floorRapido(olhoY + dirY * t);
			int z = PerlinNoise3D.floorRapido(olhoZ + dirZ * t);

			byte bloco = Mundo.obterBlocoMundo(x, y, z);

			if(bloco > 0) {
				if(blocoSele == 0) { // ar = destruir
					Mundo.defBlocoMundo(x, y, z, (byte)0);
				} else {
					int xAnt = PerlinNoise3D.floorRapido(olhoX + dirX * (t - 0.5f));
					int yAnt = PerlinNoise3D.floorRapido(olhoY + dirY * (t - 0.5f));
					int zAnt = PerlinNoise3D.floorRapido(olhoZ + dirZ * (t - 0.5f));

					if(Mundo.obterBlocoMundo(xAnt, yAnt, zAnt) == 0) {
						Mundo.defBlocoMundo(xAnt, yAnt, zAnt, blocoSele);
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
		int minX = PerlinNoise3D.floorRapido(hitbox.min.x);
		int maxX = PerlinNoise3D.floorRapido(hitbox.max.x);
		int minY = PerlinNoise3D.floorRapido(hitbox.min.y);
		int maxY = PerlinNoise3D.floorRapido(hitbox.max.y);
		int minZ = PerlinNoise3D.floorRapido(hitbox.min.z);
		int maxZ = PerlinNoise3D.floorRapido(hitbox.max.z);

		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					byte bloco = Mundo.obterBlocoMundo(x, y, z);

					if(bloco > 0) {
						blocoBox.set(minVec.set(x, y, z), maxVec.set(x + 1, y + 1, z + 1));
						if(hitbox.intersects(blocoBox)) return true;
					}
				}
			}
		}
		return false;
	}
	
	public void att(float delta) {
		if(blocoSele == 0) item = "Ar";
		else if(blocoSele == 1) item = "Grama";
		else if(blocoSele == 2) item = "Terra";
		else if(blocoSele == 3) item = "Pedra";
		// gravidade no sobrevivencia
		if(modo == 2 && !noChao) { 
            this.velocidade.y += GRAVIDADE * delta;

			if(this.velocidade.y < VELO_MAX_QUEDA) {
				this.velocidade.y = VELO_MAX_QUEDA;
			}
        }
        if(modo == 0) {
			posicao.add(velocidade.x * delta, velocidade.y * delta, velocidade.z * delta);
			attHitbox();

            camera.position.set(posicao.x, posicao.y + altura * 0.9f, posicao.z);
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
    }
}
