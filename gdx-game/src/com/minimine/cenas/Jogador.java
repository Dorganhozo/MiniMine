package com.minimine.cenas;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.minimine.PerlinNoise3D;

public class Jogador {
	public byte modo = 2; // 0 = espectador, 1 = criativo, 2 = sobrevivencia
	public PerspectiveCamera camera;
	public Vector3 posicao = new Vector3(0, 80, 0);
	public Vector3 velocidade = new Vector3();

	public float largura = 0.6f;
	public float altura = 1.8f;
	public float profundidade = 0.6f;
	public boolean noChao = true;
	public BoundingBox hitbox = new BoundingBox();
	public static final BoundingBox blocoBox = new BoundingBox();
	public static final Vector3 minVec = new Vector3();
	public static final Vector3 maxVec = new Vector3();
	
	public static final float GRAVIDADE = -30f;
	public static final float VELO_MAX_QUEDA = -50f;

	public void attHitbox() {
		float x = posicao.x;
		float y = posicao.y;
		float z = posicao.z;

		hitbox.set(minVec.set(x - largura / 2, y, z - profundidade / 2),
				   maxVec.set(x + largura / 2, y + altura, z + profundidade / 2));
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
