package com.minimine.utils;

import com.badlogic.gdx.utils.Pool;
import java.util.Arrays;

public class FloatArrayPool extends Pool<float[]> {
    public final int arrayTam;

    public FloatArrayPool(int arrayTam, int tamInicial, int maxTam) {
        super(tamInicial, maxTam);
        this.arrayTam = arrayTam;
    }

    @Override
    protected float[] newObject() {
        return new float[arrayTam];
    }

    @Override
    public void reset(float[] array) {
        Arrays.fill(array, 0);
    }
}
