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
package org.wso2.siddhi.core.query.processor.stream;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

public abstract class AbstractStreamProcessor implements Processor, EternalReferencedHolder, Snapshotable {

    protected static final Logger log = Logger.getLogger(AbstractStreamProcessor.class);

    protected Processor nextProcessor;

    protected List<Attribute> additionalAttributes;
    protected StreamEventCloner streamEventCloner;
    protected AbstractDefinition inputDefinition;
    protected ExpressionExecutor[] attributeExpressionExecutors;
    protected ExecutionPlanContext executionPlanContext;
    protected int attributeExpressionLength;
    protected ComplexEventPopulater complexEventPopulater;
    protected String elementId = null;
    private boolean outputExpectsExpiredEvents;

    public AbstractDefinition initProcessor(AbstractDefinition inputDefinition,
                                            ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext, boolean outputExpectsExpiredEvents) {
        this.outputExpectsExpiredEvents = outputExpectsExpiredEvents;
        try {
            this.inputDefinition = inputDefinition;
            this.attributeExpressionExecutors = attributeExpressionExecutors;
            this.executionPlanContext = executionPlanContext;
            this.attributeExpressionLength = attributeExpressionExecutors.length;
            if (elementId == null) {
                elementId = executionPlanContext.getElementIdGenerator().createNewId();
            }
            executionPlanContext.getSnapshotService().addSnapshotable(this);
            this.additionalAttributes = init(inputDefinition, attributeExpressionExecutors, executionPlanContext, outputExpectsExpiredEvents);

            executionPlanContext.addEternalReferencedHolder(this);

            StreamDefinition outputDefinition = StreamDefinition.id(inputDefinition.getId());
            for (Attribute attribute : inputDefinition.getAttributeList()) {
                outputDefinition.attribute(attribute.getName(), attribute.getType());
            }
            for (Attribute attribute : additionalAttributes) {
                outputDefinition.attribute(attribute.getName(), attribute.getType());
            }

            return outputDefinition;
        } catch (Throwable t) {
            throw new ExecutionPlanCreationException(t);
        }
    }

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     * @param outputExpectsExpiredEvents
     * @return the additional output attributes introduced by the function
     */
    protected abstract List<Attribute> init(AbstractDefinition inputDefinition,
                                            ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext, boolean outputExpectsExpiredEvents);

    public void process(ComplexEventChunk streamEventChunk) {
        streamEventChunk.reset();
        try {
            processEventChunk(streamEventChunk, nextProcessor, streamEventCloner, complexEventPopulater);
        } catch (RuntimeException e) {
            log.error("Dropping event chunk " + streamEventChunk + ", error in processing " + this.getClass()
                    .getCanonicalName() + ", " + e.getMessage(), e);
        }
    }

    /**
     * The main processing method that will be called upon event arrival
     *
     * @param streamEventChunk    the event chunk that need to be processed
     * @param nextProcessor        the next processor to which the success events need to be passed
     * @param streamEventCloner    helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    protected abstract void processEventChunk(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                                              StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater);

    public void setNextProcessor(Processor processor) {
        this.nextProcessor = processor;
    }

    public Processor getNextProcessor() {
        return nextProcessor;
    }

    @Override
    public Processor cloneProcessor(String key) {
        try {
            AbstractStreamProcessor abstractStreamProcessor = this.getClass().newInstance();
            abstractStreamProcessor.inputDefinition = inputDefinition;
            ExpressionExecutor[] innerExpressionExecutors = new ExpressionExecutor[attributeExpressionLength];
            ExpressionExecutor[] attributeExpressionExecutors1 = this.attributeExpressionExecutors;
            for (int i = 0; i < attributeExpressionLength; i++) {
                innerExpressionExecutors[i] = attributeExpressionExecutors1[i].cloneExecutor(key);
            }
            abstractStreamProcessor.attributeExpressionExecutors = innerExpressionExecutors;
            abstractStreamProcessor.attributeExpressionLength = attributeExpressionLength;
            abstractStreamProcessor.additionalAttributes = additionalAttributes;
            abstractStreamProcessor.complexEventPopulater = complexEventPopulater;
            abstractStreamProcessor.executionPlanContext = executionPlanContext;
            abstractStreamProcessor.elementId = elementId + "-" + key;
            abstractStreamProcessor.init(inputDefinition, attributeExpressionExecutors, executionPlanContext, outputExpectsExpiredEvents);
            abstractStreamProcessor.start();
            return abstractStreamProcessor;

        } catch (Exception e) {
            throw new ExecutionPlanRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
    }

    public void constructStreamEventPopulater(MetaStreamEvent metaStreamEvent, int streamEventChainIndex) {
        if (this.complexEventPopulater == null) {
            this.complexEventPopulater = StreamEventPopulaterFactory.constructEventPopulator(metaStreamEvent,
                    streamEventChainIndex, additionalAttributes);
        }
    }

    public void setStreamEventCloner(StreamEventCloner streamEventCloner) {
        this.streamEventCloner = streamEventCloner;
    }

    public void setToLast(Processor processor) {
        if (nextProcessor == null) {
            this.nextProcessor = processor;
        } else {
            this.nextProcessor.setToLast(processor);
        }
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
