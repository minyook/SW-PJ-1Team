package reservation;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RoomStatusTest {

    // 테스트 대상 인스턴스 (각 테스트마다 초기화됨)
    private RoomStatus instance;

    // 생성자 (JUnit5에서는 없어도 무방하지만 기본 제공됨)
    public RoomStatusTest() {
    }

    /**
     * 전체 테스트 시작 전에 한 번만 실행됨 (필요 시 사용 가능)
     */
    @BeforeAll
    public static void setUpClass() {
        // 전체 클래스 차원의 초기화가 필요할 경우 여기에 작성
    }

    /**
     * 전체 테스트 종료 후 한 번만 실행됨 (필요 시 사용 가능)
     */
    @AfterAll
    public static void tearDownClass() {
        // 전체 클래스 차원의 정리가 필요할 경우 여기에 작성
    }

    /**
     * 각 테스트 실행 전에 실행됨
     * RoomStatus 객체를 새로 생성하여 초기화
     */
    @BeforeEach
    public void setUp() {
        // 초기 상태는 "10:00~10:50" 시간대, "비어 있음" 상태
        instance = new RoomStatus("10:00~10:50", "비어 있음");
    }

    /**
     * 각 테스트 실행 후 실행됨
     * 테스트 객체를 null로 초기화하여 메모리 정리
     */
    @AfterEach
    public void tearDown() {
        instance = null;
    }

    /**
     * 테스트 1: getTimeSlot()
     * - 시간 슬롯 값이 제대로 반환되는지 확인
     */
    @Test
    public void testGetTimeSlot() {
        System.out.println("getTimeSlot");

        // getter를 통해 시간 문자열 반환
        String result = instance.getTimeSlot();

        // 예상값과 비교 (생성자에서 설정한 값)
        assertEquals("10:00~10:50", result);
    }

    /**
     * 테스트 2: getStatus()
     * - 현재 상태가 정확하게 반환되는지 확인
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");

        // getter를 통해 상태 문자열 반환
        String result = instance.getStatus();

        // 초기 생성자에서 "비어 있음"으로 설정했으므로 동일해야 함
        assertEquals("비어 있음", result);
    }

    /**
     * 테스트 3: setStatus()
     * - 상태를 변경한 후 getStatus로 변경된 값이 잘 반영되었는지 확인
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");

        // 상태 변경: "예약"으로 바꿈
        instance.setStatus("예약");

        // 변경 후 다시 가져와서 확인
        assertEquals("예약", instance.getStatus());
    }
}
