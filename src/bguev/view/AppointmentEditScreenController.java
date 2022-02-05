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


public class AppointmentEditScreenController implements Initializable {

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
    
    private int apt_id;
    
    private ZoneId z_id = ZoneId.systemDefault();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Fill in our lists
        // We disable the customer box when editing. If the customer has to change.
        // The user should just delete the appointment and make a new one.
        conn = DBConnect.getConn();
        fillTypeList();
        fillLocationList();
        customerBox.setDisable(true);
        userF.setDisable(true);
        fillTimes();
        msg.setText("Enter Appointment Information");
        dateF.setEditable(false);
        
    }    
    // When pressing the update Button.
    @FXML
    private void updateButton() throws SQLException{       
        ResultSet appts = conn.prepareStatement("Select * FROM appointment").executeQuery();
        Alert a = new Alert (AlertType.NONE );

        
        // We will parse our day and time values and them compare them to each other and other appointments
        String day = "";
        // Alert is thrown if not all fields are filled.
        if (customerBox.getValue() == null || dateF.getValue() == null || descF.getText().trim().isEmpty()
                || typeBox.getValue() == null || startBox.getValue() == null || titleF.getText().trim().isEmpty()
                || endBox.getValue() == null || locBox.getValue() == null || contF.getText().trim().isEmpty()) {
            a.setAlertType(AlertType.ERROR);
            a.setContentText("Please fill in all needed fields.");
            a.show();
            return;
        }
        
        
        try{
            day = dateF.getValue().toString();
        }
        catch (NullPointerException d)
            // Throw an error if no date is selected
        {
            a.setAlertType(AlertType.ERROR);
            a.setContentText("Select a date.");
            a.show();
            return;
        } 
        
        
        
        
        String pattern = "yyyy-MM-dd hh:mm a";
        DateTimeFormatter Parser = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
       
        ZonedDateTime startZ = ZonedDateTime.parse(day  + " " + startBox.getValue().toString(), Parser);
        ZonedDateTime utcStart = startZ.withZoneSameInstant(ZoneId.of("UTC"));
        
        
        ZonedDateTime endZ = ZonedDateTime.parse(day  + " " + endBox.getValue().toString(), Parser);
        ZonedDateTime utcEnd = endZ.withZoneSameInstant(ZoneId.of("UTC"));
       
        
        Timestamp startTS = Timestamp.valueOf(utcStart.toLocalDateTime());
        Timestamp endTS = Timestamp.valueOf(utcEnd.toLocalDateTime());
        

        // Errors are thrown for conflicting times.
        if (startTS.after(endTS)){
            a.setAlertType(AlertType.ERROR);
            a.setContentText("Your start time is after the end time.");
            a.show();
            return;
        }

        
        while (appts.next()){
            Timestamp s = appts.getTimestamp("start");
            Timestamp e = appts.getTimestamp("end");
            // If we're r
            if (appts.getInt("appointment.appointmentId") == apt_id && ((startTS.after(s) && startTS.before(e))|| startTS.equals(s))){
                
            }
            else if (appts.getInt("appointment.appointmentId") == apt_id && ((endTS.after(s) && endTS.before(e))|| endTS.equals(e))){
                
            }
            
            else if (appts.getInt("appointment.appointmentId") != apt_id && ((startTS.after(s) && startTS.before(e))|| startTS.equals(s))){
                a.setAlertType(AlertType.ERROR);
                a.setContentText("Conflicting start time.");
                a.show();
                return;
            }
            else if (appts.getInt("appointment.appointmentId") != apt_id && ((endTS.after(s) && endTS.before(e))|| endTS.equals(e))){
                a.setAlertType(AlertType.ERROR);
                a.setContentText("Conflicting end time.");
                a.show();
                return;
            }
            
        }      
        
        
        //Get CustomerID
        int cust_id = 0;
        PreparedStatement custom = conn.prepareStatement("SELECT DISTINCT customerName, customerId "
                + "FROM customer WHERE customerName = '" + customerBox.getValue()+ "'");
        ResultSet rs = custom.executeQuery();
        while (rs.next()){
            cust_id = rs.getInt("customerId");
  
        }
        // Update our appointment.
        PreparedStatement add_appt = conn.prepareStatement("UPDATE appointment "
                + "SET title = '" + titleF.getText() + "', description = '"+ descF.getText()
                + "', location = '" + locBox.getValue() +"', contact = '" + contF.getText()
                + "', type = '" + typeBox.getValue() 
                + "', url = '" + urlF.getText() + "', start = '" + startTS 
                + "', end = '" + endTS + "', lastUpdate = NOW(), lastUpdateBy = '"+user.getUsername()
                + "' WHERE appointmentId = " + apt_id);
        add_appt.executeUpdate();
        
        
        mainW.setTheMsg("Appointment Edited!");
        mainW.popTable();
        Stage thisWindow = (Stage) msg.getScene().getWindow();
        thisWindow.close();
    }
    
    protected void loadUser(User x){
        this.user = x;
        userF.setText(user.getUsername());
 
        
    }
    
    public void setController (CalendarScreenController c){
        mainW = c;
    }
    
    @FXML
    private void closeButtonAction(){
        Stage stage = (Stage) msg.getScene().getWindow();
        stage.close();
    }
    
    
    // Following are methods that use Arrays and lambda expressions to add
    // objects to our lists.
    private void fillLocationList(){
        String [] locations = {"Phoenix, Arizona", "New York, New York", "London, England"};
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
        
        // Lambda expression for an array list of the types of meetings.
        // rather than a for loop, this is an easy way to add all these
        types.forEach((n) -> typeBox.getItems().add(n));
        
    }
    
    private void fillTimes() {
        ArrayList<String> times = new ArrayList<>();
        int [] hours = {8,9,10,11,12,13,14,15,16,17,18};
        int [] minutes = {0,15,30,45};
        DateTimeFormatter form = DateTimeFormatter.ofPattern("hh:mm a");
        
        // We will use this nested for loop to create all our time-strings.
        for (int hour : hours){
            for (int min : minutes){
                LocalTime x = LocalTime.of(hour,min);
                times.add(x.format(form));
            }
        }
        
        // These lambda expressions will take the created arraylist and add them
        // to both time boxes (start and end). This is a lot more convenient than
        // creating another basic for loop.
        times.forEach((n)-> startBox.getItems().add(n));
        times.forEach((n)-> endBox.getItems().add(n));
    }

    
    // This is used in a controller to setup the fields with the appointments information.
    protected void loadAppointment (int app_id) throws SQLException{
        PreparedStatement appt_query = conn.prepareStatement("SELECT * FROM appointment, customer "
                + "WHERE appointment.customerId = customer.customerId AND appointment.appointmentId = " + app_id);
        ResultSet rs = appt_query.executeQuery();
        while (rs.next()){
            ZonedDateTime zStart;
            ZonedDateTime lStart;
            zStart = rs.getTimestamp("appointment.start").toLocalDateTime().atZone(ZoneId.of("UTC"));
            lStart = zStart.withZoneSameInstant(z_id);
            
            ZonedDateTime zEnd;
            ZonedDateTime lEnd;
            zEnd = rs.getTimestamp("appointment.end").toLocalDateTime().atZone(ZoneId.of("UTC"));
            lEnd = zEnd.withZoneSameInstant(z_id);
            
            
            customerBox.setValue(rs.getString("customer.customername"));
            dateF.setValue(lStart.toLocalDate());
            titleF.setText(rs.getString("appointment.title"));
            descF.setText(rs.getString("appointment.description"));
            typeBox.setValue(rs.getString("appointment.type"));
            startBox.setValue(lStart.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
            endBox.setValue(lEnd.toLocalDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
            locBox.setValue(rs.getString("appointment.location"));
            contF.setText(rs.getString("appointment.contact"));
            urlF.setText(rs.getString("appointment.url"));     
        
        }
        apt_id = app_id;
        
        
    }
}
