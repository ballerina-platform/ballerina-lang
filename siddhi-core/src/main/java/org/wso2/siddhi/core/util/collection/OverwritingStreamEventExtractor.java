package org.wso2.siddhi.core.util.collection;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

/**
 * Created by suho on 5/21/16.
 */
public class OverwritingStreamEventExtractor {
    private int matchingStreamEventPosition;

    public OverwritingStreamEventExtractor(int matchingStreamEventPosition) {
        this.matchingStreamEventPosition = matchingStreamEventPosition;
    }

    public StreamEvent getOverwritingStreamEvent(StateEvent updatingEvent) {
        return updatingEvent.getStreamEvent(matchingStreamEventPosition);
    }
}

