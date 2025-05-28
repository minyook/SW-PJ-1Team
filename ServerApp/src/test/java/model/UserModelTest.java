package model;

import common.User;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserModelTest {

    private static final String TEST_PATH = "src/test/resources/test_user.txt";

    @BeforeEach
    void setup() throws IOException {
        Path path = Paths.get(TEST_PATH);
        Files.createDirectories(path.getParent());
        Files.write(path, List.of("hong123,1234,학생,홍길동"));
    }

    @Test
    void testListAllReadsUsers() throws IOException {
        UserModel model = new UserModel(TEST_PATH);
        List<User> users = model.listAll();

        assertEquals(1, users.size());
        assertEquals("홍길동", users.get(0).getName());
    }

    @Test
    void testRegisterAddsNewUser() throws IOException {
        UserModel model = new UserModel(TEST_PATH);
        User newUser = new User("kim456", "abcd", "교수", "김철수");
        model.register(newUser);

        List<User> users = model.listAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("kim456")));
    }

    @Test
    void testCheckUserExists() throws IOException {
        UserModel model = new UserModel(TEST_PATH);
        assertTrue(model.checkUserExists("hong123"));
        assertFalse(model.checkUserExists("nonexist"));
    }

    @Test
    void testFindUserSuccess() throws IOException {
        UserModel model = new UserModel(TEST_PATH);
        User found = model.findUser("hong123", "1234");
        assertNotNull(found);
        assertEquals("홍길동", found.getName());
    }

    @Test
    void testFindUserFail() throws IOException {
        UserModel model = new UserModel(TEST_PATH);
        User found = model.findUser("hong123", "wrongpw");
        assertNull(found);
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_PATH));
    }
}
