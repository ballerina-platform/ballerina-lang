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

package org.ballerinalang.test.observability.metrics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ballerina.testobserve.metrics.extension.model.Metrics;
import org.ballerina.testobserve.metrics.extension.model.MockGauge;
import org.ballerina.testobserve.metrics.extension.model.MockMetric;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.test.observability.ObservabilityBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Metrics related Test Cases.
 */
public class MetricsTestCase extends ObservabilityBaseTest {
    private static final String MODULE_NAME = "testservices";
    protected static final String MODULE_ID = "ballerina-test/" + MODULE_NAME + ":0.0.1";

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.setupServer("metrics", MODULE_NAME, new int[] {10090});
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        super.cleanupServer();
    }

    private Metrics getMetrics() throws IOException {
        String requestUrl = "http://localhost:10090/metricsRegistry/getMetrics";
        String data = HttpClientRequest.doPost(requestUrl, "", Collections.emptyMap()).getData();
        Type type = new TypeToken<Metrics>() { }.getType();
        return new Gson().fromJson(data, type);
    }

    private Metrics filterByTag(Metrics metrics, String key, String value) {
        Metrics filteredMetrics = new Metrics();
        filteredMetrics.addAllCounters(filterByTag(metrics.getCounters(), key, value));
        filteredMetrics.addAllGauges(filterByTag(metrics.getGauges(), key, value));
        filteredMetrics.addAllPolledGauges(filterByTag(metrics.getPolledGauges(), key, value));
        return filteredMetrics;
    }

    private <M extends MockMetric> List<M> filterByTag(List<M> metrics, String key, String value) {
        return metrics.stream()
                .filter(metric -> {
                    Optional<Tag> tag = metric.getId().getTags().stream()
                            .filter(t -> Objects.equals(t.getKey(), key))
                            .findFirst();
                    return (tag.isEmpty() && value == null)
                            || (tag.isPresent() && Objects.equals(tag.get().getValue(), value));
                })
                .collect(Collectors.toList());
    }

    private void assertMetrics(Metrics allMetrics, String invocationPosition, long invocationCount,
                               Tag ...additionalTags) {
        Metrics functionMetrics = filterByTag(allMetrics, "src.position", invocationPosition);
        Set<Tag> tags = new HashSet<>(Arrays.asList(additionalTags));
        tags.add(Tag.of("src.module", MODULE_ID));
        tags.add(Tag.of("src.position", invocationPosition));

        Assert.assertEquals(functionMetrics.getCounters().size(), 2);
        Assert.assertEquals(functionMetrics.getCounters().stream()
                        .map(gauge -> gauge.getId().getName())
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList("requests_total", "response_time_nanoseconds_total")));
        functionMetrics.getCounters().forEach(counter -> {
            Assert.assertEquals(counter.getId().getTags(), tags);
            if (Objects.equals(counter.getId().getName(), "requests_total")) {
                Assert.assertEquals(counter.getValue(), invocationCount);
            } else if (Objects.equals(counter.getId().getName(), "response_time_nanoseconds_total")) {
                Assert.assertTrue((invocationCount > 0) ? (counter.getValue() > 0) : (counter.getValue() == 0));
            }
        });

        Assert.assertEquals(functionMetrics.getGauges().size(), 1);
        Assert.assertEquals(functionMetrics.getGauges().stream()
                        .map(gauge -> gauge.getId().getName())
                        .collect(Collectors.toSet()),
                new HashSet<>(Collections.singletonList("response_time")));
        MockGauge gauge = functionMetrics.getGauges().get(0);
        Assert.assertEquals(gauge.getId().getName(), "response_time");
        Assert.assertEquals(gauge.getId().getTags(), tags);
        Assert.assertEquals(gauge.getCount(), invocationCount);

        Assert.assertEquals(functionMetrics.getPolledGauges().size(), 0);
    }

    @Test
    public void testMainFunction() throws Exception {
        String fileName = "01_main_function.bal";
        Metrics metrics = filterByTag(this.getMetrics(), "service", null);

        Assert.assertEquals(metrics.getCounters().size(), 6);
        Assert.assertEquals(metrics.getGauges().size(), 6);
        Assert.assertEquals(metrics.getPolledGauges().size(), 0);

        assertMetrics(metrics, fileName + ":19:1", 1,
                Tag.of("function", "main"),
                Tag.of("src.entry_point.main", "true")
        );
        assertMetrics(metrics, fileName + ":24:24", 1,
                Tag.of("object_name", "ballerina-test/testservices/ObservableAdder"),
                Tag.of("function", "getSum")
        );
        assertMetrics(metrics, fileName + ":38:12", 3,
                Tag.of("function", "calculateSumWithObservability")
        );
    }
}
