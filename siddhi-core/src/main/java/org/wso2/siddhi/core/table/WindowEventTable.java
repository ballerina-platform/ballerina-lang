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

package org.wso2.siddhi.core.table;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.extension.holder.WindowProcessorExtensionHolder;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.extension.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Window implementation of SiddhiQL.
 * It can be seen as a static Window which can be referred in multiple queries.
 */
public class WindowEventTable implements FindableProcessor, Snapshotable {
    /**
     * Element id of this snapshot.
     */
    private final String elementId;

    /**
     * WindowDefinition used to construct this window.
     */
    private final WindowDefinition windowDefinition;

    /**
     * ExecutionPlanContext is used to create the elementId  and WindowProcessor.
     */
    private final ExecutionPlanContext executionPlanContext;

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
     * FindableProcessor reference for the this.windowProcessor.
     * If this.windowProcessor refers to EntryValveProcessor (if the Window is a scheduler based, it may be),
     * findableProcessor refers to the this.windowProcessor.getNextProcessor()
     */
    private FindableProcessor findableProcessor;

    /**
     * Lock to coordinate asynchronous events.
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * Construct a WindowEventTable object.
     *
     * @param windowDefinition     definition of the window
     * @param executionPlanContext execution plan context of Siddhi
     */
    public WindowEventTable(WindowDefinition windowDefinition, ExecutionPlanContext executionPlanContext) {
        this.windowDefinition = windowDefinition;
        this.executionPlanContext = executionPlanContext;
        this.elementId = executionPlanContext.getElementIdGenerator().createNewId();
    }

