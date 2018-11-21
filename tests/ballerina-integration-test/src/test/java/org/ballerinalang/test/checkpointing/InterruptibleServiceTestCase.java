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
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.persistence.store.impl.FileStorageProvider;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for interruptible services check-pointing and resume.
 *
 * @since 0.981.1
 */
public class InterruptibleServiceTestCase extends BaseTest {

    private FileStorageProvider fileStorageProvider;

    private String balFilePath;

    private String[] args;

    @BeforeClass
    public void setup() {
        balFilePath = new File("src" + File.separator + "test" + File.separator +
                                       "resources" + File.separator + "checkpointing" + File.separator +
                                       "interruptibleService.bal").getAbsolutePath();
        File statesStorageDir = new File("target" + File.separator + "ballerina-states");
        if (statesStorageDir.exists()) {
            statesStorageDir.delete();
        }
        String statesStoragePath = statesStorageDir.getAbsolutePath();
        String osName = Utils.getOSName();
        if (osName != null && osName.toLowerCase(Locale.ENGLISH).contains("windows")) {
            statesStoragePath = statesStoragePath.replace("\\", "\\\\");
        }
        args = new String[] { "-e", FileStorageProvider.INTERRUPTIBLE_STATES_FILE_PATH + "=" + statesStoragePath };
        ConfigRegistry.getInstance().addConfiguration(FileStorageProvider.INTERRUPTIBLE_STATES_FILE_PATH,
                                                      statesStoragePath);
        fileStorageProvider = new FileStorageProvider();
    }

    @Test(description = "Checkpoint will be saved and server interrupt before complete the request.")
    public void testCheckpointSuccess() throws IOException, BallerinaTestException {
        BServerInstance ballerinaServer = new BServerInstance(balServer);
        try {
            int[] requiredPorts = new int[]{9090};
            ballerinaServer.startServer(balFilePath, args, requiredPorts);
            int servicePort = Constant.DEFAULT_HTTP_PORT;
            HttpResponse response = HttpClientRequest.doGet(ballerinaServer
                    .getServiceURLHttp(servicePort, "s1/r1"));
            Assert.assertNotNull(response);
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                      .until(() -> fileStorageProvider.getAllSerializedStates().size() > 0);
        } finally {
            ballerinaServer.killServer();
        }
        List<String> allSerializedStates = fileStorageProvider.getAllSerializedStates();
        Assert.assertEquals(allSerializedStates.size(), 1,
                            "Checkpoint haven't been save during request processing.");
    }

    @Test(description = "Resume the request after server started from last checkPointed state",
          priority = 1)
    public void testCheckpointResumeSuccess() throws BallerinaTestException {
        BServerInstance ballerinaServer = new BServerInstance(balServer);
        try {
            int[] requiredPorts = new int[]{9090};
            ballerinaServer.startServer(balFilePath, args, requiredPorts);
            Awaitility.await().atMost(20, TimeUnit.SECONDS)
                      .until(() -> fileStorageProvider.getAllSerializedStates().size() == 0);
        } finally {
            ballerinaServer.shutdownServer();
        }
        List<String> allSerializedStates = fileStorageProvider.getAllSerializedStates();
        Assert.assertEquals(allSerializedStates.size(), 0,
                            "Server has not been resumed the checkpoint and complete it.");
    }
}
