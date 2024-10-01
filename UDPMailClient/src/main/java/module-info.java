module com.java.udpmailclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.java.udpmailclient to javafx.fxml;
    exports com.java.udpmailclient;
    exports com.java.udpmailclient.Controller;
    opens com.java.udpmailclient.Controller to javafx.fxml;
}