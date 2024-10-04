package server;

import controllers.ServerC;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Server instance;
    private ServerSocket serverSocket;
    private List<ServerWorker> connectedUsers; // Lista de clientes conectados
    private ServerC controller; // Referencia al controlador de la UI

    private Server() {
        connectedUsers = new ArrayList<>();
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public void setController(ServerC controller) {
        this.controller = controller;
    }

    public void makeSocket() throws IOException {
        serverSocket = new ServerSocket(12345); // Puerto del servidor
        while (true) {
            Socket clientSocket = serverSocket.accept();
            ServerWorker worker = new ServerWorker(this, clientSocket);
            connectedUsers.add(worker);
            worker.start();
        }
    }

    public void removeWorker(ServerWorker worker) {
        connectedUsers.remove(worker);
        broadcastUserList(); // Enviar lista actualizada a todos los usuarios
    }

    public void broadcastUserList() {
        List<String> userNames = new ArrayList<>();
        for (ServerWorker worker : connectedUsers) {
            userNames.add(worker.getClientName());
        }

        // Enviar la lista de usuarios al controlador para actualizar la UI
        if (controller != null) {
            controller.updateUserList(userNames);
        }

        // Enviar la lista de usuarios a todos los clientes conectados
        for (ServerWorker worker : connectedUsers) {
            worker.sendUserList(userNames);
        }
    }
}

class ServerWorker extends Thread {
    private Server server;
    private Socket clientSocket;
    private String clientName;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            writer.println("Ingrese su nombre: ");
            clientName = reader.readLine();
            server.broadcastUserList(); // Enviar la lista de usuarios conectados

            String message;
            while ((message = reader.readLine()) != null) {
                // Procesar mensajes
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.removeWorker(this); // Remover el trabajador cuando el cliente se desconecte
        }
    }

    public void sendUserList(List<String> userList) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(String.join(",", userList)); // Envía la lista de usuarios separados por comas
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
