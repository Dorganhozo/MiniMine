package com.minimine;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.Gdx;

public class Iniciador {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "MiniMine";
        config.width = 1280;
        config.height = 720;
        config.vSyncEnabled = false; // Desativa VSync
        config.foregroundFPS = 0; // 0 = Ilimitado
        config.backgroundFPS = 0;

        Debugador debug = new Debugador() {
            @Override public boolean ehArm64() { return false; }
            @Override public long obterHeapLivre() { return Runtime.getRuntime().freeMemory(); }
            @Override public long obterHeapTotal() { return Runtime.getRuntime().totalMemory(); }
        };

        // Removido o '3' daqui para compatibilidade
        Gdx.files = new com.badlogic.gdx.backends.lwjgl.LwjglFiles();

        try {
            new LwjglApplication(new Inicio(Gdx.files.getExternalStoragePath(), debug), config);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
