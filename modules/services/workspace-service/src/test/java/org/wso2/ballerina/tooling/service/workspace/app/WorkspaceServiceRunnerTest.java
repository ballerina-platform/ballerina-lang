package org.wso2.ballerina.tooling.service.workspace.app;

import org.junit.Test;

/**
 * Tests for Workspace Mirco-Service Runner.
 */
public class WorkspaceServiceRunnerTest {

    @Test
    public void testEnableCloudModeViaArgument(){
        String[] args = {"-cloudMode"};
        WorkspaceServiceRunner.main(args);
    }

}