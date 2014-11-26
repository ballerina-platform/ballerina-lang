/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.config;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.persistence.PersistenceService;
import org.wso2.siddhi.core.snapshot.SnapshotService;
import org.wso2.siddhi.core.snapshot.ThreadBarrier;
import org.wso2.siddhi.core.tracer.EventMonitorService;
import org.wso2.siddhi.core.util.generator.ElementIdGenerator;
import org.wso2.siddhi.core.util.generator.GlobalIndexGenerator;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class SiddhiContext {

    private boolean asyncProcessing;
    private int eventBatchSize;
    private SnapshotService snapshotService;
    private PersistenceService persistenceService;
    private ThreadBarrier threadBarrier;
    private ThreadPoolExecutor threadPoolExecutor;
    private ScheduledExecutorService scheduledExecutorService;
    private ProcessingState distributedProcessingState;
    private ElementIdGenerator elementIdGenerator;
    private GlobalIndexGenerator globalIndexGenerator;
    private HazelcastInstance hazelcastInstance;
    private String queryPlanIdentifier;
    private List<Class> siddhiExtensions;
    private List<EternalReferencedHolder> eternalReferencedHolders;
    private ConcurrentHashMap<String, DataSource> siddhiDataSources;
    private EventMonitorService eventMonitorService;

    public enum ProcessingState {ENABLE_INTERNAL,ENABLE_EXTERNAL,DISABLED}

    public SiddhiContext(String queryPlanIdentifier, ProcessingState distributedProcessingState) {
        this.queryPlanIdentifier = queryPlanIdentifier;
        this.distributedProcessingState = distributedProcessingState;
        this.elementIdGenerator = new ElementIdGenerator(queryPlanIdentifier);
        this.siddhiDataSources = new ConcurrentHashMap<String, DataSource>();
        this.eternalReferencedHolders = new ArrayList<EternalReferencedHolder>();
    }

    public boolean isAsyncProcessing() {
        return asyncProcessing;
    }

    public void setAsyncProcessing(boolean asyncProcessing) {
        this.asyncProcessing = asyncProcessing;
    }

    public int getEventBatchSize() {
        return eventBatchSize;
    }

    public void setEventBatchSize(int eventBatchSize) {
        this.eventBatchSize = eventBatchSize;
    }

    public void setSnapshotService(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    public SnapshotService getSnapshotService() {
        return snapshotService;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setThreadBarrier(ThreadBarrier threadBarrier) {
        this.threadBarrier = threadBarrier;
    }

    public ThreadBarrier getThreadBarrier() {
        return threadBarrier;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public boolean isDistributedProcessingEnabled() {
        return distributedProcessingState != ProcessingState.DISABLED;
    }

    public ProcessingState getDistributedProcessingState() {
        return distributedProcessingState;
    }

    public ElementIdGenerator getElementIdGenerator() {
        return elementIdGenerator;
    }

    public GlobalIndexGenerator getGlobalIndexGenerator() {
        return globalIndexGenerator;
    }


    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void setGlobalIndexGenerator(GlobalIndexGenerator globalIndexGenerator) {
        this.globalIndexGenerator = globalIndexGenerator;
    }

    public String getQueryPlanIdentifier() {
        return queryPlanIdentifier;
    }

    public void setSiddhiExtensions(List<Class> siddhiExtensions) {
        this.siddhiExtensions = siddhiExtensions;
    }

    public List<Class> getSiddhiExtensions() {
        return siddhiExtensions;
    }

    public DataSource getDataSource(String name) {
        return siddhiDataSources.get(name);
    }

    public void addDataSource(String name, DataSource dataSource) {
        siddhiDataSources.put(name, dataSource);
    }

    public EventMonitorService getEventMonitorService() {
        return eventMonitorService;
    }

    public void setEventMonitorService(EventMonitorService eventMonitorService) {
        this.eventMonitorService = eventMonitorService;
    }

    public void addEternalReferencedHolder(EternalReferencedHolder eternalReferencedHolder) {
        eternalReferencedHolders.add(eternalReferencedHolder);
    }

    public List<EternalReferencedHolder> getEternalReferencedHolders() {
        return eternalReferencedHolders;
    }

}
