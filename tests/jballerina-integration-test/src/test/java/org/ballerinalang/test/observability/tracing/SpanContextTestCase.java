/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ballerinalang.observe.mockextension.BMockSpan;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test cases span context.
 *
 * @since 2.0.0
 */
@Test(groups = "tracing-test")
public class SpanContextTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "07_span_context.bal";

    @Test
    public void test() throws Exception {
        String requestUrl = "http://localhost:19097/testServiceSeven/resourceOne/";
        String data = HttpClientRequest.doGet(requestUrl).getData();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> spanContext = new Gson().fromJson(data, type);

        List<BMockSpan> spanList = getFinishedSpans("testServiceSeven",
                DEFAULT_MODULE_ID, "/resourceOne");
        Optional<BMockSpan> span1 = spanList.stream()
                .filter(bMockSpan -> bMockSpan.getTags().get("src.position").equals(FILE_NAME + ":22:5"))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        Assert.assertEquals(span1.get().getTraceId(), spanContext.get("traceId"));
        Assert.assertEquals(span1.get().getSpanId(), spanContext.get("spanId"));
    }
}
