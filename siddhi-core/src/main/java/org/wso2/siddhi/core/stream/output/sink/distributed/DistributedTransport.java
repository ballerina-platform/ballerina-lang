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
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.List;
import java.util.Set;

/**
 * This is the base class for Distributed transports. All distributed transport types must inherit from this class
 */
public abstract class DistributedTransport extends OutputTransport {

    public static final String DISTRIBUTION_STRATEGY_KEY = "strategy";
    public static final String DISTRIBUTION_CHANNELS_KEY = "channels";
    public static final String PARTITION_KEY_FIELD_KEY = "partitionKey";

    public static final String DISTRIBUTION_STRATEGY_ROUND_ROBIN = "roundRobin";
    public static final String DISTRIBUTION_STRATEGY_ALL = "all";
    public static final String DISTRIBUTION_STRATEGY_PARTITIONED = "partitioned";

    private int channelCount = -1;
    private OptionHolder sinkOptionHolder;
    protected DistributedPublishingStrategy strategy;

    @Override
    public void configure(OptionHolder optionHolder) {
        this.sinkOptionHolder = optionHolder;
    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions) throws ConnectionUnavailableException {
        Set<Integer> destinationsToPublish = strategy.getDestinationsToPublish(payload, transportOptions);
        destinationsToPublish.forEach(destinationId -> publish(payload, transportOptions, destinationId));
    }

    public void initDistributedTransportOptions(OptionHolder distributedOptionHolder,List<OptionHolder> endpointOptionHolders,
                                                Annotation sinkAnnotation, ExecutionPlanContext executionPlanContext,
                                                DistributedPublishingStrategy strategy) {
        this.strategy = strategy;
        if (distributedOptionHolder.isOptionExists(DISTRIBUTION_CHANNELS_KEY)) {
            channelCount = Integer.parseInt(distributedOptionHolder
                    .validateAndGetStaticValue(DISTRIBUTION_CHANNELS_KEY));
            if (channelCount <= 0) {
                throw new ExecutionPlanValidationException("There must be at least one channel.");
            }
        } else {
            if (endpointOptionHolders.size() <= 0) {
                throw new ExecutionPlanValidationException("There must be at least one endpoint.");
            }
        }

        initTransport(sinkOptionHolder, endpointOptionHolders, sinkAnnotation, executionPlanContext);
    }


    public int getChannelCount() {
        if (channelCount == -1) {
            throw new ExecutionPlanValidationException("Channel count not specified.");
        }

        return channelCount;
    }


    public abstract void publish(Object payload, DynamicOptions transportOptions, int partitionId) throws ConnectionUnavailableException;


    public abstract void initTransport(OptionHolder sinkOptionHolder, List<OptionHolder> nodeOptionHolders, Annotation
            sinkAnnotation, ExecutionPlanContext executionPlanContext);

}
