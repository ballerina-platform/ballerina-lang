package org.ballerinalang.test.observability.tracing;

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
 * Test case for testing the tracing instrumentation.
 * This also tests whether the instrumentation had messed up the normal execution flows.
 */
@Test(groups = "tracing-test")
public class ResourceFunctionTestCase extends BaseTestCase {
    private static final String FILE_NAME = "02_resource_function.bal";

    @DataProvider(name = "success-response-data-provider")
    public Object[][] getSuccessResponseData(){
        return new Object[][] {
                {"resourceOne", "15", FILE_NAME + ":21:5", FILE_NAME + ":27:20", "Sum of numbers: 120"},
                {"resourceTwo", "16", FILE_NAME + ":31:5", FILE_NAME + ":37:20", "Sum of numbers: 136"}
        };
    }

    @Test(dataProvider = "success-response-data-provider")
    public void testSimpleResourceCall(String resourceName, String requestPayload, String resourceFunctionPosition,
                                       String callerResponsePosition, String expectedResponsePayload) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/" + resourceName,
                requestPayload, Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), expectedResponsePayload);
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, callerResponsePosition)));
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
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME)
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
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", callerResponsePosition),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @DataProvider(name = "error-response-data-provider")
    public Object[][] getErrorResponseData(){
        return new Object[][] {
                {"resourceThree", FILE_NAME + ":41:5", "Test Error 1"},
                {"resourceFour", FILE_NAME + ":57:5", "Test Error 2"},
                {"resourceFive", FILE_NAME + ":74:5", "Test Error. Sum: 91"},
                {"resourceSix", FILE_NAME + ":80:5", "Test Error. Sum: 153"}
        };
    }

    @Test(dataProvider = "error-response-data-provider")
    public void testResourceSingleSpanErrorResponse(String resourceName, String resourceFunctionPosition,
                                                    String expectedResponsePayload) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/" + resourceName,
                "15", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), expectedResponsePayload);
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Collections.singletonList(resourceFunctionPosition)));
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
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });
    }

    @DataProvider(name = "simple-remote-call-data-provider")
    public Object[][] getSimpleRemoteCallData(){
        return new Object[][] {
                {"resourceSeven", FILE_NAME + ":87:5", FILE_NAME + ":89:30", FILE_NAME + ":96:20",
                        "Sum of numbers: 12"},
                {"resourceThirteen", FILE_NAME + ":134:5", FILE_NAME + ":135:24", FILE_NAME + ":137:24",
                        "Successfully executed"}
        };
    }

    @Test(dataProvider = "simple-remote-call-data-provider")
    public void testResourceWithRemoteCall(String resourceName, String resourceFunctionPosition,
                                           String remoteCallPosition, String callerRespondPosition,
                                           String expectedPayload) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), expectedPayload);
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", resourceName);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, remoteCallPosition, callerRespondPosition)));
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
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", SERVER_CONNECTOR_NAME)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), remoteCallPosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", remoteCallPosition),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithReturn")
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
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", callerRespondPosition),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testResourceWithNestedRemoteCalls() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceEight",
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        final String span1Position = FILE_NAME + ":100:5";
        final String span2Position = FILE_NAME + ":101:9";
        final String span3Position = "commons.bal:36:9";
        final String span4Position = FILE_NAME + ":102:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", "resourceEight");
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
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/resourceEight"),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEight"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEight"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEight"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEight"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @DataProvider(name = "remote-error-return-data-provider")
    public Object[][] getRemoteErrorReturnData(){
        return new Object[][] {
                {"resourceNine", FILE_NAME + ":106:5", FILE_NAME + ":107:15"},
                {"resourceTen", FILE_NAME + ":112:5", FILE_NAME + ":113:20"}
        };
    }

    @Test(dataProvider = "remote-error-return-data-provider")
    public void testRemoteFunctionErrorReturn(String resourceName, String resourceFunctionPosition,
                                              String remoteCallPosition) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "Test Error");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", resourceName);
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
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", resourceName),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithErrorReturn"),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });
    }

    @Test
    public void testResourceWithIgnoredErrorReturnInRemoteCall() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceEleven",
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        final String span1Position = FILE_NAME + ":118:5";
        final String span2Position = FILE_NAME + ":119:19";
        final String span3Position = FILE_NAME + ":120:20";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", "resourceEleven");
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
            Assert.assertEquals(span.getOperationName(), "resourceEleven");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/resourceEleven"),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEleven"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEleven"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceEleven"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }

    @Test
    public void testResourceWithTrappedPanic() throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost("http://localhost:9091/testServiceOne/resourceTwelve",
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Successfully trapped panic: Test Error");
        Thread.sleep(1000);

        final String span1Position = FILE_NAME + ":124:5";
        final String span2Position = FILE_NAME + ":125:24";
        final String span3Position = FILE_NAME + ":127:24";

        List<BMockSpan> spans = this.getFinishedSpans("testServiceOne", "resourceTwelve");
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
            Assert.assertEquals(span.getOperationName(), "resourceTwelve");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span1Position),
                    new AbstractMap.SimpleEntry<>("src.entry_point.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/testServiceOne/resourceTwelve"),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwelve"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwelve"),
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
                    new AbstractMap.SimpleEntry<>("service", "testServiceOne"),
                    new AbstractMap.SimpleEntry<>("resource", "resourceTwelve"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("action", "respond")
            ));
        });
    }
}
