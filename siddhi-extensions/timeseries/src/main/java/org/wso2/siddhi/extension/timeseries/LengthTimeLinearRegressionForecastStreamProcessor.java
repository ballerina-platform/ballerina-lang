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
package org.wso2.siddhi.extension.timeseries;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.extension.timeseries.linreg.LengthTimeRegressionCalculator;
import org.wso2.siddhi.extension.timeseries.linreg.LengthTimeSimpleLinearRegressionCalculator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import java.util.ArrayList;
import java.util.List;

/*
 * Sample Query1 (time window, length window, nextX, y, x):
 * from InputStream#timeseries:lengthTimeOutlier(20 min, 20, x+2, y, x)
 * select *
 * insert into OutputStream;
 *
 * Sample Query2 (time window, length window, nextX, calculation interval, confidence interval, y, x):
 * from InputStream#timeseries:lengthTimeOutlier(20 min, 20, x+2, 2, 0.9, y, x)
 * select *
 * insert into OutputStream;
 *
 * This class performs enables users to forecast future events using linear regression
 * Number of data points could be constrained using both time and length windows.
 */
public class LengthTimeLinearRegressionForecastStreamProcessor extends StreamProcessor {
    private int paramCount; // Number of x variables +1
    private long duration; // Time window to consider for regression calculation
    private int calcInterval = 1; // The frequency of regression calculation
    private double ci = 0.95; // Confidence Interval simple linear forecast
    private LengthTimeRegressionCalculator regressionCalculator = null;
    private int paramPosition;

    /**
     * The init method of the LinearRegressionOutlierStreamProcessor,
     * this method will be called before other methods
     *
     * @param inputDefinition the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param executionPlanContext the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext,
                                   boolean outputExpectsExpiredEvents) {
        paramCount = attributeExpressionLength -3; // First three events are time window, length
                                                   // window and x value for forecasting y
        paramPosition = 3;
        // Capture Constant inputs
        // Capture duration
        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                duration = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[0]).getValue();
            } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                duration = (Long) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[0]).getValue();
            } else {
                throw new ExecutionPlanCreationException(
                        "Time window's parameter attribute should be either int or long, but found "
                                + attributeExpressionExecutors[0].getReturnType());
            }
        } else {
            throw new ExecutionPlanCreationException("Time window must be a constant");
        }
        // Capture batchSize
        int batchSize; // Maximum # of events, used for regression calculation
        if (attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                batchSize = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[1]).getValue();
            } else {
                throw new ExecutionPlanCreationException
                        ("Length window's parameter attribute should be int, but found "
                                + attributeExpressionExecutors[1].getReturnType());
            }
        } else {
            throw new ExecutionPlanCreationException("Length window must be a constant");
        }
        // Capture calculation interval and ci if provided by user
        // Default values would be used otherwise
        if (attributeExpressionExecutors[3] instanceof ConstantExpressionExecutor) {
            paramCount = paramCount - 2; // When calcInterval and ci are given by user,
                                         // parameter count must exclude those two as well
            paramPosition = 5;
            if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                calcInterval = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[3]).getValue();
            } else {
                throw new ExecutionPlanCreationException
                        ("Calculation interval should be int, but found "
                                + attributeExpressionExecutors[3].getReturnType());
            }
            if (attributeExpressionExecutors[4] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.DOUBLE) {
                    ci = (Double) ((ConstantExpressionExecutor)
                            attributeExpressionExecutors[4]).getValue();
                    if (!(0 <= ci && ci <= 1)) {
                        throw new ExecutionPlanCreationException
                                ("Confidence interval should be a value between 0 and 1");
                    }
                } else {
                    throw new ExecutionPlanCreationException
                            ("Confidence interval should be double, but found "
                                    + attributeExpressionExecutors[4].getReturnType());
                }
            } else {
                throw new ExecutionPlanCreationException("Confidence interval must be a constant");
            }
        }
        // Pick the appropriate regression calculator
        final int SIMPLE_LINREG_INPUT_PARAM_COUNT; //Number of input parameters in simple
                                                   // linear regression
        SIMPLE_LINREG_INPUT_PARAM_COUNT = 2;
        if (paramCount > SIMPLE_LINREG_INPUT_PARAM_COUNT) {
            throw new ExecutionPlanCreationException(
                    "Forecast Function is available only for simple linear regression");
        } else {
            regressionCalculator = new LengthTimeSimpleLinearRegressionCalculator(paramCount,
                    duration, batchSize, calcInterval, ci);
        }
        // Create attributes for standard error and all beta values and the Forecast Y value
        String betaVal;
        List<Attribute> attributes = new ArrayList<Attribute>(paramCount + 2);
        attributes.add(new Attribute("stderr", Attribute.Type.DOUBLE));
        for (int itr = 0; itr < paramCount; itr++) {
            betaVal = "beta" + itr;
            attributes.add(new Attribute(betaVal, Attribute.Type.DOUBLE));
        }
        attributes.add(new Attribute("forecastY", Attribute.Type.DOUBLE));
        return attributes;
    }

    /**
     * The main processing method that will be called upon event arrival
     *
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or
     *                              modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner,
                           ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
                long eventExpiryTime = currentTime + duration;
                Object[] inputData = new Object[paramCount];
                // Obtain x value that user wants to use to forecast Y
                // This could be a constant or an expression
                final int xDashPosition;
                xDashPosition = 2;
                double xDash =
                        ((Number) attributeExpressionExecutors[xDashPosition].execute(streamEvent)).doubleValue();
                for (int i = paramPosition; i < attributeExpressionLength; i++) {
                    inputData[i - paramPosition] =
                            attributeExpressionExecutors[i].execute(streamEvent);
                }
                Object[] coefficients = regressionCalculator.calculateLinearRegression(inputData,
                        eventExpiryTime);
                if (coefficients == null) {
                    streamEventChunk.remove();
                } else {
                    Object[] outputData = new Object[coefficients.length + 1];
                    System.arraycopy(coefficients, 0, outputData, 0, coefficients.length);
                    // Calculating forecast Y based on regression equation and given x
                    outputData[coefficients.length] =
                            ((Number) coefficients[coefficients.length - 2]).doubleValue() +
                                    ((Number) coefficients[coefficients.length - 1]).doubleValue() * xDash;
                    complexEventPopulater.populateComplexEvent(streamEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    /**
     * This will be called only once and this can be used to acquire required resources for the
     * processing element.
     * This will be called after initializing the system and before starting to process the events.
     */
    @Override
    public void start() {

    }

    /**
     * This will be called only once and this can be used to release the acquired resources
     * for processing.
     * This will be called before shutting down the system.
     */
    @Override
    public void stop() {

    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for reconstructing the element to the same state at a different point of time
     *
     * @return stateful objects of the processing element as an array
     */
    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the element as an array on the same order provided
     *              by currentState().
     */
    @Override
    public void restoreState(Object[] state) {

    }
}
