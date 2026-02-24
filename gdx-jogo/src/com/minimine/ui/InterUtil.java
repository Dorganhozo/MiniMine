package com.minimine.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Gdx;
import com.minimine.Inicio;

public class InterUtil {
	public static BitmapFont carregarFonte(String caminho) {
		FreeTypeFontGenerator gerador = new FreeTypeFontGenerator(caminho.equals("ui/fontes/pixel.ttf") ? Gdx.files.internal(caminho) : Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		FreeTypeFontGenerator.FreeTypeFontParameter args = new FreeTypeFontGenerator.FreeTypeFontParameter();
		args.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_?!π√×|§°←↑↓→€¥¢£+-/×%^*#$&|=,.:;!?(){}[]<>@\"'~áãâçéêèēóõôò";
		BitmapFont fonte = gerador.generateFont(args);
		gerador.dispose();
		return fonte;
	}
	public static BitmapFont carregarFonte(String caminho, int tamanho) {
		FreeTypeFontGenerator gerador = new FreeTypeFontGenerator(caminho.equals("ui/fontes/pixel.ttf") ? Gdx.files.internal(caminho) : Gdx.files.absolute(Inicio.externo+"/MiniMine/mods/"+caminho));
		FreeTypeFontGenerator.FreeTypeFontParameter args = new FreeTypeFontGenerator.FreeTypeFontParameter();
		args.size = tamanho;
		args.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_?!π√×|§°←↑↓→€¥¢£+-/×%^*#$&|=,.:;!?(){}[]<>@\"'~áãâçéêèēóõôò";
		BitmapFont fonte = gerador.generateFont(args);
		gerador.dispose();
		return fonte;
	}
}
