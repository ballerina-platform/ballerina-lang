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
package io.ballerina.runtime.values;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.TypeFlags;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BField;
import io.ballerina.runtime.types.BIntersectionType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BObjectType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.types.BTableType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.types.BXMLType;
import io.ballerina.runtime.util.Flags;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BLangFreezeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.TypeConstants.READONLY_XML_TNAME;
import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.XML_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;

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
        throw new BLangFreezeException(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER).getValue(),
                                       BLangExceptionHelper.getErrorMessage(INVALID_READONLY_VALUE_UPDATE).getValue());
    }

    public static Type setImmutableTypeAndGetEffectiveType(Type type) {
        if (TypeChecker.isInherentlyImmutableType(type)) {
            return type;
        }

        if (type.getTag() == TypeTags.INTERSECTION_TAG && type.isReadOnly()) {
            return ((BIntersectionType) type).getEffectiveType();
        }

        Type immutableType = type.getImmutableType();
        if (immutableType != null) {
            return ((BIntersectionType) immutableType).getEffectiveType();
        }


        return setImmutableIntersectionType(type, new HashSet<>()).getEffectiveType();
    }

    private static Type getImmutableType(Type type, Set<Type> unresolvedTypes) {
        if (TypeChecker.isInherentlyImmutableType(type)) {
            return type;
        }

        return setImmutableIntersectionType(type, unresolvedTypes);
    }

    private static BIntersectionType setImmutableIntersectionType(Type type, Set<Type> unresolvedTypes) {

        Type immutableType = type.getImmutableType();
        if (immutableType != null) {
            return (BIntersectionType) immutableType;
        }

        switch (type.getTag()) {
            case TypeTags.XML_COMMENT_TAG:
                BXMLType readonlyCommentType = new BXMLType(TypeConstants.READONLY_XML_COMMENT,
                                                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                       null),
                                                            TypeTags.XML_COMMENT_TAG, true);
                return createAndSetImmutableIntersectionType(type, readonlyCommentType);
            case TypeTags.XML_ELEMENT_TAG:
                BXMLType readonlyElementType = new BXMLType(TypeConstants.READONLY_XML_ELEMENT,
                                                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                       null),
                                                            TypeTags.XML_ELEMENT_TAG, true);
                return createAndSetImmutableIntersectionType(type, readonlyElementType);
            case TypeTags.XML_PI_TAG:
                BXMLType readonlyPI = new BXMLType(TypeConstants.READONLY_XML_PI,
                                                   new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
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

                List<Type> origTupleMemTypes = origTupleType.getTupleTypes();
                List<Type> immutableMemTypes = new ArrayList<>(origTupleMemTypes.size());

                for (Type origTupleMemType : origTupleMemTypes) {
                    immutableMemTypes.add(getImmutableType(origTupleMemType, unresolvedTypes));
                }

                Type origTupleRestType = origTupleType.getRestType();

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

                Map<String, Field> originalFields = origRecordType.getFields();
                Map<String, Field> fields = new HashMap<>(originalFields.size());
                BRecordType immutableRecordType = new BRecordType(origRecordType.getName().concat(" & readonly"),
                                                                  origRecordType.getPackage(),
                                                                  origRecordType.flags |= Flags.READONLY, fields,
                                                                  null, origRecordType.sealed,
                                                                  origRecordType.typeFlags);
                BIntersectionType intersectionType = createAndSetImmutableIntersectionType(origRecordType,
                                                                                           immutableRecordType);

                for (Map.Entry<String, Field> entry : originalFields.entrySet()) {
                    Field originalField = entry.getValue();
                    fields.put(entry.getKey(),
                               new BField(getImmutableType(originalField.getFieldType(), unresolvedTypes),
                                                          originalField.getFieldName(), originalField.getFlags()));
                }

                Type origRecordRestFieldType = origRecordType.restFieldType;
                if (origRecordRestFieldType != null) {
                    immutableRecordType.restFieldType = getImmutableType(origRecordRestFieldType, unresolvedTypes);
                }

                return intersectionType;
            case TypeTags.TABLE_TAG:
                BTableType origTableType = (BTableType) type;

                BTableType immutableTableType;

                Type origKeyType = origTableType.getKeyType();
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
            case TypeTags.OBJECT_TYPE_TAG:
                BObjectType origObjectType = (BObjectType) type;

                Map<String, Field> originalObjectFields = origObjectType.getFields();
                Map<String, Field> immutableObjectFields = new HashMap<>(originalObjectFields.size());
                BObjectType immutableObjectType = new BObjectType(origObjectType.getName().concat(" & readonly"),
                                                                  origObjectType.getPackage(),
                                                                  origObjectType.flags |= Flags.READONLY);
                immutableObjectType.setFields(immutableObjectFields);
                immutableObjectType.generatedInitializer = origObjectType.generatedInitializer;
                immutableObjectType.initializer = origObjectType.initializer;
                immutableObjectType.setAttachedFunctions(origObjectType.getAttachedFunctions());

                BIntersectionType objectIntersectionType = createAndSetImmutableIntersectionType(origObjectType,
                                                                                                 immutableObjectType);

                for (Map.Entry<String, Field> entry : originalObjectFields.entrySet()) {
                    Field originalField = entry.getValue();
                    immutableObjectFields.put(entry.getKey(), new BField(getImmutableType(originalField.getFieldType(),
                                                                                          unresolvedTypes),
                                                          originalField.getFieldName(), originalField.getFlags()));
                }
                return objectIntersectionType;
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
                // Constructor enforces setting the immutable type for non-immutable types.
                return (BIntersectionType) type.getImmutableType();
            default:
                BUnionType origUnionType = (BUnionType) type;
                Type resultantImmutableType;

                List<Type> readOnlyMemTypes = new ArrayList<>();

                for (Type memberType : origUnionType.getMemberTypes()) {
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

    private static BIntersectionType createAndSetImmutableIntersectionType(Type originalType, Type effectiveType) {
        return createAndSetImmutableIntersectionType(originalType.getPackage(), originalType, effectiveType);
    }

    private static BIntersectionType createAndSetImmutableIntersectionType(Module pkg, Type originalType,
                                                                           Type effectiveType) {
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

        BIntersectionType intersectionType = new BIntersectionType(pkg, // TODO: 6/3/20 Fix to use current package
                                                                   // for records and objects
                                                                   new Type []{ originalType,
                                                                           PredefinedTypes.TYPE_READONLY},
                                                                   effectiveType, typeFlags, true);
        originalType.setImmutableType(intersectionType);
        return intersectionType;
    }
}
