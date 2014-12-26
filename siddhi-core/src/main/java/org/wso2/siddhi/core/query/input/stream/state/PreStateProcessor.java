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

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.processor.Processor;

/**
 * Created on 12/17/14.
 */
public interface PreStateProcessor extends Processor {

    void addState(StateEvent stateEvent);
    void addEveryState(StateEvent stateEvent);
    public void setStateId(int stateId);

    public void init();

    void stateChanged();

    void setStartState(boolean isStartState);


    public void setStateEventPool(StateEventPool stateEventPool) ;

    public void setStreamEventPool(StreamEventPool streamEventPool) ;

    public void setStreamEventCloner(StreamEventCloner streamEventCloner);

    public void setStateEventCloner(StateEventCloner stateEventCloner) ;

    void updateState();
}
