package com.minimine.utils.ruidos;

import com.minimine.Inicio;
import com.badlogic.gdx.Gdx;

public class SimplexNoise2D {
    public long ptr;
	
	public static final float F2 = (float)(0.5 * (Math.sqrt(3.0) - 1.0));
    public static final float G2 = (float)((3.0 - Math.sqrt(3.0)) / 6.0);
    public static final int[][] GRAD2 = {
        {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    public final short[] perm512;
    public final short[] permMod8;

    public SimplexNoise2D(int semente) {
		if(Inicio.ehArm64) {
			perm512 = null;
			permMod8 = null;

            try {
                System.loadLibrary("simplex-noise2d");
            } catch(Throwable t) {}
			ptr = iniciarC(semente);
		} else {
			int[] p = new int[256];
			for(int i = 0; i < 256; i++) p[i] = i;

			int estado = semente;
			if(estado == 0) estado = 0x9E3779B9;

			for(int i = 255; i > 0; i--) {
				// xorshift32 passo
				int z = estado;
				z ^= (z << 13);
				z ^= (z >>> 17);
				z ^= (z << 5);
				estado = z;
				long urnd = z & 0xFFFFFFFFL;
				int j = (int) (urnd % (i + 1)); // em [0, i]

				int tmp = p[i];
				p[i] = p[j];
				p[j] = tmp;
			}
			this.perm512 = new short[512];
			this.permMod8 = new short[512];

			for(int i = 0; i < 512; i++) {
				short v = (short) p[i & 255];
				this.perm512[i] = v;
				this.permMod8[i] = (short) (v & 7);
			}
		}
	}

	public float ruido(float xin, float yin) {
		if(Inicio.ehArm64) return ruidoC(ptr, xin, yin);
		// 1. usar int em vez de float
		float s = (xin + yin) * 0.366025403f; // F2

		int i = (int)(xin + s);
		int j = (int)(yin + s);

		float t = (i + j) * 0.211324865f; // G2
		float X0 = i - t;
		float Y0 = j - t;
		float x0 = xin - X0;
		float y0 = yin - Y0;

		int i1 = (x0 > y0) ? 1 : 0;
		int j1 = (x0 > y0) ? 0 : 1;

		float x1 = x0 - i1 + 0.211324865f;
		float y1 = y0 - j1 + 0.211324865f;
		float x2 = x0 - 0.577350269f; // 1 - 2*G2
		float y2 = y0 - 0.577350269f;
		// 2. acesso direto a arrays
		short[] p = this.perm512;
		short[] g = this.permMod8;

		int ii = i & 255;
		int jj = j & 255;
		// 3. calcular todos os t's
		float t0 = 0.5f - x0*x0 - y0*y0;
		float t1 = 0.5f - x1*x1 - y1*y1;
		float t2 = 0.5f - x2*x2 - y2*y2;
		// 5. usa if ao inves em Math.max(...);
		float n0 = 0, n1 = 0, n2 = 0;

		if(t0 > 0) {
			t0 *= t0;
			int gi0 = g[ii + p[jj]];
			n0 = t0 * t0 * (GRAD2[gi0][0]*x0 + GRAD2[gi0][1]*y0);
		}
		if(t1 > 0) {
			t1 *= t1;
			int gi1 = g[ii + i1 + p[jj + j1]];
			n1 = t1 * t1 * (GRAD2[gi1][0]*x1 + GRAD2[gi1][1]*y1);
		}
		if(t2 > 0) {
			t2 *= t2;
			int gi2 = g[ii + 1 + p[jj + 1]];
			n2 = t2 * t2 * (GRAD2[gi2][0]*x2 + GRAD2[gi2][1]*y2);
		}
		return 70.0f * (n0 + n1 + n2);
	}
	
    // calcula o ruido fractal(FBM)
    public float ruidoFractal(float x, float z, float escala, int octaves, float persis) {
		if(Inicio.ehArm64) return ruidoFractalC(ptr, x, z, escala, octaves, persis);
        float total = 0;
        float amplitude = 1;
        float maxValor = 0;

        for(int i = 0; i < octaves; i++) {
            total += ruido(x * escala, z * escala) * amplitude;

            maxValor += amplitude;
            amplitude *= persis;
            escala *= 2;
        }
        return total / maxValor;
    }

    public void liberar() {
        if(ptr != 0) {
            if(Inicio.ehArm64) liberarC(ptr);
            ptr = 0;
        }
    }
    @Override
    protected void finalize() throws Throwable {
        liberar();
        super.finalize();
    }
    public static native long iniciarC(int semente);
    public static native float ruidoC(long ptr, float x, float y);
    public static native float ruidoFractalC(long ptr, float x, float z, float escala, int octaves, float persis);
    public static native void liberarC(long ptr);
}
