package model;

import common.Reservation;

import java.io.*;
import java.util.*;

public class ReservationModel {
    private static final String DATA_FILE = "reservation_data.txt"; // 리소스 기준
    private final List<Reservation> reservationList = new ArrayList<>();

    public ReservationModel() throws IOException {
        load();
    }

    private void load() throws IOException {
        reservationList.clear();

        InputStream is = getClass().getClassLoader().getResourceAsStream(DATA_FILE);
        if (is == null) {
            System.err.println("❌ 리소스 파일을 찾을 수 없습니다: " + DATA_FILE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 5) {
                    Reservation r = new Reservation(
                        tokens[0], // date
                        tokens[1], // time
                        tokens[2], // roomNumber
                        tokens[3], // userId
                        tokens[4],
                        tokens[5]// status
                    );
                    reservationList.add(r);
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

    // ❌ 아래는 읽기 전용 정책에 따라 비활성화 (쓰기 불가)
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
