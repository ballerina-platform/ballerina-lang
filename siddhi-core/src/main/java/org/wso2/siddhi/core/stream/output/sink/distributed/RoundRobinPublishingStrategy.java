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
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Publishing strategy to implement messages in a round robin manner to multiple destinations
 * */

@Extension(
        name = "roundRobin",
        namespace = "distributionstrategy",
        description = ""
)
public class RoundRobinPublishingStrategy extends PublishingStrategy {
    private int count = 0;
    private int destinationCount;
    private List<Integer> returnValue = new ArrayList<>();

    /**
     * Initialize the Distribution strategy with the information it will require to make decisions.
     * @param streamDefinition The stream attached to the sink this PublishingStrategy is used in
     * @param transportOptionHolder Sink options of the sink which uses this PublishingStrategy
     * @param destinationOptionHolders The list of options under @destination of the relevant sink.
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder transportOptionHolder,
                     OptionHolder distributionOptionHolder, List<OptionHolder> destinationOptionHolders) {

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
        if (destinationIds.isEmpty()){
            return PublishingStrategy.EMPTY_RETURN_VALUE;
        }

        int currentDestinationCount = destinationIds.size();
        if (destinationCount != currentDestinationCount){
            destinationCount = currentDestinationCount;
        }

        if (!returnValue.isEmpty()){
            returnValue.remove(0);
        }

        if (destinationCount > 0) {
            returnValue.add(destinationIds.get(count++ % destinationCount));
        }

        return returnValue;
    }
}
