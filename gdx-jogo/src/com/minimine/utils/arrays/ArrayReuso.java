package com.minimine.utils.arrays;

import java.util.concurrent.ConcurrentLinkedQueue;
/*
 * buffers nativos reutilizaveis pra reduzir alocações de memoria nativa
 * evita alocar ByteBuffer a cada chunk
 * reduz pressão sobre o alocador nativo
 * mantem buffers quentes no cache da CPU
*/
public class ArrayReuso {
    public static final int MAX_TAM = 32; // maximo de buffers no reuso

    public static final ConcurrentLinkedQueue<FloatArrayUtil> reusoFloat = new ConcurrentLinkedQueue<>();
    public static final ConcurrentLinkedQueue<ShortArrayUtil> reusoShort = new ConcurrentLinkedQueue<>();
    
    // estatisticas(debug)
    public static int totalFloatCriados = 0;
    public static int totalFloatReutilizados = 0;
    public static int totalShortCriados = 0;
    public static int totalShortReutilizados = 0;

    public static FloatArrayUtil obterFloatArray() {
        FloatArrayUtil array = reusoFloat.poll();
        if(array != null) {
            array.tam = 0;
            totalFloatReutilizados++;
            return array;
        }
        totalFloatCriados++;
        return new FloatArrayUtil();
    }

    public static ShortArrayUtil obterShortArray() {
        ShortArrayUtil array = reusoShort.poll();
        if(array != null) {
            array.tam = 0;
            totalShortReutilizados++;
            return array;
        }
        totalShortCriados++;
        return new ShortArrayUtil();
    }

    public static void devolver(FloatArrayUtil array) {
        if(array == null) return;
        if(reusoFloat.size() < MAX_TAM) {
            array.tam = 0;
            // repõe o limit ao maximo para o proximo uso
            array.buf.limit(array.buf.capacity());
            reusoFloat.offer(array);
        }
    }

    public static void devolver(ShortArrayUtil array) {
        if(array == null) return;
        if(reusoShort.size() < MAX_TAM) {
            array.tam = 0;
            array.buf.limit(array.buf.capacity());
            reusoShort.offer(array);
        }
    }

    public static void limparPools() {
        reusoFloat.clear();
        reusoShort.clear();
    }

    public static String estatisticas() {
        float taxaFloat = totalFloatCriados > 0 ?
            (totalFloatReutilizados * 100f) / (totalFloatCriados + totalFloatReutilizados) : 0f;
        float taxaShort = totalShortCriados > 0 ?
            (totalShortReutilizados * 100f) / (totalShortCriados + totalShortReutilizados) : 0f;

        return String.format(
            "ArrayReuso Estatisticas:\n" +
            "  Float: %d criados, %d reutilizados (%.1f%% reuso), %d no reuso\n" +
            "  Short: %d criados, %d reutilizados (%.1f%% reuso), %d no reuso",
            totalFloatCriados, totalFloatReutilizados, taxaFloat, reusoFloat.size(),
            totalShortCriados, totalShortReutilizados, taxaShort, reusoShort.size()
        );
    }
}
