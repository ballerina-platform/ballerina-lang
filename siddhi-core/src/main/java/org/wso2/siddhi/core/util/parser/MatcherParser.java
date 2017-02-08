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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public class MatcherParser {

    public static MatchingMetaStateHolder constructMatchingMetaStateHolder(MetaComplexEvent matchingMetaComplexEvent, int defaultStreamEventIndex, AbstractDefinition candsidateDefinition) {
        int candidateEventIndex = 0;
        int streamEventSize = 0;

        MetaStreamEvent eventTableStreamEvent = new MetaStreamEvent();
        eventTableStreamEvent.setTableEvent(true);
        eventTableStreamEvent.addInputDefinition(candsidateDefinition);
        for (Attribute attribute : candsidateDefinition.getAttributeList()) {
            eventTableStreamEvent.addOutputData(attribute);
        }

        MetaStateEvent metaStateEvent = null;
        if (matchingMetaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(2);
            metaStateEvent.addEvent(((MetaStreamEvent) matchingMetaComplexEvent));
            metaStateEvent.addEvent(eventTableStreamEvent);
            candidateEventIndex = 1;
            defaultStreamEventIndex = 0;
            streamEventSize = 2;
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) matchingMetaComplexEvent).getMetaStreamEvents();

            //for join
            for (; candidateEventIndex < metaStreamEvents.length; candidateEventIndex++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventIndex];
                if (candidateEventIndex != defaultStreamEventIndex && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candsidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) matchingMetaComplexEvent);
                    streamEventSize = metaStreamEvents.length;
                    break;
                }
            }

            if (metaStateEvent == null) {
                metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                    metaStateEvent.addEvent(metaStreamEvent);
                }
                metaStateEvent.addEvent(eventTableStreamEvent);
                candidateEventIndex = metaStreamEvents.length;
                streamEventSize = metaStreamEvents.length + 1;
            }
        }
        return new MatchingMetaStateHolder(metaStateEvent, defaultStreamEventIndex, candidateEventIndex, streamEventSize, metaStateEvent.getMetaStreamEvent(defaultStreamEventIndex).getLastInputDefinition(), candsidateDefinition);
    }

    public static UpdateAttributeMapper[] constructUpdateAttributeMapper(AbstractDefinition tableDefinition, List<Attribute> updatingStreamDefinition, int matchingStreamEventPosition) {
        UpdateAttributeMapper[] updateAttributeMappers = new UpdateAttributeMapper[updatingStreamDefinition.size()];
        for (int i = 0; i < updatingStreamDefinition.size(); i++) {
            Attribute streamAttribute = updatingStreamDefinition.get(i);
            updateAttributeMappers[i] = new UpdateAttributeMapper(i, tableDefinition.getAttributePosition(streamAttribute.getName()), matchingStreamEventPosition);
        }
        return updateAttributeMappers;
    }
}
