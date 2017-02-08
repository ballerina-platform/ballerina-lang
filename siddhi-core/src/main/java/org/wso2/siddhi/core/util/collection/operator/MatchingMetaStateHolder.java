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

/**
 * Created by suho on 5/21/16.
 */
public class MatchingMetaStateHolder {
    private int defaultStreamEventIndex;
    private final int candidateEventIndex;
    private int streamEventSize;
    private AbstractDefinition matchingStreamDefinition;
    private AbstractDefinition candsidateDefinition;
    private MetaStateEvent metaStateEvent;

    public MatchingMetaStateHolder(MetaStateEvent metaStateEvent, int defaultStreamEventIndex, int candidateEventIndex, int streamEventSize, AbstractDefinition matchingStreamDefinition, AbstractDefinition candsidateDefinition) {
        this.metaStateEvent = metaStateEvent;
        this.defaultStreamEventIndex = defaultStreamEventIndex;
        this.candidateEventIndex = candidateEventIndex;
        this.streamEventSize = streamEventSize;
        this.matchingStreamDefinition = matchingStreamDefinition;
        this.candsidateDefinition = candsidateDefinition;
    }

    public int getDefaultStreamEventIndex() {
        return defaultStreamEventIndex;
    }

    public int getCandidateEventIndex() {
        return candidateEventIndex;
    }

    public int getStreamEventSize() {
        return streamEventSize;
    }

    public MetaStateEvent getMetaStateEvent() {
        return metaStateEvent;
    }

    public AbstractDefinition getMatchingStreamDefinition() {
        return matchingStreamDefinition;
    }

    public AbstractDefinition getCandsidateDefinition() {
        return candsidateDefinition;
    }
}

