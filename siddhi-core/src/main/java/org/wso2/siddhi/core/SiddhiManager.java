/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.parser.ExecutionPlanParser;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.concurrent.*;


public class SiddhiManager {

    private SiddhiContext siddhiContext;
    private ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap = new ConcurrentHashMap<String, ExecutionPlanRuntime>();

    public SiddhiManager() {
        siddhiContext = new SiddhiContext();
        siddhiContext.setEventBufferSize(SiddhiConstants.DEFAULT_EVENT_BUFFER_SIZE);
        siddhiContext.setExecutorService(new ThreadPoolExecutor(5, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>()));
    }

    /**
     * add stream definitions, partitions and queries of an execution plan
     *
     * @param executionPlan executionPlan which contains stream definitions,queries and partitions
     * @return executionPlanRuntime corresponding to the given executionPlan
     * @
     */
    public ExecutionPlanRuntime createExecutionPlanRuntime(ExecutionPlan executionPlan) {
        ExecutionPlanRuntime executionPlanRuntime = ExecutionPlanParser.parse(executionPlan, siddhiContext);
        executionPlanRuntimeMap.put(executionPlanRuntime.getName(), executionPlanRuntime);
        return executionPlanRuntime;
    }

    public ExecutionPlanRuntime createExecutionPlanRuntime(String executionPlan) {
        return createExecutionPlanRuntime(SiddhiCompiler.parse(executionPlan));
    }

    public ExecutionPlanRuntime getExecutionPlanRuntime(String executionPlanName) {
        return executionPlanRuntimeMap.get(executionPlanName);
    }

    //Todo remove the execution plan from SiddhiManager
    public SiddhiContext getSiddhiContext() {
        return siddhiContext;
    }

    public void validateExecutionPlan(ExecutionPlan executionPlan) {
        ExecutionPlanParser.parse(executionPlan, siddhiContext);
    }

}
