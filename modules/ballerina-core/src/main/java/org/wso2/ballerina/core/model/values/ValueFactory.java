/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.model.values;

import org.wso2.ballerina.core.model.types.BooleanType;
import org.wso2.ballerina.core.model.types.DoubleType;
import org.wso2.ballerina.core.model.types.FloatType;
import org.wso2.ballerina.core.model.types.IntType;
import org.wso2.ballerina.core.model.types.JSONType;
import org.wso2.ballerina.core.model.types.LongType;
import org.wso2.ballerina.core.model.types.MessageType;
import org.wso2.ballerina.core.model.types.StringType;
import org.wso2.ballerina.core.model.types.Type;

/**
 * Factory for creating value of a given type.
 *
 * @since 1.0.0
 */
public class ValueFactory {

    public static BValueRef creteValue(Type type) {
        if (type instanceof IntType) {
            return new BValueRef(new IntValue(0));
        } else if (type instanceof StringType) {
            return new BValueRef(new StringValue(""));
        } else if (type instanceof LongType) {
            return new BValueRef(new LongValue(0));
        } else if (type instanceof FloatType) {
            return new BValueRef(new FloatValue(0));
        } else if (type instanceof DoubleType) {
            return new BValueRef(new DoubleValue(0));
        } else if (type instanceof BooleanType) {
            return new BValueRef(new BooleanValue(false));
        } else if (type instanceof JSONType) {
            return new BValueRef(new JSONValue("{}"));
        } else if (type instanceof MessageType) {
            return new BValueRef(new MessageValue(null));
        } else {
            throw new RuntimeException("Unsupported type: " + type.getName());
        }
    }
}
