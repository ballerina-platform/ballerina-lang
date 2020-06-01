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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

import java.util.Map;

import static org.ballerinalang.jvm.util.BLangConstants.MAP_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Common utility methods used for MapValue insertion/manipulation.
 *
 * @since 0.995.0
 */
public class MapUtils {

    public static void handleMapStore(MapValue<BString, Object> mapValue, BString fieldName, Object value) {
        BType mapType = mapValue.getType();
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                handleInherentTypeViolatingMapUpdate(value, (BMapType) mapType);
                mapValue.put(fieldName, value);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                handleInherentTypeViolatingRecordUpdate(mapValue, fieldName, value, (BRecordType) mapType,
                                                        false);
                mapValue.put(fieldName, value);
                break;
        }
    }

    public static void handleInherentTypeViolatingMapUpdate(Object value, BMapType mapType) {
        if (TypeChecker.checkIsType(value, mapType.getConstrainedType())) {
            return;
        }

        BType expType = mapType.getConstrainedType();
        BType valuesType = TypeChecker.getType(value);

        throw BallerinaErrors.createError(getModulePrefixedReason(MAP_LANG_LIB,
                                                                  INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                                          BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_MAP_INSERTION,
                                                                               expType, valuesType));
    }

    public static void handleInherentTypeViolatingRecordUpdate(MapValue mapValue, BString fieldName, Object value,
                                                               BRecordType recType, boolean initialValue) {
        BField recField = recType.getFields().get(fieldName.getValue());
        BType recFieldType;

        if (recField != null) {
            // If there is a corresponding field in the record, and an entry in the value, check if it can be
            // updated.
            // i.e., it is not a `readonly` field or this is the first insertion of the field into the record.
            // `initialValue` is only true if this is an update for a field provided in the mapping constructor
            // expression.
            if (!initialValue && mapValue.containsKey(fieldName) && Flags.isFlagOn(recField.flags, Flags.READONLY)) {

                throw BallerinaErrors.createError(
                        getModulePrefixedReason(MAP_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                        BLangExceptionHelper.getErrorMessage(RuntimeErrors.RECORD_INVALID_READONLY_FIELD_UPDATE,
                                                             fieldName, recType));
            }

            // If it can be updated, use it.
            recFieldType = recField.type;
        } else if (recType.restFieldType != null) {
            // If there isn't a corresponding field, but there is a rest field, use it
            recFieldType = recType.restFieldType;
        } else {
            // If both of the above conditions fail, the implication is that this is an attempt to insert a
            // value to a non-existent field in a closed record.
            throw BallerinaErrors.createError(MAP_KEY_NOT_FOUND_ERROR,
                                              BLangExceptionHelper.getErrorMessage(
                                                      RuntimeErrors.INVALID_RECORD_FIELD_ACCESS, fieldName, recType));
        }

        if (TypeChecker.checkIsType(value, recFieldType)) {
            return;
        }
        BType valuesType = TypeChecker.getType(value);

        throw BallerinaErrors.createError(getModulePrefixedReason(MAP_LANG_LIB,
                                                                  INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                                          BLangExceptionHelper.getErrorMessage(
                                                  RuntimeErrors.INVALID_RECORD_FIELD_ADDITION, fieldName, recFieldType,
                                                  valuesType));
    }

    public static ErrorValue createOpNotSupportedError(BType type, String op) {
        return BallerinaErrors.createError(getModulePrefixedReason(MAP_LANG_LIB,
                                                                   OPERATION_NOT_SUPPORTED_IDENTIFIER),
                                           String.format("%s not supported on type '%s'", op, type.getQualifiedName()));
    }

    public static void checkIsMapOnlyOperation(BType mapType, String op) {
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return;
            default:
                throw createOpNotSupportedError(mapType, op);
        }
    }

    public static void validateRequiredFieldForRecord(MapValue<?, ?> m, String k) {
        BType type = m.getType();
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG && isRequiredField((BRecordType) type, k)) {
            throw createOpNotSupportedErrorForRecord(type, k);
        }
    }

    public static void validateRecord(MapValue<?, ?> m) {
        BType type = m.getType();
        if (type.getTag() != TypeTags.RECORD_TYPE_TAG) {
            return;
        }
        Map<String, BField> fields = ((BRecordType) type).getFields();
        for (String key : fields.keySet()) {
            if (isRequiredField((BRecordType) type, key)) {
                throw createOpNotSupportedErrorForRecord(type, key);
            }
        }
    }

    private static boolean isRequiredField(BRecordType type, String k) {
        Map<String, BField> fields = type.getFields();
        BField field = fields.get(k);

        return (field != null && Flags.isFlagOn(field.flags, Flags.REQUIRED));
    }

    private static ErrorValue createOpNotSupportedErrorForRecord(BType type, String field) {
        return BallerinaErrors.createError(getModulePrefixedReason(
                MAP_LANG_LIB, OPERATION_NOT_SUPPORTED_IDENTIFIER), String.format(
                "failed to remove field: '%s' is a required field in '%s'", field, type.getQualifiedName()));
    }
}
