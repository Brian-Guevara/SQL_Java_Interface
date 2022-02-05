/**
 *
 * Brian Guevara
 * WGU ID: 001003681
 */
package bguev.view;

import bguev.util.DBConnect;
import bguev.classes.User;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class LoginMenuController implements Initializable {
    @FXML
    private Label title;
    @FXML
    private Label msg_label;
 
    @FXML
    private Label username_text;
            
            
            
    @FXML
    private Label password_text;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button signInBtn;
                
    @FXML
    private TextField username;
    
    @FXML
    private TextField password;
    
    User loggedInUser = new User();
    ResourceBundle rb = ResourceBundle.getBundle("resources/login", Locale.getDefault());

    
    Connection conn;
            
    @Override
    public void initialize(URL url, ResourceBundle j) {
        DBConnect.init();
        conn = DBConnect.getConn();
        
        // This is where we read from our resource bundle for our different locales
        title.setText(rb.getString("title"));
        username_text.setText(rb.getString("username"));
        password_text.setText(rb.getString("password"));
        cancelBtn.setText(rb.getString("cancel"));
        signInBtn.setText(rb.getString("signin"));
        msg_label.setText(rb.getString("signinMsg"));
        checkTime();


    }    
    
    
    // When the sign in button is pressed
    @FXML
    public void sign_in_pressed (ActionEvent e) throws IOException {
        // Check if the input is within the user table in our database.
        try {
            PreparedStatement select = conn.prepareStatement ("SELECT userId, username, password FROM user");
            ResultSet result = select.executeQuery();  
            
            while (result.next()){
                if (result.getString("username").toLowerCase().equals(username.getText().toLowerCase())
                        && result.getString("password").equals (password.getText()))
                {
                // In the case of a successful login, create a user object and load the main menu
                loggedInUser.setUserID(result.getInt("userId"));
                loggedInUser.setUsername(result.getString("username"));
                loggedInUser.setPassword(result.getString("password"));
                    try {
            
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                        Parent MainMenuFXML = loader.load();

                        Scene nextScene = new Scene(MainMenuFXML);

                        MainMenuController mainMenuController = loader.getController();
                        
                        mainMenuController.setUser(loggedInUser);
                        
                        Stage stage = new Stage();

                        stage.initOwner(title.getScene().getWindow());

                        stage.setScene(nextScene);


                        // Hide the current window
                        title.getScene().getWindow().hide();
                        stage.setTitle("Main Menu");
                        stage.show();

                    } catch (RuntimeException f) {
                    }
                }
                
                // Print that the info is wrong if the user cannot be found
                else {
                    msg_label.setText(rb.getString("wrongInf"));
                }
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(LoginMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    // Check to see if we are signing in during work hours 9-5.
    public void checkTime(){
        LocalTime now = LocalTime.now(ZoneId.of(ZoneId.systemDefault().toString()));
        LocalTime s = LocalTime.of(9, 0);
        LocalTime e = LocalTime.of(17, 0);
        
        if (now.isBefore(s) || now.isAfter(e)){
            Alert a = new Alert(AlertType.WARNING);
            a.setContentText("You are attempting to sign in during off hours.");
            a.show();
            
        }       
        
    }
    
    

    
    // If we press the cancel button then close the database and exit the program.
    @FXML
    public void cancel_pressed (ActionEvent e) throws SQLException{
        DBConnect.closeConn();
        System.exit(0);
    }
}
