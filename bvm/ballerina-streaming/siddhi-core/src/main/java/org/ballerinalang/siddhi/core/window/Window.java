/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.siddhi.core.window;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.ballerinalang.siddhi.core.exception.OperationNotSupportedException;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.ballerinalang.siddhi.core.query.processor.Processor;
import org.ballerinalang.siddhi.core.query.processor.SchedulingProcessor;
import org.ballerinalang.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.ballerinalang.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.ballerinalang.siddhi.core.stream.StreamJunction;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.Scheduler;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.lock.LockWrapper;
import org.ballerinalang.siddhi.core.util.parser.SchedulerParser;
import org.ballerinalang.siddhi.core.util.parser.SingleInputStreamParser;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.snapshot.Snapshotable;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.MemoryCalculable;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.WindowDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.ballerinalang.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Window implementation of SiddhiQL.
 * It can be seen as a global Window which can be accessed from multiple queries.
 */
public class Window implements FindableProcessor, Snapshotable, MemoryCalculable {
    /**
     * Element id of this snapshot.
     */
    private final String elementId;

    /**
     * WindowDefinition used to construct this window.
     */
    private final WindowDefinition windowDefinition;

    /**
     * SiddhiAppContext is used to create the elementId  and WindowProcessor.
     */
    private final SiddhiAppContext siddhiAppContext;
    /**
     * LockWrapper to coordinate asynchronous events.
     */
    private final LockWrapper lockWrapper;
    /**
     * TemplateBuilder to convert {@link StateEvent}s to {@link StreamEvent}s.
     */
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    /**
     * Publisher to which the output events from internal window have to be sent.
     */
    private StreamJunction.Publisher outputPublisher;
    /**
     * Processor for the internal window.
     * It will contain the PublisherProcessor as the last windowProcessor in the chain.
     */
    private Processor windowProcessor;
    /**
     * WindowProcessor reference to the actual processor which is holding the events.
     * If this.windowProcessor refers to EntryValveProcessor (if the Window is a scheduler based, it may be),
     * internalWindowProcessor refers to the this.windowProcessor.getNextProcessor()
     */
    private WindowProcessor internalWindowProcessor;
    /**
     * StreamEventPool to create new empty StreamEvent.
     */
    private StreamEventPool streamEventPool;

    /**
     * window operation latency and throughput trackers.
     */
    private LatencyTracker latencyTrackerInsert;
    private LatencyTracker latencyTrackerFind;
    private ThroughputTracker throughputTrackerFind;
    private ThroughputTracker throughputTrackerInsert;


