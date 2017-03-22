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

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
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

    private int channelCount = -1;
    private OptionHolder sinkOptionHolder;
    protected PublishingStrategy strategy;
    protected StreamDefinition streamDefinition;
    protected ExecutionPlanContext executionPlanContext;

    /**
     * Will be called for initialing the {@link OutputTransport}
     *
     * @param outputStreamDefinition
     * @param optionHolder           Option holder containing static and dynamic options related to the {@link OutputTransport}
     * @param executionPlanContext
     */
    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder, ExecutionPlanContext executionPlanContext) {
        this.streamDefinition = outputStreamDefinition;
        this.sinkOptionHolder = optionHolder;
        this.executionPlanContext = executionPlanContext;
    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions) throws ConnectionUnavailableException {
        //Set<Integer> destinationsToPublish = strategy.getDestinationsToPublish(payload, transportOptions);
        //destinationsToPublish.forEach(destinationId -> publish(payload, transportOptions, destinationId));
    }

    public void initDistributedTransportOptions(OptionHolder distributedOptionHolder,
                                                List<OptionHolder> endpointOptionHolders,
                                                Annotation sinkAnnotation,
                                                PublishingStrategy strategy) {
        this.strategy = strategy;
        initTransport(sinkOptionHolder, endpointOptionHolders, sinkAnnotation, executionPlanContext);
    }

    public abstract void publish(Object payload, DynamicOptions transportOptions, int partitionId) throws ConnectionUnavailableException;


    public abstract void initTransport(OptionHolder sinkOptionHolder, List<OptionHolder> nodeOptionHolders, Annotation
            sinkAnnotation, ExecutionPlanContext executionPlanContext);


}
