package com.minimine;

public interface JS {
	void iniciar(String caminho);
	void executar(String codigo);
	void API(Object classe, String nome);
	void config();
}
