/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observe.metrics.extension.defaultimpl;

import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.MetricRegistry;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Tags with {@link Counter}.
 */
public class CounterTagsTest {

    private MetricRegistry metricRegistry;

    @BeforeClass
    public void init() {
        DefaultMetricProvider metricProvider = new DefaultMetricProvider();
        metricRegistry = new MetricRegistry(metricProvider);
    }

    @Test
    public void testCounter() {
        Counter counter = Counter.builder("test_counter").description("Test Counter").register(metricRegistry);
        counter.increment(100L);
        Assert.assertEquals(counter.getValue(), 100L);
    }

    @Test
    public void testCounterTags() {
        Counter counter = Counter.builder("test_tags_counter").description("Test Counter Tags")
                .tags("key", "value").register(metricRegistry);
        counter.increment(100L);
        Assert.assertEquals(counter.getValue(), 100L);
    }

    @Test
    public void testCounterMultipleTags() {
        Counter counter = Counter.builder("test_multiple_tags_counter").description("Test Counter Multiple Tags")
                .tags("key1", "value1", "key2", "value2").register(metricRegistry);
        counter.increment(100L);
        counter = Counter.builder("test_multiple_tags_counter").description("Test Counter Multiple Tags")
                .tags("key2", "value2", "key1", "value1").register(metricRegistry);
        counter.increment(100L);
        Assert.assertEquals(counter.getValue(), 200L);
    }

    // TODO: This test should pass to comply with Prometheus rules
    @Test(enabled = false)
    public void testCounterDifferentMultipleTags() {
        Counter counter = Counter.builder("test_different_multiple_tags_counter")
                .tags("key1", "value1", "key2", "value2").register(metricRegistry);
        counter.increment(100L);
        Assert.assertEquals(counter.getValue(), 100L);
        try {
            Counter.builder("test_different_multiple_tags_counter")
                    .tags("key2", "value2", "key3", "value3").register(metricRegistry);
            Assert.fail("Different tags are not allowed for metrics with same name");
        } catch (IllegalArgumentException e) {
            // Test successful
        }
    }

    @Test
    public void testCounterTagsFailures() {
        try {
            Counter.builder("test_tag_fail_counter").description("Test Counter Tags Failures")
                    .tags("key").register(metricRegistry);
            Assert.fail("Different number of key values are not allowed");
        } catch (IllegalArgumentException e) {
            // Test successful
        }
    }


}
