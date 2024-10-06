package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ChatController {
    @FXML
    private ListView<String> participantsList;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageInput;

    private List<String> participants;
    private PrintWriter out;
    private BufferedReader in; // Para recibir mensajes del servidor
    private String clientName; // Almacena el nombre del cliente
    private boolean listeningStarted = false; // Controla si ya se ha iniciado la escucha

    @FXML
    public void initialize() {
        // Permitir selección única
        System.out.println("Inicializando el controlador de chat.");
        participantsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
        System.out.println("Participantes establecidos: " + participants);
        updateParticipantsList();
    }



    private void updateParticipantsList() {
        Platform.runLater(() -> {
            participantsList.getItems().clear();

            // Agregar solo los participantes que no son el cliente
            for (String participant : participants) {
                if (!participant.equals(clientName)) {
                    participantsList.getItems().add(participant);
                }
            }

            System.out.println("Participantes actualizados: " + participantsList.getItems());

            // Seleccionar el primer participante que no sea el cliente, si hay participantes
            if (!participantsList.getItems().isEmpty()) {
                participantsList.getSelectionModel().select(0);
            } else {
                // Si no hay participantes, deseleccionar cualquier selección previa
                participantsList.getSelectionModel().clearSelection();
            }
        });
    }




    public void setOutput(PrintWriter out) {
        this.out = out;
        System.out.println("PrintWriter establecido: " + (out != null));
    }

    public void setInput(BufferedReader in) {
        this.in = in;
        System.out.println("BufferedReader establecido: " + (in != null));
        if (!listeningStarted) {
            startListening(); // Asegúrate de llamar a startListening solo una vez
            listeningStarted = true;
        }
    }

    @FXML
    private void sendMessage() {
        System.out.println("Intentando enviar mensaje...");

        String message = messageInput.getText();

        if (message.isEmpty()) {
            System.out.println("El mensaje está vacío.");
            return;
        }

        // Obtener el participante seleccionado
        String selectedParticipant = participantsList.getSelectionModel().getSelectedItem();
        System.out.println("Participante seleccionado: " + selectedParticipant); // Línea de depuración

        if (selectedParticipant != null) {
            // Verificar que no se envíe un mensaje a sí mismo
            if (!selectedParticipant.equals(clientName)) {
                // Enviar mensaje privado
                System.out.println("Enviando mensaje privado a: " + selectedParticipant);
                out.println("private-" + selectedParticipant + "-" + message);
                chatArea.appendText("Yo (a " + selectedParticipant + "): " + message + "\n");
            } else {
                System.out.println("No se puede enviar un mensaje a sí mismo.");
                chatArea.appendText("No se puede enviar un mensaje a sí mismo.\n");
            }
        } else {
            // Enviar mensaje público
            System.out.println("No hay participante seleccionado, enviando mensaje público...");
            out.println("public-" + message);
            chatArea.appendText("Yo (público): " + message + "\n");
        }

        messageInput.clear();
    }



    public void receiveMessage(String message) {
        System.out.println("Received message: " + message); // Para depuración
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
        });
    }

    public void startListening() {
        System.out.println("Listening..."); // Para depuración
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Message received from server: " + message); // Verificar qué se recibe
                    receiveMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


}
