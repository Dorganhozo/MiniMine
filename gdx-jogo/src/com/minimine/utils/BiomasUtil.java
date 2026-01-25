package com.minimine.utils;

import java.util.List;
import java.util.ArrayList;
import com.minimine.cenas.Mundo;
import com.minimine.utils.chunks.Chunk;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import com.minimine.utils.chunks.ChunkUtil;

public class BiomasUtil {
	public static List<Bioma> biomas = new ArrayList<>();
	
	public static void escolher(int lx, int lz, Chunk chunk) {
		float v = (Mundo.s2D.ruidoFractal(
			(chunk.x * Mundo.TAM_CHUNK + lx) * 0.0005f,
			(chunk.z * Mundo.TAM_CHUNK + lz) * 0.0005f,
			1.0f, 4, 0.5f) + 1f) * 0.5f;

		float somaPesos = 0f;
		for(int i = 0; i < BiomasUtil.biomas.size(); i++) {
			somaPesos += 1f - BiomasUtil.biomas.get(i).raridade[0];
		}
		float acumulado = 0f;
		BiomasUtil.Bioma escolhido = null;
		for(int i = 0; i < BiomasUtil.biomas.size(); i++) {
			BiomasUtil.Bioma b = BiomasUtil.biomas.get(i);
			float peso = (1f - b.raridade[0]) / somaPesos;
			acumulado += peso;
			if(v <= acumulado) {
				escolhido = b;
				break;
			}
		}
		if(escolhido == null) escolhido = BiomasUtil.biomas.get(BiomasUtil.biomas.size() - 1);
		if(escolhido != null) escolhido.gerarColuna(lx, lz, chunk);
	}
	
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

					int nivelMar = 60;

					// === 1. CALCULO DA ALTURA DO TERRENO(2D) ===

					// ruido base(continentes grandes e suaves)
					float base = Mundo.s2D.ruidoFractal(px * 0.002f, pz * 0.002f, 1.0f, 3, 0.5f);

					// ruido de rio(caminhos sinuosos)
					// usavalor absoluto pra criar vales em "V"
					float ruidoRio = Mundo.s2D.ruidoFractal(px * 0.004f, pz * 0.004f, 1.0f, 2, 0.5f);
					ruidoRio = Mat.abs(ruidoRio); 

					// ruido de montanha(picos)
					float ruidoMontanha = Mundo.s2D.ruidoFractal(px * 0.01f, pz * 0.01f, 1.0f, 3, 0.5f);

					// calculo final da altura(Y)
					float ySolo = 70; // altura media(acima do mar)

					// adiciona relevo base
					ySolo += base * 20; 

					// aplica montanhas(mais altas, mas raras)
					if(ruidoMontanha > 0.2f) {
						ySolo += (ruidoMontanha - 0.2f) * 60; // sobe ate 60 blocos a mais
					}
					// se o valor do rio for baixo(perto de 0), cavamos fundo ate chegar na agua
					// quanto mais perto de 0, mais fundo é o rio
					float profundidadeRio = 0;
					if(ruidoRio < 0.15f) {
						// inverte quanto menor o ruido, maior a profundidade
						profundidadeRio = (0.15f - ruidoRio) * 150.0f; 
					}
					ySolo -= profundidadeRio; // subtrai altura pra criar o canal

					// limites do mundo
					int altura = (int) ySolo;
					if(altura >= Mundo.Y_CHUNK) altura = Mundo.Y_CHUNK - 1;
					if(altura < 1) altura = 1;

