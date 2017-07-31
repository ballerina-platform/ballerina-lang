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
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Class to parse {@link MatchingMetaInfoHolder}
 */
public class MatcherParser {

    public static MatchingMetaInfoHolder constructMatchingMetaStateHolder(MetaComplexEvent matchingMetaComplexEvent,
                                                                          int defaultStreamEventIndex,
                                                                          AbstractDefinition candsidateDefinition,
                                                                          int currentState) {
        int storeEventIndex = 0;

        MetaStreamEvent tableStreamEvent = new MetaStreamEvent();
        tableStreamEvent.setEventType(MetaStreamEvent.EventType.TABLE);
        tableStreamEvent.addInputDefinition(candsidateDefinition);
        for (Attribute attribute : candsidateDefinition.getAttributeList()) {
            tableStreamEvent.addOutputData(attribute);
        }

        MetaStateEvent metaStateEvent = null;
        if (matchingMetaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(2);
            metaStateEvent.addEvent(((MetaStreamEvent) matchingMetaComplexEvent));
            metaStateEvent.addEvent(tableStreamEvent);
            storeEventIndex = 1;
            defaultStreamEventIndex = 0;
            if (currentState == UNKNOWN_STATE) {
                currentState = defaultStreamEventIndex;
            }
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) matchingMetaComplexEvent).getMetaStreamEvents();

            //for join
            for (; storeEventIndex < metaStreamEvents.length; storeEventIndex++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[storeEventIndex];
                if (storeEventIndex != defaultStreamEventIndex && metaStreamEvent.getLastInputDefinition()
                        .equalsIgnoreAnnotations(candsidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) matchingMetaComplexEvent);
                    break;
                }
            }

            if (metaStateEvent == null) {
                metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                    metaStateEvent.addEvent(metaStreamEvent);
                }
                metaStateEvent.addEvent(tableStreamEvent);
                storeEventIndex = metaStreamEvents.length;
            }
        }
        return new MatchingMetaInfoHolder(metaStateEvent, defaultStreamEventIndex, storeEventIndex,
                metaStateEvent.getMetaStreamEvent(defaultStreamEventIndex).getLastInputDefinition(),
                candsidateDefinition, currentState);
    }

    public static UpdateAttributeMapper[] constructUpdateAttributeMapper(
            AbstractDefinition tableDefinition, List<Attribute> updatingStreamDefinitionAttributes,
            int matchingStreamEventPosition) {
        UpdateAttributeMapper[] updateAttributeMappers =
                new UpdateAttributeMapper[updatingStreamDefinitionAttributes.size()];
        for (int i = 0; i < updatingStreamDefinitionAttributes.size(); i++) {
            Attribute streamAttribute = updatingStreamDefinitionAttributes.get(i);
            updateAttributeMappers[i] = new UpdateAttributeMapper(i,
                    tableDefinition.getAttributePosition(streamAttribute.getName()), streamAttribute.getName(),
                    matchingStreamEventPosition);
        }
        return updateAttributeMappers;
    }
}
