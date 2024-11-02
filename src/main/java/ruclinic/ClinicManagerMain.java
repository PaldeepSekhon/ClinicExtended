package ruclinic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
/**
 * The ClinicManagerMain class serves as the entry point for the RUClinic Appointment Manager
 * application. It initializes the JavaFX application, loads the FXML layout, and sets up the main
 * application stage with the designated title and scene.
 *
 * This class launches the GUI for scheduling, canceling, and rescheduling appointments within
 * the clinic.
 *
 * @author Paldeep Sekhon
 * @author Aditya Ponni
 */
public class ClinicManagerMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClinicManagerMain.class.getResource("clinic-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setTitle("RUClinic Appointment Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}