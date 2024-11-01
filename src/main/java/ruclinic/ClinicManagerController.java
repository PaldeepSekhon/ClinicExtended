package ruclinic;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import util.Date;
import util.Timeslot;

import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.Scanner;

import java.time.LocalDate;
import java.util.Calendar;
import java.io.File;
import javafx.stage.FileChooser.ExtensionFilter;



public class ClinicManagerController{

    public TableColumn list_appointmentDateColumn;
    public TableColumn list_providerNameColumn;
    public TableColumn list_appointmentTimeColumn;
    public TableColumn list_countyColumn;
    public TableView appointmentListTable;
    public ListView sortOrderListView;
    public ComboBox sortOptionsComboBox;
    public ComboBox appointmentTypeComboBox;
    public TableColumn zipColumn;
    public TableColumn countyColumn;
    public TableColumn cityColumn;
    public TableView clinicLocationsTable;
    public ComboBox newTimeSlotComboBox;
    public TextField rescheduleFirstNameField;
    public TextField rescheduleLastNameField;
    public DatePicker rescheduleDOBField;
    public ComboBox oldTimeSlotComboBox;
    public DatePicker originalDateField;
    private util.List<Appointment> appointments; // List to hold all appointments
    private util.List<Provider> providers; // Single list for all providers
    private CircularLinkedList technicianList;

    @FXML private Button loadProviderButton;

    // Patient Information Fields
    @FXML private TextField patientFirstNameField;
    @FXML private TextField patientLastNameField;
    @FXML private DatePicker dobField;
    @FXML private TextField searchField;

    // Appointment Details Fields
    @FXML private DatePicker appointmentDateField;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private RadioButton officeVisitRadio;
    @FXML private RadioButton imagingRadio;
    @FXML private ToggleGroup appointmentTypeGroup;

    // Office Visit and Imaging Sections
    @FXML private VBox officeVisitBox;
    @FXML private ComboBox<String> providerNPIComboBox;

    @FXML private VBox imagingBox;
    @FXML private ComboBox<String> imagingTypeComboBox;


    // Reports Fields
    @FXML private DatePicker reportStartDate;
    @FXML private DatePicker reportEndDate;
    @FXML private TableView<?> reportsTable;

    // Provider Management
    @FXML private TextField providerSearchField;
    @FXML private ComboBox<String> providerFilterComboBox;
    @FXML private TableView<?> providerTable;

    // Status Areas
    @FXML private TextArea statusMessageArea;
    @FXML private TextArea providerStatusArea;

    @FXML private HBox loadProviders;
    @FXML private HBox cancelAppointment;
    @FXML private HBox reschedule;
    @FXML private HBox scheduleAppointment;

    private String selectedProviderNPI;
    private String selectedTimeSlot;

    ObservableList<String> timeSlots = FXCollections.observableArrayList(
            "9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM"
    );

