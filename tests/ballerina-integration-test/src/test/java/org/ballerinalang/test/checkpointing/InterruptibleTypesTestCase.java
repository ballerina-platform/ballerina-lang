/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.checkpointing;

import org.awaitility.Awaitility;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for interruptible services with types and functions check-pointing and resume.
 *
 * @since 0.983.0
 */
public class InterruptibleTypesTestCase extends BaseInterruptibleTest {

    private String balFilePath;

    @BeforeClass
    public void setup() {
        super.setup("ballerina-types-states");
        balFilePath = new File("src" + File.separator + "test" + File.separator +
                                       "resources" + File.separator + "checkpointing" + File.separator +
                                       "interruptibleTypesService.bal").getAbsolutePath();
    }

    @Test(description = "Checkpoint will be saved and server interrupt before complete the request.")
    public void testTypesCheckpointSuccess() throws IOException, BallerinaTestException {
        BServerInstance ballerinaServer = new BServerInstance(balServer);
        try {
            ballerinaServer.startServer(balFilePath, args, requiredPorts);
            LogLeecher funcWaitingLog = new LogLeecher("Waiting on second request");
            ballerinaServer.addLogLeecher(funcWaitingLog);
            HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(servicePort, "s1/r1"));
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                      .until(() -> fileStorageProvider.getAllSerializedStates().size() > 0);
            funcWaitingLog.waitForText(3000);
        } finally {
            ballerinaServer.killServer();
        }
        List<String> allSerializedStates = fileStorageProvider.getAllSerializedStates();
        Assert.assertEquals(allSerializedStates.size(), 1, "Checkpoint haven't been save during request processing.");
    }

    @Test(description = "Resume the request after server started from last checkPointed state",
          priority = 1)
    public void testTypesResumeSuccess() throws BallerinaTestException, IOException {
        BServerInstance ballerinaServer = new BServerInstance(balServer);
        try {
            ballerinaServer.startServer(balFilePath, args, requiredPorts);
            LogLeecher resultInt = new LogLeecher("Int value:5");
            LogLeecher resultString = new LogLeecher("Ballerina");
            LogLeecher resultByte = new LogLeecher("23");
            LogLeecher resultFloat = new LogLeecher("Float value:20.0");
            LogLeecher resultBoolean = new LogLeecher("Boolean value:true");
            LogLeecher resultTemplate = new LogLeecher("Template value:Hello Ballerina!!!");
            LogLeecher resultJson =
                    new LogLeecher("[1, false, null, \"foo\", {\"last\":\"Pala\", \"first\":\"John\"}]");
            LogLeecher resultXml = new LogLeecher("<book>The Lost World</book>");
            LogLeecher resultTuple = new LogLeecher("(10, \"John\")");
            LogLeecher resultRecord = new LogLeecher("{name:\"Waruna\", id:1, height:175.1}");
            LogLeecher resultFunctionPointer = new LogLeecher("Function Pointer value :10");

            ballerinaServer.addLogLeecher(resultInt);
            ballerinaServer.addLogLeecher(resultString);
            ballerinaServer.addLogLeecher(resultByte);
            ballerinaServer.addLogLeecher(resultFloat);
            ballerinaServer.addLogLeecher(resultBoolean);
            ballerinaServer.addLogLeecher(resultTemplate);
            ballerinaServer.addLogLeecher(resultJson);
            ballerinaServer.addLogLeecher(resultXml);
            ballerinaServer.addLogLeecher(resultTuple);
            ballerinaServer.addLogLeecher(resultRecord);
            ballerinaServer.addLogLeecher(resultFunctionPointer);
            HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(servicePort, "s1/r2"));

            resultInt.waitForText(3000);
            resultString.waitForText(3000);
            resultByte.waitForText(3000);
            resultFloat.waitForText(3000);
            resultBoolean.waitForText(3000);
            resultTemplate.waitForText(3000);
            resultJson.waitForText(3000);
            resultXml.waitForText(3000);
            resultTuple.waitForText(3000);
            resultRecord.waitForText(3000);
            resultFunctionPointer.waitForText(3000);
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                      .until(() -> fileStorageProvider.getAllSerializedStates().size() == 0);
        } finally {
            ballerinaServer.shutdownServer();
        }
        List<String> allSerializedStates = fileStorageProvider.getAllSerializedStates();
        Assert.assertEquals(allSerializedStates.size(), 0, "Server hasn't resumed the checkpoint and complete it.");
    }

    @AfterTest
    public void cleanup() {
        super.cleanup();
    }
}
