package controller;

import client.SocketClient;
import client.ClientMain;
import common.Message;
import common.RoomStatus;
import common.ScheduleEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class TimeTableControllerTest {

    private TimeTableController controller;

    @BeforeEach
    void setUp() {
        controller = new TimeTableController();
        ClientMain.serverIP = "localhost";
        ClientMain.serverPort = 12345;
    }

    // (1) SocketClient.send(...) 호출하는 메서드들
    @Test
    void testGetWeeklySchedule_returnsPayloadMap() {
        Map<String, List<String>> fakeMap = Map.of(
                "월", List.of("A", "B", "C"),
                "화", List.of("D", "E", "F")
        );
        Message fakeMsg = new Message();
        fakeMsg.setError(null);
        fakeMsg.setPayload(fakeMap);

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class)))
                    .thenReturn(fakeMsg);

            var result = controller.getWeeklySchedule(5, 2, "911");
            assertSame(fakeMap, result);
        }
    }

    @Test
    void testGetSchedule_returnsListFromListField() {
        List<ScheduleEntry> fakeList = List.of(
                new ScheduleEntry(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 50), true, "", "CS101", "ProfA"),
                new ScheduleEntry(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(10, 50), true, "", "MA202", "ProfB")
        );
        Message fakeMsg = new Message();
        fakeMsg.setError(null);
        fakeMsg.setList(fakeList);

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class)))
                    .thenReturn(fakeMsg);

            var result = controller.getSchedule();
            assertSame(fakeList, result);
        }
    }

    // (2) ClientMain.in/out 을 사용하는 메서드들
    @Test
    void testLoadScheduleFile_successAndError() throws Exception {
        // — 성공 케이스 준비 —
        Message okMsg = new Message();
        okMsg.setError(null);
        okMsg.setPayload(List.of("line1", "line2"));
        // serialize -> ClientMain.in
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos1)) {
            oos.writeObject(okMsg);
        }
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(bos1.toByteArray()));
        ClientMain.out = new ObjectOutputStream(new ByteArrayOutputStream());

        List<String> okResult = controller.loadScheduleFile("911");
        assertEquals(List.of("line1", "line2"), okResult);

        // — 에러 케이스 준비 —
        Message errMsg = new Message();
        errMsg.setError("fail");
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(errMsg);
        }
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        ClientMain.out = new ObjectOutputStream(new ByteArrayOutputStream());

        List<String> errResult = controller.loadScheduleFile("911");
        assertTrue(errResult.isEmpty());
    }

    @Test
    void testFetchScheduleFromServer_returnsPayloadList() throws Exception {
        // 1) 준비
        List<RoomStatus> fakeStatuses = List.of(
                new RoomStatus("09:00", "비어 있음"),
                new RoomStatus("10:00", "사용 중")
        );
        Message fakeMsg = new Message();
        fakeMsg.setError(null);
        fakeMsg.setPayload(fakeStatuses);

        // 2) 직렬화 → ClientMain.in
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(fakeMsg);
        }
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ClientMain.out = new ObjectOutputStream(new ByteArrayOutputStream());

        // 3) 실행
        List<RoomStatus> result = controller.fetchScheduleFromServer("2025-05-28", "911");

        // 4) 검증: 크기와 각 객체의 필드를 비교
        assertEquals(fakeStatuses.size(), result.size(), "리스트 크기 일치해야 한다");
        for (int i = 0; i < fakeStatuses.size(); i++) {
            RoomStatus expected = fakeStatuses.get(i);
            RoomStatus actual = result.get(i);
            assertEquals(expected.getTimeSlot(),
                    actual.getTimeSlot(),
                    "시간 슬롯이 그대로 와야 한다, idx=" + i);
            assertEquals(expected.getStatus(),
                    actual.getStatus(),
                    "상태 문자열이 그대로 와야 한다, idx=" + i);
        }
    }

}
