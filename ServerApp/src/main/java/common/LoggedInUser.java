/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

/**
 *
 * @author rbcks
 */
public class LoggedInUser {
     private final String userId;
    private final String name;
    private final String role;

    public LoggedInUser(String userId, String name, String role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }
    
    public String getUserId() { return userId; }
    public String getName()   { return name; }
    public String getRole()   { return role; }
}
