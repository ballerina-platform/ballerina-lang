/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.profiler.runtime;

import java.util.concurrent.TimeUnit;


public class Data {
    private String name;
    private long startTime;
    private long totalTime;
    private long minTime;
    private long maxTime;

    public Data(String name) {
        this.name = name;
        this.totalTime = 0L;
        this.startTime = 0L;
        this.minTime = Long.MAX_VALUE;
        this.maxTime = Long.MIN_VALUE;
    }

    public void start() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public void stop() {
        long elapsed = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;
        if (elapsed < this.minTime) {
            this.minTime = elapsed;
        }

        if (elapsed > this.maxTime) {
            this.maxTime = elapsed;
        }
        this.totalTime += elapsed;
    }

    private String getFormattedStats() {

        int time = (int) this.totalTime;
        String[] stackTrace = new String[]{this.name};

        // create the string representation of the output
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"time\": \"").append(time).append("\", ");
        sb.append("\"stackTrace\": ");
        for (int i = 0; i < stackTrace.length; i++) {
            sb.append(stackTrace[i]);
            if (i < stackTrace.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("},");
        return sb.toString();
    }

    public String toString() {
        return this.getFormattedStats();
    }
}
