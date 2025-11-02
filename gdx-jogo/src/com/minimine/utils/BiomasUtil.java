package com.minimine.utils;

import java.util.List;
import java.util.ArrayList;
import com.minimine.cenas.Mundo;
import com.minimine.cenas.Chunk;
import com.minimine.utils.ruidos.PerlinNoise2D;
import com.minimine.utils.ruidos.SimplexNoise2D;

public class BiomasUtil {
	public static List<Bioma> biomas = new ArrayList<>();
	
	public float[] obterClima(int localX, int y, int localZ) {
		float temp = 0f, umidade = 0f;
		
		return new float[]{ temp, umidade };
	}
	
	static {
		biomas.add(new Bioma() {
			@Override
			public void aoIniciar() {
				
			}
			@Override
			public void gerarColuna(int lx, int lz, Chunk chunk) {
				float px = chunk.x * Mundo.TAM_CHUNK + lx;
				float pz = chunk.z * Mundo.TAM_CHUNK + lz;
				byte bloco = 0;
				// ruido continental: define onde ha oceanos e terra
				float continente = SimplexNoise2D.ruidoFractal(px * 0.0003f, pz * 0.0003f, 1.0f, Mundo.seed, 5, 0.5f);
				// ruido de detalhe local
				float detalhe = SimplexNoise2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, Mundo.seed, 3, 0.5f);
				// ruido de escala intermediaria para morros
				float relevo = SimplexNoise2D.ruidoFractal(px * 0.003f, pz * 0.003f, 1.0f, Mundo.seed, 4, 0.55f);
				// continente controla a intensidade do relevo: regiões "oceanicas" ficam planas
				float base = continente;
				if(base < -0.15f) base = -0.15f; // evita profundidades exageradas
				float intensi = (base + 0.3f) * 1.6f;
				if(intensi < 0f) intensi = 0f;
				// elevação final misturando tudo
				float alturaNormalizada = (relevo * intensi) + (detalhe * 0.2f);
				// curva exponencial para picos altos e vales suaves
				if(alturaNormalizada > 0) alturaNormalizada = (float)Math.pow(alturaNormalizada, 1.8f);
				else alturaNormalizada = -((float)Math.pow(-alturaNormalizada, 0.8f));

				int altura = (int)(50 + alturaNormalizada * 180f);
				if(altura < 5) altura = 5;
				if(altura > Mundo.Y_CHUNK - 2) altura = Mundo.Y_CHUNK - 2;

				for(int y = 0; y < altura; y++) {
					if(y < altura - 8) bloco = 3;       // pedra
					else if(y < altura - 1) bloco = 2;  // terra
					else if(y == altura - 1) bloco = 1; // grama
					else bloco = 0;
					ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
				}
				if(Math.random() < 0.05) EstruturaUtil.gerarArvore(lx, altura, lz, chunk);
			}
		});
	}
	
	public static interface Bioma {
		public CharSequence[] nome = new CharSequence[1];
		public float[] status = new float[2]; // 0 = temperatura, 1 = umidade
		public void aoIniciar();
		public void gerarColuna(int localX, int localZ, Chunk chunks);
	}
}
