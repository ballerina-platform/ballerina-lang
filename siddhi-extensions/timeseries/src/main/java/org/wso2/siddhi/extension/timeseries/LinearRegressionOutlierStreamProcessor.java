/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.extension.timeseries.linreg.RegressionCalculator;
import org.wso2.siddhi.extension.timeseries.linreg.SimpleLinearRegressionCalculator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

public class LinearRegressionOutlierStreamProcessor extends StreamProcessor {

    private int paramCount = 0;                                         // Number of x variables +1
    private int calcInterval = 1;                                       // The frequency of regression calculation
    private int batchSize = 1000000000;                                 // Maximum # of events, used for regression calculation
    private double ci = 0.95;                                           // Confidence Interval
    private final int SIMPLE_LINREG_INPUT_PARAM_COUNT = 2;              // Number of Input parameters in a simple linear forecast
    private RegressionCalculator regressionCalculator = null;
    private int paramPosition = 1;
    private Object[] coefficients;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                Boolean result = false; // Becomes true if its an outlier

                Object[] inputData = new Object[attributeExpressionLength - paramPosition];
                double range = ((Number) attributeExpressionExecutors[paramPosition - 1].execute(complexEvent)).doubleValue();

                for (int i = paramPosition; i < attributeExpressionLength; i++) {
                    inputData[i - paramPosition] = attributeExpressionExecutors[i].execute(complexEvent);
                }

                if (coefficients != null) {
                    // Get the current Y value and X value
                    double nextY = ((Number) inputData[0]).doubleValue();
                    double nextX = ((Number) inputData[1]).doubleValue();

                    // Get the last computed regression coefficients
                    double stdError = ((Number) coefficients[0]).doubleValue();
                    double beta0 = ((Number) coefficients[1]).doubleValue();
                    double beta1 = ((Number) coefficients[2]).doubleValue();

                    // Forecast Y based on current coefficients and next X value
                    double forecastY = beta0 + beta1 * nextX;

                    // Create the normal range based on user provided range parameter and current std error
                    double upLimit = forecastY + range * stdError;
                    double downLimit = forecastY - range * stdError;

                    // Check whether next Y value is an outlier based on the next X value and the current regression equation
                    if (nextY < downLimit || nextY > upLimit) {
                        result = true;
                    }
                }
                // Perform regression including X and Y of current event
                coefficients = regressionCalculator.calculateLinearRegression(inputData);

                if (coefficients == null) {
                    streamEventChunk.remove();
                } else {
                    Object[] outputData = new Object[coefficients.length + 1];
                    System.arraycopy(coefficients, 0, outputData, 0, coefficients.length);
                    outputData[coefficients.length] = result;
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        paramCount = attributeExpressionLength - 1;

        if (attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
            paramCount = paramCount - 3;
            paramPosition = 4;
            try {
                calcInterval = ((Integer) attributeExpressionExecutors[0].execute(null));
                batchSize = ((Integer) attributeExpressionExecutors[1].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Calculation interval, batch size and range should be of type int");
            }
            try {
                ci = ((Double) attributeExpressionExecutors[2].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Confidence interval should be of type double");
            }
            if (!(0<=ci && ci<=1)){
                throw new ExecutionPlanCreationException("Confidence interval should be a value between 0 and 1");
            }
        }

        // Pick the appropriate regression calculator
        if (paramCount > SIMPLE_LINREG_INPUT_PARAM_COUNT) {
            throw new ExecutionPlanCreationException("Outlier Function is available only for simple linear regression");
        } else {
            regressionCalculator = new SimpleLinearRegressionCalculator(paramCount, calcInterval, batchSize, ci);
        }

        // Create attributes for standard error and all beta values and the outlier result
        String betaVal;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(paramCount + 1);
        attributes.add(new Attribute("stderr", Attribute.Type.DOUBLE));

        for (int itr = 0; itr < paramCount; itr++) {
            betaVal = "beta" + itr;
            attributes.add(new Attribute(betaVal, Attribute.Type.DOUBLE));
        }
        attributes.add(new Attribute("outlier", Attribute.Type.BOOL));
        return attributes;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
