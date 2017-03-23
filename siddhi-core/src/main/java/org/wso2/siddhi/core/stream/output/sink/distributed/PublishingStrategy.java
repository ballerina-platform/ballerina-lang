package org.wso2.siddhi.core.stream.output.sink.distributed;

import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Parent class for the Distributed publishing strategy extensions.
 */
public abstract class PublishingStrategy {

    protected StreamDefinition streamDefinition;
    protected OptionHolder transportOptionHolder;
    protected List<OptionHolder> destinationOptionHolders;
    // The set of destination IDs are kept in the sorted order
    protected List<Integer> destinationIds = new CopyOnWriteArrayList<>();
    private boolean isSuspended = false;

    /**
     * Initialize the Distribution strategy with the information it will require to make decisions.
     * @param streamDefinition The stream attached to the sink this PublishingStrategy is used in
     * @param transportOptionHolder Sink options of the sink which uses this PublishingStrategy
     * @param destinationOptionHolders The list of options under @destination of the relevant sink.
     */
    public void init(StreamDefinition streamDefinition, OptionHolder transportOptionHolder, List<OptionHolder>
            destinationOptionHolders){
        this.streamDefinition = streamDefinition;
        this.transportOptionHolder = transportOptionHolder;
        this.destinationOptionHolders = destinationOptionHolders;
        initStrategy();
    }


    /**
     * Initialize actual strategy implementations. Required information for strategy implementation can be fetched
     * inside this method
     */
    protected abstract void initStrategy();

    /**
     * This method tells the ID(s) of the destination(s) to which a given messages should be sent. There can be cases
     * where a given message is only sent to a specific destination(e.g., partition based) and message is sent to
     * multiple endpoints(e.g., broadcast)
     * @param payload payload of the message
     * @param transportOptions Dynamic transport options of the sink
     * @return Set of IDs of the destination to which the event should be sent
     */
    public abstract List<Integer> getDestinationsToPublish(Object payload, DynamicOptions transportOptions);

    /**
     * Remove a given destination from available set of destination IDs. Once this method is called for a given
     * destination ID, that particular destination ID will not returned when getDestinationsToPublish() is called
     * @param destinationId
     */
    public void removeDestination(int destinationId){
        destinationIds.remove(destinationId);
        Collections.sort(destinationIds);
    }

    /**
     * Remove a destination to available set of destination IDs. Once this method is called for a given
     * destination ID, that particular destination ID will be considered when getDestinationsToPublish() is called
     * @param destinationId
     */
    public void addDestination(int destinationId){
        destinationIds.add(destinationId);
        Collections.sort(destinationIds);
    }

    public void suspend(){
        isSuspended = true;
    }
}
