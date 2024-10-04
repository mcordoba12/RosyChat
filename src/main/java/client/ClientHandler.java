package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientHandler {
    private Socket socket;
    private List<ClientHandler> clients;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        try {
            this.socket = socket;
            this.clients = clients;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            // Puedes leer el nombre del cliente aquí

            new Thread(this::run).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        try {
            // Esperar a que el cliente envíe su nombre
            String initialMsg = dataInputStream.readUTF();
            if (initialMsg.startsWith("setname-")) {
                clientName = initialMsg.substring(8); // Extraer el nombre
            }

            while (socket.isConnected()) {
                String msg = dataInputStream.readUTF();
                handleMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleMessage(String msg) throws IOException {
        String[] parts = msg.split("-");
        if (parts.length == 3 && parts[0].equals("private")) {
            sendPrivateMessage(parts[1], parts[2]);
        } else {
            // Manejar mensajes públicos
            for (ClientHandler clientHandler : clients) {
                clientHandler.dataOutputStream.writeUTF(msg);
            }
        }
    }

    private void sendPrivateMessage(String recipient, String message) throws IOException {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.clientName.equals(recipient)) {
                clientHandler.dataOutputStream.writeUTF("Privado de " + clientName + ": " + message);
                clientHandler.dataOutputStream.flush();
                break;
            }
        }
    }

    // Método para establecer el nombre del cliente
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
