package controller;

import common.Message;
import common.RequestType;
import common.User;
import controller.UserController;
import model.UserModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @TempDir Path tempDir;

    private Path dataFile;
    private UserController controller;
    private StubUserModel stubModel;

    static class StubUserModel extends UserModel {
        User toFind;
        boolean exists;
        RuntimeException exOnCheck;
        User registered;

        public StubUserModel(Path file) throws IOException {
            super(file.toString());
        }

        @Override public User findUser(String u, String p)       { return toFind; }
        @Override public boolean checkUserExists(String u)       { if(exOnCheck!=null) throw exOnCheck; return exists; }
        @Override public void register(User u)                   { this.registered = u; }
        @Override public void reload()                           { /* no-op */ }
    }

    @BeforeEach
    void setUp() throws IOException {
        dataFile = tempDir.resolve("users.txt");
        Files.createFile(dataFile);

        stubModel  = new StubUserModel(dataFile);
        controller = new UserController(stubModel);
    }

    @Test
    void loginSuccess() {
        User u = new User("a","pw","role","Alice");
        stubModel.toFind = u;

        Message req = new Message();
        req.setType(RequestType.LOGIN);
        req.setPayload(u);

        Message res = controller.handle(req);
        assertNull(res.getError());
        System.err.println("LOGIN → " + res.getPayload());
    }

    @Test
    void loginFailure() {
        stubModel.toFind = null;

        Message req = new Message();
        req.setType(RequestType.LOGIN);
        req.setPayload(new User("b","bad","role","Bob"));

        Message res = controller.handle(req);
        assertEquals("로그인 실패: 아이디 또는 비밀번호 오류", res.getError());
        System.err.println("LOGIN FAIL → " + res.getError());
    }

    @Test
    void registerSuccess() throws IOException {
        stubModel.exists = false;

        User u = new User("c","pw2","role","Charlie");
        Message req = new Message();
        req.setType(RequestType.REGISTER);
        req.setPayload(u);

        Message res = controller.handle(req);
        assertNull(res.getError());
        System.err.println("REGISTER → " + res.getPayload());
    }

    @Test
    void registerDuplicate() {
        stubModel.exists = true;

        Message req = new Message();
        req.setType(RequestType.REGISTER);
        req.setPayload(new User("a","pw","role","Alice"));

        Message res = controller.handle(req);
        assertEquals("이미 존재하는 ID입니다.", res.getError());
        System.err.println("REGISTER DUP → " + res.getError());
    }
}
