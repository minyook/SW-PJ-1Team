/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package management.model;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author limmi
 */
public class ReservationModelTest {
    
    public ReservationModelTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getAll method, of class ReservationModel.
     */
    @Test
    public void testGetAll_emptyOnSkipLoad() throws IOException {
        // skipLoad=true 로 파일 읽기 건너뛰기
        ReservationModel model = new ReservationModel(true);

        List<Reservation> all = model.getAll();
        // 절대 null 이 아니고 빈 리스트여야 한다
        assertNotNull(all, "getAll()은 null을 리턴하면 안 됩니다");
        assertTrue(all.isEmpty(), "초기에는 빈 리스트여야 합니다");
    }

    /**
     * Test of updateStatus method, of class ReservationModel.
     */
    @Test
    void testUpdateStatus() throws IOException {
        ReservationModel model = new ReservationModel(true);

        // 1) 새 예약 추가
        Reservation r = model.addReservation(
            "911",
            "2025-05-16",
            "09:00~09:50",
            "홍길동"
        );
        assertEquals(
            Reservation.Status.PENDING,
            r.getStatus(),
            "기본 상태는 PENDING"
        );

        // 2) 문자열 '승인' 으로 상태 갱신
        model.updateStatus(r, Reservation.Status.APPROVED);

        // 3) 리스트에서 꺼내서 enum 상태 확인
        List<Reservation> all = model.getAll();
        assertEquals(1, all.size());
        assertEquals(
            Reservation.Status.APPROVED,
            all.get(0).getStatus(),
            "updateStatus(..., \"승인\") 후에는 APPROVED 상태여야 합니다"
        );
    }

    /**
     * Test of addReservation method, of class ReservationModel.
     */
    @Test
    void testAddReservation() throws IOException {
        ReservationModel model = new ReservationModel(true);

        // 새 예약 추가
        Reservation r = model.addReservation(
            "911",
            "2025-05-16",
            "09:00~09:50",
            "홍길동"
        );
        assertNotNull(r, "addReservation() 은 새 Reservation 객체를 리턴해야 합니다");

        // getAll() 결과 검증
        List<Reservation> all = model.getAll();
        assertEquals(1, all.size(), "리스트에 항목이 하나 있어야 합니다");
        assertSame(r, all.get(0), "리스트의 첫 요소는 리턴된 객체여야 합니다");
        assertEquals(
            Reservation.Status.PENDING,
            r.getStatus(),
            "새 예약의 기본 상태는 PENDING(예약 대기)여야 합니다"
        );
    }
    
}
