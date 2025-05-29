package model;

import common.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UserModel {
    private final String dataFile;
    private final List<User> userList = new ArrayList<>();

    // 운영용 기본 생성자
    public UserModel() throws IOException {
        this("storage/user.txt");
    }

    // 테스트 전용 생성자
    public UserModel(String dataFile) throws IOException {
        this.dataFile = dataFile;
        load();
    }

    private void load() throws IOException {
        userList.clear();
        Path path = Paths.get(dataFile);
        if (!Files.exists(path)) Files.createFile(path);

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] tokens = line.split(",");
            if (tokens.length == 4) {
                userList.add(new User(tokens[0], tokens[1], tokens[2], tokens[3]));
            } else {
                System.err.println("불러오기 실패: 형식이 잘못된 줄 -> " + line);
            }
        }
    }
    public void reload() throws IOException {
        load();
    }


    public List<User> listAll() throws IOException {
        load();
        return new ArrayList<>(userList);
    }

    public void register(User user) throws IOException {
        userList.add(user);
        save();
    }

    public boolean checkUserExists(String id) throws IOException {
        load();
        return userList.stream().anyMatch(u -> u.getUsername().equals(id));
    }

    public User findUser(String id, String pw) throws IOException {
        load();
        for (User u : userList) {
            if (u.getUsername().equals(id) && u.getPassword().equals(pw)) {
                return u;
            }
        }
        return null;
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : userList) {
            lines.add(String.join(",", u.getUsername(), u.getPassword(), u.getRole(), u.getName()));
        }
        Files.write(Paths.get(dataFile), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
