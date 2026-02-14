package com.minimine.mundo.geracao;

import com.minimine.utils.ruidos.Simplex2D;
import com.minimine.utils.ruidos.Simplex3D;
import com.minimine.utils.ruidos.RidgeRuido2D;
import com.minimine.utils.ruidos.CelularRuido2D;

public class GeradorTerreno {
    public final DominioDeformacao dominio;
    public final RidgeRuido2D ridge;
    public final Simplex2D ruido;
    public final Simplex3D ruido3d;
    public final Simplex3D cavernas;
    public final Simplex3D cavernasProfundas;
    public final CelularRuido2D celular; // pra biomas
    public final ErosaoHidraulica erosao;

    public final long semente;
    public final int nivelMar = 62;

    public GeradorTerreno(long semente) {
        this.semente = semente;
        this.dominio = new DominioDeformacao(semente);
        this.ridge = new RidgeRuido2D(semente ^ 0x5DEECE66DL);
        this.ruido = new Simplex2D(semente ^ 0x9E3779B9L);
        this.ruido3d = new Simplex3D(semente ^ 0x61C88647L);

        // novos ruidos para cavernas em diferentes camadas
        this.cavernas = new Simplex3D(semente ^ 0x1A2B3C4DL);
        this.cavernasProfundas = new Simplex3D(semente ^ 0x9F8E7D6CL);

        // ruido celular para divisão natural de biomas
        this.celular = new CelularRuido2D(semente ^ 0x4F3C2B1AL);

        // pré-computa erosão pra uma região
        this.erosao = new ErosaoHidraulica(semente, 512, 8.0);
        this.erosao.simularGotas(3000, dominio);
    }

    public int[] calcularDadosColuna(int x, int z) {
        // 1. base continental com dominio distorção
        double base = dominio.obterElevacaoContinental(x, z);

        // 2. identifica tipo de terreno(oceano, planicie, montanha)
        double tipoTerreno = identificarTipo(base);
        double altura = base;

        // 3. suavizaçãp: reduz a intensidade das montanhas para criar mais areas planas
        double suavizacao = ruido.ruido(x * 0.0002, z * 0.0002) * 0.5 + 0.5;

        // se for terreno elevado, adiciona detalhes de montanhas
        if(tipoTerreno > 0.3) {
            double montanhas = ridge.ridgeFractal(x * 0.0008, z * 0.0008, 2, 2.2, 0.5);
            double cordilheiras = ridge.ridgeBilateral(x * 0.0004, z * 0.0004, 2, 2.0, 0.5);

            double fatorMontanha = (tipoTerreno - 0.3) / 0.7;

            // reduz a contribuição das montanhas em 40%
            altura += montanhas * fatorMontanha * 0.48;
            altura += cordilheiras * fatorMontanha * 0.24;

            // aplica suavização progressiva
            altura = altura * (0.7 + suavizacao * 0.3);

            if(fatorMontanha > 0.5) {
                double rochoso = ridge.swiss(x * 0.002, z * 0.002, 2, 2.0, 0.4, 0.5);
                altura += rochoso * (fatorMontanha - 0.5) * 0.12;
            }
        } else {
            // areas baixas pra ter transições mais suaves
            double transicao = ruido.ruidoFractal(x * 0.001, z * 0.001, 2, 0.5, 2.0);
            altura += transicao * 0.05 * (1.0 - tipoTerreno);
        }
        // turbulencia jordan pra micro variações de solo
        double turbulencia = ridge.jordan(x * 0.001, z * 0.001, 3, 2.1, 1.0, 0.5);
        altura += turbulencia * 0.08;

        // aplica erosão pré-calculada
        double valorErosao = erosao.obterErosaoInterpolada(x, z);
        altura += valorErosao * 0.1;

        // detalhes finais de superficie
        double detalhe1 = ruido.ruidoFractal(x * 0.01, z * 0.01, 3, 0.5, 2.0) * 0.05;
        double detalhe2 = ruido.ruidoFractal(x * 0.03, z * 0.03, 2, 0.5, 2.0) * 0.03;
        altura += detalhe1 + detalhe2;

        // converte escala -1,1 para blocos reais
        int blocos;
        if(altura < 0) {
            blocos = nivelMar + (int)(altura * 25.0);
        } else {
            // reduz a escala vertical em 25% pra terrenos menos ingrimes
            blocos = nivelMar + (int)(altura * 97.0);
        }
        // variações 3D pra criar saliencias e pequenos tuneis superiiciais
        if(blocos > nivelMar + 25) {
            double var3d = ruido3d.ruidoFractal(x * 0.04, blocos * 0.08, z * 0.04, 1, 0.5, 2.0);
            if(var3d > 0.45) {
                blocos += (int)((var3d - 0.45) * 10.0);
            }
        }
        int alturaFinal = Math.max(1, Math.min(240, blocos));
        // passa a base continental ja calculada pra evitar reprocessamento no bioma
        TipoBioma bioma = determinarBioma(x, z, alturaFinal, base);

        return new int[] { alturaFinal, bioma.ordinal() };
    }

