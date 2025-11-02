package com.minimine.utils.ruidos;

public class SimplexNoise2D {
    public static final float F2 = (float)(0.5 * (Math.sqrt(3.0) - 1.0));
    public static final float G2 = (float)((3.0 - Math.sqrt(3.0)) / 6.0);

    public static final int[][] GRAD2 = {
        {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
	
    public static float ruido(float xin, float yin, int seed) {
        int[] p = new int[256];
        for(int i = 0; i < 256; i++) p[i] = i;

        int estado = seed;
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
        short[] perm512 = new short[512];
        short[] permMod8 = new short[512];
        for(int i = 0; i < 512; i++) {
            short v = (short) p[i & 255];
            perm512[i] = v;
            permMod8[i] = (short) (v & 7);
        }
        float s = (xin + yin) * F2;
        int i = PerlinNoise3D.floorRapido(xin + s);
        int j = PerlinNoise3D.floorRapido(yin + s);

        float t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;
        float x0 = xin - X0;
        float y0 = yin - Y0;

        int i1, j1;
        if(x0 > y0) { i1 = 1; j1 = 0; } else { i1 = 0; j1 = 1; }

        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1.0f + 2.0f * G2;
        float y2 = y0 - 1.0f + 2.0f * G2;

        int ii = i & 255;
        int jj = j & 255;

        float n0, n1, n2;

        float t0 = 0.5f - x0 * x0 - y0 * y0;
        if(t0 < 0) n0 = 0f;
        else {
            t0 *= t0;
            int gi0 = permMod8[ii + perm512[jj]];
            n0 = t0 * t0 * dot(GRAD2[gi0], x0, y0);
        }
        float t1 = 0.5f - x1 * x1 - y1 * y1;
        if(t1 < 0) n1 = 0f;
        else {
            t1 *= t1;
            int gi1 = permMod8[ii + i1 + perm512[jj + j1]];
            n1 = t1 * t1 * dot(GRAD2[gi1], x1, y1);
        }
        float t2 = 0.5f - x2 * x2 - y2 * y2;
        if(t2 < 0) n2 = 0f;
        else {
            t2 *= t2;
            int gi2 = permMod8[ii + 1 + perm512[jj + 1]];
            n2 = t2 * t2 * dot(GRAD2[gi2], x2, y2);
        }
        return 70.0f * (n0 + n1 + n2);
    }

    public static float dot(int[] g, float x, float y) {
        return g[0] * x + g[1] * y;
    }
	
	public static float ruidoFractal(float x, float z, float escala, int seed, int octaves, float persis) {
		float total = 0;
		float amplitude = 1;
		float maxValor = 0;

		for(int i = 0; i < octaves; i++) {
			total += ruido(x * escala, z * escala, seed + i) * amplitude;
			maxValor += amplitude;
			amplitude *= persis;
			escala *= 2;
		}
		return total / maxValor;
	}
}
