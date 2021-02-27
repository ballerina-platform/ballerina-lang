/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.utils.timeit;

import java.time.Duration;

/**
 * Entry object for operation times required by {@code TimeIt}.
 * Can perform operations on data.
 *
 * @since 2.0.0
 */
public class OperationTimeEntry {
    private Duration totalDuration;
    private int count;

    public OperationTimeEntry() {
        totalDuration = Duration.ofSeconds(0);
        count = 0;
    }

    /**
     * Adds a duration to the timed entries.
     *
     * @param duration Duration of the operation.
     */
    public void addDuration(Duration duration) {
        totalDuration = totalDuration.plus(duration);
        count++;
    }

    /**
     * Calculates the average of the durations.
     *
     * @return Average duration.
     */
    public Duration mean() {
        return totalDuration.dividedBy(count);
    }
}