    public double identificarTipo(double base) {
        if(base < -0.2) return 0.0; // oceano profundo
        if(base < 0.0) return (base + 0.2) / 0.2 * 0.2; // transição mar
        // aumento o raio de planícies para ter mais áreas planas
        if(base < 0.35) return 0.2 + (base / 0.35) * 0.2; // planicies expandidas
        return 0.4 + (Math.min(base - 0.35, 0.65) / 0.65) * 0.6; // montanhas
    }

    public TipoBioma determinarBioma(int x, int z, int altura, double base) {
        // usa ruido celular para criar regiões de biomas naturais
        double celularVal = celular.ruido(x * 0.0008, z * 0.0008);

        // temperatura baseada na latitude(Z), altura e influencia celular
        double distEquador = Math.abs(z * 0.00015);
        double temp = 1.0 - distEquador * 0.7;
        temp -= Math.max(0, (altura - nivelMar) * 0.004);
        // adiciona variação celular na temperatura
        temp += (celularVal - 0.3) * 0.2;

        // umidade baseada na proximidade com oceano(base continental baixa)
        double umidade = Math.exp(-Math.max(0, base) * 2.0);
        double varClima = ruido.ruidoFractal(x * 0.0003, z * 0.0003, 4, 0.6, 2.0);
        umidade += varClima * 0.3;
        // adiciona influencia celular na umidade pra criar bolsões
        umidade += (1.0 - celularVal) * 0.15;

        // define bioma
        if(altura <= nivelMar) {
            if(altura < nivelMar - 20) return TipoBioma.OCEANO_PROFUNDO;
            if(temp > 0.7) return TipoBioma.OCEANO_QUENTE;
            return TipoBioma.OCEANO;
        }
        // mais blocos de praia para transição suave
        if(altura < nivelMar + 5) return TipoBioma.OCEANO_COSTEIRO;

        // usa celular para criar fronteiras mais definidas entre biomas
        if(umidade < 0.25 - celularVal * 0.1) {
            return altura > nivelMar + 15 ? TipoBioma.COLINAS_DESERTO : TipoBioma.DESERTO;
        }
        if(umidade > 0.55 + celularVal * 0.1) {
            if(altura > nivelMar + 35) return TipoBioma.FLORESTA_MONTANHOSA;
			
            if(base < 0.05) return TipoBioma.FLORESTA_COSTEIRA;
            return TipoBioma.FLORESTA;
        }
        return altura > nivelMar + 20 ? TipoBioma.PLANICIES_MONTANHOSAS : TipoBioma.PLANICIES;
    }

    // verifica se deve haver caverna nesta posição
    public boolean temCaverna(int x, int y, int z) {
        // não gera cavernas muito perto da ultima camada
        if(y < 10) return false; 
        // não gera cavernas muito alto
        if(y > 180) return false; 

        boolean temCavernaAqui = false;
        double valorCaverna = 0.0;

        // cavernas principais(camada media)
        if(y >= 20 && y <= 80) {
            valorCaverna = cavernas.ruidoFractal(x * 0.02, y * 0.03, z * 0.02, 3, 0.5, 2.0);
            if(valorCaverna > 0.6) temCavernaAqui = true;
        }
        // cavernas profundas(mais raras e maiores)
        if(y >= 10 && y <= 50) {
            double valorProf = cavernasProfundas.ruidoFractal(x * 0.015, y * 0.025, z * 0.015, 2, 0.5, 2.0);
            if(valorProf > 0.65) {
                temCavernaAqui = true;
                valorCaverna = Math.max(valorCaverna, valorProf);
            }
        }
        // cavernas superficiais(pequenas) invadem a superficie
        if(y >= 50 && y <= 140) {
            double valorSup = ruido3d.ruidoFractal(x * 0.025, y * 0.035, z * 0.025, 2, 0.5, 2.0);
            // permite que cavernas superficiais cheguem mais perto da superficie
            double limiar = 0.7 - (Math.max(0, y - 100) * 0.002); // fica mais facil perto da superficie
            if(valorSup > limiar) {
                temCavernaAqui = true;
                valorCaverna = Math.max(valorCaverna, valorSup);
            }
        }
        // tuneis horizontais usando ruido verme
        double tunel = cavernas.ruido(x * 0.01, y * 0.5, z * 0.01);
        double espessura = Math.abs(ruido3d.ruido(x * 0.03, y * 0.1, z * 0.03));
        if(tunel > 0.5 && espessura < 0.15) {
            temCavernaAqui = true;
            valorCaverna = Math.max(valorCaverna, tunel);
        }
        // evita cavernas muito rasas(1-2 blocos) com grande extensão
        if(temCavernaAqui) {
            // verifica altura da caverna olhando blocos adjacentes verticalmente
            double acima = cavernas.ruidoFractal(x * 0.02, (y + 1) * 0.03, z * 0.02, 3, 0.5, 2.0);
            double abaixo = cavernas.ruidoFractal(x * 0.02, (y - 1) * 0.03, z * 0.02, 3, 0.5, 2.0);

            // se a caverna é muito rasa(ambos lados bloqueados), so mantém se o valor for muito alto
            if(acima < 0.6 && abaixo < 0.6) {
                // cavernas rasas precisam de valor muito mais alto pra existir
                if(valorCaverna < 0.75) return false;
            }
        }
        return temCavernaAqui;
    }

