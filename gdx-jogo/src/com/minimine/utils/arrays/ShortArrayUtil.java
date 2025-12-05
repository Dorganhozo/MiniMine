package com.minimine.utils.arrays;

public class ShortArrayUtil {
	public short[] arr = new short[12288]; // 2046 faces de uma vez
	public int tam = 0;

	public ShortArrayUtil() {}

	public ShortArrayUtil(int tam) {
		arr = new short[tam];
	}

	public void add(short v) {
		if(tam == arr.length) {
			short[] n = new short[arr.length*2];
			System.arraycopy(arr,0,n,0,arr.length);
			arr = n;
		}
		arr[tam++] = v;
	}

	public short[] praArray() {
		short[] r = new short[tam];
		System.arraycopy(arr,0,r,0,tam);
		return r;
	}
}
