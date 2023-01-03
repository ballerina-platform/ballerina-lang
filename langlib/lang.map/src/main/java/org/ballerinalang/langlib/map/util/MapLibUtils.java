/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.map.util;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static io.ballerina.runtime.internal.MapUtils.createOpNotSupportedError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Utility methods for map lib functions.
 *
 * @since 1.0
 */
public class MapLibUtils {

    public static Type getFieldType(Type mapType, String funcName) {
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                return ((MapType) mapType).getConstrainedType();
            case TypeTags.RECORD_TYPE_TAG:
                return getCommonTypeForRecordField((RecordType) mapType);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getFieldType(((ReferenceType) mapType).getReferredType(), funcName);
            default:
                throw createOpNotSupportedError(mapType, funcName);
        }
    }

    public static Type getCommonTypeForRecordField(RecordType  recordType) {
        LinkedHashSet<Type> typeSet = new LinkedHashSet<>();
        Collection<Field> fields = (recordType.getFields().values());

        for (Field f : fields) {
            typeSet.add(f.getFieldType());
        }

        if (recordType.getRestFieldType() != null) {
            typeSet.add(recordType.getRestFieldType());
        }

        return typeSet.size() == 1 ? typeSet.iterator().next() : TypeCreator.createUnionType(new ArrayList<>(typeSet));
    }

    public static void validateRecord(BMap m) {
        Type type = TypeUtils.getReferredType(m.getType());
        if (type.getTag() != TypeTags.RECORD_TYPE_TAG) {
            return;
        }
        Map<String, Field> fields = ((RecordType) type).getFields();
        for (String key : fields.keySet()) {
            if (isRequiredField((RecordType) type, key)) {
                throw createOpNotSupportedErrorForRecord(type, key);
            }
        }
    }

    private static boolean isRequiredField(RecordType  type, String k) {
        Map<String, Field> fields = type.getFields();
        Field field = fields.get(k);

        return (field != null && SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.REQUIRED));
    }

    private static BError createOpNotSupportedErrorForRecord(Type type, String field) {
        return ErrorCreator.createError(getModulePrefixedReason(
                MAP_LANG_LIB, OPERATION_NOT_SUPPORTED_IDENTIFIER), BLangExceptionHelper.getErrorDetails(
                        RuntimeErrors.FIELD_REMOVAL_NOT_ALLOWED, field, type.getQualifiedName()));
    }

    public static void validateRequiredFieldForRecord(BMap m, String k) {
        Type type = TypeUtils.getReferredType(m.getType());
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG && isRequiredField((RecordType) type, k)) {
            throw createOpNotSupportedErrorForRecord(type, k);
        }
    }
}
