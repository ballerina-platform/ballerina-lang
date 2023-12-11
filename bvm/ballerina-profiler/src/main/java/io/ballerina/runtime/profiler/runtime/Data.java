/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.profiler.runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This class is used as a custom data type class for the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class Data {
    protected final String stackKey;
    protected final String stackIndex;
    protected String stackTrace = null;
    public long totalTime;
    private final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();

    public Data(String stackIndex, String stackKey) {
        this.stackIndex = stackIndex;
        this.stackKey = stackKey;
        this.totalTime = 0L;
    }

    public synchronized void start(String strandId) {
        if (this.startTimes.containsKey(strandId)) {
            return;
        }
        startTimes.put(strandId, TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
    }

    public synchronized void stop(String strandId) {
        long elapsed = TimeUnit.MILLISECONDS.convert(System.nanoTime(),
                TimeUnit.NANOSECONDS) - this.startTimes.remove(strandId);
        this.totalTime += elapsed;
    }

    private String getFormattedStats() {
        return "{" + "\"time\": \"" + this.totalTime + "\", " + "\"stackTrace\": " + this.stackTrace + "}";
    }

    @Override
    public String toString() {
        return this.getFormattedStats();
    }
}
