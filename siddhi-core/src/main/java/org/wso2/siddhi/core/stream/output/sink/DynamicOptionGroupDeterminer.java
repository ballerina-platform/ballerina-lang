package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.transport.Option;

import java.util.List;

/**
 * This implementation of {@OutputEventGroupDeterminer} groups events based on dynamic options of the Sink. Events which are having the same value
 * for all dynamic options are belong to the same group.
 */
public class DynamicOptionGroupDeterminer implements OutputGroupDeterminer {

    List<Option> dynamicTransportOptions;
    public DynamicOptionGroupDeterminer(List<Option> dynamicTransportOptions){
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
        dynamicTransportOptions.forEach(transportOption -> stringBuilder.append(transportOption.getValue(event)).append(":::"));
        return stringBuilder.toString();
    }
}
