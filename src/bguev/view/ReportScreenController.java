/**
 *
 * Brian Guevara
 * WGU ID: 001003681
 */
package bguev.view;

import bguev.classes.Appointment;
import bguev.classes.Report;
import bguev.util.DBConnect;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class ReportScreenController implements Initializable {

    @FXML
    private TableView<Appointment> consultantTable;
    @FXML
    private TableView<Report> locationTable;
    @FXML
    private TableView<Report> typeTable;


    @FXML
    private TableColumn<Appointment, LocalDate> customerColumn;
    
    @FXML
    private TableColumn<Appointment, LocalDate> dateColumn;

    @FXML
    private TableColumn<Appointment, String> startColumn;

    @FXML
    private TableColumn<Appointment, String> endColumn;
    
    
 
    
    
    @FXML
    private TableColumn <Report, String> typeColumn;
    @FXML
    private TableColumn<Report, String> typeCount;
    
    @FXML
    
    private TableColumn<Report, String> locColumn;
    
    @FXML
    private TableColumn<Report, String> locCount;
    
    @FXML
    private ComboBox nameList;
    
    private Connection conn;
    
    private final ZoneId z_id = ZoneId.systemDefault();

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnect.getConn();
        
        
        // Load the columns with data
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        locColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        
        // Fill in the tables and drop boxes
        try {
            fillTypes();
            fillLocations();
            fillNameList();
            fillConsultantTable();
            
        } catch (SQLException ex) {}
    }    
    
    
    @FXML
    private void fillConsultantTable() throws SQLException{
        String where_clause = "";
        
        // If no name is selected, we will display all appointments
        if (nameList.getValue() == null){
        }
        // If there is a name then we will add to the where clause in the next step
        else{
            where_clause = "AND contact = '" + nameList.getValue().toString() + "'";

        }
        
        // Get appointments and customer info 
        PreparedStatement search = conn.prepareStatement("SELECT * FROM customer c, appointment a "
                + "WHERE c.customerId = a.customerId " + where_clause
                + " ORDER BY a.start");
        ResultSet rs = search.executeQuery();
        
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        String customer;
        String type;
        int id;
        ZonedDateTime zStart;
        ZonedDateTime lStart;
        
        
        ZonedDateTime zEnd;
        ZonedDateTime lEnd;
        
        
        
        // Load all our information into an Appointment object which goes into
        // our appointment list
        while (rs.next()){
            zStart = rs.getTimestamp("a.start").toLocalDateTime().atZone(ZoneId.of("UTC"));
            lStart = zStart.withZoneSameInstant(z_id);
            
            zEnd = rs.getTimestamp("a.end").toLocalDateTime().atZone(ZoneId.of("UTC"));
            lEnd = zEnd.withZoneSameInstant(z_id);
            customer = rs.getString("c.customerName");
            type = rs.getString("a.type");
            id = rs.getInt("a.appointmentId");
            Appointment apt = new Appointment(id, customer, type, lStart, lEnd);
            appointmentList.add(apt);
            
            
        }
        
        consultantTable.getItems().setAll(appointmentList);
        
        
    }
    
    
    // Searches the names of all the contacts/consultants and puts them into the search box.
    private void fillNameList() throws SQLException {
        PreparedStatement search = conn.prepareStatement("SELECT DISTINCT contact FROM appointment");
        ResultSet names = search.executeQuery();
        
        while (names.next()){
            String n = names.getString("contact");
            nameList.getItems().add(n);
            
        }
        
        
    }
    
    
    // Fill in the types of appointments within that report.
    private void fillTypes() throws SQLException{
        ArrayList<String> types = new ArrayList<String>();
        types.add("Status Update");
        types.add("Information Sharing");
        types.add("Decision Planning");
        types.add("Problem Solving");
        types.add("Innovation");
        types.add("Team Building");
        
        
        ObservableList<Report> typeList = FXCollections.observableArrayList();

        for (String t : types){
            PreparedStatement count =  conn.prepareStatement("SELECT count(*) FROM appointment "
                    + "WHERE type = '" + t + "'");
            ResultSet rs = count.executeQuery();
            int c = 0;
            while (rs.next()){
                c = rs.getInt("count(*)");
            }
            typeList.add(new Report (t, c));
        }
        typeTable.getItems().setAll(typeList);
        
    }
    
    // Add the locations to the location report (3rd report) and then get their count
    private void fillLocations() throws SQLException{
        String [] locations = {"Phoenix, Arizona", "New York, New York", "London, England"};
        
        
        ObservableList<Report> locList = FXCollections.observableArrayList();

        for (String loc : locations){
            PreparedStatement count =  conn.prepareStatement("SELECT count(*) FROM appointment "
                    + "WHERE location = '" + loc + "'");
            ResultSet rs = count.executeQuery();
            int c = 0;
            while (rs.next()){
                c = rs.getInt("count(*)");
            }
            locList.add(new Report (loc, c));
        }
        locationTable.getItems().setAll(locList);
        
    }
        
        
        
    
}
