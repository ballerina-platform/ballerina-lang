/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.query.output.ratelimit.snapshot;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class WrappedSnapshotOutputRateManager extends OutputRateManager {
    SnapshotOutputRateManager outputRateManager;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private final boolean groupBy;
    private final boolean windowed;
    private int attributeSize;
    private List<Integer> aggregateAttributePositionList;

    public WrappedSnapshotOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService, boolean isGroupBy, boolean isWindowed) {
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        groupBy = isGroupBy;
        windowed = isWindowed;
    }

    public void init() {
        if (attributeSize == 0) {
            throw new QueryCreationException("Output attribute size cannot be 0");
        }
        if (windowed) {
            if (groupBy) {
                if (attributeSize == aggregateAttributePositionList.size()) {   //All Aggregation
                    outputRateManager = new AllAggregationGroupByWindowedPerSnapshotOutputRateManager(value, scheduledExecutorService, this);
                } else if (aggregateAttributePositionList.size() > 0) {   //Some Aggregation
                    outputRateManager = new AggregationGroupByWindowedPerSnapshotOutputRateManager(value, scheduledExecutorService, aggregateAttributePositionList, this);
                } else { // No aggregation
                    //GroupBy is same as Non GroupBy
                    outputRateManager = new WindowedPerSnapshotOutputRateManager(value, scheduledExecutorService, this);
                }
            } else {
                if (attributeSize == aggregateAttributePositionList.size()) {   //All Aggregation
                    outputRateManager = new AllAggregationPerSnapshotOutputRateManager(value, scheduledExecutorService, this);
                } else if (aggregateAttributePositionList.size() > 0) {   //Some Aggregation
                    outputRateManager = new AggregationWindowedPerSnapshotOutputRateManager(value, scheduledExecutorService, aggregateAttributePositionList, this);
                } else { // No aggregation
                    outputRateManager = new WindowedPerSnapshotOutputRateManager(value, scheduledExecutorService, this);
                }
            }

        } else {
            if (groupBy) {
                outputRateManager = new GroupByPerSnapshotOutputRateManager(value, scheduledExecutorService, this);
            } else {
                outputRateManager = new PerSnapshotOutputRateManager(value, scheduledExecutorService, this);
            }

        }
    }


    public void setAggregateAttributePositionList(List<Integer> aggregateAttributePositionList) {
        this.aggregateAttributePositionList = aggregateAttributePositionList;
    }

    public void setAttributeSize(int attributeSize) {
        this.attributeSize = attributeSize;
    }

    @Override
    public void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        outputRateManager.send(timeStamp, currentEvent, expiredEvent, groupByKey);
    }

    public void passToCallBacks(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, StreamEvent allEvent) {
        sendToCallBacks(timeStamp, currentEvent, expiredEvent, allEvent);
    }

}
