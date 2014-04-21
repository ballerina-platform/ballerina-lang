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
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.query.creator.QueryCreator;
import org.wso2.siddhi.core.query.creator.QueryCreatorFactiory;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateManager;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.PartitionHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.TableHandlerProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.parser.QueryOutputParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.input.WindowStream;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class QueryManager {
    private String queryId;
    private Query query;
    private final StreamDefinition outputStreamDefinition;
    private List<HandlerProcessor> handlerProcessors = new ArrayList<HandlerProcessor>();
    private ArrayList<QuerySelector> querySelectorList = new ArrayList<QuerySelector>();
    private List<QueryCallback> queryCallbackList = new ArrayList<QueryCallback>();
    private OutputCallback outputCallback = null;
    private final OutputRateManager outputRateManager;


    public QueryManager(Query query, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
                        ConcurrentMap<String, StreamJunction> streamJunctionMap,
                        ConcurrentMap<String, EventTable> eventTableMap, ConcurrentMap<String, PartitionDefinition> partitionDefinitionMap, SiddhiContext siddhiContext) {
        if (query.getOutputStream() != null) {
            this.queryId = query.getOutputStream().getStreamId() + "-" + UUID.randomUUID();
        } else {
            this.queryId = UUID.randomUUID().toString();
        }
        this.query = query;

        outputRateManager = QueryOutputParser.constructOutputRateManager(query.getOutputRate(), siddhiContext.getScheduledExecutorService(),
                                                                         query.getSelector().getGroupByList().size() != 0,
                                                                         query.getInputStream() instanceof WindowStream||query.getInputStream() instanceof JoinStream);

        QueryCreator queryCreator = QueryCreatorFactiory.constructQueryCreator(queryId, query, streamTableDefinitionMap, streamJunctionMap, eventTableMap,outputRateManager, siddhiContext);
        outputStreamDefinition = queryCreator.getOutputStreamDefinition();
        if (query.getOutputStream() != null) {
            outputCallback = QueryOutputParser.constructOutputCallback(query.getOutputStream(), streamJunctionMap, eventTableMap, siddhiContext, queryCreator.getOutputStreamDefinition());
            outputRateManager.setOutputCallback(outputCallback);
            if(outputRateManager instanceof WrappedSnapshotOutputRateManager){
                ((WrappedSnapshotOutputRateManager) outputRateManager).init();
            }
        }

        QueryPartitioner queryPartitioner = new QueryPartitioner(query.getPartitionId(), queryCreator, queryCallbackList, outputCallback, querySelectorList, partitionDefinitionMap,siddhiContext);

        List<HandlerProcessor> handlerProcessorList = queryPartitioner.constructPartition();
        if (query.getPartitionId() == null) {
            handlerProcessors = handlerProcessorList;
        } else {
            List<List<PartitionExecutor>> partitionExecutors = queryPartitioner.getPartitionExecutors();
            for (int i = 0; i < handlerProcessorList.size(); i++) {
                HandlerProcessor queryStreamProcessor = handlerProcessorList.get(i);
                if ((!(queryStreamProcessor instanceof TableHandlerProcessor))) {
                    handlerProcessors.add(new PartitionHandlerProcessor(queryStreamProcessor.getStreamId(), queryPartitioner, i,partitionExecutors.get(i)));
                }
            }
        }

        for (HandlerProcessor handlerProcessor : handlerProcessors) {
            if (!(handlerProcessor instanceof TableHandlerProcessor)) {
                streamJunctionMap.get(handlerProcessor.getStreamId()).addEventFlow(handlerProcessor);
            }
        }
    }

    public String getQueryId() {
        return queryId;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QueryManager)) {
            return false;
        }

        QueryManager that = (QueryManager) o;

        if (queryId != null ? !queryId.equals(that.queryId) : that.queryId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return queryId != null ? queryId.hashCode() : 0;
    }


    public OutputCallback getOutputCallback() {
        return outputCallback;
    }

    public void removeQuery(ConcurrentMap<String, StreamJunction> streamJunctionMap,
                            ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap) {
        for (HandlerProcessor queryStreamProcessor : handlerProcessors) {
            StreamJunction junction = streamJunctionMap.get(queryStreamProcessor.getStreamId());
            if (junction != null) {
                junction.removeEventFlow(queryStreamProcessor);
            }
        }
        streamTableDefinitionMap.remove(query.getOutputStream().getStreamId());
    }


    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

    public void addCallback(QueryCallback callback) {
            outputRateManager.addQueryCallback(callback);
    }
}
