/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerina.testobserve.metrics.extension;

import io.ballerina.runtime.observability.metrics.spi.MetricReporter;

import java.io.PrintStream;

/**
 * Mock metrics reporter.
 * This does not do anything and is only created as a reporter type is required by Ballerina Observability configs.
 */
public class BMockMetricsReporter implements MetricReporter {

    private static final PrintStream out = System.out;
    private static final String NAME = "BMockMetricsReporter";

    @Override
    public void init() {
        out.println("Initialized Mock Metrics Reporter");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
