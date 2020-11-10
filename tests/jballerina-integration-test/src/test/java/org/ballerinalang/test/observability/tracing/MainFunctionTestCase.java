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
 * Test cases for main function.
 */
@Test(groups = "tracing-test")
public class MainFunctionTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "01_main_function.bal";

    @Test
    public void testSimpleResourceCall() throws Exception {
        final String span1Position = FILE_NAME + ":17:1";
        final String span2Position = FILE_NAME + ":18:5";
        final String span3Position = COMMONS_FILE_NAME + ":37:9";
        final String span4Position = FILE_NAME + ":22:15";
        final String span5Position = FILE_NAME + ":30:21";
        final String span6Position = FILE_NAME + ":36:16";

        List<BMockSpan> spans = this.getFinishedSpans("Unknown Service");
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(span1Position, span2Position, span3Position, span4Position,
                        span5Position, span6Position)));
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
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
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
            Assert.assertEquals(span.getOperationName(),
                    "ballerina-test/testservices/MockClient:callAnotherRemoteFunction");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
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
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:calculateSum");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "calculateSum")
            ));
        });

        Optional<BMockSpan> span5 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span5Position))
                .findFirst();
        Assert.assertTrue(span5.isPresent());
        span5.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithPanic");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span5Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithPanic"),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });

        Optional<BMockSpan> span6 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span6Position))
                .findFirst();
        Assert.assertTrue(span6.isPresent());
        span6.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina-test/testservices/MockClient:callWithErrorReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span6Position),
                    new AbstractMap.SimpleEntry<>("src.remote", "true"),
                    new AbstractMap.SimpleEntry<>("connector_name", "ballerina-test/testservices/MockClient"),
                    new AbstractMap.SimpleEntry<>("action", "callWithErrorReturn"),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });
    }
}
