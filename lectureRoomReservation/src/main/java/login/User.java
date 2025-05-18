package login;

import java.io.Serializable;

public class User implements Serializable {
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
    public String getRole() { return role; }
    public String getName() { return name; }
}
