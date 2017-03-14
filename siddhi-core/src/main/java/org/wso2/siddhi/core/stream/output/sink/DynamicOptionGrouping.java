package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.transport.Option;

import java.util.ArrayList;

/**
 * This implementation of {@OutputEventGroupDeterminer} groups events based on dynamic options of the Sink. Events which are having the same value
 * for all dynamic options are belong to the same group.
 */
public class DynamicOptionGrouping implements OutputGroupDeterminer {

    ArrayList<Option> dynamicTransportOptions;
    public DynamicOptionGrouping(ArrayList<Option> dynamicTransportOptions){
        this.dynamicTransportOptions = dynamicTransportOptions;
    }
    /**
     * Deciding the group of a given event and returning a unique identifier to identify a group. A correct implementation of this method
     * should be retruining  the same group identifier for all events belongs a single group.
     *
     * @param event Event that needs to be decided to which group it belongs to
     * @return Unique Identifier to identify the group of the event
     */
    @Override
    public String decideGroup(Event event) {
        StringBuilder stringBuilder = new StringBuilder("");
        for (Option transportOption : dynamicTransportOptions) {
            stringBuilder.append(transportOption.getValue(event));
            stringBuilder.append(":::");
        }
        return stringBuilder.toString();
    }
}
