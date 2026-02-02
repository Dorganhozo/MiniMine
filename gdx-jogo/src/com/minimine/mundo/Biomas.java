package com.minimine.mundo;

import com.minimine.utils.ruidos.Simplex3D;
import com.minimine.utils.ruidos.Simplex2D;
import java.util.List;
import java.util.ArrayList;

public class Biomas {
    // Frequências ajustadas para evitar o visual de "lixo" aleatório
    private static final double F_CLIMA = 0.00015;
    private static final double F_RELEVO = 0.00045;
    private static final double F_DETALHE = 0.012;
    private static final double F_CAVERNA = 0.025;

    public static void escolher(Chunk pedaco) {
        Simplex2D s2d = Mundo.s2D;
        Simplex3D s3d = Mundo.s3D;

        int mundoX = pedaco.x << 4;
        int mundoZ = pedaco.z << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int mX = mundoX + x;
                int mZ = mundoZ + z;

                // 1. Amostragem Climática Suavizada
                float temperatura = (float) s2d.ruidoFractal(mX * F_CLIMA, mZ * F_CLIMA, 3, 0.5, 2.0);
                float umidade = (float) s2d.ruidoFractal((mX + 1000) * F_CLIMA, (mZ + 1000) * F_CLIMA, 3, 0.5, 2.0);

                // 2. Cálculo de Mistura (Suaviza a transição entre biomas)
                MisturaBioma mistura = CalculadorBioma.obterMistura(temperatura, umidade);

                // 3. Relevo Baseado na Mistura Ponderada
                double ruidoBase = s2d.ruidoFractal(mX * F_RELEVO, mZ * F_RELEVO, 4, 0.5, 2.0);
                double ruidoDetalhe = s2d.ruidoFractal(mX * F_DETALHE, mZ * F_DETALHE, 2, 0.5, 2.0);

                // A altura agora funde as características dos biomas próximos
                int alturaFinal = (int) (mistura.alturaBase + (ruidoBase * mistura.variacaoAltura) + (ruidoDetalhe * 5));

                // 4. Preenchimento de Blocos Otimizado
                for (int y = 0; y < 256; y++) {
                    String bloco = null;

                    if (y <= alturaFinal) {
                        // Camadas de solo baseadas no bioma dominante
                        if (y == alturaFinal && y >= 62) {
                            bloco = mistura.blocoTopo;
                        } else if (y > alturaFinal - 4) {
                            bloco = mistura.blocoSub;
                        } else {
                            bloco = "pedra";
                        }

                        // Sistema de Cavernas (Túneis circulares e orgânicos)
                        double cav1 = s3d.ruido(mX * F_CAVERNA, y * F_CAVERNA, mZ * F_CAVERNA);
                        double cav2 = s3d.ruido((mX + 100) * F_CAVERNA, y * F_CAVERNA, (mZ + 100) * F_CAVERNA);

                        // Interseção que cria túneis em vez de buracos aleatórios
                        if ((cav1 * cav1 + cav2 * cav2) < 0.015 && y < alturaFinal - 8) {
                            bloco = null; 
                        }
                    } else if (y < 62) {
                        bloco = "agua";
                    }

                    if (bloco != null) {
                        ChunkUtil.defBloco(x, y, z, bloco, pedaco);
                    }
                }
            }
        }
    }
}

class MisturaBioma {
    float alturaBase = 0;
    float variacaoAltura = 0;
    String blocoTopo = "grama";
    String blocoSub = "terra";
}

class DadosBiomas {
		public static class Def {
			public String nome;
			public float tempAlvo, umidAlvo;
			public String topo, sub;
			public float hBase, hVar;

			public Def(String n, float t, float u, String s1, String s2, float hb, float hv) {
				this.nome = n; this.tempAlvo = t; this.umidAlvo = u;
				this.topo = s1; this.sub = s2; this.hBase = hb; this.hVar = hv;
			}
		}

		public static final List<Def> LISTA = new ArrayList<>();

		static {
			LISTA.add(new Def("Deserto", 0.9f, -0.6f, "areia", "areia", 62, 3));
			LISTA.add(new Def("Planicie", 0.1f, 0.0f, "grama", "terra", 68, 7));
			LISTA.add(new Def("Selva", 0.8f, 0.7f, "grama", "terra", 65, 25));
			LISTA.add(new Def("Tundra", -0.7f, -0.3f, "pedra", "pedra", 75, 10));
			LISTA.add(new Def("Montanha Neve", -0.9f, 0.2f, "pedra", "pedra", 90, 60));
		}
	}
	
class CalculadorBioma {
    public static MisturaBioma obterMistura(float temp, float umid) {
        MisturaBioma resultado = new MisturaBioma();
        float pesoTotal = 0;

        DadosBiomas.Def dominante = null;
        float maiorPeso = -1;

        for (DadosBiomas.Def b : DadosBiomas.LISTA) {
            float dTemp = temp - b.tempAlvo;
            float dUmid = umid - b.umidAlvo;
            float distanciaSq = dTemp * dTemp + dUmid * dUmid;

            // Peso inversamente proporcional à distância climática
            float peso = 1.0f / (distanciaSq + 0.05f); 
            peso = (float) Math.pow(peso, 4); // Expande a zona de influência

            resultado.alturaBase += b.hBase * peso;
            resultado.variacaoAltura += b.hVar * peso;
            pesoTotal += peso;

            if (peso > maiorPeso) {
                maiorPeso = peso;
                dominante = b;
            }
        }

        resultado.alturaBase /= pesoTotal;
        resultado.variacaoAltura /= pesoTotal;
        resultado.blocoTopo = dominante.topo;
        resultado.blocoSub = dominante.sub;

        return resultado;
    }
}
