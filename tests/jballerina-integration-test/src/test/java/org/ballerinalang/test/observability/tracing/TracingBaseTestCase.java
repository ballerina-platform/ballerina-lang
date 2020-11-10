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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ballerina.testobserve.tracing.extension.BMockSpan;
import org.ballerinalang.test.observability.ObservabilityBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base Test Case which is inherited by all the Tracing Test Cases.
 */
@Test(groups = "tracing-test")
public class TracingBaseTestCase extends ObservabilityBaseTest {
    private static final String MODULE_NAME = "testservices";
    protected static final String MODULE_ID = "ballerina-test/" + MODULE_NAME + ":0.0.1";
    protected static final String COMMONS_FILE_NAME = "commons.bal";

    @BeforeGroups(value = "tracing-test", alwaysRun = true)
    public void setup() throws Exception {
        super.setupServer("tracing", MODULE_NAME, new int[] {9090, 9091, 9092, 9093, 9094, 9095});
    }

    @AfterGroups(value = "tracing-test", alwaysRun = true)
    public void cleanup() throws Exception {
        super.cleanupServer();
    }

    protected List<BMockSpan> getFinishedSpans(String serviceName, String resource) throws IOException {
        return getFinishedSpans(serviceName).stream()
                .filter(span -> Objects.equals(span.getTags().get("resource"), resource))
                .collect(Collectors.toList());
    }

    protected List<BMockSpan> getFinishedSpans(String service) throws IOException {
        String requestUrl = "http://localhost:9090/mockTracer/getMockTraces";
        String data = HttpClientRequest.doPost(requestUrl, service, Collections.emptyMap()).getData();
        Type type = new TypeToken<List<BMockSpan>>() { }.getType();
        return new Gson().fromJson(data, type);
    }

    @SafeVarargs
    protected final Map<String, String> toMap(Map.Entry<String, String>... mapEntries) {
        return Stream.of(mapEntries)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
