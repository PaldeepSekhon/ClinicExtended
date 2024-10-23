module com.example.sophmeth_proj {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sophmeth_proj3 to javafx.fxml;
    exports com.example.sophmeth_proj3;
}