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
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class ConvertFunctionExecutor extends FunctionExecutor {

    private Attribute.Type returnType;
    private Attribute.Type inputType;

    @Override
    public void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("convert() must have at 2 parameters, attribute and to be converted type");
        }
        inputType = attributeExpressionExecutors[0].getReturnType();
        if (inputType == Attribute.Type.OBJECT) {
            throw new ExecutionPlanValidationException("1st parameter of convert() cannot be 'object' as it's not supported, it has to be either of (STRING, INT, LONG, FLOAT, DOUBLE, BOOL), but found " + attributeExpressionExecutors[0].getReturnType());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("2nd parameter of convert() must be 'string' have constant value either of (STRING, INT, LONG, FLOAT, DOUBLE, BOOL), but found " + attributeExpressionExecutors[0].getReturnType());
        }
        if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
            throw new ExecutionPlanValidationException("2nd parameter of convert() must have constant value either of (STRING, INT, LONG, FLOAT, DOUBLE, BOOL), but found a variable expression");
        }
        String type = (String) attributeExpressionExecutors[1].execute(null);
        if (Attribute.Type.STRING.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.STRING;
        } else if (Attribute.Type.BOOL.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.BOOL;
        } else if (Attribute.Type.DOUBLE.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.DOUBLE;
        } else if (Attribute.Type.FLOAT.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.FLOAT;
        } else if (Attribute.Type.INT.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.INT;
        } else if (Attribute.Type.LONG.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.LONG;
        } else {
            throw new ExecutionPlanValidationException("2nd parameter of convert() must have value either of (STRING, INT, LONG, FLOAT, DOUBLE, BOOL), but found '" + type + "'");
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    protected Object execute(Object[] obj) {

        Object data = obj[0];
        try {
            switch (returnType) {
                case STRING:
                    return data.toString();
                case INT:
                    switch (inputType) {
                        case STRING:
                            return Integer.parseInt((String) data);
                        case INT:
                            return data;
                        case LONG:
                            return ((Long) data).intValue();
                        case FLOAT:
                            return ((Float) data).intValue();
                        case DOUBLE:
                            return ((Double) data).intValue();
                        case BOOL:
                            return ((Boolean) data) ? 1 : 0;
                        case OBJECT:
                            return null;
                    }
                    break;
                case LONG:
                    switch (inputType) {
                        case STRING:
                            return Long.parseLong((String) data);
                        case INT:
                            return ((Integer) data).longValue();
                        case LONG:
                            return data;
                        case FLOAT:
                            return ((Float) data).longValue();
                        case DOUBLE:
                            return ((Double) data).longValue();
                        case BOOL:
                            return ((Boolean) data) ? 1l : 0l;
                        case OBJECT:
                            return null;
                    }
                    break;
                case FLOAT:
                    switch (inputType) {
                        case STRING:
                            return Float.parseFloat((String) data);
                        case INT:
                            return ((Integer) data).floatValue();
                        case LONG:
                            return ((Long) data).floatValue();
                        case FLOAT:
                            return data;
                        case DOUBLE:
                            return ((Double) data).floatValue();
                        case BOOL:
                            return ((Boolean) data) ? 1f : 0f;
                        case OBJECT:
                            return null;
                    }
                    break;
                case DOUBLE:
                    switch (inputType) {
                        case STRING:
                            return Double.parseDouble((String) data);
                        case INT:
                            return ((Integer) data).doubleValue();
                        case LONG:
                            return ((Long) data).doubleValue();
                        case FLOAT:
                            return ((Float) data).doubleValue();
                        case DOUBLE:
                            return data;
                        case BOOL:
                            return ((Boolean) data) ? 1.0 : 0.0;
                        case OBJECT:
                            return null;
                    }
                    break;
                case BOOL:
                    switch (inputType) {
                        case STRING:
                            return Boolean.parseBoolean((String) data);
                        case INT:
                            return ((Integer) data) == 1;
                        case LONG:
                            return ((Long) data) == 1l;
                        case FLOAT:
                            return ((Float) data) == 1f;
                        case DOUBLE:
                            return ((Double) data) == 1.0;
                        case BOOL:
                            return data;
                        case OBJECT:
                            return null;
                    }
                    break;
                case OBJECT:
                    break;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;

    }

    @Override
    protected Object execute(Object data) {
        //will not occur
        return null;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }

    @Override
    public Object[] currentState() {
        //No states
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
        //Nothing to be done
    }
}
