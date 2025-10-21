package com.minimine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import java.util.HashMap;
import java.util.Map;

public class Mundo {
    public static final int CHUNK_TAM = 16;
    public static final int CHUNK_ALTURA = 128;
    public byte[][][] blocos = new byte[CHUNK_TAM][CHUNK_ALTURA][CHUNK_TAM];
    public int seed;
	public Map<CharSequence, Material> mats = new HashMap<>();

    public Mundo(int seed) {
        this.seed = seed;
		mats.put("pedra", new Material(ColorAttribute.createDiffuse(new Color(0.5f,0.5f,0.5f,1f))));
        mats.put("terra", new Material(ColorAttribute.createDiffuse(new Color(0.35f,0.22f,0.12f,1f))));
        mats.put("grama", new Material(ColorAttribute.createDiffuse(new Color(0.16f,0.7f,0.12f,1f))));
    }

    public void gerarChunk(int chunkX, int chunkZ) {
        int mundoX = chunkX * CHUNK_TAM;
        int mundoZ = chunkZ * CHUNK_TAM;

        for(int x = 0; x < CHUNK_TAM; x++) {
            for(int z = 0; z < CHUNK_TAM; z++) {
                int altura = calcularAltura(mundoX + x, mundoZ + z);
                for(int y = 0; y < CHUNK_ALTURA; y++) {
                    if(y < altura - 3) blocos[x][y][z] = 1; // pedra
                    else if(y < altura - 1) blocos[x][y][z] = 2; // terra
                    else if(y < altura) blocos[x][y][z] = 3; // grama
                    else blocos[x][y][z] = 0; // ar
                }
            }
        }
    }

    public int calcularAltura(int vx, int vz) {
        double v = Math.sin(vx * 0.12 + seed * 0.001) + Math.cos(vz * 0.11 - seed * 0.0009);
        double v2 = Math.sin((vx + vz) * 0.07);
        int base = 64;
        int amp = 16;
        return base + (int)((v + v2) * 0.5 * amp);
    }

    public Model criarChunk(int chunkX, int chunkZ, ModelBuilder mb) {
        gerarChunk(chunkX, chunkZ);

        mb.begin();

        MeshPartBuilder pedraParte = mb.part("pedra", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, mats.get("pedra"));
        MeshPartBuilder terraParte  = mb.part("terra",  GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, mats.get("terra"));
        MeshPartBuilder gramaParte  = mb.part("grama",  GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, mats.get("grama"));

        int baseX = chunkX * CHUNK_TAM;
        int baseZ = chunkZ * CHUNK_TAM;

        for(int x = 0; x < CHUNK_TAM; x++) {
            for(int y = 0; y < CHUNK_ALTURA; y++) {
                for(int z = 0; z < CHUNK_TAM; z++) {
                    byte id = blocos[x][y][z];
                    if(id == 0) continue;

                    MeshPartBuilder mpb = (id == 1 ? pedraParte : (id == 2 ? terraParte : gramaParte));

                    boolean expPosX = eAr(x + 1, y, z);
                    boolean expNegX = eAr(x - 1, y, z);
                    boolean expPosY = eAr(x, y + 1, z);
                    boolean expNegY = eAr(x, y - 1, z);
                    boolean expPosZ = eAr(x, y, z + 1);
                    boolean expNegZ = eAr(x, y, z - 1);

                    float vx = baseX + x;
                    float vy = y;
                    float vz = baseZ + z;

                    if(expPosX) {
						mpb.rect(
							vx + 1f, vy + 1f, vz,
							vx + 1f, vy + 1f, vz + 1f,
							vx + 1f, vy, vz + 1f,
							vx + 1f, vy, vz,
							1f, 0f, 0f
						);
					}
					if(expNegX) {
						mpb.rect(
							vx, vy + 1f, vz + 1f, 
							vx, vy + 1f, vz,
							vx, vy, vz,
							vx, vy, vz + 1f,
							-1f, 0f, 0f
						);
					}
					if(expPosY) {
						mpb.rect(
							vx, vy + 1f, vz + 1f,
							vx + 1f, vy + 1f, vz + 1f,
							vx + 1f, vy + 1f, vz,
							vx, vy + 1f, vz,
							0f, 1f, 0f
						);
					}
					if(expNegY) {
						mpb.rect(
							vx, vy, vz,
							vx + 1f, vy, vz,
							vx + 1f, vy, vz + 1f,
							vx, vy, vz + 1f,
							0f, -1f, 0f
						);
					}
					if(expPosZ) {
						mpb.rect(
							vx + 1f, vy + 1f, vz + 1f,
							vx, vy + 1f, vz + 1f,
							vx, vy, vz + 1f,
							vx + 1f, vy, vz + 1f,
							0f, 0f, 1f
						);
					}
					if(expNegZ) {
						mpb.rect(
							vx, vy + 1f, vz,
							vx + 1f, vy + 1f, vz,
							vx + 1f, vy, vz,
							vx , vy, vz,
							0f, 0f, -1f
						);
					}
                }
            }
        }
        return mb.end();
    }

    public boolean eAr(int x, int y, int z) {
        if(x < 0 || x >= CHUNK_TAM || y < 0 || y >= CHUNK_ALTURA || z < 0 || z >= CHUNK_TAM) return true;
        return blocos[x][y][z] == 0;
    }
}
