/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.siddhi.tcp.transport.dto;

import org.wso2.siddhi.core.event.Event;

/**
 * Class to hold Siddhi Event and the corresponding stream iD. Composite was introduced because
 * {@link org.wso2.siddhi.core.event.Event} does not contain a stream ID
 */
public class SiddhiEventComposite {
    private Event siddhiEvent;
    private String streamID;

    public SiddhiEventComposite(Event siddhiEvent, String streamID) {
        this.siddhiEvent = siddhiEvent;
        this.streamID = streamID;
    }

    public Event getSiddhiEvent() {
        return siddhiEvent;
    }

    public String getStreamID() {
        return streamID;
    }
}
