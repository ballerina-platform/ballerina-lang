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
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * cast(Object value, String type)
 * Returns the value cast to the type specified
 * Accept Type(s): Object , String
 * Return Type(s): (Int,Long,Float,Double,String,Bool)
 */
public class CastFunctionExecutor extends FunctionExecutor {
    private Attribute.Type returnType = Attribute.Type.OBJECT;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to common:cast() function, " +
                    "required 2 parameters, but found " + attributeExpressionExecutors.length);
        }
        if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
            throw new ExecutionPlanValidationException("The second argument has to be a string constant specifying " +
                    "one of the supported data types (int, long, float, double, string, bool)");
        } else {
            String type = attributeExpressionExecutors[1].execute(null).toString();
            if (type.toLowerCase().equals("int")) {
                returnType = Attribute.Type.INT;
            } else if (type.toLowerCase().equals("long")) {
                returnType = Attribute.Type.LONG;
            } else if (type.toLowerCase().equals("float")) {
                returnType = Attribute.Type.FLOAT;
            } else if (type.toLowerCase().equals("double")) {
                returnType = Attribute.Type.DOUBLE;
            } else if (type.toLowerCase().equals("bool")) {
                returnType = Attribute.Type.BOOL;
            } else if (type.toLowerCase().equals("string")) {
                returnType = Attribute.Type.STRING;
            } else {
                throw new ExecutionPlanValidationException("Type must be one of int,long,float,double,bool,string");
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (returnType == Attribute.Type.LONG && data[0] instanceof Integer) {
            return ((Integer) data[0]).longValue();
        }
        return data[0];
    }

    @Override
    protected Object execute(Object data) {
        return null;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null;    //No need to maintain a state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}


