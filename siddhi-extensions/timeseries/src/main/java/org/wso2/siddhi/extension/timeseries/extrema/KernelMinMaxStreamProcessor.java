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

public class KernelMinMaxStreamProcessor extends StreamProcessor {

    public enum ExtremaType {
        MIN, MAX, MINMAX
    }

    ExtremaType extremaType;
    int[] variablePosition;
    double bw = 0;
    int windowSize = 0;
    LinkedList<StreamEvent> eventStack = null;
    Queue<Double> valueStack = null;
    Queue<StreamEvent> uniqueQueue = null;
    private int minEventPos;
    private int maxEventPos;
    ExtremaCalculator extremaCalculator = null;

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length != 4) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to KernelMinMaxStreamProcessor, required 4, " +
                    "but found " + attributeExpressionExecutors.length);
        }
        if (!(attributeExpressionExecutors[0].getReturnType() == Attribute.Type.DOUBLE || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT
                || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.FLOAT || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG)) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 1st argument of KernelMinMaxStreamProcessor, " +
                    "required " + Attribute.Type.DOUBLE + " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.INT + " or " +
                    Attribute.Type.LONG + " but found " + attributeExpressionExecutors[0].getReturnType().toString());
        }

        try {
            bw = Double.parseDouble(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
        } catch (NumberFormatException e) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 2nd argument of KernelMinMaxStreamProcessor " +
                    "required " + Attribute.Type.DOUBLE + " constant, but found " + attributeExpressionExecutors[1].getReturnType().toString());
        }

        if (!(attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor) || attributeExpressionExecutors[2].getReturnType() != Attribute.Type.INT) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 3rd argument of KernelMinMaxStreamProcessor, " +
                    "required " + Attribute.Type.INT + " constant, but found " + attributeExpressionExecutors[2].getReturnType().toString());
        }
        if (!(attributeExpressionExecutors[3] instanceof ConstantExpressionExecutor) || attributeExpressionExecutors[3].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the 4th argument of KernelMinMaxStreamProcessor, " +
                    "required " + Attribute.Type.STRING + " constant, but found " + attributeExpressionExecutors[2].getReturnType().toString());
        }

        variablePosition = ((VariableExpressionExecutor) attributeExpressionExecutors[0]).getPosition();
        windowSize = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue()));
        String extremeType = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue();

        if ("min".equalsIgnoreCase(extremeType)) {
            extremaType = ExtremaType.MIN;
        } else if ("max".equalsIgnoreCase(extremeType)) {
            extremaType = ExtremaType.MAX;
        } else {
            extremaType = ExtremaType.MINMAX;
        }
        extremaCalculator = new ExtremaCalculator();
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
                eventStack.add(event);
                valueStack.add(eventKey);

                if (eventStack.size() > windowSize) {
                    Queue<Double> smoothedValues = extremaCalculator.smooth(valueStack, bw);
                    StreamEvent minimumEvent;
                    StreamEvent maximumEvent;

                    switch (extremaType) {
                        case MINMAX:
                            maximumEvent = getMaxEvent(smoothedValues);
                            minimumEvent = getMinEvent(smoothedValues);
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
                            minimumEvent = getMinEvent(smoothedValues);
                            if (minimumEvent != null) {
                                returnEventChunk.add(minimumEvent);
                            }
                            break;
                        case MAX:
                            maximumEvent = getMaxEvent(smoothedValues);
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

    private StreamEvent getMaxEvent(Queue<Double> smoothedValues) {
        //value 1 is an optimized value for stock market domain, this value may change for other domains
        Integer maxPosition = extremaCalculator.findMax(smoothedValues, 1);
        if (maxPosition != null) {
            //values 5 and 3 are optimized values for stock market domain, these value may change for other domains
            Integer maxEventPosition = extremaCalculator.findMax(valueStack, windowSize / 5, windowSize / 3);
            StreamEvent returnMaximumEvent = getExtremaEvent(maxPosition, maxEventPosition);
            if (returnMaximumEvent != null) {
                maxEventPos = maxEventPosition;
                complexEventPopulater.populateComplexEvent(returnMaximumEvent, new Object[]{"max"});
                return returnMaximumEvent;
            }
        }
        return null;
    }

    private StreamEvent getMinEvent(Queue<Double> smoothedValues) {
        //value 1 is an optimized value for stock market domain, this value may change for other domains
        Integer minPosition = extremaCalculator.findMin(smoothedValues, 1);
        if (minPosition != null) {
            //values 5 and 3 are optimized values for stock market domain, these value may change for other domains
            Integer minEventPosition = extremaCalculator.findMin(valueStack, windowSize / 5, windowSize / 3);
            StreamEvent returnMinimumEvent = getExtremaEvent(minPosition, minEventPosition);
            if (returnMinimumEvent != null) {
                minEventPos = minEventPosition;
                complexEventPopulater.populateComplexEvent(returnMinimumEvent, new Object[]{"min"});
                return returnMinimumEvent;
            }
        }
        return null;
    }

    private StreamEvent getExtremaEvent(Integer smoothenedPosition, Integer eventPosition) {
        //values 5 and 3 are optimized values for stock market domain, these value may change for other domains
        if (eventPosition != null && eventPosition - smoothenedPosition <= windowSize / 5 && smoothenedPosition - eventPosition <= windowSize / 2) {
            StreamEvent extremaEvent = eventStack.get(eventPosition);
            if (!uniqueQueue.contains(extremaEvent)) {
                //value 5 is an optimized value for stock market domain, this value may change for other domains
                if (uniqueQueue.size() > 5) {
                    uniqueQueue.remove();
                }
                uniqueQueue.add(extremaEvent);
                eventStack.remove();
                valueStack.remove();
                return streamEventCloner.copyStreamEvent(extremaEvent);
            }
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
