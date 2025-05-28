package controller;

import client.SocketClient;
import client.ClientMain;
import common.Message;

import common.Reservation;
import common.ReservationResult;
import common.RoomStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ReservationController에 대한 단위 테스트 클래스
 * 
 * 각 메서드의 정상 및 예외 흐름을 검증하며,
 * 프로덕션 코드를 수정하지 않고 테스트만으로 다양한 시나리오를 모킹(Mock)합니다.
 */
@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    private ReservationController controller;

    /**
     * 각 테스트 실행 전마다 새로운 컨트롤러 인스턴스를 생성하고
     * processReservationRequest 내부에서 사용하는 서버 IP를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        controller = new ReservationController();
        ClientMain.serverIP = "localhost"; // 내부 구현에서 사용되는 서버 정보 초기화
    }

    /**
     * getAllReservations()의 성공 케이스를 테스트합니다.
     * SocketClient.send()를 모킹하여 fakeResponse를 반환하게 한 뒤,
     * 반환된 리스트가 그대로 리턴되는지 검증합니다.
     */
    @Test
    void testGetAllReservations_success() {
        // 1) 서버에서 돌아올 응답(Message)을 미리 준비
        Message fakeResponse = new Message();
        fakeResponse.setError(null);
        List<Reservation> dummyList = Arrays.asList(
            new Reservation("1", "2025-05-28", "09:00", "911", "UserA", "예약 완료")
        );
        fakeResponse.setList(dummyList);

        // 2) SocketClient.send()를 static 모킹하여 fakeResponse를 리턴하도록 설정
        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class)))
              .thenReturn(fakeResponse);

            // 3) 실제 메서드 호출과 결과 검증
            List<Reservation> result = controller.getAllReservations();
            assertSame(dummyList, result, "getAllReservations가 서버 리스트를 그대로 반환해야 함");
        }
    }

    /**
     * reserve() 성공 케이스: 서버 응답 error == null일 때 true 반환 확인.
     */
    @Test
    void testReserve_returnsTrueOnSuccess() {
        // 에러 없이 성공 메시지를 반환하도록 설정
        Message ok = new Message();
        ok.setError(null);

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(ok);

            Reservation r = new Reservation(
                "0", "2025-05-28", "09:00", "911", "UserA", "대기"
            );
            assertTrue(controller.reserve(r), "reserve 성공 시 true를 반환해야 함");
        }
    }

    /**
     * reserve() 실패 케이스: 서버 응답 error != null일 때 false 반환 확인.
     */
    @Test
    void testReserve_returnsFalseOnError() {
        Message fail = new Message();
        fail.setError("some error");

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(fail);

            Reservation r = new Reservation(
                "0", "2025-05-28", "09:00", "911", "UserA", "대기"
            );
            assertFalse(controller.reserve(r), "reserve 실패 시 false를 반환해야 함");
        }
    }

    /**
     * cancelReservation 성공 케이스: error == null일 때 true 반환 확인.
     */
    @Test
    void testCancelReservation_trueOnSuccess() {
        Message ok = new Message();
        ok.setError(null);

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(ok);

            Reservation r = new Reservation(
                "0", "2025-05-28", "09:00", "911", "UserA", "대기"
            );
            assertTrue(controller.cancelReservation(r), "cancelReservation 성공 시 true를 반환해야 함");
        }
    }

    /**
     * cancelReservation 실패 케이스: error != null일 때 false 반환 확인.
     */
    @Test
    void testCancelReservation_falseOnError() {
        Message fail = new Message();
        fail.setError("fail");

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(fail);

            Reservation r = new Reservation(
                "0", "2025-05-28", "09:00", "911", "UserA", "대기"
            );
            assertFalse(controller.cancelReservation(r), "cancelReservation 실패 시 false를 반환해야 함");
        }
    }

    /**
     * loadTimetable의 더미 데이터를 검증합니다.
     */
    @Test
    void testLoadTimetable_returnsDummyList() {
        List<RoomStatus> list = controller.loadTimetable("2025", "05", "28", "911");

        assertEquals(3, list.size());
        assertEquals("09:00", list.get(0).getTimeSlot());
        assertEquals("비어 있음", list.get(0).getStatus());
        assertEquals("10:00", list.get(1).getTimeSlot());
        assertEquals("사용 중", list.get(1).getStatus());
        assertEquals("11:00", list.get(2).getTimeSlot());
        assertEquals("비어 있음", list.get(2).getStatus());
    }

    /**
     * loadScheduleFile의 성공 케이스를 테스트합니다.
     */
    @Test
    void testLoadScheduleFile_success() {
        List<String> dummy = List.of("line1", "line2");
        Message fake = new Message();
        fake.setError(null);
        fake.setPayload(dummy);

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(fake);

            List<String> result = controller.loadScheduleFile("911");
            assertSame(dummy, result, "payload를 그대로 반환해야 함");
        }
    }

    /**
     * loadScheduleFile의 에러 케이스: 에러가 있을 경우 빈 리스트 반환 확인.
     */
    @Test
    void testLoadScheduleFile_errorReturnsEmpty() {
        Message fake = new Message();
        fake.setError("err");

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(fake);
            assertTrue(controller.loadScheduleFile("911").isEmpty(), "에러 시 빈 리스트를 반환해야 함");
        }
    }

    /**
     * processReservationRequest 성공 케이스: 실제 ServerSocket을 띄워 모의 서버를 구성하고,
     * 서버가 OK 응답을 반환하면 SUCCESS가 리턴되는지 검증합니다.
     */
    @Test
    void testProcessReservationRequest_success() throws Exception {
        try (ServerSocket ss = new ServerSocket(0)) {
            int port = ss.getLocalPort();
            ClientMain.serverPort = port;

            // 백그라운드 스레드로 모의 서버 동작 구현
            Thread srv = new Thread(() -> {
                try (
                    Socket sock = ss.accept();
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())
                ) {
                    // 클라이언트 요청 수신
                    in.readObject();
                    // 정상 응답 전송
                    Message res = new Message();
                    res.setError(null);
                    res.setPayload("OK");
                    out.writeObject(res);
                    out.flush();
                } catch (Exception ignore) {}
            });
            srv.start();

            // 클라이언트 측 스트림 설정
            Socket client = new Socket("localhost", port);
            ClientMain.out = new ObjectOutputStream(client.getOutputStream());
            ClientMain.out.flush();
            ClientMain.in  = new ObjectInputStream(client.getInputStream());

            // 메서드 호출 및 결과 검증
            ReservationResult result = controller.processReservationRequest(
                    "2025-05-28", "09:00", "911", "UserA"
            );
            assertEquals(ReservationResult.SUCCESS, result, "서버 OK 응답 시 SUCCESS 반환");
        }
    }

    /**
     * processReservationRequest 중복 예약 케이스: 서버가 "중복" 페이로드를 보내면 TIME_OCCUPIED를 반환하는지 검증합니다.
     */
    @Test
    void testProcessReservationRequest_duplicate() throws Exception {
        try (ServerSocket ss = new ServerSocket(0)) {
            int port = ss.getLocalPort();
            ClientMain.serverPort = port;
            Thread srv = new Thread(() -> {
                try (
                    Socket sock = ss.accept();
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())
                ) {
                    in.readObject();
                    Message res = new Message();
                    res.setError(null);
                    res.setPayload("중복");
                    out.writeObject(res);
                    out.flush();
                } catch (Exception ignore) {}
            });
            srv.start();

            Socket client = new Socket("localhost", port);
            ClientMain.out = new ObjectOutputStream(client.getOutputStream());
            ClientMain.out.flush();
            ClientMain.in  = new ObjectInputStream(client.getInputStream());

            // payload "중복" 시 TIME_OCCUPIED 반환 검증
            assertEquals(
                ReservationResult.TIME_OCCUPIED,
                controller.processReservationRequest("", "", "", ""),
                "서버 중복 응답 시 TIME_OCCUPIED 반환"
            );
        }
    }

    /**
     * processReservationRequest 에러 케이스: readObject() 호출 시 EOFException을 던지도록 모킹하여
     * catch 구문으로 빠지는지, ERROR 반환을 검증합니다.
     */
    @Test
    void testProcessReservationRequest_error() throws IOException {
        // 1) writeObject()는 정상 동작하도록 ByteArrayOutputStream + ObjectOutputStream 설정
        ClientMain.out = new ObjectOutputStream(new ByteArrayOutputStream());
        ClientMain.out.flush();

        // 2) ObjectInputStream을 Mockito로 모킹하여 readObject() 시 EOFException 발생
        ObjectInputStream mockIn = mock(ObjectInputStream.class);
        try {
            when(mockIn.readObject()).thenThrow(new EOFException());
        } catch (ClassNotFoundException ignored) {}
        ClientMain.in = mockIn;

        // 3) catch 블록이 실행되어 ERROR 반환 확인
        assertEquals(
            ReservationResult.ERROR,
            controller.processReservationRequest("2025-05-28", "09:00", "911", "UserA"),
            "입출력 예외 시 ERROR 반환"
        );
    }
}
