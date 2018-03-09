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

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

/**
 * For Siddhi extensions, extend this class to use the functionality of
 * AbstractStreamProcessor. This class processes only StreamEvents. Use
 * StreamFunctionProcessor to process StateEvents.
 */
public abstract class StreamProcessor extends AbstractStreamProcessor {

    //Introduced to maintain backward compatible
    protected boolean outputExpectsExpiredEvents;

    @Override
    protected void processEventChunk(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                                     StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        streamEventChunk.reset();
        process(streamEventChunk, nextProcessor, streamEventCloner, complexEventPopulater);
    }


    /**
     * The main processing method that will be called upon event arrival
     *
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    protected abstract void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                                    StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater);

    /**
     * The configure method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param configReader                 the config reader of StreamProcessor
     * @param siddhiAppContext         the context of the siddhi app
     * @param outputExpectsExpiredEvents   is output expects ExpiredEvents   @return the additional output attributes
     *                                     introduced by the function
     */
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                   SiddhiAppContext
                                           siddhiAppContext, boolean outputExpectsExpiredEvents) {
        this.outputExpectsExpiredEvents = outputExpectsExpiredEvents;
        return init(inputDefinition, attributeExpressionExecutors, configReader, siddhiAppContext);
    }

    protected abstract List<Attribute> init(AbstractDefinition inputDefinition,
                                            ExpressionExecutor[] attributeExpressionExecutors, ConfigReader
                                                    configReader, SiddhiAppContext
                                                    siddhiAppContext);

}
