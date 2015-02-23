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

package org.wso2.siddhi.extension.stringfunctions;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

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
    boolean isBeginIndexConstant = false;
    boolean isRegexConstant = false;
    boolean isLengthConstant = false;
    boolean isGroupNoConstant = false;
    int beginIndex;
    int length;
    int groupNo = 0;
    String regex;
    SubstrType substrType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length == 2) {
            //The substring begins at the specified beginIndex and extends to the character at last times.
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING
                    && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                substrType = SubstrType.ONE;
                if (attributeExpressionExecutors[1].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
                    isBeginIndexConstant = true;
                    beginIndex = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
                }
            } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING) {
                substrType = SubstrType.THREE;
                if (attributeExpressionExecutors[1].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
                    isRegexConstant = true;
                    regex = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
                }
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found");
            }
        } else if (attributeExpressionExecutors.length == 3) {
            //The substring begins at the specified beginIndex and extends to the given length.
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING
                    && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT
                    && attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                substrType = SubstrType.TWO;
                if (attributeExpressionExecutors[1].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
                    isBeginIndexConstant = true;
                    beginIndex = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
                }
                if (attributeExpressionExecutors[2].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
                    isLengthConstant = true;
                    length = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue();
                }
            } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING &&
                    attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                substrType = SubstrType.FOUR;
                if (attributeExpressionExecutors[1].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
                    isRegexConstant = true;
                    regex = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
                }
                if (attributeExpressionExecutors[2].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
                    isGroupNoConstant = true;
                    groupNo = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue();
                }
            } else {
                throw new ExecutionPlanCreationException("Invalid parameter type found");
            }
        } else {
            throw new ExecutionPlanCreationException("Invalid no of Arguments Passed. Required 2 or 3. Found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        String source = (String) data[0];
        String output = "";
        Pattern pattern;
        Matcher matcher;

        if (data[0] == null || data[1] == null) {
            throw new OperationNotSupportedException("Input to str:substr() function cannot be null");
        }

        switch (substrType) {
            case ONE:
                if (!isBeginIndexConstant) {
                    beginIndex = (Integer) data[1];
                }
                output = source.substring(beginIndex);
                break;
            case TWO:
                if (data[2] == null) {
                    throw new OperationNotSupportedException("Input to str:substr() function cannot be null");
                }
                if (!isBeginIndexConstant) {
                    beginIndex = (Integer) data[1];
                }
                if (!isLengthConstant) {
                    length = (Integer) data[2];
                }
                output = source.substring(beginIndex, (beginIndex + length));
                break;
            case THREE:
                if (!isRegexConstant) {
                    regex = (String) data[1];
                }
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(source);
                if (matcher.find()) {
                    output = matcher.group(groupNo);
                }
                break;
            case FOUR:
                if (data[2] == null) {
                    throw new OperationNotSupportedException("Input to str:substr() function cannot be null");
                }
                if (!isRegexConstant) {
                    regex = (String) data[1];
                }
                if (!isGroupNoConstant) {
                    groupNo = (Integer) data[2];
                }
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(source);
                if (matcher.find()) {
                    output = matcher.group(groupNo);
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
        return new Object[]{isBeginIndexConstant, isRegexConstant, isLengthConstant, isGroupNoConstant, beginIndex, length, groupNo, regex, substrType};
    }

    @Override
    public void restoreState(Object[] state) {
        isBeginIndexConstant = (Boolean) state[0];
        isRegexConstant = (Boolean) state[1];
        isLengthConstant = (Boolean) state[2];
        isGroupNoConstant = (Boolean) state[3];
        beginIndex = (Integer) state[4];
        length = (Integer) state[5];
        groupNo = (Integer) state[6];
        regex = (String) state[7];
        substrType = (SubstrType) state[8];
    }
}
