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
package org.wso2.ballerina.core.model.types;

import static org.wso2.ballerina.core.model.types.TypeConstants.ARRAY_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.BOOLEAN_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.DATATABLE_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.DOUBLE_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.FLOAT_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.INT_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.JSON_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.LONG_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.MAP_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.MESSAGE_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.STRING_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.STRUCT_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.XML_TNAME;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
 *
 * @since 0.8.0
 */
public class BTypes {

    public static final BType INT_TYPE = new BIntegerType(INT_TNAME);
    public static final BType LONG_TYPE = new BLongType(LONG_TNAME);
    public static final BType FLOAT_TYPE = new BFloatType(FLOAT_TNAME);
    public static final BType DOUBLE_TYPE = new BDoubleType(DOUBLE_TNAME);
    public static final BType BOOLEAN_TYPE = new BBooleanType(BOOLEAN_TNAME);
    public static final BType STRING_TYPE = new BStringType(STRING_TNAME);
    public static final BType XML_TYPE = new BXMLType(XML_TNAME);
    public static final BType JSON_TYPE = new BJSONType(JSON_TNAME);
    public static final BType MESSAGE_TYPE = new BMessageType(MESSAGE_TNAME);
    public static final BType MAP_TYPE = new BMapType(MAP_TNAME);
    public static final BType DATATABLE_TYPE = new BDataTableType(DATATABLE_TNAME);
    public static final BType STRUCT_TYPE = new BStructType(STRUCT_TNAME);

    private BTypes() {
    }

    public static BArrayType getArrayType(String elementTypeName) {
        String arrayTypeName = elementTypeName + ARRAY_TNAME;

        BArrayType type = BType.getType(arrayTypeName);
        if (type == null) {
            type = new BArrayType(arrayTypeName, elementTypeName);
        }

        return type;
    }

    public static void addConnectorType(String connectorName) {
        new BConnectorType(connectorName);
    }

    public static boolean isValueType(BType type) {
        if (type == BTypes.INT_TYPE ||
                type == BTypes.STRING_TYPE ||
                type == BTypes.LONG_TYPE ||
                type == BTypes.FLOAT_TYPE ||
                type == BTypes.DOUBLE_TYPE ||
                type == BTypes.BOOLEAN_TYPE) {
            return true;
        }

        return false;
    }

    public static <T extends BType> T getType(String typeName) {
        return BType.getType(typeName);
    }

    public static void addStructType(String structName) {
        new BStructType(structName);
    }
}
