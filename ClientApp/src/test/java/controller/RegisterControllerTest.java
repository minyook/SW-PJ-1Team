package controller;

import client.ClientMain;
import common.Message;
import common.RequestType;
import common.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;                         // @Mock, @InjectMocks, @Captor, Mockito.verify() 등
import org.mockito.junit.jupiter.MockitoExtension;
import view.RegisterView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)          // MockitoExtension을 통해 @Mock, @InjectMocks 활성화
public class RegisterControllerTest {

    @Mock
    private RegisterView view;               // UI 역할을 대신할 Mock 객체

    @Captor
    private ArgumentCaptor<ActionListener> listenerCaptor;
                                            // 생성자 호출 시 설정된 ActionListener를 가로챌 캡처 객체

    @InjectMocks
    private RegisterController controller;   // view Mock을 주입받는 실제 테스트 대상 컨트롤러

    private ActionListener registerListener; // 캡처한 리스너를 저장할 변수

    @BeforeEach
    void setUp() {
        // ================================
        // 1) 컨트롤러 생성자에서
        //    view.setRegisterAction(...) 호출 시
        //    전달된 리스너를 listenerCaptor로 가로챔
        // ================================
        Mockito.verify(view).setRegisterAction(listenerCaptor.capture());
        registerListener = listenerCaptor.getValue();

        // ================================
        // 2) 네트워크 연결용 IP 세팅
        //    (테스트에선 localhost만 사용)
        // ================================
        ClientMain.serverIP = "localhost";
    }

    // ============================================
    // 테스트 #1: 빈 필드 검증
    // - id가 빈 문자열일 때, 네트워크 호출 없이
    //   view.showMessage("모든 필드를 입력하세요.")만 실행되는지 확인
    // ============================================
    @Test
    void testEmptyFields_showsValidationMessage() {
        // 1) view.getUsername()이 ""을 반환하도록 stub 설정
        when(view.getUsername()).thenReturn("");
        // 2) 다른 필드는 정상 값 반환
        when(view.getPassword()).thenReturn("pw");
        when(view.getName()).thenReturn("홍길동");
        when(view.getRole()).thenReturn("학생");

        // 3) 실제 ActionListener 호출 (ActionEvent는 null로 전달해도 무방)
        registerListener.actionPerformed(null);

        // 4) 검증: 빈 필드 메시지만 한 번 호출됐는지
        verify(view).showMessage("모든 필드를 입력하세요.");
        // 5) 검증: 성공 시 호출되는 dispose()가 절대 호출되지 않았는지
        verify(view, never()).dispose();
    }

    // ============================================
    // 테스트 #2: 회원가입 성공 흐름
    // - 테스트용 ServerSocket을 띄워
    //   1) REGISTER 요청을 받고
    //   2) 에러 없이 빈 에러 필드 메시지를 응답
    //   3) controller가 "✅ 회원가입 성공!" 메시지와 dispose() 호출하는지 확인
    // ============================================
    @Test
    void testRegister_success() throws Exception {
        // -------- 1) 임시 서버 소켓 띄우기 --------
        try (ServerSocket ss = new ServerSocket(0)) {      // 포트 0: 자동 할당
            int port = ss.getLocalPort();                  // 실제 열린 포트 번호 조회
            ClientMain.serverPort = port;                  // 컨트롤러가 연결할 포트 설정

            // -------- 2) 백그라운드 fake 서버 쓰레드 --------
            Thread fakeServer = new Thread(() -> {
                try (
                    Socket sock = ss.accept();                                 // 클라이언트 연결 수락
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())
                ) {
                    // (a) 클라이언트가 보낸 Message 객체 읽기
                    Message req = (Message) in.readObject();

                    // (b) 요청 Message의 도메인·타입이 예상대로 왔는지 검증
                    assertEquals("user", req.getDomain());
                    assertEquals(RequestType.REGISTER, req.getType());

                    // (c) 성공 응답 Message 구성 (에러 필드 null)
                    Message res = new Message();
                    res.setError(null);

                    // (d) 클라이언트로 응답 전송
                    out.writeObject(res);
                    out.flush();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            fakeServer.start();    // fake 서버 실행

            // -------- 3) 클라이언트 쪽 스트림 초기화 --------
            Socket clientSock = new Socket("localhost", port);
            ClientMain.out = new ObjectOutputStream(clientSock.getOutputStream());
            ClientMain.out.flush();
            ClientMain.in  = new ObjectInputStream(clientSock.getInputStream());

            // -------- 4) view.getXXX() stub 설정 --------
            when(view.getUsername()).thenReturn("testId");
            when(view.getPassword()).thenReturn("testPw");
            when(view.getName()).thenReturn("테스터");
            when(view.getRole()).thenReturn("학생");

            // -------- 5) ActionListener 실행 (회원가입 시도) --------
            registerListener.actionPerformed(null);

            // -------- 6) 검증: 성공 메시지 및 dispose 호출 --------
            verify(view).showMessage("회원가입 성공!");
            verify(view).dispose();
        }
    }
}
