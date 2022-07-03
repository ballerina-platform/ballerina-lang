package org.ballerinalang.debugger.test.adapter.runinterminal;

import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class to test the runInTerminal feature for project based sources.
 */

public class ProjectBasedRunInTerminalTest {
    DebugTestRunner debugTestRunner;

    @BeforeMethod(alwaysRun = true)
    public void setup() throws BallerinaTestException {
        String testFolderName = "basic-project";
        String testSingleFileName = "hello_world.bal";
        debugTestRunner = new DebugTestRunner(testFolderName, testSingleFileName, true);
    }

    @Test(description = "Debug launch test in integrated terminal for project based source")
    public void testRunInIntegratedTerminal() throws BallerinaTestException {
        String integratedTerminal = "integrated";
        Boolean didRunInIntegratedTerminal = debugTestRunner.initDebugSessionInTerminal(
                DebugUtils.DebuggeeExecutionKind.RUN, integratedTerminal);
        Assert.assertTrue(didRunInIntegratedTerminal);
    }

    @Test(description = "Debug launch test in external terminal for project based source")
    public void testRunInExternalTerminal() throws BallerinaTestException {
        String externalTerminal = "external";
        Boolean didRunInIntegratedTerminal = debugTestRunner.initDebugSessionInTerminal(
                DebugUtils.DebuggeeExecutionKind.RUN, externalTerminal);

        // returned value should be true since external terminal requests are also handled as integrated terminal
        // requests, as of now.
        Assert.assertTrue(didRunInIntegratedTerminal);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
