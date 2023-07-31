package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.ballerinalang.testerina.test.BaseTestCase.balServer;
import static org.ballerinalang.testerina.test.BaseTestCase.projectBasedTestsPath;

public class ModuleExecutionWithInitStartFailuresTest {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("module-execution-tests-with-init-start-failures").toString();
    }

    @Test()
    public void testModuleExecutionFlow() throws BallerinaTestException, IOException {
        String[] args = new String[]{};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("ModuleExecutionWithInitStartFailures.txt", output);
    }

    @AfterMethod
    public void copyExec() {
        try {
            FileUtils.copyBallerinaExec(Paths.get(projectPath), String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            // ignore exception
        }
    }
}
