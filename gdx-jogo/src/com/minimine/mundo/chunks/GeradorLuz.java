package com.minimine.mundo.chunks;

public interface GeradorLuz {
	void calcularLuz(Chunk chunk);
	void recalcularLuz(Chunk chunk);
	void attLuz(Chunk chunk);
}
