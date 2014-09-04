/*
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

package org.wso2.siddhi.core.event.state;

import org.wso2.siddhi.core.event.ComplexMetaEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

/**
 * Class to hold mapping between Events and respective StateEvent.
 * This consist of array of MetaStreamEvents which represent each
 * Event within StateEvent
 */
public class MetaStateEvent implements ComplexMetaEvent{
    private MetaStreamEvent[] metaStreamEvents;
    private int eventCount = 0;
    private StreamDefinition outputStreamDefinition;

    public MetaStateEvent(int size){
        metaStreamEvents = new MetaStreamEvent[size];
    }

    public MetaStateEvent(MetaStreamEvent[] metaStreamEvents){
        this.metaStreamEvents = metaStreamEvents.clone();
        eventCount = metaStreamEvents.length;
    }

    public MetaStreamEvent getMetaEvent(int position){
        return metaStreamEvents[position];
    }

    public void addEvent(MetaStreamEvent metaStreamEvent){
        metaStreamEvents[eventCount] = metaStreamEvent;
        eventCount++;
    }

    @Override
    public void addData(Attribute attribute) {
        //TODO consider selector population
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setOutputDefinition(StreamDefinition definition) {
        this.outputStreamDefinition = definition;
    }

    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }
}
