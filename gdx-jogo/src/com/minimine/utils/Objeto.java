package com.minimine.utils;

public class Objeto {
    public boolean liberado = false;

    public void liberar() {
        if(liberado) return;
        liberado = true;
    }
}