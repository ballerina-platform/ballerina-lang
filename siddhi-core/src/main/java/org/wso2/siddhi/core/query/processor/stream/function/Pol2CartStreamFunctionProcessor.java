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

package org.wso2.siddhi.core.query.processor.stream.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 1/26/15.
 */
public class Pol2CartStreamFunctionProcessor extends StreamFunctionProcessor {


    private int inputExecutorLength;

    /**
     * The init method of the StreamFunction
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors for the function parameters
     * @param executionPlanContext         execution plan context
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        inputExecutorLength = attributeExpressionExecutors.length;

        if (inputExecutorLength < 2 || inputExecutorLength > 3) {
            throw new ExecutionPlanValidationException("Input parameters for poleCart can either be 'theta,rho' or 'theta,rho,z', but " +
                    attributeExpressionExecutors.length + " attributes found");
        }
        for (int i = 0; i < inputExecutorLength; i++) {
            ExpressionExecutor expressionExecutor = attributeExpressionExecutors[i];
            if (expressionExecutor.getReturnType() != Attribute.Type.DOUBLE) {
                throw new ExecutionPlanValidationException("Input attribute " + i + " is expected to return Double, but its returning " + expressionExecutor.getReturnType());
            }

        }
        if (attributeExpressionExecutors.length == 2) {
            for (Attribute attribute : inputDefinition.getAttributeList()) {
                if (attribute.getName().equals("x") || attribute.getName().equals("y")) {
                    throw new ExecutionPlanValidationException("Input stream " + inputDefinition.getId() + " should not contain attributes with name 'x' or 'y', but found " + attribute);
                }
            }
            return Arrays.asList(new Attribute("x", Attribute.Type.DOUBLE), new Attribute("y", Attribute.Type.DOUBLE));
        } else {
            for (Attribute attribute : inputDefinition.getAttributeList()) {
                if (attribute.getName().equals("x") || attribute.getName().equals("y") || attribute.getName().equals("z")) {
                    throw new ExecutionPlanValidationException("Input stream " + inputDefinition.getId() + " should not contain attributes with name 'x' or 'y' or 'z', but found " + attribute);
                }
            }
            return Arrays.asList(new Attribute("x", Attribute.Type.DOUBLE), new Attribute("y", Attribute.Type.DOUBLE), new Attribute("z", Attribute.Type.DOUBLE));
        }
    }

    /**
     * The process method of the StreamFunction, used when multiple function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the date for additional output attributes introduced by the function
     */
    @Override
    protected Object[] process(Object[] data) {

        double theta = (Double) data[0];
        double rho = (Double) data[1];
        Object[] output = new Object[inputExecutorLength];
        output[0] = rho * Math.cos(Math.toRadians(theta));
        output[1] = rho * Math.sin(Math.toRadians(theta));
        if (inputExecutorLength == 3) {
            output[2] = data[2];
        }
        return output;
    }

    /**
     * The process method of the StreamFunction, used when single function parameter is provided
     *
     * @param data the data value for the function parameter
     * @return the date for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        throw new IllegalStateException("pol2Cart cannot execute for single data " + data);
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
    public Object[] currentState() {
        //No state
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
        //Nothing to be done
    }
}
