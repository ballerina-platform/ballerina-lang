/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;

/**
 * This implementation of {@link OutputGroupDeterminer} groups events into 'n' number of partitions. The events will
 * be divided into predefined number
 * partitions based on the value of a given field in the event. Events which are having the same value for the
 * partitioning field will belong the to
 * the same partition.
 */
public class PartitionedGroupDeterminer implements OutputGroupDeterminer {
    int partitionFieldIndex;
    int partitionCount;

    public PartitionedGroupDeterminer(int partitionFieldIndex, int partitionCount) {
        this.partitionFieldIndex = partitionFieldIndex;
        this.partitionCount = partitionCount;
    }

    /**
     * Deciding the group of a given event and returning a unique identifier to identify a group. A correct
     * implementation of this method
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
