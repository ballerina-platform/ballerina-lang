/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.collection.operator;

import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Information holder for matching event used by in-memory table implementation.
 */
public class MatchingMetaInfoHolder {
    private int matchingStreamEventIndex;
    private int storeEventIndex;
    private AbstractDefinition matchingStreamDefinition;
    private AbstractDefinition storeDefinition;
    private MetaStateEvent metaStateEvent;
    private int currentState = UNKNOWN_STATE;

    public MatchingMetaInfoHolder(MetaStateEvent metaStateEvent, int matchingStreamEventIndex, int storeEventIndex,
                                  AbstractDefinition matchingStreamDefinition, AbstractDefinition storeDefinition,
                                  int currentState) {
        this.metaStateEvent = metaStateEvent;
        this.matchingStreamEventIndex = matchingStreamEventIndex;
        this.storeEventIndex = storeEventIndex;
        this.matchingStreamDefinition = matchingStreamDefinition;
        this.storeDefinition = storeDefinition;
        this.currentState = currentState;
    }

    public int getMatchingStreamEventIndex() {
        return matchingStreamEventIndex;
    }

    public int getStoreEventIndex() {
        return storeEventIndex;
    }

    public MetaStateEvent getMetaStateEvent() {
        return metaStateEvent;
    }

    public AbstractDefinition getMatchingStreamDefinition() {
        return matchingStreamDefinition;
    }

    public AbstractDefinition getStoreDefinition() {
        return storeDefinition;
    }

    public int getCurrentState() {
        return currentState;
    }
}

