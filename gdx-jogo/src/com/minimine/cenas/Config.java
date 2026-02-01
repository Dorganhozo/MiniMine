package com.minimine.cenas;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.InputProcessor;
import com.minimine.ui.Botao;
import com.minimine.ui.Texto;
import com.minimine.ui.EstanteVertical;
import com.minimine.graficos.Texturas;
import com.minimine.ui.InterUtil;
import com.minimine.Cenas;
import com.minimine.Inicio;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.Preferences;
import com.minimine.mundo.Mundo;
import com.minimine.ui.UI;

public class Config implements Screen, InputProcessor {
    public SpriteBatch sb;
    public BitmapFont fonteTitulo;
    public BitmapFont fonteTexto;
    public List<Texto> textos;
    public List<Botao> botoes;
	
    public Preferences prefs;
	
    public Texto txtRaioValor;
    public Texto txtSensiValor;
    public Texto txtAproxValor;
    public Texto txtDistanciaValor;
    public Texto txtPOVValor;

    @Override
    public void show() {
        sb = new SpriteBatch();
        fonteTitulo = InterUtil.carregarFonte("ui/fontes/pixel.ttf", 50);
        fonteTexto = InterUtil.carregarFonte("ui/fontes/pixel.ttf", 35);
        textos = new ArrayList<>();
        botoes = new ArrayList<>();
		
		prefs = Gdx.app.getPreferences("MiniConfig");
		
        attInterface();

        Gdx.input.setInputProcessor(this);
    }

    public void attInterface() {
        textos.clear();
        botoes.clear();

        final int largura = Gdx.graphics.getWidth();
        final int altura = Gdx.graphics.getHeight();

        textos.add(new Texto("Configurações", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					GlyphLayout layout = new GlyphLayout(fonteTitulo, texto);
					x = (v - layout.width) / 2f;
					y = h - 80;
				}
			});
        
