package com.minimine.utils.ruidos;

import com.minimine.utils.Mat;

public class Simplex3D {
    // constantes matematicas pra inclinação do espaço 3D(Simplex)
    public static final double F3 = 1.0 / 3.0;
    public static final double G3 = 1.0 / 6.0;

    public static final int[][] GRAD3 = {
        {1,1,0}, {-1,1,0}, {1,-1,0}, {-1,-1,0},
        {1,0,1}, {-1,0,1}, {1,0,-1}, {-1,0,-1},
        {0,1,1}, {0,-1,1}, {0,1,-1}, {0,-1,-1}
    };
    public final int[] p;

    public Simplex3D(long semente) {
		// embaralhamento robusto usando um misturador de bits(MurmurHash3)
		int[] perm = new int[256];
		for(int i = 0; i < 256; i++) perm[i] = i;

		for(int i = 255; i > 0; i--) {
			// misturador de bits pra garantir determinismo e alta entropia
			long m = semente + i;
			m ^= m >>> 33;
			m *= 0xff51afd7ed558ccdL;
			m ^= m >>> 33;
			m *= 0xc4ceb9fe1a85ec53L;
			m ^= m >>> 33;

			int j = (int) (Math.abs(m) % (i + 1));

			int tmp = perm[i];
			perm[i] = perm[j];
			perm[j] = tmp;
		}

		this.p = new int[512];
		for(int i = 0; i < 512; i++) {
			this.p[i] = perm[i & 255];
		}
    }

    public double ruido(double xin, double yin, double zin) {
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

        // valor de contribuição 0.6 para evitar quinas
        double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
        if(t0 < 0) n0 = 0.0;
        else {
            t0 *= t0;
            int gi0 = p[ii + p[jj + p[kk]]] % 12;
            n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0, z0);
        }
        double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
        if(t1 < 0) n1 = 0.0;
        else {
            t1 *= t1;
            int gi1 = p[ii + i1 + p[jj + j1 + p[kk + k1]]] % 12;
            n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1, z1);
        }
        double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
        if(t2 < 0) n2 = 0.0;
        else {
            t2 *= t2;
            int gi2 = p[ii + i2 + p[jj + j2 + p[kk + k2]]] % 12;
            n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2, z2);
        }
        double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
        if(t3 < 0) n3 = 0.0;
        else {
            t3 *= t3;
            int gi3 = p[ii + 1 + p[jj + 1 + p[kk + 1]]] % 12;
            n3 = t3 * t3 * dot(GRAD3[gi3], x3, y3, z3);
        }
        return 32.0 * (n0 + n1 + n2 + n3);
    }

    public static double dot(int[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    public double ruidoFractal(double x, double y, double z, int oitavas, double persistencia, double lacunaridade) {
        double total = 0;
        double frequencia = 1;
        double amplitude = 1;
        double maximo = 0;

        for(int i = 0; i < oitavas; i++) {
            total += ruido(x * frequencia, y * frequencia, z * frequencia) * amplitude;
            maximo += amplitude;
            amplitude *= persistencia;
            frequencia *= lacunaridade;
        }
        return total / maximo;
    }
}
