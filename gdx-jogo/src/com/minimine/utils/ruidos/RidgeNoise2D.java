package com.minimine.utils.ruidos;
/**
 * ridge noise pra criar cordilheiras e cadeias montanhosas realistas
 * usa valor absoluto do ruído para criar "cristas"
 */
public class RidgeNoise2D extends Simplex2D {
    public RidgeNoise2D(long semente) {
        super(semente);
    }
    // ridge noise fractal, cria cordilheiras naturais
    public double ridgeFractal(double x, double z, int oitavas, double lacunaridade, double ganho) {
        double amplitude = 0.5;
        double frequencia = 1.0;
        double resultado = 0.0;
        double pesoAcumulado = 0.0;
        double peso = 1.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x * frequencia, z * frequencia);

            // transforma em ridge(crista)
            n = Math.abs(n);
            n = 1.0 - n;
            n = n * n;  // intensifica picos

            // aplica peso da camada anterior
            n *= peso;
            resultado += n * amplitude;
            pesoAcumulado += amplitude;

            // atualiza peso baseado nesta camada
            peso = n;

            amplitude *= ganho;
            frequencia *= lacunaridade;
        }
        return resultado / pesoAcumulado;
    }

    // ridge bilateral, cria vales entre montanhas
    public double ridgeBilateral(double x, double z, int oitavas, double lacunaridade, double ganho) {
        double amplitude = 0.5;
        double frequencia = 1.0;
        double resultado = 0.0;
        double pesoAcumulado = 0.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x * frequencia, z * frequencia);

            // bilateral ridge, vale no meio, cristas nos lados
            n = Math.abs(n) * 2.0 - 1.0;
            n = n * n * Math.signum(n);

            resultado += n * amplitude;
            pesoAcumulado += amplitude;

            amplitude *= ganho;
            frequencia *= lacunaridade;
        }
        return resultado / pesoAcumulado;
    }
    // textura rochosa complexa
    public double swiss(double x, double z, int oitavas, double lacunaridade, double ganho, double warp) {
        double amplitude = 1.0;
        double frequencia = 1.0;
        double resultado = 0.0;
        double dx = 0.0;
        double dz = 0.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x + dx * warp, z + dz * warp);

            // calcula derivadas aproximadas
            double nx = ruido(x + dx * warp + 0.01, z + dz * warp);
            double nz = ruido(x + dx * warp, z + dz * warp + 0.01);

            dx += (nx - n) * amplitude;
            dz += (nz - n) * amplitude;

            resultado += amplitude * (1.0 - Math.abs(n));

            amplitude *= ganho;
            frequencia *= lacunaridade;
            x *= lacunaridade;
            z *= lacunaridade;
        }
        return resultado;
    }
    // turbulencia geologica
    public double jordan(double x, double z, int oitavas, double lacunaridade, double ganho0, double ganho) {
        double amplitude = ganho0;
        double frequencia = 1.0;
        double resultado = 0.0;
        double dx1 = 0.0;
        double dz1 = 0.0;
        double dx2 = 0.0;
        double dz2 = 0.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x, z);
            double n1x = ruido(x + 0.01, z);
            double n1z = ruido(x, z + 0.01);
            double n2x = ruido(x + dx1 + 0.01, z + dz1);
            double n2z = ruido(x + dx1, z + dz1 + 0.01);

            // primeira derivada
            dx1 = (n1x - n);
            dz1 = (n1z - n);

            // segunda derivada
            dx2 = (n2x - ruido(x + dx1, z + dz1));
            dz2 = (n2z - ruido(x + dx1, z + dz1));

            resultado += amplitude * n * (1.0 + dx2 + dz2);

            amplitude *= ganho;
            frequencia *= lacunaridade;
            x *= lacunaridade;
            z *= lacunaridade;
        }
        return resultado;
    }
}
