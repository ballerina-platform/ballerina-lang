package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test case to test sourceless tests in a project.
 */
public class SourcelessTestExecutionTests extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("sourceless-test-execution-tests").toString();
    }

    // Scenario : Modules only have tests and no sourcefiles. Default module doesnt have sources or tests
    @Test()
    public void test_SourcelessModule_TestExecution() throws BallerinaTestException {
        String msg1 = "sourcelessModulesTest.module1\n\n\n\t\t1 passing";
        String msg2 = "sourcelessModulesTest.module1\n\n\n\t\t1 passing";
        String[] args = mergeCoverageArgs(new String[]{"sourceless-modules-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to function mocking failure in test framework.");
        }
    }

    // Scenario : Modules only have tests and no sourcefiles. Default module doesnt have tests but has a source file
    @Test()
    public void test_DefaultModuleSourceOnly_TestExecution() throws BallerinaTestException {
        String msg1 = "defaultModuleSource.module1\n\n\n\t\t1 passing";
        String msg2 = "defaultModuleSource.module2\n\n\n\t\t1 passing";
        String[] args = mergeCoverageArgs(new String[]{"default-module-source-only-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to function mocking failure in test framework.");
        }
    }


    @Test()
    public void test_SourcelessProject_TestExecution() throws BallerinaTestException {
        String msg1 = "sourcelessProjectTest\n\n\n\t\t1 passing";
        String msg2 = "sourcelessProjectTest.module1\n\n\n\t\t1 passing";
        String msg3 = "sourcelessProjectTest.module2\n\n\n\t\t1 passing";
        String[] args = mergeCoverageArgs(new String[]{"sourceless-project-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2) || !output.contains(msg3)) {
            Assert.fail("Test failed due to function mocking failure in test framework.");
        }
    }
}
