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

import org.wso2.ballerina.core.exception.BallerinaException;

import java.util.HashMap;
import java.util.Map;

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
 * {@code TypeC} represent a type in Ballerina
 *
 * @since 1.0.0
 */
public class TypeC {

    protected String typeName;

    //Using a HashMap here, because there won't be any concurrent access
    // TODO Improve this to support modularity of Ballerina
    protected static final Map<String, TypeC> TYPE_MAP = new HashMap<>(20);

    public static final TypeC INT_TYPE = new TypeC(INT_TNAME);
    public static final TypeC LONG_TYPE = new TypeC(LONG_TNAME);
    public static final TypeC FLOAT_TYPE = new TypeC(FLOAT_TNAME);
    public static final TypeC DOUBLE_TYPE = new TypeC(DOUBLE_TNAME);
    public static final TypeC BOOLEAN_TYPE = new TypeC(BOOLEAN_TNAME);
    public static final TypeC STRING_TYPE = new TypeC(STRING_TNAME);
    public static final TypeC XML_TYPE = new TypeC(XML_TNAME);
    public static final TypeC JSON_TYPE = new TypeC(JSON_TNAME);
    public static final TypeC MESSAGE_TYPE = new TypeC(MESSAGE_TNAME);
    public static final TypeC CONNECTOR_TYPE = new TypeC(CONNECTOR_TNAME);
    public static final TypeC MAP_TYPE = new TypeC(MAP_TNAME);

    /**
     * Create a type from the given name
     *
     * @param typeName string name of the type
     */
    protected TypeC(String typeName) {
        this.typeName = typeName;
        TYPE_MAP.put(typeName, this);
    }

    public String toString() {
        return typeName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TypeC) {
            TypeC other = (TypeC) obj;
            return this.typeName.equals(other.typeName);
        }
        return false;
    }

    public int hashCode() {
        return typeName.length();
    }

    public static TypeC getTypeC(String typeName) {
        return TYPE_MAP.get(typeName);
    }

    public static Type getType(String typeName) {
        switch (typeName) {
        case "int":
            return new IntType();
        case "long":
            return new LongType();
        case "float":
            return new FloatType();
        case "double":
            return new DoubleType();
        case "boolean":
            return new BooleanType();
        case "string":
            return new StringType();
        case "message":
            return new MessageType();
        case "xml":
            return new XMLType();
        case "json":
            return new JSONType();
        case "map":
            return new MapType();
        case "array":
            return new ArrayType();
        case "connector":
            return new ConnectorType();
        default:
            //TODO use proper exceptions here
            throw new BallerinaException("Unknown type");
        }

    }
}
