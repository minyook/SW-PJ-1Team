
package timetable;
import java.util.*;
import java.io.*;
/**
 *
 * @author rbcks
 */
public class TimeTableModel {
    private final String[] days = { "월", "화", "수", "목", "금" };
    private final String[] times = {
        "09:00~09:50", "10:00~10:50", "11:00~11:50", "12:00~12:50",
        "13:00~13:50", "14:00~14:50", "15:00~15:50", "16:00~16:50"
    };

    public Map<String, List<String>> generateWeeklySchedule(int month, int week, String roomNumber) {
        Map<String, List<String>> schedule = initEmptySchedule();

        loadFixedSchedule(schedule, roomNumber);
        loadReservationSchedule(schedule, month, week, roomNumber);

        return schedule;
    }

    private Map<String, List<String>> initEmptySchedule() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (String day : days) {
            map.put(day, new ArrayList<>(Collections.nCopies(times.length, "")));
        }
        return map;
    }

    private void loadFixedSchedule(Map<String, List<String>> schedule, String roomNumber) {
        String roomNumberOnly = roomNumber.replaceAll("[^0-9]", "");
        String fileName = "src/main/resources/schedule_" + roomNumberOnly + ".txt";
        

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) continue;

                String day = parts[0].trim();
                String time = parts[1].trim();
                String subject = parts[2].trim();
                String professor = parts[3].trim();

                int timeIndex = Arrays.asList(times).indexOf(time);
                if (timeIndex != -1 && schedule.containsKey(day)) {
                    schedule.get(day).set(timeIndex, subject + " (" + professor + ")");
                }
            }
        } catch (IOException e) {
            System.err.println("고정 시간표 파일 읽기 실패: " + e.getMessage());
        }
    }

private void loadReservationSchedule(Map<String, List<String>> schedule, int month, int week, String roomNumber) {
    try (
        InputStream is = getClass().getClassLoader().getResourceAsStream("reservation_data.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
    ) {
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length != 5) continue;

            String dateStr = parts[0].trim(); // YYYY-MM-DD
            String time = parts[1].trim();
            String room = parts[2].trim();
            String user = parts[3].trim();
            String status = parts[4].trim();

            // 강의실 번호에서 숫자만 추출
            String roomOnly = roomNumber.replaceAll("[^0-9]", "");
            if (!room.equals(roomOnly) && !room.equals(roomNumber)) continue;

            int day = Integer.parseInt(dateStr.split("-")[2]);
            int calculatedWeek = (day - 1) / 7 + 1;
            if (calculatedWeek != week) continue;

            Calendar cal = Calendar.getInstance();
            cal.set(2025, month - 1, day);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            String korDay = switch (dayOfWeek) {
                case Calendar.MONDAY -> "월";
                case Calendar.TUESDAY -> "화";
                case Calendar.WEDNESDAY -> "수";
                case Calendar.THURSDAY -> "목";
                case Calendar.FRIDAY -> "금";
                default -> null;
            };

            // ✅ 콘솔 출력 확인용
            System.out.println("읽은 예약: " + dateStr + " / 주차: " + calculatedWeek + " / 요일: " + korDay + " / 시간: " + time + " / 상태: " + status);

            if (korDay == null || status.equals("거절")) continue;

            int timeIndex = Arrays.asList(times).indexOf(time);
            if (timeIndex != -1 && schedule.containsKey(korDay)) {
                String label = (status.equals("예약") ? "예약" : "예약대기") + "(" + user + ")";
                if (schedule.get(korDay).get(timeIndex).isEmpty()) {
                    schedule.get(korDay).set(timeIndex, label);
                }
            }
        }

    } catch (Exception e) {
        System.err.println("예약 파일 읽기 실패: " + e.getMessage());
    }
}

}
