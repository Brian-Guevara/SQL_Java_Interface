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
public class Report {
    
    private String name;
    private int count;
    
    
    public Report (String n, int c){
        this.name = n;
        this.count = c;
    }
    
    
    public String getName(){
        return this.name;
    }
    public int getCount(){
        return this.count;
    }
    
    
    
}
