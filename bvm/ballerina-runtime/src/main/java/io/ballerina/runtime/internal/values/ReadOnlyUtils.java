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
package io.ballerina.runtime.internal.values;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeBuilder;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectableReferenceType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.SelectivelyImmutableReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeHelper;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BField;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.types.semType.BSemType;
import io.ballerina.runtime.internal.types.semType.Core;
import io.ballerina.runtime.internal.types.semType.SemTypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.XML_LANG_LIB;
import static io.ballerina.runtime.api.constants.TypeConstants.READONLY_XML_TNAME;
import static io.ballerina.runtime.internal.errors.ErrorCodes.INVALID_READONLY_VALUE_UPDATE;
import static io.ballerina.runtime.internal.errors.ErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.internal.types.semType.Core.intersect;
import static io.ballerina.runtime.internal.types.semType.Core.isNever;

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
        throw ErrorCreator.createError(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER),
                                       ErrorHelper.getErrorMessage(INVALID_READONLY_VALUE_UPDATE));
    }

    public static Type getReadOnlyType(Type type) {
        if (type instanceof BSemType semType) {
            return bTypeReadonlyUtilDriver(semType, BTypeReadOnlyUtils::getReadOnlyType);
        }
        return BTypeReadOnlyUtils.getReadOnlyType(unwrap(type));
    }

    // TODO: these needs to be fixed when we can have non readonly semtypes
    // TODO: need better names for these
    private static Type bTypeReadonlyUtilDriver(BSemType semType, Function<BType, Type> fn) {
        BSemType semTypePart = intersect(semType, SemTypeUtils.ALL_SEMTYPE);
        BSemType bTypePart = intersect(semType, SemTypeUtils.ALL_BTYPE);
        if (isNever(bTypePart)) {
            return semTypePart;
        }
        Type readonlyBTypePart = fn.apply(unwrap(bTypePart));
        if (readonlyBTypePart == null) {
            return isNever(semType) ? PredefinedTypes.TYPE_NEVER : semTypePart;
        }
        BSemType lhs = TypeBuilder.wrap(readonlyBTypePart);
        return Core.union(lhs, semTypePart);
    }

    public static Type getReadOnlyType(Type type, Set<Type> unresolvedTypes) {
        if (type instanceof BSemType semType) {
            return bTypeReadonlyUtilDriver(semType,
                    bType -> BTypeReadOnlyUtils.getReadOnlyType(bType, unresolvedTypes));
        }
        return BTypeReadOnlyUtils.getReadOnlyType(unwrap(type), unresolvedTypes);
    }

    public static Type setImmutableTypeAndGetEffectiveType(Type type) {
        if (type instanceof BSemType semType) {
            return bTypeReadonlyUtilDriver(semType, BTypeReadOnlyUtils::setImmutableTypeAndGetEffectiveType);
        }
        return BTypeReadOnlyUtils.setImmutableTypeAndGetEffectiveType(unwrap(type));
    }

    public static Type setImmutableTypeAndGetEffectiveType(Type type, Set<Type> unresolvedTypes) {
        if (type instanceof BSemType semType) {
            return bTypeReadonlyUtilDriver(semType,
                    bType -> BTypeReadOnlyUtils.setImmutableTypeAndGetEffectiveType(bType, unresolvedTypes));
        }
        return BTypeReadOnlyUtils.setImmutableTypeAndGetEffectiveType(unwrap(type), unresolvedTypes);
    }

    private static Type getAvailableImmutableType(Type type) {
        if (type instanceof BSemType semType) {
            return bTypeReadonlyUtilDriver(semType, BTypeReadOnlyUtils::getAvailableImmutableType);
        }
        return BTypeReadOnlyUtils.getAvailableImmutableType(unwrap(type));
    }


    private static Type getImmutableType(Type type, Set<Type> unresolvedTypes) {
        if (type instanceof BSemType semType) {
            return bTypeReadonlyUtilDriver(semType,
                    bType -> BTypeReadOnlyUtils.getImmutableType(bType, unresolvedTypes));
        }
        return BTypeReadOnlyUtils.getImmutableType(unwrap(type), unresolvedTypes);
    }

    private static Type setImmutableIntersectionType(Type type, Set<Type> unresolvedTypes) {
        return BTypeReadOnlyUtils.setImmutableIntersectionType(unwrap(type), unresolvedTypes);
    }

    private static Type createAndSetImmutableIntersectionType(Type originalType, Type effectiveType) {
        return createAndSetImmutableIntersectionType(originalType.getPackage(), originalType, effectiveType);
    }

    private static Type createAndSetImmutableIntersectionType(Module pkg, Type originalType,
                                                                           Type effectiveType) {
        return BTypeReadOnlyUtils.createAndSetImmutableIntersectionType(pkg, unwrap(originalType),
                unwrap(effectiveType));
    }

    /**
     * Provides the mutable type used to create the intersection type.
     * @param intersectionType intersection type
     * @return mutable type
     */
    public static Type getMutableType(Type intersectionType) {
        return BTypeReadOnlyUtils.getMutableType(unwrap(intersectionType));
    }

    private ReadOnlyUtils() {
    }

    private static class BTypeReadOnlyUtils {

        private static Type getReadOnlyType(BType type) {
            if (type.isReadOnly()) {
                return type;
            }
            if (!TypeChecker.isSelectivelyImmutableType(type, new HashSet<>())) {
                throw new IllegalArgumentException(type.getName() + " cannot be a readonly type.");
            }
            return ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(type);
        }

        public static Type getReadOnlyType(BType type, Set<Type> unresolvedTypes) {
            if (type.isReadOnly()) {
                return type;
            }
            if (!TypeChecker.isSelectivelyImmutableType(type, new HashSet<>())) {
                throw new IllegalArgumentException(type.getName() + " cannot be a readonly type.");
            }
            return ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(type, unresolvedTypes);
        }

        public static Type setImmutableTypeAndGetEffectiveType(BType type) {
            Type immutableType = ReadOnlyUtils.getAvailableImmutableType(type);

            if (immutableType != null) {
                return immutableType;
            }
            BIntersectionType result =
                    TypeBuilder.expectBType(ReadOnlyUtils.setImmutableIntersectionType(type, new HashSet<>()));
            return result.getEffectiveType();
        }

        public static Type setImmutableTypeAndGetEffectiveType(BType type, Set<Type> unresolvedTypes) {
            Type immutableType = ReadOnlyUtils.getAvailableImmutableType(type);

            if (immutableType != null) {
                return immutableType;
            }

            BIntersectionType result =
                    TypeBuilder.expectBType(ReadOnlyUtils.setImmutableIntersectionType(type, unresolvedTypes));
            return result.getEffectiveType();
        }

        private static Type getAvailableImmutableType(BType type) {
            if (type.isReadOnly() || TypeChecker.isInherentlyImmutableType(type)) {
                return type;
            }

            if (type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                return ReadOnlyUtils.getAvailableImmutableType(((ReferenceType) type).getReferredType());
            }

            if (type.getTag() == TypeTags.INTERSECTION_TAG && type.isReadOnly()) {
                return TypeHelper.effectiveType(type);
            }

            if (type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                return ReadOnlyUtils.getAvailableImmutableType(((ReferenceType) type).getReferredType());
            }

            IntersectionType immutableType = ((SelectivelyImmutableReferenceType) type).getImmutableType();
            if (immutableType != null) {
                return TypeHelper.effectiveType(immutableType);
            }

            return null;
        }

        private static Type getImmutableType(BType type, Set<Type> unresolvedTypes) {
            if (type.isReadOnly() || TypeChecker.isInherentlyImmutableType(type)) {
                return type;
            }

            if (!unresolvedTypes.add(type)) {
                return type;
            }

            return ReadOnlyUtils.setImmutableIntersectionType(type, unresolvedTypes);
        }

        private static Type setImmutableIntersectionType(BType type, Set<Type> unresolvedTypes) {

            Type immutableType = type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG ? null :
                    ((SelectivelyImmutableReferenceType) type).getImmutableType();
            if (immutableType != null) {
                return immutableType;
            }

            switch (type.getTag()) {
                case TypeTags.XML_COMMENT_TAG:
                    BXmlType readonlyCommentType = new BXmlType(TypeConstants.READONLY_XML_COMMENT,
                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                    null),
                            TypeTags.XML_COMMENT_TAG, true);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(type, readonlyCommentType);
                case TypeTags.XML_ELEMENT_TAG:
                    BXmlType readonlyElementType = new BXmlType(TypeConstants.READONLY_XML_ELEMENT,
                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                    null),
                            TypeTags.XML_ELEMENT_TAG, true);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(type, readonlyElementType);
                case TypeTags.XML_PI_TAG:
                    BXmlType readonlyPI = new BXmlType(TypeConstants.READONLY_XML_PI,
                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                            TypeTags.XML_PI_TAG, true);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(type, readonlyPI);
                case TypeTags.XML_TAG:
                    BXmlType origXmlType = (BXmlType) type;
                    BXmlType immutableXmlType = new BXmlType(READONLY_XML_TNAME, origXmlType.getPackage(),
                            origXmlType.getTag(), true);
                    immutableXmlType.constraint =
                            ReadOnlyUtils.getImmutableType(origXmlType.constraint, unresolvedTypes);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(origXmlType, immutableXmlType);
                case TypeTags.ARRAY_TAG:
                    BArrayType origArrayType = (BArrayType) type;
                    BArrayType immutableArrayType =
                            new BArrayType(ReadOnlyUtils.getImmutableType(origArrayType.getElementType(),
                                    unresolvedTypes),
                                    origArrayType.getSize(), true);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(origArrayType, immutableArrayType);
                case TypeTags.TUPLE_TAG:
                    BTupleType origTupleType = (BTupleType) type;

                    List<Type> origTupleMemTypes = origTupleType.getTupleTypes();
                    List<Type> immutableMemTypes = new ArrayList<>(origTupleMemTypes.size());

                    for (Type origTupleMemType : origTupleMemTypes) {
                        immutableMemTypes.add(ReadOnlyUtils.getImmutableType(origTupleMemType, unresolvedTypes));
                    }

                    Type origTupleRestType = origTupleType.getRestType();

                    BTupleType immutableTupleType =
                            new BTupleType(immutableMemTypes, origTupleRestType == null ? null :
                                    ReadOnlyUtils.getImmutableType(origTupleRestType, unresolvedTypes),
                                    origTupleType.getTypeFlags(),
                                    origTupleType.isCyclic, true);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(origTupleType, immutableTupleType);
                case TypeTags.MAP_TAG:
                    BMapType origMapType = (BMapType) type;
                    BMapType immutableMapType =
                            new BMapType(ReadOnlyUtils.getImmutableType(origMapType.getConstrainedType(),
                                    unresolvedTypes), true);
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(origMapType, immutableMapType);
                case TypeTags.RECORD_TYPE_TAG:
                    BRecordType origRecordType = (BRecordType) type;

                    Map<String, Field> originalFields = origRecordType.getFields();
                    Map<String, Field> fields = new HashMap<>(originalFields.size());
                    for (Map.Entry<String, Field> entry : originalFields.entrySet()) {
                        Field originalField = entry.getValue();
                        fields.put(entry.getKey(),
                                new BField(
                                        ReadOnlyUtils.getImmutableType(originalField.getFieldType(), unresolvedTypes),
                                        originalField.getFieldName(), originalField.getFlags()));
                    }

                    BRecordType immutableRecordType = new BRecordType(
                            Utils.decodeIdentifier(origRecordType.getName().concat(" & readonly")),
                            origRecordType.getPackage(),
                            origRecordType.flags |= SymbolFlags.READONLY, fields,
                            null, origRecordType.sealed,
                            origRecordType.typeFlags);
                    for (Map.Entry<String, BFunctionPointer<Object, ?>> field : origRecordType.getDefaultValues()
                            .entrySet()) {
                        immutableRecordType.setDefaultValue(field.getKey(), field.getValue());
                    }
                    Type intersectionType =
                            ReadOnlyUtils.createAndSetImmutableIntersectionType(origRecordType,
                                    immutableRecordType);

                    Type origRecordRestFieldType = origRecordType.restFieldType;
                    if (origRecordRestFieldType != null) {
                        immutableRecordType.restFieldType =
                                ReadOnlyUtils.getImmutableType(origRecordRestFieldType, unresolvedTypes);
                    }

                    return intersectionType;
                case TypeTags.TABLE_TAG:
                    BTableType origTableType = (BTableType) type;

                    BTableType immutableTableType;

                    Type origKeyType = origTableType.getKeyType();
                    if (origKeyType != null) {
                        immutableTableType =
                                new BTableType(ReadOnlyUtils.getImmutableType(origTableType.getConstrainedType(),
                                        unresolvedTypes),
                                        ReadOnlyUtils.getImmutableType(origKeyType, unresolvedTypes), true);
                    } else {
                        immutableTableType =
                                new BTableType(ReadOnlyUtils.getImmutableType(origTableType.getConstrainedType(),
                                        unresolvedTypes),
                                        origTableType.getFieldNames(), true);
                    }

                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(origTableType, immutableTableType);
                case TypeTags.OBJECT_TYPE_TAG:
                    BObjectType origObjectType = (BObjectType) type;

                    Map<String, Field> originalObjectFields = origObjectType.getFields();
                    Map<String, Field> immutableObjectFields = new HashMap<>(originalObjectFields.size());
                    BObjectType immutableObjectType = new BObjectType(
                            Utils.decodeIdentifier(origObjectType.getName().concat(" & readonly")),
                            origObjectType.getPackage(), origObjectType.flags |= SymbolFlags.READONLY);
                    immutableObjectType.setFields(immutableObjectFields);
                    immutableObjectType.generatedInitMethod = origObjectType.getGeneratedInitMethod();
                    immutableObjectType.setInitMethod(origObjectType.getInitMethod());
                    immutableObjectType.setMethods(origObjectType.getMethods());

                    Type objectIntersectionType =
                            ReadOnlyUtils.createAndSetImmutableIntersectionType(origObjectType,
                                    immutableObjectType);

                    for (Map.Entry<String, Field> entry : originalObjectFields.entrySet()) {
                        Field originalField = entry.getValue();
                        immutableObjectFields.put(entry.getKey(),
                                new BField(ReadOnlyUtils.getImmutableType(originalField.getFieldType(),
                                        unresolvedTypes),
                                        originalField.getFieldName(), originalField.getFlags()));
                    }
                    return objectIntersectionType;
                case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                    BTypeReferenceType bType = (BTypeReferenceType) type;
                    BTypeReferenceType refType = new BTypeReferenceType(bType.getName(), bType.getPkg(),
                            bType.getTypeFlags(), true);
                    refType.setReferredType(ReadOnlyUtils.getImmutableType(bType.getReferredType(), unresolvedTypes));
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(bType, refType);
                case TypeTags.ANY_TAG:
                case TypeTags.ANYDATA_TAG:
                case TypeTags.JSON_TAG:
                    // Constructor enforces setting the immutable type for non-immutable types.
                    return (BIntersectionType) type.getImmutableType();
                default:
                    BUnionType origUnionType = (BUnionType) type;

                    Type resultantImmutableType;

                    List<Type> readOnlyMemTypes = new ArrayList<>();

                    for (Type memberType : TypeHelper.members(origUnionType)) {
                        if (TypeChecker.isInherentlyImmutableType(memberType)) {
                            readOnlyMemTypes.add(memberType);
                            continue;
                        }

                        if (!TypeChecker.isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                            continue;
                        }

                        readOnlyMemTypes.add(ReadOnlyUtils.getImmutableType(memberType, unresolvedTypes));
                    }

                    if (readOnlyMemTypes.size() == 1) {
                        resultantImmutableType = readOnlyMemTypes.iterator().next();
                    } else if (!unresolvedTypes.add(type)) {
                        resultantImmutableType = origUnionType;
                    } else {
                        resultantImmutableType = new BUnionType(readOnlyMemTypes, true, origUnionType.isCyclic);
                    }
                    return ReadOnlyUtils.createAndSetImmutableIntersectionType(origUnionType, resultantImmutableType);
            }
        }

        private static BIntersectionType createAndSetImmutableIntersectionType(BType originalType,
                                                                               BType effectiveType) {
            return createAndSetImmutableIntersectionType(originalType.getPackage(), originalType, effectiveType);
        }

        private static BIntersectionType createAndSetImmutableIntersectionType(Module pkg, BType originalType,
                                                                               BType effectiveType) {
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
                    new Type[]{originalType,
                            TypeBuilder.readonlyType()},
                    (IntersectableReferenceType) effectiveType,
                    typeFlags, true);
            originalType.setImmutableType(intersectionType);
            return intersectionType;
        }

        /**
         * Provides the mutable type used to create the intersection type.
         *
         * @param intersectionType intersection type
         * @return mutable type
         */
        public static BType getMutableType(BIntersectionType intersectionType) {
            for (Type type : intersectionType.getConstituentTypes()) {
                Type referredType = TypeUtils.getImpliedType(type);
                if (TypeUtils.getImpliedType(intersectionType.getEffectiveType()).getTag() == referredType.getTag()) {
                    // FIXME:
                    return unwrap(referredType);
                }
            }
            throw new IllegalStateException("Unsupported intersection type found: " + intersectionType);
        }

        private BTypeReadOnlyUtils() {
        }
    }
}
