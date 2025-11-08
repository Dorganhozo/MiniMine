package com.minimine.utils.arrays;

public class FloatArrayUtil {
	public float[] arr = new float[1024];
	public int tam = 0;

	public FloatArrayUtil() {}

	public FloatArrayUtil(int tam) {
		arr = new float[tam];
	}

	public void add(float f) {
		if(tam == arr.length) {
			float[] n = new float[arr.length*2];
			System.arraycopy(arr,0,n,0,arr.length);
			arr=n;
		}
		arr[tam++]=f;
	}

	public float[] praArray() {
		float[] r = new float[tam];
		System.arraycopy(arr,0,r,0,tam);
		return r;
	}
}
