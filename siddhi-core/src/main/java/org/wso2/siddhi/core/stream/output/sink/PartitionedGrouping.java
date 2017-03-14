package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;

/**
 * This implementation of {@OutputEventGroupDeterminer} groups events into 'n' number of partitions. The events will be divided into predefined number
 * partitions based on the value of a given field in the event. Events which are having the same value for the partitioning field will belong the to
 * the same partition.
 */
public class PartitionedGrouping implements OutputGroupDeterminer {
    int partitionFieldIndex;
    int partitionCount;

    public PartitionedGrouping (int partitionFieldIndex, int partitionCount){
        this.partitionFieldIndex = partitionFieldIndex;
        this.partitionCount = partitionCount;
    }
    /**
     * Deciding the group of a given event and returning a unique identifier to identify a group. A correct implementation of this method
     * should be returning  the same group identifier for all events belongs a single group.
     *
     * @param event Event that needs to be decided to which group it belongs to
     * @return Unique Identifier to identify the group of the event
     */
    @Override
    public String decideGroup(Event event) {
        return Integer.toString(event.getData(partitionFieldIndex).hashCode() % partitionCount);
    }
}
