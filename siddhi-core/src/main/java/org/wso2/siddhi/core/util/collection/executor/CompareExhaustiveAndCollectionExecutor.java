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

/**
 * Implementation of {@link CollectionExecutor}
 */
public class CompareExhaustiveAndCollectionExecutor implements CollectionExecutor {

    private final CollectionExecutor compareCollectionExecutor;
    private ExhaustiveCollectionExecutor exhaustiveCollectionExecutor;

    public CompareExhaustiveAndCollectionExecutor(CollectionExecutor compareCollectionExecutor,
                                                  ExhaustiveCollectionExecutor exhaustiveCollectionExecutor) {
        this.compareCollectionExecutor = compareCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner
            storeEventCloner) {
        Collection<StreamEvent> compareStreamEvents = compareCollectionExecutor.findEvents(matchingEvent,
                indexedEventHolder);
        if (compareStreamEvents == null) {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, storeEventCloner);
        } else if (compareStreamEvents.size() > 0) {
            compareStreamEvents = exhaustiveCollectionExecutor.findEvents(matchingEvent, compareStreamEvents);
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            for (StreamEvent resultEvent : compareStreamEvents) {
                if (storeEventCloner != null) {
                    returnEventChunk.add(storeEventCloner.copyStreamEvent(resultEvent));
                } else {
                    returnEventChunk.add(resultEvent);
                }
            }
            return returnEventChunk.getFirst();

        } else {
            return null;
        }
    }

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> compareStreamEvents = compareCollectionExecutor.findEvents(matchingEvent,
                indexedEventHolder);
        if (compareStreamEvents == null) {
            return null;
        } else if (compareStreamEvents.size() > 0) {
            if (exhaustiveCollectionExecutor != null) {
                return exhaustiveCollectionExecutor.findEvents(matchingEvent, compareStreamEvents);
            } else {
                return null;
            }
        } else {
            return compareStreamEvents;
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> compareStreamEvents = findEvents(matchingEvent, indexedEventHolder);
        if (compareStreamEvents == null) {
            return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
        } else {
            return compareStreamEvents.size() > 0;
        }
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> compareStreamEvents = findEvents(deletingEvent, indexedEventHolder);
        if (compareStreamEvents == null) {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        }
    }

    @Override
    public Cost getDefaultCost() {
        if (exhaustiveCollectionExecutor != null) {
            return compareCollectionExecutor.getDefaultCost();
        } else {
            return Cost.EXHAUSTIVE;
        }
    }

}
