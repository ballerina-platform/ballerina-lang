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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.util.EventTimeBasedScheduler;
import org.wso2.siddhi.core.util.Schedulable;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.SystemTimeBasedScheduler;

import java.util.concurrent.ScheduledExecutorService;

/**
 * This parser generates the scheduler based on the playback configuration. If playback is enabled, an
 * {@link EventTimeBasedScheduler} object will be returned. If the playback is disabled (default behaviour),
 * {@link SystemTimeBasedScheduler} object will be returned.
 */
public class SchedulerParser {

    private SchedulerParser() {
    }

    /**
     * Create Scheduler object based on the siddhi app playback configuration.
     *
     * @param scheduledExecutorService ScheduledExecutorService
     * @param singleThreadEntryValve   Schedulable
     * @param siddhiAppContext     SiddhiAppContext
     * @return Scheduler instance
     */
    public static Scheduler parse(ScheduledExecutorService scheduledExecutorService, Schedulable
            singleThreadEntryValve, SiddhiAppContext siddhiAppContext) {
        Scheduler scheduler;
        if (siddhiAppContext.isPlayback()) {
            // Playback mode is enabled
            scheduler = new EventTimeBasedScheduler(singleThreadEntryValve, siddhiAppContext);
        } else {
            // Default execution
            scheduler = new SystemTimeBasedScheduler(siddhiAppContext.getScheduledExecutorService(),
                    singleThreadEntryValve, siddhiAppContext);
        }

        return scheduler;
    }
}
