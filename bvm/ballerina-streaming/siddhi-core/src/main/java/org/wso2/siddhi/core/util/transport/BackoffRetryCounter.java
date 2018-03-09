/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.transport;

/**
 * Backoff Retry Counter to count when to retry next during reconnection
 */
public class BackoffRetryCounter {
    private final String[] timeIntervalNames = new String[]{"5 sec", "10 sec", "15 sec", "30 sec", "1 min", "1 min",
            "2 min", "5 min"};
    private final long[] timeIntervals = new long[]{5000, 10000, 15000, 30000, 60000, 60000, 120000, 300000};

    private int intervalIndex = 0;

    public synchronized void reset() {
        intervalIndex = 0;
    }

    public synchronized void increment() {
        if (intervalIndex < timeIntervals.length - 2) {
            intervalIndex++;
        }
    }

    public long getTimeIntervalMillis() {
        return timeIntervals[intervalIndex];
    }


    public String getTimeInterval() {
        return timeIntervalNames[intervalIndex];
    }
}
