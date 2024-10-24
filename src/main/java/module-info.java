module com.example.sophmeth_proj {
    requires javafx.controls;
    requires javafx.fxml;


    opens ruclinic to javafx.fxml;
    exports ruclinic;
}