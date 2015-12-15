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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.util.ExecutionPlanRuntimeBuilder;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PartitionParser {

    public static PartitionRuntime parse(ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder, Partition partition,
                                         ExecutionPlanContext executionPlanContext,
                                         ConcurrentMap<String, AbstractDefinition> streamDefinitionMap) {
        PartitionRuntime partitionRuntime = new PartitionRuntime(executionPlanRuntimeBuilder.getStreamDefinitionMap(), executionPlanRuntimeBuilder.getStreamJunctions(), partition, executionPlanContext);
        for (Query query : partition.getQueryList()) {
            List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
            ConcurrentMap<String, AbstractDefinition> combinedStreamMap = new ConcurrentHashMap<String, AbstractDefinition>();
            combinedStreamMap.putAll(streamDefinitionMap);
            combinedStreamMap.putAll(partitionRuntime.getLocalStreamDefinitionMap());
            QueryRuntime queryRuntime = QueryParser.parse(query, executionPlanContext, combinedStreamMap,
                    executionPlanRuntimeBuilder.getTableDefinitionMap(), executionPlanRuntimeBuilder.getEventTableMap());
            MetaStateEvent metaStateEvent = createMetaEventForPartitioner(queryRuntime.getMetaComplexEvent());
            partitionRuntime.addQuery(queryRuntime);
            partitionRuntime.addPartitionReceiver(queryRuntime, executors, metaStateEvent);
            QueryParserHelper.reduceMetaComplexEvent(metaStateEvent);
            if (queryRuntime.getMetaComplexEvent() instanceof MetaStateEvent) {
                QueryParserHelper.updateVariablePosition(metaStateEvent, executors);
            } else {
                QueryParserHelper.updateVariablePosition(metaStateEvent.getMetaStreamEvent(0), executors);
            }
            partitionRuntime.init();

        }
        return partitionRuntime;

    }

    /**
     * Create metaEvent to be used by StreamPartitioner with output attributes
     *
     * @param stateEvent metaStateEvent of the queryRuntime
     * @return metaStateEvent
     */
    private static MetaStateEvent createMetaEventForPartitioner(MetaComplexEvent stateEvent) {
        MetaStateEvent metaStateEvent;
        if (stateEvent instanceof MetaStateEvent) {
            metaStateEvent = new MetaStateEvent(((MetaStateEvent) stateEvent).getStreamEventCount());
            for (MetaStreamEvent metaStreamEvent : ((MetaStateEvent) stateEvent).getMetaStreamEvents()) {
                AbstractDefinition definition = metaStreamEvent.getLastInputDefinition();
                MetaStreamEvent newMetaStreamEvent = new MetaStreamEvent();
                for (Attribute attribute : definition.getAttributeList()) {
                    newMetaStreamEvent.addOutputData(attribute);
                }
                newMetaStreamEvent.addInputDefinition(definition);
                newMetaStreamEvent.setTableEvent(metaStreamEvent.isTableEvent());
                metaStateEvent.addEvent(newMetaStreamEvent);
            }
        } else {
            metaStateEvent = new MetaStateEvent(1);
            AbstractDefinition definition = ((MetaStreamEvent) stateEvent).getLastInputDefinition();
            MetaStreamEvent newMetaStreamEvent = new MetaStreamEvent();
            for (Attribute attribute : definition.getAttributeList()) {
                newMetaStreamEvent.addOutputData(attribute);
            }
            newMetaStreamEvent.addInputDefinition(definition);
            newMetaStreamEvent.setTableEvent(((MetaStreamEvent) stateEvent).isTableEvent());
            metaStateEvent.addEvent(newMetaStreamEvent);
        }

        return metaStateEvent;
    }
}
