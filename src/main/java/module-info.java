module com.example.sophmeth_proj {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ruclinic to javafx.fxml;
    exports ruclinic;
}