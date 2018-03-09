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

/**
 * Implementation of {@link CollectionExecutor}
 */
public class AnyAndCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor highCostCollectionExecutor;
    private final CollectionExecutor lowCollectionExecutor;
    private ExhaustiveCollectionExecutor exhaustiveCollectionExecutor;

    public AnyAndCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor
            rightCostCollectionExecutor,
                                    ExhaustiveCollectionExecutor exhaustiveCollectionExecutor) {
        if (leftCollectionExecutor.getDefaultCost().getWeight() <= rightCostCollectionExecutor.getDefaultCost()
                .getWeight()) {
            this.lowCollectionExecutor = leftCollectionExecutor;
            this.highCostCollectionExecutor = rightCostCollectionExecutor;
        } else {
            this.lowCollectionExecutor = rightCostCollectionExecutor;
            this.highCostCollectionExecutor = leftCollectionExecutor;
        }
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner
            storeEventCloner) {

        Collection<StreamEvent> resultEventSet = findEvents(matchingEvent, indexedEventHolder);
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);

        if (resultEventSet != null) {
            for (StreamEvent resultEvent : resultEventSet) {
                if (storeEventCloner != null) {
                    returnEventChunk.add(storeEventCloner.copyStreamEvent(resultEvent));
                } else {
                    returnEventChunk.add(resultEvent);
                }
            }
            return returnEventChunk.getFirst();
        } else {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, storeEventCloner);
        }
    }

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        //limit for 10 is a magic number identified via performance test
        Collection<StreamEvent> lowCostStreamEvents = lowCollectionExecutor.findEvents(matchingEvent,
                                                                                       indexedEventHolder);
        if (lowCostStreamEvents == null) {
            return null;
        } else if (lowCostStreamEvents.size() > 0) {
            if (lowCostStreamEvents.size() <= 10) {
                return exhaustiveCollectionExecutor.findEvents(matchingEvent, lowCostStreamEvents);
            } else {
                Collection<StreamEvent> highCostStreamEvents = highCostCollectionExecutor.findEvents(
                        matchingEvent, indexedEventHolder);

                if (highCostStreamEvents == null) {
                    return null;
                } else if (highCostStreamEvents.size() > 0) {
                    if (lowCostStreamEvents.size() <= 10) {
                        return exhaustiveCollectionExecutor.findEvents(matchingEvent, highCostStreamEvents);
                    } else {
                        Set<StreamEvent> returnSet = new HashSet<StreamEvent>();
                        if (highCostStreamEvents.size() > lowCostStreamEvents.size()) {
                            for (StreamEvent aStreamEvent : lowCostStreamEvents) {
                                if (highCostStreamEvents.contains(aStreamEvent)) {
                                    returnSet.add(aStreamEvent);
                                }
                            }
                        } else {
                            for (StreamEvent aStreamEvent : highCostStreamEvents) {
                                if (lowCostStreamEvents.contains(aStreamEvent)) {
                                    returnSet.add(aStreamEvent);
                                }
                            }
                        }
                        return returnSet;
                    }
                } else {
                    return highCostStreamEvents;
                }
            }
        } else {
            return lowCostStreamEvents;
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

    @Override
    public Cost getDefaultCost() {
        return lowCollectionExecutor.getDefaultCost();
    }

}
