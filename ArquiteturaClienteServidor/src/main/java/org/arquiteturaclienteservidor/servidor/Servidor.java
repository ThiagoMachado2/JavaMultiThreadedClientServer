package org.arquiteturaclienteservidor.servidor;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    private static HashMap<Integer, String> data = new HashMap<>();
    private static AtomicInteger clientCount = new AtomicInteger(0);

    public static void main(String[] args) {
        data.put(1, "Informação 1");
        data.put(2, "Informação 2");

        ExecutorService pool = Executors.newFixedThreadPool(4);
        try (ServerSocket serverSocket = new ServerSocket(5050)) {
            System.out.println("Servidor iniciado e aguardando conexões...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCount.incrementAndGet();
                pool.execute(new ClientHandler(clientSocket, clientId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int clientId;

        public ClientHandler(Socket socket, int clientId) {
            this.clientSocket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String request;
                boolean authenticated = false;

                while ((request = in.readLine()) != null) {
                    if (!authenticated) {
                        System.out.println("Cliente " + clientId + " ID recebido: " + request);
                        try {
                            int id = Integer.parseInt(request);
                            String response = data.getOrDefault(id, "ID não encontrado.");
                            out.println(response);
                            System.out.println("Cliente " + clientId + " Resposta enviada: " + response);
                            authenticated = true;
                        } catch (NumberFormatException e) {
                            out.println("ID inválido.");
                            System.out.println("Cliente " + clientId + " ID inválido.");
                        }
                    } else {
                        if ("sair".equalsIgnoreCase(request)) {
                            out.println("Conexão encerrada pelo cliente.");
                            break;
                        }
                        System.out.println("Cliente " + clientId + " Mensagem recebida: " + request);
                        out.println("Mensagem do servidor para Cliente " + clientId + ": " + request);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Conexão com o Cliente " + clientId + " encerrada.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
