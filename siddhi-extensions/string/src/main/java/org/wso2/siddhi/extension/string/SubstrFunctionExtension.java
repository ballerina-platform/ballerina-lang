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

package org.wso2.siddhi.extension.string;

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
 * substr(sourceText, beginIndex) or substr(sourceText, beginIndex, length) or substr(sourceText, regex) or substr(sourceText, regex, groupNumber)
 * Returns a new string that is a substring of this string.
 * Accept Type(s): (STRING,INT) or (STRING,INT,INT) or (STRING,STRING) or (STRING,STRING,INT)
 * Return Type(s): STRING
 */
public class SubstrFunctionExtension extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.STRING;

   /*
   * Sub-string Types are as follows:
   * ONE: str:substr(<string sourceText> , <int beginIndex>)
   * TWO: str:substr(<string sourceText> , <int beginIndex>, <int length>)
   * THREE: str:substr(<string sourceText> , <string regex>)
   * FOUR: str:substr(<string sourceText> , <string regex>, <int groupNumber>)
   * */
    public enum SubstrType {
        ONE, TWO, THREE, FOUR
    }

    //state-variables
    boolean isRegexConstant = false;
    String regexConstant;
    Pattern patternConstant;
    SubstrType substrType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of str:substr() function, " +
                    "required "+Attribute.Type.STRING+", but found "+attributeExpressionExecutors[0].getReturnType().toString());
        }
        if (attributeExpressionExecutors.length == 2) {
            if(attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT){
                substrType = SubstrType.ONE;
            } else if(attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING){
                substrType = SubstrType.THREE;
                if(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor){
                    isRegexConstant = true;
                    regexConstant = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
                    patternConstant = Pattern.compile(regexConstant);
                }
            } else {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of str:substr() function, " +
                        "required "+Attribute.Type.STRING+" or "+Attribute.Type.INT+", but found "+attributeExpressionExecutors[1].getReturnType().toString());
            }
        } else if (attributeExpressionExecutors.length == 3) {
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.INT) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of str:substr() function, " +
                        "required "+Attribute.Type.INT+", but found "+attributeExpressionExecutors[2].getReturnType().toString());
            }
            if(attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT){
                substrType = SubstrType.TWO;
            } else if(attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING){
                substrType = SubstrType.FOUR;
                if(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor){
                    isRegexConstant = true;
                    regexConstant = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
                    patternConstant = Pattern.compile(regexConstant);
                }
            } else {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of str:substr() function, " +
                        "required "+Attribute.Type.STRING+" or "+Attribute.Type.INT+", but found "+attributeExpressionExecutors[1].getReturnType().toString());
            }
        } else {
            throw new ExecutionPlanValidationException("Invalid no of Arguments passed to str:substr() function, required 2 or 3, but found "
                    + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        int beginIndex;
        int length;
        int groupNo;
        String regex;
        String output = "";
        Pattern pattern;
        Matcher matcher;

        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to str:substr() function. First argument cannot be null");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to str:substr() function. Second argument cannot be null");
        }
        String source = (String) data[0];

        switch (substrType) {
            case ONE:
                beginIndex = (Integer) data[1];
                output = source.substring(beginIndex);
                break;
            case TWO:
                if (data[2] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:substr() function. Third argument cannot be null");
                }
                beginIndex = (Integer) data[1];
                length = (Integer) data[2];
                output = source.substring(beginIndex, (beginIndex + length));
                break;
            case THREE:
                if(!isRegexConstant){
                    regex = (String) data[1];
                    pattern = Pattern.compile(regex);
                    matcher = pattern.matcher(source);
                    if (matcher.find()) {
                        output = matcher.group(0);
                    }
                } else {
                    matcher = patternConstant.matcher(source);
                    if (matcher.find()) {
                        output = matcher.group(0);
                    }
                }
                break;
            case FOUR:
                if (data[2] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:substr() function. Third argument cannot be null");
                }
                groupNo = (Integer) data[2];
                if(!isRegexConstant){
                    regex = (String) data[1];
                    pattern = Pattern.compile(regex);
                    matcher = pattern.matcher(source);
                    if (matcher.find()) {
                        output = matcher.group(groupNo);
                    }
                } else {
                    matcher = patternConstant.matcher(source);
                    if (matcher.find()) {
                        output = matcher.group(groupNo);
                    }
                }
                break;
        }
        return output;
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the substr function takes in at least 2 parameters, this method does not get called. Hence, not implemented.
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
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{isRegexConstant, regexConstant, patternConstant, substrType};
    }

    @Override
    public void restoreState(Object[] state) {
        isRegexConstant = (Boolean) state[0];
        regexConstant = (String) state[1];
        patternConstant = (Pattern) state[2];
        substrType = (SubstrType) state[3];
    }
}
