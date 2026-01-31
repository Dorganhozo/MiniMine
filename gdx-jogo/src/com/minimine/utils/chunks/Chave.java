package com.minimine.utils.chunks;

public class Chave {
	public int x, z;
	public Chave(int x, int z) {this.x = x; this.z = z;}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Chave)) return true;
		Chave chave = (Chave) o;
		return  x == chave.x && z == chave.z;
	}
	@Override
	public int hashCode() {
		return (x * 31) ^ z;
	}
}
