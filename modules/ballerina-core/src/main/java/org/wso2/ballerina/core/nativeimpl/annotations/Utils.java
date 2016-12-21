/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerina.core.nativeimpl.annotations;

import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.ArrayValue;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.DoubleValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.LongValue;
import org.wso2.ballerina.core.model.values.MapValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException;

/**
 * Common Utils methods used in Annotations.
 */
public class Utils {

    /**
     * Create Const instance from BallerinaConstant Annotation.
     *
     * @param constant annotation instance.
     * @return Const instance.
     * @throws MalformedEntryException when Type is not supported or when value conversion fails.
     */
    public static Const getConst(BallerinaConstant constant) throws MalformedEntryException {
        BValue value = getBValueFromTypeEnum(constant.type(), constant.value());
        SymbolName symbolName = new SymbolName(constant.identifier());
        TypeC type = TypeC.getTypeC(constant.type().getName());
        return new Const(type, symbolName, value);
    }

    /**
     * Get BValue from the TypeEnum and Value.
     *
     * @param typeEnum Ballerina Type.
     * @param value    assigned value.
     * @return BValue.
     * @throws MalformedEntryException when Type is not supported or when value conversion fails.
     */
    public static BValue getBValueFromTypeEnum(TypeEnum typeEnum, String value) throws MalformedEntryException {
        BValue bValue;
        try {
            switch (typeEnum) {
                case BOOLEAN:
                    bValue = new BooleanValue(Boolean.parseBoolean(value));
                    break;
                case INT:
                    bValue = new IntValue(Integer.parseInt(value));
                    break;
                case LONG:
                    bValue = new LongValue(Long.parseLong(value));
                    break;
                case DOUBLE:
                    bValue = new DoubleValue(Double.parseDouble(value));
                    break;
                case FLOAT:
                    bValue = new FloatValue(Float.parseFloat(value));
                    break;
                case STRING:
                    bValue = new StringValue(value);
                    break;
                case JSON:
                    bValue = new JSONValue(value);
                    break;
                case XML:
                    // TODO: Fix this.
                    throw new MalformedEntryException("XML not supported yet.");
                case MAP:
                    // TODO: Fix this.
                    bValue = new MapValue();
                    break;
                case ARRAY:
                    // TODO: improve logic. Current Native Annotation support only Predefined String[] only.
                    String[] values = value.substring(1, value.length() - 1).split(",");
                    bValue = new ArrayValue<>(values);
                    break;
                default:
                    throw new MalformedEntryException("Not supported entry " + typeEnum);
            }
        } catch (NumberFormatException e) {
            throw new MalformedEntryException("Error while processing value " + value, e);
        }
        return bValue;
    }
}
