package com.minimine.mundo;

public class Chave {
	public static long calcularChave(int x, int z) {
		return ((long)x << 32) | (z & 0xFFFFFFFFL);
	}

	public static int x(long chave) {
		return (int)(chave >> 32);
	}

	public static int z(long chave) {
		return (int)chave;
	}
}
