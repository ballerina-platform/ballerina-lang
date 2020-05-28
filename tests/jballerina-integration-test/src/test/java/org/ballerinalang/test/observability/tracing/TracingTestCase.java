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
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
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
    private static final String TEST_OBSERVE_JAR = "ballerina.testobserve.jar";

    private static final String DEST_FUNCTIONS_JAR = File.separator + "bre" + File.separator + "lib"
            + File.separator + TEST_NATIVES_JAR;

    @BeforeGroups(value = "tracing-test", alwaysRun = true)
    private void setup() throws Exception {
        // Don't use 9898 port here. It is used in metrics test cases.
        int[] requiredPorts = new int[]{9090, 9091, 9092, 9093, 9095};
        serverInstance = new BServerInstance(balServer);

        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),  Paths.get(Paths.get(System.getProperty
                (TEST_NATIVES_JAR)).getParent() +  File.separator + TEST_NATIVES_JAR).toFile());

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
        String[] args = new String[] { "--b7a.config.file=" + configFile };
        serverInstance.startServer(basePath, "tracingservices", null, args, requiredPorts);
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
        List<BMockSpan> mockSpans = getCollectedSpans(service);

        // 1. echoService0 -> resourceOne (Root Span)
        // 2. echoService0 -> ballerina/http/Client:get
        // 3. echoService0 -> ballerina/http/HttpClient:get
        // 4. echoService0 -> resourceTwo
        // 5. echoService0 -> ballerina/http/Caller:respond
        // 6. echoService0 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 6, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1, "Mismatch in number of root spans.");

        Optional<BMockSpan> span1 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_trace_test_ootb.bal:24:5")).findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertEquals(span.getParentId(), 0);
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/echoService/resourceOne");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
        });

        Optional<BMockSpan> span2 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_trace_test_ootb.bal:56:32")).findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Client:get");
            Assert.assertEquals(span.getTags().size(), 8);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.base_url"), "http://localhost:9090/echoService");
            Assert.assertEquals(span.getTags().get("http.url"), "/resourceTwo");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
        });

        Optional<BMockSpan> span3 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "client_endpoint.bal:164:41")).findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/HttpClient:get");
            Assert.assertEquals(span.getTags().size(), 9);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina/http:");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "0");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "0xx");
            Assert.assertEquals(span.getTags().get("http.url"), "/echoService/resourceTwo");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("peer.address"), "localhost:9090");
        });

        Optional<BMockSpan> span4 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_trace_test_ootb.bal:36:5")).findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span3.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "resourceTwo");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/echoService/resourceTwo");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
        });

        Optional<BMockSpan> span5 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_trace_test_ootb.bal:39:20")).findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span4.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
        });

        Optional<BMockSpan> span6 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_trace_test_ootb.bal:29:24")).findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
        });
    }

    @Test(dependsOnMethods = "testOOTBTracing")
    public void testObservePackageUserTraceFalse() throws Exception {
        final String service = "http://localhost:9091/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        List<BMockSpan> mockSpans = getCollectedSpans(service);

        // 7. echoService0 -> getMockTracers (Root Span)
        // 8. echoService0 -> ballerina/http/Caller:respond

        // 9. echoService1 -> resourceOne (Root Span)
        // 10. echoService1 -> uSpanOne
        // 11. echoService1 -> uSpanTwo
        // 12. echoService1 -> ballerina/http/Client:get
        // 13. echoService1 -> ballerina/http/HttpClient:get
        // 14. echoService1 -> resourceTwo
        // 15. echoService1 -> ballerina/http/Caller:respond
        // 16. echoService1 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 16, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 3, "Mismatch in number of root spans.");
    }

    @Test(dependsOnMethods = "testObservePackageUserTraceFalse")
    public void testObservePackageUserTraceTrue() throws Exception {
        final String service = "http://localhost:9092/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        List<BMockSpan> mockSpans = getCollectedSpans(service);

        // 17. echoService1 -> getMockTracers (Root Span)
        // 18. echoService1 -> ballerina/http/Caller:respond

        // 19. echoService2 -> resourceOne (Root Span)
        // 20. echoService2 -> ballerina/http/Client:get
        // 21. echoService2 -> ballerina/http/HttpClient:get
        // 22. echoService2 -> resourceTwo
        // 23. echoService2 -> ballerina/http/Caller:respond
        // 24. echoService2 -> ballerina/http/Caller:respond

        // 25. echoService2 -> uSpanThree (Root Span)
        // 26. echoService2 -> uSpanFour

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

    @Test(dependsOnMethods = "testObservePackageUserTraceTrue")
    public void testOOTBTracingWithWorkers() throws Exception {
        final String service = "http://localhost:9093/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        List<BMockSpan> mockSpans = getCollectedSpans(service);

        // 27. echoService2 -> getMockTracers (Root Span)
        // 28. echoService2 -> ballerina/http/Caller:respond

        // 29. echoService3 -> resourceOne (Root Span)
        // 30. echoService3 -> w2
        // 31. echoService3 -> w1
        // 32. echoService3 -> ballerina/http/Client:get
        // 33. echoService3 -> ballerina/http/HttpClient:get
        // 34. echoService3 -> resourceTwo
        // 35. echoService3 -> ballerina/http/Caller:respond
        // 36. echoService3 -> w2
        // 37. echoService3 -> w1
        // 38. echoService3 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 38, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 8, "Mismatch in number of root spans.");
    }

    @Test(dependsOnMethods = "testOOTBTracingWithWorkers")
    public void testOOTBTracingWithErrors() throws Exception {
        final String service = "http://localhost:9094/echoService/";
        HttpClientRequest.doGet(service + "resourceOne/3");
        Thread.sleep(1000);
        List<BMockSpan> mockSpans = getCollectedSpans(service);

        // 39. echoService3 -> getMockTracers (Root Span)
        // 40. echoService3 -> ballerina/http/Caller:respond

        // 41. echoService4 -> resourceOne (Root Span)
        // 42. echoService4 -> ballerina/http/Client:get
        // 43. echoService4 -> ballerina/http/HttpClient:get
        // 44. echoService4 -> resourceTwo
        // 45. echoService4 -> ballerina/http/Caller:respond
        // 46. echoService4 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 46, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 10, "Mismatch in number of root spans.");

        HttpClientRequest.doGet(service + "resourceOne/2");
        Thread.sleep(1000);
        mockSpans = getCollectedSpans(service);

        // 47. echoService4 -> getMockTracers (Root Span)
        // 48. echoService4 -> ballerina/http/Caller:respond

        // 49. echoService4 -> resourceOne (Root Span)
        // 50. echoService4 -> ballerina/http/Client:get
        // 51. echoService4 -> ballerina/http/HttpClient:get
        // 52. echoService4 -> resourceTwo
        // 53. echoService4 -> ballerina/http/Caller:respond
        // 54. echoService4 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 54, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 12, "Mismatch in number of root spans.");

        HttpClientRequest.doGet(service + "resourceOne/1");
        Thread.sleep(1000);
        mockSpans = getCollectedSpans(service);

        // 55. echoService4 -> getMockTracers (Root Span)
        // 56. echoService4 -> ballerina/http/Caller:respond

        // 57. echoService4 -> resourceOne (Root Span)
        // 58. echoService4 -> ballerina/http/Client:get
        // 59. echoService4 -> ballerina/http/HttpClient:get
        // 60. echoService4 -> resourceTwo
        // 61. echoService4 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 61, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 14, "Mismatch in number of root spans.");

        HttpClientRequest.doGet(service + "resourceOne/0");
        Thread.sleep(1000);
        mockSpans = getCollectedSpans(service);

        // 62. echoService4 -> getMockTracers (Root Span)
        // 63. echoService4 -> ballerina/http/Caller:respond

        // 64. echoService4 -> resourceOne (Root Span)

        Assert.assertEquals(mockSpans.size(), 64, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 16, "Mismatch in number of root spans.");
    }

    @Test(dependsOnMethods = "testOOTBTracingWithErrors")
    public void testOOTBTracingForUserDefinedFunctions() throws Exception {
        final String service = "http://localhost:9095/echoService/";
        HttpClientRequest.doGet(service + "resourceOne");
        Thread.sleep(1000);
        List<BMockSpan> mockSpans = getCollectedSpans(service);

        // 65. echoService4 -> getMockTracers (Root Span)
        // 66. echoService4 -> ballerina/http/Caller:respond

        // 67. echoService5 -> resourceOne (Root Span)
        // 68. echoService5 -> ballerina/http/Client:get
        // 69. echoService5 -> ballerina/http/HttpClient:get
        // 70. echoService5 -> resourceTwo
        // 71. echoService5 -> getGreeting2
        // 72. echoService5 -> ballerina/http/Caller:respond
        // 73. echoService5 -> getGreeting1
        // 74. echoService5 -> ballerina/http/Caller:respond

        Assert.assertEquals(mockSpans.size(), 74, "Mismatch in number of spans reported.");
        Assert.assertEquals(mockSpans.stream()
                .filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 18, "Mismatch in number of root spans.");
    }

    private List<BMockSpan> getCollectedSpans(String service) throws IOException {
        String data = HttpClientRequest.doGet(service + "getMockTracers").getData();
        Type type = new TypeToken<List<BMockSpan>>() { }.getType();
        return new Gson().fromJson(data, type);
    }

    private static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }
}
