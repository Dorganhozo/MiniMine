package com.minimine.mundo.geracao;

import com.minimine.utils.ruidos.Simplex2D;

/*
 * criar formas continentais organicas
 * usa multiplas camadas de distorção para evitar padrões repetitivos
 */
public class DominioDeformacao {
    public final Simplex2D ruido1;
    public final Simplex2D ruido2;
    public final Simplex2D ruido3;

    public DominioDeformacao(long semente) {
        this.ruido1 = new Simplex2D(semente);
        this.ruido2 = new Simplex2D(semente ^ 0x9E3779B97F4A7C15L);
        this.ruido3 = new Simplex2D(semente ^ 0x3C6EF372FE94F82AL);
    }
    /*
     * aplica distorção em multiplas escalas
     * retorna elevação base continental entre -1 e 1
     */
    public double obterElevacaoContinental(double x, double z) {
		// 1 camada de distorção
		double posX = ruido1.ruidoFractal(x * 0.0002, z * 0.0002, 2, 0.5, 2.0) * 400.0;
		double posZ = ruido1.ruidoFractal(x * 0.0002 + 1000, z * 0.0002 + 1000, 2, 0.5, 2.0) * 400.0;

		double xFinal = x + posX;
		double zFinal = z + posZ;

		// 3 oitavas
		return ruido1.ruidoFractal(xFinal * 0.0004, zFinal * 0.0004, 3, 0.5, 2.0);
	}
    // obtem gradiente de elevação pra calculos de erosão
    public double[] obterGradiente(double x, double z, double delta) {
        double h0 = obterElevacaoContinental(x, z);
        double hx = obterElevacaoContinental(x + delta, z);
        double hz = obterElevacaoContinental(x, z + delta);

        return new double[] {
            (hx - h0) / delta,
            (hz - h0) / delta
        };
    }
}

