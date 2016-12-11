/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.utils;

import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.types.DoubleType;
import org.wso2.ballerina.core.model.types.FloatType;
import org.wso2.ballerina.core.model.types.IntType;
import org.wso2.ballerina.core.model.types.JSONType;
import org.wso2.ballerina.core.model.types.LongType;
import org.wso2.ballerina.core.model.types.MessageType;
import org.wso2.ballerina.core.model.types.StringType;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.DoubleValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.LongValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.model.values.StringValue;

/**
 * factory class to create a bValue of given type
 */
public class BValueFactory {
    public static BValue createBValueFromVariableDeclaration(VariableDcl variableDcl) {
        BValue bValue;
        if (variableDcl.getType() instanceof IntType) {
            bValue = new IntValue(Integer.parseInt(variableDcl.getValue()));
            return bValue;
        } else if (variableDcl.getType() instanceof FloatType) {
            bValue = new FloatValue(Float.parseFloat(variableDcl.getValue()));
            return bValue;
        } else if (variableDcl.getType() instanceof DoubleType) {
            bValue = new DoubleValue(Double.parseDouble(variableDcl.getValue()));
            return bValue;
        } else if (variableDcl.getType() instanceof LongType) {
            bValue = new LongValue(Long.parseLong(variableDcl.getValue()));
            return bValue;
        } else if (variableDcl.getType() instanceof StringType) {
            bValue = new StringValue(variableDcl.getValue());
            return bValue;
        } else if (variableDcl.getType() instanceof JSONType) {
            bValue = new JSONValue(variableDcl.getValue());
            return bValue;
        } else if (variableDcl.getType() instanceof MessageType) {
            bValue = new MessageValue(null);
            return bValue;
        }
        return null;
    }
}
