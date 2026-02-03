package com.minimine.utils.ruidos;

public class Simplex2D {
    public final int[] permutacao = new int[512];
    
    // constantes matematicas de inclinação do espaço(Simplex)
    public static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    public static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

    // vetores de gradiente pra o calculo de direção
    public static final int[][] gradientes = {
        {1,1},{-1,1},{1,-1},{-1,-1},{1,0},{-1,0},{1,0},{-1,0},{0,1},{0,-1},{0,1},{0,-1}
    };

    // usa um misturador de bits(hash) pra garantir determinismo sem travas
    public Simplex2D(long semente) {
        int[] p = new int[256];
        for(short i = 0; i < 256; i++) p[i] = i;

        for(int i = 255; i > 0; i--) {
            int j = espalhar(i, semente) & 255;
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        // duplica a tabela pra evitar o uso de módulo(%) no loop de calculo
        for(int i = 0; i < 512; i++) {
            permutacao[i] = p[i & 255];
        }
    }

    // função de espalhamento de bits
    public int espalhar(int i, long s) {
        long h = s + i;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (int)h;
    }

    public double ruidoFractal(double x, double y, int oitavas, double persistencia, double lacunaridade) {
        double total = 0;
        double frequencia = 1;
        double amplitude = 1;
        double maximo = 0; 

        for(int i = 0; i < oitavas; i++) {
            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
            amplitude *= persistencia;
            frequencia *= lacunaridade;
        }
        return total / maximo;
    }

    // retorna um valor entre -1.0 e 1.0.
    public double ruido(double x, double y) {
        double n0, n1, n2;
        double s = (x + y) * F2;
        int i = (int)Math.floor(x + s);
        int j = (int)Math.floor(y + s);
        double t = (i + j) * G2;
        double x0 = x - (i - t);
        double y0 = y - (j - t);

        int i1 = (x0 > y0) ? 1 : 0;
        int j1 = (x0 > y0) ? 0 : 1;

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;

        int ii = i & 255;
        int jj = j & 255;

        // vertice 0
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if(t0 < 0) n0 = 0.0;
        else {
            t0 *= t0;
            int g = permutacao[ii + permutacao[jj]] % 12;
            n0 = t0 * t0 * (gradientes[g][0] * x0 + gradientes[g][1] * y0);
        }
        // vertice 1
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if(t1 < 0) n1 = 0.0;
        else {
            t1 *= t1;
            int g = permutacao[ii + i1 + permutacao[jj + j1]] % 12;
            n1 = t1 * t1 * (gradientes[g][0] * x1 + gradientes[g][1] * y1);
        }
        // vertice 2
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if(t2 < 0) n2 = 0.0;
        else {
            t2 *= t2;
            int g = permutacao[ii + 1 + permutacao[jj + 1]] % 12;
            n2 = t2 * t2 * (gradientes[g][0] * x2 + gradientes[g][1] * y2);
        }
        return 70.0 * (n0 + n1 + n2);
    }
}
