/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api.utils;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BFiniteType;
import io.ballerina.runtime.types.BType;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANY;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BYTE;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_DECIMAL;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ERROR;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FUTURE;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_JSON;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_MAP;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STREAM;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_TYPEDESC;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_XML;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_XML_ATTRIBUTES;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
 *
 * @since 2.0.0
 */
public class TypeUtils {

    public static boolean isValueType(Type type) {
        if (type == TYPE_INT || type == TYPE_BYTE ||
                type == TYPE_FLOAT ||
                type == TYPE_DECIMAL || type == TYPE_STRING ||
                type == TYPE_BOOLEAN) {
            return true;
        }

        if (type != null && type.getTag() == TypeTags.FINITE_TYPE_TAG) {
            // All the types in value space should be value types.
            for (Object value : ((BFiniteType) type).valueSpace) {
                if (!isValueType(TypeChecker.getType(value))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Type getTypeFromName(String typeName) {
        switch (typeName) {
            case TypeConstants.INT_TNAME:
                return TYPE_INT;
            case TypeConstants.BYTE_TNAME:
                return TYPE_BYTE;
            case TypeConstants.FLOAT_TNAME:
                return TYPE_FLOAT;
            case TypeConstants.DECIMAL_TNAME:
                return TYPE_DECIMAL;
            case TypeConstants.STRING_TNAME:
                return TYPE_STRING;
            case TypeConstants.BOOLEAN_TNAME:
                return TYPE_BOOLEAN;
            case TypeConstants.JSON_TNAME:
                return TYPE_JSON;
            case TypeConstants.XML_TNAME:
                return TYPE_XML;
            case TypeConstants.MAP_TNAME:
                return TYPE_MAP;
            case TypeConstants.FUTURE_TNAME:
                return TYPE_FUTURE;
            case TypeConstants.STREAM_TNAME:
                return TYPE_STREAM;
            case TypeConstants.ANY_TNAME:
                return TYPE_ANY;
            case TypeConstants.TYPEDESC_TNAME:
                return TYPE_TYPEDESC;
            case TypeConstants.NULL_TNAME:
                return TYPE_NULL;
            case TypeConstants.XML_ATTRIBUTES_TNAME:
                return TYPE_XML_ATTRIBUTES;
            case TypeConstants.ERROR:
                return TYPE_ERROR;
            case TypeConstants.ANYDATA_TNAME:
                return TYPE_ANYDATA;
            default:
                throw new IllegalStateException("Unknown type name");
        }
    }

    public static Type fromString(String typeName) {
        if (typeName.endsWith("[]")) {
            String elementTypeName = typeName.substring(0, typeName.length() - 2);
            Type elemType = fromString(elementTypeName);
            return new BArrayType(elemType);
        }
        return getTypeFromName(typeName);
    }

    public static Type getType(Object value) {
        return TypeChecker.getType(value);
    }
}
