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

package org.ballerinalang.test.cache;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Base class for ballerina task integration tests.
 */
public class CacheBaseTest extends BaseTest {
    protected static BServerInstance serverInstance;
    private static final int INVALID_NUMBER = 1000;

    @BeforeClass
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{19001};
        String balFile = Paths.get("src", "test", "resources", "cache").toAbsolutePath().toString();
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "cacheservices", requiredPorts);
    }

    @Test(description = "Ballerina cache expiry tests")
    public void testCacheExpiry() {
        // Check that the cache size gradually decreases due to cache expiry
        await().atMost(5, TimeUnit.SECONDS).until(() -> getResult() == 4);
        await().atMost(25, TimeUnit.SECONDS).until(() -> getResult() < 4);
        await().atMost(35, TimeUnit.SECONDS).until(() -> getResult() == 0);
    }

    private int getResult() {
        HttpResponse response;
        try {
            response = HttpClientRequest.doGet("http://localhost:19001/getCacheSize");
        } catch (IOException e) {
            return INVALID_NUMBER;
        }
        Assert.assertNotNull(response);
        String message = response.getData();
        return Integer.parseInt(message);
    }

    @AfterClass
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
