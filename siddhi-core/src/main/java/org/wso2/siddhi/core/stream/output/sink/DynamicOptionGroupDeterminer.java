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

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.transport.Option;

import java.util.List;

/**
 * This implementation of {@link OutputGroupDeterminer} groups events based on dynamic options of the Sink. Events
 * which are having the same value
 * for all dynamic options are belong to the same group.
 */
public class DynamicOptionGroupDeterminer implements OutputGroupDeterminer {

    List<Option> dynamicTransportOptions;

    public DynamicOptionGroupDeterminer(List<Option> dynamicTransportOptions) {
        this.dynamicTransportOptions = dynamicTransportOptions;
    }

    /**
     * Deciding the group of a given event and returning a unique identifier to identify the group. A correct
     * implementation of this method
     * should be returning  the same group identifier for all events belongs a give group.
     *
     * @param event Event that needs to be decided to which group it belongs to
     * @return Unique Identifier to identify the group of the event
     */
    @Override
    public String decideGroup(Event event) {
        StringBuilder stringBuilder = new StringBuilder("");
        dynamicTransportOptions
                .forEach(transportOption -> stringBuilder.append(transportOption.getValue(event))
                        .append(SiddhiConstants.KEY_DELIMITER));
        return stringBuilder.toString();
    }
}
