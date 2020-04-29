/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.BXMLType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BLangFreezeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.jvm.types.TypeConstants.READONLY_XML_TNAME;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;

/**
 * Util class for readonly-typed value related operations.
 *
 * @since 1.3.0
 */
class ReadOnlyUtils {

    /**
     * Method to handle an update to a value, that is invalid due to the value being immutable.
     *
     * @param moduleName the name of the langlib module for whose values the error occurred
     */
    static void handleInvalidUpdate(String moduleName) {
        throw new BLangFreezeException(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER),
                                       BLangExceptionHelper.getErrorMessage(INVALID_READONLY_VALUE_UPDATE));
    }

    static BType setImmutableType(BType type) {
        return setImmutableType(type, new HashSet<>());
    }

    static BType setImmutableType(BType type, Set<BType> unresolvedTypes) {

        if (TypeChecker.isReadonlyType(type) || type.isReadOnly()) {
            return type;
        }

        BType immutableType = type.getImmutableType();
        if (immutableType != null) {
            return immutableType;
        }

        switch (type.getTag()) {
            case TypeTags.XML_COMMENT_TAG:
                return BTypes.typeReadonlyComment;
            case TypeTags.XML_ELEMENT_TAG:
                return BTypes.typeReadonlyElement;
            case TypeTags.XML_PI_TAG:
                return BTypes.typeReadonlyProcessingInstruction;
            case TypeTags.XML_TAG:
                BXMLType origXmlType = (BXMLType) type;
                BXMLType immutableXmlType = new BXMLType(READONLY_XML_TNAME, origXmlType.getPackage(),
                                                         origXmlType.getTag(), true, null);
                immutableXmlType.constraint = setImmutableType(origXmlType.constraint, unresolvedTypes);
                origXmlType.setImmutableType(immutableXmlType);
                return immutableXmlType;
            case TypeTags.ARRAY_TAG:
                BArrayType origArrayType = (BArrayType) type;
                BArrayType immutableArrayType = new BArrayType(setImmutableType(origArrayType.getElementType(),
                                                                                unresolvedTypes),
                                                               origArrayType.getSize(), true, null);
                origArrayType.setImmutableType(immutableArrayType);
                return immutableArrayType;
            case TypeTags.TUPLE_TAG:
                BTupleType origTupleType = (BTupleType) type;

                List<BType> origTupleMemTypes = origTupleType.getTupleTypes();
                List<BType> immutableMemTypes = new ArrayList<>(origTupleMemTypes.size());

                for (BType origTupleMemType : origTupleMemTypes) {
                    immutableMemTypes.add(setImmutableType(origTupleMemType, unresolvedTypes));
                }

                BType origTupleRestType = origTupleType.getRestType();

                BTupleType immutableTupleType = new BTupleType(immutableMemTypes,
                                                               origTupleRestType == null ? null :
                                                                       setImmutableType(origTupleRestType,
                                                                                        unresolvedTypes),
                                                               origTupleType.getTypeFlags(), true, null);
                origTupleType.setImmutableType(immutableTupleType);
                return immutableTupleType;
            case TypeTags.MAP_TAG:
                BMapType origMapType = (BMapType) type;
                BMapType immutableMapType = new BMapType(setImmutableType(origMapType.getConstrainedType(),
                                                                          unresolvedTypes), true, null);
                origMapType.setImmutableType(immutableMapType);
                return immutableMapType;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType origRecordType = (BRecordType) type;

                Map<String, BField> originalFields = origRecordType.getFields();
                Map<String, BField> fields = new HashMap<>(originalFields.size());
                BRecordType immutableRecordType = new BRecordType(origRecordType.getName().concat(" & readonly"),
                                                                  origRecordType.getPackage(),
                                                                  origRecordType.flags |= Flags.READONLY, fields,
                                                                  null, origRecordType.sealed,
                                                                  origRecordType.typeFlags);
                origRecordType.setImmutableType(immutableRecordType);

                for (Map.Entry<String, BField> entry : originalFields.entrySet()) {
                    BField originalField = entry.getValue();
                    fields.put(entry.getKey(), new BField(setImmutableType(originalField.type, unresolvedTypes),
                                                          originalField.name, originalField.flags));
                }

                BType origRecordRestFieldType = origRecordType.restFieldType;
                if (origRecordRestFieldType != null) {
                    immutableRecordType.restFieldType = setImmutableType(origRecordRestFieldType, unresolvedTypes);
                }

                return immutableRecordType;
            // TODO: 4/24/28 Table
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
                // Constructor enforces setting the immutable type for non-immutable types.
                return type.getImmutableType();
            default:
                BUnionType origUnionType = (BUnionType) type;
                BType resultantImmutableType;

                List<BType> readOnlyMemTypes = new ArrayList<>();

                for (BType memberType : origUnionType.getMemberTypes()) {
                    if (TypeChecker.isReadonlyType(memberType)) {
                        readOnlyMemTypes.add(memberType);
                    }

                    if (!TypeChecker.isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                        continue;
                    }

                    readOnlyMemTypes.add(setImmutableType(memberType, unresolvedTypes));
                }

                if (readOnlyMemTypes.size() == 1) {
                    resultantImmutableType = readOnlyMemTypes.iterator().next();
                } else {
                    resultantImmutableType = new BUnionType(readOnlyMemTypes, true);
                }
                origUnionType.setImmutableType(resultantImmutableType);
                return resultantImmutableType;
        }
    }
}
