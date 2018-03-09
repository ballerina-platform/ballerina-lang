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
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.definition.Attribute.Type;

import java.util.HashMap;
import java.util.Map;

@Extension(
        name = "getAll",
        namespace = "custom",
        description = "Return the concatenations of the given input values.",
        parameters = {
                @Parameter(name = "args",
                        description = "The values that need to be concat.",
                        type = {DataType.STRING})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns the concatenated value as a string.",
                type = {DataType.STRING}),
        examples = @Example(
                syntax = "from inputStream\n" +
                        "select custom:getAll(\"hello\",\"_\",\"world\") as name\n" +
                        "insert into outputStream;",
                description = "This will concatenate given input values and return 'hello world'."
        )
)
public class StringConcatAggregatorString extends AttributeAggregator {
    private static final long serialVersionUID = 1358667438272544590L;
    private String aggregatedStringValue = "";
    private boolean appendAbc = false;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param configReader                 this hold the {@link StringConcatAggregatorString} configuration reader.
     * @param siddhiAppContext             SiddhiContext
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        appendAbc = Boolean.parseBoolean(configReader.readConfig("append.abc", "false"));

    }

    @Override
    public Type getReturnType() {
        return Type.STRING;
    }


    @Override
    public Object processAdd(Object data) {
        aggregatedStringValue = aggregatedStringValue + data;
        if (appendAbc) {
            return aggregatedStringValue + "-abc";
        } else {
            return aggregatedStringValue;
        }
    }

    @Override
    public Object processAdd(Object[] data) {
        for (Object aData : data) {
            aggregatedStringValue = aggregatedStringValue + aData;
        }
        if (appendAbc) {
            return aggregatedStringValue + "-abc";
        } else {
            return aggregatedStringValue;
        }
    }


    @Override
    public Object processRemove(Object data) {
        aggregatedStringValue = aggregatedStringValue.replaceFirst(data.toString(), "");
        if (appendAbc) {
            return aggregatedStringValue + "-abc";
        } else {
            return aggregatedStringValue;
        }
    }

    @Override
    public Object processRemove(Object[] data) {
        for (Object aData : data) {
            aggregatedStringValue = aggregatedStringValue.replaceFirst(aData.toString(), "");
        }
        if (appendAbc) {
            return aggregatedStringValue + "-abc";
        } else {
            return aggregatedStringValue;
        }
    }

    @Override
    public boolean canDestroy() {
        return aggregatedStringValue != null && aggregatedStringValue.equals("");
    }

    @Override
    public Object reset() {
        aggregatedStringValue = "";
        return aggregatedStringValue;
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("AggregatedStringValue", aggregatedStringValue);
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        aggregatedStringValue = (String) state.get("AggregatedStringValue");
    }
}
