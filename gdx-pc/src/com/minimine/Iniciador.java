package com.minimine;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Gdx;

public class Iniciador {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("MiniMine");
        config.setWindowedMode(1280, 720);

        Debugador debug = new Debugador() {
            @Override public boolean ehArm64() { return false; }
            @Override public long obterHeapLivre() { return Runtime.getRuntime().freeMemory(); }
            @Override public long obterHeapTotal() { return Runtime.getRuntime().totalMemory(); }
        };

        Gdx.files = new com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files();

        try {
            new Lwjgl3Application(new Inicio(Gdx.files.getExternalStoragePath(), debug, null), config);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