					// === 2. PREENCHIMENTO DOS BLOCOS(3D) ===
					for(int y = 0; y <= Math.max(altura, nivelMar); y++) {
						String bloco = "ar";

						// verifica se estamos no solo solido
						if(y <= altura) {
							bloco = "pedra"; // padrão pedra

							// camada de terra/grama no topo
							if(y == altura) {
								// se estiver abaixo ou no nivel do mar, é areia ou terra
								if(y <= nivelMar + 2) bloco = "areia"; 
								else bloco = "grama";
							} else if(y > altura - 4) {
								if(y <= nivelMar + 2) bloco = "areia"; // areia afunda um pouco
								else bloco = "terra";
							}

							// === CAVERNAS ===
							// se o valor for maior que 0.4, é um buraco(ar)
							// não cava se estiver muito baixo(fundo do mundo) ou na superficie exata(pra não furar o chão toda hora)
							if(y > 5 && y < altura - 2) { 
								// frequencia 0.02 cria cavernas largas(queijo suiço)
								// frequencia 0.05 cria túneis estreitos(minhoca)
								float densidadeCaverna = Mundo.s3D.ruido(px * 0.03f, y * 0.04f, pz * 0.03f);

								// buracos extras menores pra detalhe
								float detalheCaverna = Mundo.s3D.ruido(px * 0.1f, y * 0.1f, pz * 0.1f) * 0.2f;

								if(densidadeCaverna + detalheCaverna > 0.35f) {
									bloco = "ar"; // a caverna come a pedra
								}
							}
						}
						// === 3. AGUA E LAGO ===
						// se o bloco acabou sendo "ar"(porque o rio cavou ou a caverna comeu)
						// e a altura atual é menor que o nivel do mar, enche de agua
						if(bloco.equals("ar") && y <= nivelMar) {
							bloco = "agua";
						}
						// coloca o bloco no mundo
						if(!bloco.equals("ar")) {
							ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
						}
					}
				}
			});
			/*
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
					CharSequence bloco = "ar";
					// ruido continental: define onde ha oceanos e terra
					float continente = Mundo.s2D.ruidoFractal(px * 0.0005f, pz * 0.0005f, 1.0f, 3, 0.5f);
					// ruido de detalhe local
					float detalhe = Mundo.s2D.ruidoFractal(px * 0.015f, pz * 0.015f, 2.0f, 2, 0.5f);
					// ruido de escala intermediaria para morros
					float relevo = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.03f, 0.5f, 2, 0.55f);
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
						if(y < altura - 8) bloco = "pedra";       // pedra
						else if(y < altura - 1) bloco = "terra";  // terra
						else if(y == altura - 1) bloco = "grama"; // grama
						else bloco = "terra";
						if(!bloco.equals("ar")) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
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
					CharSequence bloco = "ar";
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
						if(y < altura - 8) bloco = "pedra";
						else if(y <= altura) bloco = "areia";
						else bloco = "ar";

						if(y < 50 && bloco.equals("ar")) bloco = "agua";

						if(!bloco.equals("ar")) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
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
					CharSequence bloco = "ar";
					// ruido continental: define onde ha oceanos e terra
					float continente = Mundo.s2D.ruidoFractal(px * 0.003f, pz * 0.0003f, 1.0f, 2, 0.5f);
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
						if(y < altura - 8) bloco = "pedra";
						else if(y <= altura) bloco = "areia";
						else bloco = "ar";

						if(y < 45 && bloco.equals("ar")) bloco = "agua";

						if(!bloco.equals("ar")) ChunkUtil.defBloco(lx, y, lz, bloco, chunk);
						if(Mundo.s2D.ruido(px, pz) > 0.5f) BiomasUtil.gerarCacto(lx,altura,  lz, chunk);
					}
				}
			}); */
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
				ChunkUtil.defBloco(x, y + i, z, "tronco", chunk);
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
							ChunkUtil.defBloco(xx, yy, zz, "folha", chunk);
						}
					}
				}
			}
		}
	}

	public static void gerarCacto(int lx, int y, int lz, Chunk chunk) {
		ChunkUtil.defBloco(lx, y, lz, "cacto", chunk);
		ChunkUtil.defBloco(lx, y+1, lz, "cacto", chunk);
	}

	public static boolean dentroLimite(int x, int y, int z) {
		return x >= 0 && x < 16 &&
			z >= 0 && z < 16 &&
			y >= 0 && y < 16;
	}

	public static class Bioma {
		public float[] raridade = new float[1];
		public CharSequence[] nome = new CharSequence[1];
		public float[] status = new float[2];
		public void aoIniciar() {}
		public void gerarColuna(int localX, int localZ, Chunk chunk) {}
	}
}
