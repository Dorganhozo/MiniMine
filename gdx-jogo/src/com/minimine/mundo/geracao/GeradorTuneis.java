package com.minimine.mundo.geracao;

import com.minimine.mundo.chunks.Chunk;
import com.minimine.mundo.Mundo;
import com.minimine.mundo.Chave;
import com.minimine.mundo.chunks.ChunkProcesso;

public final class GeradorTuneis {
    public static final int VERMES_POR_CHUNK = 6;
    public static final float RAIO_MIN = 1.5f;
    public static final float RAIO_MAX = 3.0f;
    public static final int MIN_Y = 1;
    public static final float VAR_DIRECAO = 0.07f; // variação por passo no vetor

    public final long semente;

    public GeradorTuneis(long semente) {
        this.semente = semente;
    }

    public void escavar(Chunk chunk, int chunkX, int chunkZ) {
        long semChunk = embaralhar(semente ^ ((long) chunk.x * 0x4A39395EEL ^ (long) chunk.z * 0x6C62272EL));
        for(int v = 0; v < VERMES_POR_CHUNK; v++) {
            semChunk = embaralhar(semChunk);
            escavarVerme(chunk, chunkX, chunkZ, semChunk);
        }
    }

    public void escavarVerme(Chunk chunkOrigem, int chunkX, int chunkZ, long sem) {
        sem = embaralhar(sem);
        float ox = chunkX + (int)(sem & 15);
        sem = embaralhar(sem);
        float oz = chunkZ + (int)(sem & 15);
        sem = embaralhar(sem);
        float oy = MIN_Y + (int)((sem & 0xFFL) % (Mundo.Y_CHUNK - MIN_Y));

        // direção inicial como vetor normalizado
        sem = embaralhar(sem);
        float dx = ((sem & 0xFFFFL) / 32767.5f) - 1.0f;
        sem = embaralhar(sem);
        float dy = (((sem & 0xFFFFL) / 32767.5f) - 1.0f) * 0.3f; // componente vertical menor
        sem = embaralhar(sem);
        float dz = ((sem & 0xFFFFL) / 32767.5f) - 1.0f;
        float inv = 1.0f / (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
        dx *= inv; dy *= inv; dz *= inv;

        sem = embaralhar(sem);
        float raio = RAIO_MIN + (sem & 0xFFL) / 255.0f * (RAIO_MAX - RAIO_MIN);

        int passos = 200 + (int)(sem & 127); // 200-327

        int chunkCX = chunkX >> 4;
        int chunkCZ = chunkZ >> 4;

        for(int p = 0; p < passos; p++) {
            ox += dx;
            oy += dy;
            oz += dz;

            // perturba direção incrementalmente, sem cos/sin
            sem = embaralhar(sem);
            dx += ((sem & 0xFFFFL) / 32767.5f - 1.0f) * VAR_DIRECAO;
            sem = embaralhar(sem);
            dy += ((sem & 0xFFFFL) / 32767.5f - 1.0f) * VAR_DIRECAO * 0.5f;
            sem = embaralhar(sem);
            dz += ((sem & 0xFFFFL) / 32767.5f - 1.0f) * VAR_DIRECAO;
            // renormaliza a cada passo
            inv = 1.0f / (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
            dx *= inv; dy *= inv; dz *= inv;

            if(oy < MIN_Y) break;

            final float raio2 = raio * raio;
            final int x0 = (int)(ox - raio) - 1;
            final int x1 = (int)(ox + raio) + 1;
            final int y0 = (int)(oy - raio) - 1;
            final int y1 = (int)(oy + raio) + 1;
            final int z0 = (int)(oz - raio) - 1;
            final int z1 = (int)(oz + raio) + 1;

            for(int by = y0; by <= y1; by++) {
                if(by < MIN_Y || by >= Mundo.Y_CHUNK) continue;
                final float ddy = by - oy; float ddy2 = ddy * ddy;

                for(int bz = z0; bz <= z1; bz++) {
                    final float ddz = bz - oz; float ddz2 = ddz * ddz;
                    if(ddy2 + ddz2 > raio2) continue; // rejeita cedo

                    for(int bx = x0; bx <= x1; bx++) {
                        final float ddx = bx - ox;
                        if(ddx*ddx + ddy2 + ddz2 > raio2) continue;

                        final int cx = bx >> 4;
                        final int cz = bz >> 4;
                        final int lx = bx - (cx << 4);
                        final int lz = bz - (cz << 4);

                        // só escava dentro da própria chunk
                        // escrever em vizinhas causa race condition com outras threads gerando malha
                        if(cx != chunkCX || cz != chunkCZ) continue;
                        ChunkProcesso.util.defBloco(lx, by, lz, 0, chunkOrigem);
                    }
                }
            }
        }
    }

    public static long embaralhar(long s) {
        s ^= s >>> 33;
        s *= 0xFF51AFD7ED558CCDL;
        s ^= s >>> 33;
        s *= 0xC4CEB9FE1A85EC53L;
        s ^= s >>> 33;
        return s;
    }
}


