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

/**
 * Implementation of {@link CollectionExecutor} which handles or condition
 */
public class OrCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor rightCollectionExecutor;
    private final CollectionExecutor leftCollectionExecutor;
    private CollectionExecutor exhaustiveCollectionExecutor;

    public OrCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor
            rightCollectionExecutor, CollectionExecutor exhaustiveCollectionExecutor) {

        this.leftCollectionExecutor = leftCollectionExecutor;
        this.rightCollectionExecutor = rightCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner
            storeEventCloner) {

        Collection<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, storeEventCloner);
        } else {
            Collection<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEvents(matchingEvent,
                    indexedEventHolder);
            if (rightStreamEvents == null) {
                return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, storeEventCloner);
            } else {
                ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
                for (StreamEvent resultEvent : leftStreamEvents) {
                    if (storeEventCloner != null) {
                        returnEventChunk.add(storeEventCloner.copyStreamEvent(resultEvent));
                    } else {
                        returnEventChunk.add(resultEvent);
                    }
                }
                for (StreamEvent resultEvent : rightStreamEvents) {
                    if (!leftStreamEvents.contains(resultEvent)) {
                        if (storeEventCloner != null) {
                            returnEventChunk.add(storeEventCloner.copyStreamEvent(resultEvent));
                        } else {
                            returnEventChunk.add(resultEvent);
                        }
                    }
                }
                return returnEventChunk.getFirst();
            }
        }


    }

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            return null;
        } else {
            Collection<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEvents(matchingEvent,
                    indexedEventHolder);
            if (rightStreamEvents == null) {
                return null;
            } else {
                HashSet<StreamEvent> resultSet = new HashSet<StreamEvent>(leftStreamEvents);
                resultSet.addAll(rightStreamEvents);
                return resultSet;
            }
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (leftStreamEvents != null && leftStreamEvents.size() > 0) {
            return true;
        }

        Collection<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEvents(matchingEvent,
                indexedEventHolder);
        if (rightStreamEvents != null && rightStreamEvents.size() > 0) {
            return true;
        }

        return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEvents(deletingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        } else {
            Collection<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEvents(deletingEvent,
                    indexedEventHolder);
            if (rightStreamEvents == null) {
                exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
            } else {
                leftCollectionExecutor.delete(deletingEvent, indexedEventHolder);
                rightCollectionExecutor.delete(deletingEvent, indexedEventHolder);
            }
        }
    }

    @Override
    public Cost getDefaultCost() {
        Cost leftCost = leftCollectionExecutor.getDefaultCost();
        Cost rightCost = rightCollectionExecutor.getDefaultCost();
        if (leftCost.getWeight() < rightCost.getWeight()) {
            return rightCost;
        } else {
            return leftCost;
        }
    }
}
