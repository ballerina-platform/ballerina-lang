/*
 *  Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.task;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests ballerina task scheduler functionality.
 */
@Test
public class MultipleAttachmentTest extends TaskBaseTest {
    @BeforeClass
    public void setup() throws BallerinaTestException {
        super.setup();
        int[] requiredPorts = new int[]{15007};
        serverInstance.startServer(balFile, "multipleattachments", requiredPorts);
    }

    @Test(description = "Tests the functionality of a scheduler with multiple attachments")
    public void testSchedulerWithMultipleAttachments() {
        String message = "Name: Sam Age: 29 A/C: 150590 Balance: 11.35";
        assertTest(5000, "http://localhost:15007/getMultipleAttachmentResult", message);
    }
}
