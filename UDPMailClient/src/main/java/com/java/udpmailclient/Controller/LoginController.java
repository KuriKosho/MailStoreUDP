package com.java.udpmailclient.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField txt_mail;

    @FXML
    public void loginAction() {
        String email = txt_mail.getText();
        if (checkEmail(email)) {
            goToMainScreen("LOGIN");
        } else {
            showAlert("Error", "Email is invalid");
        }
    }
    @FXML
    public void registerAction() {
        String email = txt_mail.getText();
        if (checkEmail(email)) {
            goToMainScreen("CREATE_ACCOUNT");
        } else {
            showAlert("Error", "Email is invalid");
        }
    }

    public boolean checkEmail(String email) {
        String regex = "^(.+)@(.+)$";
        if (email.matches(regex)) {
            return true;
        } else {
            return false;
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void goToMainScreen(String method) {
        Stage currentStage = (Stage) txt_mail.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/java/udpmailclient/dashboard-view.fxml"));
        try {
            Parent root = loader.load();
            UDPController secondController = loader.getController();
            secondController.setUserDetails(method,txt_mail.getText());
            Stage newStage = new Stage();
            newStage.setTitle("Email UDP");
            newStage.setScene(new Scene(root));
            currentStage.close();
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
