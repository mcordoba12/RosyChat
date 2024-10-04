package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class ChatController {
    @FXML
    private ListView<String> participantsList; // Para mostrar los participantes en el chat
    @FXML
    private TextArea chatArea; // Para mostrar el historial del chat
    @FXML
    private TextField messageInput; // Para escribir nuevos mensajes

    private List<String> participants; // Lista de participantes del chat

    // Método para establecer los participantes del chat
    public void setParticipants(List<String> participants) {
        this.participants = participants;
        updateParticipantsList();
    }

    // Actualiza la lista de participantes en la interfaz
    private void updateParticipantsList() {
        participantsList.getItems().clear();
        participantsList.getItems().addAll(participants); // Agregar los participantes a la lista
    }

    // Método para enviar un mensaje (puedes expandirlo según tus necesidades)
    @FXML
    private void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            // Aquí debes agregar la lógica para enviar el mensaje a través de la red
            // Por ejemplo, enviar el mensaje a los participantes del chat
            chatArea.appendText("Yo: " + message + "\n"); // Agrega el mensaje al área de chat
            messageInput.clear(); // Limpiar el campo de entrada
        }
    }
}
