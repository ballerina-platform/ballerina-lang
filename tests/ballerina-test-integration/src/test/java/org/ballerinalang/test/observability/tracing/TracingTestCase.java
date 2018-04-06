/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.observability.tracing;

import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TracingTestCase {

    private ServerInstance serverInstance;
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "tracing" + File.separator;
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";

    private static final String DEST_FUNCTIONS_JAR = File.separator + "bre" + File.separator + "lib"
            + File.separator + TEST_NATIVES_JAR;

    @BeforeClass
    private void setup() throws Exception {
        serverInstance = ServerInstance.initBallerinaServer();
        String balFile = new File(RESOURCE_LOCATION + "trace-test.bal").getAbsolutePath();
        serverInstance.setArguments(new String[]{balFile, "--observe", "-t", "name=BMockTracer"});

        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)), new File(serverInstance.getServerHome()
                + DEST_FUNCTIONS_JAR));

        serverInstance.startServer();
    }

    @Test
    public void testSpanWithTwoResources() throws IOException {
        HttpClientRequest.doGet("http://localhost:9090/echoService/resourceOne");
        Assert.assertEquals(HttpClientRequest.doGet(
                "http://localhost:9090/echoService/getFinishedSpansCount").getData(), "8");
    }

    private static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }

    @AfterClass
    private void cleanup() throws Exception {
        serverInstance.stopServer();
    }
}