    // sistema de ravinas(cavernas que invadem a superficie)
    public boolean temRavina(int x, int y, int z, int alturaSuperficie) {
		// so gera ravinas perto da superficie
		if(y < alturaSuperficie - 40 || y > alturaSuperficie + 5) return false;

		// usa dois ruídos combinados pra criar ravinas sinuosas
		double ravina1 = Math.abs(ruido3d.ruido(x * 0.012, y * 0.008, z * 0.012));
		double ravina2 = ruido3d.ruido(x * 0.008, y * 0.015, z * 0.008);

		// ravina é um "corte" estreito e profundo
		if(ravina1 < 0.08 && ravina2 > 0.3) {
			// profundidade da ravina diminui conforme se afasta da superficie
			double distSuperficie = Math.abs(y - alturaSuperficie + 10);
			double fatorProf = Math.max(0, 1.0 - distSuperficie / 35.0);
			// usa ruido para chance determinística
			double chance = Math.abs(ruido.ruido(x * 0.041 + y * 0.003, z * 0.037));
			return chance < fatorProf * 0.8;
		}
		return false;
	}

    // sistema de arcos naturais
    public boolean temArco(int x, int y, int z, int alturaSuperficie) {
        // arcos so aparecem em regiões montanhosas
        if(y < alturaSuperficie - 25 || y > alturaSuperficie + 15) return false;
        if(alturaSuperficie < nivelMar + 20) return false; // precisa ser montanhoso

        // usa ruido celular para identificar "pilares" potenciais
        double pilar = celular.ruido(x * 0.015, z * 0.015);

        // arcos são raros e aparecem onde há dois pilares proximos
        if(pilar > 0.35 && pilar < 0.55) {
            // verifica se ha um "vão" horizontal entre pilares
            double vao = Math.abs(ruido3d.ruido(x * 0.03, y * 0.05, z * 0.03));
            double formato = ruido.ruido(x * 0.02, z * 0.02);

            // arco é um vazio em forma de parabola
            double distTopo = Math.abs(y - (alturaSuperficie - 5));
            double curvatura = 1.0 - (distTopo * distTopo) / 225.0; // parabola

            if(vao < 0.2 && curvatura > 0.3 && formato > 0.4) {
                return true;
            }
        }
        return false;
    }

    // verifica se deve ter cascalho
    public boolean temCascalho(int x, int y, int z, int alturaSuperficie, TipoBioma bioma) {
		// cascalho aparece em montanhas, perto de rios e em cavernas
		// 1. montanhas rochosas
		if(alturaSuperficie > nivelMar + 30) {
			double rochoso = ridge.swiss(x * 0.003, z * 0.003, 2, 2.0, 0.4, 0.5);
			if(rochoso > 0.6 && y > alturaSuperficie - 8) {
				double chance = Math.abs(ruido.ruido(x * 0.017 + y * 0.013, z * 0.019));
				return chance < 0.3;
			}
		}
		// 2. base de cavernas e ravinas(depositos)
		if(temCaverna(x, y, z) && !temCaverna(x, y - 1, z)) {
			double chance = Math.abs(ruido.ruido(x * 0.031 + y * 0.007, z * 0.029));
			return chance < 0.4;
		}
		return false;
	}

    public enum TipoBioma {
        OCEANO, OCEANO_COSTEIRO, OCEANO_QUENTE,
		OCEANO_PROFUNDO,
        PLANICIES, PLANICIES_MONTANHOSAS,
        FLORESTA, FLORESTA_COSTEIRA, FLORESTA_MONTANHOSA,
        DESERTO, COLINAS_DESERTO
	}
}
