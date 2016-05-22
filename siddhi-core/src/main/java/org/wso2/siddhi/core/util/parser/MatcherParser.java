package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

/**
 * Created by suho on 5/21/16.
 */
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
