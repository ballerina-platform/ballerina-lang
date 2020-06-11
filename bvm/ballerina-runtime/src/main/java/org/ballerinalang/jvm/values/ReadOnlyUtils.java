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
import org.ballerinalang.jvm.types.BIntersectionType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.BXMLType;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.types.TypeFlags;
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
import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;

/**
 * Util class for readonly-typed value related operations.
 *
 * @since 1.3.0
 */
public class ReadOnlyUtils {

    /**
     * Method to handle an update to a value, that is invalid due to the value being immutable.
     *
     * @param moduleName the name of the langlib module for whose values the error occurred
     */
    static void handleInvalidUpdate(String moduleName) {
        throw new BLangFreezeException(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER),
                                       BLangExceptionHelper.getErrorMessage(INVALID_READONLY_VALUE_UPDATE));
    }

    public static BType setImmutableTypeAndGetEffectiveType(BType type) {
        if (TypeChecker.isInherentlyImmutableType(type)) {
            return type;
        }

        if (type.getTag() == TypeTags.INTERSECTION_TAG && type.isReadOnly()) {
            return ((BIntersectionType) type).getEffectiveType();
        }

        BType immutableType = type.getImmutableType();
        if (immutableType != null) {
            return ((BIntersectionType) immutableType).getEffectiveType();
        }


        return setImmutableIntersectionType(type, new HashSet<>()).getEffectiveType();
    }

    private static BType getImmutableType(BType type, Set<BType> unresolvedTypes) {
        if (TypeChecker.isInherentlyImmutableType(type)) {
            return type;
        }

        return setImmutableIntersectionType(type, unresolvedTypes);
    }

    private static BIntersectionType setImmutableIntersectionType(BType type, Set<BType> unresolvedTypes) {

        BType immutableType = type.getImmutableType();
        if (immutableType != null) {
            return (BIntersectionType) immutableType;
        }

        switch (type.getTag()) {
            case TypeTags.XML_COMMENT_TAG:
                BXMLType readonlyCommentType = new BXMLType(TypeConstants.READONLY_XML_COMMENT,
                                                            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                         null),
                                                            TypeTags.XML_COMMENT_TAG, true);
                return createAndSetImmutableIntersectionType(type, readonlyCommentType);
            case TypeTags.XML_ELEMENT_TAG:
                BXMLType readonlyElementType = new BXMLType(TypeConstants.READONLY_XML_ELEMENT,
                                                            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                         null),
                                                            TypeTags.XML_ELEMENT_TAG, true);
                return createAndSetImmutableIntersectionType(type, readonlyElementType);
            case TypeTags.XML_PI_TAG:
                BXMLType readonlyPI = new BXMLType(TypeConstants.READONLY_XML_PI,
                                                   new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                                                   TypeTags.XML_PI_TAG, true);
                return createAndSetImmutableIntersectionType(type, readonlyPI);
            case TypeTags.XML_TAG:
                BXMLType origXmlType = (BXMLType) type;
                BXMLType immutableXmlType = new BXMLType(READONLY_XML_TNAME, origXmlType.getPackage(),
                                                         origXmlType.getTag(), true);
                immutableXmlType.constraint = getImmutableType(origXmlType.constraint, unresolvedTypes);
                return createAndSetImmutableIntersectionType(origXmlType, immutableXmlType);
            case TypeTags.ARRAY_TAG:
                BArrayType origArrayType = (BArrayType) type;
                BArrayType immutableArrayType = new BArrayType(getImmutableType(origArrayType.getElementType(),
                                                                                unresolvedTypes),
                                                               origArrayType.getSize(), true);
                return createAndSetImmutableIntersectionType(origArrayType, immutableArrayType);
            case TypeTags.TUPLE_TAG:
                BTupleType origTupleType = (BTupleType) type;

                List<BType> origTupleMemTypes = origTupleType.getTupleTypes();
                List<BType> immutableMemTypes = new ArrayList<>(origTupleMemTypes.size());

                for (BType origTupleMemType : origTupleMemTypes) {
                    immutableMemTypes.add(getImmutableType(origTupleMemType, unresolvedTypes));
                }

                BType origTupleRestType = origTupleType.getRestType();

                BTupleType immutableTupleType =
                        new BTupleType(immutableMemTypes, origTupleRestType == null ? null :
                                getImmutableType(origTupleRestType, unresolvedTypes), origTupleType.getTypeFlags(),
                                       true);
                return createAndSetImmutableIntersectionType(origTupleType, immutableTupleType);
            case TypeTags.MAP_TAG:
                BMapType origMapType = (BMapType) type;
                BMapType immutableMapType = new BMapType(getImmutableType(origMapType.getConstrainedType(),
                                                                          unresolvedTypes), true);
                return createAndSetImmutableIntersectionType(origMapType, immutableMapType);
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType origRecordType = (BRecordType) type;

                Map<String, BField> originalFields = origRecordType.getFields();
                Map<String, BField> fields = new HashMap<>(originalFields.size());
                BRecordType immutableRecordType = new BRecordType(origRecordType.getName().concat(" & readonly"),
                                                                  origRecordType.getPackage(),
                                                                  origRecordType.flags |= Flags.READONLY, fields,
                                                                  null, origRecordType.sealed,
                                                                  origRecordType.typeFlags);
                BIntersectionType intersectionType = createAndSetImmutableIntersectionType(origRecordType,
                                                                                           immutableRecordType);

                for (Map.Entry<String, BField> entry : originalFields.entrySet()) {
                    BField originalField = entry.getValue();
                    fields.put(entry.getKey(), new BField(getImmutableType(originalField.type, unresolvedTypes),
                                                          originalField.name, originalField.flags));
                }

                BType origRecordRestFieldType = origRecordType.restFieldType;
                if (origRecordRestFieldType != null) {
                    immutableRecordType.restFieldType = getImmutableType(origRecordRestFieldType, unresolvedTypes);
                }

                return intersectionType;
            case TypeTags.TABLE_TAG:
                BTableType origTableType = (BTableType) type;

                BTableType immutableTableType;

                BType origKeyType = origTableType.getKeyType();
                if (origKeyType != null) {
                    immutableTableType = new BTableType(getImmutableType(origTableType.getConstrainedType(),
                                                                                     unresolvedTypes),
                                                        getImmutableType(origKeyType, unresolvedTypes), true);
                } else {
                    immutableTableType = new BTableType(getImmutableType(origTableType.getConstrainedType(),
                                                                                     unresolvedTypes),
                                                        origTableType.getFieldNames(), true);
                }

                return createAndSetImmutableIntersectionType(origTableType, immutableTableType);
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
                // Constructor enforces setting the immutable type for non-immutable types.
                return (BIntersectionType) type.getImmutableType();
            default:
                BUnionType origUnionType = (BUnionType) type;
                BType resultantImmutableType;

                List<BType> readOnlyMemTypes = new ArrayList<>();

                for (BType memberType : origUnionType.getMemberTypes()) {
                    if (TypeChecker.isInherentlyImmutableType(memberType)) {
                        readOnlyMemTypes.add(memberType);
                        continue;
                    }

                    if (!TypeChecker.isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                        continue;
                    }

                    readOnlyMemTypes.add(getImmutableType(memberType, unresolvedTypes));
                }

                if (readOnlyMemTypes.size() == 1) {
                    resultantImmutableType = readOnlyMemTypes.iterator().next();
                } else {
                    resultantImmutableType = new BUnionType(readOnlyMemTypes, true);
                }
                return createAndSetImmutableIntersectionType(origUnionType, resultantImmutableType);
        }
    }

    private static BIntersectionType createAndSetImmutableIntersectionType(BType originalType, BType effectiveType) {
        int typeFlags = 0;

        if (effectiveType.isAnydata()) {
            typeFlags |= TypeFlags.ANYDATA;
        }

        if (effectiveType.isPureType()) {
            typeFlags |= TypeFlags.PURETYPE;
        }

        if (effectiveType.isNilable()) {
            typeFlags |= TypeFlags.NILABLE;
        }

        BIntersectionType intersectionType = new BIntersectionType(new BType[]{ originalType, BTypes.typeReadonly },
                                                                   effectiveType, typeFlags, true);
        originalType.setImmutableType(intersectionType);
        return intersectionType;
    }
}
