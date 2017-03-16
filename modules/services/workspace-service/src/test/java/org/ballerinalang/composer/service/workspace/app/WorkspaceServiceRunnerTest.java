package org.ballerinalang.composer.service.workspace.app;

import org.ballerinalang.composer.service.workspace.Constants;
import org.testng.annotations.Test;

/**
 * Tests for Workspace Mirco-Service Runner.
 */
public class WorkspaceServiceRunnerTest {
    
    @Test
    public void testEnableCloudModeViaArgument() {
        String[] args = {"-cloudMode"};
        System.setProperty(Constants.SYS_BAL_COMPOSER_HOME, System.getProperty("basedir"));
        WorkspaceServiceRunner.main(args);
    }
    
}
