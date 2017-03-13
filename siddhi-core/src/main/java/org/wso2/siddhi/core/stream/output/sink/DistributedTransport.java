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

package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.List;

/**
 * This is the base class for Distributed transports. All distributed transport types must inherit from this class
 *
 */
public abstract class DistributedTransport extends OutputTransport {

    public static final String DISTRIBUTION_STRATEGY_KEY = "strategy";
    public static final String DISTRIBUTION_CHANNELS_KEY = "channels";
    public static final String PARTITION_KEY_FIELD_KEY = "partitionKey";

    public static final String DISTRIBUTION_STRATEGY_ROUND_ROBIN = "roundRobin";
    public static final String DISTRIBUTION_STRATEGY_DUPLICATE = "duplicate";
    public static final String DISTRIBUTION_STRATEGY_PARTITIONED = "partitioned";

    private String distributionStrategy;
    private int channelCount = -1;
    private int partitionFiledIndex = -1;
    private DistributedPublishingAlgorithm publisher;
    private OptionHolder sinkOptionHolder;

    @Override
    public  void init(OptionHolder optionHolder){
        this.sinkOptionHolder = optionHolder;

    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions) throws ConnectionUnavailableException {
        publisher.publish(payload,  transportOptions);
    }

    public void initDistribution(OptionHolder distributedOptionHolder, List<OptionHolder> nodeOptionHolders){
        distributionStrategy = distributedOptionHolder.validateAndGetStaticValue(DISTRIBUTION_STRATEGY_KEY);

        if (distributionStrategy == null || distributionStrategy.isEmpty()){
            throw new ExecutionPlanValidationException("Distribution strategy is not specified.");
        }

        if (distributedOptionHolder.isOptionExists(DISTRIBUTION_CHANNELS_KEY)) {
            channelCount = Integer.parseInt(distributedOptionHolder.validateAndGetStaticValue(DISTRIBUTION_CHANNELS_KEY));
            if (channelCount <= 0) {
                throw new ExecutionPlanValidationException("There must be at least one channel.");
            }
        }

        if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_ROUND_ROBIN)){
            publisher = getRoundRobinPublisher();
        } else if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_DUPLICATE)){
            publisher = getAllEndpointsPublisher();
        } else if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_PARTITIONED)){
            publisher = getPartitionedPublisher();
        } else {
            throw new ExecutionPlanValidationException("Unknown distribution strategy '" + distributionStrategy + "'.");
        }

        initTransport(sinkOptionHolder, nodeOptionHolders);
    }

    public String getDistributionStrategy(){
        return  distributionStrategy;
    }

    public int getChannelCount(){
        if (channelCount == -1){
            throw new ExecutionPlanValidationException("Channel count not specified.");
        }

        return channelCount;
    }

    public int getPartitionFiledIndex(){
        return partitionFiledIndex;
    }

    public abstract DistributedPublishingAlgorithm getRoundRobinPublisher();

    public abstract DistributedPublishingAlgorithm getAllEndpointsPublisher();

    public abstract DistributedPublishingAlgorithm getPartitionedPublisher();

    public abstract void initTransport(OptionHolder sinkOptionHolder, List<OptionHolder> nodeOptionHolders);

}