        textos.add(new Texto("Raio de Chunks:", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					x = 100;
					y = altura - 250;
				}
			});
        txtRaioValor = new Texto(String.valueOf(Mundo.RAIO_CHUNKS), 0, 0) {
            @Override
            public void aoAjustar(int v, int h) {
                GlyphLayout layout = new GlyphLayout(fonteTexto, texto);
                x = largura - 250 - layout.width;
                y = altura - 250;
            }
        };
        textos.add(txtRaioValor);

        botoes.add(new Botao(Texturas.texs.get("botao_e"), 0, 0, 80, 80, "diminuirRaio") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(Mundo.RAIO_CHUNKS > 1) {
						Mundo.RAIO_CHUNKS--;
						txtRaioValor.texto = String.valueOf(Mundo.RAIO_CHUNKS);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 230, altura - 280);
				}
			});
        botoes.add(new Botao(Texturas.texs.get("botao_d"), 0, 0, 80, 80, "aumentarRaio") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(Mundo.RAIO_CHUNKS < 20) {
						Mundo.RAIO_CHUNKS++;
						txtRaioValor.texto = String.valueOf(Mundo.RAIO_CHUNKS);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 130, altura - 280);
				}
			});
        textos.add(new Texto("Sensibilidade:", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					x = 100;
					y = altura - 350;
				}
			});
        txtSensiValor = new Texto(String.format("%.2f", UI.sensi), 0, 0) {
            @Override
            public void aoAjustar(int v, int h) {
                GlyphLayout layout = new GlyphLayout(fonteTexto, texto);
                x = largura - 250 - layout.width;
                y = altura - 350;
            }
        };
        textos.add(txtSensiValor);

        botoes.add(new Botao(Texturas.texs.get("botao_e"), 0, 0, 80, 80, "diminuirSensi") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.sensi > 0f) {
						UI.sensi -= 0.05f;
						txtSensiValor.texto = String.format("%.2f", UI.sensi);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 230, altura - 380);
				}
			});
        botoes.add(new Botao(Texturas.texs.get("botao_d"), 0, 0, 80, 80, "aumentarSensi") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.sensi < 5.0f) {
						UI.sensi += 0.05f;
						txtSensiValor.texto = String.format("%.2f", UI.sensi);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 130, altura - 380);
				}
			});
        textos.add(new Texto("Aproximação:", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					x = 100;
					y = altura - 450;
				}
			});
        txtAproxValor = new Texto(String.format("%.1f", UI.aprox), 0, 0) {
            @Override
            public void aoAjustar(int v, int h) {
                GlyphLayout layout = new GlyphLayout(fonteTexto, texto);
                x = largura - 250 - layout.width;
                y = altura - 450;
            }
        };
        textos.add(txtAproxValor);

        botoes.add(new Botao(Texturas.texs.get("botao_e"), 0, 0, 80, 80, "diminuirAprox") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.aprox > 0.1f) {
						UI.aprox -= 0.1f;
						txtAproxValor.texto = String.format("%.1f", UI.aprox);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 230, altura - 480);
				}
			});
        botoes.add(new Botao(Texturas.texs.get("botao_d"), 0, 0, 80, 80, "aumentarAprox") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.aprox < 200f) {
						UI.aprox += 0.1f;
						txtAproxValor.texto = String.format("%.1f", UI.aprox);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 130, altura - 480);
				}
			});
        textos.add(new Texto("Distância:", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					x = 100;
					y = altura - 550;
				}
			});
			
		txtDistanciaValor = new Texto(String.format("%.0f", UI.distancia), 0, 0) {
			@Override
			public void aoAjustar(int v, int h) {
				GlyphLayout layout = new GlyphLayout(fonteTexto, texto);
				x = largura - 250 - layout.width;
				y = altura - 550;
			}
		};
        textos.add(txtDistanciaValor);
        botoes.add(new Botao(Texturas.texs.get("botao_e"), 0, 0, 80, 80, "diminuirDistancia") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.distancia > 200f) {
						UI.distancia -= 50f;
						txtDistanciaValor.texto = String.format("%.0f", UI.distancia);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 230, altura - 580);
				}
			});
        botoes.add(new Botao(Texturas.texs.get("botao_d"), 0, 0, 80, 80, "aumentarDistancia") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.distancia < 1000f) {
						UI.distancia += 50f;
						txtDistanciaValor.texto = String.format("%.0f", UI.distancia);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 130, altura - 580);
				}
			});
        textos.add(new Texto("Campo de Visão:", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					x = 100;
					y = altura - 650;
				}
			});
		txtPOVValor = new Texto(String.valueOf(UI.pov), 0, 0) {
			@Override
			public void aoAjustar(int v, int h) {
				GlyphLayout layout = new GlyphLayout(fonteTexto, texto);
				x = largura - 250 - layout.width;
				y = altura - 650;
			}
		};
        textos.add(txtPOVValor);
        botoes.add(new Botao(Texturas.texs.get("botao_e"), 0, 0, 80, 80, "diminuirPOV") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.pov > 0) {
						UI.pov -= 5;
						txtPOVValor.texto = String.valueOf(UI.pov);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 230, altura - 680);
				}
			});
        botoes.add(new Botao(Texturas.texs.get("botao_d"), 0, 0, 80, 80, "aumentarPOV") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					if(UI.pov < 300) {
						UI.pov += 5;
						txtPOVValor.texto = String.valueOf(UI.pov);
					}
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos(largura - 130, altura - 680);
				}
			});
		botoes.add(new Botao(Texturas.texs.get("botao_opcao"), 0, 0, 200, 80, "voltar") {
				@Override
				public void aoTocar(int tx, int ty, int p) {
					prefs.putInteger("raioChunks", Mundo.RAIO_CHUNKS);
					prefs.putInteger("pov", UI.pov);
					prefs.putFloat("sensi", UI.sensi);
					prefs.putFloat("aprox", UI.aprox);
					prefs.putFloat("distancia", UI.distancia);
					prefs.flush();
					Inicio.defTela(Cenas.menu);
				}
				@Override
				public void aoAjustar(int v, int h) {
					defPos((v - tamX) / 2f, 50);
				}
			});
        textos.add(new Texto("Voltar", 0, 0) {
				@Override
				public void aoAjustar(int v, int h) {
					GlyphLayout layout = new GlyphLayout(fonteTexto, texto);
					x = (v - layout.width) / 2f;
					y = 85;
				}
			});
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sb.begin();

        txtRaioValor.texto = String.valueOf(Mundo.RAIO_CHUNKS);
        txtSensiValor.texto = String.format("%.2f", UI.sensi);
        txtAproxValor.texto = String.format("%.1f", UI.aprox);
        txtDistanciaValor.texto = String.format("%.0f", UI.distancia);
        txtPOVValor.texto = String.valueOf(UI.pov);

        for(Texto t : textos) {
            t.porFrame(delta, sb, (t.texto.equals("Configurações")) ? fonteTitulo : fonteTexto);
        }
        for(Botao b : botoes) {
            b.porFrame(delta, sb, fonteTexto);
        }
        sb.end();
    }

    @Override
    public void resize(int v, int h) {
        sb.getProjectionMatrix().setToOrtho2D(0, 0, v, h);
		
        for(Botao b : botoes) {
            if(b != null) b.aoAjustar(v, h);
        }
        for(Texto t : textos) {
            if(t != null) t.aoAjustar(v, h);
        }
    }

    @Override
    public void dispose() {
        if(sb != null) sb.dispose();
        if(fonteTitulo != null) fonteTitulo.dispose();
        if(fonteTexto != null) fonteTexto.dispose();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public boolean touchDown(int telaX, int telaY, int p, int b) {
        int y = Gdx.graphics.getHeight() - telaY;
        for(Botao bt : botoes) {
            if(bt.hitbox.contains(telaX, y)) {
                bt.aoTocar(telaX, y, p);
                return true;
            }
        }
        return false;
    }
	@Override public void pause() {}
    @Override public void resume() {}
    @Override public boolean touchUp(int telaX, int telaY, int p, int b) { return false; }
    @Override public boolean touchDragged(int telaX, int telaY, int p) { return false; }
    @Override public boolean mouseMoved(int telaX, int telaY) { return false; }
    @Override public boolean scrolled(float aX, float aY) { return false; }
    @Override public boolean keyDown(int c) { return false; }
    @Override public boolean keyUp(int c) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
}
