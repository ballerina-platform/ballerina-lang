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

import org.ballerina.testobserve.tracing.extension.BMockSpan;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test cases for remove function calls.
 */
@Test(groups = "tracing-test")
public class RemoteCallTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "03_remote_call.bal";
    private static final String SERVICE_NAME = "testServiceTwo";
    private static final String BASE_URL = "http://localhost:9092";

    @Test
    public void testNestedRemoteCalls() throws Exception {
        final String resourceName = "resourceOne";
        final String span1Position = FILE_NAME + ":21:5";
        final String span2Position = FILE_NAME + ":22:9";
        final String span3Position = COMMONS_FILE_NAME + ":37:9";
        final String span4Position = FILE_NAME + ":23:20";

        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position, span4Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(),
                    "ballerina-test/testservices/MockClient:callAnotherRemoteFunction");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callAnotherRemoteFunction")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithNoReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithNoReturn")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @DataProvider(name = "remote-error-return-data-provider")
    public Object[][] getRemoteErrorReturnData() {
        return new Object[][] {
                {"resourceTwo", FILE_NAME + ":27:5", FILE_NAME + ":28:15"},
                {"resourceThree", FILE_NAME + ":33:5", FILE_NAME + ":34:20"}
        };
    }

    @Test(dataProvider = "remote-error-return-data-provider")
    public void testRemoteFunctionErrorReturn(String resourceName, String resourceFunctionPosition,
                                              String remoteCallPosition) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "Test Error");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, remoteCallPosition)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), remoteCallPosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithErrorReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", remoteCallPosition),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithErrorReturn"),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });
    }

    @Test
    public void testIgnoredErrorReturnInRemoteCall() throws Exception {
        final String resourceName = "resourceFour";
        final String span1Position = FILE_NAME + ":39:5";
        final String span2Position = FILE_NAME + ":40:19";
        final String span3Position = FILE_NAME + ":41:20";

        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithErrorReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithErrorReturn"),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testTrappedPanic() throws Exception {
        final String resourceName = "resourceFive";
        final String span1Position = FILE_NAME + ":45:5";
        final String span2Position = FILE_NAME + ":46:24";
        final String span3Position = FILE_NAME + ":48:24";

        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Successfully trapped panic: Test Error");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithPanic");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithPanic"),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }
}
