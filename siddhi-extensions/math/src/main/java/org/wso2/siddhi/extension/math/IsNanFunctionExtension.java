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

package org.wso2.siddhi.extension.math;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/*
* is_nan(value);
* Returns true if 'value' is NaN; Returns false otherwise.
* Accept Type(s): FLOAT,DOUBLE
* Return Type(s): BOOLEAN
*/
public class IsNanFunctionExtension extends FunctionExecutor{
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to math:is_nan() function, " +
                    "required 1, but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.FLOAT
                && attributeExpressionExecutors[0].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of math:is_nan() function, " +
                    "required "+Attribute.Type.FLOAT+" or "+
                    Attribute.Type.DOUBLE+", but found "+attributeExpressionExecutors[0].getReturnType().toString());
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null;  //Since the is_nan function takes in only 1 parameter, this method does not get called. Hence, not implemented.
    }

    @Override
    protected Object execute(Object data) {
        if (data != null) {
            //type-conversion
            if (data instanceof Float) {
                return Float.isNaN((Float)data);
            } else {
                return Double.isNaN((Double)data);
            }
        } else {
            throw new ExecutionPlanRuntimeException("Input to the math:is_nan() function cannot be null");
        }
    }

    @Override
    public void start() {
        //Nothing to start.
    }

    @Override
    public void stop() {
        //Nothing to stop.
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.BOOL;
    }

    @Override
    public Object[] currentState() {
        return null;    //No need to maintain state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}
