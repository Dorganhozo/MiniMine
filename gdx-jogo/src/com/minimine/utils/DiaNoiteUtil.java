package com.minimine.utils;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class DiaNoiteUtil {
    public static float tempo      = 0.0f;
    public static float tempo_velo = 0.00028f;
    public static float luz        = 1.0f;
    public static long  ultimaAtt  = 0;

    public static final Vector3 dirSol = new Vector3();
    public static final Vector3 dirLua = new Vector3();

    public static final Vector3 posicaoSol = new Vector3();
    public static final Vector3 posicaoLua = new Vector3();

    public static final float[] corSol = {1.0f, 0.9f, 0.1f, 1.0f};
    public static final float[] corLua = {0.9f, 0.95f, 1.0f, 1.0f};

    public static float visibiSol = 1.0f;
    public static float visibiLua = 0.0f;

    public static float[] posTmp = new float[3];

    public static void att() {
        tempo += tempo_velo;
        if(tempo > 1.0f) tempo -= 1.0f;

        float ciclo = (float)Math.sin(tempo * Math.PI * 2.0);
        luz = Math.max(0.05f, (ciclo + 1.0f) * 0.475f + 0.05f);

        calcularPosicoesCorposCelestes();

        // sol: aparece suavemente conforme sobe acima do horizonte
        visibiSol = passoBorrado(-0.08f, 0.10f, dirSol.y);

        // lua: so aparece depois que o sol já está bem abaixo do horizonte,
        // e some antes do sol voltar, nunca coexistem visiveis no céu
        // solAbaixo=1 quando o sol está suficientemente submerso
        // luaAcima=1 quando a lua ja subiu acima do horizonte
        float solAbaixo = passoBorrado(-0.08f, -0.28f, dirSol.y);
        float luaAcima  = passoBorrado(-0.08f,  0.10f, dirLua.y);
        visibiLua = solAbaixo * luaAcima;

        corSol[3] = visibiSol;
        corLua[3] = visibiLua;

        ultimaAtt = System.currentTimeMillis();
    }

    public static void calcularPosicoesCorposCelestes() {
        double anguloSolRad = tempo * 2.0 * Math.PI;

        dirSol.set(
            (float)Math.cos(anguloSolRad),
            (float)Math.sin(anguloSolRad),
            0f
        );
        posicaoSol.set(dirSol);

        dirLua.set(-dirSol.x, -dirSol.y, 0f);
        posicaoLua.set(dirLua);
    }

    public static float obterFatorTransicao() {
        return Math.min(1.0f, Math.max(0.0f, (dirSol.y + 1.0f) * 0.5f));
    }

    public static boolean ehNoite() { return dirSol.y < -0.15f; }
    public static boolean ehDia() { return dirSol.y > 0.15f; }

    public static void aplicarShader(ShaderProgram shader) {
        posTmp[0] = dirSol.x; posTmp[1] = dirSol.y; posTmp[2] = dirSol.z;
        shader.setUniform3fv("u_posSol", posTmp, 0, 3);
        posTmp[0] = dirLua.x; posTmp[1] = dirLua.y; posTmp[2] = dirLua.z;
        shader.setUniform3fv("u_posLua", posTmp, 0, 3);
        shader.setUniform4fv("u_corSol", corSol, 0, 4);
        shader.setUniform4fv("u_corLua", corLua, 0, 4);
    }

    public static float calcularvisibiSol(float alturaNorm) {
        return passoBorrado(0.44f, 0.56f, alturaNorm);
    }
    public static float calcularvisibiLua(float alturaNorm) {
        return passoBorrado(0.44f, 0.56f, 1.0f - alturaNorm);
    }

    public static float passoBorrado(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }
}

