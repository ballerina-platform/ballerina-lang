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


package org.wso2.siddhi.core.partition;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.runtime.SingleStreamRuntime;
import org.wso2.siddhi.core.util.parser.OutputParser;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class PartitionRuntime {


    private String partitionId;
    private Partition partition;
    private ConcurrentMap<String, StreamJunction> localStreamJunctionMap = new ConcurrentHashMap<String, StreamJunction>(); //contains definition
    private ConcurrentMap<String, AbstractDefinition> localStreamDefinitionMap = new ConcurrentHashMap<String, AbstractDefinition>(); //contains stream definition
    private SiddhiContext siddhiContext;
    private ConcurrentMap<String, AbstractDefinition> streamDefinitionMap;
    private ConcurrentMap<String, StreamJunction> streamJunctionMap;
    private ConcurrentMap<String, QueryRuntime> metaQueryRuntimeMap = new ConcurrentHashMap<String, QueryRuntime>();
    private List<PartitionInstanceRuntime> partitionInstanceRuntimeList = new ArrayList<PartitionInstanceRuntime>();
    private ConcurrentMap<String, PartitionStreamReceiver> partitionStreamReceivers = new ConcurrentHashMap<String, PartitionStreamReceiver>();
    private ExecutionPlanRuntime executionPlanRuntime;


    public PartitionRuntime(ExecutionPlanRuntime executionPlanRuntime, Partition partition, SiddhiContext siddhiContext) {
        try {
            Element element = AnnotationHelper.getAnnotationElement("info", "name", partition.getAnnotations());
            if (element != null) {
                this.partitionId = element.getValue();
            }
        } catch (DuplicateAnnotationException e) {
            throw new QueryCreationException(e.getMessage() + " for the same Query " + partition.toString());
        }
        if (partitionId == null) {
            this.partitionId = UUID.randomUUID().toString();
        }
        this.partition = partition;
        this.streamDefinitionMap = executionPlanRuntime.getStreamDefinitionMap();
        this.streamJunctionMap = executionPlanRuntime.getStreamJunctions();
        this.executionPlanRuntime = executionPlanRuntime;
        this.siddhiContext = siddhiContext;
    }

    public QueryRuntime addQuery(QueryRuntime metaQueryRuntime) {
        Query query = metaQueryRuntime.getQuery();
        OutputCallback outputCallback;
        if (query.getOutputStream() instanceof InsertIntoStream && ((InsertIntoStream) query.getOutputStream()).isInnerStream()) {
            metaQueryRuntime.setToLocalStream(true);
            outputCallback = OutputParser.constructOutputCallback(query.getOutputStream(), localStreamJunctionMap,
                    metaQueryRuntime.getOutputStreamDefinition(), siddhiContext);
        } else {
            outputCallback = OutputParser.constructOutputCallback(query.getOutputStream(), streamJunctionMap,
                    metaQueryRuntime.getOutputStreamDefinition(), siddhiContext);
        }
        metaQueryRuntime.setOutputCallback(outputCallback);

        metaQueryRuntimeMap.put(metaQueryRuntime.getQueryId(), metaQueryRuntime);
        if (metaQueryRuntime.isToLocalStream()) {
            if (outputCallback != null && outputCallback instanceof InsertIntoStreamCallback) {
                defineLocalStream(((InsertIntoStreamCallback) outputCallback).getOutputStreamDefinition());
            }
        } else {
            if (outputCallback != null && outputCallback instanceof InsertIntoStreamCallback) {
                executionPlanRuntime.defineStream(((InsertIntoStreamCallback) outputCallback).getOutputStreamDefinition());
            }
        }
        return metaQueryRuntime;
    }

    public void addPartitionReceiver(QueryRuntime queryRuntime, List<VariableExpressionExecutor> executors, MetaStreamEvent metaStreamEvent) {
        Query query = queryRuntime.getQuery();
        if (queryRuntime.getStreamRuntime() instanceof SingleStreamRuntime && !((SingleInputStream) query.getInputStream()).isInnerStream()) {
            if (!partitionStreamReceivers.containsKey(((SingleInputStream) query.getInputStream()).getStreamId())) {
                List<List<PartitionExecutor>> partitionExecutors = new StreamPartitioner(query.getInputStream(), partition, metaStreamEvent,
                        executors, siddhiContext).getPartitionExecutors();
                addPartitionReceiver(new PartitionStreamReceiver(siddhiContext, metaStreamEvent,
                        (StreamDefinition) streamDefinitionMap.get(((SingleInputStream) query.getInputStream()).getStreamId()),
                        partitionExecutors.get(0), this));
            }
        }//TODO: joins

    }

    /**
     * clone all the queries of the partition for a given partition key
     *
     * @param key partition key
     */
    public void clone(String key) {
        PartitionInstanceRuntime partitionInstance = this.getPartitionInstanceRuntime(key);
        if (partitionInstance == null) {
            clonePartition(key);
        }
    }

    private synchronized void clonePartition(String key) {
        PartitionInstanceRuntime partitionInstance = this.getPartitionInstanceRuntime(key);

        if (partitionInstance == null) {
            List<QueryRuntime> queryRuntimeList = new ArrayList<QueryRuntime>();
            List<QueryRuntime> partitionedQueryRuntimeList = new CopyOnWriteArrayList<QueryRuntime>();

            for (QueryRuntime queryRuntime : metaQueryRuntimeMap.values()) { ///TODO:join
                String streamId = queryRuntime.getInputStreamId().get(0);
                QueryRuntime clonedQueryRuntime = queryRuntime.clone(key, localStreamJunctionMap);
                queryRuntimeList.add(clonedQueryRuntime);

                if (queryRuntime.isFromLocalStream()) {
                    StreamJunction streamJunction = localStreamJunctionMap.get(streamId + key);
                    if (streamJunction == null) {
                        streamJunction = new StreamJunction((StreamDefinition) localStreamDefinitionMap.get(streamId),
                                (ExecutorService) siddhiContext.getExecutorService(), siddhiContext.getDefaultEventBufferSize());
                        localStreamJunctionMap.put(streamId + key, streamJunction);
                    }
                    streamJunction.subscribe(((SingleStreamRuntime) (clonedQueryRuntime.getStreamRuntime())).getQueryStreamReceiver());
                } else {
                    partitionedQueryRuntimeList.add(clonedQueryRuntime);
                }
            }
            addPartitionInstance(new PartitionInstanceRuntime(key, queryRuntimeList));
            updatePartitionStreamReceivers(key, partitionedQueryRuntimeList);

        }

    }

    private void updatePartitionStreamReceivers(String key, List<QueryRuntime> partitionedQueryRuntimeList) {
        for (PartitionStreamReceiver partitionStreamReceiver : partitionStreamReceivers.values()) {
            partitionStreamReceiver.addStreamJunction(key, partitionedQueryRuntimeList);
        }
    }

    public void addPartitionInstance(PartitionInstanceRuntime partitionInstanceRuntime) {
        partitionInstanceRuntimeList.add(partitionInstanceRuntime);
    }

    public PartitionInstanceRuntime getPartitionInstanceRuntime(String key) {
        for(PartitionInstanceRuntime partitionInstanceRuntime:partitionInstanceRuntimeList) {
             if(key.equals(partitionInstanceRuntime.getKey())){
                 return partitionInstanceRuntime;
             }
        }
        return null;
    }

    public void addStreamJunction(String key, StreamJunction streamJunction) {
        localStreamJunctionMap.put(key, streamJunction);
    }

    private void addPartitionReceiver(PartitionStreamReceiver partitionStreamReceiver) {
        partitionStreamReceivers.put(partitionStreamReceiver.getStreamId(), partitionStreamReceiver);
        streamJunctionMap.get(partitionStreamReceiver.getStreamId()).subscribe(partitionStreamReceiver);
    }

    public String getPartitionId() {
        return partitionId;
    }

    public ConcurrentMap<String, AbstractDefinition> getLocalStreamDefinitionMap() {
        return localStreamDefinitionMap;
    }

    /**
     * define inner stream
     *
     * @param streamDefinition definition of an inner stream
     */
    public void defineLocalStream(StreamDefinition streamDefinition) {
        if (!checkEventStreamExist(streamDefinition, localStreamDefinitionMap)) {
            localStreamDefinitionMap.put(streamDefinition.getId(), streamDefinition);
            StreamJunction streamJunction = localStreamJunctionMap.get(streamDefinition.getId());
            if (streamJunction == null) {
                streamJunction = new StreamJunction(streamDefinition, (ExecutorService) siddhiContext.getExecutorService(),
                        siddhiContext.getDefaultEventBufferSize());
                localStreamJunctionMap.putIfAbsent(streamDefinition.getId(), streamJunction);
            }
        }
    }

    private boolean checkEventStreamExist(StreamDefinition newStreamDefinition, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap) {
        AbstractDefinition definition = streamDefinitionMap.get(newStreamDefinition.getId());
        if (definition != null) {
            if (definition instanceof TableDefinition) {
                throw new DifferentDefinitionAlreadyExistException("Table " + newStreamDefinition.getId() + " is already defined as "
                        + definition + ", hence cannot define " + newStreamDefinition);
            } else if (!definition.getAttributeList().equals(newStreamDefinition.getAttributeList())) {
                throw new DifferentDefinitionAlreadyExistException("Stream " + newStreamDefinition.getId() + " is already defined as "
                        + definition + ", hence cannot define " + newStreamDefinition);
            } else {
                return true;
            }
        }
        return false;
    }

}
