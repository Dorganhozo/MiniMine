package com.minimine.utils.ruidos;
/*
 * ruido de crista pra geração de relevo
 * removidas repetições desnecessarias e calculos redundantes
 */
public class CristaRuido2D extends Simplex2D {
    public CristaRuido2D(long semente) {
        super(semente);
    }
    // ruido de crista fractal
    public final double ridgeFractal(double x, double z, int oitavas, double lacunaridade, double ganho) {
        double amplitude = 0.5;
        double frequencia = 1.0;
        double resultado = 0.0;
        double pesoAcumulado = 0.0;
        double peso = 1.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x * frequencia, z * frequencia);

            // transforma em crista(valor absoluto invertido)
            n = 1.0 - Math.abs(n);
            n *= n; // intensifica os picos

            // aplica o peso da camada anterior(cria o efeito de cordilheira)
            n *= peso;
            resultado += n * amplitude;
            pesoAcumulado += amplitude;

            // prepara a proxima oitava
            peso = n; 
            if(i < oitavas - 1) { // evita calculos inuteis na ultima volta
                amplitude *= ganho;
                frequencia *= lacunaridade;
            }
        }
        return resultado / pesoAcumulado;
    }

    public final double ridgeBilateral(double x, double z, int oitavas, double lacunaridade, double ganho) {
        double amplitude = 0.5;
        double frequencia = 1.0;
        double resultado = 0.0;
        double pesoAcumulado = 0.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x * frequencia, z * frequencia);

            // logica de vale central e cristas laterais
            n = Math.abs(n) * 2.0 - 1.0;
            n = n * n * Math.signum(n);

            resultado += n * amplitude;
            pesoAcumulado += amplitude;

            if(i < oitavas - 1) {
                amplitude *= ganho;
                frequencia *= lacunaridade;
            }
        }
        return resultado / pesoAcumulado;
    }

    // textura suiça
    public final double swiss(double x, double z, int oitavas, double lacunaridade, double ganho, double warp) {
        double amplitude = 1.0;
        double resultado = 0.0;
        double dx = 0.0;
        double dz = 0.0;

        for(int i = 0; i < oitavas; i++) {
            // usa as derivadas da volta anterior para distorcer a atual
            double n = ruido(x + dx * warp, z + dz * warp);

            // aproximação de derivada(gradiente)
            double nx = ruido(x + dx * warp + 0.01, z + dz * warp);
            double nz = ruido(x + dx * warp, z + dz * warp + 0.01);

            dx += (nx - n) * amplitude;
            dz += (nz - n) * amplitude;

            resultado += amplitude * (1.0 - Math.abs(n));

            if(i < oitavas - 1) {
                amplitude *= ganho;
                x *= lacunaridade;
                z *= lacunaridade;
            }
        }
        return resultado;
    }

    // turbulencia geologica
    public final double jordan(double x, double z, int oitavas, double lacunaridade, double ganho0, double ganho) {
        double amplitude = ganho0;
        double resultado = 0.0;
        double dx1 = 0.0, dz1 = 0.0;
        double dx2 = 0.0, dz2 = 0.0;

        for(int i = 0; i < oitavas; i++) {
            double n = ruido(x, z);

            // calculo de inclinação
            double n1x = ruido(x + 0.01, z);
            double n1z = ruido(x, z + 0.01);

            dx1 = n1x - n;
            dz1 = n1z - n;

            // segunda distorção baseada na primeira inclinação
            double n2x = ruido(x + dx1 + 0.01, z + dz1);
            double n2z = ruido(x + dx1, z + dz1 + 0.01);
            double nBase = ruido(x + dx1, z + dz1);

            dx2 = n2x - nBase;
            dz2 = n2z - nBase;

            resultado += amplitude * n * (1.0 + dx2 + dz2);

            if(i < oitavas - 1) {
                amplitude *= ganho;
                x *= lacunaridade;
                z *= lacunaridade;
            }
        }
        return resultado;
    }
}
