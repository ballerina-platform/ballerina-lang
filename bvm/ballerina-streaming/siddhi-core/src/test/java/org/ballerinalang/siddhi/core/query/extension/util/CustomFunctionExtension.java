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
package org.ballerinalang.siddhi.core.query.extension.util;

import org.ballerinalang.siddhi.annotation.Example;
import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.annotation.Parameter;
import org.ballerinalang.siddhi.annotation.ReturnAttribute;
import org.ballerinalang.siddhi.annotation.util.DataType;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.function.FunctionExecutor;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.definition.Attribute;

import java.util.Map;

@Extension(
        name = "plus",
        namespace = "custom",
        description = "Return the sum of the given input values.",
        parameters = {
                @Parameter(name = "args",
                        description = "The values that need to be sum.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns the calculated sum value as a double or float.",
                type = {DataType.DOUBLE, DataType.FLOAT}),
        examples = @Example(
                syntax = "from fooStream\n" +
                        "select custom:plus(4, 6, 10) as total\n" +
                        "insert into barStream",
                description = "This will return value 20 as total."
        )
)
public class CustomFunctionExtension extends FunctionExecutor {

    private Attribute.Type returnType;

    @Override
    public void init(ExpressionExecutor[] attributeExpressionExecutors,
                     ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            Attribute.Type attributeType = expressionExecutor.getReturnType();
            if (attributeType == Attribute.Type.DOUBLE) {
                returnType = attributeType;

            } else if ((attributeType == Attribute.Type.STRING) || (attributeType == Attribute.Type.BOOL)) {
                throw new SiddhiAppCreationException("Plus cannot have parameters with types String or Bool");
            } else {
                returnType = Attribute.Type.LONG;
            }
        }

    }

    /**
     * Return type of the custom function mentioned
     *
     * @return return type
     */
    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    @Override
    protected Object execute(Object[] obj) {
        if (returnType == Attribute.Type.DOUBLE) {
            double total = 0;
            for (Object aObj : obj) {
                total += Double.parseDouble(String.valueOf(aObj));
            }

            return total;
        } else {
            long total = 0;
            for (Object aObj : obj) {
                total += Long.parseLong(String.valueOf(aObj));
            }
            return total;
        }

    }

    @Override
    protected Object execute(Object obj) {
        if (returnType == Attribute.Type.DOUBLE) {
            double total = 0;
            if (obj instanceof Object[]) {
                for (Object aObj : (Object[]) obj) {
                    total += Double.parseDouble(String.valueOf(aObj));
                }
            }
            return total;
        } else {
            long total = 0;
            if (obj instanceof Object[]) {
                for (Object aObj : (Object[]) obj) {
                    total += Long.parseLong(String.valueOf(aObj));
                }
            }
            return total;
        }
    }

    @Override
    public Map<String, Object> currentState() {
        //No state
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }
}
