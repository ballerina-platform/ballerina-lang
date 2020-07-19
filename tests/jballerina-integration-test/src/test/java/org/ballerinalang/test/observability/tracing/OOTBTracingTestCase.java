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

import java.io.IOException;
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

        final String span1Position = "01_ootb_chained_resource_functions.bal:23:5";
        final String span2Position = "01_ootb_chained_resource_functions.bal:33:45";
        final String span3Position = "client_endpoint.bal:164:41";
        final String span4Position = "01_ootb_chained_resource_functions.bal:45:5";
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
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-1");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Client:get");
            Assert.assertEquals(span.getTags().size(), 12);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.base_url"), "http://localhost:9091/test-service");
            Assert.assertEquals(span.getTags().get("http.url"), "/resource-2");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Client");
            Assert.assertEquals(span.getTags().get("action"), "get");
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/HttpClient:get");
            Assert.assertEquals(span.getTags().size(), 13);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina/http:1.0.0");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-2");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("peer.address"), "localhost:9091");
            Assert.assertEquals(span.getTags().get("service"), "testServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/HttpClient");
            Assert.assertEquals(span.getTags().get("action"), "get");
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span3.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "resourceTwo");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-2");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceTwo");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span5 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span5Position))
                .findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span4.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceTwo");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });

        Optional<BMockSpan> span6 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span6Position))
                .findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    @Test
    public void testHTTPContextPropagation() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9092/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "02_ootb_http_context_propagation.bal:23:5";
        final String span2Position = "02_ootb_http_context_propagation.bal:34:45";
        final String span3Position = "client_endpoint.bal:164:41";
        final String span4Position = "01_echo_backend.bal:23:5";
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
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-1");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceTwo");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span2 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Client:get");
            Assert.assertEquals(span.getTags().size(), 12);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.base_url"), "http://localhost:10011/echo-service");
            Assert.assertEquals(span.getTags().get("http.url"), "/echo/Hello Echo !");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceTwo");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Client");
            Assert.assertEquals(span.getTags().get("action"), "get");
        });

        Optional<BMockSpan> span3 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/HttpClient:get");
            Assert.assertEquals(span.getTags().size(), 13);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina/http:1.0.0");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("http.url"), "/echo-service/echo/Hello%20Echo%20!");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("peer.address"), "localhost:10011");
            Assert.assertEquals(span.getTags().get("service"), "testServiceTwo");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/HttpClient");
            Assert.assertEquals(span.getTags().get("action"), "get");
        });

        Optional<BMockSpan> span4 = echoBackendSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span3.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/backend:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/echo-service/echo/Hello%20Echo%20!");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "echoServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span5 = echoBackendSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span5Position))
                .findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span4.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/backend:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "echoServiceOne");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });

        Optional<BMockSpan> span6 = testServiceSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span6Position))
                .findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceTwo");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    @Test
    public void testWorkers() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9093/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "03_ootb_workers.bal:24:5";
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
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-1");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        List<BMockSpan> w1Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w1SpansPosition))
                .collect(Collectors.toList());
        Assert.assertEquals(w1Spans.size(), 3);
        w1Spans.forEach(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w1");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.worker"), "true");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
        });

        List<BMockSpan> w2Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w2SpansPosition))
                .collect(Collectors.toList());
        Assert.assertEquals(w2Spans.size(), 3);
        w2Spans.forEach(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w2");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.worker"), "true");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
        });

        Optional<BMockSpan> w3Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w3SpansPosition))
                .findFirst();
        Assert.assertTrue(w3Spans.isPresent());
        w3Spans.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w3");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.worker"), "true");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
        });

        Optional<BMockSpan> w4Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w4SpansPosition))
                .findFirst();
        Assert.assertTrue(w4Spans.isPresent());
        w4Spans.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w4");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.worker"), "true");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
        });

        Optional<BMockSpan> w5Spans = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), w5SpansPosition))
                .findFirst();
        Assert.assertTrue(w5Spans.isPresent());
        w5Spans.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "w5");
            Assert.assertEquals(span.getTags().size(), 6);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.worker"), "true");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceThree");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    private List<BMockSpan> getFinishedSpans(String serviceName) throws IOException {
        return getFinishedSpans(9090, serviceName);
    }

    private List<BMockSpan> getEchoBackendFinishedSpans() throws IOException {
        return getFinishedSpans(10010, "echoServiceOne");
    }
}
