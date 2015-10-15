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
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Random;

/*
* rand() or rand(seed);
* A sequence of calls to rand(seed) generates a stream of pseudo-random numbers.
* Accept Type(s): INT/LONG
* Return Type(s): DOUBLE
*/
public class RandomFunctionExtension extends FunctionExecutor {

    //state-variables
    Random random;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length > 1) {
            throw new ExecutionPlanValidationException("Invalid no of Arguments Passed. Required 0 or 1. Found " + attributeExpressionExecutors.length);
        }
        if(attributeExpressionExecutors.length == 1){
            if(attributeExpressionExecutors[0] == null){
                throw new ExecutionPlanValidationException("Invalid input given to math:rand() function. The 'seed' argument cannot be null");
            }
            Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
            if(type != Attribute.Type.INT && type != Attribute.Type.LONG){
                throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of math:rand() function, " +
                        "required "+Attribute.Type.INT+" or "+Attribute.Type.LONG+", but found "+type.toString());
            }
            long seed;
            if(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor){
                Object constantObj = ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
                if (type == Attribute.Type.INT) {
                    int intSeed;
                    intSeed = (Integer) constantObj;
                    seed = (long) intSeed;
                } else {
                    seed = (Long) constantObj;
                }
            } else {
                throw new ExecutionPlanValidationException("The seed argument of math:rand() function should be a constant," +
                        " but found "+attributeExpressionExecutors[0].getClass().toString());
                        //This should be a constant because the instantiation of java.util.Random should be done in the init() method.
            }
            random = new Random(seed);
        } else {
            random = new Random();
        }

    }

    @Override
    protected Object execute(Object[] data) {
        return null;  //Since the rand function takes in 0 or 1 parameter, this method does not get called. Hence, not implemented.
    }

    @Override
    protected Object execute(Object data) {
        return random.nextDouble();
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
        return Attribute.Type.DOUBLE;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{random};
    }

    @Override
    public void restoreState(Object[] state) {
        random = (Random) state[0];
    }
}
