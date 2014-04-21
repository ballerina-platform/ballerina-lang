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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.EventStreamNotExistException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.query.output.callback.DeleteTableCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoTableCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.UpdateTableCallback;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.PassThroughOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.event.AllPerEventOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.event.FirstGroupByPerEventOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.event.FirstPerEventOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.event.LastGroupByPerEventOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.event.LastPerEventOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.time.FirstGroupByPerTimeOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.time.FirstPerTimeOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.time.LastGroupByPerTimeOutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.time.LastPerTimeOutputRateManager;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.output.EventOutputRate;
import org.wso2.siddhi.query.api.query.output.OutputRate;
import org.wso2.siddhi.query.api.query.output.SnapshotOutputRate;
import org.wso2.siddhi.query.api.query.output.TimeOutputRate;
import org.wso2.siddhi.query.api.query.output.stream.DeleteStream;
import org.wso2.siddhi.query.api.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;
import org.wso2.siddhi.query.api.query.output.stream.UpdateStream;
import org.wso2.siddhi.query.api.query.selection.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

public class QueryOutputParser {
    public static QuerySelector constructQuerySelector(OutStream outStream, Selector selector, OutputRateManager outputRateManager, List<QueryEventSource> queryEventSourceList,
                                                       ConcurrentMap<String, EventTable> eventTableMap,
                                                       SiddhiContext siddhiContext) {
        boolean currentOn = false;
        boolean expiredOn = false;
        String id = null;

        if (outStream != null) {
            if (outStream.getOutputEventsFor() == OutStream.OutputEventsFor.CURRENT_EVENTS || outStream.getOutputEventsFor() == OutStream.OutputEventsFor.ALL_EVENTS) {
                currentOn = true;
            }
            if (outStream.getOutputEventsFor() == OutStream.OutputEventsFor.EXPIRED_EVENTS || outStream.getOutputEventsFor() == OutStream.OutputEventsFor.ALL_EVENTS) {
                expiredOn = true;
            }

            id = outStream.getStreamId();
        } else {
            currentOn = true;
            expiredOn = true;
        }

        return new QuerySelector(id, selector, outputRateManager, queryEventSourceList, eventTableMap, siddhiContext, currentOn, expiredOn);
    }

    public static OutputCallback constructOutputCallback(OutStream outStream, ConcurrentMap<String, StreamJunction> streamJunctionMap, ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext,
                                                         StreamDefinition outputStreamDefinition) {
        String id = outStream.getStreamId();
        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            if (eventTableMap.containsKey(id)) {
                return new InsertIntoTableCallback(eventTableMap.get(id), outputStreamDefinition);

            } else {
                StreamJunction outputStreamJunction = streamJunctionMap.get(id);
                if (outputStreamJunction == null) {
                    outputStreamJunction = new StreamJunction(id,siddhiContext.getEventMonitorService());
                    streamJunctionMap.putIfAbsent(id, outputStreamJunction);
                }
                return new InsertIntoStreamCallback(outputStreamJunction, outputStreamDefinition);
            }
        } else if (outStream instanceof DeleteStream) {
            if (eventTableMap.containsKey(id)) {
                Condition condition = ((DeleteStream) outStream).getCondition();
                EventTable eventTable = eventTableMap.get(id);
                ConditionExecutor conditionExecutor = null;
                if (condition != null) {
                    List<QueryEventSource> queryEventSourcesTemp = new ArrayList<QueryEventSource>();
                    String tempId = UUID.randomUUID().toString();
                    queryEventSourcesTemp.add(new QueryEventSource(null, tempId, outputStreamDefinition, null, null, null));
                    queryEventSourcesTemp.add(eventTable.getQueryEventSource());
                    conditionExecutor = ExecutorParser.parseCondition(condition, queryEventSourcesTemp, tempId, eventTableMap, true, siddhiContext);
                }
                return new DeleteTableCallback(eventTable, conditionExecutor);
            } else {
                throw new EventStreamNotExistException("No Event Table with name " + outStream.getStreamId() + " available ");
            }
        } else if (outStream instanceof UpdateStream) {
            if (eventTableMap.containsKey(id)) {
                Condition condition = ((UpdateStream) outStream).getCondition();
                EventTable eventTable = eventTableMap.get(id);
                ConditionExecutor conditionExecutor = null;
                if (condition != null) {
                    String tempId = UUID.randomUUID().toString();
                    List<QueryEventSource> queryEventSourcesTemp = new ArrayList<QueryEventSource>();
                    queryEventSourcesTemp.add(new QueryEventSource(null, tempId, outputStreamDefinition, null, null, null));
                    queryEventSourcesTemp.add(eventTable.getQueryEventSource());
                    conditionExecutor = ExecutorParser.parseCondition(condition, queryEventSourcesTemp, tempId, eventTableMap, true, siddhiContext);
                }
                return new UpdateTableCallback(eventTableMap.get(id), conditionExecutor, outputStreamDefinition);
            } else {
                throw new EventStreamNotExistException("No Event Table with name " + outStream.getStreamId() + " available ");
            }
        } else {
            throw new QueryCreationException(outStream.getClass().getName() + " not supported");
        }
    }

    public static OutputRateManager constructOutputRateManager(OutputRate outputRate, ScheduledExecutorService scheduledExecutorService, boolean isGroupby, boolean isWindowed) {
        if (outputRate == null) {
            return new PassThroughOutputRateManager();
        } else if (outputRate instanceof EventOutputRate) {
            switch (((EventOutputRate) outputRate).getType()) {
                case ALL:
                    return new AllPerEventOutputRateManager(((EventOutputRate) outputRate).getValue());
                case FIRST:
                    if (isGroupby) {
                        return new FirstGroupByPerEventOutputRateManager(((EventOutputRate) outputRate).getValue());
                    } else {
                        return new FirstPerEventOutputRateManager(((EventOutputRate) outputRate).getValue());
                    }
                case LAST:
                    if (isGroupby) {
                        return new LastGroupByPerEventOutputRateManager(((EventOutputRate) outputRate).getValue());
                    } else {
                        return new LastPerEventOutputRateManager(((EventOutputRate) outputRate).getValue());
                    }
            }
            //never happens
            return null;
        } else if (outputRate instanceof TimeOutputRate) {
            switch (((TimeOutputRate) outputRate).getType()) {
                case ALL:
                    return new org.wso2.siddhi.core.query.output.ratelimit.time.AllPerTimeOutputRateManager(((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                case FIRST:
                    if (isGroupby) {
                        return new FirstGroupByPerTimeOutputRateManager(((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    } else {
                        return new FirstPerTimeOutputRateManager(((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    }
                case LAST:
                    if (isGroupby) {
                        return new LastGroupByPerTimeOutputRateManager(((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    } else {
                        return new LastPerTimeOutputRateManager(((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    }
            }
            //never happens
            return null;
        } else {
            return new WrappedSnapshotOutputRateManager(((SnapshotOutputRate) outputRate).getValue(), scheduledExecutorService, isGroupby, isWindowed);
        }
    }
}
