// src/main/java/model/AdminReservationModel.java
package model;

import common.Reservation;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class AdminReservationModel {
    private static final String DEFAULT_DATA_FILE = "storage/reservation_data.txt";
    private final String dataFile;
    private final List<Reservation> reservations = new ArrayList<>();

    // 운영용
    public AdminReservationModel() throws IOException {
        this(DEFAULT_DATA_FILE);
    }

    // 테스트용 등 경로 지정 가능
    public AdminReservationModel(String dataFile) throws IOException {
        this.dataFile = dataFile;
        load();
    }

    private void load() throws IOException {
        reservations.clear();
        Path path = Paths.get(dataFile);
        // 디렉터리 생성
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        for (String line : Files.readAllLines(path)) {
            String[] tokens = line.split(",");
            // reservationId, date, time, room, user, status
            if (tokens.length >= 6) {
                reservations.add(new Reservation(
                    tokens[0], tokens[1], tokens[2],
                    tokens[3], tokens[4], tokens[5]
                ));
            } else {
                System.err.println("❌ 잘못된 라인 형식: " + line);
            }
        }
    }

    public List<Reservation> listAll() throws IOException {
        load();
        return new ArrayList<>(reservations);
    }

    public void updateStatus(int index, String status) throws IOException {
        if (index >= 0 && index < reservations.size()) {
            reservations.get(index).setStatus(status);
            save();
        }
    }

    private void save() throws IOException {
        Path path = Paths.get(dataFile);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        List<String> lines = new ArrayList<>();
        for (Reservation r : reservations) {
            lines.add(String.join(",",
                r.getReservationId(),
                r.getDate(),
                r.getTime(),
                r.getRoomNumber(),
                r.getUserName(),
                r.getStatus()
            ));
        }
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
