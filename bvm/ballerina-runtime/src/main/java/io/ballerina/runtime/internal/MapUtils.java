/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.MapValue;

import java.util.List;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static io.ballerina.runtime.internal.TypeChecker.checkIsType;
import static io.ballerina.runtime.internal.errors.ErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static io.ballerina.runtime.internal.errors.ErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;

/**
 * Common utility methods used for MapValue insertion/manipulation.
 *
 * @since 0.995.0
 */
public class MapUtils {

    public static void handleMapStore(MapValue<BString, Object> mapValue, BString fieldName, Object value) {
        updateMapValue(TypeUtils.getImpliedType(mapValue.getType()), mapValue, fieldName, value);
    }

    // We are monomorphizing handleMapStore to avoid boxing when storing basic type values in records
    public static void handleMapStore(MapValue<BString, Object> mapValue, BString fieldName, long value) {
        Type fieldType = handleInherentTypeViolatingRecordUpdateStart(mapValue, fieldName,
                (BRecordType) TypeUtils.getImpliedType(mapValue.getType()), TYPE_INT);
        if (fieldType != null) {
            handleInherentTypeViolatingRecordUpdateFinish(fieldType, fieldName, value);
        }
        mapValue.putInt(fieldName, value);
    }

    public static void handleMapStore(MapValue<BString, Object> mapValue, BString fieldName, boolean value) {
        Type fieldType = handleInherentTypeViolatingRecordUpdateStart(mapValue, fieldName,
                (BRecordType) TypeUtils.getImpliedType(mapValue.getType()), TYPE_BOOLEAN);
        if (fieldType != null) {
            handleInherentTypeViolatingRecordUpdateFinish(fieldType, fieldName, value);
        }
        mapValue.putBoolean(fieldName, value);
    }

    public static void handleMapStore(MapValue<BString, Object> mapValue, BString fieldName, double value) {
        Type fieldType = handleInherentTypeViolatingRecordUpdateStart(mapValue, fieldName,
                (BRecordType) TypeUtils.getImpliedType(mapValue.getType()), TYPE_FLOAT);
        if (fieldType != null) {
            handleInherentTypeViolatingRecordUpdateFinish(fieldType, fieldName, value);
        }
        mapValue.putFloat(fieldName, value);
    }

