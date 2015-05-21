/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.util.ExecutionPlanRuntimeBuilder;
import org.wso2.siddhi.core.util.parser.ExecutionPlanParser;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class SiddhiManager {

    private static final Logger log = Logger.getLogger(SiddhiManager.class);
    private SiddhiContext siddhiContext;
    private ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap = new ConcurrentHashMap<String, ExecutionPlanRuntime>();

    public SiddhiManager() {
        siddhiContext = new SiddhiContext();
    }

    /**
     * add stream definitions, partitions and queries of an execution plan
     *
     * @param executionPlan executionPlan which contains stream definitions,queries and partitions
     * @return executionPlanRuntime corresponding to the given executionPlan
     * @
     */
    public ExecutionPlanRuntime createExecutionPlanRuntime(ExecutionPlan executionPlan) {
        ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder = ExecutionPlanParser.parse(executionPlan, siddhiContext);
        executionPlanRuntimeBuilder.setExecutionPlanRuntimeMap(executionPlanRuntimeMap);
        ExecutionPlanRuntime executionPlanRuntime = executionPlanRuntimeBuilder.build();
        executionPlanRuntimeMap.put(executionPlanRuntime.getName(), executionPlanRuntime);
        return executionPlanRuntime;
    }

    public ExecutionPlanRuntime createExecutionPlanRuntime(String executionPlan) {
        return createExecutionPlanRuntime(SiddhiCompiler.parse(executionPlan));
    }

    public ExecutionPlanRuntime getExecutionPlanRuntime(String executionPlanName) {
        return executionPlanRuntimeMap.get(executionPlanName);
    }

    public void validateExecutionPlan(ExecutionPlan executionPlan) {
        ExecutionPlanRuntime executionPlanRuntime = ExecutionPlanParser.parse(executionPlan, siddhiContext).build();
        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    public void validateExecutionPlan(String executionPlan) {
        validateExecutionPlan(SiddhiCompiler.parse(executionPlan));
    }

    public void setPersistenceStore(PersistenceStore persistenceStore) {
        this.siddhiContext.setPersistenceStore(persistenceStore);
    }

    public void setExtension(String name, Class clazz) {
        siddhiContext.getSiddhiExtensions().put(name, clazz);
    }

    public void setDataSource(String dataSourceName, DataSource dataSource) {
        siddhiContext.addSiddhiDataSource(dataSourceName, dataSource);
    }

    public void shutdown() {
        List<String> executionPlanNames = new ArrayList<String>(executionPlanRuntimeMap.keySet());
        for (String executionPlanName : executionPlanNames) {
            executionPlanRuntimeMap.get(executionPlanName).shutdown();
        }
    }

    public void persist(){
        for(ExecutionPlanRuntime executionPlanRuntime:executionPlanRuntimeMap.values()){
            executionPlanRuntime.persist();
        }
    }
}
