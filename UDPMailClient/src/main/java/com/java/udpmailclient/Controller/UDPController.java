package com.java.udpmailclient.Controller;

import com.java.udpmailclient.Model.MailItem;
import com.java.udpmailclient.Model.MailItemPane;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UDPController {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private List<MailItem> mailItems = new ArrayList<>();
    private DatagramSocket socket;
    private InetAddress serverAddress;

    // MAIL BOX
    @FXML
    private VBox vbox;
    @FXML
    private Label lb_from_mail_box, lb_subject_mail_box, lb_date_mail_box;

    // CURRENT MAIL
    @FXML
    private Label lb_your_email;
    @FXML
    private Button btn_new;

    // TEMPLATE MAIL
    @FXML
    private Label lb_from_mail;
    @FXML
    private TextField txt_to_mail, txt_subject_mail;
    @FXML
    private TextArea txt_content_mail;
    @FXML
    private Button btn_send, btn_cancel;

    @FXML
    public void initialize() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_HOST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the thread to listen for server responses
        startListeningForResponses();

    }

    private void startListeningForResponses() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    while (true) {
                        byte[] responseBuffer = new byte[4096];  // Increased buffer size
                        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                        socket.receive(responsePacket);

                        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                        System.out.println("Server Response: " + response);

                        // Update the UI on the JavaFX Application Thread
                        updateUIWithResponse(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        // Start the task in a new thread
        Thread thread = new Thread(task);
        thread.setDaemon(true); // Daemon thread will automatically stop when the application exits
        thread.start();
    }

    // Method to update the UI based on the server's response
    private void updateUIWithResponse(String response) {
        // Ensure this is run on the JavaFX Application Thread
        Platform.runLater(() -> {
            String[] parts = response.split(":");
            if (parts.length < 2) {
                System.out.println("Invalid response: " + response);
                return;
            }

            if (response.startsWith("SEND_EMAIL")) {
                System.out.println("SEND_EMAIL response: " + response);
                String fromEmail = parts[1];
                String toEmail = parts[2];
                String subject = parts[3];
                String content = parts[4];
                String time = parts[5];


                MailItem mailItem = new MailItem(fromEmail, toEmail, subject, content, time);
                mailItems.add(mailItem);

                MailItemPane mailItemPane = new MailItemPane(this::onClickMailItem, mailItem);

                // Update the VBox with the new mail item
//                vbox.getChildren().clear();
                vbox.getChildren().add(mailItemPane);
                vbox.requestLayout();
            } else if (response.startsWith("EMAIL_SENT")) {
                showAlert("Success", "Email sent successfully");
                clearMail();
            } else if (response.startsWith("RECIPIENT_NOT_FOUND")) {
                showAlert("Error", "Recipient not found");
            } else if (response.startsWith("ERROR_SENDING_EMAIL")) {
                showAlert("Error", "Error sending email");
            } else if (response.startsWith("GET_EMAILS")) {
                System.out.println("GET_EMAILS response: " + response);
                String res = response.replace("GET_EMAILS:", "");
                List<MailItem> mailItems = parseMailResponse(res);
                vbox.getChildren().clear();
                vbox.requestLayout();
                for (MailItem mailItem : mailItems) {
                    MailItemPane mailItemPane = new MailItemPane(this::onClickMailItem, mailItem);
                    vbox.getChildren().add(mailItemPane);
                }
                vbox.requestLayout();
            } else {
                System.out.println("Unknown response: " + response);
            }
        });
    }

    // Handle click on a mail item
    private Void onClickMailItem(MailItemPane mailItemPane) {
        Platform.runLater(() -> {
            MailItem mailItem = mailItemPane.getMailItem();
            lb_from_mail.setText(mailItem.getFrom());
            txt_to_mail.setText(mailItem.getTo());
            txt_subject_mail.setText(mailItem.getSubject());
            txt_content_mail.setText(mailItem.getContent());
        });
        return null;
    }

    @FXML
    public void newMailAction() {
        lb_from_mail.setText(lb_your_email.getText());
        clearMail();
    }

    @FXML
    public void sendMailAction() {
        sendEmail();
    }

    @FXML
    public void cancelMailAction() {
        clearMail();
    }

    public void clearMail() {
        txt_to_mail.clear();
        txt_subject_mail.clear();
        txt_content_mail.clear();
    }

    public void setUserDetails(String method, String email) {
        if (method.equals("LOGIN")) {
           loginAccount(email);
            getEmails(email);
        } else if (method.equals("CREATE_ACCOUNT")) {
            createAccount(email);
        }
        lb_your_email.setText(email);
        lb_from_mail.setText(email);
    }

    private void createAccount(String accountName) {
        if (!accountName.isEmpty()) {
            String message = "CREATE_ACCOUNT:" + accountName;
            sendRequest(message);
        }
    }
    private void loginAccount(String accountName) {
        if (!accountName.isEmpty()) {
            String message = "LOGIN:" + accountName;
            sendRequest(message);
        }
    }

    private void sendEmail() {
        String fromEmail = lb_from_mail.getText().trim();
        String toEmail = txt_to_mail.getText().trim();
        String subject = txt_subject_mail.getText().trim();
        String emailContent = txt_content_mail.getText().trim();
        String time = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if (!fromEmail.isEmpty() && !toEmail.isEmpty() && !emailContent.isEmpty()) {
            String message = "SEND_EMAIL:" + fromEmail + ":" + toEmail + ":" + subject + ":" + emailContent + ":" + time;
            sendRequest(message);
        }
    }

    private void sendRequest(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void getEmails(String accountName) {
        String message = "GET_EMAILS:" + accountName;
        sendRequest(message);
    }

    @FXML
    public void goToLoginScreen() {
        Stage currentStage = (Stage) lb_your_email.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/java/udpmailclient/auth-view.fxml"));
        try {
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Email UDP");
            newStage.setScene(new Scene(root));
            currentStage.close();
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MailItem> parseMailResponse(String response) {
        List<MailItem> mailItems = new ArrayList<>();

        // Tách các email bằng dấu '|'
        String[] emailParts = response.split("\\|");

        // Duyệt qua từng email đã tách được
        for (String emailData : emailParts) {
            // Tách các trường của từng email bằng dấu ':'
            String[] fields = emailData.split(":");

            if (fields.length == 5) { // Đảm bảo đủ 5 trường
                String fromEmail = fields[0];  // Người gửi
                String toEmail = fields[1];    // Người nhận
                String subject = fields[2];    // Chủ đề
                String content = fields[3];    // Nội dung
                String time = fields[4];       // Thời gian

                // Tạo một MailItem và thêm vào danh sách
                MailItem mailItem = new MailItem(fromEmail, toEmail, subject, content, time);
                mailItems.add(mailItem);
            } else {
                System.out.println("Invalid email format: " + emailData);
            }
        }

        return mailItems;
    }

}
