package org.wso2.siddhi.core.util.collection;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

/**
 * Created by suho on 5/21/16.
 */
public class UpdateAttributeMapper {
    private final int updatingAttributePosition;
    private final int candidateAttributePosition;
    private int matchingStreamEventPosition;

    public UpdateAttributeMapper(int updatingAttributePosition, int candidateAttributePosition, int matchingStreamEventPosition) {

        this.updatingAttributePosition = updatingAttributePosition;
        this.candidateAttributePosition = candidateAttributePosition;
        this.matchingStreamEventPosition = matchingStreamEventPosition;
    }

    public int getCandidateAttributePosition() {
        return candidateAttributePosition;
    }

    public Object getOutputData(StateEvent updatingEvent) {
        return updatingEvent.getStreamEvent(matchingStreamEventPosition).getOutputData()[updatingAttributePosition];
    }

    public StreamEvent getOverwritingStreamEvent(StateEvent updatingEvent) {
        return updatingEvent.getStreamEvent(matchingStreamEventPosition);
    }
}

