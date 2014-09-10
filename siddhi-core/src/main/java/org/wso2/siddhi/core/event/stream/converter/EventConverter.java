package org.wso2.siddhi.core.event.stream.converter;/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;

public interface EventConverter {
    /**
     * Method to convert Event into StreamEvent
     *
     * @param event Event to be converted
     * @return converted StreamEvent
     */
    public StreamEvent convertToStreamEvent(Event event);

    /**
     * Method to convert(change format) StreamEvent into new StreamEvent
     *
     * @param streamEvent StreamEvent to be Converted
     * @return converted StreamEvent
     */
    public StreamEvent convertToStreamEvent(StreamEvent streamEvent);
}
