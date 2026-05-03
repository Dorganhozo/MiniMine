package com.minimine.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class TarefasUtil {
    public static ExecutorService executorGeracao;
    public static ExecutorService executorMalha;

    public static void iniciar() {
        executorGeracao = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        executorMalha   = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    public static void addGeração(Runnable tarefa) {
        executorGeracao.execute(tarefa);
    }

    public static void addMalha(Runnable tarefa) {
        executorMalha.execute(tarefa);
    }

    public static void liberar() {
        executorGeracao.shutdown();
        executorMalha.shutdown();
    }
}
