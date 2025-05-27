package model;

import common.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UserModel {
    private static final String DATA_FILE = "storage/user.txt";
    private final List<User> userList = new ArrayList<>();

    public UserModel() throws IOException {
        load();
    }

    // 파일에서 사용자 목록 읽기
    private void load() throws IOException {
    userList.clear();
    Path path = Paths.get(DATA_FILE);
    if (!Files.exists(path)) Files.createFile(path);

    List<String> lines = Files.readAllLines(path);
    for (String line : lines) {
        String[] tokens = line.split(",");
        if (tokens.length == 4) {
            // username, password, role, name
            userList.add(new User(tokens[0], tokens[1], tokens[2], tokens[3]));
        } else {
            System.err.println("불러오기 실패: 형식이 잘못된 줄 -> " + line);
        }
    }
}

    // 전체 사용자 목록
    public List<User> listAll() throws IOException {
        load();
        return new ArrayList<>(userList);
    }

    // 사용자 등록
    public void register(User user) throws IOException {
        userList.add(user);
        save();
    }
    
    public void reload() throws IOException {
    load();
}

    // 사용자 중복 확인
    public boolean checkUserExists(String id) throws IOException {
        load();
        return userList.stream().anyMatch(u -> u.getUsername().equals(id));
    }

    // 로그인 시 사용자 정보 반환
    public User findUser(String id, String pw) throws IOException {
        load();
        for (User u : userList) {
            if (u.getUsername().equals(id) && u.getPassword().equals(pw)) {
                return u;
            }
        }
        return null;
    }

    // 저장
    private void save() throws IOException {
    List<String> lines = new ArrayList<>();
    for (User u : userList) {
        lines.add(String.join(",", u.getUsername(), u.getPassword(), u.getRole(), u.getName()));
    }
    Files.write(Paths.get(DATA_FILE), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}