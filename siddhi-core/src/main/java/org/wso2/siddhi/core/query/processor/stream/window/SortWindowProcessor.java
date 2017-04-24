/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.processor.stream.window;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.*;

/**
 * Sample Query:
 * from inputStream#window.sort(5, attribute1, "asc", attribute2, "desc")
 * select attribute1, attribute2
 * insert into outputStream;
 * <p>
 * Description:
 * In the example query given, 5 is the size of the window.
 * The arguments following the size of the window are optional.
 * If neither "asc" nor "desc" is given for a certain attribute, order defaults to "asc"
 */
@Extension(
        name = "sort",
        namespace = "",
        description = "This window holds a batch of events that equal the number specified as the windowLength " +
                "and sorts them in the given order.",
        parameters = {
                @Parameter(name = "windowLength",
                        description = "The size of the window length.",
                        type = {DataType.INT}),
                @Parameter(name = "attribute",
                        description = "The attribute that should be checked for the order.",
                        type = {DataType.STRING},
                        optional = true),
                @Parameter(name = "order",
                        description = "The order define as \"asc\" or \"desc\".",
                        type = {DataType.STRING},
                        optional = true)
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns current and expired events.",
                type = {})
)
public class SortWindowProcessor extends WindowProcessor implements FindableProcessor {
    private static final String ASC = "asc";
    private static final String DESC = "desc";
    private int lengthToKeep;
    private ArrayList<StreamEvent> sortedWindow = new ArrayList<StreamEvent>();
    private ArrayList<Object[]> parameterInfo;
    private EventComparator eventComparator;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
            lengthToKeep = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue()));
        } else {
            throw new UnsupportedOperationException("The first parameter should be an integer");
        }
        parameterInfo = new ArrayList<Object[]>();
        eventComparator = new EventComparator();
        for (int i = 1, parametersLength = attributeExpressionExecutors.length; i < parametersLength; i++) {
            if (!(attributeExpressionExecutors[i] instanceof VariableExpressionExecutor)) {
                throw new UnsupportedOperationException("Required a variable, but found a string parameter");
            } else {
                ExpressionExecutor variableExpressionExecutor = attributeExpressionExecutors[i];
                int order;
                String nextParameter;
                if (i + 1 < parametersLength && attributeExpressionExecutors[i + 1].getReturnType() == Attribute.Type.STRING) {
                    nextParameter = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[i + 1]).getValue();
                    if (nextParameter.equalsIgnoreCase(DESC)) {
                        order = -1;
                        i++;
                    } else if (nextParameter.equalsIgnoreCase(ASC)) {
                        order = 1;
                        i++;
                    } else {
                        throw new UnsupportedOperationException("Parameter string literals should only be \"asc\" or \"desc\"");
                    }
                } else {
                    order = 1; //assigning the default order: "asc"
                }
                parameterInfo.add(new Object[]{variableExpressionExecutor, order});
            }
        }

    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {

        synchronized (this) {
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();

            StreamEvent streamEvent = streamEventChunk.getFirst();
            streamEventChunk.clear();
            while (streamEvent != null) {
                StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
                clonedEvent.setType(StreamEvent.Type.EXPIRED);

                StreamEvent next = streamEvent.getNext();
                streamEvent.setNext(null);
                streamEventChunk.add(streamEvent);

                sortedWindow.add(clonedEvent);
                if (sortedWindow.size() > lengthToKeep) {
                    Collections.sort(sortedWindow, eventComparator);
                    StreamEvent expiredEvent = sortedWindow.remove(sortedWindow.size() - 1);
                    expiredEvent.setTimestamp(currentTime);
                    streamEventChunk.add(expiredEvent);
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
        state.put("SortedWindow", sortedWindow);
        return state;
    }


    @Override
    public void restoreState(Map<String, Object> state) {
        sortedWindow = (ArrayList<StreamEvent>) state.get("SortedWindow");
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((Operator) compiledCondition).find(matchingEvent, sortedWindow, streamEventCloner);
    }

    @Override
    public CompiledCondition compileCondition(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder, ExecutionPlanContext executionPlanContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, String queryName) {
        return OperatorParser.constructOperator(sortedWindow, expression, matchingMetaInfoHolder, executionPlanContext, variableExpressionExecutors, eventTableMap, this.queryName);
    }

    private class EventComparator implements Comparator<StreamEvent> {
        @Override
        public int compare(StreamEvent e1, StreamEvent e2) {
            int comparisonResult;
            for (Object[] listItem : parameterInfo) {
                int[] variablePosition = ((VariableExpressionExecutor) listItem[0]).getPosition();
                Comparable comparableVariable1 = (Comparable) e1.getAttribute(variablePosition);
                Comparable comparableVariable2 = (Comparable) e2.getAttribute(variablePosition);
                comparisonResult = comparableVariable1.compareTo(comparableVariable2);
                if (comparisonResult != 0) {
                    return ((Integer) listItem[1]) * comparisonResult;
                }
            }
            return 0;
        }
    }
}
