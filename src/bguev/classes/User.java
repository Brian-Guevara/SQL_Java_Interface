/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bguev.classes;

/**
 *
 * @author coolg
 */
public class User {
    private int userID;
    private String username;
    private String password;
    
    public User (){
        
    }
    
    
    public User (int userID, String username, String password){
        this.userID = userID;
        this.username = username;
        this.password = password;
    }
    
    public int getUserID() {
        return this.userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }   
    
    
}
