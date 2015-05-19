/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

public class LinearRegressionForecastStreamProcessor extends StreamProcessor{

    private boolean constFlag = false;
    private int paramCount = 0;                                         // Number of x variables +1
    private int calcInterval = 1;                                       // The frequency of regression calculation
    private int batchSize = 1000000000;                                 // Maximum # of events, used for regression calculation
    private double ci = 0.95;                                           // Confidence Interval
    private final int SIMPLE_LINREG_INPUT_PARAM_COUNT = 2;              // Number of Input parameters in a simple linear forecast
    private RegressionCalculator regressionCalculator = null;
    private int paramPosition = 0;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (streamEventChunk.hasNext()) {
            ComplexEvent complexEvent = streamEventChunk.next();

            Object[] inputData = new Object[attributeExpressionLength-paramPosition];
            double xDash = ((Number) attributeExpressionExecutors[paramPosition-1].execute(complexEvent)).doubleValue();
            for (int i = paramPosition; i < attributeExpressionLength; i++) {
                inputData[i-paramPosition] = attributeExpressionExecutors[i].execute(complexEvent);
            }
            Object[] temp = regressionCalculator.calculateLinearRegression(inputData);
            Object[] outputData = new Object[temp.length+1];
            System.arraycopy(temp, 0, outputData, 0, temp.length);
            outputData[temp.length] = ((Number) temp[temp.length-2]).doubleValue() + ((Number) temp[temp.length-1]).doubleValue() * xDash;


            if (outputData == null) {
                streamEventChunk.remove();
            } else {
                complexEventPopulater.populateComplexEvent(complexEvent, outputData);
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        paramCount = attributeExpressionLength - 1;

        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor){
            constFlag = true;
            paramCount = paramCount - 4;
            paramPosition = 4;
            try {
                calcInterval = ((Integer)attributeExpressionExecutors[0].execute(null));
                batchSize = ((Integer)attributeExpressionExecutors[1].execute(null));
            } catch(ClassCastException c) {
                throw new ExecutionPlanCreationException("Calculation interval, batch size and range should be of type int");
            }
            try {
                ci = ((Double)attributeExpressionExecutors[2].execute(null));
            } catch(ClassCastException c) {
                throw new ExecutionPlanCreationException("Confidence interval should be of type double and a value between 0 and 1");
            }
        }

        if (paramCount > SIMPLE_LINREG_INPUT_PARAM_COUNT) {
            throw new ExecutionPlanCreationException("Forecast Function is available only for simple linear regression");
        } else {
            regressionCalculator = new SimpleLinearRegressionCalculator(paramCount, calcInterval, batchSize, ci);
        }

        String betaVal;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(paramCount);
        attributes.add(new Attribute("stderr", Attribute.Type.DOUBLE));

        for (int itr = 0; itr < paramCount; itr++) {
            betaVal = "beta" + itr;
            attributes.add(new Attribute(betaVal, Attribute.Type.DOUBLE));
        }
        attributes.add(new Attribute("forecastY", Attribute.Type.DOUBLE));
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
