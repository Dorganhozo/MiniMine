package com.minimine.mundo;

public class Chave {
	public static final long calcularChave(final int x, final int z) {
		return ((long)x << 32) | (z & 0xFFFFFFFFL);
	}

	public static final int x(final long chave) {
		return (int)(chave >> 32);
	}

	public static final int z(final long chave) {
		return (int)chave;
	}
}
