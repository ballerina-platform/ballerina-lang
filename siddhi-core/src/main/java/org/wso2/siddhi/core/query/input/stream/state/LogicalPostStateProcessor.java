/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;

/**
 * Created on 12/28/14.
 */
public class LogicalPostStateProcessor extends StreamPostStateProcessor {

    private LogicalStateElement.Type type;
    private LogicalPreStateProcessor partnerPreStateProcessor;

    public LogicalPostStateProcessor(LogicalStateElement.Type type) {

        this.type = type;
    }

    public LogicalStateElement.Type getType() {
        return type;
    }

    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        if (complexEventChunk.hasNext()) {     //one one event will be coming
            StateEvent stateEvent = (StateEvent) complexEventChunk.next();
            switch (type) {
                case AND:
                    partnerPreStateProcessor.partnerStateChanged(stateEvent);
                    if (stateEvent.getStreamEvent(partnerPreStateProcessor.getStateId()) != null) {
                        super.process(complexEventChunk);
                    } else {
                        thisStatePreProcessor.stateChanged();
                    }
                    break;
                case OR:
                    partnerPreStateProcessor.partnerStateChanged(stateEvent);
                    super.process(complexEventChunk);
                    break;
                case NOT:
                    break;
            }
        }
        complexEventChunk.clear();
    }

    public void setPartnerPreStateProcessor(LogicalPreStateProcessor partnerPreStateProcessor) {
        this.partnerPreStateProcessor = partnerPreStateProcessor;
    }

    public LogicalPreStateProcessor getPartnerPreStateProcessor() {
        return partnerPreStateProcessor;
    }
}
