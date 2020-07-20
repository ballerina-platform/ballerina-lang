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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test cases for error cases.
 */
@Test(groups = "tracing-test")
public class ErrorsTracingTestCase extends TracingBaseTest {

    @Test
    public void testReturnError() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-1");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource one");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:24:5";
        final String span2Position = "05_error_conditions.bal:29:19";
        final String span3Position = "05_error_conditions.bal:33:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceOne");
        Assert.assertEquals(spans.size(), 3);
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
            Assert.assertEquals(span.getOperationName(), "resourceOne");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-1");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
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
            Assert.assertEquals(span.getOperationName(), "returnError");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("function"), "returnError");
            Assert.assertEquals(span.getTags().get("error"), "true");
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
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceOne");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    @Test
    public void testReturnErrorWithOptionalErrorReturn() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-2");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource two");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:36:5";
        final String span2Position = "05_error_conditions.bal:41:19";
        final String span3Position = "05_error_conditions.bal:45:24";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceTwo");
        Assert.assertEquals(spans.size(), 3);
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
            Assert.assertEquals(span.getOperationName(), "resourceTwo");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-2");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceTwo");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "returnErrorWithOptionalErrorReturn");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceTwo");
            Assert.assertEquals(span.getTags().get("function"), "returnErrorWithOptionalErrorReturn");
            Assert.assertEquals(span.getTags().get("error"), "true");
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
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceTwo");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    @Test
    public void testReturnErrorWithAlternateErrorReturn() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-3");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource three");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:52:5";
        final String span2Position = "05_error_conditions.bal:57:19";
        final String span3Position = "05_error_conditions.bal:161:15";
        final String span4Position = "05_error_conditions.bal:63:24";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceThree");
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
            Assert.assertEquals(span.getOperationName(), "resourceThree");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-3");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceThree");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceThree");
            Assert.assertEquals(span.getTags().get("function"), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceThree");
            Assert.assertEquals(span.getTags().get("function"), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceThree");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    @Test
    public void testPanicError() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-4");
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "test panic error: error 4");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:70:5";
        final String span2Position = "05_error_conditions.bal:75:9";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceFour");
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
            Assert.assertEquals(span.getOperationName(), "resourceFour");
            Assert.assertEquals(span.getTags().size(), 11);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-4");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceFour");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "panicError");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceFour");
            Assert.assertEquals(span.getTags().get("function"), "panicError");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });
    }

    @Test
    public void testDirectPanicError() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-5");
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "test panic error: error 5");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:82:5";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceFive");
        Assert.assertEquals(spans.size(), 1);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Collections.singletonList(span1Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span1Position))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), "resourceFive");
            Assert.assertEquals(span.getTags().size(), 11);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-5");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceFive");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });
    }

    @Test
    public void testTrapPanic() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-6");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource six");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:91:5";
        final String span2Position = "05_error_conditions.bal:96:24";
        final String span3Position = "05_error_conditions.bal:100:24";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceSix");
        Assert.assertEquals(spans.size(), 3);
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
            Assert.assertEquals(span.getOperationName(), "resourceSix");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-6");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceSix");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "panicError");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceSix");
            Assert.assertEquals(span.getTags().get("function"), "panicError");
            Assert.assertEquals(span.getTags().get("error"), "true");
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
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceSix");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }

    @Test
    public void testCheckPanic() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-7");
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "test return error: error 7");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:107:5";
        final String span2Position = "05_error_conditions.bal:112:30";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceSeven");
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
            Assert.assertEquals(span.getOperationName(), "resourceSeven");
            Assert.assertEquals(span.getTags().size(), 11);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-7");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceSeven");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceSeven");
            Assert.assertEquals(span.getTags().get("function"), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });
    }

    @Test
    public void testCheckError() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doGet("http://localhost:9095/test-service/resource-8");
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello, World! from resource eight");
        Thread.sleep(1000);

        final String span1Position = "05_error_conditions.bal:119:5";
        final String span2Position = "05_error_conditions.bal:124:19";
        final String span3Position = "05_error_conditions.bal:176:18";
        final String span4Position = "05_error_conditions.bal:128:24";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceFive", "resourceEight");
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
            Assert.assertEquals(span.getOperationName(), "resourceEight");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "server");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.entry_point.resource"), "true");
            Assert.assertEquals(span.getTags().get("http.url"), "/test-service/resource-8");
            Assert.assertEquals(span.getTags().get("http.method"), "GET");
            Assert.assertEquals(span.getTags().get("protocol"), "http");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceEight");
            Assert.assertEquals(span.getTags().get("connector_name"), "http");
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "testCheckError");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceEight");
            Assert.assertEquals(span.getTags().get("function"), "testCheckError");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().size(), 7);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceEight");
            Assert.assertEquals(span.getTags().get("function"), "returnErrorWithAlternateErrorReturn");
            Assert.assertEquals(span.getTags().get("error"), "true");
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/http/Caller:respond");
            Assert.assertEquals(span.getTags().size(), 10);
            Assert.assertEquals(span.getTags().get("span.kind"), "client");
            Assert.assertEquals(span.getTags().get("src.module"), "ballerina-test/tracingservices:0.0.1");
            Assert.assertEquals(span.getTags().get("src.remote"), "true");
            Assert.assertEquals(span.getTags().get("http.status_code"), "200");
            Assert.assertEquals(span.getTags().get("http.status_code_group"), "2xx");
            Assert.assertEquals(span.getTags().get("service"), "testServiceFive");
            Assert.assertEquals(span.getTags().get("resource"), "resourceEight");
            Assert.assertEquals(span.getTags().get("connector_name"), "ballerina/http/Caller");
            Assert.assertEquals(span.getTags().get("action"), "respond");
        });
    }
}
