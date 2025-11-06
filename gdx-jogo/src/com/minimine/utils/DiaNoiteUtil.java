package com.minimine.utils;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DiaNoiteUtil {
	public static float tempo = 0.0f;
	public static float luz = 1.0f;
	public static long ultimaAtt = 0;

	public static void att() {
		// att a cqda 2 segundos
		if(System.currentTimeMillis() - ultimaAtt < 2000) return;
		ultimaAtt = System.currentTimeMillis();

		tempo += 0.0167f; // 1 dia = 1 minuto
		if(tempo > 1.0f) tempo = 0.0f;

		float ciclo = (float)Math.sin(tempo * Math.PI * 2);
		luz = (ciclo + 1.0f) * 0.4f + 0.2f;
	}

	public static void aplicarShader(ShaderProgram shader) {
		shader.setUniformf("u_luzGlobal", luz);
		shader.setUniformf("u_tempoDia", tempo);
	}
}
