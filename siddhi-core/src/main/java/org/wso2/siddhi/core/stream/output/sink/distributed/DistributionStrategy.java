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

import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Parent class for the Distributed publishing strategy extensions. Note that destinationId of the each
 * destination is implied by the order which @destination annotations appear in @sink annotation. Therefore, when
 * implementing a strategy, if the implementation wants refer to a certain @destination the ID can be derived by
 * looking at the listing order of @destination
 */
public abstract class DistributionStrategy {

    static final List<Integer> EMPTY_RETURN_VALUE = new ArrayList<>();
    List<Integer> destinationIds = new CopyOnWriteArrayList<>();

    /**
     * Initialize the Distribution strategy with the information it will require to make decisions.
     *
     * @param streamDefinition         The stream attached to the sink this DistributionStrategy is used in
     * @param transportOptionHolder    Sink options of the sink which uses this DistributionStrategy
     * @param distributionOptionHolder The option under @destination of the relevant sink.
     * @param destinationOptionHolders The list of options under @destination of the relevant sink.
     * @param configReader This hold the {@link DistributionStrategy} extensions configuration reader.
     */
    public abstract void init(StreamDefinition streamDefinition, OptionHolder transportOptionHolder, OptionHolder
            distributionOptionHolder, List<OptionHolder> destinationOptionHolders, ConfigReader configReader);

    /**
     * This method tells the ID(s) of the destination(s) to which a given messages should be sent. There can be cases
     * where a given message is only sent to a specific destination(e.g., partition based) and message is sent to
     * multiple endpoints(e.g., broadcast)
     *
     * @param payload          payload of the message
     * @param transportOptions Dynamic transport options of the sink
     * @return Set of IDs of the destination to which the event should be sent
     */
    public abstract List<Integer> getDestinationsToPublish(Object payload, DynamicOptions transportOptions);

    /**
     * Remove a given destination from available set of destination IDs. Once this method is called for a given
     * destination ID, that particular destination ID will not included in the return value of subsequent
     * getDestinationsToPublish() is calls
     *
     * @param destinationId the ID of the destination to be removed
     */
    public void destinationFailed(int destinationId) {
        destinationIds.remove(destinationId);
    }

    /**
     * Remove a destination to available set of destination IDs. Once this method is called for a given
     * destination ID, that particular destination ID will be considered when getDestinationsToPublish() is called
     *
     * @param destinationId the ID of the destination to be check for availability.
     */
    public void destinationAvailable(int destinationId) {
        if (destinationIds.contains(destinationId)) {
            throw new SiddhiAppValidationException("Destination ID " + destinationId + " already registered");
        }

        destinationIds.add(destinationId);
        //Destination IDs are implied by the order they appear in @sink annotation. i.e, the first @desination appear
        // is assigned ID 0 and the second is assigned 1 and so on. IDs are not changed once assigned. Therefore,
        // sorting the Ids once a new ID is added to keep the IDs in the same order as their respective @destination
        // annotations are listed
        Collections.sort(destinationIds);
    }
}
