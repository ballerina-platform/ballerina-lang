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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        List<BMockSpan> mockSpans = getCollectedSpans("testServiceOne");

        Assert.assertEquals(mockSpans.size(), 6);
        Assert.assertEquals(mockSpans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_ootb_chained_resource_functions.bal:23:5"))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(mockSpans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
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

        Optional<BMockSpan> span2 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_ootb_chained_resource_functions.bal:33:45"))
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

        Optional<BMockSpan> span3 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "client_endpoint.bal:164:41"))
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

        Optional<BMockSpan> span4 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_ootb_chained_resource_functions.bal:45:5"))
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

        Optional<BMockSpan> span5 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_ootb_chained_resource_functions.bal:52:20"))
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

        Optional<BMockSpan> span6 = mockSpans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"),
                        "01_ootb_chained_resource_functions.bal:42:20"))
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
}
