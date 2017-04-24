/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.output.sink.distributed;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.stream.output.sink.OutputTransport;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

/**
 * This is the base class for Distributed transports. All distributed transport types must inherit from this class
 */
public abstract class DistributedTransport extends OutputTransport {
    private static final Logger log = Logger.getLogger(DistributedTransport.class);
    private OptionHolder sinkOptionHolder;
    protected PublishingStrategy strategy;
    protected StreamDefinition streamDefinition;
    protected ExecutionPlanContext executionPlanContext;
    private String[] supportedDynamicOptions;

    /**
     * Will be called for initialing the {@link OutputTransport}
     *
     * @param outputStreamDefinition The stream definition this Output transport/sink is attached to
     * @param optionHolder           Option holder containing static and dynamic options related to the {@link OutputTransport}
     * @param executionPlanContext   Context of the execution plan which this output sink belongs to
     */
    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder, ExecutionPlanContext
            executionPlanContext) {
        this.streamDefinition = outputStreamDefinition;
        this.sinkOptionHolder = optionHolder;
        this.executionPlanContext = executionPlanContext;
    }

    /**
     * This is method contains the additional parameters which require to initialize distributed transport
     * @param streamDefinition
     * @param transportOptionHolder
     * @param executionPlanContext
     * @param destinationOptionHolders
     * @param sinkAnnotation
     * @param strategy
     */
    public void init(StreamDefinition streamDefinition, String type, OptionHolder transportOptionHolder,
                     OutputMapper outputMapper, String mapType, OptionHolder mapOptionHolder,String payload,
                     ExecutionPlanContext executionPlanContext, List<OptionHolder> destinationOptionHolders,
                     Annotation sinkAnnotation, PublishingStrategy strategy, String[] supportedDynamicOptions) {
        this.strategy = strategy;
        this.supportedDynamicOptions = supportedDynamicOptions;
        init(streamDefinition, type, transportOptionHolder, outputMapper, mapType, mapOptionHolder, payload, executionPlanContext);
        initTransport(sinkOptionHolder, destinationOptionHolders, sinkAnnotation, executionPlanContext);
    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions) throws ConnectionUnavailableException {
        int errorCount = 0;
        StringBuilder errorMessages = null;
        List<Integer> destinationsToPublish = strategy.getDestinationsToPublish(payload, transportOptions);
        for  (Integer destinationId : destinationsToPublish){
            try {
                publish(payload, transportOptions, destinationId);
            } catch (ConnectionUnavailableException e) {
                errorCount++;
                if (errorMessages == null){
                    errorMessages = new StringBuilder();
                }
                errorMessages.append("[Destination ").append(destinationId).append("]:").append(e.getMessage());
                log.warn("Failed to publish destination ID " + destinationId);
            }
        }

        if (errorCount > 0){
            throw new ConnectionUnavailableException(errorCount + "/" + destinationsToPublish.size()  + " connections"
                    + " failed while trying to publish with following error messages:" + errorMessages.toString());
        }
    }


    /**
     * Supported dynamic options by the transport
     *
     * @return the list of supported dynamic option keys
     */
    @Override
    public String[] getSupportedDynamicOptions() {
      return supportedDynamicOptions;
    }

    public abstract void publish(Object payload, DynamicOptions transportOptions, int destinationId) throws ConnectionUnavailableException;


    public abstract void initTransport(OptionHolder sinkOptionHolder, List<OptionHolder> destinationOptionHolders, Annotation
            sinkAnnotation, ExecutionPlanContext executionPlanContext);


}
