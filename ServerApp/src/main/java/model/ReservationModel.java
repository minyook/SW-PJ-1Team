package model;

import common.Reservation;
import java.io.*;
import java.util.*;

public class ReservationModel {
    private static final String DEFAULT_FILE = "reservation_data.txt";
    private final List<Reservation> reservationList = new ArrayList<>();

    public ReservationModel() throws IOException {
        load(DEFAULT_FILE);
    }

    public ReservationModel(String path) throws IOException {
        load(path);
    }

    private void load(String path) throws IOException {
        reservationList.clear();

        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            System.err.println("❌ 리소스 파일을 찾을 수 없습니다: " + path);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 6) {
                    Reservation r = new Reservation(
                        tokens[0], tokens[1], tokens[2],
                        tokens[3], tokens[4], tokens[5]
                    );
                    reservationList.add(r);
                } else {
                    System.err.println("❌ 잘못된 데이터 형식: " + line);
                }
            }
        }
    }

    public List<Reservation> listAll() {
        return new ArrayList<>(reservationList);
    }

    public List<Reservation> getByUser(String username) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservationList) {
            if (r.getUserName().equals(username)) {
                result.add(r);
            }
        }
        return result;
    }

    public void create(Reservation r) throws IOException {
        throw new UnsupportedOperationException("쓰기 작업은 지원되지 않습니다 (읽기 전용)");
    }

    public void update(int index, Reservation updated) throws IOException {
        throw new UnsupportedOperationException("쓰기 작업은 지원되지 않습니다 (읽기 전용)");
    }

    public void delete(int index) throws IOException {
        throw new UnsupportedOperationException("쓰기 작업은 지원되지 않습니다 (읽기 전용)");
    }
}
