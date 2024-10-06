package controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServerC {

    public VBox box;
    public ScrollPane scrollS;
    public AnchorPane pane;

    private server.Server server;
    private static VBox staticVBox;

    private List<ObjectOutputStream> clientOutputStreams; // Lista para manejar los streams de los clientes


    public void initialize() {

        // Configurar el ScrollPane
        scrollS.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollS.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollS.setFitToWidth(true); // Asegúrate de que el contenido se ajuste al ancho

        staticVBox = box;
        receiveMessage("Server Starting..");
        box.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollS.setVvalue((Double) newValue);
            }
        });

        new Thread(() -> {
            try {
                server = Server.getInstance();
                server.setController(this); // Aseguramos que el servidor tenga referencia al controlador
                server.makeSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        receiveMessage("Server Running..");
        receiveMessage("Waiting for Users..");
    }



    public void receiveMessage(String msgFromClient) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(msgFromClient);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #ffa3c4; -fx-font-weight: bold; -fx-background-radius: 20px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(0, 0, 0));

        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> staticVBox.getChildren().add(hBox));
    }


    // Método para actualizar la lista de usuarios conectados
    public void updateUserList(List<String> users) {
        Platform.runLater(() -> {
            box.getChildren().clear(); // Limpiar la caja antes de actualizar la lista de usuarios
            for (String user : users) {
                receiveMessage(user + " está conectado.");
            }
        });
    }

    public void addButtonOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(pane.getScene().getWindow());
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../chat/Login.fxml"))));
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "error cannot add customer").show();
        }
        stage.setTitle("RosyChat");
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.show();
    }

    public void displayMessage(String from, String to, String message) {
        String logMessage = from + " -> " + to + ": " + message; // Formato del mensaje
        receiveMessage(logMessage); // Muestra el mensaje en la UI
    }

    public void displayChatStatus(String chatStatus) {
        receiveMessage(chatStatus); // Muestra el estado del chat en la UI
    }


}
