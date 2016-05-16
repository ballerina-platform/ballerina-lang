/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.config;

import com.lmax.disruptor.ExceptionHandler;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.util.ElementIdGenerator;
import org.wso2.siddhi.core.util.ThreadBarrier;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.persistence.PersistenceService;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;
import org.wso2.siddhi.core.util.statistics.StatisticsManager;
import org.wso2.siddhi.core.util.timestamp.TimestampGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutionPlanContext {

    private SiddhiContext siddhiContext;
    private String name;
    private boolean playback;
    private boolean enforceOrder;
    private boolean async;
    private boolean statsEnabled = false;
    private StatisticsManager statisticsManager = null;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private List<EternalReferencedHolder> eternalReferencedHolders;
    private SnapshotService snapshotService;

    private ThreadBarrier threadBarrier = null;
    private TimestampGenerator timestampGenerator = null;
    private PersistenceService persistenceService;
    private ElementIdGenerator elementIdGenerator;
    private Map<String, EvalScript> scriptFunctionMap;
    private ExceptionHandler<Object> disruptorExceptionHandler;
    private int bufferSize;

    public ExecutionPlanContext() {
        this.eternalReferencedHolders = new CopyOnWriteArrayList<EternalReferencedHolder>();
        this.scriptFunctionMap = new HashMap<String, EvalScript>();
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

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
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

    public boolean isStatsEnabled() {
        return statsEnabled;
    }

    public void setStatsEnabled(boolean statsEnabled) {
        this.statsEnabled = statsEnabled;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public void setStatisticsManager(StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
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

    public List<EternalReferencedHolder> getEternalReferencedHolders() {
        return eternalReferencedHolders;
    }

    public ThreadBarrier getThreadBarrier() {
        return threadBarrier;
    }

    public void setThreadBarrier(ThreadBarrier threadBarrier) {
        this.threadBarrier = threadBarrier;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TimestampGenerator getTimestampGenerator() {
        return timestampGenerator;
    }

    public void setTimestampGenerator(TimestampGenerator timestampGenerator) {
        this.timestampGenerator = timestampGenerator;
    }

    public SnapshotService getSnapshotService() {
        return snapshotService;
    }

    public void setSnapshotService(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public void setElementIdGenerator(ElementIdGenerator elementIdGenerator) {
        this.elementIdGenerator = elementIdGenerator;
    }

    public ElementIdGenerator getElementIdGenerator() {
        return elementIdGenerator;
    }

    public EvalScript getEvalScript(String name) {
        return scriptFunctionMap.get(name);
    }

    public boolean isFunctionExist(String name) {
        return scriptFunctionMap.get(name) != null;
    }

    public Map<String, EvalScript> getScriptFunctionMap() {
        return scriptFunctionMap;
    }


    public void setDisruptorExceptionHandler(ExceptionHandler<Object> disruptorExceptionHandler) {
        this.disruptorExceptionHandler = disruptorExceptionHandler;
    }

    public ExceptionHandler<Object> getDisruptorExceptionHandler() {
        if (disruptorExceptionHandler != null) {
            return disruptorExceptionHandler;
        } else {
            return siddhiContext.getDefaultDisrupterExceptionHandler();
        }
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}