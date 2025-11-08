package com.minimine.utils.arrays;

public class IntArrayUtil {
	public int[] arr = new int[1024];
	public int tam = 0;

	public IntArrayUtil() {}

	public IntArrayUtil(int tam) {
		arr = new int[tam];
	}

	public void add(int v) {
		if(tam == arr.length) {
			int[] n = new int[arr.length*2];
			System.arraycopy(arr,0,n,0,arr.length);
			arr = n;
		}
		arr[tam++] = v;
	}

	public int[] praArray() {
		int[] r = new int[tam];
		System.arraycopy(arr,0,r,0,tam);
		return r;
	}
}
