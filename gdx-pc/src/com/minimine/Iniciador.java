package com.minimine;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

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

        try {
            new Lwjgl3Application(new Inicio("/home/shiniga/", debug, null), config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
