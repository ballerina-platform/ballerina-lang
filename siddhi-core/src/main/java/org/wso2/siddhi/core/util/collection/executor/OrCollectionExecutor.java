/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Set;

public class OrCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor rightCollectionExecutor;
    private final CollectionExecutor leftCollectionExecutor;
    private CollectionExecutor exhaustiveCollectionExecutor;

    public OrCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor rightCollectionExecutor, CollectionExecutor exhaustiveCollectionExecutor) {

        this.leftCollectionExecutor = leftCollectionExecutor;
        this.rightCollectionExecutor = rightCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        Set<StreamEvent> resultEventSet = findEventSet(matchingEvent, indexedEventHolder);
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);

        if (resultEventSet != null) {
            for (StreamEvent resultEvent : resultEventSet) {
                if (candidateEventCloner != null) {
                    returnEventChunk.add(candidateEventCloner.copyStreamEvent(resultEvent));
                } else {
                    returnEventChunk.add(resultEvent);
                }
            }
            return returnEventChunk.getFirst();
        } else {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            return null;
        } else {
            Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
            if (rightStreamEvents == null) {
                return null;

            } else {
                leftStreamEvents.addAll(rightStreamEvents);
                return leftStreamEvents;
            }
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (leftStreamEvents != null && leftStreamEvents.size() > 0) {
            return true;
        }

        Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (rightStreamEvents != null && rightStreamEvents.size() > 0) {
            return true;
        }

        return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEventSet(deletingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        } else {
            Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(deletingEvent, indexedEventHolder);
            if (rightStreamEvents == null) {
                exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
            } else {
                leftCollectionExecutor.delete(deletingEvent, indexedEventHolder);
                rightCollectionExecutor.delete(deletingEvent, indexedEventHolder);
            }
        }
    }

}
