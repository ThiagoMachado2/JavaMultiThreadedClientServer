package org.arquiteturaclienteservidor.interfacecliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class InterfaceCliente extends JFrame {
    private JTextField inputField;
    private JTextArea displayArea;
    private JComboBox<String> clientSelector;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private boolean authenticated = false;

    public InterfaceCliente() {
        super("Interface Cliente");
        clientSelector = new JComboBox<>(new String[] { "Cliente 1", "Cliente 2" });
        clientSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                selectClient((String) clientSelector.getSelectedItem());
            }
        });

        inputField = new JTextField();
        inputField.setEditable(false);
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String userInput = event.getActionCommand();
                out.println(userInput);
                inputField.setText("");
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(clientSelector, BorderLayout.WEST);
        topPanel.add(inputField, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setSize(400, 300);
        setVisible(true);
    }

    public void startClient(String clientId) {
        try {
            socket = new Socket("localhost", 5050);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            inputField.setEditable(true);
            new Thread(() -> listenToServer(clientId)).start();
        } catch (IOException e) {
            displayArea.append("Erro na conexão com o servidor.\n");
        }
    }

    private void listenToServer(String clientId) {
        try {
            out.println(clientId);

            String response;
            while ((response = in.readLine()) != null) {
                final String finalResponse = response;
                SwingUtilities.invokeLater(() -> displayArea.append(finalResponse + "\n"));
                if (!authenticated && !response.equals("ID não encontrado.") && !response.equals("ID inválido.")) {
                    authenticated = true;
                } else if (authenticated && "Conexão encerrada pelo cliente.".equals(finalResponse)) {
                    closeConnection();
                    break;
                }
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> displayArea.append("Erro na conexão com o servidor.\n"));
        }
    }

    private void selectClient(String client) {
        closeConnection();
        displayArea.setText("");
        authenticated = false;
        if ("Cliente 1".equals(client)) {
            startClient("1");
        } else if ("Cliente 2".equals(client)) {
            startClient("2");
        }
    }

    private void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        InterfaceCliente client = new InterfaceCliente();
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
        client.startClient("1");
    }
}
