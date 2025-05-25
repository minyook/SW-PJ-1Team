package model;

import common.Reservation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AdminReservationModel {
    private static final String DATA_FILE = "resources/reservation_data.txt";
    private final List<Reservation> reservations = new ArrayList<>();

    public AdminReservationModel() throws IOException {
        load();
    }

    private void load() throws IOException {
        reservations.clear();
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) Files.createFile(path);

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] tokens = line.split(",");
            if (tokens.length == 5) {
                reservations.add(new Reservation(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]));
            }
        }
    }

    public List<Reservation> listAll() throws IOException {
        load();
        return new ArrayList<>(reservations);
    }

    public void updateStatus(int index, String status) throws IOException {
        if (index >= 0 && index < reservations.size()) {
            Reservation r = reservations.get(index);
            r.setStatus(status);
            save();
        }
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Reservation r : reservations) {
            lines.add(String.join(",",
                r.getDate(),
                r.getTime(),
                r.getRoomNumber(),
                r.getUserName() ,
                r.getStatus()
            ));
        }
        Files.write(Paths.get(DATA_FILE), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}