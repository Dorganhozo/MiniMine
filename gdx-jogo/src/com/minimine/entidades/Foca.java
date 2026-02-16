package com.minimine.entidades;

import com.badlogic.gdx.math.MathUtils;
import com.minimine.mundo.Mundo;
import com.minimine.utils.Mat;
import com.minimine.mundo.blocos.Bloco;

public class Foca extends Entidade {
    public float cronometroDecisao = 0;
    public float direcaoX, direcaoZ;
    public float necessidadeAgua = 0;

    public Foca(float x, float y, float z) {
        super();
        this.posicao.set(x, y, z);
        this.largura = 0.8f; 
        this.altura = 0.6f;
        this.profundidade = 1.2f;
    }

    @Override
    public void att(float delta) {
        cronometroDecisao -= delta;

        // === logica de estados ===
        if(cronometroDecisao <= 0) {
            // se estiver muito tempo no seco, a foca "decide" procurar água
            if(!naAgua) necessidadeAgua += 5.0f; 
            else necessidadeAgua = 0;

            if(necessidadeAgua > 20) {
                procurarAgua();
                cronometroDecisao = 60.0f; // foca no objetivo por 1 minuto
            } else {
                // vaga a esmo ou fica parada
                direcaoX = MathUtils.random(-1f, 1f);
                direcaoZ = MathUtils.random(-1f, 1f);
                cronometroDecisao = MathUtils.random(2f, 5f);
            }
        }
        // === fisica de gravidade ===
        if(naAgua) {
            // na água ela flutua suavemente ou submerge
            velocidade.y = MathUtils.lerp(velocidade.y, 0.2f, 0.1f); 
        }
        // === movimentação e colisão ==
        float veloFinal = naAgua ? velo * 1.5f : velo * 0.3f; // rapida na água, lerda na terra

        // tenta mover no X e Z
        moverColisao(direcaoX * veloFinal * delta, 0, direcaoZ * veloFinal * delta);

        // aplica o movimento vertical(gravidade/pulo)
        moverVertical(velocidade.y * delta);
		
		if(posicao.y < 0) posicao.y = 200;
    }

    private void moverVertical(float dy) {
        posicao.y += dy;
        attHitbox();
        if(colideMundo()) {
            posicao.y -= dy;
            if(velocidade.y < 0) noChao = true; // bateu no chão
            velocidade.y = 0;
        } else {
            noChao = false;
        }
    }

    private void moverColisao(float dx, float dy, float dz) {
        posicao.x += dx;
        attHitbox();
        if(colideMundo()) {
            posicao.x -= dx;
            cronometroDecisao = 0; // se bateu na parede, muda de ideia
        }
        posicao.z += dz;
        attHitbox();
        if(colideMundo()) {
            posicao.z -= dz;
            cronometroDecisao = 0;
        }
    }

    private void procurarAgua() {
        // escaneia ao redor para achar bloco de água
        for(int x = -10; x <= 10; x++) {
            for(int z = -10; z <= 10; z++) {
                Bloco bloco = Bloco.numIds.get(Mundo.obterBlocoMundo((int)posicao.x + x, (int)posicao.y, (int)posicao.z + z));
                if(bloco != null && bloco.nome.equals("agua")) {
                    direcaoX = x > 0 ? 1 : -1;
                    direcaoZ = z > 0 ? 1 : -1;
                    return;
                }
            }
        }
    }
}
