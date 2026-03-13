package com.minimine.utils;

import com.minimine.mundo.Mundo;
import java.io.File;
import com.minimine.Inicio;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Map;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import com.minimine.ui.UI;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Gdx;
import java.io.FileWriter;
import com.badlogic.gdx.math.Matrix4;
import com.minimine.entidades.Jogador;
import java.io.FileReader;
import java.util.List;
import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;
import com.badlogic.gdx.graphics.Texture;
import com.minimine.entidades.Inventario;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import com.minimine.mundo.Chave;
import java.util.concurrent.ConcurrentHashMap;
import com.minimine.mundo.Chunk;
import com.minimine.mundo.ChunkUtil;
import com.minimine.mundo.blocos.Bloco;
import com.badlogic.gdx.graphics.Mesh;
import com.minimine.graficos.Texturas;
import com.minimine.cenas.Jogo;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.minimine.mundo.blocos.BlocoEstrutura;
import com.minimine.mundo.ChunkLuz;

public class ArquivosUtil {
    public static final int[] VERSAO = { 0, 0, 1 };
	public static final String versao = "v" + VERSAO[0] + "." + VERSAO[1] + "." + VERSAO[2];
	public static boolean debug = true;

    public static final int VERSAO_MINIES = 1;
    public static final String ID_BLOCO_NULO = "bloco_nulo";

