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

package org.wso2.siddhi.extension.markov;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * StreamProcessor which implements the following function.
 * <code>markovChain( id, state, durationToKeep, alertThreshold, notificationsHoldLimit/markovMatrixStorageLocation, train )</code>
 * Returns last state, transition probability and notification.
 * Accept Type(s): STRING, STRING, INT/LONG/TIME, DOUBLE, (INT/LONG, STRING), BOOLEAN
 * Return Type: STRING, DOUBLE, BOOLEAN
 */
public class MarkovChainStreamProcessor extends StreamProcessor implements SchedulingProcessor {

    private enum TrainingMode {
        PREDEFINED_MATRIX, REAL_TIME
    }

    private Scheduler scheduler;
    private TrainingMode trainingMode;
    private long durationToKeep;
    private double alertThresholdProbability;
    private long notificationsHoldLimit;
    private String markovMatrixStorageLocation;
    private Boolean trainingOption;
    private ExpressionExecutor trainingOptionExpressionExecutor;
    private MarkovChainTransitionProbabilitiesCalculator markovChainTransitionProbabilitiesCalculator;
    private long lastScheduledTime;

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
            ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

        if (!(attributeExpressionExecutors.length == 5 || attributeExpressionExecutors.length == 6)) {
            throw new ExecutionPlanValidationException(
                    "Markov chain function has to have exactly 5 or 6 parameters, currently "
                            + attributeExpressionExecutors.length + " parameters provided.");
        }

        trainingOption = true;
        trainingMode = TrainingMode.REAL_TIME;

        if (!(attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor)) {
            throw new ExecutionPlanValidationException("Duration has to be a constant.");
        }

        if (!(attributeExpressionExecutors[3] instanceof ConstantExpressionExecutor)) {
            throw new ExecutionPlanValidationException("Alert threshold probability value has to be a constant.");
        }

        if (!(attributeExpressionExecutors[4] instanceof ConstantExpressionExecutor)) {
            throw new ExecutionPlanValidationException("Training batch size has to be a constant.");
        }

        Object durationObject = attributeExpressionExecutors[2].execute(null);
        if (durationObject instanceof Integer) {
            durationToKeep = (Integer) durationObject;
        } else if (durationObject instanceof Long) {
            durationToKeep = (Long) durationObject;
        } else {
            throw new ExecutionPlanValidationException("Duration should be of type int or long. But found "
                    + attributeExpressionExecutors[2].getReturnType());
        }

        Object alertThresholdProbabilityObject = attributeExpressionExecutors[3].execute(null);
        if (alertThresholdProbabilityObject instanceof Double) {
            alertThresholdProbability = (Double) alertThresholdProbabilityObject;
        } else {
            throw new ExecutionPlanValidationException(
                    "Alert threshold probability should be of type double. But found "
                            + attributeExpressionExecutors[3].getReturnType());
        }

        Object object = attributeExpressionExecutors[4].execute(null);
        if (object instanceof String) {
            markovMatrixStorageLocation = (String) object;
            trainingMode = TrainingMode.PREDEFINED_MATRIX;

            File file = new File(markovMatrixStorageLocation);
            if (!file.exists()) {
                throw new ExecutionPlanValidationException(
                        markovMatrixStorageLocation + " does not exist. Please provide a valid file path.");
            } else if (!file.isFile()) {
                throw new ExecutionPlanValidationException(
                        markovMatrixStorageLocation + " is not a file. Please provide a valid csv file.");
            }
        } else if (object instanceof Integer) {
            notificationsHoldLimit = (Integer) object;
        } else if (object instanceof Long) {
            notificationsHoldLimit = (Long) object;
        } else {
            throw new ExecutionPlanValidationException(
                    "5th parameter should be the Training batch size or Markov matrix storage location. "
                            + "They should be of types int/long or String. But found "
                            + attributeExpressionExecutors[4].getReturnType());
        }

        if (attributeExpressionExecutors.length == 6) {
            if (attributeExpressionExecutors[5] instanceof ConstantExpressionExecutor) {
                Object trainingOptionObject = attributeExpressionExecutors[5].execute(null);
                if (trainingOptionObject instanceof Boolean) {
                    trainingOption = (Boolean) trainingOptionObject;
                } else {
                    throw new ExecutionPlanValidationException("Training option should be of type boolean. But found "
                            + attributeExpressionExecutors[5].getReturnType());
                }
            } else {
                trainingOptionExpressionExecutor = attributeExpressionExecutors[5];
            }
        }

        if (trainingMode == TrainingMode.PREDEFINED_MATRIX) {
            markovChainTransitionProbabilitiesCalculator = new MarkovChainTransitionProbabilitiesCalculator(
                    durationToKeep, alertThresholdProbability, markovMatrixStorageLocation);
        } else {
            markovChainTransitionProbabilitiesCalculator = new MarkovChainTransitionProbabilitiesCalculator(
                    durationToKeep, alertThresholdProbability, notificationsHoldLimit);
        }

        List<Attribute> attributeList = new ArrayList<Attribute>(3);
        attributeList.add(new Attribute("lastState", Attribute.Type.STRING));
        attributeList.add(new Attribute("transitionProbability", Attribute.Type.DOUBLE));
        attributeList.add(new Attribute("notify", Attribute.Type.BOOL));
        return attributeList;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
            StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();

                if (streamEvent.getType() == ComplexEvent.Type.TIMER) {
                    markovChainTransitionProbabilitiesCalculator
                            .removeExpiredEvents(executionPlanContext.getTimestampGenerator().currentTime());
                    continue;
                } else if (streamEvent.getType() != ComplexEvent.Type.CURRENT) {
                    continue;
                }
                lastScheduledTime = executionPlanContext.getTimestampGenerator().currentTime() + durationToKeep;
                scheduler.notifyAt(lastScheduledTime);

                if (trainingOptionExpressionExecutor != null) {
                    trainingOption = (Boolean) attributeExpressionExecutors[5].execute(streamEvent);
                }
                String id = (String) attributeExpressionExecutors[0].execute(streamEvent);
                String state = (String) attributeExpressionExecutors[1].execute(streamEvent);
                Object[] outputData = markovChainTransitionProbabilitiesCalculator.processData(id, state,
                        trainingOption);

                if (outputData == null) {
                    streamEventChunk.remove();
                } else {
                    complexEventPopulater.populateComplexEvent(streamEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[] { markovChainTransitionProbabilitiesCalculator, trainingOption,
                trainingOptionExpressionExecutor, lastScheduledTime };
    }

    @Override
    public void restoreState(Object[] state) {
        markovChainTransitionProbabilitiesCalculator = (MarkovChainTransitionProbabilitiesCalculator) state[0];
        trainingOption = (Boolean) state[1];
        trainingOptionExpressionExecutor = (ExpressionExecutor) state[2];
        lastScheduledTime = (Long) state[3];
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

}
