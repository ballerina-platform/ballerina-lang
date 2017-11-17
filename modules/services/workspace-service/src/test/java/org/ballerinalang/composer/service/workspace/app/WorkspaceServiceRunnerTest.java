/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.composer.service.workspace.app;

import org.ballerinalang.composer.service.workspace.Constants;
import org.testng.annotations.Test;

/**
 * Tests for Workspace Mirco-Service Runner.
 */
public class WorkspaceServiceRunnerTest {
    
    @Test(enabled = false)
    public void testEnableCloudModeViaArgument() {
        String[] args = {"-cloudMode", "src/test/resources/samples/service/helloWorld.bal"};
        System.setProperty(Constants.SYS_BAL_COMPOSER_HOME, System.getProperty("basedir"));
        WorkspaceServiceRunner.main(args);
    }
    
}
