package controller;

import client.ClientMain;
import client.SocketClient;
import common.Message;
import common.Reservation;
import common.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import view.MyReservationFrame;
import view.ReservationFrame;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyReservationControllerTest {

    @Mock
    MyReservationFrame view;
    @Mock
    JButton deleteButton, changeButton;
    @Mock
    JTable table;

    @Captor
    ArgumentCaptor<List<Reservation>> listCaptor;

    private MyReservationController controller;
    private User dummyUser;

    private static MockedStatic<JOptionPane> jopMock;

    @BeforeAll
    static void disableDialogs() {
        jopMock = mockStatic(JOptionPane.class);
        jopMock.when(() -> JOptionPane.showMessageDialog(any(), anyString()))
                .thenAnswer(invocation -> null);
    }

    @AfterAll
    static void releaseDialogs() {
        jopMock.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        // 초기 loadMyReservations() 대응 더미 스트림
        Message initResp = new Message();
        initResp.setPayload(List.of());
        ByteArrayOutputStream initBos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(initBos)) {
            oos.writeObject(initResp);
        }
        ClientMain.in = new ObjectInputStream(
                new ByteArrayInputStream(initBos.toByteArray())
        );

        // view.getUser() 스텁
        dummyUser = new User("userA", "pwA", "학생", "홍길동");
        doReturn(dummyUser).when(view).getUser();

        // view 컴포넌트 스텁
        when(view.getDeleteButton()).thenReturn(deleteButton);
        when(view.getChangeButton()).thenReturn(changeButton);
        when(view.getReservationTable()).thenReturn(table);

        // 출력을 위한 out 초기화
        ClientMain.out = new ObjectOutputStream(new ByteArrayOutputStream());

        // 컨트롤러 생성 (loadMyReservations 호출)
        controller = new MyReservationController(view, dummyUser.getUsername());
        // 생성 시 호출된 updateReservationTable 호출 기록 초기화
        clearInvocations(view);
    }

    @Test
    void testLoadMyReservations_withData() throws Exception {
        List<Reservation> dummyList = Arrays.asList(
                new Reservation("1", "2025-05-28", "09:00", "911", dummyUser.getName(), "예약 완료")
        );
        Message resp = new Message();
        resp.setPayload(dummyList);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(resp);
        }
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

        Method load = MyReservationController.class.getDeclaredMethod("loadMyReservations");
        load.setAccessible(true);
        assertDoesNotThrow(() -> load.invoke(controller));

        verify(view).updateReservationTable(listCaptor.capture());
        List<Reservation> actual = listCaptor.getValue();
        
    }

    @Test
    void testLoadMyReservations_nullData() throws Exception {
        Message resp = new Message();
        resp.setPayload(null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(resp);
        }
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

        Method load = MyReservationController.class.getDeclaredMethod("loadMyReservations");
        load.setAccessible(true);
        assertDoesNotThrow(() -> load.invoke(controller));

        verify(view).updateReservationTable(List.of());
    }

    @Test
    void testHandleDelete_success() throws Exception {
        when(table.getSelectedRow()).thenReturn(0);
        when(table.getValueAt(0, 0)).thenReturn("123");

        Message ok = new Message();
        ok.setError(null);

        Message secondResp = new Message();
        secondResp.setPayload(List.of());
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(secondResp);
        }
        ClientMain.in = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(ok);

            Method delete = MyReservationController.class.getDeclaredMethod("handleDelete");
            delete.setAccessible(true);
            assertDoesNotThrow(() -> delete.invoke(controller));

            verify(view, times(1)).updateReservationTable(anyList());
        }
    }

    @Test
    void testHandleDelete_error() throws Exception {
        when(table.getSelectedRow()).thenReturn(0);
        when(table.getValueAt(0, 0)).thenReturn("999");

        Message fail = new Message();
        fail.setError("삭제 오류");
        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(fail);

            Method delete = MyReservationController.class.getDeclaredMethod("handleDelete");
            delete.setAccessible(true);
            assertDoesNotThrow(() -> delete.invoke(controller));
        }
    }

    @Test
    void testHandleChange() throws Exception {
        when(table.getSelectedRow()).thenReturn(0);
        when(table.getValueAt(0, 0)).thenReturn("456");

        try (MockedConstruction<ReservationFrame> mocked
                = mockConstruction(ReservationFrame.class)) {
            Method change = MyReservationController.class.getDeclaredMethod("handleChange");
            change.setAccessible(true);
            assertDoesNotThrow(() -> change.invoke(controller));

            verify(view).dispose();
            assertEquals(1, mocked.constructed().size());
        }
    }
}
