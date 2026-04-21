package com.minimine.entidades;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.minimine.graficos.Modelos;
import com.minimine.mundo.Mundo;

public class ItemMundo extends Entidade {
    public CharSequence nome;
    public int quantidade;
    public float tempoVida = 300f;

    public float tempoFlutuacao = 0f;
    public float anguloRotacao = 0f;

    // raio de coleta pelo jogador
    public static final float RAIO_COLETA = 1.5f;
    public static final float RAIO_ATRACAO = 2.5f;

    public static final float VELO_ROTACAO = 120f;
    public static final float AMPLITUDE_BOB = 0.12f;
    public static final float FREQUENCIA_BOB = 2.0f;
    public static final float IMPULSO_HORIZONTAL = 2.5f;

    public ModelInstance modelo;

    public ItemMundo(CharSequence nome, int quantidade, float x, float y, float z) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.posicao.set(x, y, z);

        this.largura = 0.25f;
        this.altura = 0.25f;
        this.profundidade = 0.25f;

        // item nasce no ar, fisica vai resolver o pouso
        this.noChao = false;

        // impulso leve so horizontal + saltinho pequeno
        float angulo = MathUtils.random(0f, MathUtils.PI2);
        this.velocidade.set(
            MathUtils.cos(angulo) * IMPULSO_HORIZONTAL,
            2.0f,
            MathUtils.sin(angulo) * IMPULSO_HORIZONTAL
        );
        modelo = Modelos.modeloItem(nome);
    }

    @Override
    public void att(float delta) {
        tempoVida -= delta;

        // === movimento horizontal com atrito ===
        velocidade.x *= 0.75f;
        velocidade.z *= 0.75f;

        // === move e colide no Y ===
        final float dy = velocidade.y * delta;
        posicao.y += dy;
        attHitbox();
        if(colideMundo()) {
            posicao.y -= dy;
            attHitbox();
            if(dy < 0) noChao = true;
            velocidade.y = 0f;
        } else {
            noChao = ehChao();
        }
        // === move e colide no X ===
        final float dx = velocidade.x * delta;
        posicao.x += dx;
        attHitbox();
        if(colideMundo()) {
            posicao.x -= dx;
            velocidade.x = 0f;
            attHitbox();
        }
        // === move e colide no Z ===
        final float dz = velocidade.z * delta;
        posicao.z += dz;
        attHitbox();
        if(colideMundo()) {
            posicao.z -= dz;
            velocidade.z = 0f;
            attHitbox();
        }
        // === animação ===
        anguloRotacao = (anguloRotacao + VELO_ROTACAO * delta) % 360f;
        if(noChao) tempoFlutuacao += delta;

        // === luz ===
        final int bx = (int)posicao.x;
        final int by = (int)(posicao.y);
        final int bz = (int)posicao.z;
        final int posX = bx >> 4;
        final int posZ = bz >> 4;
        if(chunkCache == null || posX != chunkCache.x || posZ != chunkCache.z) {
            chunkCache = Mundo.obterChunk(posX, posZ);
        }
        if(chunkCache != null && by >= 0 && by < Mundo.Y_CHUNK) {
            final int lx = bx & 0xF, lz = bz & 0xF;
            final int idc = lx + (lz << 4) + (by << 8);
            final int luzTotal = chunkCache.luz[idc] & 0xFF;
            dadosLuz[0] = luzTotal >> 4;
            dadosLuz[1] = luzTotal & 0x0F;
        }

        if(modelo != null) {
            float bobPos = noChao
                ? MathUtils.sin(tempoFlutuacao * FREQUENCIA_BOB) * AMPLITUDE_BOB
                : 0f;
            modelo.transform.setToTranslation(posicao.x, posicao.y + bobPos, posicao.z);
            modelo.transform.rotate(Vector3.Y, anguloRotacao);
            modelo.transform.scale(1.5f, 1.5f, 1.5f);
            modelo.calculateTransforms();
            modelo.userData = dadosLuz;
        }
    }

    @Override
    public void render(ModelBatch mb) {
        if(modelo != null) mb.render(modelo);
    }
}

