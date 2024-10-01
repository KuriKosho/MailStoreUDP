package com.java.udpmailclient.Model;


import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Callback;


public class MailItemPane extends Pane {

    private CheckBox pickMail;
    private Label mailTitle;
    private Label mailTime;
    private Label userEmail;

    private MailItem mailItem;
    // Callback to handle mail selection
    private Callback<MailItemPane, Void> onMailSelect;

    public MailItemPane(Callback<MailItemPane, Void> onMailSelect, MailItem mailItem) {
        this.onMailSelect = onMailSelect;
        this.mailItem = mailItem;

        // Set up the Pane with fixed size
        double fixedWidth = 202.0;
        double fixedHeight = 55.0;

        this.setPrefSize(fixedWidth, fixedHeight);
        this.setMinSize(fixedWidth, fixedHeight); // Fixed minimum size
        this.setMaxSize(fixedWidth, fixedHeight); // Fixed maximum size
        this.setStyle("-fx-border-color: #D6D5D5;");

        // Create the CheckBox
        pickMail = new CheckBox();
        pickMail.setLayoutX(6.0);
        pickMail.setLayoutY(15.0);
        pickMail.setPrefSize(10.0, 3.0);

        // Create the Mail Title Label
        mailTitle = new Label(mailItem.getFrom());
        mailTitle.setLayoutX(34.0);
        mailTitle.setLayoutY(6.0);
        mailTitle.setPrefSize(135.0, 17.0);
        mailTitle.setFont(Font.font("System Bold", 12.0));

        // Create the Mail Time Label
        mailTime = new Label(mailItem.getSubject());
        mailTime.setLayoutX(34.0);
        mailTime.setLayoutY(29.0);
        mailTime.setPrefSize(34.0, 17.0);

        // Create the User Email Label
        userEmail = new Label(mailItem.getTime());
        userEmail.setLayoutX(68.0);
        userEmail.setLayoutY(29.0);
        userEmail.setPrefSize(126.0, 17.0);
        userEmail.setFont(Font.font("System Bold", 12.0));

        // Add children to the Pane
        this.getChildren().addAll(pickMail, mailTitle, mailTime, userEmail);

        // Add event handler for the Pane click, invoke the callback
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println("Mail item clicked!");
            if (onMailSelect != null) {
                System.out.println("Callback onMailSelect is called!");
                onMailSelect.call(this);  // Call the callback method
            }
        });
    }

    public MailItem getMailItem() {
        return mailItem;
    }

    public void setMailItem(MailItem mailItem) {
        this.mailItem = mailItem;
    }

    public CheckBox getPickMail() {
        return pickMail;
    }

    public void setPickMail(CheckBox pickMail) {
        this.pickMail = pickMail;
    }

    public Label getMailTitle() {
        return mailTitle;
    }

    public void setMailTitle(Label mailTitle) {
        this.mailTitle = mailTitle;
    }

    public Label getMailTime() {
        return mailTime;
    }

    public void setMailTime(Label mailTime) {
        this.mailTime = mailTime;
    }

    public Label getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(Label userEmail) {
        this.userEmail = userEmail;
    }

    public Callback<MailItemPane, Void> getOnMailSelect() {
        return onMailSelect;
    }

    public void setOnMailSelect(Callback<MailItemPane, Void> onMailSelect) {
        this.onMailSelect = onMailSelect;
    }

}
