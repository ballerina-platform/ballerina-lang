/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.extension.eventtable.cache.CachingTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import static org.wso2.siddhi.core.util.SiddhiConstants.*;


/**
 * Operator which does In-memory cache related operations
 */
public class CacheInMemoryOperator implements Operator {
    private FinderStateEvent event;
    private ExpressionExecutor expressionExecutor;
    private int candidateEventPosition;
    private int matchingEventPosition;
    private int streamEvents;
    private long withinTime;
    private CachingTable cachingTable;


    public CacheInMemoryOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition, int matchingEventPosition, int streamEvents, long withinTime, CachingTable cachingTable) {
        this.streamEvents = streamEvents;
        this.withinTime = withinTime;
        this.event = new FinderStateEvent(streamEvents, 0);
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.matchingEventPosition = matchingEventPosition;
        this.cachingTable = cachingTable;
    }

    public boolean execute(StreamEvent candidateEvent) {
        event.setEvent(candidateEventPosition, candidateEvent);
        boolean result = (Boolean) expressionExecutor.execute(event);
        event.setEvent(candidateEventPosition, null);
        return result;
    }

    @Override
    public Finder cloneFinder() {
        return new CacheInMemoryOperator(expressionExecutor, candidateEventPosition, matchingEventPosition, streamEvents, withinTime, cachingTable);
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
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StreamEvent deletingEvent = deletingEventChunk.next();
            try {
                this.event.setEvent(matchingEventPosition, deletingEvent);
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
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StreamEvent updatingEvent = updatingEventChunk.next();
            try {
                this.event.setEvent(matchingEventPosition, updatingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    update((ComplexEventChunk) candidateEvents, mappingPosition, updatingEvent);
                } else if (candidateEvents instanceof Map) {
                    update(((Map) candidateEvents).values(), mappingPosition, updatingEvent);
                } else if (candidateEvents instanceof Collection) {
                    update((Collection) candidateEvents, mappingPosition, updatingEvent);
                } else {
                    throw new OperationNotSupportedException(CacheInMemoryOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    private void update(ComplexEventChunk<StreamEvent> candidateEventChunk, int[] mappingPosition, StreamEvent updatingEvent) {
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
                    streamEvent.setOutputData(updatingEvent.getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }

    private void update(Collection<StreamEvent> candidateEvents, int[] mappingPosition, StreamEvent updatingEvent) {
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
                    streamEvent.setOutputData(updatingEvent.getOutputData()[i], mappingPosition[i]);
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
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
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
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
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
        private StateEvent matchingStateEvent;

        public FinderStateEvent(int size, int outputSize) {
            super(size, outputSize);
        }

        public void setEvent(StateEvent matchingStateEvent) {
            this.matchingStateEvent = matchingStateEvent;
        }

        public StreamEvent getStreamEvent(int position) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                return matchingStateEvent.getStreamEvent(position);
            } else {
                return super.getStreamEvent(position);
            }
        }

        public StreamEvent[] getStreamEvents() {
            if (matchingStateEvent != null) {
                int length = matchingStateEvent.getStreamEvents().length;
                StreamEvent[] streamEvents = Arrays.copyOf(matchingStateEvent.getStreamEvents(), length + 1);
                streamEvents[length] = this.streamEvents[length];
                return streamEvents;
            } else {
                return super.getStreamEvents();
            }
        }

        public void setNext(ComplexEvent stateEvent) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setNext(stateEvent);
            } else {
                super.setNext(stateEvent);
            }
        }

        public StateEvent getNext() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getNext();
            } else {
                return super.getNext();
            }
        }

        @Override
        public void setOutputData(Object object, int index) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setOutputData(object, index);
            } else {
                super.setOutputData(object, index);
            }
        }

        @Override
        public Object getAttribute(int[] position) {
            if (position[STREAM_ATTRIBUTE_TYPE_INDEX] == STATE_OUTPUT_DATA_INDEX) {
                return outputData[position[STREAM_ATTRIBUTE_INDEX]];
            } else {
                int index = position[STREAM_EVENT_CHAIN_INDEX];
                if (matchingStateEvent != null && index < matchingStateEvent.getStreamEvents().length) {
                    return matchingStateEvent.getAttribute(position);
                } else {
                    return super.getAttribute(position);
                }
            }
        }

        public Object[] getOutputData() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getOutputData();
            } else {
                return super.getOutputData();
            }
        }

        public long getTimestamp() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getTimestamp();
            } else {
                return super.getTimestamp();
            }
        }

        public void setTimestamp(long timestamp) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setTimestamp(timestamp);
            } else {
                super.setTimestamp(timestamp);
            }
        }

        public void setType(Type type) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setType(type);
            } else {
                super.setType(type);
            }
        }

        @Override
        public Type getType() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getType();
            } else {
                return super.getType();
            }
        }

        public void setEvent(int position, StreamEvent streamEvent) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                matchingStateEvent.setEvent(position, streamEvent);
            } else {
                super.setEvent(position, streamEvent);
            }
        }

        public void addEvent(int position, StreamEvent streamEvent) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                matchingStateEvent.addEvent(position, streamEvent);
            } else {
                super.addEvent(position, streamEvent);
            }
        }

        public void removeLastEvent(int position) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                matchingStateEvent.removeLastEvent(position);
            } else {
                super.removeLastEvent(position);
            }
        }

        @Override
        public String toString() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.toString() + streamEvents[streamEvents.length - 1].toString();
            } else {
                return super.toString();
            }
        }

        public long getId() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getId();
            } else {
                return super.getId();
            }
        }

        public void setId(long id) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setId(id);
            } else {
                super.setId(id);
            }
        }
    }
}
