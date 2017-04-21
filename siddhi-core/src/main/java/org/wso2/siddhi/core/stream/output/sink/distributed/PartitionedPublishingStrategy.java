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
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajith on 4/21/17.
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
    private int partitionKeyFieldPosition = -1;
    protected List<Integer> returnValue = new ArrayList<>();
    /**
     * Initialize actual strategy implementations. Required information for strategy implementation can be fetched
     * inside this method
     */
    @Override
    protected void initStrategy() {
        totalDestinationCount = destinationOptionHolders.size();
        String partitionKey = distributionOptionHolder.validateAndGetStaticValue(SiddhiConstants
                .PARTITION_KEY_FIELD_KEY);

        if (partitionKey == null || partitionKey.isEmpty()){
            throw new ExecutionPlanValidationException("PartitionKey is required for partitioned distribution " +
                    "strategy.");
        }

        try {
            partitionKeyFieldPosition = streamDefinition.getAttributePosition(partitionKey);
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
        String partitionKeyValue = (String)transportOptions.getEvent().getData(partitionKeyFieldPosition);
        int destinationId = partitionKeyValue.hashCode() % totalDestinationCount;

        if (destinationIds.contains(destinationId)){
            returnValue.clear();
            returnValue.add(destinationId);
            return returnValue;
        } else {
            return EMPTY_RETURN_VALUE;
        }
    }
}
