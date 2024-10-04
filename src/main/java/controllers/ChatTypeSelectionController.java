package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ChatTypeSelectionController {
    private String clientName;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    private Button grupal;
    @FXML
    private Button privado;
    @FXML
    private ListView<String> onlineUsersList;

    // Lista de usuarios en línea (esto será dinámico, recibido desde el servidor)
    private ObservableList<String> onlineUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Permitir selección múltiple en la lista de usuarios
        onlineUsersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Aquí puedes iniciar la solicitud para obtener la lista de usuarios conectados
        // Por ejemplo, puedes enviar un mensaje al servidor para obtener la lista de usuarios.
    }

    public void setSocket(Socket socket, PrintWriter out, BufferedReader in) {
        this.socket = socket;
        this.out = out;
        this.in = in;

        // Iniciar un hilo para recibir la lista de usuarios
        startReceivingMessages();
    }

    // Método para iniciar el hilo de recepción de mensajes
    private void startReceivingMessages() {
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    // Aquí asumimos que el servidor envía la lista de usuarios conectados como una cadena separada por comas
                    if (serverMessage.contains(",")) {
                        String[] connectedUsers = serverMessage.split(","); // Asumiendo que el servidor envía una lista separada por comas
                        Platform.runLater(() -> updateOnlineUsers(connectedUsers));
                    } else {
                        // Otro tipo de mensaje recibido
                        System.out.println("Mensaje del servidor: " + serverMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Actualizar la lista de usuarios conectados en la UI
    private void updateOnlineUsers(String[] connectedUsers) {
        onlineUsers.clear();
        onlineUsers.addAll(connectedUsers);
        onlineUsersList.setItems(onlineUsers); // Actualiza el ListView
    }

    public void setClientName(String name) {
        clientName = name;
    }


    @FXML
    private void handleGroupChat() {
        // Obtener los usuarios seleccionados para el chat grupal
        List<String> selectedUsers = onlineUsersList.getSelectionModel().getSelectedItems();
        if (selectedUsers.size() > 1) {
            navigateToChat("../chat/chat.fxml", selectedUsers); // Pasa los usuarios seleccionados al chat grupal
        } else {
            showAlert("Debe seleccionar al menos dos usuarios para iniciar un chat grupal.");
        }
    }

    @FXML
    private void handlePrivateChat() {
        String selectedUser = onlineUsersList.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            navigateToChat("../chat/chat.fxml", List.of(selectedUser)); // Pasa el usuario seleccionado al chat privado
        } else {
            showAlert("Debe seleccionar un usuario para iniciar un chat privado.");
        }
    }

    private void navigateToChat(String fxmlFile, List<String> participants) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene chatScene = new Scene(loader.load());

            // Aquí puedes pasar los participantes al controlador de la vista de chat, si es necesario
            ChatController chatController = loader.getController();
            chatController.setParticipants(participants);

            Stage stage = (Stage) grupal.getScene().getWindow(); // Obtener la ventana actual
            stage.setScene(chatScene); // Cambiar a la vista de chat
            stage.show(); // Mostrar la nueva escena
        } catch (IOException e) {
            showAlert("Error al cambiar a la vista de chat.");
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Selección de chat");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
