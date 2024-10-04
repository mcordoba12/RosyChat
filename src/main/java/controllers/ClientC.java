package controllers;

import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List; // Asegúrate de que esta línea esté presente y sea de java.util


public class ClientC {
    public AnchorPane pane;
    public ScrollPane scrollP;
    public VBox box;
    public TextField txtMsg;
    public Text txtLabel;

    private boolean isPrivateChat;
    private String selectedRecipient;
    private ComboBox<String> recipientComboBox;



    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName = "Client";


    private List<String> connectedUserNames = new ArrayList<>();

    // Método para agregar un nuevo usuario a la lista
    public void addUser(String username) {
        connectedUserNames.add(username);
        recipientComboBox.getItems().add(username); // Actualiza el ComboBox para reflejar el nuevo usuario
    }


    public void initialize() {
        setupScrollPane();
        txtLabel.setText(clientName);
        connectToServer();
        setupVBoxListener();
        connectedUserNames.add("Grupo"); // Agregar opción para chat grupal
        recipientComboBox.getItems().addAll(connectedUserNames);
    }

    private void setupScrollPane() {
        scrollP.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollP.setFitToWidth(true);
    }

    public void setClientName(String name) {
        clientName = name;
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 3001);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("setname-" + clientName); // Enviar nombre del cliente
                dataOutputStream.flush();

                System.out.println("Client logged in");
                ServerC.receiveMessage(clientName + " joined.");

                while (socket.isConnected()) {
                    String receivingMsg = dataInputStream.readUTF();
                    receiveMessage(receivingMsg, box);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupVBoxListener() {
        this.box.heightProperty().addListener((observableValue, oldValue, newValue) ->
                scrollP.setVvalue((Double) newValue)
        );
    }

    public void shutdown() {
        ServerC.receiveMessage(clientName + " left.");
    }

    public void txtMsgOnAction(ActionEvent actionEvent) {
        sendButtonOnAction(actionEvent);
    }

    public void sendButtonOnAction(ActionEvent actionEvent) {
        String msgToSend = txtMsg.getText();
        String recipient = recipientComboBox.getValue();

        // Si estás en un chat privado, asigna el destinatario
        if (isPrivateChat) { // Cambia esta condición según tu lógica
            recipient = selectedRecipient; // Asigna el destinatario seleccionado
        }

        sendMsg(msgToSend, recipient);
    }


    private void sendMsg(String msgToSend, String recipient) {
        if (!msgToSend.isEmpty()) {
            HBox hBox = createMessageHBox(msgToSend);
            box.getChildren().addAll(hBox, createTimeHBox());

            try {
                if (recipient != null && !recipient.equals("Grupo")) { // Mensaje privado
                    dataOutputStream.writeUTF("private-" + recipient + "-" + msgToSend);
                } else { // Mensaje grupal
                    dataOutputStream.writeUTF("group-" + msgToSend);
                }
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            txtMsg.clear();
        }
    }



    private HBox createMessageHBox(String msgToSend) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 0, 10));

        Text text = new Text(msgToSend);
        text.setStyle("-fx-font-size: 14");
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #ff7f7f; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);");
        textFlow.setPadding(new Insets(10, 15, 10, 15));
        text.setFill(Color.WHITE);

        hBox.getChildren().add(textFlow);
        return hBox;
    }

    private HBox createTimeHBox() {
        HBox hBoxTime = new HBox();
        hBoxTime.setAlignment(Pos.CENTER_RIGHT);
        hBoxTime.setPadding(new Insets(0, 5, 5, 10));

        String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Text time = new Text(stringTime);
        time.setStyle("-fx-font-size: 8; -fx-text-fill: gray;");

        hBoxTime.getChildren().add(time);
        return hBoxTime;
    }

    private void sendImage(String msgToSend) {
        Image image = new Image(msgToSend);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 5, 5, 10));
        hBox.getChildren().add(imageView);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(hBox);

        try {
            dataOutputStream.writeUTF(clientName + "-" + msgToSend);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveMessage(String msg, VBox vBox) throws IOException {
        Platform.runLater(() -> {
            if (msg.matches(".*\\.(png|jpe?g|gif)$")) {
                displayImageMessage(msg, vBox);
            } else {
                displayTextMessage(msg, vBox);
            }
        });
    }

    private static void displayImageMessage(String msg, VBox vBox) {
        String[] parts = msg.split("[-]");
        String name = parts[0];
        String imageUrl = parts[1];

        HBox hBoxName = createNameHBox(name);
        Image image = new Image(imageUrl);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));
        hBox.getChildren().add(imageView);

        vBox.getChildren().addAll(hBoxName, hBox);
    }

    private static void displayTextMessage(String msg, VBox vBox) {
        String[] parts = msg.split("-");
        String name = parts[0];
        String msgFromServer = parts[1];

        HBox hBoxName = createNameHBox(name);
        HBox hBox = createTextMessageHBox(msgFromServer);

        vBox.getChildren().addAll(hBoxName, hBox);
    }

    private static HBox createNameHBox(String name) {
        HBox hBoxName = new HBox();
        hBoxName.setAlignment(Pos.CENTER_LEFT);
        Text textName = new Text(name);
        textName.setStyle("-fx-font-weight: bold; -fx-fill: #333; -fx-font-size: 12;");
        TextFlow textFlowName = new TextFlow(textName);
        hBoxName.getChildren().add(textFlowName);
        return hBoxName;
    }

    private static HBox createTextMessageHBox(String msgFromServer) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(msgFromServer);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #db9ca8; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);");
        textFlow.setPadding(new Insets(10, 15, 10, 15));
        text.setFill(Color.BLACK);

        hBox.getChildren().add(textFlow);
        return hBox;
    }

}
