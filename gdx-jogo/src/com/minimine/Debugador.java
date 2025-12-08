package com.minimine;

public interface Debugador {
	long obterHeapLivre();
	long obterHeapTotal();
	boolean ehArm64();
}
