/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Publishing strategy to allow publish messages to multiple destination by partitioning
 */
@Extension(
        name = "partitioned",
        namespace = "distributionstrategy",
        description = ""
)
public class PartitionedPublishingStrategy extends PublishingStrategy {
    /**
     * Keep track of all the destinations regardless of their connectivity status
     */
    private int totalDestinationCount = 0;
    private Option partitionOption;
    private List<Integer> returnValue = new ArrayList<>();

    /**
     * Initialize the Distribution strategy with the information it will require to make decisions.
     *
     * @param streamDefinition         The stream attached to the sink this PublishingStrategy is used in
     * @param transportOptionHolder    Sink options of the sink which uses this PublishingStrategy
     * @param destinationOptionHolders The list of options under @destination of the relevant sink.
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder transportOptionHolder,
                     OptionHolder distributionOptionHolder, List<OptionHolder> destinationOptionHolders) {
        totalDestinationCount = destinationOptionHolders.size();
        String partitionKey = distributionOptionHolder.validateAndGetStaticValue(SiddhiConstants
                .PARTITION_KEY_FIELD_KEY);

        if (partitionKey == null || partitionKey.isEmpty()) {
            throw new ExecutionPlanValidationException("PartitionKey is required for partitioned distribution " +
                    "strategy.");
        }
        try {
            int partitionKeyFieldPosition = streamDefinition.getAttributePosition(partitionKey);
            partitionOption = new Option(partitionKeyFieldPosition);
        } catch (AttributeNotExistException e){
            throw new ExecutionPlanValidationException("Could not find partition key attribute", e);
        }

    }

    /**
     * This method tells the ID(s) of the destination(s) to which a given messages should be sent. There can be cases
     * where a given message is only sent to a specific destination(e.g., partition based) and message is sent to
     * multiple endpoints(e.g., broadcast)
     *
     * @param payload          payload of the message
     * @param transportOptions Dynamic transport options of the sink
     * @return Set of IDs of the destination to which the event should be sent
     */
    @Override
    public List<Integer> getDestinationsToPublish(Object payload, DynamicOptions transportOptions) {
        String partitionKeyValue = partitionOption.getValue(transportOptions);
        int destinationId = partitionKeyValue.hashCode() % totalDestinationCount;

        if (destinationIds.contains(destinationId)) {
            returnValue.clear();
            returnValue.add(destinationId);
            return returnValue;
        } else {
            return EMPTY_RETURN_VALUE;
        }
    }
}
