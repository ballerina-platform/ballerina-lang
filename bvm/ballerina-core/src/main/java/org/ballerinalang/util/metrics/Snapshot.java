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
package org.ballerinalang.util.metrics;

import java.util.Arrays;

/**
 * Snapshot of all distribution statistics at a point in time.
 */
public final class Snapshot {
    private static final PercentileValue[] EMPTY_PERCENTILE_VALUES = new PercentileValue[0];

    public final double mean;
    public final double max;
    public final PercentileValue[] percentileValues;

    public Snapshot(double mean, double max, PercentileValue[] percentileValues) {
        this.mean = mean;
        this.max = max;
        this.percentileValues = percentileValues;
    }

    public double getMean() {
        return mean;
    }

    public double getMax() {
        return max;
    }

    public PercentileValue[] getPercentileValues() {
        return percentileValues != null ? Arrays.copyOf(percentileValues, percentileValues.length) :
                EMPTY_PERCENTILE_VALUES;
    }

}
