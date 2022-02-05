/**
 *
 * Brian Guevara
 * WGU ID: 001003681
 */
package bguev.view;

import bguev.classes.User;
import bguev.util.DBConnect;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class MainMenuController implements Initializable {

    @FXML
    private Label title;

    @FXML
    private Label userLabel;
    private User user;

    private Connection conn;

    // This main menu does not need much to initialize. All that was done in the preceding page (login)
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnect.getConn();
        try {
            checkAppointments();
        } catch (SQLException ex) {
            
        }
    }

    
    // The following methods will load the page of the button they refer to
    @FXML
    public void calendar_press(ActionEvent e) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CalendarScreen.fxml"));
            Parent CalendarScreenFXML = loader.load();

            Scene nextScene = new Scene(CalendarScreenFXML);

            CalendarScreenController controller = loader.getController();
            Stage stage = new Stage();

            stage.initOwner(title.getScene().getWindow());
            stage.setTitle("Calendar");
            stage.setScene(nextScene);
            controller.loadUser(user);

            // Hide the current window            
            stage.show();

        } catch (RuntimeException f) {
        }
    }

    @FXML
    public void reports_press(ActionEvent e) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportScreen.fxml"));
            Parent ReportScreenFXML = loader.load();

            Scene nextScene = new Scene(ReportScreenFXML);

            ReportScreenController reportScreenController = loader.getController();
            Stage stage = new Stage();

            stage.initOwner(title.getScene().getWindow());

            stage.setScene(nextScene);

            // Hide the current window
            stage.setTitle("Reports");
            stage.show();

        } catch (RuntimeException f) {
        }
    }

    @FXML
    public void customers_pressed(ActionEvent e) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerEdit.fxml"));
            Parent CustomerEditFXML = loader.load();

            Scene nextScene = new Scene(CustomerEditFXML);

            CustomerEditController CustomerEditController = loader.getController();
            Stage stage = new Stage();

            CustomerEditController.setUser(user);

            stage.initOwner(title.getScene().getWindow());

            stage.setScene(nextScene);

            // Hide the current window
            stage.setTitle("Customer Records");
            stage.show();

        } catch (RuntimeException f) {
        }
    }

    
    // This method is used in the login page to set the user of the current session.
    public void setUser(User x) {
        user = x;
        userLabel.setText(user.getUsername());
        String savestr = "logins.txt"; 
        File f = new File(savestr);

        PrintWriter out = null;
        if ( f.exists() && !f.isDirectory() ) {
            try {
                out = new PrintWriter(new FileOutputStream(new File(savestr), true));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoginMenuController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            try {
                out = new PrintWriter(savestr);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoginMenuController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        out.append(user.getUsername() + " has logged in at " + now.toString() + "\n");
        out.close();
    }

    
    // When pressing exit, disconnect from the database
    @FXML
    public void exit() {
        DBConnect.closeConn();
        System.exit(0);

    }
    
    
    private void checkAppointments() throws SQLException{
        PreparedStatement srch = conn.prepareStatement("SELECT * FROM appointment " +
        "WHERE current_timestamp() <= start AND start <= date_add(current_timestamp(), INTERVAL 15 MINUTE)");
        ResultSet rs = srch.executeQuery();
        int count = 0;
        while (rs.next()){
            count += 1;
        }
        
        if (count != 0){
            Alert a = new Alert(AlertType.WARNING);
            a.setContentText("There's an appointment in 15 minutes!");
            a.show();
            
        }
        
        
    }

}
