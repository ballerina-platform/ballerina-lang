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
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.runtime.observability.ObservabilityConstants.DEFAULT_SERVICE_NAME;

/**
 * Test cases for main function.
 */
@Test(groups = "tracing-test")
public class MainFunctionTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "01_main_function.bal";

    @Test
    public void testMainMethod() throws Exception {
        final String span1Position = FILE_NAME + ":19:1";
        final String span2Position = FILE_NAME + ":20:5";
        final String span3Position = MOCK_CLIENT_FILE_NAME + ":32:9";
        final String span4Position = FILE_NAME + ":24:15";
        final String span5Position = FILE_NAME + ":32:21";
        final String span6Position = FILE_NAME + ":38:16";
        final String entryPointFunctionModule = DEFAULT_MODULE_ID;
        final String entryPointFunctionName = "main";
        final List<BMockSpan.CheckPoint> expectedCheckpoints = Arrays.asList(
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":22:13"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":24:15"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":32:16"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":32:21"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":33:11"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":38:16"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":39:11"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":44:43"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":44:43"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":54:31"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":55:5"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":55:5"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":56:11"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":56:5"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":56:5"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":57:1"),
                new BMockSpan.CheckPoint(entryPointFunctionModule, FILE_NAME + ":29:9")
        );
        List<BMockSpan> spans = this.getFinishedSpans(DEFAULT_SERVICE_NAME);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position, span4Position,
                        span5Position, span6Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId().equals(ZERO_SPAN_ID))
                .count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        String traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId().equals(traceId)
                    && mockSpan.getSpanId().equals(span.getParentId())));
            Assert.assertEquals(span.getOperationName(), "main");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", entryPointFunctionModule),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", entryPointFunctionName),
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.main", "true"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "main")
            ));
            Assert.assertEquals(span.getCheckpoints(), expectedCheckpoints);
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(),
                    MOCK_CLIENT_OBJECT_NAME + ":callAnotherRemoteFunction");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", entryPointFunctionModule),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", entryPointFunctionName),
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callAnotherRemoteFunction")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithNoReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", entryPointFunctionModule),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", entryPointFunctionName),
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", UTILS_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithNoReturn")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":calculateSum");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", entryPointFunctionModule),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", entryPointFunctionName),
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "calculateSum")
            ));
        });

        Optional<BMockSpan> span5 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span5Position))
                .findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithPanic");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", entryPointFunctionModule),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", entryPointFunctionName),
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span5Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithPanic"),
                    new AbstractMap.SimpleEntry<>("error", "true"),
                    new AbstractMap.SimpleEntry<>("error.message", "Test Error\n" +
                            "\tat intg_tests.tracing_tests.utils.0.MockClient:callWithPanic(" +
                            "mock_client_endpoint.bal:58)\n" +
                            "\t   intg_tests.tracing_tests.0:main(01_main_function.bal:32)")
            ));
        });

        Optional<BMockSpan> span6 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span6Position))
                .findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithErrorReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", entryPointFunctionModule),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", entryPointFunctionName),
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span6Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithErrorReturn"),
                    new AbstractMap.SimpleEntry<>("error", "true"),
                    new AbstractMap.SimpleEntry<>("error.message", "Test Error\n" +
                            "\tat intg_tests.tracing_tests.utils.0.MockClient:callWithErrorReturn(" +
                            "mock_client_endpoint.bal:46)\n" +
                            "\t   intg_tests.tracing_tests.0:main(01_main_function.bal:38)")
            ));
        });
    }

    @Test
    public void testProgrammaticallyStartedService() throws Exception {
        final String serviceName = "intg_tests_tracing_tests_svc_0";
        final String basePath = "testServiceOne";
        final String resourceName = "resourceOne";
        final String resourceFunctionPosition = "01_main_function.bal:45:9";
        final String callerResponsePosition = "01_main_function.bal:51:24";

        HttpResponse httpResponse = HttpClientRequest.doPost(
                "http://localhost:19091/" + basePath + "/" + resourceName, "15", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Sum of numbers: 120");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(serviceName, DEFAULT_MODULE_ID, "/" + resourceName);
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
                    new AbstractMap.SimpleEntry<>("http.url", "/" + basePath + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", serviceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("src.object.name", serviceName),
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
                    new AbstractMap.SimpleEntry<>("entrypoint.service.name", serviceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.name", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("entrypoint.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }
}
