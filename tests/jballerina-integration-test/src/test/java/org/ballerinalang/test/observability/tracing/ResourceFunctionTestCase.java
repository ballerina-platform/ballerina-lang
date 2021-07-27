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

package org.ballerinalang.test.observability.tracing;

import org.ballerinalang.observe.mockextension.BMockSpan;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test cases for resource functions.
 */
@Test(groups = "tracing-test")
public class ResourceFunctionTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "02_resource_function.bal";
    private static final String BASE_PATH = "/testServiceTwo";
    private static final String BASE_URL = "http://localhost:19092";

    @DataProvider(name = "success-response-data-provider")
    public Object[][] getSuccessResponseData() {
        return new Object[][] {
                {"resourceOne", "15", FILE_NAME + ":22:5", FILE_NAME + ":28:20", "Sum of numbers: 120"},
                {"resourceTwo", "16", FILE_NAME + ":32:5", FILE_NAME + ":38:20", "Sum of numbers: 136"}
        };
    }

    @Test(dataProvider = "success-response-data-provider")
    public void testSimpleResourceCall(String resourceName, String requestPayload, String resourceFunctionPosition,
                                       String callerResponsePosition, String expectedResponsePayload) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + BASE_PATH + "/" + resourceName,
                requestPayload, Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), expectedResponsePayload);
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(BASE_PATH, DEFAULT_MODULE_ID, "/" + resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, callerResponsePosition)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId().equals(ZERO_SPAN_ID))
                .count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        String traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId().equals(traceId)
                    && mockSpan.getSpanId().equals(span.getParentId())));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", BASE_PATH + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.object.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), callerResponsePosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", callerResponsePosition),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }

    @DataProvider(name = "error-response-data-provider")
    public Object[][] getErrorResponseData() {
        return new Object[][] {
                {"resourceThree", FILE_NAME + ":42:5", "Test Error 1", "Test Error 1\n" +
                        "    at intg_tests.tracing_tests.0.$anonType$_0:$post$resourceThree" +
                        "(02_resource_function.bal:53)"},
                {"resourceFour", FILE_NAME + ":58:5", "Test Error 2", "Test Error 2\n" +
                        "    at intg_tests.tracing_tests.0.$anonType$_0:$post$resourceFour" +
                        "(02_resource_function.bal:69),"},
                {"resourceFive", FILE_NAME + ":75:5", "Test Error. Sum: 91", "Test Error. Sum: 91\n" +
                        "    at intg_tests.tracing_tests.0:panicAfterCalculatingSum(commons.bal:36)\n" +
                        "       intg_tests.tracing_tests.0.$anonType$_0:$post$resourceFive" +
                        "(02_resource_function.bal:76)"},
                {"resourceSix", FILE_NAME + ":81:5", "Test Error. Sum: 153", "Test Error. Sum: 153\n" +
                        "    at intg_tests.tracing_tests.0:panicAfterCalculatingSum(commons.bal:36)\n" +
                        "       intg_tests.tracing_tests.0.$anonType$_0:panicAfterCalculatingSum$lambda0$" +
                        "(02_resource_function.bal:82)\n" +
                        "       intg_tests.tracing_tests.0.$anonType$_0:$post$resourceSix" +
                        "(02_resource_function.bal:83)"}
        };
    }

    @Test(dataProvider = "error-response-data-provider")
    public void testResourceSingleSpanErrorResponse(String resourceName, String resourceFunctionPosition,
                                                    String expectedResponsePayload, String errorMessage)
            throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + BASE_PATH + "/" + resourceName,
                "15", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), expectedResponsePayload);
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(BASE_PATH, DEFAULT_MODULE_ID, "/" + resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Collections.singletonList(resourceFunctionPosition)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId().equals(ZERO_SPAN_ID))
                .count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        String traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId().equals(traceId)
                    && mockSpan.getSpanId().equals(span.getParentId())));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Map<String, Object> tags = span.getTags();
//            TODO: Remove the bellow line once #ballerina-lang/issues/28686 is fixed
            tags.keySet().removeIf(key -> key.equals("error.message"));
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", BASE_PATH + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("src.object.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("error", "true"),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
//                    TODO: Uncomment the bellow line once #ballerina-lang/issues/28686 is fixed
//                    new AbstractMap.SimpleEntry<>("error.message", errorMessage),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });
    }

    @DataProvider(name = "simple-remote-call-data-provider")
    public Object[][] getSimpleRemoteCallData() {
        return new Object[][] {
                {"resourceSeven", FILE_NAME + ":88:5", FILE_NAME + ":90:30", FILE_NAME + ":97:20",
                        "Sum of numbers: 12"},
                {"resourceEight", FILE_NAME + ":101:5", FILE_NAME + ":102:24", FILE_NAME + ":104:24",
                        "Successfully executed"}
        };
    }

    @Test(dataProvider = "simple-remote-call-data-provider")
    public void testResourceWithRemoteCall(String resourceName, String resourceFunctionPosition,
                                           String remoteCallPosition, String callerRespondPosition,
                                           String expectedPayload) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + BASE_PATH + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), expectedPayload);
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(BASE_PATH, DEFAULT_MODULE_ID, "/" + resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, remoteCallPosition, callerRespondPosition)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId().equals(ZERO_SPAN_ID))
                .count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        String traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId().equals(traceId)
                    && mockSpan.getSpanId().equals(span.getParentId())));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", BASE_PATH + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("src.object.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), remoteCallPosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", remoteCallPosition),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithReturn")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), callerRespondPosition))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", callerRespondPosition),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", BASE_PATH),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }
}
