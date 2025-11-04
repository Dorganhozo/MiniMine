package com.minimine.utils.ruidos;

import com.minimine.utils.Mat;

public final class PerlinNoise3D {
    public static final int[] P = new int[512];
    
    public static final int[] GX = {
        1,-1,1,-1, 1,-1,1,-1, 0, 0, 0, 0, 1,-1,1,-1
    };
    public static final int[] GY = {
        1, 1,-1,-1, 0, 0,0, 0, 1,-1, 1,-1, 1, 1,-1,-1
    };
    public static final int[] GZ = {
        0, 0, 0, 0, 1, 1,-1,-1, 1, 1,-1,-1, 0, 0, 0, 0
    };

    static {
		final int[] perm = {
			151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
			190,6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,88,237,149,56,87,174,20,
			125,136,171,168,68,175,74,165,71,134,139,48,27,166,77,146,158,231,83,111,229,122,60,211,133,230,220,
			105,92,41,55,46,245,40,244,102,143,54,65,25,63,161,1,216,80,73,209,76,132,187,208,89,18,169,200,196,
			135,130,116,188,159,86,164,100,109,198,173,186,3,64,52,217,226,250,124,123,5,202,38,147,118,126,255,82,
			85,212,207,206,59,227,47,16,58,17,182,189,28,42,223,183,170,213,119,248,152,2,44,154,163,70,221,153,
			101,155,167,43,172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,218,246,97,228,251,34,
			242,193,238,210,144,12,191,179,162,241,81,51,145,235,249,14,239,107,49,192,214,31,181,199,106,157,184,
			84,204,176,115,121,50,45,127,4,150,254,138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,
			61,156,180
		};
        for(int i = 0; i < 256; i++) {
            P[i] = perm[i];
            P[i + 256] = perm[i];
        }
    }

    public static float ruido(float x, float y, float z, int seed) {
        final int[] p = P; // local ref
        int X = (Mat.floor(x) + seed) & 255;
        int Y = (Mat.floor(y) + seed) & 255;
        int Z = (Mat.floor(z) + seed) & 255;

        float xf = x - Mat.floor(x);
        float yf = y - Mat.floor(y);
        float zf = z - Mat.floor(z);

        float u = fade(xf);
        float v = fade(yf);
        float w = fade(zf);

        int A  = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B  = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;
        // g000
        int h = p[AA] & 15;
        float g000 = GX[h] * xf + GY[h] * yf + GZ[h] * zf;
        // g001
        h = p[AA + 1] & 15;
        float g001 = GX[h] * xf + GY[h] * yf + GZ[h] * (zf - 1f);
        // g010
        h = p[AB] & 15;
        float g010 = GX[h] * xf + GY[h] * (yf - 1f) + GZ[h] * zf;
        // g011
        h = p[AB + 1] & 15;
        float g011 = GX[h] * xf + GY[h] * (yf - 1f) + GZ[h] * (zf - 1f);
        // g100
        h = p[BA] & 15;
        float g100 = GX[h] * (xf - 1f) + GY[h] * yf + GZ[h] * zf;
        // g101
        h = p[BA + 1] & 15;
        float g101 = GX[h] * (xf - 1f) + GY[h] * yf + GZ[h] * (zf - 1f);
        // g110
        h = p[BB] & 15;
        float g110 = GX[h] * (xf - 1f) + GY[h] * (yf - 1f) + GZ[h] * zf;
        // g111
        h = p[BB + 1] & 15;
        float g111 = GX[h] * (xf - 1f) + GY[h] * (yf - 1f) + GZ[h] * (zf - 1f);

        float lerpX00 = g000 + u * (g100 - g000);
        float lerpX01 = g001 + u * (g101 - g001);
        float lerpX10 = g010 + u * (g110 - g010);
        float lerpX11 = g011 + u * (g111 - g011);

        float lerpY0 = lerpX00 + v * (lerpX10 - lerpX00);
        float lerpY1 = lerpX01 + v * (lerpX11 - lerpX01);

        return lerpY0 + w * (lerpY1 - lerpY0);
    }

    public static float fade(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    public static float ruidoFractal(float x, float y, float z, int seed, int octaves, float persis) {
        float total = 0f;
        float amplitude = 1f;
        float maxValor = 0f;
        float lx = x;
        float ly = y;
        float lz = z;
        for(int i = 0; i < octaves; i++) {
            total += ruido(lx, ly, lz, seed + i) * amplitude;
            maxValor += amplitude;
            amplitude *= persis;
            lx *= 2f;
            ly *= 2f;
            lz *= 2f;
        }
        return total / maxValor;
    }
}
