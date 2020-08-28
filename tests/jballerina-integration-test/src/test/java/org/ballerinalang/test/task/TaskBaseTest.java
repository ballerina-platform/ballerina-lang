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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Base test class for ballerina task module integration tests.
 */
public class TaskBaseTest extends BaseTest {
    protected static BServerInstance serverInstance;
    protected static String balFile;

    void setup() throws BallerinaTestException {
        balFile = Paths.get("src", "test", "resources", "task").toAbsolutePath().toString();
        serverInstance = new BServerInstance(balServer);
    }

    void cleanup() throws BallerinaTestException {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

    // This class is does not have @BeforeGroups or @AfterGroups methods as there were issues with the test setup.
    // These tests need multiple main functions (Otherwise we can't run the functions using ballerina server instance),
    // hence they cannot be in the same ballerina test module.
    // Therefore the ballerina server invocation is moved to class level.
    private boolean validateResponse(String url, String message) {
        HttpResponse response;
        try {
            response = HttpClientRequest.doGet(url);
        } catch (IOException e) {
            return false;
        }
        Assert.assertNotNull(response);
        return message.equals(response.getData());
    }

    void assertTest(int timeout, String url, String message) {
        await().atMost(timeout, TimeUnit.MILLISECONDS).until(() -> validateResponse(url, message));
    }
}
