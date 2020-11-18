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
package io.ballerina.runtime.observability.metrics.noop;

import io.ballerina.runtime.observability.metrics.AbstractMetric;
import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.MetricId;

/**
 * Implementation of No-Op {@link Counter}.
 */
public class NoOpCounter extends AbstractMetric implements Counter {

    public NoOpCounter(MetricId metricId) {
        super(metricId);
    }

    @Override
    public void reset() {
        //no nothing.
    }

    @Override
    public void increment(long amount) {
        // Do nothing
    }

    @Override
    public long getValue() {
        return 0;
    }

    @Override
    public long getValueThenReset() {
        return 0;
    }
}
