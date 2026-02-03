package com.minimine.mundo.geracao;

import com.minimine.utils.ruidos.Simplex2D;
import com.minimine.utils.ruidos.Simplex3D;
import com.minimine.utils.ruidos.RidgeNoise;

public class GeradorTerreno {
    public final DominioDeformacao dominio;
    public final RidgeNoise ridge;
    public final Simplex2D ruido;
    public final Simplex3D ruido3d;
    public final ErosaoHidraulica erosao;

    public final long semente;
    public final int nivelMar = 62;

    public GeradorTerreno(long semente) {
        this.semente = semente;
        this.dominio = new DominioDeformacao(semente);
        this.ridge = new RidgeNoise(semente ^ 0x5DEECE66DL);
        this.ruido = new Simplex2D(semente ^ 0x9E3779B9L);
        this.ruido3d = new Simplex3D(semente ^ 0x61C88647L);

        // pré-computa erosão para uma região
        this.erosao = new ErosaoHidraulica(semente, 512, 8.0);
        this.erosao.simularGotas(3000, dominio);
    }

    // calcula altura do terreno em blocos
    public int calcularAltura(int x, int z) {
        // 1. base continental com dominio distorção
        double base = dominio.obterElevacaoContinental(x, z);

        // 2. identifica tipo de terreno baseado na base
        double tipoTerreno = identificarTipo(base);

        double altura = base;

        // 3. aplica recursos especificos por tipo
        if(tipoTerreno > 0.3) {
            // região montanhosa = usa ridge noise
            double montanhas = ridge.ridgeFractal(x * 0.0008, z * 0.0008, 2, 2.2, 0.5);
            double cordilheiras = ridge.ridgeBilateral(x * 0.0004, z * 0.0004, 2, 2.0, 0.5);

            // mix baseado em quanto é montanhoso
            double fatorMontanha = (tipoTerreno - 0.3) / 0.7;
            altura += montanhas * fatorMontanha * 0.8;
            altura += cordilheiras * fatorMontanha * 0.4;

            // textura rochosa em montanhas altas
            if(fatorMontanha > 0.5) {
                double rochoso = ridge.swiss(x * 0.002, z * 0.002, 2, 2.0, 0.4, 0.5);
                altura += rochoso * (fatorMontanha - 0.5) * 0.2;
            }
        }
        // 4. adiciona turbulencia geologica
        double turbulencia = ridge.jordan(x * 0.001, z * 0.001, 3, 2.1, 1.0, 0.5);
        altura += turbulencia * 0.15;

        // 5. aplica erosão pré-computada
        double valorErosao = erosao.obterErosaoInterpolada(x, z);
        altura += valorErosao * 0.1;

        // 6. detalhes finais em multiplas escalas
        double detalhe1 = ruido.ruidoFractal(x * 0.01, z * 0.01, 3, 0.5, 2.0) * 0.08;
        double detalhe2 = ruido.ruidoFractal(x * 0.03, z * 0.03, 2, 0.5, 2.0) * 0.04;
        altura += detalhe1 + detalhe2;

        // 7. converte pra blocos
        int blocos;
        if(altura < 0) {
            // oceano
            blocos = nivelMar + (int)(altura * 25.0);
        } else {
            // terra
            blocos = nivelMar + (int)(altura * 130.0);
        }
        // 8. adiciona variação 3D pra beirais
        if(blocos > nivelMar + 25) {
            double var3d = ruido3d.ruidoFractal(x * 0.04, blocos * 0.08, z * 0.04, 1, 0.5, 2.0);
            if(var3d > 0.4) {
                blocos += (int)((var3d - 0.4) * 15.0);
            }
        }
        return Math.max(1, Math.min(240, blocos));
    }

    public double identificarTipo(double base) {
        // suaviza a transição usando uma curva
        if(base < -0.2) return 0.0;  // oceano profundo
        if(base < 0.0) return (base + 0.2) / 0.2 * 0.2;  // oceano raso
        if(base < 0.2) return 0.2 + (base / 0.2) * 0.2;  // planicie
        return 0.4 + (Math.min(base - 0.2, 0.8) / 0.8) * 0.6;  // montanhas
    }

    // determina bioma baseado em clima
    public TipoBioma determinarBioma(int x, int z, int altura) {
        // clima baseado em posição
        double distEquador = Math.abs(z * 0.00015);  // 0 = equador, 1 = polo

        // temperatura diminui com latitude e altitude
        double temp = 1.0 - distEquador * 0.7;
        temp -= Math.max(0, (altura - nivelMar) * 0.004);

        // umidade baseada em continentalidade
        double base = dominio.obterElevacaoContinental(x, z);
        double umidade = Math.exp(-Math.max(0, base) * 2.0);

        // adiciona variação climatica regional
        double varClima = ruido.ruidoFractal(x * 0.0003, z * 0.0003, 4, 0.6, 2.0);
        umidade += varClima * 0.3;

        // chuva orografica
        double[] grad = dominio.obterGradiente(x, z, 10.0);
        double inclinacao = Math.sqrt(grad[0] * grad[0] + grad[1] * grad[1]);
        if(altura > nivelMar + 20) {
            umidade += inclinacao * 0.5;
        }
        // define bioma
        if(altura <= nivelMar) {
            if(altura < nivelMar - 20) return TipoBioma.OCEANO_PROFUNDO;
            if(temp > 0.7) return TipoBioma.OCEANO_QUENTE;
            return TipoBioma.OCEANO;
        }
        if(altura < nivelMar + 3) return TipoBioma.OCEANO_COSTEIRO;

        if(umidade < 0.25) {
            return altura > nivelMar + 15 ? TipoBioma.COLINAS_DESERTO : TipoBioma.DESERTO;
        }
        if(umidade > 0.55) {
            if(altura > nivelMar + 35) return TipoBioma.FLORESTA_MONTANHOSA;

            double rio = Math.abs(ruido.ruido(x * 0.008, z * 0.008));
            if(rio < 0.08) return TipoBioma.FLORESTA_COM_RIOS;

            if(base < 0.05) return TipoBioma.FLORESTA_COSTEIRA;
            return TipoBioma.FLORESTA;
        }
        double lago = ruido.ruido(x * 0.015, z * 0.015);
        if(lago < -0.5 && altura < nivelMar + 8) {
            return TipoBioma.PLANICIES_AGUADAS;
        }
        return altura > nivelMar + 20 ? TipoBioma.PLANICIES_MONTANHOSAS : TipoBioma.PLANICIES;
    }

    public enum TipoBioma {
        OCEANO, OCEANO_COSTEIRO, OCEANO_QUENTE, OCEANO_PROFUNDO,
        PLANICIES, PLANICIES_MONTANHOSAS, PLANICIES_AGUADAS,
        FLORESTA, FLORESTA_COSTEIRA, FLORESTA_COM_RIOS, FLORESTA_MONTANHOSA,
        DESERTO, COLINAS_DESERTO
	}
}

