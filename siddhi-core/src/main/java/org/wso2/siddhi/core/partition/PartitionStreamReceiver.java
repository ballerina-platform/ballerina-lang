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

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.constructor.EventConstructor;
import org.wso2.siddhi.core.event.stream.constructor.StreamEventConverterFactory;
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
    private SiddhiContext siddhiContext;
    private PartitionRuntime partitionRuntime;
    private List<PartitionExecutor> partitionExecutors;
    private EventConstructor eventConstructor;
    private Map<String, StreamJunction> cachedStreamJunctionMap = new ConcurrentHashMap<String, StreamJunction>();

    public PartitionStreamReceiver(SiddhiContext siddhiContext, MetaStreamEvent metaStreamEvent, StreamDefinition streamDefinition,
                                   List<PartitionExecutor> partitionExecutors,
                                   PartitionRuntime partitionRuntime) {
        this.streamDefinition = streamDefinition;
        this.partitionRuntime = partitionRuntime;
        this.partitionExecutors = partitionExecutors;
        this.siddhiContext = siddhiContext;
        streamId = streamDefinition.getId();
        eventConstructor = StreamEventConverterFactory.getConverter(metaStreamEvent);
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    @Override
    public void receive(StreamEvent streamEvent) {
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            String key = partitionExecutor.execute(streamEvent);
            send(key, streamEvent);
        }
    }

    @Override
    public void receive(Event event) {
        StreamEvent streamEvent = eventConstructor.constructStreamEvent(event);
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            String key = partitionExecutor.execute(streamEvent);
            send(key, streamEvent);
        }
        eventConstructor.returnEvent(streamEvent);
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        StreamEvent streamEvent = eventConstructor.constructStreamEvent(event);
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            String key = partitionExecutor.execute(streamEvent);
            send(key, streamEvent);
        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        StreamEvent streamEvent = eventConstructor.constructStreamEvent(timeStamp, data);
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            String key = partitionExecutor.execute(streamEvent);
            send(key, streamEvent);
        }
        eventConstructor.returnEvent(streamEvent);
    }

    private void send(String key, StreamEvent event) {
        if (key == null) {
            return;
        }
        partitionRuntime.clone(key);
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
                        streamJunction = new StreamJunction(streamDefinition, (ExecutorService) siddhiContext.getExecutorService(),
                                siddhiContext.getDefaultEventBufferSize());
                        partitionRuntime.addStreamJunction(streamId + key, streamJunction);
                        cachedStreamJunctionMap.put(streamId + key, streamJunction);
                    }
                    streamJunction.subscribe(((SingleStreamRuntime) streamRuntime).getQueryStreamReceiver());
                }
            }

        }
    }

}
