package model;

import common.Room;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RoomModel {
    private static final String DEFAULT_DATA_FILE = "resources/room_data.txt";
    private final String dataFile;
    private final List<Room> roomList = new ArrayList<>();

    // 운영용 생성자
    public RoomModel() throws IOException {
        this(DEFAULT_DATA_FILE);
    }

    // ★ 테스트용 생성자: 경로를 외부에서 주입
    public RoomModel(String dataFile) throws IOException {
        this.dataFile = dataFile;
        load();
    }

    private void load() throws IOException {
        roomList.clear();
        Path path = Paths.get(dataFile);
        if (!Files.exists(path)) Files.createFile(path);

        for (String line : Files.readAllLines(path)) {
            String[] tokens = line.split(",");
            if (tokens.length >= 1) {
                Room r = new Room(tokens[0]);
                roomList.add(r);
            }
        }
    }

    public List<Room> listAll() throws IOException {
        load();
        return new ArrayList<>(roomList);
    }

    public void create(Room r) throws IOException {
        roomList.add(r);
        save();
    }

    public void delete(int index) throws IOException {
        if (index >= 0 && index < roomList.size()) {
            roomList.remove(index);
            save();
        }
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Room r : roomList) {
            lines.add(r.getRoomId());
        }
        Files.write(Paths.get(dataFile), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
