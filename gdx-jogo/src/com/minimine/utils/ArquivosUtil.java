package com.minimine.utils;

import com.minimine.cenas.Mundo;
import java.io.File;
import com.minimine.Jogo;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Map;
import com.minimine.cenas.Chunk;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import com.minimine.cenas.UI;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Gdx;
import java.io.FileWriter;

public class ArquivosUtil {
	public static void svMundo(Mundo mundo) {
		try {
			File pasta = new File(Jogo.externo+"/MiniMine");
			if(!pasta.exists()) pasta.mkdirs();

			File arquivo = new File(pasta, mundo.nome+".mini");
			FileOutputStream fos = new FileOutputStream(arquivo);
			salvarMundo(fos, mundo.seed, mundo.chunksMod);
			fos.close();
		} catch(IOException e) {
			Gdx.app.log("ArquivosUtil", "[ERRO]: "+e.getMessage());
		}
	}

	public static void crMundo(Mundo mundo) {
		try {
			File arquivo = new File(Jogo.externo + "/MiniMine/"+mundo.nome+".mini");
			if(!arquivo.exists()) {
				Mundo.carregado = true;
				return;
			}
			FileInputStream fis = new FileInputStream(arquivo);
			carregarMundo(fis, mundo);
			fis.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void salvarMundo(OutputStream saida, int seed, Map<ChunkUtil.Chave, Chunk> chunksCarregados) throws IOException {
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(saida));
		// seed
		dos.writeInt(seed);
		// quantos chunks salvos
		dos.writeInt(chunksCarregados.size());
		for(Map.Entry<ChunkUtil.Chave, Chunk> entry : chunksCarregados.entrySet()) {
			ChunkUtil.Chave chave = entry.getKey(); // chave da chunk
			Chunk chunk = entry.getValue();
			int cx = Mundo.TAM_CHUNK;
			int cy = Mundo.Y_CHUNK;
			int cz = Mundo.TAM_CHUNK;
			// salva a chave da chunk
			dos.writeInt(chave.x);
			dos.writeInt(chave.z);
			// conta quantos bloxos tem na chunk
			int totalNaoAr = 0;
			for(int x = 0; x < cx; x++) {
				for(int y = 0; y < cy; y++) {
					for(int z = 0; z < cz; z++) {
						byte b = ChunkUtil.obterBloco(x, y, z, chunk);
						if(b != 0) totalNaoAr++;
					}
				}
			}
			dos.writeInt(totalNaoAr);
			// salva os dados dos blocos
			for(int x = 0; x < cx; x++) {
				for(int y = 0; y < cy; y++) {
					for(int z = 0; z < cz; z++) {
						byte b = ChunkUtil.obterBloco(x, y, z, chunk);
						if(b != 0) {
							dos.writeInt(x);
							dos.writeInt(y);
							dos.writeInt(z);
							dos.writeByte(b);
						}
					}
				}
			}
		}
		dos.writeFloat(UI.jogador.posicao.x);
		dos.writeFloat(UI.jogador.posicao.y);
		dos.writeFloat(UI.jogador.posicao.z);

		dos.writeFloat(UI.tom);
		dos.writeFloat(UI.yaw);
		
		dos.flush();
		dos.close();
	}

	public static void carregarMundo(InputStream entrada, Mundo mundo) throws IOException {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(entrada));
		// bota a seed
		mundo.seed = dis.readInt();
		// quantidade ds chumks
		int totalChunks = dis.readInt();

		for(int i = 0; i < totalChunks; i++) {
			int chunkX = dis.readInt();
			int chunkZ = dis.readInt();
			
			Chunk chunk = new Chunk();
			chunk.x = chunkX;
			chunk.z = chunkZ;
			// constroi a chunk
			int totalNaoAr = dis.readInt();
			for(int k = 0; k < totalNaoAr; k++) {
				int x = dis.readInt();
				int y = dis.readInt();
				int z = dis.readInt();
				byte id = dis.readByte();
				// convertendo pra coordenadas globais
				ChunkUtil.defBloco(x, y, z, id, chunk);
			}
			mundo.chunksMod.put(new ChunkUtil.Chave(chunkX, chunkZ), chunk);
			mundo.chunks.put(new ChunkUtil.Chave(chunkX, chunkZ), chunk);
		}
		UI.jogador.posicao = new Vector3(dis.readFloat(), dis.readFloat(), dis.readFloat());
		UI.tom = dis.readFloat();
		UI.yaw = dis.readFloat();
		
		dis.close();
	}
	
	public static void criar(String caminho) {
        int ultimoPasso = caminho.lastIndexOf(File.separator);
        if(ultimoPasso > 0) {
            String dirCaminho = caminho.substring(0, ultimoPasso);
            criarDir(dirCaminho);
        }
        File arquivo = new File(caminho);
        try {
            if(!arquivo.exists()) arquivo.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void escreverArquivo(String caminho, String texto) {
        criar(caminho);
        FileWriter escritor = null;
        try {
            escritor = new FileWriter(new File(caminho), false);
            escritor.write(texto);
            escritor.flush();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(escritor != null) escritor.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	public static boolean existe(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists();
    }

    public static void criarDir(String caminho) {
        if(!existe(caminho)) {
            File arquivo = new File(caminho);
            arquivo.mkdirs();
        }
    }
}
