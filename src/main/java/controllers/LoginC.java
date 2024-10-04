package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginC {
    public TextField usuario;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void initialize() {
        // Inicialización si es necesario
    }

    public void logInButtonOnAction(ActionEvent actionEvent) throws IOException {
        String username = usuario.getText().trim();

        if (!username.isEmpty() && username.matches("[A-Za-z0-9]+")) {
            // Conectar al servidor
            try {
                socket = new Socket("localhost", 12345); // Cambia esto al host y puerto de tu servidor
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Envía el nombre de usuario al servidor
                out.println(username);

                // Carga la selección de tipo de chat
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../chat/ChatTypeSelection.fxml"));
                Stage selectionStage = new Stage();
                selectionStage.setScene(new Scene(fxmlLoader.load()));
                selectionStage.setTitle("Seleccionar Tipo de Chat");
                selectionStage.setResizable(false);
                selectionStage.centerOnScreen();
                selectionStage.show();

                // Obtén el controlador correcto
                ChatTypeSelectionController controller = fxmlLoader.getController();
                controller.setClientName(username);
                controller.setSocket(socket, out, in); // Pasa los sockets al controlador

                usuario.clear();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "No se pudo conectar al servidor").show();
                e.printStackTrace();
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Por favor, ingresa tu nombre").show();
        }
    }
}
