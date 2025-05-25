package model;

import common.Room;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RoomModel {
    private static final String DATA_FILE = "resources/room_data.txt";
    private final List<Room> roomList = new ArrayList<>();

    public RoomModel() throws IOException {
        load();
    }

    private void load() throws IOException {
        roomList.clear();
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) Files.createFile(path);

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] tokens = line.split(",");
            if (tokens.length >= 1) {
                Room r = new Room(tokens[0]); // 단순히 RoomID만 저장된 경우
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
        Files.write(Paths.get(DATA_FILE), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}