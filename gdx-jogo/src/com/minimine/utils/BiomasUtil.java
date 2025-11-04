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
				nome[0] = "floresta";
				status[0] = 0.5f;
				status[1] = 0.7f;
			}
			@Override
			public void gerarColuna(int lx, int lz, Chunk chunk) {
				int px = chunk.x * Mundo.TAM_CHUNK + lx;
				int pz = chunk.z * Mundo.TAM_CHUNK + lz;
				byte bloco = 0;
				// ruido continental: define onde ha oceanos e terra
				float continente = Mundo.s2D.ruidoFractal(px * 0.0003f, pz * 0.0003f, 1.0f, 5, 0.5f);
				// ruido de detalhe local
				float detalhe = Mundo.s2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, 3, 0.5f);
				// ruido de escala intermediaria para morros
				float relevo = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.003f, 1.0f, 4, 0.55f);
				// continente controla a intensidade do relevo: regiões "oceanicas" ficam planas
				float base = continente;
				if(base < -0.25f) base = -0.25f; // evita profundidades exageradas
				float intensi = (base + 0.3f) * 1.6f;
				if(intensi < 0f) intensi = 0f;
				// elevação final misturando tudo
				float alturaNormalizada = (relevo * intensi) + (detalhe * 0.2f);
				// curva exponencial para picos altos e vales suaves
				if(alturaNormalizada > 0) alturaNormalizada = (float)Math.pow(alturaNormalizada, 1.8f);
				else alturaNormalizada = -((float)Math.pow(-alturaNormalizada, 0.8f));

				int altura = (int)(50 + alturaNormalizada * 180f);
				if(altura < 30) altura = 30 - altura;
				if(altura > Mundo.Y_CHUNK - 2) altura = Mundo.Y_CHUNK - 2;

				for(int y = 0; y < altura; y++) {
					if(y < altura - 8) bloco = 3;       // pedra
					else if(y < altura - 1) bloco = 2;  // terra
					else if(y == altura - 1) bloco = 1; // grama
					else bloco = 0;
					if(bloco != 0) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
				}
				if(Mundo.s2D.ruido(px, pz) > 0.9f) EstruturaUtil.gerarArvore(lx, altura, lz, chunk);
			}
		});
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					nome[0] = "sei la";
					status[0] = 0.3f;
					status[1] = 0.4f;
				}
				@Override
				public void gerarColuna(int lx, int lz, Chunk chunk) {
					int px = chunk.x * Mundo.TAM_CHUNK + lx;
					int pz = chunk.z * Mundo.TAM_CHUNK + lz;
					byte bloco = 0;
					// ruido continental: define onde ha oceanos e terra
					float continente = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.0003f, 1.0f, 4, 0.5f);
					// ruido de detalhe local
					float detalhe = Mundo.s2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, 2, 0.5f);
					// ruido de escala intermediaria para morros
					float relevo = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.03f, 1.0f, 3, 0.55f);
					// continente controla a intensidade do relevo: regiões "oceanicas" ficam planas
					float base = continente;
					if(base < -0.25f) base = -0.25f; // evita profundidades exageradas
					float intensi = (base + 0.3f) * 1.6f;
					if(intensi < 0f) intensi = 0f;
					// elevação final misturando tudo
					float alturaNormalizada = (relevo * intensi) + (detalhe * 0.2f);
					// curva exponencial para picos altos e vales suaves
					if(alturaNormalizada > 0) alturaNormalizada = (float)Math.pow(alturaNormalizada, 1.8f);
					else alturaNormalizada = -((float)Math.pow(-alturaNormalizada, 0.8f));

					int altura = (int)(50 + alturaNormalizada * 160f);
					if(altura < 30) altura = 30;
					if(altura > Mundo.Y_CHUNK - 2) altura = Mundo.Y_CHUNK - 2;

					for(int y = 0; y < altura; y++) {
						if(y < altura - 8) bloco = 3;       // pedra
						else if(y < altura - 1) bloco = 2;  // terra
						else if(y == altura - 1) bloco = 1; // grama
						else bloco = 2;
						if(bloco != 0) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
					}
					if(Mundo.s2D.ruido(px, pz) > 0.7f) EstruturaUtil.gerarArvore(lx, altura, lz, chunk);
				}
			});
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					nome[0] = "deserto bugado";
					status[0] = 0.3f;
					status[1] = 0.4f;
				}
				@Override
				public void gerarColuna(int lx, int lz, Chunk chunk) {
					int px = chunk.x * Mundo.TAM_CHUNK + lx;
					int pz = chunk.z * Mundo.TAM_CHUNK + lz;
					byte bloco = 0;
					// ruido continental: define onde ha oceanos e terra
					float continente = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.0003f, 1.0f, 4, 0.5f);
					// ruido de detalhe local
					float detalhe = Mundo.s2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, 2, 0.5f);
					// ruido de escala intermediaria para morros
					float relevo = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.03f, 1.0f, 3, 0.55f);
					// continente controla a intensidade do relevo: regiões "oceanicas" ficam planas
					float base = continente;
					if(base < -0.25f) base = -0.25f; // evita profundidades exageradas
					float intensi = (base + 0.3f) * 1.6f;
					if(intensi < 0f) intensi = 0f;
					// elevação final misturando tudo
					float alturaNormalizada = (relevo * intensi) + (detalhe * 0.2f);
					// curva exponencial para picos altos e vales suaves
					if(alturaNormalizada > 0) alturaNormalizada = (float)Math.pow(alturaNormalizada, 1.8f);
					else alturaNormalizada = -((float)Math.pow(-alturaNormalizada, 0.8f));

					int altura = (int)(50 + alturaNormalizada * 160f);
					if(altura < 30) altura = 30;
					if(altura > Mundo.Y_CHUNK - 2) altura = Mundo.Y_CHUNK - 2;
					int limiteY = Math.max(altura, 50);
					for(int y = 0; y < limiteY; y++) {
						if(y < altura - 8) bloco = 3;
						else if(y <= altura) bloco = 5;
						else bloco = 0;

						if(y < 50 && bloco == 0) bloco = 4;

						if(bloco != 0) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
					}
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
