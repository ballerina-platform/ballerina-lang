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
package org.ballerinalang.util.metrics.noop;

import org.ballerinalang.util.metrics.AbstractMetric;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.Summary;

/**
 * Implementation of No-Op {@link Summary}.
 */
public class NoOpSummary extends AbstractMetric implements Summary {

    public NoOpSummary(MetricId id) {
        super(id);
    }

    @Override
    public void record(double amount) {
        // Do nothing
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public double mean() {
        return 0;
    }

    @Override
    public double max() {
        return 0;
    }

    @Override
    public double percentile(double percentile) {
        return 0;
    }
}
