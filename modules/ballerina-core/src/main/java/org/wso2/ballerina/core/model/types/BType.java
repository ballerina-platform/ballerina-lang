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

import java.util.HashMap;
import java.util.Map;

import static org.wso2.ballerina.core.model.types.TypeConstants.ARRAY_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.BOOLEAN_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.CONNECTOR_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.DOUBLE_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.FLOAT_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.INT_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.JSON_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.LONG_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.MAP_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.MESSAGE_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.STRING_TNAME;
import static org.wso2.ballerina.core.model.types.TypeConstants.XML_TNAME;

/**
 * {@code Type} represent a type in Ballerina
 *
 * @since 1.0.0
 */
public class BType {

    protected String typeName;

    //Using a HashMap here, because there won't be any concurrent access
    // TODO Improve this to support modularity of Ballerina
    private static final Map<String, BType> TYPE_MAP = new HashMap<>(20);

    public static final BType INT_TYPE = new BType(INT_TNAME);
    public static final BType LONG_TYPE = new BType(LONG_TNAME);
    public static final BType FLOAT_TYPE = new BType(FLOAT_TNAME);
    public static final BType DOUBLE_TYPE = new BType(DOUBLE_TNAME);
    public static final BType BOOLEAN_TYPE = new BType(BOOLEAN_TNAME);
    public static final BType STRING_TYPE = new BType(STRING_TNAME);
    public static final BType XML_TYPE = new BType(XML_TNAME);
    public static final BType JSON_TYPE = new BType(JSON_TNAME);
    public static final BType MESSAGE_TYPE = new BType(MESSAGE_TNAME);
    public static final BType CONNECTOR_TYPE = new BType(CONNECTOR_TNAME);
    public static final BType MAP_TYPE = new BType(MAP_TNAME);

    /**
     * Create a type from the given name
     *
     * @param typeName string name of the type
     */
    protected BType(String typeName) {
        this.typeName = typeName;
        TYPE_MAP.put(typeName, this);
    }

    public static BArrayType getArrayType(String elementTypeName) {
        String arrayTypeName = ARRAY_TNAME + elementTypeName;

        BArrayType aType = (BArrayType) TYPE_MAP.get(arrayTypeName);
        if (aType == null) {
            aType = new BArrayType(arrayTypeName, elementTypeName);
        }

        return aType;
    }

    public static boolean isValueType(BType type) {
        if (type == BType.INT_TYPE ||
                type == BType.STRING_TYPE ||
                type == BType.LONG_TYPE ||
                type == BType.FLOAT_TYPE ||
                type == BType.DOUBLE_TYPE ||
                type == BType.BOOLEAN_TYPE) {
            return true;
        }

        return false;
    }

    public String toString() {
        return typeName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BType) {
            BType other = (BType) obj;
            return this.typeName.equals(other.typeName);
        }
        return false;
    }

    public int hashCode() {
        return typeName.length();
    }

    public static BType getType(String typeName) {
        return TYPE_MAP.get(typeName);
    }
}
