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
package org.wso2.ballerina.core.model.util;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * BValue utility methods
 *
 * @since 1.0.0
 */
public class BValueUtils {

    // TODO How about storing these default values in hash map. This would avoid creating new objects for each
    // TODO every variable declaration.
    // TODO One option is to implement a pool of BValue objects
    public static BValue getDefaultValue(TypeC type) {
        if (type == TypeC.INT_TYPE) {
            return new BInteger(0);

        } else if (type == TypeC.LONG_TYPE) {
            return new BLong(0);

        } else if (type == TypeC.FLOAT_TYPE) {
            return new BFloat(0);

        } else if (type == TypeC.DOUBLE_TYPE) {
            return new BDouble(0);

        } else if (type == TypeC.BOOLEAN_TYPE) {
            return new BBoolean(false);

        } else if (type == TypeC.STRING_TYPE) {
            return new BString("");
//
//        } else if (type == TypeC.JSON_TYPE) {
//            return new BValueRef(new JSONValue("{}"));
//
//        } else if (type == TypeC.XML_TYPE) {
//            return new BValueRef(new XMLValue());
//
//        } else if (type == TypeC.MESSAGE_TYPE) {
//            return new BValueRef(new MessageValue(null));
//
//        } else if (type == TypeC.MAP_TYPE) {
//            return new BValueRef(new MapValue());
//
//        } else if (type == TypeC.CONNECTOR_TYPE) {
//            return new BValueRef(new ConnectorValue(null, null));
        } else {
            throw new BallerinaException("Unsupported type: " + type);
        }
    }

    // TODO we need to improve this logic
    public static BValue clone(TypeC type, BValue bValue) {
        if (TypeC.isValueType(type)) {
            return bValue;
        }

        throw new BallerinaException("Cloning reference types are not yet supported in Ballerina");
    }
}
