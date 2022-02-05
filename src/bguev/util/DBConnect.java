/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bguev.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static javafx.application.Application.launch;
/**
 *
 * @author coolg
 */
public class DBConnect {
    private static Connection connDB;
    
    public DBConnect(){}
    
  
    public static void init(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
   
            connDB = DriverManager.getConnection("jdbc:mysql://wgudb.ucertify.com:3306/U05tXH", "U05tXH", "53688606232");

        }catch (ClassNotFoundException ce){
            System.out.println("Cannot find the right class.  Did you remember to add the mysql library to your Run Configuration?");
        }catch(Exception e){
        }
    }
        public static Connection getConn(){
    
        return connDB;
    }
    
    //Closes connections
    public static void closeConn(){
        try{
            connDB.close();
        }catch (SQLException e){
        }
    }
    
    
    
}
