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
 */kage org.wso2.siddhi.extension.string;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * contains(inputSequence, searchingSequence)
 * This method returns true if and only if this string contains the specified sequence of char values.
 * searchingSequence - the sequence to search for. eg: "WSO2"
 * inputSequence - the input string eg: "21 products are produced by WSO2 currently"
 * Accept Type(s) for contains(inputSequence, searchingSequence);
 *         inputSequence : STRING
 *         searchingSequence : STRING
 * Return Type(s): BOOLEAN
 */
public class ContainsFunctionExtension extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.BOOL;
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to str:contains() function, " +
                    "required 2, but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                    "str:contains() function, required "+Attribute.Type.STRING+", " +
                    "but found "+attributeExpressionExecutors[0].getReturnType().toString());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                    "str:contains() function, required "+Attribute.Type.STRING+", " +
                    "but found "+attributeExpressionExecutors[1].getReturnType().toString());
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to str:contains() function. First argument cannot be null");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to str:contains() function. Second argument cannot be null");
        }
        String source = (String) data[0];
        String sequenceToSearch = (String) data[1];
        return source.contains(sequenceToSearch);

    }

    @Override
    protected Object execute(Object data) {
        return null;//Since the contains function takes in 2 parameters, this method does not get called. Hence, not implemented.
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null;
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
