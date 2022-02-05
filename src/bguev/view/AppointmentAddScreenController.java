/**
 *
 * Brian Guevara
 * WGU ID: 001003681
 */
package bguev.view;

import bguev.classes.User;
import bguev.util.DBConnect;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AppointmentAddScreenController implements Initializable {

    @FXML
    private TextField userF;

    @FXML
    private ComboBox customerBox;

    @FXML
    private DatePicker dateF;

    @FXML
    private TextField descF;
    @FXML
    private TextField titleF;

    @FXML
    private ComboBox typeBox;

    @FXML
    private ComboBox startBox;

    @FXML
    private ComboBox endBox;

    @FXML
    private ComboBox locBox;

    @FXML
    private TextField contF;

    @FXML
    private TextField urlF;

    @FXML
    private Label msg;

    private Connection conn;
    private User user;

    private CalendarScreenController mainW;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnect.getConn();

        // Fill in the lists in our page.
        fillTypeList();
        fillLocationList();
        try {
            fillCustomerList();
        } catch (SQLException ex) {
        }
        userF.setDisable(true);
        fillTimes();

        // We will make it so the date can only be edited by the picker, not user text.
        msg.setText("Enter Appointment Information");
        dateF.setEditable(false);

    }

    // Saves the new appointment.
    @FXML
    private void saveButtonAction() throws SQLException {
        ResultSet appts = conn.prepareStatement("Select * FROM appointment").executeQuery();
        Alert a = new Alert(AlertType.NONE);

        String day = "";
        
        if (customerBox.getValue() == null || dateF.getValue() == null || descF.getText().trim().isEmpty()
                || typeBox.getValue() == null || startBox.getValue() == null || titleF.getText().trim().isEmpty()
                || endBox.getValue() == null || locBox.getValue() == null || contF.getText().trim().isEmpty()) {
            a.setAlertType(AlertType.ERROR);
            a.setContentText("Please fill in all needed fields.");
            a.show();
            return;
        }
        
        try {
            day = dateF.getValue().toString();
        } catch (NullPointerException d) {
            a.setAlertType(AlertType.ERROR);
            a.setContentText("Select a date.");
            a.show();
            return;
        }
        // Checks to see that all fields are filled, besides URL which is optional.
        

        // Create the formatted version we need.
        String pattern = "yyyy-MM-dd hh:mm a";
        DateTimeFormatter Parser = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
       
        ZonedDateTime startZ = ZonedDateTime.parse(day  + " " + startBox.getValue().toString(), Parser);
        ZonedDateTime utcStart = startZ.withZoneSameInstant(ZoneId.of("UTC"));
        
        
        ZonedDateTime endZ = ZonedDateTime.parse(day  + " " + endBox.getValue().toString(), Parser);
        ZonedDateTime utcEnd = endZ.withZoneSameInstant(ZoneId.of("UTC"));
       
        
        Timestamp startTS = Timestamp.valueOf(utcStart.toLocalDateTime());
        Timestamp endTS = Timestamp.valueOf(utcEnd.toLocalDateTime());
        // Check the value of the input times to ensure start is not after end.

        if (startTS.after(endTS)) {
            a.setAlertType(AlertType.ERROR);
            a.setContentText("Your start time is after the end time.");
            a.show();
            return;
        }

        // Next we search our database for any appointments that are in those times.
        // We throw out a msg if there is a conflicting thing.
        while (appts.next()) {
            Timestamp s = appts.getTimestamp("start");
            Timestamp e = appts.getTimestamp("end");
            if ((startTS.after(s) && startTS.before(e)) || startTS.equals(s)) {
                a.setAlertType(AlertType.ERROR);
                a.setContentText("Conflicting start time.");
                a.show();
                return;
            }
            if ((endTS.after(s) && endTS.before(e)) || endTS.equals(e)) {
                a.setAlertType(AlertType.ERROR);
                a.setContentText("Conflicting end time.");
                a.show();
                return;
            }
        }

        

        //Get CustomerID
        int cust_id = 0;
        PreparedStatement custom = conn.prepareStatement("SELECT DISTINCT customerName, customerId "
                + "FROM customer WHERE customerName = '" + customerBox.getValue() + "'");
        ResultSet rs = custom.executeQuery();
        while (rs.next()) {
            cust_id = rs.getInt("customerId");

        }

        // Add our appointment to the database.
        PreparedStatement add_appt = conn.prepareStatement("INSERT INTO appointment "
                + "(customerId, userId, title, description, location, contact, type, url, start, end, "
                + "createDate, createdBy, lastUpdate, lastUpdateBy) VALUES ("
                + cust_id + ", " + user.getUserID() + ", '" + titleF.getText()
                + "', '" + descF.getText() + "', '" + locBox.getValue() + "', '"
                + contF.getText() + "', '" + typeBox.getValue() + "', '"
                + urlF.getText().toLowerCase() + "', '" + startTS + "', '"
                + endTS + "', NOW(), '" + user.getUsername() + "', NOW(), '"
                + user.getUsername() + "')");
        add_appt.executeUpdate();

        mainW.setTheMsg("Appointment Added!");
        mainW.popTable();
        Stage thisWindow = (Stage) msg.getScene().getWindow();
        thisWindow.close();
    }

    protected void loadUser(User x) {
        this.user = x;
        userF.setText(user.getUsername());

    }

    public void setController(CalendarScreenController c) {
        mainW = c;
    }

    @FXML
    private void closeButtonAction() {
        Stage stage = (Stage) msg.getScene().getWindow();
        stage.close();
    }

    // Following methods are the same as the ones in the Apt Edit Screen page. They use Arrays
    // and lambda expressions to help create our combo box fields. 
    private void fillCustomerList() throws SQLException {
        PreparedStatement custom = conn.prepareStatement("SELECT DISTINCT customerName "
                + "FROM customer");
        ResultSet rs = custom.executeQuery();
        while (rs.next()) {
            customerBox.getItems().add(rs.getString("customerName"));

        }

    }

    private void fillLocationList() {
        String[] locations = {"Phoenix, Arizona", "New York, New York", "London, England"};
        for (String location : locations) {
            locBox.getItems().add(location);
        }

    }

    private void fillTypeList() {
        ArrayList<String> types = new ArrayList<String>();
        types.add("Status Update");
        types.add("Information Sharing");
        types.add("Decision Planning");
        types.add("Problem Solving");
        types.add("Innovation");
        types.add("Team Building");
        types.forEach((n) -> typeBox.getItems().add(n));

    }

    private void fillTimes() {
        ArrayList<String> times = new ArrayList<>();
        int[] hours = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        int[] minutes = {0, 15, 30, 45};
        DateTimeFormatter form = DateTimeFormatter.ofPattern("hh:mm a");
        for (int hour : hours) {
            for (int min : minutes) {
                LocalTime x = LocalTime.of(hour, min);
                times.add(x.format(form));
            }
        }
        times.forEach((n) -> startBox.getItems().add(n));
        times.forEach((n) -> endBox.getItems().add(n));
    }

}
