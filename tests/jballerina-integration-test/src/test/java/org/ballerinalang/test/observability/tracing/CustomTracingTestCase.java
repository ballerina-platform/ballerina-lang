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
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test cases for custom trace spans.
 */
@Test(groups = "tracing-test")
public class CustomTracingTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "06_custom_trace_spans.bal";
    private static final String SERVICE_NAME = "testServiceFive";
    private static final String BASE_URL = "http://localhost:9095";

    @Test
    public void testAddCustomSpanToSystemTrace() throws Exception {
        final String resourceName = "resourceOne";
        final String span1Position = FILE_NAME + ":21:5";
        final String span3Position = FILE_NAME + ":28:19";
        final String span5Position = FILE_NAME + ":41:20";

        HttpResponse httpResponse = HttpClientRequest.doGet(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName);
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello! from resource one");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, resourceName);
        Assert.assertEquals(spans.size(), 5);
        Assert.assertEquals(spans.stream()
                        .filter(span -> !Objects.equals(span.getTags().get("custom"), "true"))
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span3Position, span5Position)));
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
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getOperationName(), "customSpanOne"))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("custom", "true"),
                    new AbstractMap.SimpleEntry<>("index", "1")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "calculateSumWithObservability");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("function", "calculateSumWithObservability")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getOperationName(), "customSpanTwo"))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("custom", "true"),
                    new AbstractMap.SimpleEntry<>("index", "2")
            ));
        });

        Optional<BMockSpan> span5 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span5Position))
                .findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span4.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span5Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testCustomTrace() throws Exception {
        final String resourceName = "resourceTwo";
        final String span1Position = FILE_NAME + ":51:5";
        final String span2Position = FILE_NAME + ":70:15";
        final String span3Position = FILE_NAME + ":59:20";

        HttpResponse httpResponse = HttpClientRequest.doGet(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName);
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello! from resource two");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, resourceName);
        Assert.assertEquals(spans.size(), 5);
        Assert.assertEquals(spans.stream()
                        .filter(span -> !Objects.equals(span.getTags().get("custom"), "true"))
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 2);

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
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
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
            Assert.assertEquals(span.getOperationName(), "calculateSumWithObservability");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("service", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("function", "calculateSumWithObservability")
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

        Optional<BMockSpan> customSpan1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getOperationName(), "customSpanThree"))
                .findFirst();
        Assert.assertTrue(customSpan1.isPresent());
        long customTraceId = customSpan1.get().getTraceId();
        customSpan1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("custom", "true"),
                    new AbstractMap.SimpleEntry<>("index", "3")
            ));
        });

        Optional<BMockSpan> customSpan2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getOperationName(), "customSpanFour"))
                .findFirst();
        Assert.assertTrue(customSpan2.isPresent());
        customSpan2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), customTraceId);
            Assert.assertEquals(span.getParentId(), customSpan1.get().getSpanId());
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("custom", "true"),
                    new AbstractMap.SimpleEntry<>("index", "4")
            ));
        });
    }
}
