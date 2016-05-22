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

