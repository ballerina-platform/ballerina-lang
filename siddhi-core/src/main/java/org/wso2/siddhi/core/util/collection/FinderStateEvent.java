package org.wso2.siddhi.core.util.collection;

import org.wso2.siddhi.core.event.state.StateEvent;

/**
 * Created by suho on 5/21/16.
 */
public class FinderStateEvent extends StateEvent {

    public FinderStateEvent(int streamEventsSize, int outputSize) {
        super(streamEventsSize, outputSize);
    }

    public void setEvent(StateEvent matchingStateEvent) {
        if (matchingStateEvent != null) {
            System.arraycopy(matchingStateEvent.getStreamEvents(), 0, streamEvents, 0,
                    matchingStateEvent.getStreamEvents().length);
        } else {
            for (int i = 0; i < streamEvents.length - 1; i++) {
                streamEvents[i] = null;
            }
        }
    }
}
