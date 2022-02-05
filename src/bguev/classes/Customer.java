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
public class Customer {
    private int id;
    private String name;
    private String address;
    private String phone;
    
    public Customer (){
        
    }
    
    public Customer (int id, String name, String address, String phone){
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
      
    }
    public Customer (int id, String name){
        this.id = id;
        this.name = name;
        
    }
    
    public int getId(){
        return this.id;
    }
    
    public void setId(int id){
        this.id = id;
    }
   
    public String getName(){
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getAddress(){
        return this.address;
    }
    
    public void setAddress(String address){
        this.address = address;
    }
    
    public String getPhone(){
        return this.phone;
    }
    
    public void setPhone(String phone){
        this.phone = phone;
    }
    
    
    
    
    
    
    
    
}
