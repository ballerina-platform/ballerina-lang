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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import io.ballerina.runtime.internal.values.MapValue;

import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Common utility methods used for MapValue insertion/manipulation.
 *
 * @since 0.995.0
 */
public class MapUtils {

    public static void handleMapStore(MapValue<BString, Object> mapValue, BString fieldName, Object value) {
        Type mapType = mapValue.getType();
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                handleInherentTypeViolatingMapUpdate(value, (BMapType) mapType);
                mapValue.put(fieldName, value);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                handleInherentTypeViolatingRecordUpdate(mapValue, fieldName, value, (BRecordType) mapType, false);
                mapValue.put(fieldName, value);
                break;
        }
    }

    public static void handleInherentTypeViolatingMapUpdate(Object value, BMapType mapType) {
        if (TypeChecker.checkIsType(value, mapType.getConstrainedType())) {
            return;
        }

        Type expType = mapType.getConstrainedType();
        Type valuesType = TypeChecker.getType(value);

        throw ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB,
                                                               INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                                       BLangExceptionHelper.getErrorDetails(RuntimeErrors.INVALID_MAP_INSERTION,
                                                                               expType, valuesType));
    }

    public static void handleInherentTypeViolatingRecordUpdate(MapValue mapValue, BString fieldName, Object value,
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
                        BLangExceptionHelper.getErrorDetails(RuntimeErrors.RECORD_INVALID_READONLY_FIELD_UPDATE,
                                                             fieldName, recType));
            }

            // If it can be updated, use it.
            recFieldType = recField.getFieldType();
        } else if (recType.restFieldType != null) {
            // If there isn't a corresponding field, but there is a rest field, use it
            recFieldType = recType.restFieldType;
        } else {
            // If both of the above conditions fail, the implication is that this is an attempt to insert a
            // value to a non-existent field in a closed record.
            throw ErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR,
                                           BLangExceptionHelper.getErrorDetails(
                                                      RuntimeErrors.INVALID_RECORD_FIELD_ACCESS, fieldName, recType));
        }

        if (TypeChecker.checkIsType(value, recFieldType)) {
            return;
        }
        Type valuesType = TypeChecker.getType(value);

        throw ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB,
                                                               INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                                       BLangExceptionHelper.getErrorDetails(
                                                  RuntimeErrors.INVALID_RECORD_FIELD_ADDITION, fieldName, recFieldType,
                                                  valuesType));
    }

    public static BError createOpNotSupportedError(Type type, String op) {
        return ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB, OPERATION_NOT_SUPPORTED_IDENTIFIER),
                BLangExceptionHelper.getErrorDetails(RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR, op, type));
    }

    public static void checkIsMapOnlyOperation(Type mapType, String op) {
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return;
            default:
                throw createOpNotSupportedError(mapType, op);
        }
    }
}
