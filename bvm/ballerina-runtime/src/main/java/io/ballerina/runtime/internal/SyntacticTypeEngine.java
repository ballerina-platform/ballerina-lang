/*
 *
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 * /
 *
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BField;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BNetworkObjectType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BResourceMethodType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeIdSet;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.StreamValue;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANY;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_JSON;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_READONLY_JSON;
import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.api.utils.TypeUtils.isValueType;
import static io.ballerina.runtime.internal.TypeChecker.MAX_TYPECAST_ERROR_COUNT;
import static io.ballerina.runtime.internal.TypeHelper.arrayState;
import static io.ballerina.runtime.internal.TypeHelper.effectiveType;
import static io.ballerina.runtime.internal.TypeHelper.listMemberTypes;
import static io.ballerina.runtime.internal.TypeHelper.listRestType;
import static io.ballerina.runtime.internal.TypeHelper.paramValueType;
import static io.ballerina.runtime.internal.TypeHelper.mappingRestFieldType;
import static io.ballerina.runtime.internal.TypeHelper.mappingRequiredFields;
import static io.ballerina.runtime.internal.TypeHelper.mappingTypeSealed;
import static io.ballerina.runtime.internal.TypeHelper.referredType;
import static io.ballerina.runtime.internal.TypeHelper.typeConstraint;
import static io.ballerina.runtime.internal.TypeConverter.ERROR_MESSAGE_UNION_END;
import static io.ballerina.runtime.internal.TypeConverter.ERROR_MESSAGE_UNION_SEPARATOR;
import static io.ballerina.runtime.internal.TypeConverter.ERROR_MESSAGE_UNION_START;

public class SyntacticTypeEngine {

    static boolean checkIsType(List<String> errors, Object sourceVal, BType sourceType, BType targetType) {
        if (TypeChecker.checkIsType(sourceVal, sourceType, targetType, null)) {
            return true;
        }

        if (getImpliedType(sourceType).getTag() == TypeTags.XML_TAG && !targetType.isReadOnly()) {
            XmlValue val = (XmlValue) sourceVal;
            if (val.getNodeType() == XmlNodeType.SEQUENCE) {
                return checkIsLikeOnValue(errors, sourceVal, sourceType, targetType, new ArrayList<>(), false, null);
            }
        }

        if (isMutable(sourceVal, sourceType)) {
            return false;
        }

        return checkIsLikeOnValue(errors, sourceVal, sourceType, targetType, new ArrayList<>(), false, null);
    }

    static boolean isSameType(BType sourceType, BType targetType) {
        return sourceType == targetType || sourceType.equals(targetType);
    }

    static boolean checkIsType(BType sourceType, BType targetType) {
        return checkIsType(sourceType, targetType, null);
    }

    static boolean checkIsType(BType sourceType, BType targetType, List<TypeChecker.TypePair> unresolvedTypes) {
        // First check whether both types are the same.
        if (sourceType == targetType || (sourceType.getTag() == targetType.getTag() && sourceType.equals(targetType))) {
            return true;
        }

        if (MapTypeChecker.checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(sourceType)) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        switch (sourceTypeTag) {
            case TypeTags.INTERSECTION_TAG:
                return TypeChecker.checkIsType(effectiveType(sourceType),
                        targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                TypeHelper.effectiveType(targetType), unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return TypeChecker.checkIsType(referredType(sourceType),
                        targetTypeTag != TypeTags.TYPE_REFERENCED_TYPE_TAG ? targetType :
                                TypeHelper.referredType(targetType), unresolvedTypes);
            case TypeTags.PARAMETERIZED_TYPE_TAG:
                Type paramValueType = paramValueType(sourceType);
                if (targetTypeTag != TypeTags.PARAMETERIZED_TYPE_TAG) {
                    return TypeChecker.checkIsType(paramValueType, targetType, unresolvedTypes);
                }
                return TypeChecker.checkIsType(paramValueType, paramValueType(targetType), unresolvedTypes);
            case TypeTags.READONLY_TAG:
                return TypeChecker.checkIsType(PredefinedTypes.ANY_AND_READONLY_OR_ERROR_TYPE,
                        targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return TypeChecker.isUnionTypeMatch(sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                if ((targetTypeTag == TypeTags.FINITE_TYPE_TAG || targetTypeTag <= TypeTags.NULL_TAG ||
                        targetTypeTag == TypeTags.XML_TEXT_TAG)) {
                    return TypeChecker.isFiniteTypeMatch(sourceType, targetType);
                }
                break;
            default:
                break;
        }

        switch (targetTypeTag) {
            case TypeTags.BYTE_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.CHAR_STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
                return sourceTypeTag == targetTypeTag;
            case TypeTags.STRING_TAG:
                return TypeTags.isStringTypeTag(sourceTypeTag);
            case TypeTags.XML_TEXT_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    return TypeHelper.typeConstraint(sourceType).getTag() == TypeTags.NEVER_TAG;
                }
                return sourceTypeTag == targetTypeTag;
            case TypeTags.INT_TAG:
                return sourceTypeTag == TypeTags.INT_TAG || sourceTypeTag == TypeTags.BYTE_TAG ||
                        (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.UNSIGNED32_INT_TAG);
            case TypeTags.SIGNED16_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG ||
                        (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.SIGNED16_INT_TAG);
            case TypeTags.SIGNED32_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG ||
                        (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.SIGNED32_INT_TAG);
            case TypeTags.UNSIGNED8_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG;
            case TypeTags.UNSIGNED16_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG ||
                        sourceTypeTag == TypeTags.UNSIGNED16_INT_TAG;
            case TypeTags.UNSIGNED32_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG ||
                        sourceTypeTag == TypeTags.UNSIGNED16_INT_TAG || sourceTypeTag == TypeTags.UNSIGNED32_INT_TAG;
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG:
                return sourceType.isAnydata();
            case TypeTags.SERVICE_TAG:
                return ObjectTypeChecker.checkIsServiceType(sourceType, targetType,
                        unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
            case TypeTags.HANDLE_TAG:
                return sourceTypeTag == TypeTags.HANDLE_TAG;
            case TypeTags.READONLY_TAG:
                return TypeChecker.checkIsType(sourceType, PredefinedTypes.ANY_AND_READONLY_OR_ERROR_TYPE,
                        unresolvedTypes);
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
                return targetTypeTag == sourceTypeTag;
            case TypeTags.INTERSECTION_TAG:
                return TypeChecker.checkIsType(sourceType, TypeHelper.effectiveType(targetType), unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return TypeChecker.checkIsType(sourceType, TypeHelper.referredType(targetType), unresolvedTypes);
            default:
                return checkIsRecursiveType(sourceType, targetType,
                        unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
        }
    }

    static boolean isInherentlyImmutableType(BType sourceType) {
        sourceType = unwrap(getImpliedType(sourceType));
        if (isSimpleBasicType(sourceType)) {
            return true;
        }

        switch (sourceType.getTag()) {
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.FINITE_TYPE_TAG: // Assuming a finite type will only have members from simple basic types.
            case TypeTags.READONLY_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.NEVER_TAG:
            case TypeTags.ERROR_TAG:
            case TypeTags.INVOKABLE_TAG:
            case TypeTags.SERVICE_TAG:
            case TypeTags.TYPEDESC_TAG:
            case TypeTags.FUNCTION_POINTER_TAG:
            case TypeTags.HANDLE_TAG:
            case TypeTags.REG_EXP_TYPE_TAG:
                return true;
            case TypeTags.XML_TAG:
                return TypeHelper.typeConstraint(sourceType).getTag() == TypeTags.NEVER_TAG;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return TypeChecker.isInherentlyImmutableType(TypeHelper.referredType(sourceType));
            default:
                return false;
        }
    }

    static boolean isSimpleBasicType(BType type) {
        return getImpliedType(type).getTag() < TypeTags.NULL_TAG;
    }

    static boolean isNumericType(BType type) {
        type = (BType) getImpliedType(type);
        return type.getTag() < TypeTags.STRING_TAG || TypeTags.isIntegerTypeTag(type.getTag());
    }

    static boolean isSelectivelyImmutableType(BType type, Set<Type> unresolvedTypes) {
        if (!unresolvedTypes.add(type)) {
            return true;
        }

        switch (type.getTag()) {
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.XML_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                Type elementType = TypeHelper.listRestType(type);
                return TypeChecker.isInherentlyImmutableType(elementType) ||
                        TypeChecker.isSelectivelyImmutableType(elementType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                for (Type tupMemType : TypeHelper.listMemberTypes(type)) {
                    if (!TypeChecker.isInherentlyImmutableType(tupMemType) &&
                            !TypeChecker.isSelectivelyImmutableType(tupMemType, unresolvedTypes)) {
                        return false;
                    }
                }

                Type tupRestType = TypeHelper.listRestType(type);
                if (tupRestType == null) {
                    return true;
                }

                return TypeChecker.isInherentlyImmutableType(tupRestType) ||
                        TypeChecker.isSelectivelyImmutableType(tupRestType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) type;
                for (Field field : recordType.getFields().values()) {
                    Type fieldType = field.getFieldType();
                    if (!TypeChecker.isInherentlyImmutableType(fieldType) &&
                            !TypeChecker.isSelectivelyImmutableType(fieldType, unresolvedTypes)) {
                        return false;
                    }
                }

                Type recordRestType = recordType.restFieldType;
                if (recordRestType == null) {
                    return true;
                }

                return TypeChecker.isInherentlyImmutableType(recordRestType) ||
                        TypeChecker.isSelectivelyImmutableType(recordRestType, unresolvedTypes);
            case TypeTags.OBJECT_TYPE_TAG:
                BObjectType objectType = (BObjectType) type;

                if (SymbolFlags.isFlagOn(objectType.flags, SymbolFlags.CLASS) &&
                        !SymbolFlags.isFlagOn(objectType.flags, SymbolFlags.READONLY)) {
                    return false;
                }

                for (Field field : objectType.getFields().values()) {
                    Type fieldType = field.getFieldType();
                    if (!TypeChecker.isInherentlyImmutableType(fieldType) &&
                            !TypeChecker.isSelectivelyImmutableType(fieldType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.MAP_TAG:
                Type constraintType = ((BMapType) type).getConstrainedType();
                return TypeChecker.isInherentlyImmutableType(constraintType) ||
                        TypeChecker.isSelectivelyImmutableType(constraintType, unresolvedTypes);
            case TypeTags.TABLE_TAG:
                Type tableConstraintType = ((BTableType) type).getConstrainedType();
                return TypeChecker.isInherentlyImmutableType(tableConstraintType) ||
                        TypeChecker.isSelectivelyImmutableType(tableConstraintType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                boolean readonlyIntersectionExists = false;
                for (Type memberType : ((BUnionType) type).getMemberTypes()) {
                    if (TypeChecker.isInherentlyImmutableType(memberType) ||
                            TypeChecker.isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                        readonlyIntersectionExists = true;
                        break;
                    }
                }
                return readonlyIntersectionExists;
            case TypeTags.INTERSECTION_TAG:
                return TypeChecker.isSelectivelyImmutableType(((BIntersectionType) type).getEffectiveType(),
                        unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return TypeChecker.isSelectivelyImmutableType(((BTypeReferenceType) type).getReferredType(),
                        unresolvedTypes);
            default:
                return false;
        }
    }

    static boolean checkIsType(Object sourceVal, BType sourceType, BType targetType,
                               List<TypeChecker.TypePair> unresolvedTypes) {
        sourceType = unwrap(getImpliedType(sourceType));
        targetType = unwrap(getImpliedType(targetType));

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
            return checkIsType(sourceType, targetType);
        }

        if (sourceType == targetType || (sourceType.getTag() == targetType.getTag() && sourceType.equals(targetType))) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        switch (targetTypeTag) {
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.READONLY_TAG:
                return isInherentlyImmutableType(sourceType) || sourceType.isReadOnly();
            default:
                return checkIsRecursiveTypeOnValue(sourceVal, sourceType, targetType, sourceTypeTag, targetTypeTag,
                        unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
        }
    }

    /**
     * Check whether a given value confirms to a given type. First it checks if the type of the value, and if fails then
     * falls back to checking the value.
     *
     * @param errors                 list to collect typecast errors
     * @param sourceValue            Value to check
     * @param targetType             Target type
     * @param unresolvedValues       Values that are unresolved so far
     * @param allowNumericConversion Flag indicating whether to perform numeric conversions
     * @param varName                variable name to identify the parent of a record field
     * @return True if the value confirms to the provided type. False, otherwise.
     */
    static boolean checkIsLikeType(List<String> errors, Object sourceValue, BType targetType,
                                   List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion, String varName) {
        Type sourceType = TypeChecker.getType(sourceValue);
        if (TypeChecker.checkIsType(sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        return checkIsLikeOnValue(errors, sourceValue, sourceType, targetType, unresolvedValues, allowNumericConversion,
                varName);
    }

    /**
     * Check whether a given value confirms to a given type. Strictly checks the value only, and does not consider the
     * type of the value for consideration.
     *
     * @param errors                 list to collect typecast errors
     * @param sourceValue            Value to check
     * @param sourceType             Type of the value
     * @param targetType             Target type
     * @param unresolvedValues       Values that are unresolved so far
     * @param allowNumericConversion Flag indicating whether to perform numeric conversions
     * @param varName                variable name to identify the parent of a record field
     * @return True if the value confirms to the provided type. False, otherwise.
     */
    private static boolean checkIsLikeOnValue(List<String> errors, Object sourceValue, Type sourceType,
                                              Type targetType, List<TypeValuePair> unresolvedValues,
                                              boolean allowNumericConversion, String varName) {
        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        switch (sourceTypeTag) {
            case TypeTags.INTERSECTION_TAG:
                return checkIsLikeOnValue(errors, sourceValue, TypeHelper.effectiveType(sourceType),
                        targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                TypeHelper.effectiveType(targetType), unresolvedValues,
                        allowNumericConversion, varName);
            case TypeTags.PARAMETERIZED_TYPE_TAG:
                if (targetTypeTag != TypeTags.PARAMETERIZED_TYPE_TAG) {
                    return checkIsLikeOnValue(errors, sourceValue, TypeHelper.paramValueType(sourceType),
                            targetType, unresolvedValues, allowNumericConversion, varName);
                }
                return checkIsLikeOnValue(errors, sourceValue, TypeHelper.paramValueType(sourceType),
                        TypeHelper.paramValueType(targetType), unresolvedValues, allowNumericConversion,
                        varName);
            default:
                break;
        }

        switch (targetTypeTag) {
            case TypeTags.READONLY_TAG:
                return true;
            case TypeTags.BYTE_TAG:
                if (TypeTags.isIntegerTypeTag(sourceTypeTag)) {
                    return TypeChecker.isByteLiteral(((Number) sourceValue).longValue());
                }
                return allowNumericConversion && TypeConverter.isConvertibleToByte(sourceValue);
            case TypeTags.INT_TAG:
                return allowNumericConversion && TypeConverter.isConvertibleToInt(sourceValue);
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                if (TypeTags.isIntegerTypeTag(sourceTypeTag)) {
                    return TypeConverter.isConvertibleToIntSubType(sourceValue, targetType);
                }
                return allowNumericConversion && TypeConverter.isConvertibleToIntSubType(sourceValue, targetType);
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                return allowNumericConversion && TypeConverter.isConvertibleToFloatingPointTypes(sourceValue);
            case TypeTags.CHAR_STRING_TAG:
                return TypeConverter.isConvertibleToChar(sourceValue);
            case TypeTags.RECORD_TYPE_TAG:
                return TypeChecker.checkIsLikeType(sourceValue, targetType, unresolvedValues, allowNumericConversion,
                        varName, errors);
            case TypeTags.TABLE_TAG:
            case TypeTags.MAP_TAG:
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
            case TypeTags.ERROR_TAG:
                return TypeChecker.checkIsLikeType(sourceValue, targetType, unresolvedValues, allowNumericConversion);
            case TypeTags.JSON_TAG:
                return TypeChecker.checkIsLikeType(sourceValue, sourceType, targetType, unresolvedValues,
                        allowNumericConversion);
            case TypeTags.STREAM_TAG:
                return TypeChecker.checkIsLikeType(sourceValue, targetType);
            case TypeTags.ANYDATA_TAG:
                return checkIsLikeAnydataType(sourceValue, sourceType, unresolvedValues, allowNumericConversion);
            case TypeTags.FINITE_TYPE_TAG:
                return TypeChecker.checkFiniteTypeAssignable(sourceValue, sourceType, targetType, unresolvedValues,
                        allowNumericConversion);
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                if (TypeTags.isXMLTypeTag(sourceTypeTag)) {
                    return XmlTypeChecker.checkIsLikeXmlValueSingleton((XmlValue) sourceValue, targetType);
                }
                return false;
            case TypeTags.XML_TAG:
                if (TypeTags.isXMLTypeTag(sourceTypeTag)) {
                    return XmlTypeChecker.checkIsLikeXMLSequenceType((XmlValue) sourceValue, targetType);
                }
                return false;
            case TypeTags.UNION_TAG:
                return TypeChecker.checkIsLikeType(errors, sourceValue, targetType, unresolvedValues,
                        allowNumericConversion, varName);
            case TypeTags.INTERSECTION_TAG:
                return checkIsLikeOnValue(errors, sourceValue, sourceType,
                        TypeHelper.effectiveType(targetType), unresolvedValues, allowNumericConversion,
                        varName);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsLikeOnValue(errors, sourceValue, sourceType,
                        TypeHelper.referredType(targetType), unresolvedValues, allowNumericConversion,
                        varName);
            default:
                return false;
        }
    }

    private static boolean isMutable(Object value, BType sourceType) {
        // All the value types are immutable
        sourceType = unwrap(getImpliedType(sourceType));
        if (value == null || sourceType.getTag() < TypeTags.NULL_TAG ||
                sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        return !((BRefValue) value).isFrozen();
    }

    static boolean checkIsLikeUnionType(List<String> errors, Object sourceValue, BUnionType targetType,
                                                List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                                String varName) {
        if (allowNumericConversion) {
            List<Type> compatibleTypesWithNumConversion = new ArrayList<>();
            List<Type> compatibleTypesWithoutNumConversion = new ArrayList<>();
            for (Type type : targetType.getMemberTypes()) {
                List<TypeValuePair> tempList = new ArrayList<>(unresolvedValues.size());
                tempList.addAll(unresolvedValues);

                if (TypeChecker.checkIsLikeType(null, sourceValue, type, tempList, false, varName)) {
                    compatibleTypesWithoutNumConversion.add(type);
                }

                if (TypeChecker.checkIsLikeType(null, sourceValue, type, unresolvedValues, true, varName)) {
                    compatibleTypesWithNumConversion.add(type);
                }
            }
            // Conversion should only be possible to one other numeric type.
            return !compatibleTypesWithNumConversion.isEmpty() &&
                    compatibleTypesWithNumConversion.size() - compatibleTypesWithoutNumConversion.size() <= 1;
        } else {
            return checkIsLikeUnionType(errors, sourceValue, targetType, unresolvedValues, varName);
        }
    }

    private static boolean checkIsLikeUnionType(List<String> errors, Object sourceValue, BUnionType targetType,
                                                List<TypeValuePair> unresolvedValues, String varName) {
        if (errors == null) {
            for (Type type : targetType.getMemberTypes()) {
                if (TypeChecker.checkIsLikeType(null, sourceValue, type, unresolvedValues, false, varName)) {
                    return true;
                }
            }
        } else {
            int initialErrorCount;
            errors.add(ERROR_MESSAGE_UNION_START);
            int initialErrorListSize = errors.size();
            for (Type type : targetType.getMemberTypes()) {
                initialErrorCount = errors.size();
                if (TypeChecker.checkIsLikeType(errors, sourceValue, type, unresolvedValues, false, varName)) {
                    errors.subList(initialErrorListSize - 1, errors.size()).clear();
                    return true;
                }
                if (initialErrorCount != errors.size()) {
                    errors.add(ERROR_MESSAGE_UNION_SEPARATOR);
                }
            }
            int currentErrorListSize = errors.size();
            errors.remove(currentErrorListSize - 1);
            if (initialErrorListSize != currentErrorListSize) {
                errors.add(ERROR_MESSAGE_UNION_END);
            }
        }
        return false;
    }

    private static boolean isLikeAnydataType(Object[] objects, List<TypeValuePair> unresolvedValues,
                                             boolean allowNumericConversion) {
        for (Object value : objects) {
            if (!TypeChecker.checkIsLikeType(null, value, TYPE_ANYDATA, unresolvedValues, allowNumericConversion,
                    null)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeAnydataType(Object sourceValue, Type sourceType,
                                                  List<TypeValuePair> unresolvedValues,
                                                  boolean allowNumericConversion) {
        sourceType = getImpliedType(sourceType);
        switch (sourceType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                return isLikeAnydataType(((MapValueImpl) sourceValue).values().toArray(),
                        unresolvedValues, allowNumericConversion);
            case TypeTags.TABLE_TAG:
                return isLikeAnydataType(((TableValueImpl) sourceValue).values().toArray(),
                        unresolvedValues, allowNumericConversion);
            case TypeTags.ARRAY_TAG:
                ArrayValue arr = (ArrayValue) sourceValue;
                BArrayType arrayType = (BArrayType) getImpliedType(arr.getType());
                switch (getImpliedType(arrayType.getElementType()).getTag()) {
                    case TypeTags.INT_TAG:
                    case TypeTags.FLOAT_TAG:
                    case TypeTags.DECIMAL_TAG:
                    case TypeTags.STRING_TAG:
                    case TypeTags.BOOLEAN_TAG:
                    case TypeTags.BYTE_TAG:
                        return true;
                    default:
                        return isLikeAnydataType(arr.getValues(), unresolvedValues, allowNumericConversion);
                }
            case TypeTags.TUPLE_TAG:
                return isLikeAnydataType(((ArrayValue) sourceValue).getValues(), unresolvedValues,
                        allowNumericConversion);
            default:
                return sourceType.isAnydata();
        }
    }

    static boolean checkFiniteTypeAssignable(Object sourceValue, Type sourceType, BFiniteType targetType,
                                                     List<TypeValuePair> unresolvedValues,
                                                     boolean allowNumericConversion) {
        if (targetType.valueSpace.size() == 1) {
            Type valueType = getImpliedType(TypeChecker.getType(targetType.valueSpace.iterator().next()));
            if (!TypeChecker.isSimpleBasicType(valueType) && valueType.getTag() != TypeTags.NULL_TAG) {
                return checkIsLikeOnValue(null, sourceValue, sourceType, valueType, unresolvedValues,
                        allowNumericConversion, null);
            }
        }

        for (Object valueSpaceItem : targetType.valueSpace) {
            // TODO: 8/13/19 Maryam fix for conversion
            if (isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem, allowNumericConversion)) {
                return true;
            }
        }
        return false;
    }

    static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                     boolean allowNumericConversion) {
        Type valueSpaceItemType = TypeChecker.getType(valueSpaceItem);
        int sourceTypeTag = getImpliedType(sourceType).getTag();
        int valueSpaceItemTypeTag = getImpliedType(valueSpaceItemType).getTag();
        if (valueSpaceItemTypeTag > TypeTags.DECIMAL_TAG) {
            return valueSpaceItemTypeTag == sourceTypeTag &&
                    (valueSpaceItem == sourceValue || valueSpaceItem.equals(sourceValue));
        }

        switch (sourceTypeTag) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                switch (valueSpaceItemTypeTag) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return ((Number) sourceValue).longValue() == ((Number) valueSpaceItem).longValue();
                    case TypeTags.FLOAT_TAG:
                        return ((Number) sourceValue).longValue() == ((Number) valueSpaceItem).longValue() &&
                                allowNumericConversion;
                    case TypeTags.DECIMAL_TAG:
                        return ((Number) sourceValue).longValue() == ((DecimalValue) valueSpaceItem).intValue() &&
                                allowNumericConversion;
                }
            case TypeTags.FLOAT_TAG:
                switch (valueSpaceItemTypeTag) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return ((Number) sourceValue).doubleValue() == ((Number) valueSpaceItem).doubleValue()
                                && allowNumericConversion;
                    case TypeTags.FLOAT_TAG:
                        return (((Number) sourceValue).doubleValue() == ((Number) valueSpaceItem).doubleValue() ||
                                (Double.isNaN((Double) sourceValue) && Double.isNaN((Double) valueSpaceItem)));
                    case TypeTags.DECIMAL_TAG:
                        return ((Number) sourceValue).doubleValue() == ((DecimalValue) valueSpaceItem).floatValue()
                                && allowNumericConversion;
                }
            case TypeTags.DECIMAL_TAG:
                switch (valueSpaceItemTypeTag) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return TypeChecker.checkDecimalEqual((DecimalValue) sourceValue,
                                DecimalValue.valueOf(((Number) valueSpaceItem).longValue())) && allowNumericConversion;
                    case TypeTags.FLOAT_TAG:
                        return TypeChecker.checkDecimalEqual((DecimalValue) sourceValue,
                                DecimalValue.valueOf(((Number) valueSpaceItem).doubleValue())) &&
                                allowNumericConversion;
                    case TypeTags.DECIMAL_TAG:
                        return TypeChecker.checkDecimalEqual((DecimalValue) sourceValue, (DecimalValue) valueSpaceItem);
                }
            default:
                if (sourceTypeTag != valueSpaceItemTypeTag) {
                    return false;
                }
                return valueSpaceItem.equals(sourceValue);
        }
    }

    private static boolean checkIsRecursiveTypeOnValue(Object sourceVal, Type sourceType, Type targetType,
                                                       int sourceTypeTag, int targetTypeTag,
                                                       List<TypeChecker.TypePair> unresolvedTypes) {
        // FIXME:
        switch (targetTypeTag) {
            case TypeTags.ANYDATA_TAG:
                if (sourceTypeTag == TypeTags.OBJECT_TYPE_TAG) {
                    return false;
                }
                return MapTypeChecker.checkRecordBelongsToAnydataType((MapValue) sourceVal, (BRecordType) sourceType,
                        unresolvedTypes);
            case TypeTags.MAP_TAG:
                return MapTypeChecker.checkIsMapType(sourceVal, sourceType, (BMapType) targetType, unresolvedTypes);
            case TypeTags.JSON_TAG:
                return MapTypeChecker.checkIsMapType(sourceVal, sourceType,
                        new BMapType(targetType.isReadOnly() ? TYPE_READONLY_JSON :
                                TYPE_JSON), unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return MapTypeChecker.checkIsRecordType(sourceVal, sourceType, (BRecordType) targetType,
                        unresolvedTypes);
            case TypeTags.UNION_TAG:
                for (Type type : ((BUnionType) targetType).getMemberTypes()) {
                    if (TypeChecker.checkIsType(sourceVal, sourceType, type, unresolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.OBJECT_TYPE_TAG:
                return ObjectTypeChecker.checkObjectEquivalency(sourceVal, sourceType, (BObjectType) targetType,
                        unresolvedTypes);
            default:
                return false;
        }
    }

    static boolean isFiniteTypeMatch(BFiniteType sourceType, Type targetType) {
        for (Object bValue : sourceType.valueSpace) {
            if (!TypeChecker.checkIsType(bValue, targetType)) {
                return false;
            }
        }
        return true;
    }

    static boolean isUnionTypeMatch(BUnionType sourceType, Type targetType,
                                    List<TypeChecker.TypePair> unresolvedTypes) {
        for (Type type : sourceType.getMemberTypes()) {
            if (!TypeChecker.checkIsType(type, targetType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    static boolean checkIsUnionType(Type sourceType, BUnionType targetType,
                                    List<TypeChecker.TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        sourceType = getImpliedType(sourceType);
        TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        switch (sourceType.getTag()) {
            case TypeTags.UNION_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.ANYDATA_TAG:
                return isUnionTypeMatch((BUnionType) sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
            default:
                for (Type type : targetType.getMemberTypes()) {
                    if (TypeChecker.checkIsType(sourceType, type, unresolvedTypes)) {
                        return true;
                    }
                }
                return false;

        }
    }

    private static boolean checkIsRecursiveType(Type sourceType, Type targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                return MapTypeChecker.checkIsMapType(sourceType, (BMapType) targetType, unresolvedTypes);
            case TypeTags.STREAM_TAG:
                return StreamTypeChecker.checkIsStreamType(sourceType, (BStreamType) targetType, unresolvedTypes);
            case TypeTags.TABLE_TAG:
                return TableTypeChecker.checkIsTableType(sourceType, (BTableType) targetType, unresolvedTypes);
            case TypeTags.JSON_TAG:
                return MapTypeChecker.checkIsJSONType(sourceType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return MapTypeChecker.checkIsRecordType(sourceType, (BRecordType) targetType, unresolvedTypes);
            case TypeTags.FUNCTION_POINTER_TAG:
                return FunctionTypeChecker.checkIsFunctionType(sourceType, (BFunctionType) targetType);
            case TypeTags.ARRAY_TAG:
                return ListTypeChecker.checkIsArrayType(sourceType, (BArrayType) targetType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                return MapTypeChecker.checkIsTupleType(sourceType, (BTupleType) targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return checkIsUnionType(sourceType, (BUnionType) targetType, unresolvedTypes);
            case TypeTags.OBJECT_TYPE_TAG:
                return ObjectTypeChecker.checkObjectEquivalency(sourceType, (BObjectType) targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return checkIsFiniteType(sourceType, (BFiniteType) targetType);
            case TypeTags.FUTURE_TAG:
                return FutureTypeChecker.checkIsFutureType(sourceType, (BFutureType) targetType, unresolvedTypes);
            case TypeTags.ERROR_TAG:
                return ErrorTypeChecker.checkIsErrorType(sourceType, (BErrorType) targetType, unresolvedTypes);
            case TypeTags.TYPEDESC_TAG:
                return TypeDescTypeChecker.checkTypeDescType(sourceType, (BTypedescType) targetType, unresolvedTypes);
            case TypeTags.XML_TAG:
                return XmlTypeChecker.checkIsXMLType(sourceType, targetType, unresolvedTypes);
            default:
                // other non-recursive types shouldn't reach here
                return false;
        }
    }

    private static boolean checkIsAnyType(Type sourceType) {
        sourceType = getImpliedType(sourceType);
        switch (sourceType.getTag()) {
            case TypeTags.ERROR_TAG:
            case TypeTags.READONLY_TAG:
                return false;
            case TypeTags.UNION_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsAnyType(memberType)) {
                        return false;
                    }
                }
                return true;
            default:
                return true;
        }
    }

    private static boolean checkIsFiniteType(Type sourceType, BFiniteType targetType) {
        sourceType = getImpliedType(sourceType);
        if (sourceType.getTag() != TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        BFiniteType sourceFiniteType = (BFiniteType) sourceType;
        if (sourceFiniteType.valueSpace.size() != targetType.valueSpace.size()) {
            return false;
        }

        return targetType.valueSpace.containsAll(sourceFiniteType.valueSpace);
    }

    private static final class FutureTypeChecker {

        private static boolean checkIsFutureType(Type sourceType, BFutureType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            if (sourceType.getTag() != TypeTags.FUTURE_TAG) {
                return false;
            }
            return TableTypeChecker.checkConstraints(((BFutureType) sourceType).getConstrainedType(),
                    targetType.getConstrainedType(),
                    unresolvedTypes);
        }
    }

    static final class StreamTypeChecker {

        static boolean checkIsLikeStreamType(Object sourceValue, BStreamType targetType) {
            if (!(sourceValue instanceof StreamValue)) {
                return false;
            }

            BStreamType streamType = (BStreamType) ((StreamValue) sourceValue).getType();

            return streamType.getConstrainedType() == targetType.getConstrainedType();
        }

        private static boolean checkIsStreamType(Type sourceType, BStreamType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            if (sourceType.getTag() != TypeTags.STREAM_TAG) {
                return false;
            }
            return TableTypeChecker.checkConstraints(((BStreamType) sourceType).getConstrainedType(),
                    targetType.getConstrainedType(),
                    unresolvedTypes)
                    && TableTypeChecker.checkConstraints(((BStreamType) sourceType).getCompletionType(),
                    targetType.getCompletionType(),
                    unresolvedTypes);
        }
    }

    static final class ErrorTypeChecker {

        static boolean checkIsLikeErrorType(Object sourceValue, BErrorType targetType,
                                            List<TypeValuePair> unresolvedValues,
                                            boolean allowNumericConversion) {
            Type sourceTypeReferredType = getImpliedType(TypeChecker.getType(sourceValue));
            if (sourceValue == null || sourceTypeReferredType.getTag() != TypeTags.ERROR_TAG) {
                return false;
            }
            if (!TypeChecker.checkIsLikeType(null, ((ErrorValue) sourceValue).getDetails(), targetType.detailType,
                    unresolvedValues, allowNumericConversion, null)) {
                return false;
            }
            if (targetType.typeIdSet == null) {
                return true;
            }
            BTypeIdSet sourceIdSet = ((BErrorType) unwrap(sourceTypeReferredType)).typeIdSet;
            if (sourceIdSet == null) {
                return false;
            }
            return sourceIdSet.containsAll(targetType.typeIdSet);
        }

        private static boolean checkIsErrorType(Type sourceType, BErrorType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            if (sourceType.getTag() != TypeTags.ERROR_TAG) {
                return false;
            }
            // Handle recursive error types.
            TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceType, targetType);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);
            BErrorType bErrorType = (BErrorType) sourceType;

            if (!TypeChecker.checkIsType(bErrorType.detailType, targetType.detailType, unresolvedTypes)) {
                return false;
            }

            if (targetType.typeIdSet == null) {
                return true;
            }

            BTypeIdSet sourceTypeIdSet = bErrorType.typeIdSet;
            if (sourceTypeIdSet == null) {
                return false;
            }

            return sourceTypeIdSet.containsAll(targetType.typeIdSet);
        }
    }

    private static final class TypeDescTypeChecker {

        private static boolean checkTypeDescType(Type sourceType, BTypedescType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            if (sourceType.getTag() != TypeTags.TYPEDESC_TAG) {
                return false;
            }

            BTypedescType sourceTypedesc = (BTypedescType) sourceType;
            return TypeChecker.checkIsType(sourceTypedesc.getConstraint(), targetType.getConstraint(), unresolvedTypes);
        }
    }

    private static final class XmlTypeChecker {

        private static boolean checkIsXMLType(Type sourceType, Type targetType,
                                              List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            int sourceTag = sourceType.getTag();
            if (sourceTag == TypeTags.FINITE_TYPE_TAG) {
                return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
            }

            BXmlType target = ((BXmlType) targetType);
            if (sourceTag == TypeTags.XML_TAG) {
                Type targetConstraint = getRecursiveTargetConstraintType(target);
                Type constraint = typeConstraint(sourceType);
                if (constraint.getTag() == TypeTags.NEVER_TAG) {
                    if (targetConstraint.getTag() == TypeTags.UNION_TAG) {
                        return TypeChecker.checkIsUnionType(sourceType, targetConstraint, unresolvedTypes);
                    }
                    return targetConstraint.getTag() == TypeTags.XML_TEXT_TAG ||
                            targetConstraint.getTag() == TypeTags.NEVER_TAG;
                }
                return TypeChecker.checkIsType(constraint, targetConstraint, unresolvedTypes);
            }
            if (TypeTags.isXMLTypeTag(sourceTag)) {
                return TypeChecker.checkIsType(sourceType, target.constraint, unresolvedTypes);
            }
            return false;
        }

        private static Type getRecursiveTargetConstraintType(BXmlType target) {
            Type targetConstraint = getImpliedType(target.constraint);
            // TODO: Revisit and check why xml<xml<constraint>>> on chained iteration
            while (targetConstraint.getTag() == TypeTags.XML_TAG) {
                targetConstraint = getImpliedType(typeConstraint(targetConstraint));
            }
            return targetConstraint;
        }

        private static boolean checkIsLikeXmlValueSingleton(XmlValue xmlSource, Type targetType) {
            XmlNodeType targetXmlNodeType = getXmlNodeType(targetType);
            XmlNodeType xmlSourceNodeType = xmlSource.getNodeType();

            if (xmlSourceNodeType == targetXmlNodeType) {
                return true;
            }

            if (xmlSourceNodeType == XmlNodeType.SEQUENCE) {
                XmlSequence seq = (XmlSequence) xmlSource;
                return seq.size() == 1 && seq.getChildrenList().get(0).getNodeType() == targetXmlNodeType ||
                        (targetXmlNodeType == XmlNodeType.TEXT && seq.isEmpty());
            }

            return false;
        }

        private static XmlNodeType getXmlNodeType(Type type) {
            switch (getImpliedType(type).getTag()) {
                case TypeTags.XML_ELEMENT_TAG:
                    return XmlNodeType.ELEMENT;
                case TypeTags.XML_COMMENT_TAG:
                    return XmlNodeType.COMMENT;
                case TypeTags.XML_PI_TAG:
                    return XmlNodeType.PI;
                default:
                    return XmlNodeType.TEXT;
            }
        }

        private static boolean checkIsLikeXMLSequenceType(XmlValue xmlSource, Type targetType) {
            Set<XmlNodeType> acceptedNodeTypes = new HashSet<>();
            populateTargetXmlNodeTypes(acceptedNodeTypes, targetType);

            XmlNodeType xmlSourceNodeType = xmlSource.getNodeType();
            if (xmlSourceNodeType != XmlNodeType.SEQUENCE) {
                return acceptedNodeTypes.contains(xmlSourceNodeType);
            }

            XmlSequence seq = (XmlSequence) xmlSource;
            for (BXml m : seq.getChildrenList()) {
                if (!acceptedNodeTypes.contains(m.getNodeType())) {
                    return false;
                }
            }
            return true;
        }

        private static void populateTargetXmlNodeTypes(Set<XmlNodeType> nodeTypes, Type targetType) {
            // there are only 4 xml subtypes
            if (nodeTypes.size() == 4) {
                return;
            }

            Type referredType = getImpliedType(targetType);
            switch (referredType.getTag()) {
                case TypeTags.UNION_TAG:
                    for (Type memberType : ((UnionType) referredType).getMemberTypes()) {
                        populateTargetXmlNodeTypes(nodeTypes, memberType);
                    }
                    break;
                case TypeTags.INTERSECTION_TAG:
                    populateTargetXmlNodeTypes(nodeTypes, ((IntersectionType) referredType).getEffectiveType());
                    break;
                case TypeTags.XML_ELEMENT_TAG:
                    nodeTypes.add(XmlNodeType.ELEMENT);
                    break;
                case TypeTags.XML_COMMENT_TAG:
                    nodeTypes.add(XmlNodeType.COMMENT);
                    break;
                case TypeTags.XML_PI_TAG:
                    nodeTypes.add(XmlNodeType.PI);
                    break;
                case TypeTags.XML_TEXT_TAG:
                    nodeTypes.add(XmlNodeType.TEXT);
                    break;
                case TypeTags.XML_TAG:
                    populateTargetXmlNodeTypes(nodeTypes, ((BXmlType) referredType).constraint);
                    break;
                default:
                    break;

            }
        }
    }

    static final class TableTypeChecker {

        private static boolean checkIsTableType(Type sourceType, BTableType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            if (sourceType.getTag() != TypeTags.TABLE_TAG) {
                return false;
            }

            BTableType srcTableType = (BTableType) sourceType;

            if (!checkConstraints(srcTableType.getConstrainedType(), targetType.getConstrainedType(),
                    unresolvedTypes)) {
                return false;
            }

            if (targetType.getKeyType() == null && targetType.getFieldNames().length == 0) {
                return true;
            }

            if (targetType.getKeyType() != null) {
                if (srcTableType.getKeyType() != null &&
                        (checkConstraints(srcTableType.getKeyType(), targetType.getKeyType(), unresolvedTypes))) {
                    return true;
                }

                if (srcTableType.getFieldNames().length == 0) {
                    return false;
                }

                List<Type> fieldTypes = new ArrayList<>();
                Arrays.stream(srcTableType.getFieldNames()).forEach(field -> fieldTypes
                        .add(Objects.requireNonNull(getTableConstraintField(srcTableType.getConstrainedType(), field))
                                .getFieldType()));

                if (fieldTypes.size() == 1) {
                    return checkConstraints(fieldTypes.get(0), targetType.getKeyType(), unresolvedTypes);
                }

                BTupleType tupleType = new BTupleType(fieldTypes);
                return checkConstraints(tupleType, targetType.getKeyType(), unresolvedTypes);
            }

            return Arrays.equals(srcTableType.getFieldNames(), targetType.getFieldNames());
        }

        static BField getTableConstraintField(Type constraintType, String fieldName) {
            switch (constraintType.getTag()) {
                case TypeTags.RECORD_TYPE_TAG:
                    Map<String, Field> fieldList = TypeHelper.mappingRequiredFields(constraintType);
                    return (BField) fieldList.get(fieldName);
                case TypeTags.INTERSECTION_TAG:
                    Type effectiveType = TypeHelper.effectiveType(constraintType);
                    return getTableConstraintField(effectiveType, fieldName);
                case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                    Type referredType = TypeHelper.referredType(constraintType);
                    return getTableConstraintField(referredType, fieldName);
                case TypeTags.UNION_TAG:
                    List<Type> memTypes = TypeHelper.memberList(constraintType);
                    List<BField> fields = memTypes.stream().map(type -> getTableConstraintField(type, fieldName))
                            .filter(Objects::nonNull).collect(Collectors.toList());

                    if (fields.size() != memTypes.size()) {
                        return null;
                    }

                    if (fields.stream().allMatch(
                            field -> TypeChecker.isSameType(field.getFieldType(), fields.get(0).getFieldType()))) {
                        return fields.get(0);
                    }
                    return null;
                default:
                    return null;
            }
        }

        private static boolean checkConstraints(Type sourceConstraint, Type targetConstraint,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            if (sourceConstraint == null) {
                sourceConstraint = TYPE_ANY;
            }

            if (targetConstraint == null) {
                targetConstraint = TYPE_ANY;
            }

            return TypeChecker.checkIsType(sourceConstraint, targetConstraint, unresolvedTypes);
        }

        static boolean checkIsLikeTableType(Object sourceValue, BTableType targetType,
                                                    List<TypeValuePair> unresolvedValues,
                                                    boolean allowNumericConversion) {
            if (!(sourceValue instanceof TableValueImpl)) {
                return false;
            }
            TableValueImpl tableValue = (TableValueImpl) sourceValue;
            BTableType sourceType = (BTableType) getImpliedType(tableValue.getType());
            if (targetType.getKeyType() != null && sourceType.getFieldNames().length == 0) {
                return false;
            }

            if (sourceType.getKeyType() != null &&
                    !TypeChecker.checkIsType(tableValue.getKeyType(), targetType.getKeyType())) {
                return false;
            }

            TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
            if (unresolvedValues.contains(typeValuePair)) {
                return true;
            }

            Object[] objects = tableValue.values().toArray();
            for (Object object : objects) {
                if (!TypeChecker.checkIsLikeType(object, targetType.getConstrainedType(), allowNumericConversion)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class FunctionTypeChecker {

        private static boolean checkIsFunctionType(Type sourceType, BFunctionType targetType) {
            sourceType = getImpliedType(sourceType);
            if (sourceType.getTag() != TypeTags.FUNCTION_POINTER_TAG) {
                return false;
            }

            BFunctionType source = (BFunctionType) sourceType;
            if (ObjectTypeChecker.hasIncompatibleIsolatedFlags(targetType, source) ||
                    hasIncompatibleTransactionalFlags(targetType, source)) {
                return false;
            }

            if (SymbolFlags.isFlagOn(targetType.getFlags(), SymbolFlags.ANY_FUNCTION)) {
                return true;
            }

            if (source.parameters.length != targetType.parameters.length) {
                return false;
            }

            for (int i = 0; i < source.parameters.length; i++) {
                if (!TypeChecker.checkIsType(targetType.parameters[i].type, source.parameters[i].type,
                        new ArrayList<>())) {
                    return false;
                }
            }

            return TypeChecker.checkIsType(source.retType, targetType.retType, new ArrayList<>());
        }

        private static boolean hasIncompatibleTransactionalFlags(FunctionType target, FunctionType source) {
            return SymbolFlags.isFlagOn(source.getFlags(), SymbolFlags.TRANSACTIONAL) && !SymbolFlags
                    .isFlagOn(target.getFlags(), SymbolFlags.TRANSACTIONAL);
        }
    }

    private static final class ObjectTypeChecker {

        private static boolean checkObjectEquivalency(Type sourceType, BObjectType targetType,
                                                      List<TypeChecker.TypePair> unresolvedTypes) {
            return checkObjectEquivalency(null, sourceType, targetType, unresolvedTypes);
        }

        private static boolean checkObjectEquivalency(Object sourceVal, Type sourceType, BObjectType targetType,
                                                      List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            if (sourceType.getTag() != TypeTags.OBJECT_TYPE_TAG && sourceType.getTag() != TypeTags.SERVICE_TAG) {
                return false;
            }
            // If we encounter two types that we are still resolving, then skip it.
            // This is done to avoid recursive checking of the same type.
            TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceType, targetType);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);

            BObjectType sourceObjectType = (BObjectType) sourceType;

            if (SymbolFlags.isFlagOn(targetType.flags, SymbolFlags.ISOLATED) &&
                    !SymbolFlags.isFlagOn(sourceObjectType.flags, SymbolFlags.ISOLATED)) {
                return false;
            }

            Map<String, Field> targetFields = targetType.getFields();
            Map<String, Field> sourceFields = sourceObjectType.getFields();
            List<MethodType> targetFuncs = getAllFunctionsList(targetType);
            List<MethodType> sourceFuncs = getAllFunctionsList(sourceObjectType);

            if (targetType.getFields().values().stream().anyMatch(field -> SymbolFlags
                    .isFlagOn(field.getFlags(), SymbolFlags.PRIVATE))
                    || targetFuncs.stream().anyMatch(func -> SymbolFlags.isFlagOn(func.getFlags(),
                    SymbolFlags.PRIVATE))) {
                return false;
            }

            if (targetFields.size() > sourceFields.size() || targetFuncs.size() > sourceFuncs.size()) {
                return false;
            }

            String targetTypeModule = Optional.ofNullable(targetType.getPackage()).map(Module::toString).orElse("");
            String sourceTypeModule =
                    Optional.ofNullable(sourceObjectType.getPackage()).map(Module::toString).orElse("");

            if (sourceVal == null) {
                if (!checkObjectSubTypeForFields(targetFields, sourceFields, targetTypeModule, sourceTypeModule,
                        unresolvedTypes)) {
                    return false;
                }
            } else if (!checkObjectSubTypeForFieldsByValue(targetFields, sourceFields, targetTypeModule,
                    sourceTypeModule,
                    (BObject) sourceVal, unresolvedTypes)) {
                return false;
            }

            return checkObjectSubTypeForMethods(unresolvedTypes, targetFuncs, sourceFuncs, targetTypeModule,
                    sourceTypeModule, sourceObjectType, targetType);
        }

        private static List<MethodType> getAllFunctionsList(BObjectType objectType) {
            List<MethodType> functionList = new ArrayList<>(Arrays.asList(objectType.getMethods()));
            if (objectType.getTag() == TypeTags.SERVICE_TAG ||
                    (objectType.flags & SymbolFlags.CLIENT) == SymbolFlags.CLIENT) {
                Collections.addAll(functionList, ((BNetworkObjectType) objectType).getResourceMethods());
            }

            return functionList;
        }

        private static boolean checkObjectSubTypeForFields(Map<String, Field> targetFields,
                                                           Map<String, Field> sourceFields, String targetTypeModule,
                                                           String sourceTypeModule,
                                                           List<TypeChecker.TypePair> unresolvedTypes) {
            for (Field lhsField : targetFields.values()) {
                Field rhsField = sourceFields.get(lhsField.getFieldName());
                if (rhsField == null ||
                        !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsField.getFlags(),
                                rhsField.getFlags()) || MapTypeChecker.hasIncompatibleReadOnlyFlags(lhsField,
                        rhsField) ||
                        !TypeChecker.checkIsType(rhsField.getFieldType(), lhsField.getFieldType(), unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkObjectSubTypeForFieldsByValue(Map<String, Field> targetFields,
                                                                  Map<String, Field> sourceFields,
                                                                  String targetTypeModule,
                                                                  String sourceTypeModule, BObject sourceObjVal,
                                                                  List<TypeChecker.TypePair> unresolvedTypes) {
            for (Field lhsField : targetFields.values()) {
                String name = lhsField.getFieldName();
                Field rhsField = sourceFields.get(name);
                if (rhsField == null ||
                        !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsField.getFlags(),
                                rhsField.getFlags()) || MapTypeChecker.hasIncompatibleReadOnlyFlags(lhsField,
                        rhsField)) {
                    return false;
                }

                if (SymbolFlags.isFlagOn(rhsField.getFlags(), SymbolFlags.FINAL)) {
                    Object fieldValue = sourceObjVal.get(StringUtils.fromString(name));
                    Type fieldValueType = TypeChecker.getType(fieldValue);

                    if (fieldValueType.isReadOnly()) {
                        if (!TypeChecker.checkIsLikeType(fieldValue, lhsField.getFieldType())) {
                            return false;
                        }
                        continue;
                    }

                    if (!TypeChecker.checkIsType(fieldValueType, lhsField.getFieldType(), unresolvedTypes)) {
                        return false;
                    }
                } else if (!TypeChecker.checkIsType(rhsField.getFieldType(), lhsField.getFieldType(),
                        unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkObjectSubTypeForMethods(List<TypeChecker.TypePair> unresolvedTypes,
                                                            List<MethodType> targetFuncs,
                                                            List<MethodType> sourceFuncs,
                                                            String targetTypeModule, String sourceTypeModule,
                                                            BObjectType sourceType, BObjectType targetType) {
            for (MethodType lhsFunc : targetFuncs) {
                Optional<MethodType> rhsFunction = getMatchingInvokableType(sourceFuncs, lhsFunc, unresolvedTypes);
                if (rhsFunction.isEmpty()) {
                    return false;
                }

                MethodType rhsFunc = rhsFunction.get();
                if (rhsFunc == null ||
                        !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsFunc.getFlags(),
                                rhsFunc.getFlags())) {
                    return false;
                }
                if (SymbolFlags.isFlagOn(lhsFunc.getFlags(), SymbolFlags.REMOTE) != SymbolFlags
                        .isFlagOn(rhsFunc.getFlags(), SymbolFlags.REMOTE)) {
                    return false;
                }
            }

            // Target type is not a distinct type, no need to match type-ids
            BTypeIdSet targetTypeIdSet = targetType.typeIdSet;
            if (targetTypeIdSet == null) {
                return true;
            }

            BTypeIdSet sourceTypeIdSet = sourceType.typeIdSet;
            if (sourceTypeIdSet == null) {
                return false;
            }

            return sourceTypeIdSet.containsAll(targetTypeIdSet);
        }

        private static boolean isInSameVisibilityRegion(String lhsTypePkg, String rhsTypePkg, long lhsFlags,
                                                        long rhsFlags) {
            if (SymbolFlags.isFlagOn(lhsFlags, SymbolFlags.PRIVATE)) {
                return lhsTypePkg.equals(rhsTypePkg);
            } else if (SymbolFlags.isFlagOn(lhsFlags, SymbolFlags.PUBLIC)) {
                return SymbolFlags.isFlagOn(rhsFlags, SymbolFlags.PUBLIC);
            }
            return !SymbolFlags.isFlagOn(rhsFlags, SymbolFlags.PRIVATE) && !SymbolFlags
                    .isFlagOn(rhsFlags, SymbolFlags.PUBLIC) &&
                    lhsTypePkg.equals(rhsTypePkg);
        }

        private static Optional<MethodType> getMatchingInvokableType(List<MethodType> rhsFuncs,
                                                                     MethodType lhsFunc,
                                                                     List<TypeChecker.TypePair> unresolvedTypes) {
            Optional<MethodType> matchingFunction = rhsFuncs.stream()
                    .filter(rhsFunc -> lhsFunc.getName().equals(rhsFunc.getName()))
                    .filter(rhsFunc -> checkFunctionTypeEqualityForObjectType(rhsFunc.getType(), lhsFunc.getType(),
                            unresolvedTypes))
                    .findFirst();

            if (matchingFunction.isEmpty()) {
                return matchingFunction;
            }
            // For resource function match, we need to check whether lhs function resource path type belongs to
            // rhs function resource path type
            MethodType matchingFunc = matchingFunction.get();
            boolean lhsFuncIsResource = SymbolFlags.isFlagOn(lhsFunc.getFlags(), SymbolFlags.RESOURCE);
            boolean matchingFuncIsResource = SymbolFlags.isFlagOn(matchingFunc.getFlags(), SymbolFlags.RESOURCE);

            if (!lhsFuncIsResource && !matchingFuncIsResource) {
                return matchingFunction;
            }

            if ((lhsFuncIsResource && !matchingFuncIsResource) || (matchingFuncIsResource && !lhsFuncIsResource)) {
                return Optional.empty();
            }

            Type[] lhsFuncResourcePathTypes = ((BResourceMethodType) lhsFunc).pathSegmentTypes;
            Type[] rhsFuncResourcePathTypes = ((BResourceMethodType) matchingFunc).pathSegmentTypes;

            int lhsFuncResourcePathTypesSize = lhsFuncResourcePathTypes.length;
            if (lhsFuncResourcePathTypesSize != rhsFuncResourcePathTypes.length) {
                return Optional.empty();
            }

            for (int i = 0; i < lhsFuncResourcePathTypesSize; i++) {
                if (!TypeChecker.checkIsType(lhsFuncResourcePathTypes[i], rhsFuncResourcePathTypes[i])) {
                    return Optional.empty();
                }
            }

            return matchingFunction;
        }

        private static boolean checkFunctionTypeEqualityForObjectType(FunctionType source, FunctionType target,
                                                                      List<TypeChecker.TypePair> unresolvedTypes) {
            if (hasIncompatibleIsolatedFlags(target, source)) {
                return false;
            }

            if (source.getParameters().length != target.getParameters().length) {
                return false;
            }

            for (int i = 0; i < source.getParameters().length; i++) {
                if (!TypeChecker.checkIsType(target.getParameters()[i].type, source.getParameters()[i].type,
                        unresolvedTypes)) {
                    return false;
                }
            }

            if (source.getReturnType() == null && target.getReturnType() == null) {
                return true;
            } else if (source.getReturnType() == null || target.getReturnType() == null) {
                return false;
            }

            return TypeChecker.checkIsType(source.getReturnType(), target.getReturnType(), unresolvedTypes);
        }

        private static boolean hasIncompatibleIsolatedFlags(FunctionType target, FunctionType source) {
            return SymbolFlags.isFlagOn(target.getFlags(), SymbolFlags.ISOLATED) && !SymbolFlags
                    .isFlagOn(source.getFlags(), SymbolFlags.ISOLATED);
        }

        private static boolean checkIsServiceType(Type sourceType, Type targetType,
                                                  List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            if (sourceType.getTag() == TypeTags.SERVICE_TAG) {
                return checkObjectEquivalency(sourceType, (BObjectType) targetType, unresolvedTypes);
            }

            if (sourceType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
                var flags = ((BObjectType) sourceType).flags;
                return (flags & SymbolFlags.SERVICE) == SymbolFlags.SERVICE;
            }

            return false;
        }
    }

    static final class ListTypeChecker {

        static boolean checkIsLikeTupleType(Object sourceValue, BTupleType targetType,
                                            List<TypeValuePair> unresolvedValues,
                                            boolean allowNumericConversion) {
            if (!(sourceValue instanceof ArrayValue)) {
                return false;
            }

            ArrayValue source = (ArrayValue) sourceValue;
            List<Type> targetTypes = targetType.getTupleTypes();
            int sourceTypeSize = source.size();
            int targetTypeSize = targetTypes.size();
            Type targetRestType = targetType.getRestType();

            if (sourceTypeSize < targetTypeSize) {
                return false;
            }
            if (targetRestType == null && sourceTypeSize > targetTypeSize) {
                return false;
            }

            for (int i = 0; i < targetTypeSize; i++) {
                if (!TypeChecker.checkIsLikeType(null, source.getRefValue(i), targetTypes.get(i), unresolvedValues,
                        allowNumericConversion, null)) {
                    return false;
                }
            }
            for (int i = targetTypeSize; i < sourceTypeSize; i++) {
                if (!TypeChecker.checkIsLikeType(null, source.getRefValue(i), targetRestType, unresolvedValues,
                        allowNumericConversion, null)) {
                    return false;
                }
            }
            return true;
        }

        static boolean checkIsLikeArrayType(Object sourceValue, BArrayType targetType,
                                            List<TypeValuePair> unresolvedValues,
                                            boolean allowNumericConversion) {
            if (!(sourceValue instanceof ArrayValue)) {
                return false;
            }

            ArrayValue source = (ArrayValue) sourceValue;
            Type targetTypeElementType = targetType.getElementType();
            if (source.getType().getTag() == TypeTags.ARRAY_TAG) {
                Type sourceElementType = ((BArrayType) source.getType()).getElementType();
                if (isValueType(sourceElementType)) {

                    if (TypeChecker.checkIsType(sourceElementType, targetTypeElementType, new ArrayList<>())) {
                        return true;
                    }

                    if (allowNumericConversion && TypeChecker.isNumericType(sourceElementType)) {
                        if (TypeChecker.isNumericType(targetTypeElementType)) {
                            return true;
                        }

                        if (targetTypeElementType.getTag() != TypeTags.UNION_TAG) {
                            return false;
                        }

                        List<Type> targetNumericTypes = new ArrayList<>();
                        for (Type memType : ((BUnionType) targetTypeElementType).getMemberTypes()) {
                            if (TypeChecker.isNumericType(memType) && !targetNumericTypes.contains(memType)) {
                                targetNumericTypes.add(memType);
                            }
                        }
                        return targetNumericTypes.size() == 1;
                    }

                    if (targetTypeElementType.getTag() == TypeTags.FLOAT_TAG ||
                            targetTypeElementType.getTag() == TypeTags.DECIMAL_TAG) {
                        return false;
                    }
                }
            }

            int sourceSize = source.size();
            if ((targetType.getState() != ArrayType.ArrayState.OPEN) && (sourceSize != targetType.getSize())) {
                return false;
            }
            for (int i = 0; i < sourceSize; i++) {
                if (!TypeChecker.checkIsLikeType(null, source.get(i), targetTypeElementType, unresolvedValues,
                        allowNumericConversion, null)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkIsArrayType(BArrayType sourceType, BArrayType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            switch (sourceType.getState()) {
                case OPEN:
                    if (targetType.getState() != ArrayType.ArrayState.OPEN) {
                        return false;
                    }
                    break;
                case CLOSED:
                    if (targetType.getState() == ArrayType.ArrayState.CLOSED &&
                            sourceType.getSize() != targetType.getSize()) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
            return TypeChecker.checkIsType(sourceType.getElementType(), targetType.getElementType(), unresolvedTypes);
        }

        private static boolean checkIsArrayType(BTupleType sourceType, BArrayType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            List<Type> tupleTypes = sourceType.getTupleTypes();
            Type sourceRestType = sourceType.getRestType();
            Type targetElementType = targetType.getElementType();

            if (targetType.getState() == ArrayType.ArrayState.OPEN) {
                for (Type sourceElementType : tupleTypes) {
                    if (!TypeChecker.checkIsType(sourceElementType, targetElementType, unresolvedTypes)) {
                        return false;
                    }
                }
                if (sourceRestType != null) {
                    return TypeChecker.checkIsType(sourceRestType, targetElementType, unresolvedTypes);
                }
                return true;
            }
            if (sourceRestType != null) {
                return false;
            }
            if (tupleTypes.size() != targetType.getSize()) {
                return false;
            }
            for (Type sourceElementType : tupleTypes) {
                if (!TypeChecker.checkIsType(sourceElementType, targetElementType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkIsArrayType(Type sourceType, BArrayType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            int sourceTypeTag = sourceType.getTag();

            if (sourceTypeTag == TypeTags.UNION_TAG) {
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsArrayType(memberType, targetType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            }

            if (sourceTypeTag != TypeTags.ARRAY_TAG && sourceTypeTag != TypeTags.TUPLE_TAG) {
                return false;
            }

            if (sourceTypeTag == TypeTags.ARRAY_TAG) {
                return checkIsArrayType((BArrayType) sourceType, targetType, unresolvedTypes);
            }
            return checkIsArrayType((BTupleType) sourceType, targetType, unresolvedTypes);
        }

        private static boolean checkIsTupleType(BArrayType sourceType, BTupleType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            Type sourceElementType = sourceType.getElementType();
            List<Type> targetTypes = targetType.getTupleTypes();
            Type targetRestType = targetType.getRestType();

            switch (sourceType.getState()) {
                case OPEN:
                    if (targetRestType == null) {
                        return false;
                    }
                    if (targetTypes.isEmpty()) {
                        return TypeChecker.checkIsType(sourceElementType, targetRestType, unresolvedTypes);
                    }
                    return false;
                case CLOSED:
                    if (sourceType.getSize() < targetTypes.size()) {
                        return false;
                    }
                    if (targetTypes.isEmpty()) {
                        if (targetRestType != null) {
                            return TypeChecker.checkIsType(sourceElementType, targetRestType, unresolvedTypes);
                        }
                        return sourceType.getSize() == 0;
                    }

                    for (Type targetElementType : targetTypes) {
                        if (!(TypeChecker.checkIsType(sourceElementType, targetElementType, unresolvedTypes))) {
                            return false;
                        }
                    }
                    if (sourceType.getSize() == targetTypes.size()) {
                        return true;
                    }
                    if (targetRestType != null) {
                        return TypeChecker.checkIsType(sourceElementType, targetRestType, unresolvedTypes);
                    }
                    return false;
                default:
                    return false;
            }
        }

        private static boolean checkIsTupleType(BTupleType sourceType, BTupleType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            List<Type> sourceTypes = sourceType.getTupleTypes();
            Type sourceRestType = sourceType.getRestType();
            List<Type> targetTypes = targetType.getTupleTypes();
            Type targetRestType = targetType.getRestType();

            if (sourceRestType != null && targetRestType == null) {
                return false;
            }
            int sourceTypeSize = sourceTypes.size();
            int targetTypeSize = targetTypes.size();

            if (sourceRestType == null && targetRestType == null && sourceTypeSize != targetTypeSize) {
                return false;
            }

            if (sourceTypeSize < targetTypeSize) {
                return false;
            }

            for (int i = 0; i < targetTypeSize; i++) {
                if (!TypeChecker.checkIsType(sourceTypes.get(i), targetTypes.get(i), unresolvedTypes)) {
                    return false;
                }
            }
            if (sourceTypeSize == targetTypeSize) {
                if (sourceRestType != null) {
                    return TypeChecker.checkIsType(sourceRestType, targetRestType, unresolvedTypes);
                }
                return true;
            }

            for (int i = targetTypeSize; i < sourceTypeSize; i++) {
                if (!TypeChecker.checkIsType(sourceTypes.get(i), targetRestType, unresolvedTypes)) {
                    return false;
                }
            }
            if (sourceRestType != null) {
                return TypeChecker.checkIsType(sourceRestType, targetRestType, unresolvedTypes);
            }
            return true;
        }
    }

    static final class MapTypeChecker {

        private static boolean checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(Type type) {
            Set<String> visitedTypeSet = new HashSet<>();
            return checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(type, visitedTypeSet);
        }

        private static boolean checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(Type type,
                                                                                       Set<String> visitedTypeSet) {
            switch (type.getTag()) {
                case TypeTags.NEVER_TAG:
                    return true;
                case TypeTags.RECORD_TYPE_TAG:
                    visitedTypeSet.add(type.getName());
                    for (Field field : mappingRequiredFields(type).values()) {
                        // skip check for fields with self referencing type and not required fields.
                        if ((SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.REQUIRED) ||
                                !SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL)) &&
                                !visitedTypeSet.contains(field.getFieldType()) &&
                                checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(field.getFieldType(),
                                        visitedTypeSet)) {
                            return true;
                        }
                    }
                    return false;
                case TypeTags.TUPLE_TAG:
                    visitedTypeSet.add(type.getName());
                    for (Type mem : listMemberTypes(type)) {
                        if (!visitedTypeSet.add(mem.getName())) {
                            continue;
                        }
                        if (checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(mem, visitedTypeSet)) {
                            return true;
                        }
                    }
                    return false;
                case TypeTags.ARRAY_TAG:
                    visitedTypeSet.add(type.getName());
                    Type elemType = listRestType(type);
                    visitedTypeSet.add(elemType.getName());
                    return arrayState(type) != ArrayType.ArrayState.OPEN &&
                            checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(elemType, visitedTypeSet);
                case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                    return checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(
                            TypeHelper.referredType(type), visitedTypeSet);
                case TypeTags.INTERSECTION_TAG:
                    return checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(
                            TypeHelper.effectiveType(type), visitedTypeSet);
                default:
                    return false;
            }
        }

        private static boolean checkIsRecordType(MapValue sourceRecordValue, BRecordType sourceRecordType,
                                                 BRecordType targetType, List<TypeChecker.TypePair> unresolvedTypes) {
            TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceRecordType, targetType);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);

            // Unsealed records are not equivalent to sealed records, unless their rest field type is 'never'. But
            // vice-versa is allowed.
            if (targetType.sealed && !sourceRecordType.sealed && (sourceRecordType.restFieldType == null ||
                    getImpliedType(sourceRecordType.restFieldType).getTag() != TypeTags.NEVER_TAG)) {
                return false;
            }

            // If both are sealed check the rest field type
            if (!sourceRecordType.sealed && !targetType.sealed &&
                    !TypeChecker.checkIsType(sourceRecordType.restFieldType, targetType.restFieldType,
                            unresolvedTypes)) {
                return false;
            }

            Map<String, Field> sourceFields = sourceRecordType.getFields();
            Set<String> targetFieldNames = targetType.getFields().keySet();

            for (Map.Entry<String, Field> targetFieldEntry : targetType.getFields().entrySet()) {
                String fieldName = targetFieldEntry.getKey();
                Field targetField = targetFieldEntry.getValue();
                Field sourceField = sourceFields.get(fieldName);

                if (getImpliedType(targetField.getFieldType()).getTag() == TypeTags.NEVER_TAG &&
                        containsInvalidNeverField(sourceField, sourceRecordType)) {
                    return false;
                }

                if (sourceField == null) {
                    if (!SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)) {
                        return false;
                    }

                    if (!sourceRecordType.sealed &&
                            !TypeChecker.checkIsType(sourceRecordType.restFieldType, targetField.getFieldType(),
                                    unresolvedTypes)) {
                        return false;
                    }

                    continue;
                }

                if (hasIncompatibleReadOnlyFlags(targetField, sourceField)) {
                    return false;
                }

                boolean optionalTargetField = SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL);
                boolean optionalSourceField = SymbolFlags.isFlagOn(sourceField.getFlags(), SymbolFlags.OPTIONAL);

                if (SymbolFlags.isFlagOn(sourceField.getFlags(), SymbolFlags.READONLY)) {
                    BString fieldNameBString = StringUtils.fromString(fieldName);

                    if (optionalSourceField && !sourceRecordValue.containsKey(fieldNameBString)) {
                        if (!optionalTargetField) {
                            return false;
                        }
                        continue;
                    }

                    if (!TypeChecker.checkIsLikeType(sourceRecordValue.get(fieldNameBString),
                            targetField.getFieldType())) {
                        return false;
                    }
                } else {
                    if (!optionalTargetField && optionalSourceField) {
                        return false;
                    }

                    if (!TypeChecker.checkIsType(sourceField.getFieldType(), targetField.getFieldType(),
                            unresolvedTypes)) {
                        return false;
                    }
                }
            }

            if (targetType.sealed) {
                for (String sourceFieldName : sourceFields.keySet()) {
                    if (targetFieldNames.contains(sourceFieldName)) {
                        continue;
                    }

                    if (!checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(
                            sourceFields.get(sourceFieldName).getFieldType())) {
                        return false;
                    }
                }
                return true;
            }

            for (Map.Entry<String, Field> targetFieldEntry : sourceFields.entrySet()) {
                String fieldName = targetFieldEntry.getKey();
                Field field = targetFieldEntry.getValue();
                if (targetFieldNames.contains(fieldName)) {
                    continue;
                }

                if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY)) {
                    if (!TypeChecker.checkIsLikeType(sourceRecordValue.get(StringUtils.fromString(fieldName)),
                            targetType.restFieldType)) {
                        return false;
                    }
                } else if (!TypeChecker.checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkIsMapType(Type sourceType, BMapType targetType,
                                              List<TypeChecker.TypePair> unresolvedTypes) {
            Type targetConstrainedType = targetType.getConstrainedType();
            sourceType = getImpliedType(sourceType);
            switch (sourceType.getTag()) {
                case TypeTags.MAP_TAG:
                    return TableTypeChecker.checkConstraints(((BMapType) sourceType).getConstrainedType(),
                            targetConstrainedType,
                            unresolvedTypes);
                case TypeTags.RECORD_TYPE_TAG:
                    BRecordType recType = (BRecordType) sourceType;
                    BUnionType wideTypeUnion = new BUnionType(getWideTypeComponents(recType));
                    return TableTypeChecker.checkConstraints(wideTypeUnion, targetConstrainedType, unresolvedTypes);
                default:
                    return false;
            }
        }

        private static boolean checkIsMapType(Object sourceVal, Type sourceType, BMapType targetType,
                                              List<TypeChecker.TypePair> unresolvedTypes) {
            Type targetConstrainedType = targetType.getConstrainedType();
            sourceType = getImpliedType(sourceType);
            switch (sourceType.getTag()) {
                case TypeTags.MAP_TAG:
                    return TableTypeChecker.checkConstraints(((BMapType) sourceType).getConstrainedType(),
                            targetConstrainedType,
                            unresolvedTypes);
                case TypeTags.RECORD_TYPE_TAG:
                    return checkIsMapType((MapValue) sourceVal, (BRecordType) sourceType, unresolvedTypes,
                            targetConstrainedType);
                default:
                    return false;
            }
        }

        private static boolean checkIsMapType(MapValue sourceVal, BRecordType sourceType,
                                              List<TypeChecker.TypePair> unresolvedTypes,
                                              Type targetConstrainedType) {
            for (Field field : sourceType.getFields().values()) {
                if (!SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY)) {
                    if (!TypeChecker.checkIsType(field.getFieldType(), targetConstrainedType, unresolvedTypes)) {
                        return false;
                    }
                    continue;
                }

                BString name = StringUtils.fromString(field.getFieldName());

                if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL) && !sourceVal.containsKey(name)) {
                    continue;
                }

                if (!TypeChecker.checkIsLikeType(sourceVal.get(name), targetConstrainedType)) {
                    return false;
                }
            }

            if (sourceType.sealed) {
                return true;
            }

            return TypeChecker.checkIsType(sourceType.restFieldType, targetConstrainedType, unresolvedTypes);
        }

        private static List<Type> getWideTypeComponents(BRecordType recType) {
            List<Type> types = new ArrayList<>();
            for (Field f : recType.getFields().values()) {
                types.add(f.getFieldType());
            }
            if (!recType.sealed) {
                types.add(recType.restFieldType);
            }
            return types;
        }

        private static boolean checkIsRecordType(Type sourceType, BRecordType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            switch (sourceType.getTag()) {
                case TypeTags.RECORD_TYPE_TAG:
                    return checkIsRecordType((BRecordType) sourceType, targetType, unresolvedTypes);
                case TypeTags.MAP_TAG:
                    return checkIsRecordType((BMapType) sourceType, targetType, unresolvedTypes);
                default:
                    return false;
            }
        }

        private static boolean checkIsRecordType(BRecordType sourceRecordType, BRecordType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            // If we encounter two types that we are still resolving, then skip it.
            // This is done to avoid recursive checking of the same type.
            TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceRecordType, targetType);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);

            // Unsealed records are not equivalent to sealed records, unless their rest field type is 'never'. But
            // vice-versa is allowed.
            if (targetType.sealed && !sourceRecordType.sealed && (sourceRecordType.restFieldType == null ||
                    getImpliedType(sourceRecordType.restFieldType).getTag() != TypeTags.NEVER_TAG)) {
                return false;
            }

            // If both are sealed check the rest field type
            if (!sourceRecordType.sealed && !targetType.sealed &&
                    !TypeChecker.checkIsType(sourceRecordType.restFieldType, targetType.restFieldType,
                            unresolvedTypes)) {
                return false;
            }

            Map<String, Field> sourceFields = sourceRecordType.getFields();
            Set<String> targetFieldNames = targetType.getFields().keySet();

            for (Map.Entry<String, Field> targetFieldEntry : targetType.getFields().entrySet()) {
                Field targetField = targetFieldEntry.getValue();
                Field sourceField = sourceFields.get(targetFieldEntry.getKey());

                if (sourceField == null) {
                    if (!SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)) {
                        return false;
                    }

                    if (!sourceRecordType.sealed &&
                            !TypeChecker.checkIsType(sourceRecordType.restFieldType, targetField.getFieldType(),
                                    unresolvedTypes)) {
                        return false;
                    }

                    continue;
                }

                if (hasIncompatibleReadOnlyFlags(targetField, sourceField)) {
                    return false;
                }

                // If the target field is required, the source field should be required as well.
                if (!SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)
                        && SymbolFlags.isFlagOn(sourceField.getFlags(), SymbolFlags.OPTIONAL)) {
                    return false;
                }

                if (!TypeChecker.checkIsType(sourceField.getFieldType(), targetField.getFieldType(), unresolvedTypes)) {
                    return false;
                }
            }

            // If there are fields remaining in the source record, first check if it's a closed record. Closed records
            // should only have the fields specified by its type.
            if (targetType.sealed) {
                return targetFieldNames.containsAll(sourceFields.keySet());
            }

            // If it's an open record, check if they are compatible with the rest field of the target type.
            for (Map.Entry<String, Field> sourceFieldEntry : sourceFields.entrySet()) {
                if (targetFieldNames.contains(sourceFieldEntry.getKey())) {
                    continue;
                }

                if (!TypeChecker.checkIsType(sourceFieldEntry.getValue().getFieldType(), targetType.restFieldType,
                        unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkIsRecordType(BMapType sourceType, BRecordType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            // If we encounter two types that we are still resolving, then skip it.
            // This is done to avoid recursive checking of the same type.
            TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceType, targetType);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);

            if (targetType.sealed) {
                return false;
            }

            Type constraintType = sourceType.getConstrainedType();

            for (Field field : targetType.getFields().values()) {
                var flags = field.getFlags();
                if (!SymbolFlags.isFlagOn(flags, SymbolFlags.OPTIONAL)) {
                    return false;
                }

                if (SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY) && !sourceType.isReadOnly()) {
                    return false;
                }

                if (!TypeChecker.checkIsType(constraintType, field.getFieldType(), unresolvedTypes)) {
                    return false;
                }
            }

            return TypeChecker.checkIsType(constraintType, targetType.restFieldType, unresolvedTypes);
        }

        private static boolean checkRecordBelongsToAnydataType(MapValue sourceVal, BRecordType recordType,
                                                               List<TypeChecker.TypePair> unresolvedTypes) {
            Type targetType = TYPE_ANYDATA;
            TypeChecker.TypePair pair = new TypeChecker.TypePair(recordType, targetType);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);

            Map<String, Field> fields = recordType.getFields();

            for (Map.Entry<String, Field> fieldEntry : fields.entrySet()) {
                String fieldName = fieldEntry.getKey();
                Field field = fieldEntry.getValue();

                if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY)) {
                    BString fieldNameBString = StringUtils.fromString(fieldName);

                    if (SymbolFlags
                            .isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL) &&
                            !sourceVal.containsKey(fieldNameBString)) {
                        continue;
                    }

                    if (!TypeChecker.checkIsLikeType(sourceVal.get(fieldNameBString), targetType)) {
                        return false;
                    }
                } else {
                    if (!TypeChecker.checkIsType(field.getFieldType(), targetType, unresolvedTypes)) {
                        return false;
                    }
                }
            }

            if (recordType.sealed) {
                return true;
            }

            return TypeChecker.checkIsType(recordType.restFieldType, targetType, unresolvedTypes);
        }

        private static boolean checkIsRecordType(Object sourceVal, Type sourceType, BRecordType targetType,
                                                 List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            switch (sourceType.getTag()) {
                case TypeTags.RECORD_TYPE_TAG:
                    return checkIsRecordType((MapValue) sourceVal, (BRecordType) sourceType, targetType,
                            unresolvedTypes);
                case TypeTags.MAP_TAG:
                    return checkIsRecordType((BMapType) sourceType, targetType, unresolvedTypes);
                default:
                    return false;
            }
        }

        private static boolean containsInvalidNeverField(Field sourceField, BRecordType sourceRecordType) {
            if (sourceField != null) {
                return !containsNeverType(sourceField.getFieldType());
            }
            if (sourceRecordType.isSealed()) {
                return true;
            }
            return !containsNeverType(sourceRecordType.getRestFieldType());
        }

        private static boolean containsNeverType(Type fieldType) {
            fieldType = getImpliedType(fieldType);
            int fieldTag = fieldType.getTag();
            if (fieldTag == TypeTags.NEVER_TAG) {
                return true;
            }
            if (fieldTag == TypeTags.UNION_TAG) {
                List<Type> memberTypes = ((BUnionType) unwrap(fieldType)).getOriginalMemberTypes();
                for (Type member : memberTypes) {
                    if (getImpliedType(member).getTag() == TypeTags.NEVER_TAG) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean hasIncompatibleReadOnlyFlags(Field targetField, Field sourceField) {
            return SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.READONLY) && !SymbolFlags
                    .isFlagOn(sourceField.getFlags(),
                            SymbolFlags.READONLY);
        }

        static boolean checkIsLikeJSONType(Object sourceValue, Type sourceType, BJsonType targetType,
                                                   List<TypeValuePair> unresolvedValues,
                                                   boolean allowNumericConversion) {
            Type referredSourceType = getImpliedType(sourceType);
            switch (referredSourceType.getTag()) {
                case TypeTags.ARRAY_TAG:
                    ArrayValue source = (ArrayValue) sourceValue;
                    Type elementType = listRestType(referredSourceType);
                    if (TypeChecker.checkIsType(elementType, targetType, new ArrayList<>())) {
                        return true;
                    }

                    Object[] arrayValues = source.getValues();
                    for (int i = 0; i < source.size(); i++) {
                        if (!checkIsLikeType(null, arrayValues[i], targetType, unresolvedValues,
                                allowNumericConversion, null)) {
                            return false;
                        }
                    }
                    return true;
                case TypeTags.TUPLE_TAG:
                    for (Object obj : ((TupleValueImpl) sourceValue).getValues()) {
                        if (!checkIsLikeType(null, obj, targetType, unresolvedValues, allowNumericConversion,
                                null)) {
                            return false;
                        }
                    }
                    return true;
                case TypeTags.MAP_TAG:
                    return checkIsMappingLikeJsonType((MapValueImpl) sourceValue, targetType, unresolvedValues,
                            allowNumericConversion);
                case TypeTags.RECORD_TYPE_TAG:
                    TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
                    if (unresolvedValues.contains(typeValuePair)) {
                        return true;
                    }
                    unresolvedValues.add(typeValuePair);
                    return checkIsMappingLikeJsonType((MapValueImpl) sourceValue, targetType, unresolvedValues,
                            allowNumericConversion);
                default:
                    return false;
            }
        }

        private static boolean checkIsMappingLikeJsonType(MapValueImpl sourceValue, BJsonType targetType,
                                                          List<TypeValuePair> unresolvedValues,
                                                          boolean allowNumericConversion) {
            for (Object value : sourceValue.values()) {
                if (!checkIsLikeType(null, value, targetType, unresolvedValues, allowNumericConversion,
                        null)) {
                    return false;
                }
            }
            return true;
        }

        static boolean checkIsLikeRecordType(Object sourceValue, BRecordType targetType,
                                                     List<TypeValuePair> unresolvedValues,
                                                     boolean allowNumericConversion,
                                                     String varName, List<String> errors) {
            if (!(sourceValue instanceof MapValueImpl)) {
                return false;
            }

            TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
            if (unresolvedValues.contains(typeValuePair)) {
                return true;
            }
            unresolvedValues.add(typeValuePair);

            Map<String, Type> targetFieldTypes = new HashMap<>();
            Type restFieldType = targetType.restFieldType;
            boolean returnVal = true;

            for (Field field : targetType.getFields().values()) {
                targetFieldTypes.put(field.getFieldName(), field.getFieldType());
            }

            for (Map.Entry targetTypeEntry : targetFieldTypes.entrySet()) {
                String fieldName = targetTypeEntry.getKey().toString();
                String fieldNameLong = TypeConverter.getLongFieldName(varName, fieldName);
                Field targetField = targetType.getFields().get(fieldName);

                if (!(((MapValueImpl) sourceValue).containsKey(StringUtils.fromString(fieldName))) &&
                        !SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)) {
                    TypeChecker.addErrorMessage((errors == null) ? 0 : errors.size(),
                            "missing required field '" + fieldNameLong +
                                    "' of type '" + targetField.getFieldType().toString() + "' in record '" +
                                    targetType +
                                    "'",
                            errors);
                    if ((errors == null) || (errors.size() >= MAX_TYPECAST_ERROR_COUNT + 1)) {
                        return false;
                    }
                    returnVal = false;
                }
            }

            for (Object object : ((MapValueImpl) sourceValue).entrySet()) {
                Map.Entry valueEntry = (Map.Entry) object;
                String fieldName = valueEntry.getKey().toString();
                String fieldNameLong = TypeConverter.getLongFieldName(varName, fieldName);
                int initialErrorCount = (errors == null) ? 0 : errors.size();

                if (targetFieldTypes.containsKey(fieldName)) {
                    if (!TypeChecker.checkIsLikeType(errors, (valueEntry.getValue()), targetFieldTypes.get(fieldName),
                            unresolvedValues, allowNumericConversion, fieldNameLong)) {
                        TypeChecker.addErrorMessage(initialErrorCount,
                                "field '" + fieldNameLong + "' in record '" + targetType +
                                        "' should be of type '" + targetFieldTypes.get(fieldName) + "', found '" +
                                        TypeConverter.getShortSourceValue(valueEntry.getValue()) + "'", errors);
                        returnVal = false;
                    }
                } else {
                    if (!targetType.sealed) {
                        if (!TypeChecker.checkIsLikeType(errors, (valueEntry.getValue()), restFieldType,
                                unresolvedValues,
                                allowNumericConversion, fieldNameLong)) {
                            TypeChecker.addErrorMessage(initialErrorCount, "value of field '" + valueEntry.getKey() +
                                            "' adding to the record '" + targetType + "' should be of type '" + restFieldType +
                                            "', found '" + TypeConverter.getShortSourceValue(valueEntry.getValue()) + "'",
                                    errors);
                            returnVal = false;
                        }
                    } else {
                        TypeChecker.addErrorMessage(initialErrorCount, "field '" + fieldNameLong +
                                "' cannot be added to the closed record '" + targetType + "'", errors);
                        returnVal = false;
                    }
                }
                if ((!returnVal) && ((errors == null) || (errors.size() >= MAX_TYPECAST_ERROR_COUNT + 1))) {
                    return false;
                }
            }
            return returnVal;
        }

        private static boolean checkIsTupleType(Type sourceType, BTupleType targetType,
                                                List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            int sourceTypeTag = sourceType.getTag();

            if (sourceTypeTag == TypeTags.UNION_TAG) {
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsTupleType(memberType, targetType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            }

            if (sourceTypeTag != TypeTags.ARRAY_TAG && sourceTypeTag != TypeTags.TUPLE_TAG) {
                return false;
            }

            if (sourceTypeTag == TypeTags.ARRAY_TAG) {
                return ListTypeChecker.checkIsTupleType((BArrayType) sourceType, targetType, unresolvedTypes);
            }
            return ListTypeChecker.checkIsTupleType((BTupleType) sourceType, targetType, unresolvedTypes);
        }

        static boolean checkIsLikeMapType(Object sourceValue, BMapType targetType,
                                          List<TypeValuePair> unresolvedValues,
                                          boolean allowNumericConversion) {
            if (!(sourceValue instanceof MapValueImpl)) {
                return false;
            }

            for (Object mapEntry : ((MapValueImpl) sourceValue).values()) {
                if (!TypeChecker.checkIsLikeType(null, mapEntry, targetType.getConstrainedType(), unresolvedValues,
                        allowNumericConversion, null)) {
                    return false;
                }
            }
            return true;
        }

        static boolean checkIsJSONType(Type sourceType, List<TypeChecker.TypePair> unresolvedTypes) {
            sourceType = getImpliedType(sourceType);
            // If we encounter two types that we are still resolving, then skip it.
            // This is done to avoid recursive checking of the same type.
            TypeChecker.TypePair pair = new TypeChecker.TypePair(sourceType, TYPE_JSON);
            if (unresolvedTypes.contains(pair)) {
                return true;
            }
            unresolvedTypes.add(pair);

            switch (sourceType.getTag()) {
                case TypeTags.STRING_TAG:
                case TypeTags.CHAR_STRING_TAG:
                case TypeTags.INT_TAG:
                case TypeTags.SIGNED32_INT_TAG:
                case TypeTags.SIGNED16_INT_TAG:
                case TypeTags.SIGNED8_INT_TAG:
                case TypeTags.UNSIGNED32_INT_TAG:
                case TypeTags.UNSIGNED16_INT_TAG:
                case TypeTags.UNSIGNED8_INT_TAG:
                case TypeTags.BYTE_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.NULL_TAG:
                case TypeTags.JSON_TAG:
                    return true;
                case TypeTags.ARRAY_TAG:
                    // Element type of the array should be 'is type' JSON
                    return TypeChecker.checkIsType(listRestType(sourceType), TYPE_JSON, unresolvedTypes);
                case TypeTags.FINITE_TYPE_TAG:
                    return TypeChecker.isFiniteTypeMatch(sourceType, TYPE_JSON);
                case TypeTags.MAP_TAG:
                    return TypeChecker.checkIsType(typeConstraint(sourceType), TYPE_JSON, unresolvedTypes);
                case TypeTags.RECORD_TYPE_TAG:
                    for (Field field : mappingRequiredFields(sourceType).values()) {
                        if (!TypeChecker.checkIsJSONType(field.getFieldType(), unresolvedTypes)) {
                            return false;
                        }
                    }

                    if (!mappingTypeSealed(sourceType)) {
                        return TypeChecker.checkIsJSONType(mappingRestFieldType(sourceType), unresolvedTypes);
                    }
                    return true;
                case TypeTags.TUPLE_TAG:
                    for (Type memberType : listMemberTypes(sourceType)) {
                        if (!TypeChecker.checkIsJSONType(memberType, unresolvedTypes)) {
                            return false;
                        }
                    }
                    Type tupleRestType = listRestType(sourceType);
                    if (tupleRestType != null) {
                        return checkIsJSONType(tupleRestType, unresolvedTypes);
                    }
                    return true;
                case TypeTags.UNION_TAG:
                    for (Type member : TypeHelper.members(sourceType)) {
                        if (!TypeChecker.checkIsJSONType(member, unresolvedTypes)) {
                            return false;
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }
    }
}
