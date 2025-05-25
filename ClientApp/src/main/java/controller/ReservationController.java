package controller;

import client.*;
import common.Message;
import common.RequestType;
import common.Reservation;
import common.ReservationResult;
import common.RoomStatus;
import java.util.ArrayList;

import java.util.List;
import javax.swing.JOptionPane;

public class ReservationController {
    @SuppressWarnings("unchecked")
    public List<Reservation> getAllReservations() {
        try {
            Message req = new Message();
            req.setDomain("reservation");
            req.setType(RequestType.LIST);

            Message res = SocketClient.send(req);
            if (res.getError() == null) {
                return (List<Reservation>) res.getList();
            } else {
                System.out.println("조회 실패: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return List.of();
    }


    public boolean reserve(Reservation r) {
        try {
            Message req = new Message();
            req.setDomain("reservation");
            req.setType(RequestType.CREATE);
            req.setPayload(r);

            Message res = SocketClient.send(req);
            if (res.getError() == null) return true;
            else System.out.println("예약 실패: " + res.getError());
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return false;
    }

    public boolean cancelReservation(Reservation r) {
        try {
            Message req = new Message();
            req.setDomain("reservation");
            req.setType(RequestType.DELETE);
            req.setPayload(r);

            Message res = SocketClient.send(req);
            if (res.getError() == null) return true;
            else System.out.println("예약 취소 실패: " + res.getError());
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return false;
    }
    public List<RoomStatus> loadTimetable(String year, String month, String day, String roomNumber) {
        // 여기서는 서버 요청을 보내는 대신, 더미 데이터로 테스트합니다
        List<RoomStatus> list = new ArrayList<>();
        list.add(new RoomStatus("09:00", "비어 있음"));
        list.add(new RoomStatus("10:00", "사용 중"));
        list.add(new RoomStatus("11:00", "비어 있음"));
        return list;
    }
    public List<String> loadScheduleFile(String roomNumber) {
        try {
            Message msg = new Message();
            msg.setDomain("schedule");
            msg.setType(RequestType.LOAD_SCHEDULE_FILE);
            msg.setPayload(roomNumber); // 예: "911"

            Message res = SocketClient.send(msg);
            if (res.getError() == null) {
                return (List<String>) res.getPayload();
            } else {
                System.out.println("시간표 파일 요청 실패: " + res.getError());
            }
        } catch (Exception e) {
            System.out.println("서버 통신 오류: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public ReservationResult processReservationRequest(String date, String time, String room, String name) {
        try {
            Reservation reservation = new Reservation(date, time, room, name, "대기");

            Message req = new Message();
            req.setDomain("reservation");
            req.setType(RequestType.RESERVE);
            req.setPayload(reservation);

            ClientMain.out.writeObject(req);
            ClientMain.out.flush();

            Message res = (Message) ClientMain.in.readObject();

            if (res.getError() != null) {
                return ReservationResult.ERROR;
            }

            String status = (String) res.getPayload();
            if ("중복".equals(status)) {
                return ReservationResult.TIME_OCCUPIED;
            }

            return ReservationResult.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return ReservationResult.ERROR;
        }
    }
    
}