    public static void handleInherentTypeViolatingMapUpdate(Object value, BMapType mapType) {
        if (checkIsType(value, mapType.getConstrainedType())) {
            return;
        }

        Type expType = mapType.getConstrainedType();
        Type valuesType = TypeChecker.getType(value);

        throw ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB,
                                                               INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                                       ErrorHelper.getErrorDetails(ErrorCodes.INVALID_MAP_INSERTION,
                                                                               expType, valuesType));
    }

    // This handles most common cases for basic types, but can't do type checking for basic type subtypes without boxing
    // the value. In such a case this will return the type of the field that needs to be checked against otherwise null
    private static Type handleInherentTypeViolatingRecordUpdateStart(MapValue mapValue, BString fieldName,
                                                                     BRecordType recType, Type basicType) {
        Field recField = recType.getFields().get(fieldName.getValue());
        Type fieldType;
        if (recField != null) {
            if (SymbolFlags.isFlagOn(recField.getFlags(), SymbolFlags.READONLY)) {
                throw ErrorCreator.createError(
                        getModulePrefixedReason(MAP_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        ErrorHelper.getErrorDetails(ErrorCodes.RECORD_INVALID_READONLY_FIELD_UPDATE,
                                fieldName, mapValue.getType()));
            }
            fieldType = recField.getFieldType();
        } else if (recType.restFieldType == null) {
            throw ErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR, ErrorHelper.getErrorDetails(
                    ErrorCodes.INVALID_RECORD_FIELD_ACCESS, fieldName, mapValue.getType()));
        } else {
            fieldType = recType.restFieldType;
        }

        if (basicType == fieldType || (basicType.getTag() == fieldType.getTag() && basicType.equals(fieldType))) {
            return null;
        }
        return fieldType;
    }

    // This finishes the type checking started by handleInherentTypeViolatingRecordUpdateStart.
    // Note this will box the value
    private static void handleInherentTypeViolatingRecordUpdateFinish(Type inherentFieldType, BString fieldName,
                                                                      Object value) {
        if (checkIsType(value, inherentFieldType)) {
            return;
        }
        Type valuesType = TypeChecker.getType(value);
        throw ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB,
                        INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                ErrorHelper.getErrorDetails(
                        ErrorCodes.INVALID_RECORD_FIELD_ADDITION, fieldName, inherentFieldType, valuesType));
    }

    public static boolean handleInherentTypeViolatingRecordUpdate(MapValue mapValue, BString fieldName, Object value,
                                                                  BRecordType recType, boolean initialValue) {
        Field recField = recType.getFields().get(fieldName.getValue());
        Type recFieldType;

        if (recField != null) {
            // If there is a corresponding field in the record, check if it can be updated.
            // i.e., it is not a `readonly` field or this is an insertion on creation.
            // `initialValue` is only true if this is an update for a field provided in the mapping constructor
            // expression.
            if (!initialValue && SymbolFlags.isFlagOn(recField.getFlags(), SymbolFlags.READONLY)) {

                throw ErrorCreator.createError(
                        getModulePrefixedReason(MAP_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        ErrorHelper.getErrorDetails(ErrorCodes.RECORD_INVALID_READONLY_FIELD_UPDATE,
                                fieldName, mapValue.getType()));
            }

            // If it can be updated, use it.
            recFieldType = recField.getFieldType();
            if (value == null && SymbolFlags.isFlagOn(recField.getFlags(), SymbolFlags.OPTIONAL)
                    && !containsNilType(recFieldType)) {
                return false;
            }
        } else if (recType.restFieldType != null) {
            // If there isn't a corresponding field, but there is a rest field, use it
            recFieldType = recType.restFieldType;
        } else {
            // If both of the above conditions fail, the implication is that this is an attempt to insert a
            // value to a non-existent field in a closed record.
            throw ErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR, ErrorHelper.getErrorDetails(
                    ErrorCodes.INVALID_RECORD_FIELD_ACCESS, fieldName, mapValue.getType()));
        }

        if (checkIsType(value, recFieldType)) {
            return true;
        }
        Type valuesType = TypeChecker.getType(value);

        throw ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB,
                        INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                ErrorHelper.getErrorDetails(
                        ErrorCodes.INVALID_RECORD_FIELD_ADDITION, fieldName, recFieldType,
                        valuesType));
    }

    private static boolean containsNilType(Type type) {
        type = TypeUtils.getImpliedType(type);
        int tag = type.getTag();
        if (tag == TypeTags.UNION_TAG) {
            List<Type> memTypes = ((BUnionType) type).getMemberTypes();
            for (Type memType : memTypes) {
                if (containsNilType(memType)) {
                    return true;
                }
            }
        }
        return tag == TypeTags.NULL_TAG;
    }

    public static BError createOpNotSupportedError(Type type, String op) {
        return ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB, OPERATION_NOT_SUPPORTED_IDENTIFIER),
                ErrorHelper.getErrorDetails(ErrorCodes.OPERATION_NOT_SUPPORTED_ERROR, op, type));
    }

    public static void checkIsMapOnlyOperation(Type mapType, String op) {
        switch (TypeUtils.getImpliedType(mapType).getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return;
            default:
                throw createOpNotSupportedError(mapType, op);
        }
    }

    private static void updateMapValue(Type mapType, MapValue<BString, Object> mapValue, BString fieldName,
                                       Object value) {

        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                handleInherentTypeViolatingMapUpdate(value, (BMapType) mapType);
                mapValue.put(fieldName, value);
                return;
            case TypeTags.RECORD_TYPE_TAG:
                if (handleInherentTypeViolatingRecordUpdate(mapValue, fieldName, value, (BRecordType) mapType, false)) {
                    mapValue.put(fieldName, value);
                    return;
                }
                mapValue.remove(fieldName);
                return;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                updateMapValue(((BTypeReferenceType) mapType).getReferredType(), mapValue, fieldName, value);
        }
    }

}
