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
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Map;

/**
 * Executor class for convert function. Function execution logic is implemented in execute here.
 */
@Extension(
        name = "convert",
        namespace = "",
        description = "Converts the first input parameter according to the convertedTo parameter.",
        parameters = {
                @Parameter(name = "to.be.converted",
                        description = "This specifies the value to be converted.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT, DataType.STRING,
                                DataType.BOOL}),
                @Parameter(name = "converted.to",
                        description = "A string constant parameter to which type the attribute need to be converted " +
                                " using one of the following strings values: 'int', 'long', 'float', 'double', " +
                                "'string', 'bool'.",
                        type = DataType.STRING)
        },
        returnAttributes = @ReturnAttribute(
                description = "Based on the given convertedTo parameter.",
                type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT, DataType.STRING, DataType.BOOL}),
        examples = {
                @Example(
                        syntax = "from fooStream\n" +
                                "select convert(temp, 'double') as temp\n" +
                                "insert into barStream;",
                        description = "This will convert fooStream temp value into 'double'."),
                @Example(
                        syntax = "from fooStream\n" +
                                "select convert(temp, 'int') as temp\n" +
                                "insert into barStream;",
                        description = "This will convert fooStream temp value into 'int' (value = \"convert(45.9, " +
                                "'int') returns 46\")."
                )
        }
)
public class ConvertFunctionExecutor extends FunctionExecutor {

    private Attribute.Type returnType;
    private Attribute.Type inputType;

    @Override
    public void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new SiddhiAppValidationException("convert() must have at 2 parameters, attribute and to be " +
                                                               "converted type");
        }
        inputType = attributeExpressionExecutors[0].getReturnType();
        if (inputType == Attribute.Type.OBJECT) {
            throw new SiddhiAppValidationException("1st parameter of convert() cannot be 'object' as " +
                                                               "it's not supported, it has to be either of (STRING, " +
                                                               "INT, LONG, FLOAT, DOUBLE, BOOL), but found " +
                                                               attributeExpressionExecutors[0].getReturnType());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
            throw new SiddhiAppValidationException("2nd parameter of convert() must be 'string' have constant " +
                                                               "value either of (STRING, INT, LONG, FLOAT, DOUBLE, "
                                                               + "BOOL), but found " +
                                                               attributeExpressionExecutors[0].getReturnType());
        }
        if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppValidationException("2nd parameter of convert() must have constant value either " +
                                                               "of (STRING, INT, LONG, FLOAT, DOUBLE, BOOL), but found "
                                                               + "a variable expression");
        }
        String type = (String) attributeExpressionExecutors[1].execute(null);
        if (Attribute.Type.STRING.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.STRING;
        } else if (Attribute.Type.BOOL.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.BOOL;
        } else if (Attribute.Type.DOUBLE.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.DOUBLE;
        } else if (Attribute.Type.FLOAT.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.FLOAT;
        } else if (Attribute.Type.INT.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.INT;
        } else if (Attribute.Type.LONG.toString().equalsIgnoreCase(type)) {
            returnType = Attribute.Type.LONG;
        } else {
            throw new SiddhiAppValidationException("2nd parameter of convert() must have value either of " +
                                                               "(STRING, INT, LONG, FLOAT, DOUBLE, BOOL), but found '" +
                                                               type + "'");
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    protected Object execute(Object[] obj) {
        Object data = obj[0];
        if (data != null) {
            try {
                switch (returnType) {
                    case STRING:
                        return data.toString();
                    case INT:
                        switch (inputType) {
                            case STRING:
                                return Integer.parseInt((String) data);
                            case INT:
                                return data;
                            case LONG:
                                return ((Long) data).intValue();
                            case FLOAT:
                                return ((Float) data).intValue();
                            case DOUBLE:
                                return ((Double) data).intValue();
                            case BOOL:
                                return ((Boolean) data) ? 1 : 0;
                            case OBJECT:
                                return null;
                        }
                        break;
                    case LONG:
                        switch (inputType) {
                            case STRING:
                                return Long.parseLong((String) data);
                            case INT:
                                return ((Integer) data).longValue();
                            case LONG:
                                return data;
                            case FLOAT:
                                return ((Float) data).longValue();
                            case DOUBLE:
                                return ((Double) data).longValue();
                            case BOOL:
                                return ((Boolean) data) ? 1L : 0L;
                            case OBJECT:
                                return null;
                        }
                        break;
                    case FLOAT:
                        switch (inputType) {
                            case STRING:
                                return Float.parseFloat((String) data);
                            case INT:
                                return ((Integer) data).floatValue();
                            case LONG:
                                return ((Long) data).floatValue();
                            case FLOAT:
                                return data;
                            case DOUBLE:
                                return ((Double) data).floatValue();
                            case BOOL:
                                return ((Boolean) data) ? 1F : 0F;
                            case OBJECT:
                                return null;
                        }
                        break;
                    case DOUBLE:
                        switch (inputType) {
                            case STRING:
                                return Double.parseDouble((String) data);
                            case INT:
                                return ((Integer) data).doubleValue();
                            case LONG:
                                return ((Long) data).doubleValue();
                            case FLOAT:
                                return ((Float) data).doubleValue();
                            case DOUBLE:
                                return data;
                            case BOOL:
                                return ((Boolean) data) ? 1.0 : 0.0;
                            case OBJECT:
                                return null;
                        }
                        break;
                    case BOOL:
                        switch (inputType) {
                            case STRING:
                                return Boolean.parseBoolean((String) data);
                            case INT:
                                return ((Integer) data) == 1;
                            case LONG:
                                return ((Long) data) == 1L;
                            case FLOAT:
                                return ((Float) data) == 1F;
                            case DOUBLE:
                                return ((Double) data) == 1.0;
                            case BOOL:
                                return data;
                            case OBJECT:
                                return null;
                        }
                        break;
                    case OBJECT:
                        break;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    protected Object execute(Object data) {
        //will not occur
        return null;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }

    @Override
    public Map<String, Object> currentState() {
        //No states
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }
}
