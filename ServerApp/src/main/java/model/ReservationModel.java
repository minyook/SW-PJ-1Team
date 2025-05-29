package model;

import common.Reservation;

import java.io.*;
import java.util.*;

public class ReservationModel {
    private static final String DEFAULT_RESOURCE = "reservation_data.txt";
    private final String resourcePath;
    private final List<Reservation> reservationList = new ArrayList<>();

    /**
     * 운영용 생성자: classpath 상의 reservation_data.txt를 읽어들입니다.
     */
    public ReservationModel() throws IOException {
        this(DEFAULT_RESOURCE);
    }

    /**
     * 테스트용 생성자: resourcePath에 지정된 이름의 classpath 리소스를 읽습니다.
     *
     * @param resourcePath src/test/resources 아래에 있는 파일 이름 (예: "reservation_data.txt")
     */
    public ReservationModel(String resourcePath) throws IOException {
        this.resourcePath = resourcePath;
        loadFromResource();
    }

    /**
     * 오직 classpath 리소스만 읽어들여 reservationList를 채웁니다.
     * 파일 시스템의 변경은 절대 발생하지 않습니다.
     */
    private void loadFromResource() throws IOException {
        reservationList.clear();
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("❌ 리소스 파일을 찾을 수 없습니다: " + resourcePath);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 6) {
                    reservationList.add(new Reservation(
                        tokens[0], // reservationId
                        tokens[1], // date
                        tokens[2], // time
                        tokens[3], // roomNumber
                        tokens[4], // userName
                        tokens[5]  // status
                    ));
                } else {
                    System.err.println("❌ 잘못된 데이터 형식: " + line);
                }
            }
        }
    }

    /**
     * 전체 예약 목록을 반환합니다.
     * @return 읽기 전용으로 복사된 List<Reservation>
     */
    public List<Reservation> listAll() {
        return new ArrayList<>(reservationList);
    }

    /**
     * 특정 사용자 이름의 예약만 필터링해서 반환합니다.
     * @param username 사용자 이름
     * @return 해당 사용자의 예약 리스트
     */
    public List<Reservation> getByUser(String username) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservationList) {
            if (r.getUserName().equals(username)) {
                result.add(r);
            }
        }
        return result;
    }

    // 아래 메서드들은 모두 읽기 전용이므로 UnsupportedOperationException을 던집니다.

    public void create(Reservation r) {
        throw new UnsupportedOperationException("쓰기 작업은 지원되지 않습니다 (읽기 전용 모델).");
    }

    public void update(int index, Reservation updated) {
        throw new UnsupportedOperationException("쓰기 작업은 지원되지 않습니다 (읽기 전용 모델).");
    }

    public void delete(int index) {
        throw new UnsupportedOperationException("쓰기 작업은 지원되지 않습니다 (읽기 전용 모델).");
    }
}
