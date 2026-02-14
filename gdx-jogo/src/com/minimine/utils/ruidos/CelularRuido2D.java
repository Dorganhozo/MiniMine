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

    // retorna a posição aleatoria de um ponto dentro de uma celula da grade
    public double obterPontoQuadrado(int x, int y, int eixo) {
        int h = p[(p[x & 255] + y) & 255] + eixo;
        // gera um valor "aleatório" entre 0 e 1 baseado no hash
        return (double)(p[h & 255]) / 255.0;
    }

    public double ruido(double x, double y) {
        int xInt = (int)Math.floor(x);
        int yInt = (int)Math.floor(y);

        double distMinima = 1.0e10;

        // verifica o quadrado atual e os 8 vizinhos
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                int cx = xInt + i;
                int cy = yInt + j;

                // posição do ponto dentro da celula vizinha
                double px = cx + obterPontoQuadrado(cx, cy, 0);
                double py = cy + obterPontoQuadrado(cx, cy, 1);

                double dx = px - x;
                double dy = py - y;
                double dist = dx * dx + dy * dy; // distancia euclidiana ao quadrado

                if(dist < distMinima) distMinima = dist;
            }
        }
        // Retorna a raiz para ter a distância real (ajustado para range 0-1)
        return Math.sqrt(distMinima);
    }
}
