// src/test/java/controller/AdminReservationControllerTest.java
package controller;

import client.SocketClient;
import common.Message;
import common.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import view.AdminReservationFrame;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReservationControllerTest {

    @Mock
    private AdminReservationFrame mockView;

    @InjectMocks
    private AdminReservationController controller;

    @Test
    void testUpdateStatus_success() {
        Reservation dummy = new Reservation("1","2025-05-28","09:00","911","user","PENDING");
        Message ok = new Message();
        ok.setError(null);

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(ok);
            assertTrue(controller.updateStatus(dummy),
                       "error == null 이면 true");
        }
    }

    @Test
    void testUpdateStatus_failure() {
        Reservation dummy = new Reservation("1","2025-05-28","09:00","911","user","PENDING");
        Message fail = new Message();
        fail.setError("oops");

        try (MockedStatic<SocketClient> sc = mockStatic(SocketClient.class)) {
            sc.when(() -> SocketClient.send(any(Message.class))).thenReturn(fail);
            assertFalse(controller.updateStatus(dummy),
                        "error != null 이면 false");
        }
    }
}
