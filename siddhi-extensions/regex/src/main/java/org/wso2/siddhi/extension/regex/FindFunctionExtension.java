/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.regex;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * find(string: input sequence, regex: regular expression pattern)
 * find(string: input sequence, regex: regular expression pattern, startingIndex: specific starting index to match the regex pattern)
 * This method attempts to find the next sub-sequence of the 'string' that matches the 'regex' pattern.
 * Accept Type(s): (STRING,STRING)
 * Return Type(s): BOOLEAN
 */
public class FindFunctionExtension extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.BOOL;

    //state-variables
    boolean isRegexConstant = false;
    String regexConstant;
    Pattern patternConstant;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2 && attributeExpressionExecutors.length != 3){
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to regex:find() function, required 2 or 3, " +
                    "but found " + attributeExpressionExecutors.length);
        }else{
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of regex:find() function, " +
                        "required "+Attribute.Type.STRING+", but found "+attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of regex:find() function, " +
                        "required " + Attribute.Type.STRING + ", but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
            if (attributeExpressionExecutors.length == 3){
                if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.INT) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of str:find() function, " +
                            "required "+Attribute.Type.INT+", but found "+attributeExpressionExecutors[1].getReturnType().toString());
                }
            }
        }

        if(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor){
            isRegexConstant = true;
            regexConstant = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            patternConstant = Pattern.compile(regexConstant);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        String regex;
        Pattern pattern;
        Matcher matcher;

        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to regex:find() function. First argument cannot be null");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to regex:find() function. Second argument cannot be null");
        }

        String source = (String) data[0];

        if(data.length == 2){
            if(!isRegexConstant){
                regex = (String) data[1];
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(source);
                return matcher.find();

            } else {
                matcher = patternConstant.matcher(source);
                return matcher.find();
            }
        }else{
            if (data[2] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to regex:find() function. Second argument cannot be null");
            }
            if(!isRegexConstant){
                regex = (String) data[1];
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(source);
                return matcher.find((Integer) data[2]);

            } else {
                matcher = patternConstant.matcher(source);
                return matcher.find((Integer) data[2]);
            }
        }
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the find function takes in 2 parameters, this method does not get called. Hence, not implemented.
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
        return new Object[]{isRegexConstant, regexConstant, patternConstant};
    }

    @Override
    public void restoreState(Object[] state) {
        isRegexConstant = (Boolean) state[0];
        regexConstant = (String) state[1];
        patternConstant = (Pattern) state[2];
    }
}
