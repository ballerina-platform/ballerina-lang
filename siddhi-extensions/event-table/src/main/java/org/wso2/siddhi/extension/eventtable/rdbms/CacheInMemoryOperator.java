/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.eventtable.rdbms;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.extension.eventtable.cache.CachingTable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.ANY;


/**
 * Operator which does In-memory cache related operations
 */
public class CacheInMemoryOperator implements Operator {
    private final StreamEvent matchingEvent;
    private final ZeroStreamEventConverter streamEventConverter;
    private FinderStateEvent event;
    private ExpressionExecutor expressionExecutor;
    private int candidateEventPosition;
    private int matchingEventPosition;
    private int streamEvents;
    private long withinTime;
    private int matchingEventOutputSize;
    private CachingTable cachingTable;


    public CacheInMemoryOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition, int matchingEventPosition, int streamEvents, long withinTime, CachingTable cachingTable, int matchingEventOutputSize) {
        this.streamEvents = streamEvents;
        this.withinTime = withinTime;
        this.matchingEventOutputSize = matchingEventOutputSize;
        this.event = new FinderStateEvent(streamEvents, 0);
        this.matchingEvent = new StreamEvent(0, 0, matchingEventOutputSize);
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.matchingEventPosition = matchingEventPosition;
        this.cachingTable = cachingTable;
        this.streamEventConverter = new ZeroStreamEventConverter();
    }

    public boolean execute(StreamEvent candidateEvent) {
        event.setEvent(candidateEventPosition, candidateEvent);
        boolean result = (Boolean) expressionExecutor.execute(event);
        event.setEvent(candidateEventPosition, null);
        return result;
    }

    @Override
    public Finder cloneFinder() {
        return new CacheInMemoryOperator(expressionExecutor, candidateEventPosition, matchingEventPosition, streamEvents, withinTime, cachingTable, matchingEventOutputSize);
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {

        try {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, ((StreamEvent) matchingEvent));
            } else {
                this.event.setEvent(((StateEvent) matchingEvent));
            }
            if (candidateEvents instanceof ComplexEventChunk) {
                return find((ComplexEventChunk) candidateEvents, streamEventCloner);
            } else if (candidateEvents instanceof Map) {
                return find(((Map) candidateEvents).values(), streamEventCloner);
            } else if (candidateEvents instanceof Collection) {
                return find((Collection) candidateEvents, streamEventCloner);
            } else {
                throw new OperationNotSupportedException(CacheInMemoryOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        } finally {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, null);
            } else {
                this.event.setEvent(null);
            }
        }
    }

    @Override
    public void delete(ComplexEventChunk deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            ComplexEvent deletingEvent = deletingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(deletingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    delete((ComplexEventChunk) candidateEvents);
                } else if (candidateEvents instanceof Map) {
                    delete(((Map) candidateEvents).values());
                } else if (candidateEvents instanceof Collection) {
                    delete((Collection) candidateEvents);
                } else {
                    throw new OperationNotSupportedException(CacheInMemoryOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    private void delete(ComplexEventChunk<StreamEvent> candidateEventChunk) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.delete(streamEvent);
                candidateEventChunk.remove();
            }
        }
    }

    private void delete(Collection<StreamEvent> candidateEvents) {
        for (Iterator<StreamEvent> iterator = candidateEvents.iterator(); iterator.hasNext(); ) {
            StreamEvent streamEvent = iterator.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.delete(streamEvent);
                iterator.remove();
            }
        }
    }

    @Override
    public void update(ComplexEventChunk updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            ComplexEvent updatingEvent = updatingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(updatingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    update((ComplexEventChunk) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof Map) {
                    update(((Map) candidateEvents).values(), mappingPosition);
                } else if (candidateEvents instanceof Collection) {
                    update((Collection) candidateEvents, mappingPosition);
                } else {
                    throw new OperationNotSupportedException(CacheInMemoryOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    @Override
    public void overwriteOrAdd(ComplexEventChunk overwritingOrAddingEventChunk, Object candidateEvents, int[] mappingPosition) {
        overwritingOrAddingEventChunk.reset();
        while (overwritingOrAddingEventChunk.hasNext()) {
            ComplexEvent updatingEvent = overwritingOrAddingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(updatingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    overwriteOrAdd((ComplexEventChunk) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof Map) {
                    overwriteOrAdd(((Map) candidateEvents).values(), mappingPosition);
                } else if (candidateEvents instanceof Collection) {
                    overwriteOrAdd((Collection) candidateEvents, mappingPosition);
                } else {
                    throw new OperationNotSupportedException(CacheInMemoryOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }

    }

    private void update(ComplexEventChunk<StreamEvent> candidateEventChunk, int[] mappingPosition) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.update(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }

    private void overwriteOrAdd(ComplexEventChunk<StreamEvent> candidateEventChunk, int[] mappingPosition) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.overwriteOrAdd(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }

    private void update(Collection<StreamEvent> candidateEvents, int[] mappingPosition) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.update(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }


    private void overwriteOrAdd(Collection<StreamEvent> candidateEvents, int[] mappingPosition) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.overwriteOrAdd(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {
        try {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, ((StreamEvent) matchingEvent));
            } else {
                this.event.setEvent(((StateEvent) matchingEvent));
            }
            if (candidateEvents instanceof ComplexEventChunk) {
                return contains((ComplexEventChunk) candidateEvents);
            } else if (candidateEvents instanceof Map) {
                return contains(((Map) candidateEvents).values());
            } else if (candidateEvents instanceof Collection) {
                return contains((Collection) candidateEvents);
            } else {
                throw new OperationNotSupportedException(CacheInMemoryOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        } finally {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, null);
            } else {
                this.event.setEvent(null);
            }
        }
    }

    private boolean contains(Collection<StreamEvent> candidateEvents) {

        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.contains(streamEvent);
                return true;
            }
        }
        return false;
    }

    private boolean contains(ComplexEventChunk<StreamEvent> candidateEventChunk) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                cachingTable.contains(streamEvent);
                return true;
            }
        }
        return false;
    }

    private StreamEvent find(ComplexEventChunk<StreamEvent> candidateEventChunk, StreamEventCloner streamEventCloner) {
        candidateEventChunk.reset();
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }


    protected StreamEvent find(Collection<StreamEvent> candidateEvents, StreamEventCloner streamEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    break;
                }
            }
            if (execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }


    protected class FinderStateEvent extends StateEvent {

        public FinderStateEvent(int size, int outputSize) {
            super(size, outputSize);
        }

        public void setEvent(StateEvent matchingStateEvent) {
            if (matchingStateEvent != null) {
                System.arraycopy(matchingStateEvent.getStreamEvents(), 0, streamEvents, 0, matchingStateEvent.getStreamEvents().length);
            } else {
                for (int i = 0; i < streamEvents.length - 1; i++) {
                    streamEvents[i] = null;
                }
            }
        }
    }
}
