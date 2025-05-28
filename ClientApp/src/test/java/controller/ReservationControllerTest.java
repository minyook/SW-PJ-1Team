package controller;


import client.ClientMain;
import common.Message;


import common.ReservationResult;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


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

    @BeforeEach
    void setUp() throws IOException {
        controller = new ReservationController();
        // out은 버려도 되도록 빈 스트림
        ClientMain.out = new ObjectOutputStream(new ByteArrayOutputStream());
    }

    /**
     * 성공 케이스: 서버가 { error=null, payload="OK" } 을 보내면 SUCCESS
     */
    @Test
    void testProcessReservationRequest_success() throws Exception {
        // 1) fake 응답 메시지 준비
        Message fake = new Message();
        fake.setError(null);
        fake.setPayload("OK");

        // 2) fake를 직렬화 → ClientMain.in으로 연결
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream    oos  = new ObjectOutputStream(baos);
        oos.writeObject(fake);
        oos.flush();
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        // 3) 호출 & 검증
        ReservationResult result =
            controller.processReservationRequest("2025-05-28","09:00","911","UserA");
        assertEquals(ReservationResult.SUCCESS, result);
    }

    /**
     * 중복 케이스: payload="중복" 이면 TIME_OCCUPIED
     */
    @Test
    void testProcessReservationRequest_duplicate() throws Exception {
        Message fake = new Message();
        fake.setError(null);
        fake.setPayload("중복");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos).writeObject(fake);
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        ReservationResult result =
            controller.processReservationRequest("","","","");
        assertEquals(ReservationResult.TIME_OCCUPIED, result);
    }

    /**
     * 에러 케이스: readObject() 중 예외(EOFException 등)가 나면 ERROR
     */
    @Test
    void testProcessReservationRequest_errorOnRead() throws Exception {
        // out은 그대로, in만 EOFException 던지도록 mock
        ObjectInputStream mockIn = mock(ObjectInputStream.class);
        when(mockIn.readObject()).thenThrow(new EOFException());
        ClientMain.in = mockIn;

        assertEquals(
            ReservationResult.ERROR,
            controller.processReservationRequest("","","","")
        );
    }
}
