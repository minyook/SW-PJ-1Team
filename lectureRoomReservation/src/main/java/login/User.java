/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;

/**
 *
 * @author 00rya
 */
public class User {
    private String username;
    private String password;
    private String role;
    private String name;
    
    public User(String username, String password, String role, String name) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() {return role;}
    public String getName() {return name;}
            
    
}
