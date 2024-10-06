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

            // Leer el nombre del cliente
            clientName = reader.readLine();
            server.broadcastUserList(); // Enviar la lista de usuarios conectados

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
            writer.println(String.join(",", userList)); // Envía la lista de usuarios separados por comas
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String msg) throws IOException {
        String[] parts = msg.split("-", 4); // Cambia a 4 para que coincida con sender, recipient, y message
        if (parts.length == 4 && parts[0].equals("private")) {
            String sender = parts[1]; // Suponiendo que `parts[1]` es el emisor
            String recipient = parts[2]; // Suponiendo que `parts[2]` es el receptor
            String message = parts[3]; // Suponiendo que `parts[3]` es el mensaje

            // Enviar el mensaje privado
            sendPrivateMessage(sender, recipient, message);
        } else {
            // Mensaje público
            for (ServerWorker clientHandler : clients) {
                System.out.println("Sending message to: " + clientHandler.clientName); // Verifica a quién se envía el mensaje
                clientHandler.out.println(msg);
            }
        }
    }




    private void sendPrivateMessage(String sender, String recipient, String message) throws IOException {
        // Verificar que el destinatario no sea el mismo que el emisor
        if (sender.equals(recipient)) {
            System.out.println("No se puede enviar un mensaje a sí mismo.");
            return; // Salir si son la misma persona
        }

        // Buscar el destinatario y enviar el mensaje
        for (ServerWorker clientHandler : clients) {
            if (clientHandler.clientName.equals(recipient)) {
                clientHandler.out.println("Privado de " + sender + ": " + message);
                break; // Salir después de enviar el mensaje
            }
        }
    }






    // Método para establecer el nombre del cliente
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
