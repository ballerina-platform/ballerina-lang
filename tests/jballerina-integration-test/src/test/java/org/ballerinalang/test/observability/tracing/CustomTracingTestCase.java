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
public class CustomTracingTestCase extends TracingBaseTest {

    @Test
    public void testAddCustomSpanToSystemTrace() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9096/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "06_custom_trace_spans.bal:28:5";
        final String span3Position = "06_custom_trace_spans.bal:35:19";
        final String span5Position = "06_custom_trace_spans.bal:50:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceSix", "resourceOne");
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
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/test-service/resource-1"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSix"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "http")
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
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
            Assert.assertEquals(span.getOperationName(), "calculateSumWithObservableFunction");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSix"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("function", "calculateSumWithObservableFunction")
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
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span5Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSix"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testCustomTrace() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9096/test-service/resource-2");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource two");
        Thread.sleep(1000);

        final String span1Position = "06_custom_trace_spans.bal:64:5";
        final String span2Position = "06_custom_trace_spans.bal:85:15";
        final String span3Position = "06_custom_trace_spans.bal:74:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceSix", "resourceTwo");
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
            Assert.assertEquals(span.getOperationName(), "resourceTwo");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/test-service/resource-2"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSix"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
                    new AbstractMap.SimpleEntry<>("connector_name", "http")
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "calculateSumWithObservableFunction");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSix"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
                    new AbstractMap.SimpleEntry<>("function", "calculateSumWithObservableFunction")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSix"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
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
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
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
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
                    new AbstractMap.SimpleEntry<>("custom", "true"),
                    new AbstractMap.SimpleEntry<>("index", "4")
            ));
        });
    }
}
