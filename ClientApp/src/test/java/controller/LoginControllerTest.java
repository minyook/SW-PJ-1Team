package controller;


import client.ClientMain;                   // 컨트롤러가 사용하는 서버 IP/포트 설정을 위해
import common.Message;                     // 서버와 주고받는 Message 객체
import common.User;                        // Message.payload 에 담아 보낼 User DTO
import org.junit.jupiter.api.BeforeEach;   // 각 테스트 메소드 실행 전에 실행되는 설정용
import org.junit.jupiter.api.Test;         // 테스트 메소드에 붙이는 어노테이션
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;            // Mockito가 객체 생성 후 주입해 주도록
import org.mockito.Mock;                   // Mockito가 가짜(Mock) 객체를 만들어 주도록
import org.mockito.junit.jupiter.MockitoExtension; // Mockito와 JUnit5 연동용
import view.LoginView;                    // 컨트롤러가 화면에 결과를 표시할 뷰 인터페이스

import java.io.ObjectInputStream;         // 소켓 통신을 흉내 낼 때 스트림으로 쓰려고
import java.io.ObjectOutputStream;
import java.net.ServerSocket;             // 가벼운 테스트용 “임시 서버” 실행을 위해
import java.net.Socket;

import static org.mockito.Mockito.*; 

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    // LoginView 의 “가짜 객체(Mock)”를 만들어 달라고 Mockito에 지시
    @Mock
    private LoginView view;


    // LoginController 인스턴스를 생성하되, 생성자 파라미터로
    // 위 @Mock view 객체를 주입(inject)해 달라고 Mockito에 지시
    @InjectMocks
    private LoginController controller;

    // @BeforeEach: 각 @Test 실행 직전에 매번 호출
    // 이 안에서 클라이언트가 사용할 serverIP를 “localhost”로 지정
    // => 테스트 중 localhost:포트 로만 접속 시도하게 함

    @BeforeEach
    void setup() {
        ClientMain.serverIP = "localhost";
    }


    // 1️. 연결 실패 시나리오
    // — 포트를 12345 로 지정했는데, 
    //   실제로는 어떤 프로세스도 listen 중이 아니므로
    //   new Socket(...) 에서 ConnectException 발생
    // — 컨트롤러 내부 catch 블록이 실행되며 
    //   view.showError(...) 와 view.setLoginEnabled(true) 을 부른 걸 검증

    @Test
    void testLogin_connectionError() {
        // 포트를 열지 않는 12345번으로 설정
        ClientMain.serverPort = 12345;

        // 실제 테스트 대상 메소드 호출
        controller.login("anyId", "anyPw");

        // 1) 첫 시작에 항상 로그인 버튼 비활성화
        verify(view).setLoginEnabled(false);
        // 2) 예외 처리 후에는 버튼 다시 활성화
        verify(view).setLoginEnabled(true);
        // 3) “서버 연결 중 오류 발생:” 로 시작하는 에러 메시지를 띄웠는지
        verify(view).showError(startsWith("서버 연결 중 오류 발생:"));
    }

    // ———————————————————————————————————————————————————————————————
    // 2️ "로그인 성공" 시나리오
    // — 테스트 중 가짜 서버를 띄워서
    //   클라이언트(login())가 연결 → 요청 → 응답 → 정상 흐름을 타도록 함
    // — 서버는 클라이언트가 보내는 요청 객체는 무시하고,
    //   곧바로 성공용 Message(payload=User)를 반환
    // — 컨트롤러는 view.showMessage(...) 와 view.dispose() 를 호출해야 함
    // ———————————————————————————————————————————————————————————————
    @Test
    void testLogin_success() throws Exception {
        // 1) ServerSocket(0): 운영체제가 비어 있는 임의 포트를 할당
        try (ServerSocket ss = new ServerSocket(0)) {
            int port = ss.getLocalPort();    // 실제 열린 포트 번호
            ClientMain.serverPort = port;    // 컨트롤러가 여기로 접속하게 설정

            // 2) 백그라운드에서 가짜 서버 실행
            Thread fakeServer = new Thread(() -> {
                try (
                    Socket sock = ss.accept();  // 클라이언트 접속 대기
                    ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream())
                ) {
                    // 3) 클라이언트가 보낸 Message 객체(요청)를 읽기만 하고 무시
                    in.readObject();

                    // 4) 성공 응답 Message 생성
                    Message res = new Message();
                    res.setError(null);  // 에러 없음
                    // payload 에 테스트용 User("TestUser") 를 담음
                    res.setPayload(new User("id", "pw", "s", "TestUser"));
                    // 5) 클라이언트로 전송
                    out.writeObject(res);
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();  // 테스트 중 서버 쓰레드 오류가 있으면 터미널에 출력
                }
            });
            fakeServer.start();  // 가짜 서버 쓰레드 실행

            // 6) 실제 컨트롤러의 login() 호출 → 성공 흐름 타게 됨
            controller.login("id", "pw");

            // ——————————————————————————————————————————————————————
            // 7) 검증: 성공 시나리오에서 반드시 일어나야 할 호출들
            // ——————————————————————————————————————————————————————
            verify(view).setLoginEnabled(false);                        // 시작하자마자 버튼 비활성화
            verify(view).showMessage(startsWith("로그인 성공:"));     // “로그인 성공” 메시지
            verify(view).dispose();                                     // 로그인 뷰 닫고 다음 화면 띄우는 dispose()
        }
    }
}
