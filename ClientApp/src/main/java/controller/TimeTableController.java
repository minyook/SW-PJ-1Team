package controller;

import client.SocketClient;
import common.Message;
import common.RequestType;
import common.ScheduleEntry;

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

            if (res.getError() == null) {
                return (Map<String, List<String>>) res.getPayload();
            }
        } catch (Exception e) {
            System.out.println("서버 오류: " + e.getMessage());
        }

        return Map.of(); // 실패 시 빈 맵
    }

    
    @SuppressWarnings("unchecked")
    public List<ScheduleEntry> getSchedule() {
        try {
            Message req = new Message();
            req.setDomain("timetable");
            req.setType(RequestType.LIST);

            Message res = SocketClient.send(req);
            if (res.getError() == null) {
                return (List<ScheduleEntry>) res.getList();
            } else {
                System.out.println("시간표 조회 실패: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return List.of();
    }
}
