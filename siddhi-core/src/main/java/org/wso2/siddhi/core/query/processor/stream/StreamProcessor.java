/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.processor.stream;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulater;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

public abstract class StreamProcessor implements Processor {

    protected static final Logger log = Logger.getLogger(StreamProcessor.class);

    protected Processor nextProcessor;
    protected List<Attribute> additionalAttributes;
    protected StreamEventPopulater streamEventPopulater;
    protected StreamEventCloner streamEventCloner;
    protected ExpressionExecutor[] inputExecutors;

    public AbstractDefinition initProcessor(AbstractDefinition inputDefinition, ExpressionExecutor[] inputExecutors) {
        this.inputExecutors = inputExecutors;
        additionalAttributes = init(inputDefinition, inputExecutors);

        StreamDefinition outputDefinition = new StreamDefinition(inputDefinition.getId());
        for (Attribute attribute : inputDefinition.getAttributeList()) {
            outputDefinition.attribute(attribute.getName(), attribute.getType());
        }
        for (Attribute attribute : additionalAttributes) {
            outputDefinition.attribute(attribute.getName(), attribute.getType());
        }

        return outputDefinition;
    }

    protected abstract List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] inputExecutors);

    public void process(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        try {
            process(complexEventChunk, nextProcessor, streamEventCloner, streamEventPopulater);
        } catch (Throwable t) {    //todo improve
            log.error(t.getMessage(), t);
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

    public Processor cloneProcessor() {
        StreamProcessor streamProcessor = cloneStreamProcessor();
        streamProcessor.additionalAttributes = additionalAttributes;
        streamProcessor.streamEventPopulater = streamEventPopulater;
        streamProcessor.inputExecutors = inputExecutors;
        return streamProcessor;
    }

    protected abstract StreamProcessor cloneStreamProcessor();

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
}
