/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.model;

/**
 *
 * @author limmi
 */
import management.model.Reservation;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class AdminModel {
    // 프로젝트 루트 기준 src/main/resources 폴더를 가리킵니다
    private static final Path RESOURCES = Paths.get(
        System.getProperty("user.dir"),
        "src", "main", "resources"
    );
    private static final Path ROOMS_FILE = RESOURCES.resolve("rooms.txt");
    private static final Path RES_FILE   = RESOURCES.resolve("reservations.txt");

    // 강의실 목록 읽기
    public List<Room> loadRooms() throws IOException {
    ensureFilesExist();

    return Files.readAllLines(ROOMS_FILE)
        .stream()
        // 1) 공백 라인 제거
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        // 2) 최소한 id와 blocked flag를 둘 다 가진 라인만
        .filter(line -> line.contains(","))
        // 3) split limit=2 로 안전하게 분할
        .map(line -> {
            String[] parts = line.split(",", 2);
            String id      = parts[0].trim();
            boolean blocked = Boolean.parseBoolean(parts[1].trim());
            return new Room(id, blocked);
        })
        .toList();
}

    // 강의실 목록 저장
    public void saveRooms(List<Room> rooms) throws IOException {
        ensureFilesExist();
        List<String> lines = rooms.stream()
            .map(r -> String.join(",",
                r.getId(),
                String.valueOf(r.isBlocked())
            ))
            .toList();
        Files.write(ROOMS_FILE, lines,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    // 예약 목록 읽기
    public List<Reservation> loadReservations() throws IOException {
    ensureFilesExist();

    return Files.readAllLines(RES_FILE)
        .stream()
        .map(String::trim)
        .filter(line -> !line.isEmpty() && line.contains(","))
        .map(line -> {
            String[] f = line.split(",", 5);
            // f[0]=id, f[1]=room, f[2]=user, f[3]=status, f[4]=reason(선택)
            return new Reservation(
                f[0].trim(),
                f[1].trim(),
                f[2].trim(),
                f[3].trim(),
                (f.length > 4 ? f[4].trim() : "")
            );
        })
        .toList();
}

    // 예약 목록 저장
    public void saveReservations(List<Reservation> ress) throws IOException {
        ensureFilesExist();
        List<String> lines = ress.stream()
            .map(r -> String.join(",",
                r.getId(),
                r.getRoomId(),
                r.getUser(),
                r.getStatus(),
                r.getReason() != null ? r.getReason() : ""
            ))
            .toList();
        Files.write(RES_FILE, lines,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    // resources 폴더·파일이 없으면 만들어 줍니다
    private void ensureFilesExist() throws IOException {
        if (!Files.exists(RESOURCES)) {
            Files.createDirectories(RESOURCES);
        }
        if (!Files.exists(ROOMS_FILE)) {
            Files.createFile(ROOMS_FILE);
        }
        if (!Files.exists(RES_FILE)) {
            Files.createFile(RES_FILE);
        }
    }
}
