package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginC {
    public TextField usuario;

    public void initialize() {
        // Inicialización si es necesario
    }

    public void logInButtonOnAction(ActionEvent actionEvent) throws IOException {
        String username = usuario.getText().trim();

        if (!username.isEmpty() && username.matches("[A-Za-z0-9]+")) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../chat/Client.fxml"));
            Stage primaryStage = new Stage();

            ClientC controller = new ClientC();
            controller.setClientName(username);
            fxmlLoader.setController(controller);

            primaryStage.setScene(new Scene(fxmlLoader.load()));
            primaryStage.setTitle(username);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.setOnCloseRequest(windowEvent -> controller.shutdown());
            primaryStage.show();

            usuario.clear();
        } else {
            new Alert(Alert.AlertType.ERROR, "Please enter your name").show();
        }
    }
}

