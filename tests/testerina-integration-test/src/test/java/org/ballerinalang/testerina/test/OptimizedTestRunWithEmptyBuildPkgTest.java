package org.ballerinalang.testerina.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test case to check `bal test --eliminate-dead-code` command with an empty buildPkg and a test with a FP to a lang
 * module function.
 *
 * @since 2201.11.0
 */
public class OptimizedTestRunWithEmptyBuildPkgTest extends BaseTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizedExecutableTestingTest.class);
    private BMainInstance balClient;
    private String projectPath;
    private static final String TARGET = "target";
    private static final String PROJECT_NAME = "optimized-test-run-with-empty-build-pkg-test";
    private static final String DEAD_CODE_ELIMINATION_REPORT = "dead_code_elimination_report.json";
    private static final Path EXPECTED_CODEGEN_OPTIMIZATION_REPORTS_DIR =
            Paths.get("src", "test", "resources", "codegen-optimization-reports", PROJECT_NAME);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve(PROJECT_NAME).toString();
    }

    @Test()
    public void testWithEmptyBuildPkg() throws BallerinaTestException, IOException {
        String output = balClient.runMainAndReadStdOut("test",
                new String[]{"--eliminate-dead-code", "--dead-code-elimination-report"}, new HashMap<>(), projectPath,
                true);
        AssertionUtils.assertOutput("OptimizedExecutableTestingTest-testWithEmptyBuildPkg.txt",
                AssertionUtils.replaceBIRNodeAnalysisTime(output));
        assertBuildProjectJsonReportsAreSimilar(Path.of(projectPath));
    }

    private void assertBuildProjectJsonReportsAreSimilar(Path buildProjectPath) {
        Path actualJsonPath = buildProjectPath.resolve(TARGET).resolve(DEAD_CODE_ELIMINATION_REPORT);
        JsonObject expectedJsonObject =
                fileContentAsObject(EXPECTED_CODEGEN_OPTIMIZATION_REPORTS_DIR.resolve(DEAD_CODE_ELIMINATION_REPORT));
        JsonObject actualJsonObject = fileContentAsObject(actualJsonPath);
        Assert.assertEquals(actualJsonObject, expectedJsonObject);
    }

    private static JsonObject fileContentAsObject(Path filePath) {
        String contentAsString = "";
        try {
            contentAsString = new String(Files.readAllBytes(filePath));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return JsonParser.parseString(contentAsString).getAsJsonObject();
    }
}