    // salva o mundo compactado(.mini), e faz escrita atomica para evitar arquivos truncados
    public static void svMundo(Mundo mundo, Jogador jogador) {
        File pasta = new File(Inicio.externo + "/MiniMine/mundos");
        if(!pasta.exists()) pasta.mkdirs();

        File destino = new File(pasta, URLEncoder.encode(mundo.nome) + ".mini");
        File tmp = new File(pasta, URLEncoder.encode(mundo.nome) + ".mini.tmp");

        try {
            // escreve em arquivo temporario
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

            try {
                // versao.txt
                zos.putNextEntry(new ZipEntry("versao.txt"));

                byte[] vt = versao.getBytes(Charset.forName("UTF-8"));
                zos.write(vt);
                zos.closeEntry();
                // mundo.bin(escreve diretamente no zip usando o mesmo DataOutputStream)
                zos.putNextEntry(new ZipEntry("mundo.bin"));
                gravarMundo(dos, mundo);
                dos.flush();
                zos.closeEntry();
                // jogador.bin
                zos.putNextEntry(new ZipEntry("jogador.bin"));
                gravarJogador(dos, jogador);
                dos.flush();
                zos.closeEntry();
                // inventario.bin
                zos.putNextEntry(new ZipEntry("inventario.bin"));
                gravarInventario(dos, jogador);
                dos.flush();
                zos.closeEntry();
                // ciclo.bin
                zos.putNextEntry(new ZipEntry("ciclo.bin"));
                gravarCiclo(dos);
                dos.flush();
                zos.closeEntry();
                zos.finish();
            } finally {
                try { dos.close(); } catch(Throwable t) {}
            }
            // renomeia de forma atomica quando possivel
            if(tmp.exists()) {
                if(destino.exists()) destino.delete();
                boolean ok = tmp.renameTo(destino);
                if(!ok) {
                    FileOutputStream fos = null;
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(tmp);
                        fos = new FileOutputStream(destino);
                        byte[] buf = new byte[8192];
                        int r;
                        while((r = fis.read(buf)) > 0) fos.write(buf, 0, r);
                        fos.flush();
                    } finally {
                        try { if(fos != null) fos.close(); } catch(Throwable t) {}
                        try { if(fis != null) fis.close(); } catch(Throwable t) {}
                    }
                    tmp.delete();
                }
            }
            if(debug) Gdx.app.log("ArquivosUtil", "[AVISO] mundo salvo");
        } catch(Throwable t) {
            Gdx.app.log("ArquivosUtil", "[ERRO] falha ao salvar mundo: " + t.getMessage());
            if(tmp.exists()) tmp.delete();
        }
    }
    // carrega o mundo, nao marca Mundo.carregado a menos que o carregamento seja concluído com sucesso
    public static void crMundo(Mundo mundo, Jogador jogador) {
        File arquivo = new File(Inicio.externo + "/MiniMine/mundos/" + mundo.nome + ".mini");
        if(!arquivo.exists() || arquivo.length() <= 4) {
            if(debug) Gdx.app.log("ArquivosUtil", "[INFO] .mini não existe ou é muito pequeno: " + arquivo.getAbsolutePath());
            Mundo.carregado = false;
            return;
        }
        ZipInputStream zis = null;
        boolean sucesso = false;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(arquivo)));
            DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

            ZipEntry e;
            boolean qualquer = false;
            while((e = zis.getNextEntry()) != null) {
                qualquer = true;
                String nome = e.getName();
                if(debug) Gdx.app.log("ArquivosUtil", "[DEBUG] lendo entrada: " + nome);
                try {
                    if("versao.txt".equals(nome)) {
                        // ler linha simples
                        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                        byte[] buf = new byte[512];
                        int r;
                        while((r = zis.read(buf)) > 0) tmp.write(buf, 0, r);
                        String v = new String(tmp.toByteArray(), Charset.forName("UTF-8")).trim();
						if(!versao.equals(v)) {
							if(debug) Gdx.app.log("ArquivosUtil", "[AVISO] a versao "+v+" do mundo não e a mais atual "+versao);
						}
                        if(debug) Gdx.app.log("ArquivosUtil", "[DEBUG] versao.txt: " + v);
                    } else if("mundo.bin".equals(nome)) {
                        lerMundo(dis, mundo);
                        if(debug) Gdx.app.log("ArquivosUtil", "[DEBUG] mundo.bin lido");
                    } else if("jogador.bin".equals(nome)) {
                        lerJogador(dis, jogador);
                        if(debug) Gdx.app.log("ArquivosUtil", "[DEBUG] jogador.bin lido");
                    } else if("inventario.bin".equals(nome)) {
                        lerInventario(dis, jogador);
                        if(debug) Gdx.app.log("ArquivosUtil", "[DEBUG] inventario.bin lido");
                    } else if("ciclo.bin".equals(nome)) {
                        DiaNoiteUtil.tempo = dis.readFloat();
                        DiaNoiteUtil.tempo_velo = dis.readFloat();
                        if(debug) Gdx.app.log("ArquivosUtil", "[DEBUG] ciclo.bin lido");
                    } else {
                        // garante consumo da entrada
                        byte[] pularBuf = new byte[512];
                        while(zis.read(pularBuf) > 0) {}
                        if(debug) Gdx.app.log("ArquivosUtil", "[AVISO] entrada desconhecida: " + nome);
                    }
                } catch(Throwable inner) {
                    Gdx.app.log("ArquivosUtil", "[ERRO] falha ao processar entrada '" + nome + "': " + inner.getMessage());
                    // continua pra tentar carregar o maximo possivel
                } finally {
                    try { zis.closeEntry(); } catch(Throwable t) {}
                }
            }
            if(!qualquer) {
                Gdx.app.log("ArquivosUtil", "[ERRO] .mini vazio ou corrompido");
                sucesso = false;
            } else {
                sucesso = true;
            }
        } catch(Throwable t) {
            Gdx.app.log("ArquivosUtil", "[ERRO] falha geral ao ler .mini: " + t.getMessage());
            sucesso = false;
        } finally {
            try { if(zis != null) zis.close(); } catch(Throwable t) {}
        }
        if(sucesso && debug) Gdx.app.log("ArquivosUtil", "[AVISO] mundo carregado");
    }
	// gravadores e leitores de binarios:
    public static void gravarMundo(DataOutputStream dos, Mundo mundo) throws IOException {
        dos.writeLong(mundo.semente);
        // quantos chunks salvos
        dos.writeInt(mundo.chunksMod.size());
        for(Map.Entry<Long, Chunk> e : mundo.chunksMod.entrySet()) {
            long chave = e.getKey();
            Chunk chunk = e.getValue();
            int cx = Mundo.TAM_CHUNK;
            int cy = Mundo.Y_CHUNK;
            int cz = Mundo.TAM_CHUNK;
            dos.writeLong(chave);
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

            for(int x = 0; x < cx; x++) {
                for(int y = 0; y < cy; y++) {
                    for(int z = 0; z < cz; z++) {
                        int b = ChunkUtil.obterBloco(x, y, z, chunk);
                        if(b != 0) {
							CharSequence bloco = Bloco.numIds.get(b).nome;
                            dos.writeInt(x);
                            dos.writeInt(y);
                            dos.writeInt(z);
                            dos.writeUTF(""+bloco);
                        }
                    }
                }
            }
			short[] meta = chunk.meta;
			dos.writeInt(meta.length);
			for(int i = 0; i < meta.length; i++) dos.writeShort(meta[i]);
        }
        dos.flush();
    }
    public static void gravarJogador(DataOutputStream dos, Jogador jogador) throws IOException {
        dos.writeInt(jogador.modo);
        dos.writeFloat(jogador.posicao.x);
        dos.writeFloat(jogador.posicao.y);
        dos.writeFloat(jogador.posicao.z);
        dos.writeFloat(jogador.yaw);
        dos.writeFloat(jogador.tom);
        dos.writeUTF(""+jogador.item);
        dos.writeInt(jogador.ALCANCE);
        dos.writeInt(jogador.inv != null ? jogador.inv.slotSelecionado : 0);
		dos.writeFloat(jogador.velo);
		dos.writeBoolean(jogador.agachado);
		dos.writeBoolean(jogador.nasceu);
        dos.flush();
    }

    public static void gravarInventario(DataOutputStream dos, Jogador jogador) throws IOException {
		try {
			if(jogador.inv == null || jogador.inv.itens == null) {
				dos.writeInt(0);
				dos.flush();
				return;
			}
			dos.writeInt(jogador.inv.itens.length);
			for(int i = 0; i < jogador.inv.itens.length; i++) {
				if(jogador.inv.itens[i] == null) {
					dos.writeBoolean(false);
				} else {
					dos.writeBoolean(true);
					dos.writeUTF(jogador.inv.itens[i].nome+"");
					dos.writeInt(jogador.inv.itens[i].quantidade);
				}
			}
			dos.flush();
		} catch(Exception e) {
			Gdx.app.log("ArquivosUtil", "[ERRO] ao gravar inv: "+e);
		}
    }

    public static void gravarCiclo(DataOutputStream dos) throws IOException {
        dos.writeFloat(DiaNoiteUtil.tempo);
        dos.writeFloat(DiaNoiteUtil.tempo_velo);
        dos.flush();
    }

	// leitores
    public static void lerMundo(DataInputStream dis, Mundo mundo) throws IOException {
        mundo.semente = dis.readLong();
        int totalChunks = dis.readInt();

        for(int i = 0; i < totalChunks; i++) {
            long chave = dis.readLong();

            Chunk chunk = new Chunk();
            ChunkUtil.compactar(ChunkUtil.bitsPraMaxId(chunk.maxIds), chunk);
            chunk.x = Chave.x(chave);
            chunk.z = Chave.z(chave);

            int totalNaoAr = dis.readInt();
            for(int k = 0; k < totalNaoAr; k++) {
                int x = dis.readInt();
                int y = dis.readInt();
                int z = dis.readInt();
                CharSequence id = dis.readUTF();
                ChunkUtil.defBloco(x, y, z, id, chunk);
            }
			short[] meta = new short[dis.readInt()];
			for(int d = 0; d < meta.length; d++) meta[d] = dis.readShort();
			chunk.meta = meta;

			chunk.malha = null;
            if(mundo.chunksMod == null) mundo.chunksMod = new ConcurrentHashMap<Long, Chunk>();
            if(mundo.chunks == null) mundo.chunks = new ConcurrentHashMap<Long, Chunk>();

            mundo.chunksMod.put(chave, chunk);
			mundo.chunks.put(chave, chunk);

            chunk.att = true;
			chunk.dadosProntos = true;
			ChunkLuz.calcularLuz(chunk);
			mundo.estados.put(chave, 2);
        }
    }

    public static void lerJogador(DataInputStream dis, Jogador jogador) throws IOException {
        jogador.modo = dis.readInt();
        jogador.posicao = new Vector3(dis.readFloat(), dis.readFloat(), dis.readFloat());
        jogador.yaw = dis.readFloat();
        jogador.tom = dis.readFloat();
        jogador.item = dis.readUTF();
        jogador.ALCANCE = dis.readInt();
        if(jogador.inv == null) jogador.inv = new Inventario(jogador);
        jogador.inv.slotSelecionado = dis.readInt();
		jogador.velo = dis.readFloat();
		jogador.agachado = dis.readBoolean();
		jogador.nasceu = dis.readBoolean();
    }

    public static void lerInventario(DataInputStream dis, Jogador jogador) throws IOException {
		try {
			int total = dis.readInt();
			if(jogador.inv == null || total == 0) jogador.inv = new Inventario(jogador);
			if(jogador.inv.itens == null || (jogador.inv.itens.length != total && total != 0)) jogador.inv.itens = new Inventario.Item[total];

			for(int i = 0; i < total; i++) {
				boolean temItem = false;
				try {
					temItem = dis.readBoolean();
				} catch(Throwable t) {
					Gdx.app.log("ArquivosUtil", "[ERRO] falha ao ler marcação de item slot " + i + ": " + t.getMessage());
					jogador.inv.itens[i] = null;
					continue;
				}
				if(temItem) {
					String nome = dis.readUTF();
					int quantidade = dis.readInt();

					TextureRegion textura = null;

					for(Bloco b : Bloco.blocos) {
                        if(b == null) continue;
                        if(b.nome.equals(nome)) {
                            textura = Texturas.atlas.obter(b.lados);
                            break;
                        }
                    }
                    if(textura == null) {
                        Gdx.app.log("[Inventario]", "textura não encontrada para: " + nome);
                        textura = Texturas.atlas.obter("terra");
                    }
					jogador.inv.itens[i] = new Inventario.Item(nome, textura, quantidade);
				} else {
					jogador.inv.itens[i] = null;
				}
			}
		} catch(Exception e) {
			Gdx.app.log("ArquivosUtil", "[ERRO] ao carregar inv: "+e);
		}
    }
	
    // svEstrutura: salva uma região do mundo como .minies
    /*
     * varre a bcaixa[baseX..baseX+larg-1, baseY..baseY+alt-1, baseZ..baseZ+prof-1],
     * descarta ar(id==0) e bloco_nulo, salva os demais com coordenadas locais

     * arquivo: MiniMine/estruturas/<nome>.minies
     * formato: veja cabeçalho de BlocoEstrutura.java
	 */
    public static void svEstrutura(
		String nome,
		int larg, int alt, int prof,
		int ancX, int ancY, int ancZ,
		int baseX, int baseY, int baseZ) throws IOException {

        File pasta = new File(Inicio.externo + "/MiniMine/estruturas");
        if(!pasta.exists()) pasta.mkdirs();

        File destino = new File(pasta, nome + ".minies");
        File tmp = new File(pasta, nome + ".minies.tmp");

        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            // cabeçalho
            dos.writeInt(VERSAO_MINIES);
            dos.writeUTF(nome);
            dos.writeInt(larg);
            dos.writeInt(alt);
            dos.writeInt(prof);
            dos.writeInt(ancX);
            dos.writeInt(ancY);
            dos.writeInt(ancZ);

            // primeiro passo: conta blocos validos
            int total = 0;
            for(int lx = 0; lx < larg; lx++) {
                for(int ly = 0; ly < alt; ly++) {
                    for(int lz = 0; lz < prof; lz++) {
                        int id = Mundo.obterBlocoMundo(baseX + lx, baseY + ly, baseZ + lz);
                        if(id == 0) continue;
                        Bloco b = Bloco.numIds.get(id);
                        if(b == null) continue;
                        if(ID_BLOCO_NULO.equals("" + b.nome)) continue;
                        total++;
                    }
                }
            }
            dos.writeInt(total);

            // segundo passo: escreve blocos
            for(int lx = 0; lx < larg; lx++) {
                for(int ly = 0; ly < alt; ly++) {
                    for(int lz = 0; lz < prof; lz++) {
                        int id = Mundo.obterBlocoMundo(baseX + lx, baseY + ly, baseZ + lz);
                        if(id == 0) continue;
                        Bloco b = Bloco.numIds.get(id);
                        if(b == null) continue;
                        if(ID_BLOCO_NULO.equals("" + b.nome)) continue;
                        dos.writeInt(lx);
                        dos.writeInt(ly);
                        dos.writeInt(lz);
                        dos.writeUTF("" + b.nome);
						dos.writeShort(Mundo.obterMetaMundo(baseX + lx, baseY + ly, baseZ + lz));
                    }
                }
            }
            dos.flush();
        } finally {
            try {
				if(dos != null) dos.close();
			} catch(Throwable t) {}
        }
        // escrita atomica
        if(tmp.exists()) {
            if(destino.exists()) destino.delete();
            boolean ok = tmp.renameTo(destino);
            if(!ok) {
                FileOutputStream fos = null;
                FileInputStream  fis = null;
                try {
                    fis = new FileInputStream(tmp);
                    fos = new FileOutputStream(destino);
                    byte[] buf = new byte[8192];
                    int r;
                    while((r = fis.read(buf)) > 0) fos.write(buf, 0, r);
                    fos.flush();
                } finally {
                    try {
						if(fos != null) fos.close();
					} catch(Throwable t) {}
                    try {
						if(fis != null) fis.close();
					} catch(Throwable t) {}
                }
                tmp.delete();
            }
        }
        if(debug) Gdx.app.log("ArquivosUtil", "[AVISO] estrutura salva: " + destino.getAbsolutePath());
    }

    // crEstrutura — carrega um .minies e retorna os dados
    /*
     * retorna um DadosEstrutura com todos os blocos e metadados,
     * ou null se o arquivo não existir ou estiver corrompido
	 */
    public static DadosEstrutura crEstrutura(String nome) {
        File arquivo = new File(
            Inicio.externo + "/MiniMine/estruturas/" + nome + ".minies");

        if(!arquivo.exists() || arquivo.length() <= 4) {
            if(debug) Gdx.app.log("ArquivosUtil", "[INFO] .minies não encontrado: " + arquivo.getAbsolutePath());
            return null;
        }
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(arquivo)));

            int versao = dis.readInt();
            if(versao != VERSAO_MINIES) {
                Gdx.app.log("ArquivosUtil", "[AVISO] versão .minies incompatível: " + versao);
                // tenta carregar mesmo assim, estrutura não mudou ainda
            }
            DadosEstrutura d = new DadosEstrutura();
            d.nome = dis.readUTF();
            d.larg = dis.readInt();
            d.alt = dis.readInt();
            d.prof = dis.readInt();
            d.ancX = dis.readInt();
            d.ancY = dis.readInt();
            d.ancZ = dis.readInt();

            int total = dis.readInt();
            d.lx = new int[total];
            d.ly = new int[total];
            d.lz = new int[total];
            d.ids = new String[total];
            d.meta = new short[total];

            for(int i = 0; i < total; i++) {
                d.lx[i] = dis.readInt();
                d.ly[i] = dis.readInt();
                d.lz[i] = dis.readInt();
                d.ids[i] = dis.readUTF();
                d.meta[i] = dis.readShort();
            }
            if(debug) Gdx.app.log("ArquivosUtil", "[AVISO] estrutura carregada: " + nome + " (" + total + " blocos)");
            return d;
        } catch(Throwable t) {
            Gdx.app.log("ArquivosUtil", "[ERRO] falha ao ler .minies '" + nome + "': " + t.getMessage());
            return null;
        } finally {
            try {
				if(dis != null) dis.close();
			} catch(Throwable t) {}
        }
    }

    // DadosEstrutura: dados retornados por crEstrutura()
    public static final class DadosEstrutura {
        public String nome;
        public int larg, alt, prof;
        public int ancX, ancY, ancZ;
        // coordenadas locais de cada bloco
        public int[] lx, ly, lz;
        // id de string de cada bloco
        public String[] ids;
        // metadado de cada bloco
        public short[] meta;
        /*
         * coloca a estrutura no mundo com origem em(ox, oy, oz)
         * a ancora é descontada: o bloco de ancora fica em(ox, oy, oz)
         * blocos de ar(ids[i] == null ou "ar") são ignorados
         * pra sobrescrever tudo inclusive ar, chame colocarMundo(ox,oy,oz,true)
         */
        public void colocarMundo(int ox, int oy, int oz) {
            colocarMundo(ox, oy, oz, false);
        }

        public void colocarMundo(int ox, int oy, int oz, boolean sobrescreverTudo) {
            if(lx == null) return;
            // coleta chaves de chunks afetados (incluindo vizinhos de borda)
            java.util.Set<Long> afetados = new java.util.HashSet<Long>();
            for(int i = 0; i < lx.length; i++) {
                int vx = ox + (lx[i] - ancX);
                int vy = oy + (ly[i] - ancY);
                int vz = oz + (lz[i] - ancZ);
                String id = ids[i];
                if(!sobrescreverTudo && (id == null || "ar".equals(id))) continue;
                Mundo.defBlocoMundo(vx, vy, vz, id);
                Mundo.defMetaMundo(vx, vy, vz, meta[i]);
                // chunk do bloco e vizinhos laterais (faces compartilhadas entre chunks)
                int cx = Math.floorDiv(vx, Mundo.TAM_CHUNK);
                int cz = Math.floorDiv(vz, Mundo.TAM_CHUNK);
                afetados.add(Chave.calcularChave(cx, cz));
                afetados.add(Chave.calcularChave(cx + 1, cz));
                afetados.add(Chave.calcularChave(cx - 1, cz));
                afetados.add(Chave.calcularChave(cx, cz + 1));
                afetados.add(Chave.calcularChave(cx, cz - 1));
            }
            for(long chave : afetados) {
                Chunk c = Mundo.chunks.get(chave);
                if(c != null) c.att = true;
            }
        }
    }

    // utilitarios:
    public static void criar(String caminho) {   
        caminho = caminho.replace("/", File.separator);
		int ultimoPasso = caminho.lastIndexOf(File.separator);    
		if(ultimoPasso > 0) {    
			String dirCaminho = caminho.substring(0, ultimoPasso);    
			criarDir(dirCaminho);    
		}    
		File arquivo = new File(caminho);    
		try {    
			if(!arquivo.exists()) arquivo.createNewFile();    
		} catch(Exception e) {    
			Gdx.app.log("ArquivosUtil", "[ERRO]: criando "+e.getMessage()+File.separator+caminho+File.separator);    
		}    
	}    

	public static String ler(String caminho) {    
        caminho = caminho.replace("/", File.separator);
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
					Gdx.app.log("ArquivosUtil", "[ERRO]: lendo "+e.getMessage()+" \""+caminho+"\"");
				}    
			}    
		}    
		return sb.toString();    
	}    

	public static void escrever(String caminho, String texto) {
        caminho = caminho.replace("/", File.separator);
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
				Gdx.app.log("ArquivosUtil", "[ERRO]: escrevendo "+e.getMessage()+" caminho \""+caminho+"\"");    
			}    
		}    
	}    

	public static void delete(String caminho) {    
        caminho = caminho.replace("/", File.separator);
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
				if(subArquivo.isFile()) subArquivo.delete();    
			}    
		}    
		arquivo.delete();    
	}    

	public static List<String> listar(String caminho) {
        caminho = caminho.replace("/", File.separator);
		List<String> lista = new ArrayList<>();
		File dir = new File(caminho);    
		if(!dir.exists() || dir.isFile()) return null;

		File[] listaArquivos = dir.listFiles();    
		if(listaArquivos == null || listaArquivos.length <= 0) return null;    

		if(lista==null) return null;    
		lista.clear();    
		for(File arquivo : listaArquivos) {    
			lista.add(arquivo.getName());    
		}
		return lista;
	}    

	public static void listarAbs(String caminho, List<String> lista) {    
        caminho = caminho.replace("/", File.separator);
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
        caminho = caminho.replace("/", File.separator); 
		File arquivo = new File(caminho);    
		return arquivo.exists();    
	}    

	public static void criarDir(String caminho) {    
        caminho = caminho.replace("/", File.separator);
		if(!existe(caminho)) {    
			File arquivo = new File(caminho);    
			arquivo.mkdirs();    
		}    
	}
}
