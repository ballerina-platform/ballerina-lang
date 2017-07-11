/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.processor.stream.window;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link WindowProcessor} which represent a Window operating based on event frequency.
 */
@Extension(
        name = "lossyFrequent",
        namespace = "",
        description = "This window identifies and returns all the events of which the current frequency exceeds " +
                "the value specified for the supportThreshold parameter.",
        parameters = {
                @Parameter(name = "support.threshold",
                        description = "The support threshold value.",
                        type = {DataType.DOUBLE}),
                @Parameter(name = "error.bound",
                        description = "The error bound value.",
                        type = {DataType.DOUBLE}),
                @Parameter(name = "attribute",
                        description = "The attributes to group the events. If no attributes are given, " +
                                "the concatenation of all the attributes of the event is considered.",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "The concatenation of all the attributes of the event is considered.")
        },
        examples = {
                @Example(
                        syntax = "define stream purchase (cardNo string, price float);\n" +
                                "define window purchaseWindow (cardNo string, price float) lossyFrequent(0.1, " +
                                "0.01);\n" +
                                "@info(name = 'query0')\n" +
                                "from purchase[price >= 30]\n" +
                                "insert into purchaseWindow;\n" +
                                "@info(name = 'query1')\n" +
                                "from purchaseWindow\n" +
                                "select cardNo, price\n" +
                                "insert all events into PotentialFraud;",
                        description = "lossyFrequent(0.1, 0.01) returns all the events of which the current " +
                                "frequency exceeds 0.1, with an error bound of 0.01."
                ),
                @Example(
                        syntax = "define stream purchase (cardNo string, price float);\n" +
                                "define window purchaseWindow (cardNo string, price float) lossyFrequent(0.3, 0.05," +
                                " cardNo);\n" +
                                "@info(name = 'query0')\n" +
                                "from purchase[price >= 30]\n" +
                                "insert into purchaseWindow;\n" +
                                "@info(name = 'query1')\n" +
                                "from purchaseWindow\n" +
                                "select cardNo, price\n" +
                                "insert all events into PotentialFraud;",
                        description = "lossyFrequent(0.3, 0.05, cardNo) returns all the events of which the cardNo " +
                                "attributes frequency exceeds 0.3, with an error bound of 0.05."
                )
        }
)
public class LossyFrequentWindowProcessor extends WindowProcessor implements FindableProcessor {
    private static final Logger log = Logger.getLogger(LossyFrequentWindowProcessor.class);
    private ConcurrentHashMap<String, LossyCount> countMap = new ConcurrentHashMap<String, LossyCount>();
    private ConcurrentHashMap<String, StreamEvent> map = new ConcurrentHashMap<String, StreamEvent>();
    private VariableExpressionExecutor[] variableExpressionExecutors;

    private int totalCount = 0;
    private double currentBucketId = 1;
    private double support;           // these will be initialize during init
    private double error;             // these will be initialize during init
    private double windowWidth;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader, boolean
            outputExpectsExpiredEvents, SiddhiAppContext siddhiAppContext) {
        support = Double.parseDouble(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                .getValue()));
        if (attributeExpressionExecutors.length > 1) {
            error = Double.parseDouble(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1])
                    .getValue()));
        } else {
            error = support / 10; // recommended error is 10% of 20$ of support value;
        }
        if ((support > 1 || support < 0) || (error > 1 || error < 0)) {
            log.error("Wrong argument has provided, Error executing the window");
        }
        variableExpressionExecutors = new VariableExpressionExecutor[attributeExpressionExecutors.length - 2];
        if (attributeExpressionExecutors.length > 2) {  // by-default all the attributes will be compared
            for (int i = 2; i < attributeExpressionExecutors.length; i++) {
                variableExpressionExecutors[i - 2] = (VariableExpressionExecutor) attributeExpressionExecutors[i];
            }
        }
        windowWidth = Math.ceil(1 / error);
        currentBucketId = 1;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner) {

        synchronized (this) {
            long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();

            StreamEvent streamEvent = streamEventChunk.getFirst();
            streamEventChunk.clear();
            while (streamEvent != null) {
                StreamEvent next = streamEvent.getNext();
                streamEvent.setNext(null);

                StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
                clonedEvent.setType(StreamEvent.Type.EXPIRED);

                totalCount++;
                if (totalCount != 1) {
                    currentBucketId = Math.ceil(totalCount / windowWidth);
                }
                String currentKey = generateKey(streamEvent);
                StreamEvent oldEvent = map.put(currentKey, clonedEvent);
                if (oldEvent != null) {    // this event is already in the store
                    countMap.put(currentKey, countMap.get(currentKey).incrementCount());
                } else {
                    //  This is a new event
                    LossyCount lCount;
                    lCount = new LossyCount(1, (int) currentBucketId - 1);
                    countMap.put(currentKey, lCount);
                }
                // calculating all the events in the system which match the
                // requirement provided by the user
                List<String> keys = new ArrayList<String>();
                keys.addAll(countMap.keySet());
                for (String key : keys) {
                    LossyCount lossyCount = countMap.get(key);
                    if (lossyCount.getCount() >= ((support - error) * totalCount)) {
                        // among the selected events, if the newly arrive event is there we mark it as an inEvent
                        if (key.equals(currentKey)) {
                            streamEventChunk.add(streamEvent);
                        }
                    }
                }
                if (totalCount % windowWidth == 0) {
                    // its time to run the data-structure prune code
                    keys = new ArrayList<String>();
                    keys.addAll(countMap.keySet());
                    for (String key : keys) {
                        LossyCount lossyCount = countMap.get(key);
                        if (lossyCount.getCount() + lossyCount.getBucketId() <= currentBucketId) {
                            log.info("Removing the Event: " + key + " from the window");
                            countMap.remove(key);
                            StreamEvent expirtedEvent = map.remove(key);
                            expirtedEvent.setTimestamp(currentTime);
                            streamEventChunk.add(expirtedEvent);
                        }
                    }
                }
                streamEvent = next;
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }


    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("CountMap", countMap);
        }
        return state;
    }


    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        countMap = (ConcurrentHashMap<String, LossyCount>) state.get("CountMap");
    }

    private String generateKey(StreamEvent event) {      // for performance reason if its all attribute we don't do
        // the attribute list check
        StringBuilder stringBuilder = new StringBuilder();
        if (variableExpressionExecutors.length == 0) {
            for (Object data : event.getOutputData()) {
                stringBuilder.append(data);
            }
        } else {
            for (VariableExpressionExecutor executor : variableExpressionExecutors) {
                stringBuilder.append(event.getAttribute(executor.getPosition()));
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((Operator) compiledCondition).find(matchingEvent, map.values(), streamEventCloner);
    }

    @Override
    public CompiledCondition compileCondition(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        return OperatorParser.constructOperator(map.values(), expression, matchingMetaInfoHolder,
                siddhiAppContext, variableExpressionExecutors, tableMap, this.queryName);
    }

    /**
     * Inner class to keep the lossy count
     */
    public class LossyCount {
        int count;
        int bucketId;

        public LossyCount(int count, int bucketId) {
            this.count = count;
            this.bucketId = bucketId;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getBucketId() {
            return bucketId;
        }

        public void setBucketId(int bucketId) {
            this.bucketId = bucketId;
        }

        public LossyCount incrementCount() {
            this.count++;
            return this;
        }
    }
}
