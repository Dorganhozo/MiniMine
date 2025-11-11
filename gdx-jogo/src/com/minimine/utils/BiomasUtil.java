package com.minimine.utils;

import java.util.List;
import java.util.ArrayList;
import com.minimine.cenas.Mundo;
import com.minimine.cenas.Chunk;
import com.minimine.utils.ruidos.PerlinNoise2D;
import com.minimine.utils.ruidos.SimplexNoise2D;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class BiomasUtil {
	public static List<Bioma> biomas = new ArrayList<>();
	
	static {
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					nome[0] = "padrao";
					status[0] = 0.3f;
					status[1] = 0.4f;
					raridade[0] = 0.3f;
				}
				@Override
				public void gerarColuna(int lx, int lz, Chunk chunk) {
					int px = chunk.x * Mundo.TAM_CHUNK + lx;
					int pz = chunk.z * Mundo.TAM_CHUNK + lz;
					byte bloco = 0;
					// ruido continental: define onde ha oceanos e terra
					float continente = Mundo.s2D.ruidoFractal(px * 0.0005f, pz * 0.0005f, 1.0f, 3, 0.5f);
					// ruido de detalhe local
					float detalhe = Mundo.s2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, 2, 0.5f);
					// ruido de escala intermediaria para morros
					float relevo = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.03f, 0.5f, 3, 0.55f);
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
					if(Mundo.s2D.ruido(px, pz) > 0.7f) BiomasUtil.gerarArvore(lx,altura,  lz, chunk);
				}
			});
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					nome[0] = "planicie";
					status[0] = 0.3f;
					status[1] = 0.4f;
					raridade[0] = 0.4f;
				}
				@Override
				public void gerarColuna(int lx, int lz, Chunk chunk) {
					int px = chunk.x * Mundo.TAM_CHUNK + lx;
					int pz = chunk.z * Mundo.TAM_CHUNK + lz;
					byte bloco = 0;
					// ruido continental: define onde ha oceanos e terra
					float continente = Mundo.s2D.ruidoFractal(px * 0.0005f, pz * 0.0005f, 1.0f, 3, 0.5f);
					// ruido de detalhe local
					float detalhe = Mundo.s2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, 2, 0.5f);
					// ruido de escala intermediaria para morros
					float relevo = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.03f, 0.5f, 3, 0.55f);
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

					int altura = (int)(60 + alturaNormalizada * 70f);
					if(altura < 30) altura = 30;
					if(altura > Mundo.Y_CHUNK - 2) altura = Mundo.Y_CHUNK - 2;

					for(int y = 0; y < altura; y++) {
						if(y < altura - 8) bloco = 3;       // pedra
						else if(y < altura - 1) bloco = 2;  // terra
						else if(y == altura - 1) bloco = 1; // grama
						else bloco = 2;
						if(bloco != 0) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
					}
				}
			});
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					nome[0] = "deserto";
					status[0] = 0.3f;
					status[1] = 0.4f;
					raridade[0] = 0.2f;
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
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					nome[0] = "deserto_cacto";
					status[0] = 0.3f;
					status[1] = 0.5f;
					raridade[0] = 0.27f;
				}
				@Override
				public void gerarColuna(int lx, int lz, Chunk chunk) {
					int px = chunk.x * Mundo.TAM_CHUNK + lx;
					int pz = chunk.z * Mundo.TAM_CHUNK + lz;
					int bloco = 0;
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
					float alturaNorm = (relevo * intensi) + (detalhe * 0.2f);
					// curva exponencial para picos altos e vales suaves
					if(alturaNorm > 0) alturaNorm = (float)Math.pow(alturaNorm, 1.8f);
					else alturaNorm = -((float)Math.pow(-alturaNorm, 0.8f));

					int altura = (int)(50 + alturaNorm * 160f);
					if(altura < 30) altura = 30;
					if(altura > Mundo.Y_CHUNK - 2) altura = Mundo.Y_CHUNK - 2;
					int limiteY = Math.max(altura, 50);
					for(int y = 0; y < limiteY; y++) {
						if(y < altura - 8) bloco = 3;
						else if(y <= altura) bloco = 5;
						else bloco = 0;

						if(y < 45 && bloco == 0) bloco = 4;

						if(bloco != 0) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
						if(Mundo.s2D.ruido(px, pz) > 0.5f) BiomasUtil.gerarCacto(lx,altura,  lz, chunk);
					}
				}
			});
	}
	
	public static void addBioma(final LuaFunction inicio, final LuaFunction gerarColuna) {
		biomas.add(new Bioma() {
				@Override
				public void aoIniciar() {
					inicio.call();
				}
				@Override
				public void gerarColuna(int localX, int localZ, Chunk chunk) {
					gerarColuna.call(LuaValue.valueOf(localX), LuaValue.valueOf(localZ), CoerceJavaToLua.coerce(chunk));
				}
			});
	}
	
	public static void defBioma(final LuaFunction inicio, final LuaFunction gerarColuna, int indice) {
		biomas.set(indice, new Bioma() {
				@Override
				public void aoIniciar() {
					inicio.call(CoerceJavaToLua.coerce(nome), CoerceJavaToLua.coerce(status), CoerceJavaToLua.coerce(raridade));
				}
				@Override
				public void gerarColuna(int localX, int localZ, Chunk chunk) {
					gerarColuna.call(LuaValue.valueOf(localX), LuaValue.valueOf(localZ), CoerceJavaToLua.coerce(chunk));
				}
			});
	}
	
	public static void gerarArvore(int x, int y, int z, Chunk chunk) {
		// tronco
		for(int i = 0; i < 5; i++) {
			if(dentroLimite(x, y + i, z)) {
				ChunkUtil.defBloco(x, y + i, z, 6, chunk);
			}
		}
		// copa(duas camadas e topo)
		for(int dy = 4; dy <= 6; dy++) {
			int raio = dy == 4 ? 2 : (dy == 5 ? 1 : 0);
			for(int dx = -raio; dx <= raio; dx++) {
				for(int dz = -raio; dz <= raio; dz++) {
					if(Mat.abs(dx) + Mat.abs(dz) <= raio + 1) {
						int xx = x + dx;
						int yy = y + dy;
						int zz = z + dz;
						if(dentroLimite(xx, yy, zz)) {
							ChunkUtil.defBloco(xx, yy, zz, 7, chunk);
						}
					}
				}
			}
		}
	}
	
	public static void gerarCacto(int lx, int y, int lz, Chunk chunk) {
		ChunkUtil.defBloco(lx, y, lz, 9, chunk);
		ChunkUtil.defBloco(lx, y+1, lz, 9, chunk);
	}

	public static boolean dentroLimite(int x, int y, int z) {
		return x >= 0 && x < Mundo.TAM_CHUNK &&
			z >= 0 && z < Mundo.TAM_CHUNK &&
			y >= 0 && y < Mundo.Y_CHUNK;
	}
	
	public static class Bioma {
		public float[] raridade = new float[1];
		public CharSequence[] nome = new CharSequence[1];
		public float[] status = new float[2];
		public void aoIniciar() {}
		public void gerarColuna(int localX, int localZ, Chunk chunk) {}
	}
}
