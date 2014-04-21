/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.siddhi.core.tracer;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.StreamEvent;

public class EventMonitorService {
    private SiddhiContext siddhiContext;
    private volatile boolean enableTrace;
    private volatile boolean enableStats;
    private EventMonitor eventMonitor = new PassThroughEventMonitor();

    public EventMonitorService(SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
    }

    public void setEventMonitor(EventMonitor eventMonitor) {
        this.eventMonitor = eventMonitor;
    }

    public void setEnableTrace(boolean enableTrace) {
        this.enableTrace = enableTrace;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }

    public boolean isEnableTrace() {
        return enableTrace;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void trace(ComplexEvent complexEvent, String message) {
        if (enableTrace) {
            eventMonitor.trace(complexEvent, message);
        }
    }

    public void calculateStats(StreamEvent streamEvent) {
        if (enableStats) {
            eventMonitor.calculateStats(streamEvent);
        }
    }


}
