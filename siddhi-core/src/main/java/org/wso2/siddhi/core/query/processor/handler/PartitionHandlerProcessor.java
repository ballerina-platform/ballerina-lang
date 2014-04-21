/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.processor.handler;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.query.QueryPartitioner;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionHandlerProcessor implements HandlerProcessor {

    static final Logger log = Logger.getLogger(PartitionHandlerProcessor.class);
    private final String streamId;
    private QueryPartitioner queryPartitioner;
    private final int handlerId;
    private List<PartitionExecutor> partitionExecutors;
    private ConcurrentHashMap<String, HandlerProcessor> partitionedHandlerMap = new ConcurrentHashMap<String, HandlerProcessor>();


    public PartitionHandlerProcessor(String streamId, QueryPartitioner queryPartitioner, int handlerId, List<PartitionExecutor> partitionExecutors) {
        this.streamId = streamId;
        this.queryPartitioner = queryPartitioner;
        this.handlerId = handlerId;
        this.partitionExecutors = partitionExecutors;
    }

    @Override
    public void receive(StreamEvent streamEvent) {
        for (PartitionExecutor partitionExecutor : partitionExecutors) {
            if (streamEvent instanceof AtomicEvent) {
                String key = partitionExecutor.execute(((AtomicEvent) streamEvent));
                send(key, (AtomicEvent) streamEvent);
            } else {
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    AtomicEvent event = ((ListEvent) streamEvent).getEvent(i);
                    String key = partitionExecutor.execute(event);
                    send(key, event);

                }
            }
        }
    }

    private void send(String key, AtomicEvent atomicEvent) {
        if (key == null) {
            return;
        }
        HandlerProcessor handlerProcessor = partitionedHandlerMap.get(key);
        if (handlerProcessor == null) {
            handlerProcessor = queryPartitioner.newPartition(handlerId, key);
            partitionedHandlerMap.put(key, handlerProcessor);
        }
        handlerProcessor.receive((StreamEvent) atomicEvent);
    }

    public String getStreamId() {
        return streamId;
    }


}
