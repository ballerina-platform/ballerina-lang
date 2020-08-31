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
 * Test cases for out of the box tracing.
 */
@Test(groups = "tracing-test")
public class OOTBTracingTestCase extends TracingBaseTest {

    @Test
    public void testChainedResourceFunctions() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9091/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "01_ootb_chained_resource_functions.bal:27:5";
        final String span2Position = "01_ootb_chained_resource_functions.bal:33:45";
        final String span3Position = "client_endpoint.bal:164:41";
        final String span4Position = "01_ootb_chained_resource_functions.bal:49:5";
        final String span5Position = "01_ootb_chained_resource_functions.bal:52:20";
        final String span6Position = "01_ootb_chained_resource_functions.bal:42:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne");
        Assert.assertEquals(spans.size(), 6);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position, span4Position, span5Position,
                        span6Position)));
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Client:get");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.base_url", "http://localhost:9091/test-service"),
                    new AbstractMap.SimpleEntry<>("http.url", "/resource-2"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Client"),
                    new AbstractMap.SimpleEntry<>("action", "get")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/HttpClient:get");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina/http:1.0.0"),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("http.url", "/test-service/resource-2"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("peer.address", "localhost:9091"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/HttpClient"),
                    new AbstractMap.SimpleEntry<>("action", "get")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span3.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "resourceTwo");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/test-service/resource-2"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
                    new AbstractMap.SimpleEntry<>("connector_name", "http")
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwo"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });

        Optional<BMockSpan> span6 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span6Position))
                .findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span6Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testHTTPContextPropagation() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9092/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "02_ootb_http_context_propagation.bal:27:5";
        final String span2Position = "02_ootb_http_context_propagation.bal:34:45";
        final String span3Position = "client_endpoint.bal:164:41";
        final String span4Position = "01_echo_backend.bal:27:5";
        final String span5Position = "01_echo_backend.bal:30:20";
        final String span6Position = "02_ootb_http_context_propagation.bal:43:20";

        List<BMockSpan> testServiceSpans = this.getFinishedSpans("testServiceTwo");
        Assert.assertEquals(testServiceSpans.size(), 4);
        Assert.assertEquals(testServiceSpans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position, span6Position)));
        Assert.assertEquals(testServiceSpans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        List<BMockSpan> echoBackendSpans = this.getEchoBackendFinishedSpans();
        Assert.assertEquals(echoBackendSpans.size(), 2);
        Assert.assertEquals(echoBackendSpans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span4Position, span5Position)));
        Assert.assertEquals(echoBackendSpans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 0);

        Optional<BMockSpan> span1 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(testServiceSpans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceTwo"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "http")
            ));
        });

        Optional<BMockSpan> span2 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Client:get");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.base_url", "http://localhost:10011/echo-service"),
                    new AbstractMap.SimpleEntry<>("http.url", "/echo/Hello Echo !"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceTwo"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Client"),
                    new AbstractMap.SimpleEntry<>("action", "get")
            ));
        });

        Optional<BMockSpan> span3 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/HttpClient:get");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina/http:1.0.0"),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("http.url", "/echo-service/echo/Hello%20Echo%20!"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("peer.address", "localhost:10011"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceTwo"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/HttpClient"),
                    new AbstractMap.SimpleEntry<>("action", "get")
            ));
        });

        Optional<BMockSpan> span4 = echoBackendSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span3.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/backend:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/echo-service/echo/Hello%20Echo%20!"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "echoServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "http")
            ));
        });

        Optional<BMockSpan> span5 = echoBackendSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span5Position))
                .findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span4.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/backend:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span5Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "echoServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });

        Optional<BMockSpan> span6 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span6Position))
                .findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span6Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceTwo"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testWorkers() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9093/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "03_ootb_workers.bal:28:5";
        final String w1SpansPosition = "03_ootb_workers.bal:42:5";
        final String w2SpansPosition = "03_ootb_workers.bal:49:5";
        final String w3SpansPosition = "03_ootb_workers.bal:65:5";
        final String w4SpansPosition = "03_ootb_workers.bal:79:24"; // TODO : Update after fork worker pos is fixed
        final String w5SpansPosition = "03_ootb_workers.bal:88:4";  // TODO : Update after fork worker pos is fixed
        final String span3Position = "03_ootb_workers.bal:37:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceThree");
        Assert.assertEquals(spans.size(), 11);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, w1SpansPosition, w2SpansPosition, w3SpansPosition,
                        w4SpansPosition, w5SpansPosition, span3Position)));
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "http")
            ));
        });

        List<BMockSpan> w1Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w1SpansPosition))
                .collect(Collectors.toList());
        Assert.assertEquals(w1Spans.size(), 3);
        w1Spans.forEach(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w1");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", w1SpansPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne")
            ));
        });

        List<BMockSpan> w2Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w2SpansPosition))
                .collect(Collectors.toList());
        Assert.assertEquals(w2Spans.size(), 3);
        w2Spans.forEach(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w2");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", w2SpansPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne")
            ));
        });

        Optional<BMockSpan> w3Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w3SpansPosition))
                .findFirst();
        Assert.assertTrue(w3Spans.isPresent());
        w3Spans.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w3");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", w3SpansPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne")
            ));
        });

        Optional<BMockSpan> w4Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w4SpansPosition))
                .findFirst();
        Assert.assertTrue(w4Spans.isPresent());
        w4Spans.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w4");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", w4SpansPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne")
            ));
        });

        Optional<BMockSpan> w5Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w5SpansPosition))
                .findFirst();
        Assert.assertTrue(w5Spans.isPresent());
        w5Spans.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w5");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", w5SpansPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne")
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceThree"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testObservableAnnotation() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9094/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "04_ootb_observe_annotation.bal:27:5";
        final String span2Position = "04_ootb_observe_annotation.bal:31:23";
        final String span3Position = "04_ootb_observe_annotation.bal:43:23";
        final String span4Position = "04_ootb_observe_annotation.bal:54:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFour");
        Assert.assertEquals(spans.size(), 4);
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
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/test-service/resource-1"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceFour"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceFour"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
            Assert.assertEquals(span.getOperationName(), "ballerina-test/tracingservices/ObservableAdder:getSum");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("service", "testServiceFour"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("function", "getSum"),
                    new AbstractMap.SimpleEntry<>("object_name", "ballerina-test/tracingservices/ObservableAdder")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceFour"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testAsyncCall() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9097/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "07_ootb_async.bal:27:5";
        final String span2Position = "07_ootb_async.bal:31:35";
        final String span3Position = "07_ootb_async.bal:44:35";
        final String span4Position = "07_ootb_async.bal:56:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceSeven");
        Assert.assertEquals(spans.size(), 4);
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
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/test-service/resource-1"),
                    new AbstractMap.SimpleEntry<>("http.method", "GET"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSeven"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceSeven"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
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
            Assert.assertEquals(span.getOperationName(), "ballerina-test/tracingservices/ObservableAdder:getSum");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSeven"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("function", "getSum"),
                    new AbstractMap.SimpleEntry<>("object_name", "ballerina-test/tracingservices/ObservableAdder")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("http.status_code", "200"),
                    new AbstractMap.SimpleEntry<>("http.status_code_group", "2xx"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceSeven"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceOne"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/http/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testMainMethod() throws Exception {
        final String span1Position = "08_main_method.bal:17:1";
        final String span2Position = "08_main_method.bal:20:15";

        List<BMockSpan> spans = this.getFinishedSpans("Unknown Service");
        Assert.assertEquals(spans.size(), 2);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), "main");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", "ballerina-test/tracingservices:0.0.1"),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.main", "true"),
                    new AbstractMap.SimpleEntry<>("function", "main")
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
                    new AbstractMap.SimpleEntry<>("function", "calculateSumWithObservableFunction")
            ));
        });
    }
}
