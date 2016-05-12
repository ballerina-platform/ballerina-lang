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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.ANY;

/**
 * Operator which is related to non-indexed Hazelcast table operations.
 */
public class HazelcastOperator implements Operator {
    private final ZeroStreamEventConverter streamEventConverter;
    private final StreamEvent matchingEvent;
    private final StreamEventPool streamEventPool;
    protected ExpressionExecutor expressionExecutor;
    protected int candidateEventPosition;
    protected int matchingEventPosition;
    protected int streamEventSize;
    protected long withinTime;
    private FinderStateEvent event;
    private int matchingEventOutputSize;
    private int indexedPosition;

    public HazelcastOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition,
                             int matchingEventPosition, int streamEventSize, long withinTime,
                             int matchingEventOutputSize, int indexedPosition) {
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.matchingEventPosition = matchingEventPosition;
        this.streamEventSize = streamEventSize;
        this.withinTime = withinTime;
        this.matchingEventOutputSize = matchingEventOutputSize;
        this.indexedPosition = indexedPosition;
        this.event = new FinderStateEvent(streamEventSize, 0);
        this.matchingEvent = new StreamEvent(0, 0, matchingEventOutputSize);
        this.streamEventPool = new StreamEventPool(0, 0, matchingEventOutputSize, 10);
        this.streamEventConverter = new ZeroStreamEventConverter();
    }

    private boolean execute(StreamEvent candidateEvent) {
        event.setEvent(candidateEventPosition, candidateEvent);
        boolean result = (Boolean) expressionExecutor.execute(event);
        event.setEvent(candidateEventPosition, null);
        return result;
    }

    /**
     * Checks whether a Stream event resides with in the current window.
     *
     * @param streamEvent Stream event to check.
     * @return whether the Stream event resides with in the current window.
     */
    private boolean outsideTimeWindow(StreamEvent streamEvent) {
        if (withinTime != ANY) {
            long timeDifference = event.getStreamEvent(matchingEventPosition).getTimestamp() -
                    streamEvent.getTimestamp();
            if ((0 > timeDifference) || (timeDifference > withinTime)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Finder cloneFinder() {
        return new HazelcastOperator(expressionExecutor, candidateEventPosition, matchingEventPosition,
                streamEventSize, withinTime, matchingEventOutputSize, indexedPosition);
    }

    /**
     * Called to find a event from event table.
     *
     * @param matchingEvent     the event to be matched with the events at the processor.
     * @param candidateEvents   Map of candidate events.
     * @param streamEventCloner StreamEventCloner to copy new StreamEvent from existing StreamEvent.
     * @return StreamEvent  event found.
     */
    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {
        try {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, (StreamEvent) matchingEvent);
            } else {
                this.event.setEvent((StateEvent) matchingEvent);
            }
            if (candidateEvents instanceof ComplexEventChunk) {
                return findInComplexEventChunk((ComplexEventChunk) candidateEvents, streamEventCloner);
            } else if (candidateEvents instanceof Map) {
                return findInCollection(((Map) candidateEvents).values(), streamEventCloner);
            } else if (candidateEvents instanceof Collection) {
                return findInCollection((Collection) candidateEvents, streamEventCloner);
            } else {
                throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                        " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        } finally {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, null);
            } else {
                this.event.setEvent(null);
            }
        }
    }

    /**
     * Finds an event from a Complex Event Chunk.
     *
     * @param candidateEventChunk Set of events as a ComplexEventChunk.
     * @param streamEventCloner   StreamEventCloner to copy new StreamEvent from existing StreamEvent.
     * @return StreamEvent  event found.
     */
    private StreamEvent findInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk,
                                                StreamEventCloner streamEventCloner) {
        candidateEventChunk.reset();
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = event.getStreamEvent(matchingEventPosition).getTimestamp() -
                        streamEvent.getTimestamp();
                if ((0 > timeDifference) || (timeDifference > withinTime)) {
                    break;
                }
            }
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }

    /**
     * Finds an event from a Collection of events.
     *
     * @param candidateEvents   Collection of events.
     * @param streamEventCloner StreamEventCloner to copy new StreamEvent from existing StreamEvent.
     * @return StreamEvent  event found.
     */
    protected StreamEvent findInCollection(Collection<StreamEvent> candidateEvents,
                                           StreamEventCloner streamEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }

    /**
     * Called when deleting an event chunk from event table.
     *
     * @param deletingEventChunk Event list for deletion.
     * @param candidateEvents    Collection / Map of candidate events.
     */
    @Override
    public void delete(ComplexEventChunk deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            ComplexEvent deletingEvent = deletingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(deletingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);

                if (candidateEvents instanceof ComplexEventChunk) {
                    deleteInComplexEventChunk((ComplexEventChunk) candidateEvents);
                } else if (candidateEvents instanceof Map) {
                    deleteInMap((Map) candidateEvents);
                } else if (candidateEvents instanceof Collection) {
                    deleteInCollection((Collection) candidateEvents);
                } else {
                    throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                            " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    /**
     * Deletes events from a Complex Event Chunk.
     *
     * @param candidateEventChunk Set of events as a ComplexEventChunk.
     */
    private void deleteInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                candidateEventChunk.remove();
            }
        }
    }

    /**
     * Deletes events from a Map of StreamEvent.
     *
     * @param candidateEvents Map of events.
     */
    public void deleteInMap(Map<Object, StreamEvent> candidateEvents) {
        for (Map.Entry<Object, StreamEvent> entry : candidateEvents.entrySet()) {
            StreamEvent streamEvent = entry.getValue();
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                candidateEvents.remove(entry.getKey());
            }
        }
    }

    /**
     * Deletes events from a Collection of StreamEvent.
     *
     * @param candidateEvents Collection of events.
     */
    private void deleteInCollection(Collection<StreamEvent> candidateEvents) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                candidateEvents.remove(streamEvent);
            }
        }
    }

    /**
     * Called when updating the event table entries.
     *
     * @param updatingEventChunk Event list that needs to be updated.
     * @param candidateEvents    Map of candidate events.
     * @param mappingPosition    Mapping positions array.
     */
    @Override
    public void update(ComplexEventChunk updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            ComplexEvent updatingEvent = updatingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(updatingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    updateInComplexEventChunk((ComplexEventChunk) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof List) {
                    updateInList((List) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof Map) {
                    updateInCollection(((Map) candidateEvents).values(), mappingPosition);
                } else if (candidateEvents instanceof Collection) {
                    updateInCollection((Collection) candidateEvents, mappingPosition);
                } else {
                    throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                            " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    /**
     * Called when updating list of events in a ComplexEventChunk.
     *
     * @param candidateEventChunk ComplexEventChunk of candidate events.
     * @param mappingPosition     Mapping positions array.
     */
    private void updateInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk, int[] mappingPosition) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
            }
        }
    }

    /**
     * Called when updating events in a Collection of stream events.
     *
     * @param candidateEvents Collection of candidate stream events.
     * @param mappingPosition Mapping positions array.
     */
    private void updateInCollection(Collection<StreamEvent> candidateEvents, int[] mappingPosition) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
            }
        }
    }

    /**
     * Called when updating events in a List of stream events.
     *
     * @param candidateEvents List of candidate stream events.
     * @param mappingPosition Mapping positions array.
     */
    private void updateInList(List<StreamEvent> candidateEvents, int[] mappingPosition) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                int streamEventIndex = candidateEvents.indexOf(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
                candidateEvents.set(streamEventIndex, streamEvent);
            }
        }
    }

    /**
     * Called when having "in" condition, to check the existence of the event.
     *
     * @param matchingEvent   Event that need to be check for existence.
     * @param candidateEvents Map of candidate events.
     * @return existence of the event.
     */
    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {
        try {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, (StreamEvent) matchingEvent);
            } else {
                this.event.setEvent((StateEvent) matchingEvent);
            }
            if (candidateEvents instanceof ComplexEventChunk) {
                return containsInComplexEventChunk((ComplexEventChunk) candidateEvents);
            } else if (candidateEvents instanceof Map) {
                return containsInCollection(((Map) candidateEvents).values());
            } else if (candidateEvents instanceof Collection) {
                return containsInCollection((Collection) candidateEvents);
            } else {
                throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                        " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        } finally {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, null);
            } else {
                this.event.setEvent(null);
            }
        }
    }

    /**
     * Check the existence of a particular event in a Collection of StreamEvents.
     *
     * @param candidateEvents Collection of candidate events.
     * @return existence of the event.
     */
    private boolean containsInCollection(Collection<StreamEvent> candidateEvents) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the existence of a particular event in a ComplexEventChunk.
     *
     * @param candidateEventChunk ComplexEventChunk of candidate events.
     * @return existence of the event.
     */
    private boolean containsInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void overwriteOrAdd(ComplexEventChunk overwritingOrAddingEventChunk,
                               Object candidateEvents, int[] mappingPosition) {
        overwritingOrAddingEventChunk.reset();
        while (overwritingOrAddingEventChunk.hasNext()) {
            ComplexEvent overwritingOrAddingEvent = overwritingOrAddingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(overwritingOrAddingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    overwriteOrAddInComplexEventChunk((ComplexEventChunk) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof List) {
                    overwriteOrAddInList((List) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof Map) {
                    overwriteOrAddInMap(((Map) candidateEvents), mappingPosition);
                } else if (candidateEvents instanceof Collection) {
                    overwriteOrAddInCollection((Collection) candidateEvents, mappingPosition);
                } else {
                    throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                            " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    private void overwriteOrAddInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk,
                                                   int[] mappingPosition) {
        candidateEventChunk.reset();
        boolean updated = false;
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
                updated = true;
            }
        }
        if (!updated) {
            StreamEvent insertEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertStreamEvent(matchingEvent, insertEvent);
            candidateEventChunk.add(insertEvent);
        }
    }

    private void overwriteOrAddInList(List<StreamEvent> candidateEvents, int[] mappingPosition) {
        boolean updated = false;
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                int streamEventIndex = candidateEvents.indexOf(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
                candidateEvents.set(streamEventIndex, streamEvent);
                updated = true;
            }
        }
        if (!updated) {
            StreamEvent insertEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertStreamEvent(matchingEvent, insertEvent);
            candidateEvents.add(insertEvent);
        }
    }

    private void overwriteOrAddInCollection(Collection<StreamEvent> candidateEvents, int[] mappingPosition) {
        boolean updated = false;
        for (StreamEvent streamEvent : candidateEvents) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
                updated = true;
            }
        }
        if (!updated) {
            StreamEvent insertEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertStreamEvent(matchingEvent, insertEvent);
            candidateEvents.add(insertEvent);
        }
    }

    private void overwriteOrAddInMap(Map candidateEvents, int[] mappingPosition) {
        boolean updated = false;
        for (StreamEvent streamEvent : (Collection<StreamEvent>) candidateEvents.values()) {
            if (outsideTimeWindow(streamEvent)) {
                break;
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i],
                            mappingPosition[i]);
                }
                updated = true;
            }
        }
        if (!updated) {
            StreamEvent insertEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertStreamEvent(matchingEvent, insertEvent);
            candidateEvents.put(insertEvent.getOutputData()[indexedPosition], insertEvent);
        }
    }

    protected class FinderStateEvent extends StateEvent {

        public FinderStateEvent(int size, int outputSize) {
            super(size, outputSize);
        }

        public void setEvent(StateEvent matchingStateEvent) {
            if (matchingStateEvent != null) {
                System.arraycopy(matchingStateEvent.getStreamEvents(), 0, streamEvents, 0,
                        matchingStateEvent.getStreamEvents().length);
            } else {
                for (int i = 0; i < streamEvents.length - 1; i++) {
                    streamEvents[i] = null;
                }
            }
        }
    }
}
