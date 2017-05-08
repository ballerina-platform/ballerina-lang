/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

/**
 * Class to calculate Siddhi latency.
 */
public class SiddhiLatencyMetric implements LatencyTracker {
    // Using thread local variables to keep the timer track the time of the same execution path by different threads.
    private ThreadLocal<Timer> execLatencyTimer;
    private ThreadLocal<Timer.Context> context;
    private String metricName;

    public SiddhiLatencyMetric(String name, final MetricRegistry metricRegistry) {
        this.metricName = name + ".latency";
        execLatencyTimer = new ThreadLocal<Timer>() {
            protected Timer initialValue() {
                return metricRegistry.timer(metricName);
            }
        };
        context = new ThreadLocal<Timer.Context>() {
            protected Timer.Context initialValue() {
                return null;
            }
        };
    }

    /**
     * This is called when the processing of the event is started. This is
     * called at ProcessStreamReceiver#receive before the event is passed into
     * process chain
     */
    public void markIn() {
        if (context.get() != null) {
            throw new IllegalStateException("MarkIn consecutively called without calling markOut in " + metricName);
        }
        context.set(execLatencyTimer.get().time());
    }

    /**
     * This is called to when the processing of an event is finished. This is called at
     * two places,
     * 1. OutputRateLimiter#sendToCallBacks - When the event is processed and by the full chain and emitted out
     * 2. ProcessStreamReceiver#receive - When event is not processed by full process chain(e.g. Filtered out by a
     * filter)
     */
    @Override
    public void markOut() {
        if (context.get() != null) {
            context.get().stop();
            context.set(null);
        }
    }

    /**
     * @return Name of the latency tracker
     */
    @Override
    public String getName() {
        return metricName;
    }
}
