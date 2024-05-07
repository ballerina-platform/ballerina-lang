/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.ParameterizedType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;

final class FallbackTypeChecker {

    private FallbackTypeChecker() {
    }

    static boolean checkIsType(List<String> errors, Object sourceVal, BType sourceType, BType targetType) {
        if (TypeChecker.checkIsType(sourceVal, sourceType, targetType, null)) {
            return true;
        }

        if (getImpliedType(sourceType).getTag() == TypeTags.XML_TAG && !targetType.isReadOnly()) {
            XmlValue val = (XmlValue) sourceVal;
            if (val.getNodeType() == XmlNodeType.SEQUENCE) {
                return TypeChecker.checkIsLikeOnValue(errors, sourceVal, sourceType, targetType, new ArrayList<>(),
                        false, null);
            }
        }

        if (TypeChecker.isMutable(sourceVal, sourceType)) {
            return false;
        }

        return TypeChecker.checkIsLikeOnValue(errors, sourceVal, sourceType, targetType, new ArrayList<>(), false,
                null);
    }

    @Deprecated
    static boolean checkIsType(BType sourceType, BType targetType, List<TypeChecker.TypePair> unresolvedTypes) {
        // First check whether both types are the same.
        if (sourceType == targetType || (sourceType.getTag() == targetType.getTag() && sourceType.equals(targetType))) {
            return true;
        }

        if (TypeChecker.checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(sourceType)) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        switch (sourceTypeTag) {
            case TypeTags.INTERSECTION_TAG:
                return TypeChecker.checkIsType(((IntersectionType) sourceType).getEffectiveType(),
                        targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                ((IntersectionType) targetType).getEffectiveType(), unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return TypeChecker.checkIsType(((ReferenceType) sourceType).getReferredType(),
                        targetTypeTag != TypeTags.TYPE_REFERENCED_TYPE_TAG ? targetType :
                                ((ReferenceType) targetType).getReferredType(), unresolvedTypes);
            case TypeTags.PARAMETERIZED_TYPE_TAG:
                if (targetTypeTag != TypeTags.PARAMETERIZED_TYPE_TAG) {
                    return TypeChecker.checkIsType(((ParameterizedType) sourceType).getParamValueType(), targetType,
                            unresolvedTypes);
                }
                return TypeChecker.checkIsType(((ParameterizedType) sourceType).getParamValueType(),
                        ((ParameterizedType) targetType).getParamValueType(), unresolvedTypes);
            case TypeTags.READONLY_TAG:
                return TypeChecker.checkIsType(PredefinedTypes.ANY_AND_READONLY_OR_ERROR_TYPE,
                        targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return TypeChecker.isUnionTypeMatch((BUnionType) sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                if ((targetTypeTag == TypeTags.FINITE_TYPE_TAG || targetTypeTag <= TypeTags.NULL_TAG ||
                        targetTypeTag == TypeTags.XML_TEXT_TAG)) {
                    return TypeChecker.isFiniteTypeMatch((BFiniteType) sourceType, targetType);
                }
                break;
            default:
                break;
        }

        return switch (targetTypeTag) {
            case TypeTags.BYTE_TAG, TypeTags.SIGNED8_INT_TAG, TypeTags.FLOAT_TAG, TypeTags.DECIMAL_TAG,
                 TypeTags.CHAR_STRING_TAG, TypeTags.BOOLEAN_TAG, TypeTags.NULL_TAG -> sourceTypeTag == targetTypeTag;
            case TypeTags.STRING_TAG -> TypeTags.isStringTypeTag(sourceTypeTag);
            case TypeTags.XML_TEXT_TAG -> {
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    yield ((BXmlType) sourceType).constraint.getTag() == TypeTags.NEVER_TAG;
                }
                yield sourceTypeTag == targetTypeTag;
            }
            case TypeTags.INT_TAG -> sourceTypeTag == TypeTags.INT_TAG || sourceTypeTag == TypeTags.BYTE_TAG ||
                    (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.UNSIGNED32_INT_TAG);
            case TypeTags.SIGNED16_INT_TAG -> sourceTypeTag == TypeTags.BYTE_TAG ||
                    (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.SIGNED16_INT_TAG);
            case TypeTags.SIGNED32_INT_TAG -> sourceTypeTag == TypeTags.BYTE_TAG ||
                    (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.SIGNED32_INT_TAG);
            case TypeTags.UNSIGNED8_INT_TAG ->
                    sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG;
            case TypeTags.UNSIGNED16_INT_TAG ->
                    sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG ||
                            sourceTypeTag == TypeTags.UNSIGNED16_INT_TAG;
            case TypeTags.UNSIGNED32_INT_TAG ->
                    sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG ||
                            sourceTypeTag == TypeTags.UNSIGNED16_INT_TAG ||
                            sourceTypeTag == TypeTags.UNSIGNED32_INT_TAG;
            case TypeTags.ANY_TAG -> TypeChecker.checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG -> sourceType.isAnydata();
            case TypeTags.SERVICE_TAG -> TypeChecker.checkIsServiceType(sourceType, targetType,
                    unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
            case TypeTags.HANDLE_TAG -> sourceTypeTag == TypeTags.HANDLE_TAG;
            case TypeTags.READONLY_TAG ->
                    TypeChecker.checkIsType(sourceType, PredefinedTypes.ANY_AND_READONLY_OR_ERROR_TYPE,
                            unresolvedTypes);
            case TypeTags.XML_ELEMENT_TAG, TypeTags.XML_COMMENT_TAG, TypeTags.XML_PI_TAG ->
                    targetTypeTag == sourceTypeTag;
            case TypeTags.INTERSECTION_TAG ->
                    TypeChecker.checkIsType(sourceType, ((IntersectionType) targetType).getEffectiveType(),
                            unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG ->
                    TypeChecker.checkIsType(sourceType, ((ReferenceType) targetType).getReferredType(),
                            unresolvedTypes);
            default -> TypeChecker.checkIsRecursiveType(sourceType, targetType,
                    unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
        };
    }

    static boolean checkIsType(Object sourceVal, BType sourceBType, BType targetBType,
                               List<TypeChecker.TypePair> unresolvedTypes) {
        Type sourceType = getImpliedType(sourceBType);
        Type targetType = getImpliedType(targetBType);

        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        // If the source type is neither a record type nor an object type, check `is` type by looking only at the types.
        // Else, since records and objects may have `readonly` or `final` fields, need to use the value also.
        // e.g.,
        //      const HUNDRED = 100;
        //
        //      type Foo record {
        //          HUNDRED i;
        //      };
        //
        //      type Bar record {
        //          readonly string|int i;
        //      };
        //
        // where `Bar b = {i: 100};`, `b is Foo` should evaluate to true.
        if (sourceTypeTag != TypeTags.RECORD_TYPE_TAG && sourceTypeTag != TypeTags.OBJECT_TYPE_TAG) {
            return TypeChecker.checkIsType(sourceType, targetType);
        }

        if (sourceType == targetType || (sourceType.getTag() == targetType.getTag() && sourceType.equals(targetType))) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        return switch (targetTypeTag) {
            case TypeTags.ANY_TAG -> TypeChecker.checkIsAnyType(sourceType);
            case TypeTags.READONLY_TAG -> TypeChecker.isInherentlyImmutableType(sourceType) || sourceType.isReadOnly();
            default -> TypeChecker.checkIsRecursiveTypeOnValue(sourceVal, sourceType, targetType, sourceTypeTag,
                    targetTypeTag, unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
        };
    }
}
