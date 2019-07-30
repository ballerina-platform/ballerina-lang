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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test cases for observability tracing.
 */
@Test(groups = "tracing-test")
public class TracingTestCase extends BaseTest {
    private static BServerInstance serverInstance;

    private static final String OBESERVABILITY_TEST_BIR = System.getProperty("observability.test.utils");
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "tracing" + File.separator;
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";
    private static final String TEST_OBSERVE_JAR = "testobserve.jar";

    private static final String DEST_FUNCTIONS_JAR = File.separator + "bre" + File.separator + "lib"
            + File.separator + TEST_NATIVES_JAR;

    @BeforeGroups(value = "tracing-test", alwaysRun = true)
    private void setup() throws Exception {
        int[] requiredPorts = new int[]{9090, 9091, 9092};
        serverInstance = new BServerInstance(balServer);

        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)), new File(serverInstance.getServerHome()
                + DEST_FUNCTIONS_JAR));

        // copy to bre/libs
        Path observeTestBaloPath =
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-balo", "repo", "ballerina", "testobserve");
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverInstance.getServerHome(), "lib", "repo", "ballerina").toFile());

        // copy to bir-cache
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverInstance.getServerHome(), "bir-cache", "ballerina").toFile());
        FileUtils.copyDirectoryToDirectory(
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir", "ballerina", "testobserve").toFile(),
                Paths.get(serverInstance.getServerHome(), "bir-cache", "ballerina").toFile());

        // copy code-gen-ed generated-jar
        copyFile(Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir-jar", TEST_OBSERVE_JAR).toFile(),
                Paths.get(serverInstance.getServerHome(), "bre", "lib", TEST_OBSERVE_JAR).toFile());

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "observability" + File.separator + "tracing").getAbsolutePath();

        String configFile = new File(RESOURCE_LOCATION + "ballerina.conf").getAbsolutePath();
        String[] args = new String[]{"--config", configFile};
        serverInstance.startServer(basePath, "tracingservices", args, requiredPorts);
    }

    @AfterGroups(value = "tracing-test", alwaysRun = true)
    private void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

    @Test
    public void testOOTBTracing() throws Exception {
        final String service = "http://localhost:9090/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        Type type = new TypeToken<List<BMockSpan>>() {
        }.getType();
        String data = HttpClientRequest.doGet(service + "getMockTracers").getData();
        PrintStream out = System.out;
        out.println(data);
        List<BMockSpan> mockSpans = new Gson().fromJson(data, type);

        // 1. helloWorld_service_0 -> resourceOne (Root Span)
        // 2. helloWorld_service_0 -> ballerina/http/Client:get
        // 3. helloWorld_service_0 -> ballerina/http/HttpClient:get
        // 4. helloWorld_service_0 -> resourceTwo
        // 5. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 6. helloWorld_service_0 -> ballerina/http/Caller:respond
        Assert.assertEquals(mockSpans.size(), 6, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1, "Mismatch in number of root spans.");
    }

    @Test
    public void testObservePackageUserTraceFalse() throws Exception {
        final String service = "http://localhost:9091/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        Type type = new TypeToken<List<BMockSpan>>() {
        }.getType();
        String data = HttpClientRequest.doGet(service + "getMockTracers").getData();
        PrintStream out = System.out;
        out.println(data);
        List<BMockSpan> mockSpans = new Gson().fromJson(data, type);

        // 1. helloWorld_service_0 -> resourceOne (Root Span)
        // 2. helloWorld_service_0 -> ballerina/http/Client:get
        // 3. helloWorld_service_0 -> ballerina/http/HttpClient:get
        // 4. helloWorld_service_0 -> resourceTwo
        // 5. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 6. helloWorld_service_0 -> ballerina/http/Caller:respond

        // 7. helloWorld_service_0 -> getMockTracers (Root Span)
        // 8. helloWorld_service_0 -> ballerina/http/Caller:respond

        // 9. helloWorld_service_0 -> resourceOne (Root Span)
        // 10. helloWorld_service_0 -> uSpanOne
        // 11. helloWorld_service_0 -> uSpanTwo
        // 12. helloWorld_service_0 -> ballerina/http/Client:get
        // 13. helloWorld_service_0 -> ballerina/http/HttpClient:get
        // 14. helloWorld_service_0 -> resourceTwo
        // 15. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 16. helloWorld_service_0 -> ballerina/http/Caller:respond
        Assert.assertEquals(mockSpans.size(), 16, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans
                .stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0)
                .count(), 3, "Mismatch in number of root spans.");
    }

    @Test
    public void testObservePackageUserTraceTrue() throws Exception {
        final String service = "http://localhost:9092/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        Type type = new TypeToken<List<BMockSpan>>() {
        }.getType();
        String data = HttpClientRequest.doGet(service + "getMockTracers").getData();
        PrintStream out = System.out;
        out.println(data);
        List<BMockSpan> mockSpans = new Gson().fromJson(data, type);

        // 1. helloWorld_service_0 -> resourceOne (Root Span)
        // 2. helloWorld_service_0 -> ballerina/http/Client:get
        // 3. helloWorld_service_0 -> ballerina/http/HttpClient:get
        // 4. helloWorld_service_0 -> resourceTwo
        // 5. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 6. helloWorld_service_0 -> ballerina/http/Caller:respond

        // 7. helloWorld_service_0 -> getMockTracers (Root Span)
        // 8. helloWorld_service_0 -> ballerina/http/Caller:respond

        // 9. helloWorld_service_0 -> resourceOne (Root Span)
        // 10. helloWorld_service_0 -> uSpanOne
        // 11. helloWorld_service_0 -> uSpanTwo
        // 12. helloWorld_service_0 -> ballerina/http/Client:get
        // 13. helloWorld_service_0 -> ballerina/http/HttpClient:get
        // 14. helloWorld_service_0 -> resourceTwo
        // 15. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 16. helloWorld_service_0 -> ballerina/http/Caller:respond

        // 17. helloWorld_service_0 -> getMockTracers (Root Span)
        // 18. helloWorld_service_0 -> ballerina/http/Caller:respond

        // 19. helloWorld_service_0 -> resourceOne (Root Span)
        // 20. helloWorld_service_0 -> ballerina/http/Client:get
        // 21. helloWorld_service_0 -> ballerina/http/HttpClient:get
        // 22. helloWorld_service_0 -> resourceTwo
        // 23. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 24. helloWorld_service_0 -> ballerina/http/Caller:respond
        // 24. helloWorld_service_0 -> uSpanThree (Root Span)
        // 24. helloWorld_service_0 -> uSpanFour
        Assert.assertEquals(mockSpans.size(), 26, "Mismatch in number of spans reported.");

        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 6, "Mismatch in number of root spans.");

        Optional<BMockSpan> uSpanTwo = mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getOperationName().equals("uSpanFour")).findFirst();

        Assert.assertTrue(uSpanTwo.isPresent());
        uSpanTwo.ifPresent(bMockSpan -> {
            Assert.assertEquals(bMockSpan.getTags().get("Allowed"), "Successful", "Tag not found");
            Assert.assertNull(bMockSpan.getTags().get("Disallowed"), "Unexpected tag found");
        });
    }

    private static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }
}
