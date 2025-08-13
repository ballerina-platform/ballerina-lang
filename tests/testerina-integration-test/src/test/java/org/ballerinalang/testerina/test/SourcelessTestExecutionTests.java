package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Test case to test sourceless tests in a project.
 */
public class SourcelessTestExecutionTests extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("sourceless-test-execution-tests").toString();
    }

    // Scenario : Modules only have tests and no sourcefiles. Default module doesnt have sources or tests
    @Test()
    public void test_SourcelessModule_TestExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"sourceless-modules-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput(projectPath, "SourcelessTestExecutionTests-" +
                "test_SourcelessModule_TestExecution.txt", output);
    }

    // Scenario : Modules only have tests and no sourcefiles. Default module doesnt have tests but has a source file
    @Test()
    public void test_DefaultModuleSourceOnly_TestExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"default-module-source-only-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput(projectPath, "SourcelessTestExecutionTests-" +
                "test_DefaultModuleSourceOnly_TestExecution.txt", output);
    }


    @Test()
    public void test_SourcelessProject_TestExecution() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"sourceless-project-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput(projectPath, "SourcelessTestExecutionTests-" +
                "test_SourcelessProject_TestExecution.txt", output);
    }
}
