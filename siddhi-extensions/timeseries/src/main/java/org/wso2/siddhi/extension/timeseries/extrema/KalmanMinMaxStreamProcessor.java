/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.timeseries.extrema;


import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.extension.timeseries.extrema.util.ExtremaCalculator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class KalmanMinMaxStreamProcessor extends StreamProcessor {

    public enum ExtremaType {
        MIN, MAX, MINMAX
    }

    ExtremaType extremaType;
    private int[] variablePosition;
    private int windowSize = 0;
    private LinkedList<StreamEvent> eventStack = null;
    private Queue<Double> valueStack = null;
    private Queue<StreamEvent> uniqueQueue = null;
    private double Q;
    private double R;
    private int minEventPos;
    private int maxEventPos;
    ExtremaCalculator extremaCalculator = null;

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 5) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to KalmanMinMaxStreamProcessor, required 5, but found " + attributeExpressionExecutors.length);
        }

        if (!(attributeExpressionExecutors[0].getReturnType() == Attribute.Type.DOUBLE || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT
                || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.FLOAT || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG)) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 1st argument of KalmanMinMaxStreamProcessor, " +
                    "required " + Attribute.Type.DOUBLE + " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.INT + " or " +
                    Attribute.Type.LONG + " but found " + attributeExpressionExecutors[0].getReturnType().toString());
        }
        variablePosition = ((VariableExpressionExecutor) attributeExpressionExecutors[0]).getPosition();

        try {
            Q = Double.parseDouble(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
        } catch (NumberFormatException e) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 2nd argument of KalmanMinMaxStreamProcessor " +
                    "required " + Attribute.Type.DOUBLE + " constant, but found " + attributeExpressionExecutors[1].getReturnType().toString());
        }
        try {
            R = Double.parseDouble(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue()));
        } catch (NumberFormatException e) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 3rd argument of KalmanMinMaxStreamProcessor " +
                    "required " + Attribute.Type.DOUBLE + " constant, but found " + attributeExpressionExecutors[2].getReturnType().toString());
        }
        try {
            windowSize = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
        } catch (NumberFormatException e) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 4th argument of KalmanMinMaxStreamProcessor " +
                    "required " + Attribute.Type.INT + " constant, but found " + attributeExpressionExecutors[3].getReturnType().toString());
        }

        String extremeType = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[4]).getValue();

        if ("min".equalsIgnoreCase(extremeType)) {
            extremaType = ExtremaType.MIN;
        } else if ("max".equalsIgnoreCase(extremeType)) {
            extremaType = ExtremaType.MAX;
        } else {
            extremaType = ExtremaType.MINMAX;
        }
        eventStack = new LinkedList<StreamEvent>();
        valueStack = new LinkedList<Double>();
        uniqueQueue = new LinkedList<StreamEvent>();

        List<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute("extremaType", Attribute.Type.STRING));
        return attributeList;

    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner,
                           ComplexEventPopulater complexEventPopulater) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        synchronized (this) {
            while (streamEventChunk.hasNext()) {

                StreamEvent event = streamEventChunk.next();
                streamEventChunk.remove();
                Double eventKey = (Double) event.getAttribute(variablePosition);
                extremaCalculator = new ExtremaCalculator(Q, R);
                eventStack.add(event);
                valueStack.add(eventKey);

                if (eventStack.size() > windowSize) {

                    Queue<Double> output = extremaCalculator.kalmanFilter(valueStack);
                    StreamEvent maximumEvent;
                    StreamEvent minimumEvent;

                    switch (extremaType) {
                        case MINMAX:
                            maximumEvent = getMaxEvent(output);
                            minimumEvent = getMinEvent(output);
                            if (maximumEvent != null && minimumEvent != null) {
                                if (maxEventPos > minEventPos) {
                                    returnEventChunk.add(minimumEvent);
                                    returnEventChunk.add(maximumEvent);
                                } else {
                                    returnEventChunk.add(maximumEvent);
                                    returnEventChunk.add(minimumEvent);
                                }
                            } else if (maximumEvent != null) {
                                returnEventChunk.add(maximumEvent);
                            } else if (minimumEvent != null) {
                                returnEventChunk.add(minimumEvent);
                            }
                            break;
                        case MIN:
                            minimumEvent = getMinEvent(output);
                            if (minimumEvent != null) {
                                returnEventChunk.add(minimumEvent);
                            }
                            break;
                        case MAX:
                            maximumEvent = getMaxEvent(output);
                            if (maximumEvent != null) {
                                returnEventChunk.add(maximumEvent);
                            }
                            break;
                    }
                    eventStack.remove();
                    valueStack.remove();
                }
            }
        }
        if (returnEventChunk.getFirst() != null) {
            nextProcessor.process(returnEventChunk);
        }
    }

    private StreamEvent getMinEvent(Queue<Double> output) {
        // value 2 is an optimized value for stock market domain, this value may change for other domains
        Integer smoothenedMinEventPosition = extremaCalculator.findMin(output, 2);
        if (smoothenedMinEventPosition != null) {
            //value 10 is an optimized value for stock market domain, this value may change for other domains
            Integer minEventPosition = extremaCalculator.findMin(valueStack, 10);
            if (minEventPosition != null) {
                StreamEvent returnMinimumEvent = getExtremaEvent(minEventPosition);
                if (returnMinimumEvent != null) {
                    minEventPos = minEventPosition;
                    complexEventPopulater.populateComplexEvent(returnMinimumEvent, new Object[]{"min"});
                    return returnMinimumEvent;
                }
            }
        }
        return null;
    }

    private StreamEvent getMaxEvent(Queue<Double> output) {
        // value 2 is an optimized value for stock market domain, this value may change for other domains
        Integer smoothenedMaxEventPosition = extremaCalculator.findMax(output, 2);
        if (smoothenedMaxEventPosition != null) {
            //value 10 is an optimized value for stock market domain, this value may change for other domains
            Integer maxEventPosition = extremaCalculator.findMax(valueStack, 10);
            if (maxEventPosition != null) {
                StreamEvent returnMaximumEvent = getExtremaEvent(maxEventPosition);
                if (returnMaximumEvent != null) {
                    maxEventPos = maxEventPosition;
                    complexEventPopulater.populateComplexEvent(returnMaximumEvent, new Object[]{"max"});
                    return returnMaximumEvent;
                }
            }
        }
        return null;
    }


    private StreamEvent getExtremaEvent(Integer eventPosition) {
        StreamEvent extremaEvent = eventStack.get(eventPosition);
        if (!uniqueQueue.contains(extremaEvent)) {
            //value 5 is an optimized value for stock market domain, this value may change for other domains
            if (uniqueQueue.size() > 5) {
                uniqueQueue.remove();
            }
            uniqueQueue.add(extremaEvent);
            return streamEventCloner.copyStreamEvent(extremaEvent);
        }
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[]{eventStack, valueStack, uniqueQueue};
    }

    @Override
    public void restoreState(Object[] state) {
        eventStack = (LinkedList<StreamEvent>) state[0];
        valueStack = (Queue<Double>) state[1];
        uniqueQueue = (Queue<StreamEvent>) state[2];
    }


}
