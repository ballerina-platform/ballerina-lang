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
package org.wso2.siddhi.core.query.processor.stream;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

/**
 * Abstract implementation of {@link Processor} intended to be used by any Stream Processors.
 */
public abstract class AbstractStreamProcessor implements Processor, EternalReferencedHolder, Snapshotable {

    private static final Logger log = Logger.getLogger(AbstractStreamProcessor.class);

    protected Processor nextProcessor;

    protected List<Attribute> additionalAttributes;
    protected StreamEventCloner streamEventCloner;
    protected AbstractDefinition inputDefinition;
    protected ExpressionExecutor[] attributeExpressionExecutors;
    protected SiddhiAppContext siddhiAppContext;
    protected int attributeExpressionLength;
    protected ComplexEventPopulater complexEventPopulater;
    protected String elementId = null;
    private ConfigReader configReader;
    protected String queryName;
    private boolean outputExpectsExpiredEvents;

    public AbstractDefinition initProcessor(AbstractDefinition inputDefinition,
                                            ExpressionExecutor[] attributeExpressionExecutors,
                                            ConfigReader configReader, SiddhiAppContext
                                                    siddhiAppContext, boolean outputExpectsExpiredEvents,
                                            String queryName) {
        this.configReader = configReader;
        this.outputExpectsExpiredEvents = outputExpectsExpiredEvents;
        try {
            this.inputDefinition = inputDefinition;
            this.attributeExpressionExecutors = attributeExpressionExecutors;
            this.siddhiAppContext = siddhiAppContext;
            this.attributeExpressionLength = attributeExpressionExecutors.length;
            this.queryName = queryName;
            if (elementId == null) {
                elementId = "AbstractStreamProcessor-" + siddhiAppContext.getElementIdGenerator().createNewId();
            }
            siddhiAppContext.getSnapshotService().addSnapshotable(queryName, this);
            this.additionalAttributes = init(inputDefinition, attributeExpressionExecutors, configReader,
                                             siddhiAppContext,
                                             outputExpectsExpiredEvents);

            siddhiAppContext.addEternalReferencedHolder(this);

            StreamDefinition outputDefinition = StreamDefinition.id(inputDefinition.getId());
            for (Attribute attribute : inputDefinition.getAttributeList()) {
                outputDefinition.attribute(attribute.getName(), attribute.getType());
            }
            for (Attribute attribute : additionalAttributes) {
                outputDefinition.attribute(attribute.getName(), attribute.getType());
            }

            return outputDefinition;
        } catch (Throwable t) {
            throw new SiddhiAppCreationException(t);
        }
    }

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param configReader this hold the {@link AbstractStreamProcessor} extensions configuration reader.
     * @param siddhiAppContext         the context of the siddhi app
     * @param outputExpectsExpiredEvents   is output expects ExpiredEvents   @return the additional output attributes
     *                                     introduced by the function
     * @return list of attributes.
     */
    protected abstract List<Attribute> init(AbstractDefinition inputDefinition,
                                            ExpressionExecutor[] attributeExpressionExecutors, ConfigReader
                                                    configReader, SiddhiAppContext
                                                    siddhiAppContext, boolean outputExpectsExpiredEvents);

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
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    protected abstract void processEventChunk(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                                              StreamEventCloner streamEventCloner, ComplexEventPopulater
                                                      complexEventPopulater);

    public Processor getNextProcessor() {
        return nextProcessor;
    }

    public void setNextProcessor(Processor processor) {
        this.nextProcessor = processor;
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
            abstractStreamProcessor.siddhiAppContext = siddhiAppContext;
            abstractStreamProcessor.elementId = elementId + "-" + key;
            abstractStreamProcessor.init(inputDefinition, attributeExpressionExecutors, configReader,
                                         siddhiAppContext,
                                         outputExpectsExpiredEvents);
            abstractStreamProcessor.start();
            return abstractStreamProcessor;

        } catch (Exception e) {
            throw new SiddhiAppRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
    }

    public void constructStreamEventPopulater(MetaStreamEvent metaStreamEvent, int streamEventChainIndex) {
        if (this.complexEventPopulater == null) {
            this.complexEventPopulater = StreamEventPopulaterFactory.constructEventPopulator(metaStreamEvent,
                                                                                             streamEventChainIndex,
                                                                                             additionalAttributes);
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
