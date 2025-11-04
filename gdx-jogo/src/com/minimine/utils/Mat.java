package com.minimine.utils;

public final class Mat {
	public static int abs(int num) {
		return num < 0 ? -num : num;
	}

	public static float abs(float num) {
		return num < 0 ? -num : num;
	}

	public static int floor(float x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
	// pros ruidos:
	public static float dot(int[] g, float x, float y) {
        return g[0] * x + g[1] * y;
    }

	public static float dot(int[] g, float x, float y, float z) {
        return g[0]*x + g[1]*y + g[2]*z;
    }

    public static float lerp(float t, float a, float b) {  
        return a + t * (b - a);  
    }  
}
