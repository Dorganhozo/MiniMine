package com.minimine.mundo;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import com.minimine.entidades.Jogador;
import java.util.Map;
import com.minimine.entidades.Foca;
import com.minimine.entidades.Entidade;
import java.util.Iterator;

public class GerenciadorEntidades {
	public static final Random aleatorio = new Random();
    public static float tempo = 0f;
    public static final float INTERVALO = 5f; // segundos
    public static final int MAX_ENTIDADES = 20;
    public static final float DIST_MIN_NASCER = 10f;
	
	public static void att(float delta, Mundo mundo, Jogador jg) {
		// remove entidades se saiu da area visivel
		Iterator<Entidade> it = mundo.entidades.iterator();
		while(it.hasNext()) {
			Entidade e = it.next();
			long chaveE = Chave.calcularChave((int)e.posicao.x >> 4, (int)e.posicao.z >> 4);
			if(!mundo.chunks.containsKey(chaveE)) {
				e.liberar();
				it.remove();
			}
		}
		if(mundo.carregado) {
			tempo += delta;
			if(tempo >= INTERVALO && mundo.entidades.size() < MAX_ENTIDADES) {
				tempo = 0f;
				tentarNascerEntidade(jg, mundo);
			}
		}
		for(Entidade e : mundo.entidades) {
			e.att(delta);

			if(!e.noChao || e.naAgua) {
				int fluidez = 0;
				if(e.naAgua) fluidez = 10;

				e.velocidade.y += (mundo.GRAVIDADE + fluidez) * delta;

				if(e.velocidade.y < e.VELO_MAX_QUEDA) e.velocidade.y = e.VELO_MAX_QUEDA;
			}
		}
	}
	
	public static void tentarNascerEntidade(Jogador jogador, Mundo mundo) {
		if(!jogador.bioma.equals("Mar Quente")) return;
		// pega um chunk carregado aleatório(estado 2 = malha pronta)
		List<Long> disponiveis = new ArrayList<>();
		for(Map.Entry<Long, Integer> e : mundo.estados.entrySet()) {
			if(e.getValue() == 2) disponiveis.add(e.getKey());
		}
		if(disponiveis.isEmpty()) return;

		// embaralha tentando até 5 chunks candidatos
		for(int t = 0; t < 5; t++) {
			long chave = disponiveis.get(aleatorio.nextInt(disponiveis.size()));
			int cx = Chave.x(chave);
			int cz = Chave.z(chave);

			// posição aleatória dentro da chunk
			int wx = cx * mundo.TAM_CHUNK + aleatorio.nextInt(mundo.TAM_CHUNK);
			int wz = cz * mundo.TAM_CHUNK + aleatorio.nextInt(mundo.TAM_CHUNK);

			// distancia minima do jogador
			float dx = wx - jogador.posicao.x;
			float dz = wz - jogador.posicao.z;
			if(dx * dx + dz * dz < DIST_MIN_NASCER * DIST_MIN_NASCER) continue;

			int wy = mundo.obterAlturaChao(wx, wz);
			if(wy <= 1) continue;
			
			Entidade entidade = null;
			
			entidade = new Foca(wx, wy, wz);
			
			if(entidade != null) mundo.entidades.add(entidade);
			return;
		}
	}
}
