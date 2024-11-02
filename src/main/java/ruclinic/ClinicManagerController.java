package ruclinic;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.Map;
import java.util.HashMap;

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

    public TableColumn list_countyColumn;
    public TableColumn list_timeslot;
    public TableColumn list_name;
    @FXML private TableView <String> appointmentListTable;
    public ListView sortOrderListView;
    @FXML private ComboBox<String> sortOptionsComboBox;
    @FXML private ComboBox<String> appointmentTypeComboBox;
    public TableColumn zipColumn;
    public TableColumn countyColumn;
    public TableColumn cityColumn;
    public TableView clinicLocationsTable;

    //Reschedule
    @FXML private TextField rescheduleFirstNameField;
    @FXML private TextField rescheduleLastNameField;
    @FXML private DatePicker rescheduleDOBField;
    @FXML  private ComboBox<String> oldTimeSlotComboBox;
    @FXML private DatePicker originalDateField;
    @FXML private ComboBox<String> newTimeSlotComboBox;


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

    @FXML private ComboBox<String> sortOptionComboBox;


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
    @FXML private TextArea tab_textbox;
    @FXML private TextArea rescheduleTextArea;

    @FXML private HBox loadProviders;
    @FXML private HBox cancelAppointment;
    @FXML private HBox reschedule;
    @FXML private HBox scheduleAppointment;

    private String selectedProviderNPI;
    private String selectedTimeSlot;


    //Sorting
    private String selectedSortOrder;
    private String selectedAppointmentType;

    private String selectedOldTimeSlot;
    private String selectedNewTimeSlot;

    @FXML private ComboBox<String> billingSegmentComboBox;

    private String selectedBillingSegment;



    ObservableList<String> timeSlots = FXCollections.observableArrayList(
            "9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM"
    );

    public void initialize() {


        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        countyColumn.setCellValueFactory(new PropertyValueFactory<>("county"));
        zipColumn.setCellValueFactory(new PropertyValueFactory<>("zip"));


        timeSlotComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        oldTimeSlotComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        newTimeSlotComboBox.setItems(FXCollections.observableArrayList(timeSlots));


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

        oldTimeSlotComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedOldTimeSlot = newValue;
            }
        });

        newTimeSlotComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedNewTimeSlot = newValue;
            }
        });

        // Set up listener for providerNPIComboBox to capture selected provider NPI
        providerNPIComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.contains("#")) {
                selectedProviderNPI = newValue.replaceAll(".*#(\\d{2}).*", "$1");
            }
        });

        sortOptionsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedSortOrder = newValue;
            }
        });

        appointmentTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedAppointmentType = newValue;
            }
        });

        billingSegmentComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedBillingSegment = newValue;
            }
        });

        this.appointments = new util.List<>(); // Custom List for appointments
        this.providers = new util.List<>(); // Single Custom List for all providers
        this.technicianList = new CircularLinkedList();

        statusMessageArea.appendText("Running clinic manager..\n");  // Test message to confirm setup


    }

    @FXML
    private void handleLoadProviderButton(ActionEvent event) {
        // Code to load provider information
       // System.out.println("Loading provider information...");

        // For example, you could fetch provider data from a database or file
        // Here, add your logic to populate the Appointment Management tab with provider details
        ObservableList<Location> locationData = FXCollections.observableArrayList();

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

                    boolean locationExists = false;
                    for(Location existingLocation : locationData) {
                        if(existingLocation.getCity().equals(location.getCity()) &&
                                existingLocation.getCounty().equals(location.getCounty()) &&
                                existingLocation.getZip().equals(location.getZip())) {
                            locationExists = true;
                            break;
                        }
                    }
                    if(!locationExists) {
                        locationData.add(location);
                    }

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

                    boolean locationExists = false;
                    for(Location existingLocation : locationData) {
                        if(existingLocation.getCity().equals(location.getCity()) &&
                                existingLocation.getCounty().equals(location.getCounty()) &&
                                existingLocation.getZip().equals(location.getZip())) {
                            locationExists = true;
                            break;
                        }
                    }
                    if(!locationExists) {
                        locationData.add(location);
                    }
                }
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            statusMessageArea.appendText("Error: providers.txt not found.");
        } catch (Exception e) {
            statusMessageArea.appendText("Error loading providers: " + e.getMessage());
        }

        clinicLocationsTable.setItems(locationData);

        //print
        ObservableList<String> providerOptions = FXCollections.observableArrayList();
        Sort.provider(providers); // Sort by provider profile
        for (Provider provider : providers) {
            if(provider instanceof Doctor) {
                Doctor doctor = (Doctor) provider;
                String providerDetails = String.format("[%s %s %s, %s, %s %s]",
                        provider.getProfile().getFirstName(),
                        provider.getProfile().getLastName(),
                        provider.getProfile().getDob(),
                        provider.getLocation().getCity(),
                        provider.getLocation().getCounty(),
                        provider.getLocation().getZip());

                providerDetails += String.format("[%s, #%s]",
                        doctor.getSpecialty().getNameOnly(),
                        doctor.getNpi());

                statusMessageArea.appendText(providerDetails);
                providerOptions.add(providerDetails);
            }
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
        if (localAppointmentDate == null) {
            statusMessageArea.appendText("Error: Please select a valid appointment date.\n");
            nullField();
            return;
        }

        Date appointmentDate = new Date(localAppointmentDate.getYear(), localAppointmentDate.getMonthValue(), localAppointmentDate.getDayOfMonth());

        // Verify if the time slot and provider are selected
        if (selectedTimeSlot == null) {
            statusMessageArea.appendText("Error: Please select a time slot.\n");
            nullField();
            return;
        }

        // Convert the selected time slot to Timeslot object
        Timeslot timeslotObj = Timeslot.fromString(selectedTimeSlot);
        if (timeslotObj == null) {
            statusMessageArea.appendText("Error: Invalid or null timeslot selected.\n");
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

        String firstName = patientFirstNameField.getText();
        String lastName = patientLastNameField.getText();
        LocalDate localAppointmentDate = appointmentDateField.getValue();
        Date appointmentDate = new Date(localAppointmentDate.getYear(), localAppointmentDate.getMonthValue(), localAppointmentDate.getDayOfMonth());
        Timeslot timeslotObj = Timeslot.fromString(selectedTimeSlot);
        LocalDate localDOB = dobField.getValue();
        Date dobDate = new Date(localDOB.getYear(), localDOB.getMonthValue(), localDOB.getDayOfMonth());

        boolean found = false;
        for (Appointment appt : appointments) {
            if (appt.getPatient().getFirstName().equalsIgnoreCase(firstName)
                    && appt.getPatient().getLastName().equalsIgnoreCase(lastName)
                    && appt.getDate().equals(
                    appointmentDate)
                    && appt.getTimeslot().equals(timeslotObj)) {
                appointments.remove(appt);
                statusMessageArea.appendText(String.format("%s %s - %s %s %s - appointment has been canceled.%n",
                        appointmentDate, timeslotObj, firstName, lastName, dobDate));
                found = true;
                break;
            }
        }
        if (!found) {
            statusMessageArea.appendText(String.format("%s %s - %s %s %s - appointment does not exist.%n",
                    appointmentDate, timeslotObj, firstName, lastName, dobDate));
        }
    }

    @FXML
    private void handleRescheduleAppointment(ActionEvent event) {
        String firstName = rescheduleFirstNameField.getText();
        String lastName = rescheduleLastNameField.getText();
        LocalDate localRescheduleDob = rescheduleDOBField.getValue();
        Date rescheduleDob = new Date(localRescheduleDob.getYear(), localRescheduleDob.getMonthValue(), localRescheduleDob.getDayOfMonth());
        LocalDate localRescheduleOriginalDate = originalDateField.getValue();
        Date rescheduleOriginalDate = new Date(localRescheduleOriginalDate.getYear(), localRescheduleOriginalDate.getMonthValue(), localRescheduleOriginalDate.getDayOfMonth());
        Timeslot oldtimeSlot = Timeslot.fromString(selectedOldTimeSlot);
        Timeslot newtimeSlot = Timeslot.fromString(selectedNewTimeSlot);

        boolean hasConflict = false;
        for (Appointment appt : appointments) {
            if (appt.getPatient().getFirstName().equalsIgnoreCase(firstName)
                    && appt.getPatient().getLastName().equalsIgnoreCase(lastName)
                    && appt.getDate().equals(rescheduleOriginalDate)
                    && appt.getTimeslot().equals(newtimeSlot)) {
                hasConflict = true;
                break;
            }
        }

        if (hasConflict) {
            rescheduleTextArea.appendText(String.format("%s %s %s %s has an existing appointment at %s %s%n",
                    firstName, lastName, rescheduleDob, "on", rescheduleOriginalDate, newtimeSlot));
            return; // Exit the method if there's a conflict
        }

        boolean found = false;
        for (Appointment appt : appointments) {
            if (appt.getPatient().getFirstName().equalsIgnoreCase(firstName)
                    && appt.getPatient().getLastName().equalsIgnoreCase(lastName)
                    && appt.getDate().equals(rescheduleOriginalDate)
                    && appt.getTimeslot().equals(oldtimeSlot)) {
                appt.setTimeSlot(newtimeSlot); // Reschedule
                rescheduleTextArea.appendText(String.format("Rescheduled to %s %s %s %s [%s, %s, %s]%n",
                        localRescheduleOriginalDate, newtimeSlot, firstName, lastName,
                        appt.getProvider().getProfile().getFirstName(), // Assuming provider has a profile with a
                        // name
                        appt.getProvider().getLocation().getCity(),
                        appt.getProvider().getLocation().getCounty()));
                found = true;

            }
        }

        if (!found) {
            rescheduleTextArea.appendText(String.format("%s %s %s %s does not exist.%n",
                    rescheduleOriginalDate, oldtimeSlot, firstName, lastName));
        }





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

    @FXML
    private void onBillingDisplay(ActionEvent event) {
        if(selectedBillingSegment.equals("Patient"))
        {
            displayBillingStatements();
        }
        if (selectedBillingSegment.equals("Provider"))
        {
            displayCredits();
        }
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

    @FXML
    private  void handleDisplaySorting(ActionEvent actionEvent) {

        if(selectedSortOrder.equals("date,time,name")&&selectedAppointmentType.equals("All Appointments"))
        {
            tab_textbox.appendText("** List of all appointments, ordered by date/time/provider.\n");
            Sort.appointmentByDateTimeAndProvider(appointments);
            listAllAppointments();
            tab_textbox.appendText("** end of list\n");

        }
        if(selectedSortOrder.equals("patient,date,time")&&selectedAppointmentType.equals("All Appointments"))
        {
            tab_textbox.appendText("** List of all appointments, ordered by patient/date/time.\n");
            Sort.appointment(appointments,'p');
            listAllAppointments();
            tab_textbox.appendText("** end of list\n");

        }
        if(selectedSortOrder.equals("county,date,time")&&selectedAppointmentType.equals("All Appointments"))
        {
            tab_textbox.appendText("** List of all appointments, ordered by county/date/time.\n");
            Sort.appointmentByCounty(appointments);
            listAllAppointments();
            tab_textbox.appendText("** end of list\n");

        }


        if(selectedSortOrder.equals("date,time,name")&&selectedAppointmentType.equals("Imaging"))
        {
            tab_textbox.appendText("** List of Imaging appointments, ordered by date/time/provider.\n");
            Sort.appointmentByDateTimeAndProvider(appointments);
            listImagingAppointments();
            tab_textbox.appendText("** end of list\n");


        }
        if(selectedSortOrder.equals("patient,date,time")&&selectedAppointmentType.equals("Imaging"))
        {
            tab_textbox.appendText("** List of Imaging appointments, ordered by patient/date/time.\n");
            Sort.appointment(appointments,'p');
            listImagingAppointments();
            tab_textbox.appendText("** end of list\n");

        }
        if(selectedSortOrder.equals("county,date,time")&&selectedAppointmentType.equals("Imaging"))
        {
            tab_textbox.appendText("** List of Imaging appointments, ordered by county/date/time.\n");
            Sort.appointmentByCounty(appointments);
            listImagingAppointments();
            tab_textbox.appendText("** end of list\n");

        }


        if(selectedSortOrder.equals("date,time,name")&&selectedAppointmentType.equals("Office Appointments"))
        {
            tab_textbox.appendText("** List of Office appointments, ordered by date/time/provider.\n");
            Sort.appointmentByDateTimeAndProvider(appointments);
            listOfficeAppointments();
            tab_textbox.appendText("** end of list\n");


        }
        if(selectedSortOrder.equals("patient,date,time")&&selectedAppointmentType.equals("Office Appointments"))
        {
            tab_textbox.appendText("** List of Office appointments, ordered by patient/date/time.\n");
            Sort.appointment(appointments,'p');
            listOfficeAppointments();
            tab_textbox.appendText("** end of list\n");

        }
        if(selectedSortOrder.equals("county,date,time")&&selectedAppointmentType.equals("Office Appointments"))
        {
            tab_textbox.appendText("** List of Office appointments, ordered by county/date/time.\n");
            Sort.appointmentByCounty(appointments);
            listOfficeAppointments();
            tab_textbox.appendText("** end of list\n");

        }



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

    private void listOfficeAppointments() {

        // Sort appointments by county, date, and timeslot

        // Display sorted appointments
        for (Appointment appointment : appointments) {
            if (!(appointment instanceof Imaging)) { // Only list office appointments
                displayRegularAppointment(appointment);
            }
        }

    }

    private void listAllAppointments() {
        for (Appointment appointment : appointments) {
            if (appointment instanceof Imaging) {
                displayImagingAppointment((Imaging) appointment);
            } else {
                displayRegularAppointment(appointment);

            }
        }
    }

    private void displayImagingAppointment(Imaging imagingAppointment) {
        String appointmentDate = imagingAppointment.getDate().toString();
        String timeslot = imagingAppointment.getTimeslot().toString();
        Person patient = imagingAppointment.getPatient();
        Provider provider = imagingAppointment.getProvider();
        String roomType = imagingAppointment.getRoom().toString();
        double rate = provider.rate();

        tab_textbox.appendText(String.format("%s %s %s %s %s [%s %s %s, %s, %s %s][rate: $%.2f][%s]%n",
                appointmentDate,
                timeslot,
                patient.getProfile().getFirstName(),
                patient.getProfile().getLastName(),
                patient.getProfile().getDob(),

                provider.getProfile().getFirstName(),
                provider.getProfile().getLastName(),
                provider.getProfile().getDob(),

                provider.getLocation().getCity(),
                provider.getLocation().getCounty(),
                provider.getLocation().getZip(),
                rate,
                roomType));
    }

    private void displayRegularAppointment(Appointment appointment) {
        Person patient = appointment.getPatient();
        Provider provider = appointment.getProvider();
        Doctor doctor = (Doctor) provider; // Cast the provider to Doctor

        tab_textbox.appendText(String.format("%s %s %s %s %s [%s %s %s, %s, %s %s][%s, #%s]%n",
                appointment.getDate(),
                appointment.getTimeslot(),
                patient.getProfile().getFirstName(),
                patient.getProfile().getLastName(),
                patient.getProfile().getDob(),
                doctor.getProfile().getFirstName(),
                doctor.getProfile().getLastName(),
                doctor.getProfile().getDob(),
                doctor.getLocation().getCity(),
                doctor.getLocation().getCounty(),
                doctor.getLocation().getZip(),
                doctor.getSpecialty().name(),
                doctor.getNpi()));
    }


    private void listImagingAppointments() {

        for (Appointment appointment : appointments) {
            if (appointment instanceof Imaging) { // Only display imaging appointments
                Imaging imagingAppointment = (Imaging) appointment; // Cast to Imaging to access additional details

                // Extract details from the imaging appointment
                String appointmentDate = imagingAppointment.getDate().toString(); // Assuming toString returns the
                // desired date format
                String timeslot = imagingAppointment.getTimeslot().toString(); // Assuming toString returns the desired
                // timeslot format
                Person patient = imagingAppointment.getPatient(); // Get patient details
                Provider provider = imagingAppointment.getProvider(); // Get provider details
                String roomType = imagingAppointment.getRoom().toString(); // Get the imaging room type
                double rate = provider.rate(); // Assuming rate is a method in Provider

                // Print formatted output
                tab_textbox.appendText(String.format("%s %s %s %s [%s %s, %s, %s, %s][rate: $%.2f][%s]%n",
                        appointmentDate, // Date of appointment
                        timeslot, // Timeslot of appointment
                        patient.getProfile().getFirstName(), // Patient's first name
                        patient.getProfile().getLastName(), // Patient's last name
                        provider.getProfile().getFirstName(), // Provider's first name
                        provider.getProfile().getDob(), // Provider's date of birth
                        provider.getLocation().getCity(), // Provider's city
                        provider.getLocation().getCounty(), // Provider's county
                        provider.getLocation().getZip(), // Provider's ZIP code
                        rate, // Rate charged by provider
                        roomType)); // Imaging room type
            }
        }
    }

    private void displayBillingStatements() {

        tab_textbox.appendText("** Billing Statements ordered by Patients **\n");

        // Create a map to accumulate charges by patient (using a unique key based on name and dob)
        Map<String, Double> billingMap = new HashMap<>();

        // Iterate over all appointments
        for (Appointment appointment : appointments) {
            Person patient = appointment.getPatient();
            String patientName = patient.getProfile().getFirstName() + " " + patient.getProfile().getLastName();
            String dob = patient.getProfile().getDob().toString(); // Assuming dob is in a String format

            // Use a unique key to identify each patient by name and dob
            String uniquePatientKey = patientName + " " + dob;

            // Determine the charge based on the provider (doctor or technician)
            Provider provider = appointment.getProvider();
            double charge = 0;

            if (provider instanceof Doctor) {
                Doctor doctor = (Doctor) provider;
                charge = doctor.rate();
            } else if (provider instanceof Technician) {
                Technician technician = (Technician) provider;
                charge = technician.getRatePerVisit();
            }

            // Accumulate the charge for the patient
            billingMap.put(uniquePatientKey, billingMap.getOrDefault(uniquePatientKey, 0.0) + charge);
        }

        // Display the total charges for each patient
        int index = 1;
        for (Map.Entry<String, Double> entry : billingMap.entrySet()) {
            String patientIdentifier = entry.getKey();
            double totalCharge = entry.getValue();
            tab_textbox.appendText(String.format("(%d) %s [total due: $%.2f]%n", index++, patientIdentifier, totalCharge));
        }

        tab_textbox.appendText("** end of list **\n");

        // Clear appointments list if needed
        appointments = null;
    }

    private void displayCredits() {

       tab_textbox.appendText(("** Credit amount ordered by provider. **\n"));

        // Sort providers by profile
        Sort.provider(providers);

        for (int i = 0; i < providers.size(); i++) {
            Provider provider = providers.get(i);
            String creditDetails;

            // Determine if the provider is a Doctor or Technician
            if (provider instanceof Doctor) {
                creditDetails = String.format("%s [%s] [credit amount: $%.2f]",
                        provider.getProfile().getFirstName() + " " + provider.getProfile().getLastName(),
                        provider.getProfile().getDob(),
                        (double) provider.rate()); // Cast to double for formatting
            } else if (provider instanceof Technician) {
                creditDetails = String.format("%s [%s] [credit amount: $%.2f]",
                        provider.getProfile().getFirstName() + " " + provider.getProfile().getLastName(),
                        provider.getProfile().getDob(),
                        (double) provider.rate()); // Cast to double for formatting
            } else {
                continue; // Skip any other type of provider not handled
            }

            // Print the formatted output with the index
            tab_textbox.appendText(String.format("(%d) %s%n", (i + 1), creditDetails));
        }

        tab_textbox.appendText("** end of list **\n");
    }


}


