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

import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFiniteType;

import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_ANY;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_BYTE;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_DECIMAL;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_ERROR;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_FUTURE;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_JSON;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_MAP;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_NEVER;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_STREAM;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_TYPEDESC;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_XML;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_XML_ATTRIBUTES;

/**
 * This class contains various methods to manipulate {@link Type}s in Ballerina.
 *
 * @since 2.0.0
 */
public final class TypeUtils {

    private TypeUtils() {
    }

    public static boolean isValueType(Type type) {
        Type referredType = TypeUtils.getImpliedType(type);
        return switch (referredType.getTag()) {
            case TypeTags.INT_TAG,
                 TypeTags.BYTE_TAG,
                 TypeTags.FLOAT_TAG,
                 TypeTags.DECIMAL_TAG,
                 TypeTags.BOOLEAN_TAG,
                 TypeTags.STRING_TAG -> true;
            case TypeTags.FINITE_TYPE_TAG -> {
                for (Object value : ((BFiniteType) referredType).valueSpace) {
                    if (!isValueType(TypeChecker.getType(value))) {
                        yield false;
                    }
                }
                yield true;
            }
            default -> false;
        };
    }

    public static Type getTypeFromName(String typeName) {
        return switch (typeName) {
            case TypeConstants.INT_TNAME -> TYPE_INT;
            case TypeConstants.BYTE_TNAME -> TYPE_BYTE;
            case TypeConstants.FLOAT_TNAME -> TYPE_FLOAT;
            case TypeConstants.DECIMAL_TNAME -> TYPE_DECIMAL;
            case TypeConstants.STRING_TNAME -> TYPE_STRING;
            case TypeConstants.BOOLEAN_TNAME -> TYPE_BOOLEAN;
            case TypeConstants.JSON_TNAME -> TYPE_JSON;
            case TypeConstants.XML_TNAME -> TYPE_XML;
            case TypeConstants.MAP_TNAME -> TYPE_MAP;
            case TypeConstants.FUTURE_TNAME -> TYPE_FUTURE;
            case TypeConstants.STREAM_TNAME -> TYPE_STREAM;
            case TypeConstants.ANY_TNAME -> TYPE_ANY;
            case TypeConstants.TYPEDESC_TNAME -> TYPE_TYPEDESC;
            case TypeConstants.NULL_TNAME -> TYPE_NULL;
            case TypeConstants.XML_ATTRIBUTES_TNAME -> TYPE_XML_ATTRIBUTES;
            case TypeConstants.ERROR -> TYPE_ERROR;
            case TypeConstants.ANYDATA_TNAME -> TYPE_ANYDATA;
            case TypeConstants.NEVER_TNAME -> TYPE_NEVER;
            default -> throw new IllegalStateException("Unknown type name");
        };
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
        Type referredType = type.getCachedReferredType();
        if (referredType != null) {
            return referredType;
        }
        if (type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            Type rType = ((ReferenceType) type).getReferredType();
            if (rType == null) {
                referredType = type;
            } else {
                referredType = getReferredType(rType);
            }
        } else {
            referredType = type;
        }
        type.setCachedReferredType(referredType);
        return referredType;
    }

    /**
     * Retrieve the referred type if a given type is a type reference type or
     * retrieve the effective type if the given type is an intersection type.
     *
     * @param type type to retrieve the implied type
     * @return the implied type if provided with a type reference type or an intersection type,
     * else returns the original type
     */
    public static Type getImpliedType(Type type) {
        Type impliedType = type.getCachedImpliedType();
        if (impliedType != null) {
            return impliedType;
        }
        type = getReferredType(type);

        if (type.getTag() == TypeTags.INTERSECTION_TAG) {
            impliedType = getImpliedType(((IntersectionType) type).getEffectiveType());
        } else {
            impliedType = type;
        }
        type.setCachedImpliedType(impliedType);
        return impliedType;
    }
}
