package server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {
    private Server server;
    private Socket clientSocket;
    private String clientName;
    private List<ServerWorker> clients;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private PrintWriter out;

    public ServerWorker(Server server, Socket clientSocket, List<ServerWorker> clients) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clients = clients;
        try {
            this.dataInputStream = new DataInputStream(clientSocket.getInputStream());
            this.dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            // Inicializa el PrintWriter aquí para enviar mensajes al cliente
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Enviar un mensaje de bienvenida pidiendo el nombre
            out.println("Ingrese su nombre: ");

            clientName = reader.readLine();
            setClientName(clientName);
            System.out.println("Nombre del cliente establecido: " + clientName);


            server.broadcastUserList(); // Asegúrate de que esto se llama después de establecer el nombre

            String message;
            // Escuchar mensajes del cliente
            while ((message = reader.readLine()) != null) {
                handleMessage(message); // Manejar el mensaje recibido
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.removeWorker(this); // Remover el trabajador cuando el cliente se desconecte
        }
    }



    public void sendUserList(List<String> userList) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(String.join(",", userList));
            System.out.println("Lista de usuarios enviada: " + String.join(",", userList)); // Para depuración
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleMessage(String msg) throws IOException {
        String[] parts = msg.split("-", 4);
        if (parts.length == 4 && parts[0].equals("private")) {
            String sender = parts[1]; // Emisor
            String recipient = parts[2]; // Receptor
            String message = parts[3]; // Mensaje

            // Llama a sendPrivateMessage para manejar el envío
            sendPrivateMessage(sender, recipient, message);
        } else {
            // Mensaje público
            for (ServerWorker clientHandler : clients) {
                clientHandler.out.println(msg);
            }
        }
    }

    private void sendPrivateMessage(String sender, String recipient, String message) {
        // Buscar el destinatario y enviar el mensaje
        ServerWorker recipientHandler = findClientByName(recipient);
        if (recipientHandler != null) {
            recipientHandler.out.println("private-" + sender + ": " + message);
        } else {
            System.out.println("Destinatario no encontrado: " + recipient);
        }
    }



    // Método para establecer el nombre del cliente
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    private ServerWorker findClientByName(String name) {
        for (ServerWorker client : clients) {
            if (client.clientName.equals(name)) {
                return client;
            }
        }
        return null; // Si no se encuentra el cliente
    }
}
