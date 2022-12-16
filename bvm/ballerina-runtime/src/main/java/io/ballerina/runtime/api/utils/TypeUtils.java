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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BType;

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
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NEVER;
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

    private TypeUtils() {
    }

    public static boolean isValueType(Type type) {
        Type referredType = TypeUtils.getReferredType(type);
        switch (referredType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.STRING_TAG:
                return true;
            case TypeTags.FINITE_TYPE_TAG:
                for (Object value : ((BFiniteType) referredType).valueSpace) {
                    if (!isValueType(TypeChecker.getType(value))) {
                        return false;
                    }
                }
                return true;
            default:
                return false;

        }
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
            case TypeConstants.NEVER_TNAME:
                return TYPE_NEVER;
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

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    public static boolean isSameType(Type sourceType, Type targetType) {
        return TypeChecker.isSameType(sourceType, targetType);
    }

    /**
     * Retrieve the referred type if a given type is a type reference type.
     *
     * @param type type to retrieve referred
     * @return the referred type if provided with a type reference type, else returns the original type
     */
    public static Type getReferredType(Type type) {
        Type constraint = type;
        if (type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            constraint = getReferredType(((ReferenceType) type).getReferredType());
        }
        return constraint;
    }

}
