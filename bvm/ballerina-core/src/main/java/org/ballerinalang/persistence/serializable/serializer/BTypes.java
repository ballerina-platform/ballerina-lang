/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable.serializer;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.exceptions.BallerinaException;

import static org.ballerinalang.model.types.TypeConstants.ANY_TNAME;
import static org.ballerinalang.model.types.TypeConstants.BOOLEAN_TNAME;
import static org.ballerinalang.model.types.TypeConstants.BYTE_TNAME;
import static org.ballerinalang.model.types.TypeConstants.FLOAT_TNAME;
import static org.ballerinalang.model.types.TypeConstants.FUTURE_TNAME;
import static org.ballerinalang.model.types.TypeConstants.INT_TNAME;
import static org.ballerinalang.model.types.TypeConstants.JSON_TNAME;
import static org.ballerinalang.model.types.TypeConstants.MAP_TNAME;
import static org.ballerinalang.model.types.TypeConstants.NULL_TNAME;
import static org.ballerinalang.model.types.TypeConstants.STREAM_TNAME;
import static org.ballerinalang.model.types.TypeConstants.STRING_TNAME;
import static org.ballerinalang.model.types.TypeConstants.TABLE_TNAME;
import static org.ballerinalang.model.types.TypeConstants.XML_ATTRIBUTES_TNAME;
import static org.ballerinalang.model.types.TypeConstants.XML_TNAME;

/**
 * Mapping from {@link BType} string representations to {@link BType} instances.
 */
public class BTypes {
    public static BType fromString(String typeName) {
        switch (typeName) {
            case INT_TNAME:
                return org.ballerinalang.model.types.BTypes.typeInt;
            case BYTE_TNAME:
                return org.ballerinalang.model.types.BTypes.typeByte;
            case FLOAT_TNAME:
                return org.ballerinalang.model.types.BTypes.typeFloat;
            case STRING_TNAME:
                return org.ballerinalang.model.types.BTypes.typeString;
            case BOOLEAN_TNAME:
                return org.ballerinalang.model.types.BTypes.typeBoolean;
            case MAP_TNAME:
                return org.ballerinalang.model.types.BTypes.typeMap;
            case FUTURE_TNAME:
                return org.ballerinalang.model.types.BTypes.typeFuture;
            case XML_TNAME:
                return org.ballerinalang.model.types.BTypes.typeXML;
            case JSON_TNAME:
                return org.ballerinalang.model.types.BTypes.typeJSON;
            case TABLE_TNAME:
                return org.ballerinalang.model.types.BTypes.typeTable;
            case STREAM_TNAME:
                return org.ballerinalang.model.types.BTypes.typeStream;
            case ANY_TNAME:
                return org.ballerinalang.model.types.BTypes.typeAny;
            case NULL_TNAME:
                return org.ballerinalang.model.types.BTypes.typeNull;
            case XML_ATTRIBUTES_TNAME:
                return org.ballerinalang.model.types.BTypes.typeXMLAttributes;
            default:
                throw new BallerinaException("Unknown type name: " + typeName);
        }
    }
}
