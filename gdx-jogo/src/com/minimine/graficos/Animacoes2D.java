package com.minimine.graficos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.Map;
import java.util.HashMap;
import com.minimine.cenas.Mundo;
import com.minimine.Inicio;
import java.util.ArrayList;
import java.util.List;

public class Animacoes2D {
    public static InfoAnimacao[] animacoes;
	public static List<InfoAnimacao> conta = new ArrayList<>();
    public static float tempoTotal = 0f;

    public static class InfoAnimacao {
        public int idTextura;
        public int frameAtual = 0;
        public int totalFrames;
        public float tempoPorFrame;
        public int largura;
        public int altura;
        public Pixmap[] frames;
        public float acumulador = 0f;

        public InfoAnimacao(int idTextura, int totalFrames, float fps, int largura, int altura) {
            this.idTextura = idTextura;
            this.totalFrames = totalFrames;
            this.tempoPorFrame = 1f / fps;
            this.largura = largura;
            this.altura = altura;
            this.frames = new Pixmap[totalFrames];
        }

        public void liberar() {
            for(Pixmap px : frames) {
                if(px != null) px.dispose();
            }
        }
    }

	public static void config() {
		int tam = conta.size();
		animacoes = new InfoAnimacao[tam];
		for(int i = 0; i < tam; i++) {
			animacoes[i] = conta.get(i);
		}
		conta.clear();
		conta = null;
	}

    public static Pixmap carregarPixmap(Object fonte) {
        if(fonte instanceof String) {
            String caminho = (String)fonte;
            FileHandle arquivo = Gdx.files.absolute(Inicio.externo+caminho);
            if(!arquivo.exists()) {
                throw new RuntimeException("Arquivo não existe: " + caminho);
            }
            return new Pixmap(arquivo);
        } else if(fonte instanceof Texture) {
            Texture tex = (Texture)fonte;
            if(!tex.getTextureData().isPrepared()) {
                tex.getTextureData().prepare();
            }
            Pixmap px = tex.getTextureData().consumePixmap();
            Pixmap copia = new Pixmap(px.getWidth(), px.getHeight(), px.getFormat());
            copia.drawPixmap(px, 0, 0);
            px.dispose();
            return copia;
        } else if(fonte instanceof Pixmap) {
            Pixmap px = (Pixmap)fonte;
            Pixmap copia = new Pixmap(px.getWidth(), px.getHeight(), px.getFormat());
            copia.drawPixmap(px, 0, 0);
            return copia;
        }
        throw new IllegalArgumentException("Fonte inválida");
    }

    public static void add(int idTextura, Object[] frames, float fps) {
		if(conta == null) conta = new ArrayList<>();
        if(frames == null || frames.length == 0) return;

        Pixmap primeiro = carregarPixmap(frames[0]);
        int largura = primeiro.getWidth();
        int altura = primeiro.getHeight();
        primeiro.dispose();

        InfoAnimacao anim = new InfoAnimacao(idTextura, frames.length, fps, largura, altura);

        for(int i = 0; i < frames.length; i++) anim.frames[i] = carregarPixmap(frames[i]);

        conta.add(anim);
    }

    public static void att(float delta) {
        tempoTotal += delta;

        for(int i = 0; i < animacoes.length; i++) {
            animacoes[i].acumulador += delta;

            if(animacoes[i].acumulador >= animacoes[i].tempoPorFrame) {
                animacoes[i].acumulador = 0;
                animacoes[i].frameAtual = (animacoes[i].frameAtual + 1) % animacoes[i].totalFrames;
                attAtlas(animacoes[i]);
            }
        }
    }

    public static void attAtlas(InfoAnimacao anim) {
        if(Mundo.atlasGeral == null) return;

        Pixmap frameAtual = anim.frames[anim.frameAtual];
        float[] uvs = Mundo.atlasUVs.get(anim.idTextura);
        if(uvs == null) return;

        int atlasLargura = Mundo.atlasGeral.getWidth();
        int atlasAltura = Mundo.atlasGeral.getHeight();

        int x = (int)(uvs[0] * atlasLargura);
        int y = (int)(uvs[1] * atlasAltura);

        Mundo.atlasGeral.bind();

        Gdx.gl.glTexSubImage2D(
            GL20.GL_TEXTURE_2D,
            0,
            x, y,
            frameAtual.getWidth(),
            frameAtual.getHeight(),
            frameAtual.getGLFormat(),
            frameAtual.getGLType(),
            frameAtual.getPixels()
        );
    }

    public static void liberar() {
        for(int i = 0; i < animacoes.length; i++) animacoes[i].liberar();
		animacoes = null;
    }
}
