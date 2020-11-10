/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerina.testobserve;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test cases for listener written to be used in unit tests.
 */
@Test(groups = "mock-listener-tests")
public class ListenerEndpointTest {
    private static final String TEST_OBSERVE_JAR = "ballerina-testobserve-0.0.0.jar";
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";

    private static BalServer balServer;
    private static BServerInstance servicesServerInstance;

    @BeforeGroups(value = "mock-listener-tests", alwaysRun = true)
    private void setup() throws Exception {
        balServer = new BalServer();
        final String serverHome = balServer.getServerHome();

        // Copy for Ballerina.toml reference to natives Jar
        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),
                Paths.get(Paths.get(System.getProperty(TEST_NATIVES_JAR)).getParent().toString(), TEST_NATIVES_JAR)
                        .toFile());

        // Copy to bre/libs
        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),
                Paths.get(serverHome, "bre", "lib", TEST_NATIVES_JAR).toFile());
        copyFile(Paths.get("build", "generated-bir-jar", TEST_OBSERVE_JAR).toFile(),
                Paths.get(serverHome, "bre", "lib", TEST_OBSERVE_JAR).toFile());

        // Copy to lib/repo
        File observeTestBaloFile =
                Paths.get("build", "generated-balo", "repo", "ballerina", "testobserve").toFile();
        FileUtils.copyDirectoryToDirectory(observeTestBaloFile,
                Paths.get(serverHome, "lib", "repo", "ballerina").toFile());

        // Copy to bir-cache
        FileUtils.copyDirectoryToDirectory(observeTestBaloFile,
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());
        FileUtils.copyDirectoryToDirectory(
                Paths.get("build", "generated-bir", "ballerina", "testobserve").toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);
        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "listener").getAbsolutePath();
        servicesServerInstance.startServer(basePath, "testservice", null, new String[0], new int[]{9091});
    }

    @AfterGroups(value = "mock-listener-tests", alwaysRun = true)
    private void cleanup() throws Exception {
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
        balServer.cleanup();
    }

    @Test
    public void testHelloWorldResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceOne",
                "dummy-ignored-input-1", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello from Resource One");
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceTwo",
                "10", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Sum of numbers: 55");
    }

    @Test
    public void testErrorReturnResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceTwo",
                "invalid-number", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "{ballerina/lang.int}NumberParsingError");
    }

    @Test
    public void testPanicResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceThree",
                "dummy-ignored-input-2", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "Test Error");
    }

    private void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }
}
