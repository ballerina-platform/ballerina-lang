/*
 *
 *  * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 *  * All Rights Reserved.
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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PartitionParser {

    public static PartitionRuntime parse(ExecutionPlanRuntime executionPlanRuntime, Partition partition, SiddhiContext siddhiContext,
                                         ConcurrentMap<String, AbstractDefinition> streamDefinitionMap) {
        PartitionRuntime partitionRuntime = new PartitionRuntime(executionPlanRuntime, partition, siddhiContext);
        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        for (Query query : partition.getQueryList()) {

            QueryRuntime queryRuntime;
            if (query.getInputStream() instanceof BasicSingleInputStream && ((BasicSingleInputStream) query.getInputStream()).isInnerStream()) {
                queryRuntime = QueryParser.parse(query, siddhiContext, partitionRuntime.getLocalStreamDefinitionMap());
            } else {
                queryRuntime = QueryParser.parse(query, siddhiContext, streamDefinitionMap);
            }
            MetaStreamEvent metaStreamEvent = createMetaEventForPartitioner(queryRuntime.getMetaStateEvent());
            partitionRuntime.addQuery(queryRuntime);
            partitionRuntime.addPartitionReceiver(queryRuntime, executors, metaStreamEvent);
        }
        return partitionRuntime;

    }

    /**
     * Create metaEvent to be used by StreamPartitioner with output attributes
     *
     * @param metaStateEvent metaStateEvent of the queryRuntime
     * @return metaStateEvent
     */
    private static MetaStreamEvent createMetaEventForPartitioner(MetaStateEvent metaStateEvent) {   //TOdo : handle joins
        AbstractDefinition definition = metaStateEvent.getMetaEvent(0).getInputDefinition();
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        for (Attribute attribute : definition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }
        metaStreamEvent.setInputDefinition(definition);
        return metaStreamEvent;
    }
}
