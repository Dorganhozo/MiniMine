package com.minimine.utils.ruidos;

public class Simplex2D {
    public final int[] permutacao = new int[512];
    public final int[] permutacaoResto12 = new int[512]; // vetor veloz para os restos

    // constantes matematicas de inclinação do espaço(Simplex) resolvidas
    public static final double F2 = 0.3660254037844386;
    public static final double G2 = 0.21132486540518713;

    // usa um misturador de bits pra garantir determinismo sem travas
    public Simplex2D(long semente) {
        int[] p = new int[256];
        for(short i = 0; i < 256; i++) p[i] = i;

        for(int i = 255; i > 0; i--) {
            int j = espalhar(i, semente) & 255;
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        // duplica a tabela e ja resolve a matemática do resto de divisão
        for(int i = 0; i < 512; i++) {
            this.permutacao[i] = p[i & 255];
            this.permutacaoResto12[i] = this.permutacao[i] % 12;
        }
    }

    // função de espalhamento de bits
    public final int espalhar(int i, long s) {
        long h = s + i;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (int)h;
    }

    public final double ruidoFractal(double x, double y, int oitavas, double persistencia, double lacunaridade) {
        double total = 0.0;
        double frequencia = 1.0;
        double amplitude = 1.0;
        double maximo = 0.0; 

        // otimização mantida pra 2 oitavas
        if(oitavas == 2) {
            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
            amplitude *= persistencia;
            frequencia *= lacunaridade;

            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;

            return total / maximo;
        } else if(oitavas == 3) {
            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
            amplitude *= persistencia;
            frequencia *= lacunaridade;

            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
			
			total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;

            return total / maximo;
        } else if(oitavas == 4) {
            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
            amplitude *= persistencia;
            frequencia *= lacunaridade;

            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;

			total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
			
			total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;

            return total / maximo;
        }
        for(int i = 0; i < oitavas; i++) {
            total += ruido(x * frequencia, y * frequencia) * amplitude;
            maximo += amplitude;
            amplitude *= persistencia;
            frequencia *= lacunaridade;
        }
        return total / maximo;
    }

    // retorna um valor entre -1.0 e 1.0.
    public final double ruido(double x, double y) {
        double s = (x + y) * F2;

        // aubstituição do Math.floor por conversão matematica rapida
        double somaX = x + s;
        double somaY = y + s;
        int i = somaX >= 0 ? (int)somaX : (int)somaX - 1;
        int j = somaY >= 0 ? (int)somaY : (int)somaY - 1;

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

        double n0, n1, n2;

        // vertice 0
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if(t0 < 0) n0 = 0.0;
        else {
            t0 *= t0;
            int g = permutacaoResto12[ii + permutacao[jj]];
            n0 = t0 * t0 * gradiente(g, x0, y0);
        }
        // vertice 1
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if(t1 < 0) n1 = 0.0;
        else {
            t1 *= t1;
            int g = permutacaoResto12[ii + i1 + permutacao[jj + j1]];
            n1 = t1 * t1 * gradiente(g, x1, y1);
        }
        // vertice 2
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if(t2 < 0) n2 = 0.0;
        else {
            t2 *= t2;
            int g = permutacaoResto12[ii + 1 + permutacao[jj + 1]];
            n2 = t2 * t2 * gradiente(g, x2, y2);
        }
        return 70.0 * (n0 + n1 + n2);
    }

    // função exata de vetores de direção sem consultar memória
    public final static double gradiente(int indice, double x, double y) {
        switch(indice) {
            case 0: return x + y;
            case 1: return -x + y;
            case 2: return x - y;
            case 3: return -x - y;
            case 4: return x;
            case 5: return -x;
            case 6: return x;
            case 7: return -x;
            case 8: return y;
            case 9: return -y;
            case 10: return y;
            case 11: return -y;
            default: return 0.0;
        }
    }
}
