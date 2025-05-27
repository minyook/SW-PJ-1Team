package controller;

import client.ClientMain;
import client.SocketClient;
import common.Message;
import common.RequestType;
import common.RoomStatus;
import common.ScheduleEntry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeTableController {

    public Map<String, List<String>> getWeeklySchedule(int month, int week, String roomId) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("month", String.valueOf(month));
            payload.put("week", String.valueOf(week));
            payload.put("roomId", roomId);

            Message req = new Message();
            req.setDomain("schedule");
            req.setType(RequestType.LIST);
            req.setPayload(payload);

            Message res = SocketClient.send(req);

            if (res.getMessage() == null) {
                return (Map<String, List<String>>) res.getPayload();
            }
        } catch (Exception e) {
            System.out.println("서버 오류: " + e.getMessage());
        }

        return Map.of(); // 실패 시 빈 맵
    }

    @SuppressWarnings("unchecked")
    public List<String> loadScheduleFile(String room) {
        try {
            Message req = new Message();
            req.setDomain("schedule");
            req.setType(RequestType.LOAD_SCHEDULE_FILE);
            req.setPayload(room);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            if (res.getMessage() != null) {
                throw new IOException(res.getMessage());
            }
            return (List<String>) res.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ScheduleEntry> getSchedule() {
        try {
            Message req = new Message();
            req.setDomain("timetable");
            req.setType(RequestType.LIST);

            Message res = SocketClient.send(req);
            if (res.getMessage() == null) {
                return (List<ScheduleEntry>) res.getList();
            } else {
                System.out.println("시간표 조회 실패: " + res.getMessage());
            }
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * 주어진 (ISO) 날짜와 강의실에 대해 서버에서 RoomStatus(예약된 시간대) 목록을 가져옵니다.
     */
    public List<RoomStatus> fetchScheduleFromServer(String isoDate, String room) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("date", isoDate);
            payload.put("room", room);

            Message req = new Message();
            req.setDomain("timetable");
            req.setType(RequestType.LOAD_TIMETABLE);
            req.setPayload(payload);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();
            if (res.getMessage() != null) {
                throw new IOException(res.getMessage());
            }
            return (List<RoomStatus>) res.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 주차(1~5)와 월(1~12), 그리고 room 을 받아 그 주(월~금)에 해당하는 ISO 날짜를 구하고, 위
     * fetchScheduleFromServer 로 매일 “예약됨” 시간대를 채워 반환합니다.
     */
    public Map<String, List<String>> getWeeklyReservations(int month, int week, String room) {
        // 1) 기준 연도 없이 이번 연도로만
        LocalDate firstOfMonth = LocalDate.now().withMonth(month).withDayOfMonth(1);
        LocalDate start = firstOfMonth.plusDays((week - 1) * 7);
        LocalDate end = start.plusDays(6);

        String[] days = {"월", "화", "수", "목", "금"};
        String[] times = {"09:00~09:50", "10:00~10:50", "11:00~11:50", "12:00~12:50",
            "13:00~13:50", "14:00~14:50", "15:00~15:50", "16:00~16:50"};
        Map<String, List<String>> weekly = new HashMap<>();
        for (String d : days) {
            weekly.put(d, new ArrayList<>(Collections.nCopies(times.length, "")));
        }

        File file = new File("storage/reservation_data.txt");
        System.out.println("▶︎ 예약 데이터 파일 경로: " + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("! 파일이 없습니다.");
            return weekly;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("읽은 줄: " + line);
                String[] p = line.split(",");
                if (p.length < 5) {
                    System.out.println("  → 토큰 부족, skip");
                    continue;
                }
                String dateStr = p[0], timeSlot = p[1], roomNo = p[2], status = p[4];
                System.out.printf("  → date=%s, time=%s, room=%s, status=%s%n", dateStr, timeSlot, roomNo, status);

                if (!roomNo.equals(room)) {
                    System.out.println("    → 방 번호 불일치, skip");
                    continue;
                }

                LocalDate d = LocalDate.parse(dateStr);
                System.out.println("    → 파싱된 LocalDate: " + d
                        + " (월=" + d.getMonthValue()
                        + ", dayOfMonth=" + d.getDayOfMonth() + ")");

                if (d.getMonthValue() != month
                        || d.isBefore(start)
                        || d.isAfter(end)) {
                    System.out.println("    → 주차 범위 밖, skip");
                    continue;
                }

                String korDay = switch (d.getDayOfWeek()) {
                    case MONDAY ->
                        "월";
                    case TUESDAY ->
                        "화";
                    case WEDNESDAY ->
                        "수";
                    case THURSDAY ->
                        "목";
                    case FRIDAY ->
                        "금";
                    default ->
                        null;
                };
                System.out.println("    → 요일(Kor): " + korDay);

                int idx = Arrays.asList(times).indexOf(timeSlot);
                if (korDay != null && idx >= 0) {
                    System.out.printf("    → 배치: %s행 %d열에 [%s] 기록%n", korDay, idx, status);
                    weekly.get(korDay).set(idx, status);
                } else {
                    System.out.println("    → 시간 슬롯 불일치, skip");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("▶︎ 완성된 weekly map: " + weekly);
        return weekly;
    }

}
