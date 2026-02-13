package com.minimine.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Array;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Net {
    public static final String NOME = "[MiniMine]: ";
    public static final int TCP_PORTA = 9001;
    public static final int UDP_PORTA = 9002;
    public static final String CLIENTE_MODO = "CLIENTE";
    public static final String SERVIDOR_MODO = "SERVIDOR";
    
    public String modoAtual = SERVIDOR_MODO;
    // estruturas do servidor
    public ServerSocket servidorSocket;
    public Array<Cliente> clientes = new Array<>();
    public DatagramSocket udpSocket;
    // estruturas do cliente
    public Socket clienteSocket;
    public PrintWriter clienteDados;
    public BufferedReader clienteEntrada;
    public boolean conectado = false;
    public volatile String IP = null; // IP descoberto automaticamente

    public Net(String modoAtual) {
        Gdx.app.log(NOME, "Iniciando como: " + modoAtual);

        if(modoAtual.equals(SERVIDOR_MODO)) {
            // se for servidor, inicia a thread pra aceitar clientes TCP
            Gdx.app.postRunnable(new Runnable() {
					public void run() {
						iniciarTcpServidor();
					}
				});
            // e inicia a thread pra responder a pedidos de descoberta UDP
            new Thread(new Runnable() {
					public void run() {
						iniciarReceptor();
					}
				}).start();
        } else if(modoAtual.equals(CLIENTE_MODO)) {
            // se for cliente, inicia a thread de descoberta
            new Thread(new Runnable() {
					public void run() {
						procurarE_Conectar();
					}
				}).start();
        }
    }
    // logica do servidor(TCP, conexão e UDP(descoberta))
    public void iniciarTcpServidor() {
        ServerSocketHints servidorInfo = new ServerSocketHints();
        servidorInfo.acceptTimeout = 0;
        try {
            servidorSocket = Gdx.net.newServerSocket(Protocol.TCP, TCP_PORTA, servidorInfo);
            Gdx.app.log(NOME, "Servidor TCP iniciado. Porta " + TCP_PORTA);
            // inicia a thread que aceita novas conexões TCP
            new Thread(new Runnable() {
					public void run() {
						while(servidorSocket != null) {
							try {
								Socket socket = servidorSocket.accept(null);
								Gdx.app.log(NOME, "Cliente TCP conectado: " + socket.getRemoteAddress());

								Cliente cliente = new Cliente(socket);
								clientes.add(cliente);
								new Thread(cliente).start();
							} catch(Exception e) {
								Gdx.app.error(NOME, "Erro ao aceitar conexão TCP: " + e.getMessage());
								if(servidorSocket == null) break;
							}
						}
					}
				}).start();
        } catch(Exception e) {
            Gdx.app.error(NOME, "Falha ao iniciar Servidor TCP: " + e.getMessage());
        }
    }
    // escuta por pacotes de descoberta UDP e responde
    public void iniciarReceptor() {
        Gdx.app.log(NOME, "Ouvindo por pedidos de descoberta UDP na porta " + UDP_PORTA);
        try {
            udpSocket = new DatagramSocket(UDP_PORTA);
            byte[] buffer = new byte[1024];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

            while(udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.receive(pacote);
                String msg = new String(pacote.getData(), 0, pacote.getLength());

                if(msg.startsWith("[MINIMINE]: descobrindo servidor")) {
                    Gdx.app.log(NOME + "-descoberta", "Pedido de descoberta de " + pacote.getAddress().getHostAddress());
                    
                    byte[] respostaDados = "[MiniMine]: servidor encontrado".getBytes();
                    // servidor responde diretamente pro cliente que enviou a mrnsagem
                    DatagramPacket respostaPacote = new DatagramPacket(respostaDados, respostaDados.length, pacote.getAddress(), pacote.getPort());

                    udpSocket.send(respostaPacote);
                    Gdx.app.log(NOME + "-descoberta", "Resposta de confirmação enviada.");
                }
            }
        } catch(Exception e) {
            Gdx.app.error(NOME, "Erro no listener UDP: " + e.getMessage());
        } finally {
            if(udpSocket != null) udpSocket.close();
        }
    }
    // classe pra comunicar 1 cliente especifico
    public class Cliente implements Runnable {
        public final Socket socket;
        public final BufferedReader entrada;
        public final PrintWriter dados;
        public boolean rodando = true;

        public Cliente(Socket socket) throws IOException {
            this.socket = socket;
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.dados = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                String linhaEntrada;
                while(rodando && (linhaEntrada = entrada.readLine()) != null) {
                    Gdx.app.log(NOME + "-Servidor", "Recebido do Cliente: " + linhaEntrada);
                    // envia mensagem devolta
                    String resposta = "Servidor recebeu: " + linhaEntrada + "Olá devolta do servidor";
                    dados.println(resposta);
                    Gdx.app.log(NOME + "-Servidor", "Enviando de volta: " + resposta);
                }
            } catch(IOException e) {
                Gdx.app.error(NOME + "-Servidor", "Conexão perdida com o cliente: " + socket.getRemoteAddress());
            } finally {
                clientes.removeValue(this, true);
                try {
                    socket.dispose();
                } catch(Exception e) {
                    Gdx.app.error(NOME, "Erro ao fechar socket do cliente.", e);
                }
            }
        }
    }
    // logica do cliente(UDP - Descoberta e TCP - Conexão) :
    // tenta descobrir o servidor via UDP e então conecta via TCP
    public void procurarE_Conectar() {
        Gdx.app.log(NOME, "Iniciando descoberta de Servidor...");
        int tentativas = 0;
        final int MAX_TENTATIVAS = 5;
        // tenta descobrir o servidor via UDP Broadcast
        while(IP == null && tentativas < MAX_TENTATIVAS) {
            procurarServidor();
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            tentativas++;
        }
        if(IP != null) {
            Gdx.app.log(NOME, "Servidor encontrado em: " + IP);
            // conecta ao servidor via TCP
            Gdx.app.postRunnable(new Runnable() {
					public void run() {
						conectarServidorTcp();
					}
				});
        } else {
            Gdx.app.error(NOME, "Não foi possível encontrar o Servidor após " + MAX_TENTATIVAS + " tentativas.");
        }
    }

    public void procurarServidor() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(500); // espera 0.5s pela resposta
            // envia o pacote pra todos na rede
            byte[] dadosEnvio = "[MINIMINE]: descobrindo servidor".getBytes();
            
            DatagramPacket envioPacote = new DatagramPacket(dadosEnvio, dadosEnvio.length, InetAddress.getByName("255.255.255.255"), UDP_PORTA);
            socket.send(envioPacote);
            Gdx.app.log(NOME + "-descoberta", "Pacote de descoberta enviado...");
            // espera a resposta do servidor
            byte[] receBuffer = new byte[1024];
            DatagramPacket pacoteRecebido = new DatagramPacket(receBuffer, receBuffer.length);
            socket.receive(pacoteRecebido);

            String resposta = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()).trim();
            // verifica se a resposta é a confirmação do servidor
            if(resposta.equals("[MiniMine]: servidor encontrado")) {
                // IP do servidor encontrado a partir do pacote de resposta
                IP = pacoteRecebido.getAddress().getHostAddress();
            }
        } catch(IOException e) {
            Gdx.app.log(NOME + "-descoberta", "Nenhuma resposta de servidor na rede.");
        } finally {
            if(socket != null) socket.close();
        }
    }
    // conecta ao servidor TCP usando o IP descoberto
    public void conectarServidorTcp() {
        SocketHints clienteInfo = new SocketHints();
        clienteInfo.connectTimeout = 5000;
        try {
            clienteSocket = Gdx.net.newClientSocket(Protocol.TCP, IP, TCP_PORTA, clienteInfo);
            clienteDados = new PrintWriter(clienteSocket.getOutputStream(), true);
            clienteEntrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            conectado = true;

            Gdx.app.log(NOME, "Conexão TCP estabelecida com " + IP + "!");
            // envia a mensagem de teste
            enviarMsg("Oi do Cliente");
            // pra escutar a resposta(echo) do servidor
            new Thread(new Runnable() {
					public void run() {
						receberMsgServidor();
					}
				}).start();
        } catch(Exception e) {
            Gdx.app.error(NOME, "Falha ao conectar ao Servidor TCP: " + e.getMessage());
            conectado = false;
        }
    }

    public void enviarMsg(String msg) {
        if(conectado) {
            Gdx.app.log(NOME + "-Cliente", "Enviando mensagem: " + msg);
            clienteDados.println(msg);
        } else {
            Gdx.app.error(NOME + "-Cliente", "Não conectado. Não pode enviar mensagem.");
        }
    }

    public void receberMsgServidor() {
        try {
            String servidorMsg;
            while(conectado && clienteEntrada != null && (servidorMsg = clienteEntrada.readLine()) != null) {
                Gdx.app.log("Mensagem Recebida", servidorMsg);
            }
        } catch(IOException e) {
            Gdx.app.error(NOME + "-Cliente", "Conexão com o servidor perdida: " + e.getMessage());
        } finally {
            conectado = false;
            Gdx.app.log(NOME + "-Cliente", "Cliente desconectado.");
        }
    }

    public void liberar() {
        if(servidorSocket != null) {
            for(Cliente cliente : clientes) {
                cliente.rodando = false;
            }
            try {
                servidorSocket.dispose();
            } catch(Exception e) {
                Gdx.app.error(NOME, "Erro ao fechar servidor socket.", e);
            }
            servidorSocket = null;
        }
        if(udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        if(clienteSocket != null) {
            try {
                clienteSocket.dispose();
            } catch(Exception e) {
                Gdx.app.error(NOME, "Erro ao fechar cliente socket.", e);
            }
            clienteSocket = null;
        }
    }
}
