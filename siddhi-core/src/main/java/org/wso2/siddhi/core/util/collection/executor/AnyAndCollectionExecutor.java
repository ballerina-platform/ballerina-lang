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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AnyAndCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor rightCollectionExecutor;
    private final CollectionExecutor leftCollectionExecutor;
    private CollectionExecutor exhaustiveCollectionExecutor;

    public AnyAndCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor rightCollectionExecutor, CollectionExecutor exhaustiveCollectionExecutor) {

        this.leftCollectionExecutor = leftCollectionExecutor;
        this.rightCollectionExecutor = rightCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        Collection<StreamEvent> resultEventSet = findEvents(matchingEvent, indexedEventHolder);
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

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            return null;
        } else if (leftStreamEvents.size() > 0) {
            Collection<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
            if (rightStreamEvents == null) {
                return null;
            } else if (rightStreamEvents.size() > 0) {
                Set<StreamEvent> returnSet = new HashSet<StreamEvent>();
                if (rightStreamEvents.size() > leftStreamEvents.size()) {
                    for (StreamEvent aStreamEvent : leftStreamEvents) {
                        if (rightStreamEvents.contains(aStreamEvent)) {
                            returnSet.add(aStreamEvent);
                        }
                    }
                } else {
                    for (StreamEvent aStreamEvent : rightStreamEvents) {
                        if (leftStreamEvents.contains(aStreamEvent)) {
                            returnSet.add(aStreamEvent);
                        }
                    }
                }
                return returnSet;
            } else {
                return rightStreamEvents;
            }
        } else {
            return leftStreamEvents;
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> resultEventSet = findEvents(matchingEvent, indexedEventHolder);
        if (resultEventSet != null) {
            return resultEventSet.size() > 0;
        } else {
            return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
        }
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> resultEventSet = findEvents(deletingEvent, indexedEventHolder);
        if (resultEventSet != null) {
            indexedEventHolder.deleteAll(resultEventSet);
        } else {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        }
    }

}
