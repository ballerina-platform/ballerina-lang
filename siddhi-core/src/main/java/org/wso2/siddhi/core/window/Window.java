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

package org.wso2.siddhi.core.window;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.SchedulerParser;
import org.wso2.siddhi.core.util.parser.SingleInputStreamParser;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Window implementation of SiddhiQL.
 * It can be seen as a global Window which can be accessed from multiple queries.
 */
public class Window implements FindableProcessor, Snapshotable {
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
     * TemplateBuilder to convert {@link StateEvent}s to {@link StreamEvent}s
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
     * Construct a Window object.
     *
     * @param windowDefinition     definition of the window
     * @param siddhiAppContext siddhi app context of Siddhi
     */
    public Window(WindowDefinition windowDefinition, SiddhiAppContext siddhiAppContext) {
        this.windowDefinition = windowDefinition;
        this.siddhiAppContext = siddhiAppContext;
        this.elementId = siddhiAppContext.getElementIdGenerator().createNewId();
        this.lockWrapper = new LockWrapper(windowDefinition.getId());
        this.lockWrapper.setLock(new ReentrantLock());
    }

    /**
     * Initialize the WindowEvent table by creating {@link WindowProcessor} to handle the events.
     *
     * @param tableMap  map of {@link Table}s
     * @param eventWindowMap map of EventWindows
     * @param latencyTracker to rack the latency if statistic of underlying {@link WindowProcessor} is required
     * @param queryName name of the query window belongs to.
     */
    public void init(Map<String, Table> tableMap, Map<String, Window> eventWindowMap, LatencyTracker
            latencyTracker, String queryName) {
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
            while (complexEvents != null) {
                StreamEvent nextEvent = streamEventPool.borrowEvent();
                eventConverter.convertComplexEvent(complexEvents, nextEvent);
                currentEvent.setNext(nextEvent);
                currentEvent = nextEvent;
                complexEvents = complexEvents.getNext();
            }

            // Send to the window windowProcessor
            windowProcessor.process(new ComplexEventChunk<StreamEvent>(firstEvent, currentEvent, complexEventChunk
                    .isBatch()));
        } finally {
            this.lockWrapper.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((FindableProcessor) this.internalWindowProcessor).find(matchingEvent, compiledCondition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompiledCondition compileCondition(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        if (this.internalWindowProcessor instanceof FindableProcessor) {
            return ((FindableProcessor) this.internalWindowProcessor).compileCondition(expression,
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


        public StreamPublishProcessor(OutputStream.OutputEventType outputEventType) {
            this.outputEventType = outputEventType;
            this.allowCurrentEvents = (outputEventType == OutputStream.OutputEventType.CURRENT_EVENTS ||
                    outputEventType == OutputStream.OutputEventType.ALL_EVENTS);
            this.allowExpiredEvents = (outputEventType == OutputStream.OutputEventType.EXPIRED_EVENTS ||
                    outputEventType == OutputStream.OutputEventType.ALL_EVENTS);
        }

        public void process(ComplexEventChunk complexEventChunk) {
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
