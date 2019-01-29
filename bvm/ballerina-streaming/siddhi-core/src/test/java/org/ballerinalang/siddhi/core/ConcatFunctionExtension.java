/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.function.FunctionExecutor;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Map;

/**
* concat(string1, string2, ..., stringN).
* Returns a string that is the result of concatenating two or more string values.
* Accept Type(s): STRING. There should be at least two arguments.
* Return Type(s): STRING
*/
public class ConcatFunctionExtension extends FunctionExecutor {

    private Attribute.Type returnType = Attribute.Type.STRING;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length < 2) {
            throw new SiddhiAppValidationException("str:concat() function requires at least two arguments, " +
                    "but found only " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        StringBuilder sb = new StringBuilder();
        for (Object aData : data) {
            if (aData != null) {
                sb.append(aData);
            }
        }
        return sb.toString();
    }

    @Override
    protected Object execute(Object data) {
        return data;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;    //No states
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }
}
