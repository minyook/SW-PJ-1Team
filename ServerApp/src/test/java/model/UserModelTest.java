package model;

import common.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @TempDir
    Path tempDir;               // 매 테스트마다 새 임시 폴더

    private Path testFile;
    private UserModel model;

    @BeforeEach
    void setUp() throws IOException {
        // 1) tempDir 아래에 users.txt 생성 (4개 토큰: id,pw,role,name)
        testFile = tempDir.resolve("users.txt");
        Files.write(testFile, List.of(
            "alice,pass123,student,Alice Wonderland",
            "bob,secret,teacher,Bob Builder"
        ), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 2) 생성자에 파일 경로 그대로 넘겨서 모델 초기화
        model = new UserModel(testFile.toString());
    }

    @Test
    void testFindUserSuccess() throws IOException {
        // 올바른 자격증명으로 사용자 찾기
        User u = model.findUser("alice", "pass123");
        assertNotNull(u, "alice/패스워드로는 User 객체가 반환되어야 합니다");
        assertEquals("alice", u.getUsername());
        assertEquals("Alice Wonderland", u.getName());

        System.err.println(">> found user: " + u.getUsername() + " (" + u.getRole() + ")");
    }

    @Test
    void testFindUserFailure() throws IOException {
        // 존재하지 않거나 비번 불일치 시 null
        assertNull(model.findUser("alice", "wrongpw"), "비번이 틀리면 null 반환");
        assertNull(model.findUser("charlie", "pw"), "존재하지 않는 ID면 null 반환");
    }

    @Test
    void testCheckUserExists() throws IOException {
        assertTrue(model.checkUserExists("bob"),   "bob은 존재해야 합니다");
        assertFalse(model.checkUserExists("daniel"), "daniel은 없어야 합니다");

        System.err.println(">> users exist: alice="
            + model.checkUserExists("alice")
            + ",   bob=" + model.checkUserExists("bob"));
    }

    @Test
    void testRegisterAndReload() throws IOException {
        // 등록 전에는 없어야 함
        assertFalse(model.checkUserExists("charlie"), "등록 전 charlie는 없어야 합니다");

        // 신규 User 생성 (4개 인자 생성자 사용)
        User newUser = new User("charlie", "pw456", "student", "Charlie Chaplin");
        model.register(newUser);
        model.reload();  // save() 후 load()

        // 파일 내용 출력
        System.err.println("=== users.txt 내용 ===");
        Files.readAllLines(testFile).forEach(System.err::println);
        System.err.println("=====================");

        // 등록 후 검증
        assertTrue(model.checkUserExists("charlie"), "등록 후 charlie가 있어야 합니다");
        User u = model.findUser("charlie", "pw456");
        assertNotNull(u, "등록된 charlie를 findUser로 찾을 수 있어야 합니다");
        assertEquals("Charlie Chaplin", u.getName());

        System.err.println(">> after register: " 
            + u.getUsername() + " / " + u.getRole() + " / " + u.getName());
    }
}
