package controller;

import common.Message;
import common.RequestType;
import common.User;
import model.UserModel;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    // --------- StubUserModel ---------
    static class StubUserModel extends UserModel {
        private final User toFind;
        private final boolean exists;
        private final RuntimeException exOnCheck;

        // IOException을 던지도록 선언
        public StubUserModel(User toFind, boolean exists, RuntimeException exOnCheck) throws IOException {
            super(); // UserModel() 호출, IOException 발생 가능
            this.toFind     = toFind;
            this.exists     = exists;
            this.exOnCheck  = exOnCheck;
        }

        @Override
        public User findUser(String username, String password) {
            return toFind;
        }

        @Override
        public boolean checkUserExists(String username) {
            if (exOnCheck != null) throw exOnCheck;
            return exists;
        }

        @Override
        public void register(User newUser) {
            // no-op
        }

        @Override
        public void reload() {
            // no-op
        }
    }

    // --------- Tests ---------

    @Nested
    class LoginTests {
        @Test
        void loginSuccess() throws Exception {
            User stub = new User("alice", "pw");
            UserController controller = new UserController(new StubUserModel(stub, false, null));

            Message req = new Message();
            req.setType(RequestType.LOGIN);
            req.setPayload(stub);

            Message res = controller.handle(req);

            assertNull(res.getError());
            assertEquals("user", res.getDomain());
            assertEquals(RequestType.LOGIN, res.getType());
            assertSame(stub, res.getPayload());
        }

        @Test
        void loginFailure() throws Exception {
            UserController controller = new UserController(new StubUserModel(null, false, null));

            Message req = new Message();
            req.setType(RequestType.LOGIN);
            req.setPayload(new User("bob", "bad"));

            Message res = controller.handle(req);

            assertNull(res.getPayload());
            assertEquals("로그인 실패: 아이디 또는 비밀번호 오류", res.getError());
        }
    }

    @Nested
    class RegisterTests {
        @Test
        void registerSuccess() throws Exception {
            User newUser = new User("charlie", "pw2");
            UserController controller = new UserController(new StubUserModel(null, false, null));

            Message req = new Message();
            req.setType(RequestType.REGISTER);
            req.setPayload(newUser);

            Message res = controller.handle(req);

            assertNull(res.getError());
            assertEquals("회원가입 완료", res.getPayload());
        }

        @Test
        void registerDuplicate() throws Exception {
            User dup = new User("alice", "pw");
            UserController controller = new UserController(new StubUserModel(null, true, null));

            Message req = new Message();
            req.setType(RequestType.REGISTER);
            req.setPayload(dup);

            Message res = controller.handle(req);

            assertNull(res.getPayload());
            assertEquals("이미 존재하는 ID입니다.", res.getError());
        }

        @Test
        void registerThrowsException() throws Exception {
            User u = new User("x", "y");
            UserController controller = new UserController(new StubUserModel(null, false, new RuntimeException("DB 오류")));

            Message req = new Message();
            req.setType(RequestType.REGISTER);
            req.setPayload(u);

            Message res = controller.handle(req);

            assertTrue(res.getError().contains("오류 발생: DB 오류"));
        }
    }

    @Test
    void unsupportedType() throws Exception {
        UserController controller = new UserController(new StubUserModel(null, false, null));

        Message req = new Message();
        req.setType(RequestType.LOGOUT);  // 지원하지 않는 타입
        req.setPayload(null);

        Message res = controller.handle(req);

        assertEquals("지원하지 않는 사용자 요청입니다.", res.getError());
    }
}
