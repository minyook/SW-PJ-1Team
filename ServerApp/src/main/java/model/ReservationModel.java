package model;

import common.Reservation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ReservationModel {
    private final String path;
    private final List<Reservation> reservationList = new ArrayList<>();

    // 운영용 생성자
    public ReservationModel() throws IOException {
        this("reservation_data.txt");
    }

    // 테스트용 생성자 (클래스패스 리소스 or 파일 경로)
    public ReservationModel(String path) throws IOException {
        this.path = path;
        load();
    }

    private void load() throws IOException {
        reservationList.clear();

        // 1) 먼저 클래스패스 리소스로 시도
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                parseLines(reader);
            }
            return;
        }

        // 2) 클래스패스 리소스가 없으면 파일 시스템 경로로 시도
        Path p = Paths.get(path);
        if (Files.exists(p)) {
            try (BufferedReader reader = Files.newBufferedReader(p)) {
                parseLines(reader);
            }
        } else {
            System.err.println("❌ 파일을 찾을 수 없습니다: " + path);
        }
    }

    // 공통 파싱 로직
    private void parseLines(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length == 6) {
                reservationList.add(new Reservation(
                    tokens[0], tokens[1], tokens[2],
                    tokens[3], tokens[4], tokens[5]
                ));
            } else {
                System.err.println("❌ 잘못된 데이터 형식: " + line);
            }
        }
    }

    public List<Reservation> listAll() {
        return new ArrayList<>(reservationList);
    }

    public List<Reservation> getByUser(String username) {
        List<Reservation> r = new ArrayList<>();
        for (Reservation e : reservationList) {
            if (e.getUserName().equals(username)) r.add(e);
        }
        return r;
    }

    public void create(Reservation r) {
        throw new UnsupportedOperationException("읽기 전용 모델");
    }
    public void update(int i, Reservation r) {
        throw new UnsupportedOperationException("읽기 전용 모델");
    }
    public void delete(int i) {
        throw new UnsupportedOperationException("읽기 전용 모델");
    }
}
