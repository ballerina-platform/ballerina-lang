/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.processor.stream;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulater;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

public abstract class StreamProcessor implements Processor, EternalReferencedHolder, Snapshotable {

    protected static final Logger log = Logger.getLogger(StreamProcessor.class);

    protected Processor nextProcessor;

    protected List<Attribute> additionalAttributes;
    protected StreamEventCloner streamEventCloner;
    protected AbstractDefinition inputDefinition;
    protected ExpressionExecutor[] attributeExpressionExecutors;
    protected ExecutionPlanContext executionPlanContext;
    protected int attributeExpressionLength;
    protected StreamEventPopulater streamEventPopulater;
    protected String elementId= null;

    public AbstractDefinition initProcessor(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.inputDefinition = inputDefinition;
        this.attributeExpressionExecutors = attributeExpressionExecutors;
        this.executionPlanContext = executionPlanContext;
        this.attributeExpressionLength = attributeExpressionExecutors.length;
        if(elementId==null) {
            elementId=executionPlanContext.getElementIdGenerator().createNewId();
        }
        executionPlanContext.getSnapshotService().addSnapshotable(this);
        this.additionalAttributes = init(inputDefinition, attributeExpressionExecutors, executionPlanContext);

        executionPlanContext.addEternalReferencedHolder(this);

        StreamDefinition outputDefinition = StreamDefinition.id(inputDefinition.getId());
        for (Attribute attribute : inputDefinition.getAttributeList()) {
            outputDefinition.attribute(attribute.getName(), attribute.getType());
        }
        for (Attribute attribute : additionalAttributes) {
            outputDefinition.attribute(attribute.getName(), attribute.getType());
        }

        return outputDefinition;
    }

    /**
     * The init method of the StreamFunction
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors for the function parameters
     * @param executionPlanContext         the execution plan context
     * @return the additional output attributes introduced by the function
     */
    protected abstract List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext);

    public void process(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        try {
            process(complexEventChunk, nextProcessor, streamEventCloner, streamEventPopulater);
        } catch (RuntimeException e) {
            log.error("Dropping event chunk " + complexEventChunk + ", error in processing " + this.getClass().getCanonicalName() + ", " + e.getMessage(), e);
        }
    }

    protected abstract void process(ComplexEventChunk complexEventChunk, Processor nextProcessor,
                                    StreamEventCloner streamEventCloner, StreamEventPopulater streamEventPopulater);

    public void setNextProcessor(Processor processor) {
        this.nextProcessor = processor;
    }

    public Processor getNextProcessor() {
        return nextProcessor;
    }

    @Override
    public Processor cloneProcessor(String key) {
        try {
            StreamProcessor streamProcessor = this.getClass().newInstance();
            streamProcessor.inputDefinition = inputDefinition;
            ExpressionExecutor[] innerExpressionExecutors = new ExpressionExecutor[attributeExpressionLength];
            ExpressionExecutor[] attributeExpressionExecutors1 = this.attributeExpressionExecutors;
            for (int i = 0; i < attributeExpressionLength; i++) {
                innerExpressionExecutors[i] = attributeExpressionExecutors1[i].cloneExecutor(key);
            }
            streamProcessor.attributeExpressionExecutors = innerExpressionExecutors;
            streamProcessor.attributeExpressionLength = attributeExpressionLength;
            streamProcessor.additionalAttributes = additionalAttributes;
            streamProcessor.streamEventPopulater = streamEventPopulater;
            streamProcessor.elementId = elementId + "-" + key;
            streamProcessor.init(inputDefinition, attributeExpressionExecutors, executionPlanContext);
            streamProcessor.start();
            return streamProcessor;

        } catch (Exception e) {
            throw new ExecutionPlanRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
    }

    public void constructStreamEventPopulater(MetaStreamEvent metaStreamEvent, int streamEventChainIndex) {
        if (this.streamEventPopulater == null) {
            this.streamEventPopulater = StreamEventPopulaterFactory.constructEventPopulator(metaStreamEvent,
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
