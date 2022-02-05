/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bguev.classes;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 *
 * @author coolg
 */
public class Appointment {
    
    private int appointmentId;
    private String customer;
    private String type;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private LocalDate date;
        
    public Appointment (){
    }
    

    
    public Appointment (int id, String customer, String type, ZonedDateTime start, ZonedDateTime end){
        this.appointmentId = id;
        this.customer = customer;
        this.type = type;
        this.start = start;
        this.end = end;
        this.date = start.toLocalDate();
    }
    

    
    
     
    public int getAppointmentId(){
        return this.appointmentId;
    }
    
    public void setAppointmentId(int appointmentId){
        this.appointmentId = appointmentId;
    }
        
    public String getCustomer(){
        return this.customer;
    }
    
    public void setCustomer(String customer){
        this.customer = customer;
    }    
    public String getStart(){
        return start.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    public LocalDate getDate(){
        return date;
    }
    
    public void setStart(ZonedDateTime start){
        this.start = start;
    }    
    public String getEnd(){
        return end.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    
    public void setEnd(ZonedDateTime end){
        this.end = end;
    }
    public String getType (){
        return this.type;
    }
        
        
        
}
