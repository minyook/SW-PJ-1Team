
package reservation;

/**
 *
 * @author rbcks
 */
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ReservationModel {

    private static final List<String> TIME_SLOTS = Arrays.asList(
        "09:00~09:50", "10:00~10:50", "11:00~11:50",
        "13:00~13:50", "14:00~14:50", "15:00~15:50",
        "16:00~16:50", "17:00~17:50"
    );

    public List<RoomStatus> loadTimetable(String date, String roomNumber) {
        List<RoomStatus> result = new ArrayList<>();

        // 1. 초기 상태: 전부 "비어 있음"
        for (String time : TIME_SLOTS) {
            result.add(new RoomStatus(time, "비어 있음"));
        }

        // 2. 요일 구하기
        String dayOfWeek = getDayOfWeek(date); // "월", "화" 등

        // 3. 수업 시간표 반영
        String schedulePath = "src/main/resources/schedule_" + roomNumber + ".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(schedulePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].equals(dayOfWeek)) {
                    String time = parts[1];
                    String subject = parts[2];
                    String professor = parts[3];
                    for (RoomStatus rs : result) {
                        if (rs.getTimeSlot().equals(time)) {
                            rs.setStatus(subject + "(" + professor + ")");
                        }
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("수업 시간표 파일 오류: " + e.getMessage());
        }

        // 4. 예약 상태 반영
        String reservationPath = "src/main/resources/reservation_data.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(reservationPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                
                String[] parts = line.split(",");
                

                if (parts.length >= 5 
                    && parts[0].trim().equals(date) 
                    && parts[2].trim().equals(roomNumber)) {

                    String time = parts[1].trim();
                    String status = parts[4].trim();

                    if (!status.equals("거절")) {
                        for (RoomStatus rs : result) {
                            if (rs.getTimeSlot().equals(time) && rs.getStatus().equals("비어 있음")) {
                                rs.setStatus(status);  // "예약" or "예약 대기"
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("예약 파일 오류: " + e.getMessage());
        }

        return result;
    }

    private String getDayOfWeek(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        DayOfWeek day = date.getDayOfWeek();
        switch (day) {
            case MONDAY: return "월";
            case TUESDAY: return "화";
            case WEDNESDAY: return "수";
            case THURSDAY: return "목";
            case FRIDAY: return "금";
            default: return "";
        }
    }
    //예약 상태
    public enum ReservationResult {
        SUCCESS,         // 예약 저장 성공
        TIME_OCCUPIED,   // 이미 수업/예약된 시간
        NOT_SELECTED,    // 아무 행도 선택되지 않음
        ERROR            // 저장 실패 등 알 수 없는 오류
    }
    
    //빈 강의실 체크
    public boolean checkAvailability(String date, String time, String room) {
        List<RoomStatus> statusList = loadTimetable(date, room);
        for (RoomStatus rs : statusList) {
            if (rs.getTimeSlot().equals(time)) {
                return rs.getStatus().equals("비어 있음");
            }
        }
        return false;
    }
    
    //예약 내역 텍스트 파일에 저장
    public boolean saveReservation(String date, String time, String room, String name, String status) {
        String path = "src/main/resources/reservation_data.txt";
        String line = String.join(",", date, time, room, name, status);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("예약 저장 실패: " + e.getMessage());
            return false;
            }
    }

}