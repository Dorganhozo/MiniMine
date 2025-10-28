package com.minimine.cenas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.cenas.blocos.Luz;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.minimine.utils.ChunkUtil;
/*
public class LuzUtil {
    public static int TAM_CHUNK = Mundo.TAM_CHUNK;
    public static Pixmap luzPx;
    public static Texture luzTextura;
    public static Mundo mundo;
    public static final float EPS = 1e-5f;
	
    public static float[][][] att(Chunk chunkAlvo) {
        int ax = chunkAlvo.chunkX;
        int az = chunkAlvo.chunkZ;

        int raChunks = Mundo.RAIO_CHUNKS;
        final List<LuzFonte> fontes = new ArrayList<>(64);
        float maxRaio = 0f;

        for(Map.Entry<ChunkUtil.Chave, Chunk> e : mundo.chunksAtivos.entrySet()) {
            ChunkUtil.Chave chave = e.getKey();
			
            int cx = chave.x;
            int cz = chave.z;
            if(Math.abs(cx - ax) > raChunks || Math.abs(cz - az) > raChunks) continue;
            Chunk sc = e.getValue();
            if(sc == null) continue;
            if(sc.luzes == null || sc.luzes.isEmpty()) continue;
            for(Luz l : sc.luzes) {
                int gx = l.x + cx * TAM_CHUNK;
                int gz = l.z + cz * TAM_CHUNK;
                float lr = (l.cor != null) ? l.cor.r : 0f;
                float lg = (l.cor != null) ? l.cor.g : 0f;
                float lb = (l.cor != null) ? l.cor.b : 0f;
                float raio = l.raio;
                fontes.add(new LuzFonte(gx, gz, lr, lg, lb, raio));
                if(raio > maxRaio) maxRaio = raio;
            }
        }
        int maxRaioBlocos = (int)Math.ceil(maxRaio) + 1;
        
        int minGX = ax * TAM_CHUNK - maxRaioBlocos;
        int maxGX = ax * TAM_CHUNK + TAM_CHUNK - 1 + maxRaioBlocos;
        int minGZ = az * TAM_CHUNK - maxRaioBlocos;
        int maxGZ = az * TAM_CHUNK + TAM_CHUNK - 1 + maxRaioBlocos;

        final Map<Long, Float> melhorSe = new HashMap<>(2048);
        final float[][][] acumu = new float[TAM_CHUNK][TAM_CHUNK][3];

        Deque<Node> q = new ArrayDeque<>();
        for(LuzFonte s : fontes) {
            if(s.x < minGX - 1 || s.x > maxGX + 1 || s.z < minGZ - 1 || s.z > maxGZ + 1) continue;
            float soma = s.r + s.g + s.b;
            if(soma <= EPS) continue;
			
            float fator;
            if(s.raio <= 1.0f) fator = 0.0f;
            else {
                fator = 1.0f - (1.0f / s.raio);
                if(fator < 0f) fator = 0f;
                if(fator > 0.999f) fator = 0.999f;
            }
            q.addLast(new Node(s.x, s.z, s.r, s.g, s.b, fator));
            melhorSe.put(pack(s.x, s.z), soma);
        }
        while(!q.isEmpty()) {
            Node n = q.removeFirst();
            if(n.x >= ax * TAM_CHUNK && n.x < ax * TAM_CHUNK + TAM_CHUNK &&
                n.z >= az * TAM_CHUNK && n.z < az * TAM_CHUNK + TAM_CHUNK) {

                int lx = n.x - ax * TAM_CHUNK;
                int lz = n.z - az * TAM_CHUNK;
                
                acumu[lx][lz][0] += n.r;
                acumu[lx][lz][1] += n.g;
                acumu[lx][lz][2] += n.b;
            }
            float nr = n.r * n.decaimento;
            float ng = n.g * n.decaimento;
            float nb = n.b * n.decaimento;
            if(nr <= 0.001f && ng <= 0.001f && nb <= 0.001f) continue;

            propagar(n.x + 1, n.z, nr, ng, nb, n.decaimento, minGX, maxGX, minGZ, maxGZ, melhorSe, q);
            propagar(n.x - 1, n.z, nr, ng, nb, n.decaimento, minGX, maxGX, minGZ, maxGZ, melhorSe, q);
            propagar(n.x, n.z + 1, nr, ng, nb, n.decaimento, minGX, maxGX, minGZ, maxGZ, melhorSe, q);
            propagar(n.x, n.z - 1, nr, ng, nb, n.decaimento, minGX, maxGX, minGZ, maxGZ, melhorSe, q);
        }
		return acumu;
	}
	
    public static void propagar(int gx, int gz, float r, float g, float b, float decaimento, int minGX, int maxGX, int minGZ, int maxGZ, Map<Long, Float> melhor, Deque<Node> q) {
        if(gx < minGX || gx > maxGX || gz < minGZ || gz > maxGZ) return;
        long chave = pack(gx, gz);
        float soma = r + g + b;
        Float se = melhor.get(chave);
        if(se != null && soma <= se + 1e-6f) return;
        melhor.put(chave, soma);
        q.addLast(new Node(gx, gz, r, g, b, decaimento));
    }

    public static long pack(int x, int z) {
        return (((long)x) << 32) | (z & 0xffffffffL);
    }
    public static final class LuzFonte {
        int x, z;
        float r,g,b, raio;
        LuzFonte(int x, int z, float r, float g, float b, float raio) {
            this.x = x;
			this.z = z;
			this.r = r;
			this.g = g;
			this.b = b;
			this.raio = raio;
        }
    }
    public static final class Node {
        int x,z;
        float r,g,b;
        float decaimento;
        Node(int x,int z,float r,float g,float b,float decaimento){
			this.x=x;
			this.z=z;
			this.r=r;
			this.g=g;
			this.b=b;
			this.decaimento=decaimento;
		}
    }
    public static void liberar() {
		luzPx.dispose();
        luzTextura.dispose();
    }
}
*/
