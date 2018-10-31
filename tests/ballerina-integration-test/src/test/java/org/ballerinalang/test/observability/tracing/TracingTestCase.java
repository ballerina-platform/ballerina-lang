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
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test cases for observability tracing.
 */
@Test(groups = "tracing-test")
public class TracingTestCase extends BaseTest {
    private static BServerInstance serverInstance;

    private static final String BASEDIR = System.getProperty("basedir");
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "tracing" + File.separator;
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";

    private static final String DEST_FUNCTIONS_JAR = File.separator + "bre" + File.separator + "lib"
            + File.separator + TEST_NATIVES_JAR;

    @BeforeGroups(value = "tracing-test", alwaysRun = true)
    private void setup() throws Exception {
        int[] requiredPorts = new int[]{9090, 9091, 9092};

        serverInstance = new BServerInstance(balServer);

        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)), new File(serverInstance.getServerHome()
                + DEST_FUNCTIONS_JAR));

        FileUtils.copyDirectoryToDirectory(
                new File(BASEDIR + File.separator + "target" + File.separator + "lib" + File.separator + "repo" +
                        File.separator + "ballerina" + File.separator + "testobserve"),
                new File(serverInstance.getServerHome()
                        + File.separator + "lib" + File.separator + "repo" + File.separator + "ballerina"));

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

        Assert.assertEquals(mockSpans.size(), 5, "Mismatch in number of spans reported.");
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

        Assert.assertEquals(mockSpans.size(), 14, "Mismatch in number of spans reported.");
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

        Assert.assertEquals(mockSpans.size(), 23, "Mismatch in number of spans reported.");

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
