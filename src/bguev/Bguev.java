

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import bguev.view.AppointmentAddScreenController;
import bguev.view.CalendarScreenController;
import bguev.view.LoginMenuController;
import bguev.view.MainMenuController;
import bguev.view.ReportScreenController;
import bguev.util.DBConnect;
import java.sql.Connection;


/**
 *
 * @author coolg
 */
public class Bguev extends Application {
    
    private static Connection connection;
    
    @Override
    public void start(Stage stage) throws Exception {
         
        
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("bguev/view/LoginMenu.fxml"));
        Scene scene = new Scene(root);
        
        stage.setTitle("Log In Screen");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        
    }
    
}
