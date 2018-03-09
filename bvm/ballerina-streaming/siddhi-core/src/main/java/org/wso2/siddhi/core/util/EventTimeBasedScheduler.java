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

package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.util.timestamp.EventTimeBasedMillisTimestampGenerator;

/**
 * Scheduler which operate based on event's timestamp
 */
public class EventTimeBasedScheduler extends Scheduler {

    public EventTimeBasedScheduler(Schedulable singleThreadEntryValve, SiddhiAppContext siddhiAppContext) {
        super(singleThreadEntryValve, siddhiAppContext);

        if (siddhiAppContext.isPlayback()) {
            ((EventTimeBasedMillisTimestampGenerator) siddhiAppContext.getTimestampGenerator())
                    .addTimeChangeListener(new EventTimeBasedMillisTimestampGenerator.TimeChangeListener() {
                        @Override
                        public void onTimeChange(long currentTimestamp) {
                            Long lastTime = toNotifyQueue.peek();
                            if (lastTime != null && lastTime <= currentTimestamp) {
                                // If executed in a separate thread, while it is processing,
                                // the new event will come into the window. As the result of it,
                                // the window will emit the new event as an existing current event.
                                sendTimerEvents();
                            }
                        }
                    });
        }
    }

    @Override
    public void schedule(long time) {
        // Do nothing
    }

    @Override
    public Scheduler clone(String key, EntryValveProcessor entryValveProcessor) {
        Scheduler scheduler = new EventTimeBasedScheduler(entryValveProcessor, siddhiAppContext);
        scheduler.elementId = elementId + "-" + key;
        return scheduler;
    }
}
