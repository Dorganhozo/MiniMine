package com.minimine.cenas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.minimine.cenas.blocos.Luz;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Texture;

public class LuzUtil {
	public static int TAM_CHUNK = 16;
	public static Pixmap luzPx;
	public static Texture luzTextura;
	public static List<Luz> luzes = new ArrayList<>();
	
	public static void addLuz(Luz luz, byte[][][] chunk) {
		luzPx.setColor(0.0f, 0.0f, 0.0f, 1.0f);
		luzPx.fill();
		
		LuzUtil.propagarLuz(luz.x + 0.5f, luz.z + 0.5f, luz.cor, luz.raio);
		
		luzTextura.draw(luzPx, 0, 0);
		
		luzes.add(luz);
	}
	
	public static void att(byte[][][] chunk) {
		luzPx.setColor(0.0f, 0.0f, 0.0f, 1.0f);
		luzPx.fill();
		for(Luz luz : luzes) {
			if(luz.x < 0 || luz.z < 0 || 
				luz.x >= TAM_CHUNK || luz.z >= TAM_CHUNK) {
				continue;
			}
			LuzUtil.propagarLuz(luz.x + 0.5f, luz.z + 0.5f, luz.cor, luz.raio);
		}
		luzTextura.draw(luzPx, 0, 0);
	}
	
	public static void propagarLuz(float X, float Z, Color cor, float raio) {
		for(int x = 0; x < TAM_CHUNK; x++) {
			for(int z = 0; z < TAM_CHUNK; z++) {
				float pixelX = x + 0.5f; 
				float pixelZ = z + 0.5f;

				float dist2 = (X - pixelX) * (X - pixelX) + (Z - pixelZ) * (Z - pixelZ);
				float dist = (float) Math.sqrt(dist2);

				if(dist < raio) {
					float nivel = 1.0f - (dist / raio); 

					float r = cor.r * nivel;
					float g = cor.g * nivel;
					float b = cor.b * nivel;

					Color existe = new Color(luzPx.getPixel(x, z));

					float finalR = Math.min(1.0f, existe.r + r);
					float finalG = Math.min(1.0f, existe.g + g);
					float finalB = Math.min(1.0f, existe.b + b);

					luzPx.setColor(finalR, finalG, finalB, 1.0f);
					luzPx.drawPixel(x, z);
				}
			}
		}
	}
	
	public static void liberar() {
		luzPx.dispose();
		luzTextura.dispose();
	}
}
