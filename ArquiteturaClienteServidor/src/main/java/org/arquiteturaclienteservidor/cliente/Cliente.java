package org.arquiteturaclienteservidor.cliente;

import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        while (true) {
            System.out.println("Digite o ID (ou 'sair' para terminar): ");
            try {
                userInput = stdIn.readLine();
                if ("sair".equalsIgnoreCase(userInput)) {
                    break;
                }

                try (Socket socket = new Socket("localhost", 5050);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    out.println(userInput);
                    String response = in.readLine();
                    System.out.println("Resposta do servidor: " + response);

                    if (!response.equals("ID não encontrado.") && !response.equals("ID inválido.")) {
                        while (true) {
                            System.out.println("Digite uma mensagem (ou 'sair' para terminar): ");
                            userInput = stdIn.readLine();
                            out.println(userInput);
                            if ("sair".equalsIgnoreCase(userInput)) {
                                break;
                            }
                            response = in.readLine();
                            System.out.println("Resposta do servidor: " + response);
                        }
                    }
                } catch (UnknownHostException e) {
                    System.err.println("Host desconhecido.");
                    System.exit(1);
                } catch (IOException e) {
                    System.err.println("Não foi possível obter E/S para a conexão.");
                    System.exit(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
