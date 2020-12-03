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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test cases for listener written to be used in unit tests.
 */
@Test(groups = "mock-listener-tests")
public class ListenerEndpointTest {
    private static final String OBESERVABILITY_TEST_UTILS_BALO = System.getProperty("observability.test.utils.balo");
    private static final String OBESERVABILITY_TEST_UTILS_JAR = System.getProperty("observability.test.utils.jar");
    private static final String BALLERINA_TOML_TEST_NATIVES_JAR_NAME = "observability-test-utils.jar";

    private static BalServer balServer;
    private static BServerInstance servicesServerInstance;

    private static final String SERVICE_BASE_URL = "http://localhost:9091/testServiceOne";

    @BeforeGroups(value = "mock-listener-tests", alwaysRun = true)
    private void setup() throws Exception {
        balServer = new BalServer();
        final String serverHome = balServer.getServerHome();
        final Path testUtilsJar = Paths.get(OBESERVABILITY_TEST_UTILS_JAR);

        // Copy jar for Ballerina.toml reference to natives Jar
        copyFile(testUtilsJar, Paths.get(testUtilsJar.getParent().toString(),
                BALLERINA_TOML_TEST_NATIVES_JAR_NAME));

        // Copy jar for bre/libs
        copyFile(testUtilsJar, Paths.get(serverHome, "bre", "lib", testUtilsJar.getFileName().toString()));

        // Copy caches
        try (FileSystem fs = FileSystems.newFileSystem(Paths.get(OBESERVABILITY_TEST_UTILS_BALO),
                ListenerEndpointTest.class.getClassLoader())) {
            copyDir(fs.getPath("/"), Paths.get(serverHome, "repo"));
        }

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);
        String sourcesDir = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "listener_tests").getAbsolutePath();
        servicesServerInstance.startServer(sourcesDir, "listener_tests", null, new String[0], new int[]{9091});
    }

    @AfterGroups(value = "mock-listener-tests", alwaysRun = true)
    private void cleanup() throws Exception {
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
        balServer.cleanup();
    }

    @Test
    public void testHelloWorldResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceOne",
                "dummy-ignored-input-1", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello from Resource One");
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceTwo",
                "10", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Sum of numbers: 55");
    }

    @Test
    public void testErrorReturnResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceTwo",
                "invalid-number", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "{ballerina/lang.int}NumberParsingError");
    }

    @Test
    public void testPanicResponse() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceThree",
                "dummy-ignored-input-2", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "Test Error");
    }

    private void copyDir(Path source, Path dest) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            Path relativeSourcePath = source.relativize(sourcePath);
            try {
                Path targetPath = dest.resolve(relativeSourcePath.toString());
                if (!targetPath.toFile().isDirectory() || !targetPath.toFile().exists()) {
                    copyFile(sourcePath, targetPath);
                }
            } catch (IOException ex) {
                Assert.fail("Failed to copy file " + relativeSourcePath + " in directory " + source.toString()
                        + " to " + dest.toString(), ex);
            }
        });
    }

    private void copyFile(Path source, Path dest) throws IOException {
        dest.getParent().toFile().mkdirs();     // Create parent directory
        Files.copy(source, dest, REPLACE_EXISTING);
    }
}
