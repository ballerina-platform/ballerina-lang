package org.wso2.integration.tooling.service.workspace.app;

import org.junit.Test;

import static org.junit.Assert.*;

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