    public void initialize() {

        timeSlotComboBox.setItems(FXCollections.observableArrayList(timeSlots));

        imagingTypeComboBox.setItems(FXCollections.observableArrayList("X-Ray", "Ultrasound", "CT Scan"));

        // Toggle visibility of office visit and imaging sections based on selection
        appointmentTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == officeVisitRadio) {
                showOfficeVisitFields();
            } else {
                showImagingFields();
            }
        });

        this.appointments = new util.List<>(); // Custom List for appointments
        this.providers = new util.List<>(); // Single Custom List for all providers
        this.technicianList = new CircularLinkedList();

    }

    @FXML
    private void handleLoadProviderButton(ActionEvent event) {
        // Code to load provider information
       // System.out.println("Loading provider information...");

        // For example, you could fetch provider data from a database or file
        // Here, add your logic to populate the Appointment Management tab with provider details
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Source File for the Import");
        chooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("All Files", "*.*"));
        Stage stage = new Stage();

        //read
        try {
            File file = chooser.showOpenDialog(stage);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                    continue; // Ignore empty lines

                String[] tokens = line.split("\\s+"); // Split by whitespace

                if (tokens[0].equalsIgnoreCase("D")) {
                    // Doctor
                    String firstName = tokens[1];
                    String lastName = tokens[2];
                    String dob = tokens[3]; // format: MM/DD/YYYY
                    Location location = Location.valueOf(tokens[4].toUpperCase()); // Using the enum
                    Specialty specialty = Specialty.valueOf(tokens[5].toUpperCase()); // Get specialty enum
                    String npi = tokens[6];

                    // Parse the date from the string
                    String[] dobParts = dob.split("/");
                    int month = Integer.parseInt(dobParts[0]);
                    int day = Integer.parseInt(dobParts[1]);
                    int year = Integer.parseInt(dobParts[2]);

                    Profile profile = new Profile(firstName, lastName, new Date(year, month, day));
                    // Create Doctor instance using the Specialty enum
                    Provider doctor = new Doctor(profile, location, specialty, npi);
                    providers.add(doctor); // Add to the list of providers

                } else if (tokens[0].equalsIgnoreCase("T")) {
                    // Technician
                    String firstName = tokens[1];
                    String lastName = tokens[2];
                    String dob = tokens[3]; // format: MM/DD/YYYY
                    Location location = Location.valueOf(tokens[4].toUpperCase()); // Using the enum
                    int ratePerVisit = Integer.parseInt(tokens[5]);

                    // Parse the date from the string
                    String[] dobParts = dob.split("/");
                    int month = Integer.parseInt(dobParts[0]);
                    int day = Integer.parseInt(dobParts[1]);
                    int year = Integer.parseInt(dobParts[2]);

                    Profile profile = new Profile(firstName, lastName, new Date(year, month, day));
                    Technician technician = new Technician(profile, location, ratePerVisit); // Create Technician
                    // instance
                    providers.add(technician); // Add to the list of providers
                    technicianList.addTechnician(technician);
                }
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            statusMessageArea.appendText("Error: providers.txt not found.");
        } catch (Exception e) {
            statusMessageArea.appendText("Error loading providers: " + e.getMessage());
        }

        //print
        ObservableList<String> providerOptions = FXCollections.observableArrayList();
        Sort.provider(providers); // Sort by provider profile

        for (Provider provider : providers) {
            String providerDetails = String.format("[%s %s %s, %s, %s %s]",
                    provider.getProfile().getFirstName(),
                    provider.getProfile().getLastName(),
                    provider.getProfile().getDob(),
                    provider.getLocation().getCity(),
                    provider.getLocation().getCounty(),
                    provider.getLocation().getZip());

            if (provider instanceof Doctor) {
                Doctor doctor = (Doctor) provider;
                // Display doctor's specialty and NPI, using the testing methods
                providerDetails += String.format("[%s, #%s]",
                        doctor.getSpecialty().getNameOnly(),
                        doctor.getNpi());
            } else if (provider instanceof Technician) {
                Technician technician = (Technician) provider;
                // Display technician's rate per visit formatted to two decimal places
                providerDetails += String.format("[rate: $%.2f]", (double) technician.rate());
            }

            statusMessageArea.appendText(providerDetails);
            providerOptions.add(providerDetails);

        }
        statusMessageArea.appendText("\n");
        providerNPIComboBox.setItems(providerOptions);

    }




    // Show Office Visit Fields
    private void showOfficeVisitFields() {
        officeVisitBox.setVisible(true);
        officeVisitBox.setManaged(true);
        imagingBox.setVisible(false);
        imagingBox.setManaged(false);
    }

    private void showImagingFields() {
        imagingBox.setVisible(true);
        imagingBox.setManaged(true);
        officeVisitBox.setVisible(false);
        officeVisitBox.setManaged(false);
    }

    @FXML
    void importFile(ActionEvent event)  {


    }




    @FXML
    private void handleScheduleAppointment(ActionEvent event) {
        String firstName = patientFirstNameField.getText();
        String lastName = patientLastNameField.getText();
        LocalDate localAppointmentDate = appointmentDateField.getValue();
        Date appointmentDate = new Date(localAppointmentDate.getYear(), localAppointmentDate.getMonthValue(), localAppointmentDate.getDayOfMonth());
        String timeSlot = timeSlotComboBox.getValue();
        LocalDate dobLocalDate = dobField.getValue();
        Date dob = new Date(dobLocalDate.getYear(), dobLocalDate.getMonthValue(), dobLocalDate.getDayOfMonth());

        if (!appointmentDate.isValid()) {
            statusMessageArea.appendText("Appointment date: " + appointmentDate + " is not a valid calendar date.");
            return;
        }
        // Check if the appointment is today or before today
        Date today = new Date(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        if (appointmentDate.compareTo(today) <= 0) {
            statusMessageArea.appendText("Appointment date: " + appointmentDate + " is today or a date before today.");
            return;
        }

        // Check if the appointment is on a weekend
        if (appointmentDate.isWeekend()) {
            statusMessageArea.appendText("Appointment date: " + appointmentDate + " is Saturday or Sunday.");
            return;
        }

        // Check if the appointment is within six months
        if (!appointmentDate.isWithinSixMonths()) {
            statusMessageArea.appendText("Appointment date: " + appointmentDate + " is not within six months.");
            return;
        }
        timeSlotComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedTimeSlot = String.valueOf(timeSlots.indexOf(newValue) + 1); // Get the position as 1-based index

            }
        });

        Timeslot timeslot = Timeslot.fromString(selectedTimeSlot);


        Profile profile = new Profile(firstName, lastName, dob);
        Person patient = new Person(profile);

        for (Appointment appt : appointments) {
            if (appt.getPatient().getProfile().equals(profile) &&
                    appt.getDate().equals(appointmentDate) &&
                    appt.getTimeslot().toString().equals(selectedTimeSlot)) {
                System.out.println(firstName + " " + lastName + " " + dob.toString()
                        + " has an existing appointment at the same time slot.");
                return;
            }
        }


        providerNPIComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.contains("#")) { // Ensure there is an NPI in the format
                // Regex to capture the NPI number after '#'
                selectedProviderNPI = newValue.replaceAll(".*#(\\d{2}).*", "$1"); // Captures the NPI as a group


                // You can then use `npi` as needed, e.g., store it, display it, etc.
            }
        });

        statusMessageArea.appendText(selectedProviderNPI);

        Provider provider = findProviderByNPI(selectedProviderNPI);

        if (provider == null) {
            statusMessageArea.appendText(selectedProviderNPI + " - provider doesn't exist.");
            return;
        }


        if (!isProviderAvailable(provider, appointmentDate, timeslot)) {
            Doctor doctor = (Doctor) provider;
            statusMessageArea.appendText(String.format("[%s %s %s, %s, %s %s][%s, #%s] is not available at slot %s.",
                    doctor.getProfile().getFirstName(),
                    doctor.getProfile().getLastName(),
                    doctor.getProfile().getDob(),
                    doctor.getLocation().getCity(),
                    doctor.getLocation().getCounty(),
                    doctor.getLocation().getZip(),
                    doctor.getSpecialty().getNameOnly(),
                    doctor.getNpi(),
                    timeslot.toString()));
            return;
        }

        Appointment newAppointment = new Appointment(appointmentDate, timeslot, patient, provider);
        appointments.add(newAppointment);

        Doctor doctor = (Doctor) provider;
        statusMessageArea.appendText(String.format("%s %s %s %s %s [%s %s %s, %s, %s %s][%s, #%s] booked.%n",
                appointmentDate,
                timeslot,
                firstName,
                lastName,
                dob.toString(),
                doctor.getProfile().getFirstName(),
                doctor.getProfile().getLastName(),
                doctor.getProfile().getDob(),
                doctor.getLocation().getCity(),
                doctor.getLocation().getCounty(),
                doctor.getLocation().getZip(),
                doctor.getSpecialty().getNameOnly(),
                doctor.getNpi()));








        // Check if the provider is available at the specified timeslot










        statusMessageArea.appendText("Scheduling appointment for " + firstName + " " + lastName +
                " on " + appointmentDate + " at " + timeSlot + " with " + provider.getFirstName() + "\n");
        // Implement scheduling logic here



    }


    @FXML
    private void handlePatientSearch(ActionEvent event) {

    }

    @FXML
    private void handleCancelAppointment(ActionEvent event) {

    }

    @FXML
    private void handleRescheduleAppointment(ActionEvent event) {

    }

    @FXML
    private void handleViewOfficeAppointments(ActionEvent event) {

    }

    @FXML
    private void handleViewImagingAppointments(ActionEvent event) {

    }

    @FXML
    private void handleViewProviderCredits(ActionEvent event) {

    }

    @FXML
    private void handleExportReport(ActionEvent event) {

    }

    @FXML
    private void handleAddProvider(ActionEvent event) {

    }

    private Provider findProviderByNPI(String npi) {
        for (Provider provider : providers) {
            if (provider instanceof Doctor) {
                Doctor doctor = (Doctor) provider;
                if (doctor.getNpi().equals(npi)) {
                    return doctor;
                }
            }
        }
        return null;
    }

    private boolean isProviderAvailable(Provider provider, Date date, Timeslot timeslot) {
        for (Appointment appointment : appointments) {
            if (appointment.getProvider().equals(provider) &&
                    appointment.getDate().equals(date) &&
                    appointment.getTimeslot().equals(timeslot)) {
                return false; // Provider has an existing appointment at that date and timeslot
            }
        }
        return true; // Provider is available
    }


    public void handleAddSortOrder(ActionEvent actionEvent) {
    }

    public void handleClearSortOrder(ActionEvent actionEvent) {
    }
}


