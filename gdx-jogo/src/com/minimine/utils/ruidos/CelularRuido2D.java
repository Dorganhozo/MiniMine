package com.minimine.utils.ruidos;

public class CelularRuido2D {
    public final int[] p = new int[512];

    public CelularRuido2D(long semente) {
        // usando a mesma logica de embaralhamento
        int[] perm = new int[256];
        for(int i = 0; i < 256; i++) perm[i] = i;

        for(int i = 255; i > 0; i--) {
            long m = semente + i;
            m ^= m >>> 33;
            m *= 0xff51afd7ed558ccdL;
            m ^= m >>> 33;
            int j = (int) (Math.abs(m) % (i + 1));
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }
        for(int i = 0; i < 512; i++) p[i] = perm[i & 255];
    }

    public double ruido(double x, double y) {
		int xInt = (int)Math.floor(x);
		int yInt = (int)Math.floor(y);

		double distMinima = Double.POSITIVE_INFINITY;

		// calcula e usa o ponto
		for(int i = -1; i <= 1; i++) {
			for(int j = -1; j <= 1; j++) {
				int cx = xInt + i;
				int cy = yInt + j;
				
				// pega uma posição aleatoria de um ponto dentro de uma celula da grade
				// calcula o hash
				int h = p[(p[cx & 255] + cy) & 255];

				// calcula X e Y com
				double px = cx + ((double)(p[h & 255]) / 255.0);
				double py = cy + ((double)(p[(h + 1) & 255]) / 255.0);

				double dx = px - x;
				double dy = py - y;
				double dist = dx * dx + dy * dy;

				if(dist < distMinima) distMinima = dist;
			}
		}
		return Math.sqrt(distMinima);
	}
}
