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

    private Map<String, List<String>> chatSessions = new HashMap<>(); // Almacena sesiones de chat

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
            ServerWorker worker = new ServerWorker(this, clientSocket, connectedUsers);
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

    public void updateChatSession(String user, List<String> chatUsers) {
        chatSessions.put(user, chatUsers);
        broadcastChatStatus(user, chatUsers);
    }

    private void broadcastChatStatus(String user, List<String> chatUsers) {
        StringBuilder message = new StringBuilder(" está en chat con: ");
        for (String chatUser : chatUsers) {
            message.append(chatUser).append(", ");
        }
        // Eliminar la última coma y espacio
        if (message.length() > 0) {
            message.setLength(message.length() - 2);
        }
        // Mostrar en la interfaz del servidor
        controller.displayChatStatus(message.toString());
    }

    public void startPrivateChat(String sender, String recipient) {
        List<String> chatUsers = Arrays.asList(sender, recipient);
        updateChatSession(sender, chatUsers);
        sendParticipantList(); // Asegúrate de implementar este método
    }

    public void startGroupChat(String initiator, List<String> participants) {
        List<String> chatUsers = new ArrayList<>(participants); // Hacer una copia
        chatUsers.add(initiator); // Agregar al iniciador
        updateChatSession(initiator, chatUsers);
        sendParticipantList(); // Asegúrate de implementar este método
    }

    private void sendParticipantList() {
        List<String> userNames = getConnectedUserNames();
        for (ServerWorker worker : connectedUsers) {
            worker.sendUserList(userNames); // Envía la lista a cada trabajador
        }
    }

    private List<String> getConnectedUserNames() {
        List<String> userNames = new ArrayList<>();
        for (ServerWorker worker : connectedUsers) {
            userNames.add(worker.getClientName());
        }
        return userNames;
    }
}

