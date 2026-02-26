package com.minimine.utils;

public final class Mat {
	public static final int abs(int num) {
		return num < 0 ? -num : num;
	}

	public static final int floor(float x) {
        int xi = (int)x;
        return x < xi ? xi - 1 : xi;
    }
}
