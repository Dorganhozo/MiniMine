package com.minimine.mundo.blocos;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// interface base para qualquer bloco que tenha uma UI associada
public interface InterfaceBloco {
    /*
     * abre a interface para o bloco na posição(bx, by, bz) do mundo.
     * deve setar aberta = true, liberar o cursor e configurar modoTexto
     * via UI se necessario
   */
    void abrir(int bx, int by, int bz);

    void fechar();
    void renderizar(SpriteBatch sb, BitmapFont fonte, float delta);
    boolean aberta();
    boolean processarToque(int x, int y, boolean pressionado);
    void liberar();
}
