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
package io.ballerina.runtime.observability.metrics;

/**
 * A precomputed percentile of a distribution.
 */
public class PercentileValue {

    private final double percentile;
    private final double value;

    public PercentileValue(double percentile, double value) {
        this.percentile = percentile;
        this.value = value;
    }

    /**
     * Get the percentile in domain [0, 1]. For example, 0.5 represents the 50th percentile of the distribution.
     *
     * @return The percentile.
     */
    public double getPercentile() {
        return percentile;
    }

    /**
     * Get the value at specific percentile.
     *
     * @return The value at specific percentile.
     */
    public double getValue() {
        return value;
    }

}
