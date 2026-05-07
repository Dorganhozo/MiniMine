package com.minimine;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class Iniciador {
    static File assetsDir;

    public static void main(String[] arg) throws Exception {
        extrairAssets();

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "MiniMine";
        config.addIcon("minimine.png", com.badlogic.gdx.Files.FileType.Internal);
        config.width = 1280;
        config.height = 720;

        Debugador debug = new Debugador() {
            @Override public boolean ehArm64() { return false; }
            @Override public long obterHeapLivre() { return Runtime.getRuntime().freeMemory(); }
            @Override public long obterHeapTotal() { return Runtime.getRuntime().totalMemory(); }
        };

        Instalador instalador = new Instalador() {
            public void instalar(String caminho) {}
        };

        Gdx.files = new LwjglFiles();
        final Inicio inicio = new Inicio(Gdx.files.getExternalStoragePath(), debug, instalador);

        try {
            new LwjglApplication(new ApplicationListener() {
                boolean inicializado = false;

                public void create() {
                    inicio.create();
                }

                public void render() {
                    if (!inicializado) {
                        inicializado = true;
                        final LwjglFiles base = new LwjglFiles();
                        Gdx.files = new com.badlogic.gdx.Files() {
                            public FileHandle getFileHandle(String path, com.badlogic.gdx.Files.FileType type) { return internal(path); }
                            public FileHandle internal(String path) {
                                return new LwjglFileHandle(new File(assetsDir, path), com.badlogic.gdx.Files.FileType.Absolute);
                            }
                            public FileHandle classpath(String path) { return base.classpath(path); }
                            public FileHandle external(String path) { return base.external(path); }
                            public FileHandle absolute(String path) { return base.absolute(path); }
                            public FileHandle local(String path) { return base.local(path); }
                            public String getExternalStoragePath() { return base.getExternalStoragePath(); }
                            public boolean isExternalStorageAvailable() { return base.isExternalStorageAvailable(); }
                            public String getLocalStoragePath() { return base.getLocalStoragePath(); }
                            public boolean isLocalStorageAvailable() { return base.isLocalStorageAvailable(); }
                        };
                    }
                    inicio.render();
                }

                public void resize(int w, int h) { inicio.resize(w, h); }
                public void pause() { inicio.pause(); }
                public void resume() { inicio.resume(); }
                public void dispose() { inicio.dispose(); }
            }, config);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void extrairAssets() throws Exception {
        URI uri = Iniciador.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        final Path jarPath = Paths.get(uri);
        final Path destino = Paths.get(System.getProperty("java.io.tmpdir"), "minimine-assets");
        assetsDir = destino.toFile();

        long jarModificado = jarPath.toFile().lastModified();
        Path marcador = destino.getParent().resolve(".minimine-versao");
        if (marcador.toFile().exists()) {
            String versao = new String(java.nio.file.Files.readAllBytes(marcador));
            if (versao.trim().equals(String.valueOf(jarModificado))) return;
        }

        try (FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null)) {
            final Path raiz = fs.getPath("/");
            java.nio.file.Files.walk(raiz).forEach(new Consumer<Path>() {
                @Override
                public void accept(Path origem) {
                    try {
                        String rel = raiz.relativize(origem).toString();
                        if (rel.isEmpty()) return;
                        Path alvo = destino.resolve(rel);
                        if (java.nio.file.Files.isDirectory(origem)) {
                            alvo.toFile().mkdirs();
                        } else {
                            java.nio.file.Files.copy(origem, alvo, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception ignorar) {}
                }
            });
        }

        java.nio.file.Files.write(marcador, String.valueOf(jarModificado).getBytes());
    }
}