    /**
     * Initialize the WindowEvent table by creating {@link WindowProcessor} to handle the events.
     *
     * @param eventTableMap       map of {@link EventTable}s
     * @param windowEventTableMap map of WindowEventTables
     * @param latencyTracker      to rack the latency if statistic of underlying {@link WindowProcessor} is required
     */
    public void init(Map<String, EventTable> eventTableMap, Map<String, WindowEventTable> windowEventTableMap, LatencyTracker latencyTracker) {
        if (this.windowProcessor != null) {
            return;
        }

        // Create and initialize MetaStreamEvent
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(windowDefinition);
        metaStreamEvent.setWindowEvent(true);
        metaStreamEvent.initializeAfterWindowData();
        for (Attribute attribute : windowDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        StreamEventPool streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        StreamEventCloner streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
        OutputStream.OutputEventType outputEventType = windowDefinition.getOutputEventType();
        boolean outputExpectsExpiredEvents = outputEventType != OutputStream.OutputEventType.CURRENT_EVENTS;

        WindowProcessor currentProcessor = WindowEventTable.createWindowProcessor(windowDefinition.getWindow(), metaStreamEvent, new ArrayList<VariableExpressionExecutor>(), this.executionPlanContext, eventTableMap, outputExpectsExpiredEvents);
        currentProcessor.setStreamEventCloner(streamEventCloner);
        currentProcessor.constructStreamEventPopulater(metaStreamEvent, 0);

        EntryValveProcessor entryValveProcessor = null;
        if (currentProcessor instanceof SchedulingProcessor) {
            entryValveProcessor = new EntryValveProcessor(this.executionPlanContext);
            Scheduler scheduler = new Scheduler(this.executionPlanContext.getScheduledExecutorService(), entryValveProcessor, this.executionPlanContext);
            scheduler.init(this.reentrantLock);
            scheduler.setStreamEventPool(streamEventPool);
            ((SchedulingProcessor) currentProcessor).setScheduler(scheduler);
        }
        if (entryValveProcessor != null) {
            entryValveProcessor.setToLast(currentProcessor);
            this.windowProcessor = entryValveProcessor;
        } else {
            this.windowProcessor = currentProcessor;
        }

        // PublishProcessor must be the last in chain so that it can publish the events to StreamJunction
        this.windowProcessor.setToLast(new PublisherProcessor(outputEventType));

        // Caste the processor to FindableProcessor
        if (windowProcessor instanceof FindableProcessor) {
            this.findableProcessor = (FindableProcessor) windowProcessor;
        } else if (windowProcessor instanceof EntryValveProcessor && windowProcessor.getNextProcessor() instanceof FindableProcessor) {
            this.findableProcessor = (FindableProcessor) windowProcessor.getNextProcessor();
        }
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
     * Return the {@link WindowDefinition} used to construct this WindowEventTable.
     *
     * @return the window definition
     */
    public WindowDefinition getWindowDefinition() {
        return this.windowDefinition;
    }

    /**
     * Add the given ComplexEventChunk to the WindowEventTable.
     *
     * @param complexEventChunk the event chunk to be added
     */
    public void add(ComplexEventChunk complexEventChunk) {
        try {
            this.reentrantLock.lock();
            // Send to the window windowProcessor
            windowProcessor.process(complexEventChunk);
        } finally {
            this.reentrantLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamEvent find(StateEvent matchingEvent, Finder finder) {
        if (this.findableProcessor == null) {
            throw new OperationNotSupportedException("Cannot find in window " + this.windowDefinition.getWindow());
        }
//        try {
//            this.reentrantLock.lock();
            return this.findableProcessor.find(matchingEvent, finder);
//        } finally {
//            this.reentrantLock.unlock();
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        if (this.findableProcessor == null) {
            throw new OperationNotSupportedException("Cannot find in window " + this.windowDefinition.getWindow());
        }
        return this.findableProcessor.constructFinder(expression, matchingMetaStateHolder, executionPlanContext, variableExpressionExecutors, eventTableMap);
    }


    /**
     * Return an object array containing the processor which keeps all the events.
     *
     * @return current state of the WindowEventTable
     */
    @Override
    public Object[] currentState() {
        return new Object[]{windowProcessor};
    }

    /**
     * Restore the windowProcessor using given state.
     *
     * @param state the stateful objects of the element as an array on
     */
    @Override
    public void restoreState(Object[] state) {
        this.windowProcessor = (Processor) state[0];
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
     * A utility method to create a {@link WindowProcessor} for the window of WindowEventTable.
     *
     * @param window                      the window for which a processor is required
     * @param metaEvent                   meta information of the WindowEventTable
     * @param variableExpressionExecutors a list used to return any variables used in the window
     * @param executionPlanContext        execution plan context of Siddhi
     * @param eventTableMap               Map of {@link EventTable}s used ti initialize the {@link WindowProcessor}
     * @param outputExpectsExpiredEvents  whether the {@link WindowProcessor} should return expired events or not
     * @return windowProcessor for the given window
     */
    private static WindowProcessor createWindowProcessor(Window window, MetaComplexEvent metaEvent, List<VariableExpressionExecutor> variableExpressionExecutors, ExecutionPlanContext executionPlanContext, Map<String, EventTable> eventTableMap, boolean outputExpectsExpiredEvents) {
        Expression[] parameters = window.getParameters();
        MetaStreamEvent metaStreamEvent;
        int stateIndex = SiddhiConstants.UNKNOWN_STATE;
        if (metaEvent instanceof MetaStateEvent) {
            stateIndex = ((MetaStateEvent) metaEvent).getStreamEventCount() - 1;
            metaStreamEvent = ((MetaStateEvent) metaEvent).getMetaStreamEvent(stateIndex);
        } else {
            metaStreamEvent = (MetaStreamEvent) metaEvent;
        }

        ExpressionExecutor[] attributeExpressionExecutors;
        if (parameters != null) {
            if (parameters.length > 0) {
                attributeExpressionExecutors = new ExpressionExecutor[parameters.length];
                for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                    attributeExpressionExecutors[i] = ExpressionParser.parseExpression(parameters[i], metaEvent, stateIndex, eventTableMap, variableExpressionExecutors,
                            executionPlanContext, false, SiddhiConstants.CURRENT);
                }
            } else {
                List<Attribute> attributeList = metaStreamEvent.getLastInputDefinition().getAttributeList();
                int parameterSize = attributeList.size();
                attributeExpressionExecutors = new ExpressionExecutor[parameterSize];
                for (int i = 0; i < parameterSize; i++) {
                    attributeExpressionExecutors[i] = ExpressionParser.parseExpression(new Variable(attributeList.get(i).getName()), metaEvent, stateIndex, eventTableMap, variableExpressionExecutors,
                            executionPlanContext, false, SiddhiConstants.CURRENT);
                }
            }
        } else {
            attributeExpressionExecutors = new ExpressionExecutor[0];
        }
        WindowProcessor windowProcessor;
        if (window instanceof Extension) {
            windowProcessor = (WindowProcessor) SiddhiClassLoader.loadExtensionImplementation((Extension) window,
                    WindowProcessorExtensionHolder.getInstance(executionPlanContext));
        } else {
            windowProcessor = (WindowProcessor) SiddhiClassLoader.loadSiddhiImplementation(window.getFunction(),
                    WindowProcessor.class);
        }
        windowProcessor.initProcessor(metaStreamEvent.getLastInputDefinition(), attributeExpressionExecutors, executionPlanContext, outputExpectsExpiredEvents);
        return windowProcessor;
    }

    /**
     * PublisherProcessor receives events from the last window processor of WindowEventTable,
     * filter them depending on user defined output type and publish them to the stream junction.
     */
    private class PublisherProcessor implements Processor {
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


        public PublisherProcessor(OutputStream.OutputEventType outputEventType) {
            this.outputEventType = outputEventType;
            this.allowCurrentEvents = (outputEventType == OutputStream.OutputEventType.CURRENT_EVENTS || outputEventType == OutputStream.OutputEventType.ALL_EVENTS);
            this.allowExpiredEvents = (outputEventType == OutputStream.OutputEventType.EXPIRED_EVENTS || outputEventType == OutputStream.OutputEventType.ALL_EVENTS);
        }

        public void process(ComplexEventChunk complexEventChunk) {
            // Filter the events depending on user defined output type.
            // if(allowCurrentEvents && allowExpiredEvents)
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (((event.getType() != StreamEvent.Type.CURRENT || !allowCurrentEvents) && (event.getType() != StreamEvent.Type.EXPIRED || !allowExpiredEvents))) {
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
            return new PublisherProcessor(this.outputEventType);
        }
    }
}
