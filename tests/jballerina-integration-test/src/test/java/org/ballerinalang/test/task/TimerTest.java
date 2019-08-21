/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.task;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This is the test class to test ballerina task scheduler timer related functionalities.
 */
@Test(groups = "task-test")
public class TimerTest extends TaskBaseTest {
    @BeforeClass
    public void setup() throws BallerinaTestException {
        super.setup();
        int[] requiredPorts = new int[]{15004};
        serverInstance.startServer(balFile, "timerservices", requiredPorts);
    }

    @Test(description = "Test task timer with attachment")
    public void testTaskTimerWithAttachment() {
        String message = "Sam is 5 years old";
        assertTest(10000, "http://localhost:15004/getTaskAttachmentResult", message);
    }

    @Test(description = "Test task timer with multiple services attached")
    public void testTaskTimerWithMultipleServices() {
        String message = "Multiple services invoked";
        assertTest(5000, "http://localhost:15004/getMultipleServicesResult", message);
    }

    @AfterClass
    public void cleanup() throws BallerinaTestException {
        super.cleanup();
    }
}
