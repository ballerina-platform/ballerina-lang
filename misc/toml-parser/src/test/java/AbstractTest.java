import java.nio.file.Paths;

public class AbstractTest {

    static void testFile(String path, String filePath) {
        ParserTestUtils.test(Paths.get(path), Paths.get(filePath));
    }
}
