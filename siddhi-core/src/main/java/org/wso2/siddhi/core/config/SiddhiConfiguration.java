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

import java.util.List;
import java.util.UUID;

public class SiddhiConfiguration {

    /**
     * Core pool size of the Siddhi thread executor
     */
    private int threadExecutorCorePoolSize;

    /**
     * Max pool size of the Siddhi thread executor
     */
    private int threadExecutorMaxPoolSize;

    /**
     * Core pool size of the Siddhi thread scheduler
     */
    private int threadSchedulerCorePoolSize;


    /**
     * To enable Async Processing that uses intermediate queues
     */
    private boolean asyncProcessing;

    /**
     * Number of max events bundled when event rates are high
     */
    private int eventBatchSize;

    /**
     * Identifier to current Siddhi instance
     */
    private String instanceIdentifier;

    /**
     * To enable Hazelcast based distributed processing
     */
    private boolean distributedProcessing;

    /**
     * Query plan and Hazelcast Cluster Identifier
     */
    private String queryPlanIdentifier;

    private List<Class> siddhiExtensions;

    public SiddhiConfiguration() {

        threadExecutorCorePoolSize = Runtime.getRuntime().availableProcessors();

        threadExecutorMaxPoolSize = 50;

        threadSchedulerCorePoolSize = 20;

        asyncProcessing = false;

        distributedProcessing = false;

        eventBatchSize = 50;

        instanceIdentifier = UUID.randomUUID().toString();

        queryPlanIdentifier = UUID.randomUUID().toString();
    }

    public int getThreadExecutorCorePoolSize() {
        return threadExecutorCorePoolSize;
    }

    public SiddhiConfiguration setThreadExecutorCorePoolSize(int threadExecutorCorePoolSize) {
        this.threadExecutorCorePoolSize = threadExecutorCorePoolSize;
        return this;
    }

    public int getThreadExecutorMaxPoolSize() {
        return threadExecutorMaxPoolSize;
    }

    public SiddhiConfiguration setThreadExecutorMaxPoolSize(int threadExecutorMaxPoolSize) {
        this.threadExecutorMaxPoolSize = threadExecutorMaxPoolSize;
        return this;
    }

    public int getThreadSchedulerCorePoolSize() {
        return threadSchedulerCorePoolSize;
    }

    public SiddhiConfiguration setThreadSchedulerCorePoolSize(int threadSchedulerCorePoolSize) {
        this.threadSchedulerCorePoolSize = threadSchedulerCorePoolSize;
        return this;
    }

    public boolean isAsyncProcessing() {
        return asyncProcessing;
    }

    public SiddhiConfiguration setAsyncProcessing(boolean asyncProcessing) {
        this.asyncProcessing = asyncProcessing;
        return this;
    }

    public int getEventBatchSize() {
        return eventBatchSize;
    }

    public SiddhiConfiguration setEventBatchSize(int eventBatchSize) {
        this.eventBatchSize = eventBatchSize;
        return this;
    }

    public String getInstanceIdentifier() {
        return instanceIdentifier;
    }

    public SiddhiConfiguration setInstanceIdentifier(String instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
        return this;
    }

    public boolean isDistributedProcessing() {
        return distributedProcessing;
    }

    public SiddhiConfiguration setDistributedProcessing(boolean distributedProcessing) {
        this.distributedProcessing = distributedProcessing;
        return this;
    }

    public SiddhiConfiguration setQueryPlanIdentifier(String queryPlanIdentifier) {
        this.queryPlanIdentifier = queryPlanIdentifier;
        return this;
    }

    public String getQueryPlanIdentifier() {
        return queryPlanIdentifier;
    }

    public List<Class> getSiddhiExtensions() {
        return siddhiExtensions;
    }

    public void setSiddhiExtensions(List<Class> siddhiExtensions) {
        this.siddhiExtensions = siddhiExtensions;
    }
}
