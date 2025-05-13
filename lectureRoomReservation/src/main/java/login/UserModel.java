/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;

/**
 *
 * @author 00rya
 */

public class UserModel {

    
    private List<User> users;

    public UserModel() {
        loadUsersFromFile(); // 텍스트 파일에서 유저 리스트 로드
    }

    private void loadUsersFromFile() {
        users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream("user.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    String name = parts[3].trim();
                    users.add(new User(username, password, role, name));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}