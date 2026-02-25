package com.minimine.utils.ruidos;

public class Simplex3D {
    // constantes matematicas pra inclinação do espaço 3D(Simplex)
    public static final double F3 = 1.0 / 3.0;
    public static final double G3 = 1.0 / 6.0;

    public final int[] p;
    public final int[] pResto12; // vetor rapido para evitar a conta de resto

    public Simplex3D(long semente) {
        // embaralhamento robusto usando um misturador de bits
        int[] perm = new int[256];
        for(int i = 0; i < 256; i++) perm[i] = i;

        for(int i = 255; i > 0; i--) {
            long m = semente + i;
            m ^= m >>> 33;
            m *= 0xff51afd7ed558ccdL;
            m ^= m >>> 33;
            m *= 0xc4ceb9fe1a85ec53L;
            m ^= m >>> 33;

            int j = (int)(Math.abs(m) % (i + 1));

            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }

        this.p = new int[512];
        this.pResto12 = new int[512]; // inicia o vetor de otimização

        for(int i = 0; i < 512; i++) {
            this.p[i] = perm[i & 255];
            // resolve o calculo de resto antecipadamente
            this.pResto12[i] = this.p[i] % 12; 
        }
    }

    public final double ruido(double xin, double yin, double zin) {
        double s = (xin + yin + zin) * F3;

        int i = (xin + s) >= 0 ? (int)(xin + s) : (int)(xin + s) - 1;
        int j = (yin + s) >= 0 ? (int)(yin + s) : (int)(yin + s) - 1;
        int k = (zin + s) >= 0 ? (int)(zin + s) : (int)(zin + s) - 1;

        double t = (i + j + k) * G3;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        double z0 = zin - Z0;

        int i1, j1, k1;
        int i2, j2, k2;

        if(x0 >= y0) {
            if(y0 >= z0) { i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
            else if(x0 >= z0) { i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1; }
            else { i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1; }
        } else {
            if(y0 < z0) { i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1; }
            else if(x0 < z0) { i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1; }
            else { i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
        }
        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3;
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3;
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;

        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        double n0, n1, n2, n3;

        // uso do vetor rapido pResto12 e função direta de gradiente
        double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
        if(t0 < 0) n0 = 0.0;
        else {
            t0 *= t0;
            int gi0 = pResto12[ii + p[jj + p[kk]]];
            n0 = t0 * t0 * gradiente(gi0, x0, y0, z0);
        }
        double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
        if(t1 < 0) n1 = 0.0;
        else {
            t1 *= t1;
            int gi1 = pResto12[ii + i1 + p[jj + j1 + p[kk + k1]]];
            n1 = t1 * t1 * gradiente(gi1, x1, y1, z1);
        }
        double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
        if(t2 < 0) n2 = 0.0;
        else {
            t2 *= t2;
            int gi2 = pResto12[ii + i2 + p[jj + j2 + p[kk + k2]]];
            n2 = t2 * t2 * gradiente(gi2, x2, y2, z2);
        }
        double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
        if(t3 < 0) n3 = 0.0;
        else {
            t3 *= t3;
            int gi3 = pResto12[ii + 1 + p[jj + 1 + p[kk + 1]]];
            n3 = t3 * t3 * gradiente(gi3, x3, y3, z3);
        }
        return 32.0 * (n0 + n1 + n2 + n3);
    }

    // estrutura de escolha direta que substitui a matriz GRAD3
    // evita acesso constante a memória e multiplicações por zero
    public final static double gradiente(int indice, double x, double y, double z) {
        switch(indice) {
            case 0: return x + y;
            case 1: return -x + y;
            case 2: return x - y;
            case 3: return -x - y;
            case 4: return x + z;
            case 5: return -x + z;
            case 6: return x - z;
            case 7: return -x - z;
            case 8: return y + z;
            case 9: return -y + z;
            case 10: return y - z;
            case 11: return -y - z;
            default: return 0.0;
        }
    }

    public final double ruidoFractal(double x, double y, double z, int oitavas, double persistencia, double lacunaridade) {
        double total = 0.0;
        double frequencia = 1.0;
        double amplitude = 1.0;
        double maximo = 0.0;
		
        for(int i = 0; i < oitavas; i++) {
            total += ruido(x * frequencia, y * frequencia, z * frequencia) * amplitude;
            maximo += amplitude;
			
			if(i < oitavas - 1) {
				amplitude *= persistencia;
				frequencia *= lacunaridade;
			}
        }
        return total / maximo;
    }
}
