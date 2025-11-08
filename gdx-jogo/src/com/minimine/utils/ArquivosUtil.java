package com.minimine.utils;

import com.minimine.cenas.Mundo;
import java.io.File;
import com.minimine.Inicio;
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
import com.badlogic.gdx.math.Matrix4;
import com.minimine.cenas.Jogador;
import java.io.FileReader;
import java.util.List;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;

public class ArquivosUtil {
	public static void svMundo(Mundo mundo, Jogador jogador) {
		try {
			File pasta = new File(Inicio.externo+"/MiniMine/mundos");
			if(!pasta.exists()) pasta.mkdirs();

			File arquivo = new File(pasta, mundo.nome+".mini");
			FileOutputStream fos = new FileOutputStream(arquivo);
			salvarMundo(fos, mundo.seed, mundo.chunksMod, jogador);
			fos.close();
		} catch(IOException e) {
			Gdx.app.log("ArquivosUtil", "[ERRO]: "+e.getMessage());
		}
	}

	public static void crMundo(Mundo mundo, Jogador jogador) {
		try {
			File arquivo = new File(Inicio.externo + "/MiniMine/mundos/"+mundo.nome+".mini");
			if(!arquivo.exists()) {
				Mundo.carregado = true;
				return;
			}
			FileInputStream fis = new FileInputStream(arquivo);
			carregarMundo(fis, mundo, jogador);
			fis.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void salvarMundo(OutputStream saida, int seed, Map<ChunkUtil.Chave, Chunk> chunksCarregados, Jogador jogador) throws IOException {
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(saida));
		dos.writeInt(Inicio.versao);
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
						int b = ChunkUtil.obterBloco(x, y, z, chunk);
						if(b != 0) totalNaoAr++;
					}
				}
			}
			dos.writeInt(totalNaoAr);
			// salva os dados dos blocos
			for(int x = 0; x < cx; x++) {
				for(int y = 0; y < cy; y++) {
					for(int z = 0; z < cz; z++) {
						int b = ChunkUtil.obterBloco(x, y, z, chunk);
						if(b != 0) {
							dos.writeInt(x);
							dos.writeInt(y);
							dos.writeInt(z);
							dos.writeInt(b);
						}
					}
				}
			}
		}
		dos.writeFloat(jogador.posicao.x);
		dos.writeFloat(jogador.posicao.y);
		dos.writeFloat(jogador.posicao.z);

		dos.writeFloat(jogador.tom);
		dos.writeFloat(jogador.yaw);
		
		dos.flush();
		dos.close();
	}

	public static void carregarMundo(InputStream entrada, Mundo mundo, Jogador jogador) throws IOException {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(entrada));
		int versao = dis.readInt();
		if(Inicio.versao != versao) {
			Gdx.app.log("ArquivosUtil", "[AVISO] a versão do mundo não é a mais atual");
		}
		// bota a seed
		mundo.seed = dis.readInt();
		// quantidade ds chumks
		int totalChunks = dis.readInt();

		for(int i = 0; i < totalChunks; i++) {
			int chunkX = dis.readInt();
			int chunkZ = dis.readInt();

			Chunk chunk = new Chunk();
			ChunkUtil.compactar(ChunkUtil.bitsPraMaxId(chunk.maxIds), chunk);
			chunk.x = chunkX;
			chunk.z = chunkZ;

			int totalNaoAr = dis.readInt();
			for(int k = 0; k < totalNaoAr; k++) {
				int x = dis.readInt();
				int y = dis.readInt();
				int z = dis.readInt();
				int id = dis.readInt();
				
				ChunkUtil.defBloco(x, y, z, id, chunk);
			}
			
			chunk.mesh = Mundo.meshReuso.obtain();

			mundo.chunksMod.put(new ChunkUtil.Chave(chunkX, chunkZ), chunk);
			mundo.chunks.put(new ChunkUtil.Chave(chunkX, chunkZ), chunk);
			
			attMeshChunk(chunk);
		}
		jogador.posicao = new Vector3(dis.readFloat(), dis.readFloat(), dis.readFloat());
		jogador.tom = dis.readFloat();
		jogador.yaw = dis.readFloat();

		dis.close();
	}
	
	public static void attMeshChunk(final Chunk chunk) {
		Mundo.exec.submit(new Runnable() {
				@Override
				public void run() {
					final FloatArrayUtil vertsGeral = new FloatArrayUtil(); 
					final ShortArrayUtil idcGeral = new ShortArrayUtil();

					ChunkUtil.attMesh(chunk, vertsGeral, idcGeral);

					Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								ChunkUtil.defMesh(chunk.mesh, vertsGeral, idcGeral);
								Matrix4 matrizTmp = new Matrix4();
								matrizTmp.setToTranslation(chunk.x * Mundo.TAM_CHUNK, 0, chunk.z * Mundo.TAM_CHUNK);
								chunk.mesh.transform(matrizTmp);
								chunk.att = false;
							}
						});
				}
			});
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
	
	public static String ler(String caminho) {
        criar(caminho);

        StringBuilder sb = new StringBuilder();
        FileReader fr = null;

        try {
            fr = new FileReader(new File(caminho));

            char[] buff = new char[1024];
            int tamanho = 0;

            while((tamanho = fr.read(buff)) > 0) sb.append(new String(buff, 0, tamanho));
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(fr != null) {
                try {
                    fr.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
	
	public static void escrever(String caminho, String texto) {
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
	
	public static void delete(String caminho) {
        File arquivo = new File(caminho);

        if(!arquivo.exists()) return;
        if(arquivo.isFile()) {
            arquivo.delete();
            return;
        }
        File[] arquivos = arquivo.listFiles();

        if(arquivos != null) {
            for(File subArquivo : arquivos) {
                if(subArquivo.isDirectory()) {
                    delete(subArquivo.getAbsolutePath());
                }
                if(subArquivo.isFile()) {
                    subArquivo.delete();
                }
            }
        }
        arquivo.delete();
    }
	
	public static void listar(String caminho, List<String> lista) {
        File dir = new File(caminho);
        if(!dir.exists() || dir.isFile()) return;

        File[] listaArquivos = dir.listFiles();
        if(listaArquivos == null || listaArquivos.length <= 0) return;

        if(lista==null) return;
        lista.clear();
        for(File arquivo : listaArquivos) {
            lista.add(arquivo.getName());
        }
    }

    public static void listarAbs(String caminho, List<String> lista) {
        File dir = new File(caminho);
        if(!dir.exists() || dir.isFile()) return;

        File[] listaArquivos = dir.listFiles();
        if(listaArquivos==null || listaArquivos.length <= 0) return;

        if(lista==null) return;
        lista.clear();
        for(File arquivo : listaArquivos) {
            lista.add(arquivo.getAbsolutePath());
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
