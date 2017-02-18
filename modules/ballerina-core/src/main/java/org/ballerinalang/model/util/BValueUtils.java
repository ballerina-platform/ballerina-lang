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
package org.ballerinalang.model.util;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * BValue utility methods.
 *
 * @since 0.8.0
 */
public class BValueUtils {

//    // TODO How about storing these default values in hash map. This would avoid creating new objects for each
//    // TODO every variable declaration.
//    // TODO One option is to implement a pool of BValue objects
//    public static BValue getDefaultValue(BType type) {
//        if (type == BType.INT_TYPE) {
//            return new BInteger(0);
//
//        } else if (type == BType.typeLong) {
//            return new BLong(0);
//
//        } else if (type == BType.typeFloat) {
//            return new BFloat(0);
//
//        } else if (type == BType.typeDouble) {
//            return new BDouble(0);
//
//        } else if (type == BType.typeBoolean) {
//            return new BBoolean(false);
//
//        } else if (type == BType.typeString) {
//            return new BString("");
//
//        } else if (type == BType.typeJSON) {
//            return new BJSON("{}");
//
//        } else if (type == BType.typeXML) {
//            return new BXML();
//
//        } else if (type == BType.typeMessage) {
//            return new BMessage(null);
//
//        } else if (type == BType.CONNECTOR_TYPE) {
//            return new BConnector(null, null);
//
//        } else if (type instanceof BArrayType) {
//            BArrayType arrayType = (BArrayType) type;
//
//            BValue[] stringBArray = BArray.createArray(BInteger[].class);
////
//        }
//
//
//        throw new BallerinaException("Unsupported type: " + type);
//    }

    // TODO we need to improve this logic
    public static BValue clone(BType type, BValue bValue) {
        if (BTypes.isValueType(type)) {
            return bValue;
        }

        throw new BallerinaException("Cloning reference types are not yet supported in Ballerina");
    }
}
