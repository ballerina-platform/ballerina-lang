/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.config;

import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.table.EventTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;

public class ExecutionPlanContext {

    private SiddhiContext siddhiContext;
    private String name;
    private boolean playback;
    private boolean enforceOrder;
    private boolean parallel;

    private ThreadPoolExecutor executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private List<EternalReferencedHolder> eternalReferencedHolders;

    private Lock sharedLock =null;
    private Map<String, EventTable> eventTableMap;

    public ExecutionPlanContext() {
        this.eternalReferencedHolders = new ArrayList<EternalReferencedHolder>();
    }

    public SiddhiContext getSiddhiContext() {
        return siddhiContext;
    }

    public void setSiddhiContext(SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public boolean isPlayback() {
        return playback;
    }

    public void setPlayback(boolean playback) {
        this.playback = playback;
    }

    public boolean isEnforceOrder() {
        return enforceOrder;
    }

    public void setEnforceOrder(boolean enforceOrder) {
        this.enforceOrder = enforceOrder;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public void addEternalReferencedHolder(EternalReferencedHolder eternalReferencedHolder) {
        eternalReferencedHolders.add(eternalReferencedHolder);
    }

    public Lock getSharedLock() {
        return sharedLock;
    }

    public void setSharedLock(Lock sharedLock) {
        this.sharedLock = sharedLock;
    }

    public void setExecutorService(ThreadPoolExecutor executorService) {
        this.executorService = executorService;
    }

    public ThreadPoolExecutor getExecutorService() {
        return executorService;
    }

    public void setEventTableMap(Map<String, EventTable> eventTableMap) {
        this.eventTableMap = eventTableMap;
    }

    public Map<String, EventTable> getEventTableMap() {
        return eventTableMap;
    }
}
