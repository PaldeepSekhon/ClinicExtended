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

        imagingTypeComboBox.setItems(FXCollections.observableArrayList("XRAY", "ULTRASOUND", "CATSCAN"));

        // Toggle visibility of office visit and imaging sections based on selection
        appointmentTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == officeVisitRadio) {
                showOfficeVisitFields();
            } else {
                showImagingFields();
            }
        });

        // Set up listener for timeSlotComboBox to capture selected time slot
        timeSlotComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedTimeSlot = newValue;
            }
        });

        // Set up listener for providerNPIComboBox to capture selected provider NPI
        providerNPIComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.contains("#")) {
                selectedProviderNPI = newValue.replaceAll(".*#(\\d{2}).*", "$1");
            }
        });

        this.appointments = new util.List<>(); // Custom List for appointments
        this.providers = new util.List<>(); // Single Custom List for all providers
        this.technicianList = new CircularLinkedList();

        statusMessageArea.appendText("Status message initialized.\n");  // Test message to confirm setup


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
        // Retrieve patient information
        String firstName = patientFirstNameField.getText();
        String lastName = patientLastNameField.getText();
        LocalDate localAppointmentDate = appointmentDateField.getValue();
        Date appointmentDate = new Date(localAppointmentDate.getYear(), localAppointmentDate.getMonthValue(), localAppointmentDate.getDayOfMonth());

        // Verify if the time slot and provider are selected
        if (selectedTimeSlot == null) {
            statusMessageArea.appendText("Error: Please select a time slot.\n");
            nullField();
            return;
        }
        if (officeVisitRadio.isSelected() && selectedProviderNPI == null) {
            statusMessageArea.appendText("Error: Please select a provider.\n");
            nullField();
            return;
        }

        // Validate date and other appointment constraints
        if (!appointmentDate.isValid()) {
            statusMessageArea.appendText("Error: Appointment date " + appointmentDate + " is not a valid calendar date.\n");
            nullField();
            return;
        }

        Date today = new Date(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        if (appointmentDate.compareTo(today) <= 0) {
            statusMessageArea.appendText("Error: Appointment date " + appointmentDate + " is today or a date before today.\n");
            nullField();
            return;
        }

        if (appointmentDate.isWeekend()) {
            statusMessageArea.appendText("Error: Appointment date " + appointmentDate + " is Saturday or Sunday.\n");
            nullField();
            return;
        }

        if (!appointmentDate.isWithinSixMonths()) {
            statusMessageArea.appendText("Error: Appointment date " + appointmentDate + " is not within six months.\n");
            nullField();
            return;
        }

        LocalDate localDOB = dobField.getValue();
        if (localDOB == null) {
            statusMessageArea.appendText("Error: Date of birth is not selected.\n");
            return;
        }

        Date dobDate = new Date(localDOB.getYear(), localDOB.getMonthValue(), localDOB.getDayOfMonth());

        // Check if dobDate is valid
        if (!dobDate.isValid()) {
            statusMessageArea.appendText("Patient dob: " + dobDate + " is not a valid calendar date.\n");
            nullField();
            return;
        }

        // Check if dobDate is today or a future date
        if (dobDate.compareTo(today) >= 0) {
            statusMessageArea.appendText("Patient dob: " + dobDate + " is today or a day after today.\n");
            nullField();
            return;
        }


        // Convert the selected time slot to Timeslot object
        Timeslot timeslotObj = Timeslot.fromString(selectedTimeSlot);

        // Patient profile creation
        Profile profile = new Profile(firstName, lastName, new Date(dobField.getValue().getYear(), dobField.getValue().getMonthValue(), dobField.getValue().getDayOfMonth()));
        Person patient = new Person(profile);

        // Check for duplicate appointments
        for (Appointment appt : appointments) {
            if (appt.getPatient().getProfile().equals(profile) &&
                    appt.getDate().equals(appointmentDate) &&
                    appt.getTimeslot().equals(timeslotObj)) {
                statusMessageArea.appendText("Error: Appointment already exists for " + firstName + " " + lastName + " at the same time slot.\n");
                nullField();
                return;
            }
        }

        // Office Visit Appointment
        if (officeVisitRadio.isSelected()) {
            Provider provider = findProviderByNPI(selectedProviderNPI);

            if (provider == null) {
                statusMessageArea.appendText("Error: Provider with NPI " + selectedProviderNPI + " does not exist.\n");
                nullField();
                return;
            }

            if (!isProviderAvailable(provider, appointmentDate, timeslotObj)) {
                statusMessageArea.appendText("Error: Provider is not available at the selected time slot.\n");
                nullField();
                return;
            }

            Appointment newAppointment = new Appointment(appointmentDate, timeslotObj, patient, provider);
            appointments.add(newAppointment);
            statusMessageArea.appendText("Appointment scheduled successfully for " + firstName + " " + lastName + " on " + appointmentDate + " at " + selectedTimeSlot + " with " + provider.getFirstName() + ".\n");
        }

        // Imaging Appointment
        if (imagingRadio.isSelected()) {
            String imagingService = imagingTypeComboBox.getSelectionModel().getSelectedItem();
            Technician technician = assignTechnicianForService(imagingService, appointmentDate, timeslotObj);

            if (technician == null) {
                statusMessageArea.appendText("Error: No available technician for " + imagingService + " at the selected time slot.\n");
                nullField();
                return;
            }

            Radiology room = Radiology.valueOf(imagingService.toUpperCase());
            Imaging imagingAppointment = new Imaging(appointmentDate, timeslotObj, patient, technician, room);
            appointments.add(imagingAppointment);
            statusMessageArea.appendText("Imaging appointment scheduled successfully for " + firstName + " " + lastName + " on " + appointmentDate + " at " + selectedTimeSlot + " with technician " + technician.getFirstName() + ".\n");
        }
        // Reset fields for next scheduling
        nullField();

    }

    private void nullField() {
        selectedTimeSlot = null;
        timeSlotComboBox.getSelectionModel().clearSelection();
        selectedProviderNPI = null;
        providerNPIComboBox.getSelectionModel().clearSelection();
        patientFirstNameField.clear();
        patientLastNameField.clear();
        appointmentDateField.setValue(null);
        dobField.setValue(null);
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

    private Technician lastAssignedTechnician = null;

    private Technician assignTechnicianForService(String imagingService, Date appointmentDate, Timeslot timeslotObj) {
        if (technicianList.isEmpty()) {
            return null;
        }

        // Get starting technician based on last assignment
        Technician startingTech;
        if (lastAssignedTechnician == null) {
            startingTech = technicianList.getFirstTechnician();
        } else {
            startingTech = technicianList.getNextTechnician(); // Start from the technician after the last assigned one
        }

        Technician currentTech = startingTech;

        // Loop through all technicians once
        do {

            // Check if technician is available and their location's room is available
            if (isTechnicianAvailable(currentTech, appointmentDate, timeslotObj) &&
                    isRoomAvailableAtLocation(currentTech, imagingService, appointmentDate, timeslotObj)) {

                // Book both technician and room

                // Update last assigned technician for next rotation
                lastAssignedTechnician = currentTech;
                return currentTech;
            }

            currentTech = technicianList.getNextTechnician();
        } while (currentTech != startingTech);

        // Update the last assigned technician even when no one is available
        // This ensures next search starts from the next person in rotation
        technicianList.getFirstTechnician();

        return null;
    }

    private boolean isTechnicianAvailable(Technician technician, Date appointmentDate, Timeslot timeslot) {
        // Check if technician is already booked at this time by examining the
        // appointments list
        for (Appointment appointment : appointments) {
            if (appointment.getTechnician() != null && appointment.getTechnician().equals(technician) &&
                    appointment.getDate().equals(appointmentDate) &&
                    appointment.getTimeslot().equals(timeslot)) {
                return false; // Technician is not available
            }
        }
        return true; // Technician is available
    }

    private boolean isRoomAvailableAtLocation(Technician technician, String serviceType,
                                              Date appointmentDate, Timeslot slot) {
        String location = technician.getLocation().getCity();

        // Check existing appointments for room conflict at this location
        for (Appointment appointment : appointments) {
            if (appointment instanceof Imaging && appointment.getTechnician().getLocation().getCity().equals(location)
                    &&
                    appointment.getServiceType().equalsIgnoreCase(serviceType) &&
                    appointment.getDate().equals(appointmentDate) &&
                    appointment.getTimeslot().equals(slot)) {

                return false;
            }
        }

        return true;
    }
}


