package com.minimine.mundo.geracao;

import com.badlogic.gdx.files.FileHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.minimine.utils.ArquivosUtil;

public final class RegistroBiomas {
    public final Map<String, DadosBioma> biomas = new LinkedHashMap<String, DadosBioma>();
    public DadosBioma padrao;

    public void carregarBiomas(FileHandle pasta) {
        final FileHandle[] arquivos = ArquivosUtil.listarAssets(pasta);
        if(arquivos == null || arquivos.length == 0)
            throw new RuntimeException("nenhum bioma encontrado em: " + pasta.path());
        for(FileHandle a : arquivos) {
            final String chave = a.nameWithoutExtension();
            final DadosBioma bioma = DadosBioma.compilar(chave, a.readString("UTF-8"));
            biomas.put(chave, bioma);
            if(padrao == null) padrao = bioma;
        }
    }

    public DadosBioma selecionar(float calor, float umidade, int altura) {
        DadosBioma melhor = padrao;
        float menorDist = Float.MAX_VALUE;
        for(DadosBioma b : biomas.values()) {
            if(altura < b.altMin || altura > b.altMax) continue;
            final float dc = calor - b.clima.calor;
            final float du = umidade - b.clima.umidade;
            final float dist = (dc * dc + du * du) / b.peso;
            if(dist < menorDist) {
                menorDist = dist;
                melhor = b;
            }
        }
        return melhor;
    }

    public DadosBioma obter(String chave) {
        final DadosBioma b = biomas.get(chave);
        if(b == null) throw new RuntimeException("bioma desconhecido: " + chave);
        return b;
    }

    public boolean existe(String chave) {
        return biomas.containsKey(chave);
    }

    public Collection<DadosBioma> todos() {
        return biomas.values();
    }

    public int total() {
        return biomas.size();
    }
}

