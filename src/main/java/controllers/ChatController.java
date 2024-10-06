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
            // Para enviar mensajes al servidor

        @FXML
        public void initialize() {
            // Permitir selección única
            participantsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }


        public void setParticipants(List<String> participants) {
            this.participants = participants;
            updateParticipantsList();
        }

        private void updateParticipantsList() {
            participantsList.getItems().clear();
            participantsList.getItems().addAll(participants);
            System.out.println("Participantes actualizados: " + participantsList.getItems());
        }


        public void setInput(BufferedReader in) {
            this.in = in;
            startListening(); // Asegúrate de llamar a startListening aquí
        }

        public void setOutput(PrintWriter out) {
            this.out = out;
        }


        @FXML
        private void sendMessage() {
            System.out.println("Sending message...");

            String message = messageInput.getText();
            System.out.println("Mensaje: " + message); // Verificar contenido del mensaje
            System.out.println("out es null: " + (out == null)); // Verificar si out es null

            if (!message.isEmpty() && out != null) {
                String selectedParticipant = participantsList.getSelectionModel().getSelectedItem();
                System.out.println("Participante seleccionado: " + selectedParticipant); // Verificar selección

                if (selectedParticipant != null) {
                    // Enviar mensaje privado
                    System.out.println("private-" + selectedParticipant + "-" + message);
                    out.println("private-" + selectedParticipant + "-" + message);
                } else {
                    // Enviar mensaje público
                    out.println(message);
                }
                chatArea.appendText("Yo: " + message + "\n");
                messageInput.clear();
            }
        }


        public void receiveMessage(String message) {
            System.out.println("Received message: " + message); // Para depuración
            Platform.runLater(() -> chatArea.appendText(message + "\n")); // Asegúrate de que `chatArea` esté inicializado
        }


        public void startListening() {
            System.out.println("Listening..."); // Para depuración
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Message received from server: " + message); // Agrega esto para verificar qué se recibe
                        receiveMessage(message); // Asegúrate de que este método se llame
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }






    }