    /**
     * Construct a Window object.
     *
     * @param windowDefinition definition of the window
     * @param siddhiAppContext siddhi app context of Siddhi
     */
    public Window(WindowDefinition windowDefinition, SiddhiAppContext siddhiAppContext) {
        this.windowDefinition = windowDefinition;
        this.siddhiAppContext = siddhiAppContext;
        this.elementId = siddhiAppContext.getElementIdGenerator().createNewId();
        this.lockWrapper = new LockWrapper(windowDefinition.getId());
        this.lockWrapper.setLock(new ReentrantLock());
        if (siddhiAppContext.getStatisticsManager() != null) {
            latencyTrackerFind = QueryParserHelper.createLatencyTracker(siddhiAppContext, windowDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_FIND);
            latencyTrackerInsert = QueryParserHelper.createLatencyTracker(siddhiAppContext, windowDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_INSERT);

            throughputTrackerFind = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    windowDefinition.getId(), SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_FIND);
            throughputTrackerInsert = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    windowDefinition.getId(), SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_INSERT);
        }
    }

    /**
     * Initialize the WindowEvent table by creating {@link WindowProcessor} to handle the events.
     *
     * @param tableMap       map of {@link Table}s
     * @param eventWindowMap map of EventWindows
     * @param queryName      name of the query window belongs to.
     */
    public void init(Map<String, Table> tableMap, Map<String, Window> eventWindowMap,
                     String queryName) {
        if (this.windowProcessor != null) {
            return;
        }

        // Create and initialize MetaStreamEvent
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(windowDefinition);
        metaStreamEvent.setEventType(MetaStreamEvent.EventType.WINDOW);
        metaStreamEvent.initializeAfterWindowData();
        for (Attribute attribute : windowDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        this.streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        StreamEventCloner streamEventCloner = new StreamEventCloner(metaStreamEvent, this.streamEventPool);
        OutputStream.OutputEventType outputEventType = windowDefinition.getOutputEventType();
        boolean outputExpectsExpiredEvents = outputEventType != OutputStream.OutputEventType.CURRENT_EVENTS;

        WindowProcessor internalWindowProcessor = (WindowProcessor) SingleInputStreamParser.generateProcessor
                (windowDefinition.getWindow(), metaStreamEvent, new ArrayList<VariableExpressionExecutor>(), this
                        .siddhiAppContext, tableMap, false, outputExpectsExpiredEvents, queryName);
        internalWindowProcessor.setStreamEventCloner(streamEventCloner);
        internalWindowProcessor.constructStreamEventPopulater(metaStreamEvent, 0);

        EntryValveProcessor entryValveProcessor = null;
        if (internalWindowProcessor instanceof SchedulingProcessor) {
            entryValveProcessor = new EntryValveProcessor(this.siddhiAppContext);
            Scheduler scheduler = SchedulerParser.parse(this.siddhiAppContext.getScheduledExecutorService(),
                    entryValveProcessor, this.siddhiAppContext);
            scheduler.init(this.lockWrapper, queryName);
            scheduler.setStreamEventPool(streamEventPool);
            ((SchedulingProcessor) internalWindowProcessor).setScheduler(scheduler);
        }
        if (entryValveProcessor != null) {
            entryValveProcessor.setToLast(internalWindowProcessor);
            this.windowProcessor = entryValveProcessor;
        } else {
            this.windowProcessor = internalWindowProcessor;
        }

        // StreamPublishProcessor must be the last in chain so that it can publish the events to StreamJunction
        this.windowProcessor.setToLast(new StreamPublishProcessor(outputEventType));
        this.internalWindowProcessor = internalWindowProcessor;
    }


    /**
     * Set Publisher to which the the output events from internal window have to be sent.
     *
     * @param publisher output publisher
     */
    public void setPublisher(StreamJunction.Publisher publisher) {
        this.outputPublisher = publisher;
    }

    /**
     * Return the {@link WindowDefinition} used to construct this Window.
     *
     * @return the window definition
     */
    public WindowDefinition getWindowDefinition() {
        return this.windowDefinition;
    }

    /**
     * Add the given ComplexEventChunk to the Window.
     *
     * @param complexEventChunk the event chunk to be added
     */
    public void add(ComplexEventChunk complexEventChunk) {
        try {
            this.lockWrapper.lock();
            complexEventChunk.reset();

            // Convert all events to StreamEvent because StateEvents can be passed if directly received from a join
            ComplexEvent complexEvents = complexEventChunk.getFirst();
            StreamEvent firstEvent = streamEventPool.borrowEvent();
            eventConverter.convertComplexEvent(complexEvents, firstEvent);
            StreamEvent currentEvent = firstEvent;
            complexEvents = complexEvents.getNext();
            int numberOfEvents = 0;
            while (complexEvents != null) {
                numberOfEvents++;
                StreamEvent nextEvent = streamEventPool.borrowEvent();
                eventConverter.convertComplexEvent(complexEvents, nextEvent);
                currentEvent.setNext(nextEvent);
                currentEvent = nextEvent;
                complexEvents = complexEvents.getNext();
            }

            try {
                if (throughputTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerInsert.eventsIn(numberOfEvents);
                    latencyTrackerInsert.markIn();
                }
                // Send to the window windowProcessor
                windowProcessor.process(new ComplexEventChunk<StreamEvent>(firstEvent, currentEvent,
                        complexEventChunk.isBatch()));
            } finally {
                if (throughputTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerInsert.markOut();
                }
            }
        } finally {
            this.lockWrapper.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        try {
            if (throughputTrackerFind != null && siddhiAppContext.isStatsEnabled()) {
                throughputTrackerFind.eventIn();
                latencyTrackerFind.markIn();
            }
            return ((FindableProcessor) this.internalWindowProcessor).find(matchingEvent, compiledCondition);
        } finally {
            if (throughputTrackerFind != null && siddhiAppContext.isStatsEnabled()) {
                latencyTrackerFind.markOut();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompiledCondition compileCondition(Expression condition, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        if (this.internalWindowProcessor instanceof FindableProcessor) {
            return ((FindableProcessor) this.internalWindowProcessor).compileCondition(condition,
                    matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap,
                    queryName);
        } else {
            throw new OperationNotSupportedException("Cannot construct finder for the window " + this
                    .windowDefinition.getWindow());
        }

    }

    public LockWrapper getLock() {
        return lockWrapper;
    }


    /**
     * Return an object array containing the internal state of the internalWindowProcessor.
     *
     * @return current state of the Window
     */
    @Override
    public Map<String, Object> currentState() {
        return this.internalWindowProcessor.currentState();
    }

    /**
     * Restore the internalWindowProcessor using given state.
     *
     * @param state the stateful objects of the element as an array on
     */
    @Override
    public void restoreState(Map<String, Object> state) {
        this.internalWindowProcessor.restoreState(state);
    }


    /**
     * Return the elementId which may be used for snapshot creation.
     *
     * @return the element id of this {@link Snapshotable} object
     */
    @Override
    public String getElementId() {
        return this.elementId;
    }

    /**
     * PublisherProcessor receives events from the last window processor of Window,
     * filter them depending on user defined output type and publish them to the stream junction.
     */
    private class StreamPublishProcessor implements Processor {
        /**
         * Allow current events.
         */
        private final boolean allowCurrentEvents;

        /**
         * Allow expired events.
         */
        private final boolean allowExpiredEvents;

        /**
         * User preference of output event type. Stored for cloning purpose.
         */
        private final OutputStream.OutputEventType outputEventType;


        StreamPublishProcessor(OutputStream.OutputEventType outputEventType) {
            this.outputEventType = outputEventType;
            this.allowCurrentEvents = (outputEventType == OutputStream.OutputEventType.CURRENT_EVENTS ||
                    outputEventType == OutputStream.OutputEventType.ALL_EVENTS);
            this.allowExpiredEvents = (outputEventType == OutputStream.OutputEventType.EXPIRED_EVENTS ||
                    outputEventType == OutputStream.OutputEventType.ALL_EVENTS);
        }

        public void process(ComplexEventChunk complexEventChunk) {
            if (throughputTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                latencyTrackerInsert.markOut();
            }
            // Filter the events depending on user defined output type.
            // if(allowCurrentEvents && allowExpiredEvents)
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (((event.getType() != StreamEvent.Type.CURRENT || !allowCurrentEvents) && (event.getType() !=
                        StreamEvent.Type.EXPIRED || !allowExpiredEvents))) {
                    complexEventChunk.remove();
                }
            }
            complexEventChunk.reset();
            if (complexEventChunk.hasNext()) {
                // Publish the events
                outputPublisher.send(complexEventChunk.getFirst());
            }
        }

        public Processor getNextProcessor() {
            return null;
        }


        public void setNextProcessor(Processor processor) {
            // Do nothing
        }


        public void setToLast(Processor processor) {
            // Do nothing
        }


        public Processor cloneProcessor(String key) {
            return new StreamPublishProcessor(this.outputEventType);
        }
    }
}
