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

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.converter.EventManager;
import org.wso2.siddhi.core.event.stream.converter.StreamEventManagerFactory;
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.runtime.SingleStreamRuntime;
import org.wso2.siddhi.core.stream.runtime.StreamRuntime;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class PartitionStreamReceiver implements StreamJunction.Receiver {

    private String streamId;
    private StreamDefinition streamDefinition;
    private ExecutionPlanContext executionPlanContext;
    private PartitionRuntime partitionRuntime;
    private List<PartitionExecutor> partitionExecutors;
    private EventManager eventManager;
    private Map<String, StreamJunction> cachedStreamJunctionMap = new ConcurrentHashMap<String, StreamJunction>();


    public PartitionStreamReceiver(ExecutionPlanContext executionPlanContext, MetaStreamEvent metaStreamEvent, StreamDefinition streamDefinition,
                                   List<PartitionExecutor> partitionExecutors,
                                   PartitionRuntime partitionRuntime) {
        this.streamDefinition = streamDefinition;
        this.partitionRuntime = partitionRuntime;
        this.partitionExecutors = partitionExecutors;
        this.executionPlanContext = executionPlanContext;
        streamId = streamDefinition.getId();
        eventManager = StreamEventManagerFactory.constructEventManager(metaStreamEvent);
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    @Override
    public void receive(StreamEvent streamEvent) {
        while (streamEvent != null){
            StreamEvent aStreamEvent = streamEvent.getNext();
            streamEvent.setNext(null);
            for (PartitionExecutor partitionExecutor : partitionExecutors) {
                String key = partitionExecutor.execute(streamEvent);
                send(key, streamEvent);
            }
            streamEvent = aStreamEvent;
        }
    }

    @Override
    public void receive(Event event) {
        StreamEvent borrowedEvent = eventManager.borrowEvent();
        eventManager.convertEvent(event, borrowedEvent);
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            String key = partitionExecutor.execute(borrowedEvent);
            send(key, borrowedEvent);
        }
        eventManager.returnEvent(borrowedEvent);
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        receive(event);
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        StreamEvent borrowedEvent = eventManager.borrowEvent();
        eventManager.convertData(timeStamp, data, borrowedEvent);
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            String key = partitionExecutor.execute(borrowedEvent);
            send(key, borrowedEvent);
        }
        eventManager.returnEvent(borrowedEvent);
    }

    @Override
    public void receive(Event[] events) {
        for(Event event:events){
            StreamEvent borrowedEvent = eventManager.borrowEvent();
            eventManager.convertEvent(event, borrowedEvent);
            for (PartitionExecutor partitionExecutor : partitionExecutors) {
                String key = partitionExecutor.execute(borrowedEvent);
                send(key, borrowedEvent);
            }
            eventManager.returnEvent(borrowedEvent);
        }
    }

    private void send(String key, StreamEvent event) {
        partitionRuntime.cloneIfNotExist(key);
        cachedStreamJunctionMap.get(streamId + key).sendEvent(event);
    }

    /**
     * create local streamJunctions through which events received by partitionStreamReceiver, are sent to queryStreamReceivers
     *
     * @param key              partitioning key
     * @param queryRuntimeList queryRuntime list of the partition
     */
    public void addStreamJunction(String key, List<QueryRuntime> queryRuntimeList) {
        if (!partitionExecutors.isEmpty()) {
            for (QueryRuntime queryRuntime : queryRuntimeList) {
                if (queryRuntime.getInputStreamId().get(0).equals(streamId)) {
                    StreamRuntime streamRuntime = queryRuntime.getStreamRuntime();
                    StreamJunction streamJunction = cachedStreamJunctionMap.get(streamId + key);
                    if (streamJunction == null) {
                        streamJunction = new StreamJunction(streamDefinition,
                                (ExecutorService) executionPlanContext.getSiddhiContext().getExecutorService(),
                                executionPlanContext.getSiddhiContext().getEventBufferSize(), executionPlanContext);
                        partitionRuntime.addStreamJunction(streamId + key, streamJunction);
                        cachedStreamJunctionMap.put(streamId + key, streamJunction);
                    }
                    streamJunction.subscribe(((SingleStreamRuntime) streamRuntime).getQueryStreamReceiver());
                }
            }

        }
    }

}
