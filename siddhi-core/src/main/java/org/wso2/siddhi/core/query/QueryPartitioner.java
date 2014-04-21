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
package org.wso2.siddhi.core.query;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.partition.executor.RangePartitionExecutor;
import org.wso2.siddhi.core.partition.executor.VariablePartitionExecutor;
import org.wso2.siddhi.core.query.creator.QueryCreator;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.query.processor.PreSelectProcessingElement;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.QueryPartComposite;
import org.wso2.siddhi.core.util.parser.ExecutorParser;
import org.wso2.siddhi.query.api.condition.ConditionValidator;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionType;
import org.wso2.siddhi.query.api.definition.partition.RangePartitionType;
import org.wso2.siddhi.query.api.definition.partition.VariablePartitionType;
import org.wso2.siddhi.query.api.expression.ExpressionValidator;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class QueryPartitioner {
    private final QueryCreator queryCreator;
    private List<QueryCallback> queryCallbackList;
    private List<QuerySelector> querySelectorList;
    private ConcurrentHashMap<String, List<HandlerProcessor>> partitionMap = new ConcurrentHashMap<String, List<HandlerProcessor>>();
    private List<List<PartitionExecutor>> partitionExecutors = new ArrayList<List<PartitionExecutor>>();
    private OutputCallback outputCallback = null;


    public QueryPartitioner(String partitionId, QueryCreator queryCreator, List<QueryCallback> queryCallbackList, OutputCallback outputCallback, List<QuerySelector> querySelectorList,
                            ConcurrentMap<String, PartitionDefinition> partitionDefinitionMap, SiddhiContext siddhiContext) {
        this.queryCreator = queryCreator;
        this.queryCallbackList = queryCallbackList;
        this.outputCallback = outputCallback;
        this.querySelectorList = querySelectorList;

        if (partitionId != null) {
            PartitionDefinition partitionDefinition = partitionDefinitionMap.get(partitionId);
            if (partitionDefinition == null) {
                throw new QueryCreationException("Partition ID " + partitionId + " was not defined!");
            }
            List<QueryEventSource> tempQueryEventSourceList = new ArrayList<QueryEventSource>();
            Map<String, EventTable> eventTableMap = new HashMap<String, EventTable>();

            for (QueryEventSource queryEventSource : queryCreator.getQueryEventSourceList()) {
                ArrayList<PartitionExecutor> executorList = new ArrayList<PartitionExecutor>();
                partitionExecutors.add(executorList);

                tempQueryEventSourceList.clear();
                tempQueryEventSourceList.add(queryEventSource);

                for (PartitionType partitionType : partitionDefinition.getPartitionTypeList()) {
                    Set<String> depencySet;
                    if (partitionType instanceof VariablePartitionType) {
                        depencySet = ExpressionValidator.getDependencySet(((VariablePartitionType) partitionType).getVariable());
                        if (depencySet.isEmpty() || depencySet.contains(queryEventSource.getSourceId())) {
                            executorList.add(new VariablePartitionExecutor(ExecutorParser.parseExpression(((VariablePartitionType) partitionType).getVariable(), tempQueryEventSourceList, queryEventSource.getSourceId(), true, siddhiContext)));
                        }
                    } else {
                        depencySet = ConditionValidator.getDependencySet(((RangePartitionType) partitionType).getCondition());
                        if (depencySet.isEmpty() || depencySet.contains(queryEventSource.getSourceId())) {
                            executorList.add(new RangePartitionExecutor(ExecutorParser.parseCondition(((RangePartitionType) partitionType).getCondition(), tempQueryEventSourceList, queryEventSource.getSourceId(), eventTableMap, true, siddhiContext), ((RangePartitionType) partitionType).getPartitionKey()));
                        }
                    }
                }
            }
        }
    }


    public HandlerProcessor newPartition(int handlerId, String partitionKey) {

        List<HandlerProcessor> handlerProcessorList = partitionMap.get(partitionKey);
        if (handlerProcessorList == null) {
            handlerProcessorList = constructPartition();
            partitionMap.put(partitionKey, handlerProcessorList);
        }
        return handlerProcessorList.get(handlerId);

    }

    public List<HandlerProcessor> constructPartition() {

        QueryPartComposite queryPartComposite = queryCreator.constructQuery();
        querySelectorList.add(queryPartComposite.getQuerySelector());
        for (PreSelectProcessingElement preSelectProcessingElement : queryPartComposite.getPreSelectProcessingElementList()) {
            preSelectProcessingElement.setNext(queryPartComposite.getQuerySelector());
        }

        return queryPartComposite.getHandlerProcessorList();
    }

    public List<List<PartitionExecutor>> getPartitionExecutors() {
        return partitionExecutors;
    }
}
