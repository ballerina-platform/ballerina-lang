/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.transaction;

import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;

/**
 * Testing micro transaction header behaviour.
 */
public class MicroTransactionTestCase {
    private ServerInstance initiator;
    private ServerInstance participant1;
    private ServerInstance participant2;

    @BeforeClass
    private void setup() throws Exception {
        initiator = ServerInstance.initBallerinaServer(8888);
        initiator.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "initiator.bal").getAbsolutePath());

        participant1 = ServerInstance.initBallerinaServer(8889);
        participant1.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "participant1.bal").getAbsolutePath());

        participant2 = ServerInstance.initBallerinaServer(8890);
        participant2.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "participant2.bal").getAbsolutePath());
    }

    @Test(description = "Test participant1 transaction id")
    public void testParticipantTransactionId() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp(""));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "equal id", "payload mismatched");
    }

    @AfterClass
    private void cleanup() throws Exception {
        initiator.stopServer();
        participant1.stopServer();
        participant2.stopServer();
    }
}
