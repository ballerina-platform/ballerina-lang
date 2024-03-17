/*
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
 */

package io.ballerina.runtime.api;

import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.MaybeRoType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.types.semtype.BSemType;
import io.ballerina.runtime.internal.types.semtype.BSubType;
import io.ballerina.runtime.internal.types.semtype.Core;
import io.ballerina.runtime.internal.types.semtype.SemTypeUtils;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.XML_LANG_LIB;
import static io.ballerina.runtime.internal.TypeChecker.isAnydata;
import static io.ballerina.runtime.internal.TypeChecker.isNever;
import static io.ballerina.runtime.internal.TypeChecker.isReadonly;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_DECIMAL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_FLOAT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_INT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_NEVER;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_STRING;

// TODO: merge this with the TypeCreator. Currently keeping this separate to avoid conflicts with the old code.
public final class TypeBuilder {

    private TypeBuilder() {
    }

    public static <T extends BType> T expectBType(Type type) {
        return (T) type;
    }

    // TODO: these are temporary hacks to convert between SemTypes and BTypes. Ideally we should never create BTypes
    //   and use TypeHelper to do stuff we currently need specific BTypes for. This should happen naturally as we
    //   convert more and more types to SemTypes
    @Deprecated
    public static BSemType toSemType(Type type) {
        if (type instanceof BSemType semType) {
            if (semType.poisoned) {
                BType bType = semType.getBType();
                return toSemType(bType);
            }
            return semType;
        }
        if (type instanceof BUnionType unionType) {
            // TODO: not sure if this needs ordered members
            BSemType semType = unionFrom(unionType.getName(), unionType.getPackage(), unionType.getMemberTypes());
            if (unionType instanceof BJsonType jsonType && !jsonType.getMemberTypes().isEmpty()) {
                semType.setBTypeClass(BSubType.BTypeClass.BJson);
            } else if (unionType instanceof BAnydataType) {
                semType.setBTypeClass(BSubType.BTypeClass.BAnyData);
            }
            return semType;
        }
        if (type instanceof BTypeReferenceType referenceType) {
            return toSemType(referenceType.getReferredType());
        }
        return SemTypeUtils.SemTypeBuilder.from((BType) type);
    }

    @Deprecated
    public static <T extends Type> T toBType(Type type) {
        Type innerType = type;
        while (innerType instanceof BSemType semType) {
            innerType = semType.getBType();
        }
        return (T) innerType;
    }

    // Basic Types
    public static Type neverType() {
        return SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NEVER);
    }

    public static Type nilType() {
        return SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NIL);
    }

    public static Type intType() {
        return SemTypeUtils.SemTypeBuilder.from(BT_INT);
    }

    public static Type decimalType() {
        return SemTypeUtils.SemTypeBuilder.from(BT_DECIMAL);
    }

    public static Type booleanType() {
        return SemTypeUtils.SemTypeBuilder.from(BT_BOOLEAN);
    }


    public static Type streamType() {
        return toSemType(new BStreamType(TypeConstants.STREAM_TNAME, TypeCache.TYPE_ANY,
                SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NIL),
                TypeCache.EMPTY_MODULE));
    }


    public static Type listType() {
        // TODO: list is any|error[] (we need union operation here)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // Type operations
    // TODO: once semtypes are properly implemented these should become just wrappers to Core
    public static Type intersect(Type type1, Type type2) {
        if (isReadonly(type1) || isReadonly(type2)) {
            Type other = isReadonly(type1) ? type2 : type1;
            return toReadonlyType(other);
        }
        throw new UnsupportedOperationException("unsupported intersection");
    }

    public static Type union(Type type1, Type type2, Identifier identifier) {
        BSemType semType1 = toSemType(type1);
        BSemType semType2 = toSemType(type2);
        BSemType result = Core.union(semType1, semType2);
        // TODO: avoid creating new modules instead use some sort of flyweight here
        String name = identifier.name;
        if (name != null && !name.isEmpty()) {
            result.setIdentifiers(name, new Module(identifier.org, identifier.pkgName, identifier.version));
        } else {
            result.setOrderedUnionMembers(new Type[]{type1, type2});
        }
        return result;
    }

    public static Type union(Type type1, Type type2) {
        BSemType semType1 = toSemType(type1);
        BSemType semType2 = toSemType(type2);
        BSemType result = Core.union(semType1, semType2);
        result.setOrderedUnionMembers(new Type[]{type1, type2});
        return result;
    }

    static Type unionFrom(Collection<Type> types) {
        Iterator<Type> it = types.iterator();
        Type first = it.next();
        if (!it.hasNext()) {
            return first;
        }
        Type second = it.next();
        List<Type> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        return union(first, second, rest.toArray(new Type[0]));
    }

    public static Type union(Type type1, Type type2, Type... rest) {
        Type result = union(type1, type2);
        for (Type type : rest) {
            result = union(result, type);
        }

        BSemType semType = (BSemType) result;
        setMemberOrdering(semType, type1, type2, rest);

        return result;
    }

    // Set the ordering so we can get proper string output
    private static void setMemberOrdering(BSemType semType, Type type1, Type type2, Type... rest) {
        Type[] orderedMembers = new Type[rest.length + 2];
        orderedMembers[0] = type1;
        orderedMembers[1] = type2;
        System.arraycopy(rest, 0, orderedMembers, 2, rest.length);
        semType.setOrderedUnionMembers(orderedMembers);
    }

    public static Type union(String name, Module module, Type type1, Type type2, Type... rest) {
        Type result = union(type1, type2);
        for (Type type : rest) {
            result = union(result, type);
        }
        // TODO: it is better if we can avoid casting may be make union base method return a BSemType (need to update
        //   compiler side for this to work)
        BSemType semType = (BSemType) result;
        if (name != null && !name.isEmpty()) {
            semType.setIdentifiers(name, module);
        } else {
            setMemberOrdering(semType, type1, type2, rest);
        }
        return result;
    }

    static BSemType unionFrom(String name, Module module, Collection<Type> members) {
        Iterator<Type> it = members.iterator();
        if (!it.hasNext()) {
            return SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NEVER);
        }
        Type first = it.next();
        if (!it.hasNext()) {
            return toSemType(first);
        }
        Type second = it.next();
        List<Type> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        return (BSemType) union(name, module, first, second, rest.toArray(new Type[0]));
    }

    @Deprecated
    public static Type byteType() {
        BSemType semType = SemTypeUtils.SemTypeBuilder.intSubType(0L, UNSIGNED8_MAX_VALUE.longValue());
        semType.setByteClass();
        return semType;
    }

    public static Type floatType() {
        return SemTypeUtils.SemTypeBuilder.from(BT_FLOAT);
    }

    public static Type stringType() {
        return SemTypeUtils.SemTypeBuilder.from(BT_STRING);
    }

    public static Type xmlType() {
        return toSemType(new BXmlType(TypeConstants.XML_TNAME,
                union(TypeCache.TYPE_ELEMENT, TypeCache.TYPE_COMMENT, TypeCache.TYPE_PROCESSING_INSTRUCTION,
                        TypeCache.TYPE_TEXT), TypeCache.EMPTY_MODULE));
    }

    // With SemType we will accept a Bdd instead of type
    public static Type xmlSubType(Type constraint) {
        return toSemType(new BXmlType(constraint, constraint.isReadOnly()));
    }

    public static Type xmlSimpleSubType(int primitives) {
        List<Type> constraints = new ArrayList<>();
        boolean isReadonly = true;
        for (int primitive : XML_PRIMITIVES) {
            if ((primitives & primitive) != 0) {
                constraints.add(xmlPrimitiveType(primitive));
                if (primitive >= XML_PRIMITIVE_ELEMENT_RW) {
                    isReadonly = false;
                }
            }
        }
        if (constraints.size() == 1) {
            return constraints.get(0);
        }
        // TODO: if all rw primitives are set also return the top type
        return toSemType(new BXmlType(unionFrom(constraints), isReadonly));
    }

    private static Type xmlPrimitiveType(int primitive) {
        return switch (primitive) {
            case XML_PRIMITIVE_ELEMENT_RO -> TypeCache.TYPE_READONLY_ELEMENT;
            case XML_PRIMITIVE_PI_RO -> TypeCache.TYPE_READONLY_PROCESSING_INSTRUCTION;
            case XML_PRIMITIVE_COMMENT_RO -> TypeCache.TYPE_READONLY_COMMENT;
            case XML_PRIMITIVE_ELEMENT_RW -> TypeCache.TYPE_ELEMENT;
            case XML_PRIMITIVE_PI_RW -> TypeCache.TYPE_PROCESSING_INSTRUCTION;
            case XML_PRIMITIVE_COMMENT_RW -> TypeCache.TYPE_COMMENT;
            case XML_PRIMITIVE_TEXT -> TypeCache.TYPE_TEXT;
            // We currently don't support unions, never and text
            case XML_PRIMITIVE_NEVER -> TypeCache.TYPE_XML_NEVER;
            default -> throw new IllegalStateException("Unexpected value: " + primitive);
        };
    }

    static final int XML_PRIMITIVE_NEVER = 1;
    static final int XML_PRIMITIVE_TEXT = 1 << 1;
    static final int XML_PRIMITIVE_ELEMENT_RO = 1 << 2;
    static final int XML_PRIMITIVE_PI_RO = 1 << 3;
    static final int XML_PRIMITIVE_COMMENT_RO = 1 << 4;
    static final int XML_PRIMITIVE_ELEMENT_RW = 1 << 5;
    static final int XML_PRIMITIVE_PI_RW = 1 << 6;
    static final int XML_PRIMITIVE_COMMENT_RW = 1 << 7;
    static final int[] XML_PRIMITIVES =
            {XML_PRIMITIVE_NEVER, XML_PRIMITIVE_TEXT, XML_PRIMITIVE_ELEMENT_RO, XML_PRIMITIVE_PI_RO,
                    XML_PRIMITIVE_COMMENT_RO, XML_PRIMITIVE_ELEMENT_RW, XML_PRIMITIVE_PI_RW, XML_PRIMITIVE_COMMENT_RW};

    // TODO: this is because we treat BTuple and BArray types are separate types. This will be replaced by
    //  listSubType method
    @Deprecated
    public static Type tupleSubType(Type[] memberTypes, int fixedLength, Type restType) {
        List<Type> memberTypeList = new ArrayList<>(fixedLength);
        boolean setAnydataFlag = isAnydata(restType);
        for (int i = 0; i < fixedLength; i++) {
            if (i < memberTypes.length) {
                Type memberType = memberTypes[i];
                if (!isAnydata(memberType)) {
                    setAnydataFlag = false;
                }
                memberTypeList.add(memberType);
            } else {
                memberTypeList.add(memberTypes[memberTypes.length - 1]);
            }
        }
        // TODO: how to figure out other flags?
        int flags = 0;
        if (setAnydataFlag) {
            flags |= TypeFlags.ANYDATA;
        }
        if (isNever(restType)) {
            return toSemType(new BTupleType(memberTypeList, null, flags, false, false));
        }
        // TODO: how to detect if the type is cyclic?
        return toSemType(new BTupleType(memberTypeList, restType, flags, false, false));
    }

    // TODO: when we have proper intersection / readonly in semtype this should be just a wrapper for type & READONLY
    private static Type toReadonlyType(Type type) {
        if (TypeChecker.isSameType(type, TypeCache.TYPE_ANY)) {
            return TypeCache.TYPE_READONLY_ANY;
        }
        if (TypeChecker.isSameType(type, PredefinedTypes.TYPE_ANYDATA)) {
            return PredefinedTypes.TYPE_READONLY_ANYDATA;
        }
        if (TypeChecker.isSameType(type, PredefinedTypes.TYPE_JSON)) {
            return PredefinedTypes.TYPE_READONLY_JSON;
        }
        Type inner = toBType(type);
        if (inner instanceof MaybeRoType maybeRoType) {
            return maybeRoType.toReadonlyType();
        }
        throw new UnsupportedOperationException("Can't convert " + type + " to readonly type");
    }

    public static Type listSubType(Type[] memberTypes, int fixedLength, Type restType) {
        if (fixedLength != 0) {
            return toSemType(new BArrayType(memberTypes[0], fixedLength));
        }
        return toSemType(new BArrayType(restType));
    }

    public static Type intSubType(long min, long max) {
        return SemTypeUtils.SemTypeBuilder.intSubType(min, max);
    }

    public static Type floatSubType(boolean allowed, double... values) {
        return SemTypeUtils.SemTypeBuilder.floatSubType(allowed, values);
    }

    public static Type decimalSubType(boolean allowed, BigDecimal... values) {
        return SemTypeUtils.SemTypeBuilder.decimalSubType(allowed, values);
    }

    public static Type booleanSubType(boolean value) {
        return value ? TypeCache.BOOLEAN_TRUE : TypeCache.BOOLEAN_FALSE;
    }

    public static Type stringSubType(StringSubtypeData data) {
        if (data.nonCharsAllowed && data.nonChars.length == 0 && !(data.charsAllowed) && data.chars.length == 0) {
            return TypeCache.TYPE_STRING_CHAR;
        }
        return SemTypeUtils.SemTypeBuilder.stringSubType(data.charsAllowed, data.chars, data.nonCharsAllowed,
                data.nonChars);
    }

    public record Identifier(String name, String org, String pkgName, String version) {

    }

    // TODO: ideally this should cache things such as common integer singletons, boolean subtypes etc. Also note that
    //  this is suppose to be a implementation detail of TypeBuilder unlike Predefined types which is part of the
    //  public API
    // TODO: ideally we should initialize these lazily (we shouldn't waste memory/time for type users never uses)
    private static class TypeCache {

        private static final Module EMPTY_MODULE = new Module(null, null, null);

        private static final Type TYPE_STRING_CHAR =
                SemTypeUtils.SemTypeBuilder.stringSubType(false, new String[0], true, new String[0]);

        private static final Type TYPE_ELEMENT = toSemType(new BXmlType(TypeConstants.XML_ELEMENT,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                        null), TypeTags.XML_ELEMENT_TAG, false));
        private static final Type TYPE_READONLY_ELEMENT =
                toSemType(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ELEMENT));

        private static final Type TYPE_PROCESSING_INSTRUCTION = toSemType(new BXmlType(TypeConstants.XML_PI,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                        XML_LANG_LIB, null),
                TypeTags.XML_PI_TAG, false));
        private static final Type TYPE_READONLY_PROCESSING_INSTRUCTION =
                toSemType(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_PROCESSING_INSTRUCTION));

        private static final Type TYPE_COMMENT = toSemType(new BXmlType(TypeConstants.XML_COMMENT,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                        null), TypeTags.XML_COMMENT_TAG, false));
        private static final Type TYPE_READONLY_COMMENT =
                toSemType(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_COMMENT));

        private static final Type TYPE_TEXT = toSemType(new BXmlType(TypeConstants.XML_TEXT,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                TypeTags.XML_TEXT_TAG, true));

        private static final Type TYPE_XML_NEVER =
                toSemType(new BXmlType(TypeConstants.XML_TNAME, SemTypeUtils.SemTypeBuilder.from(BT_NEVER),
                        EMPTY_MODULE, true));

        private static final Type TYPE_ANY = toSemType(new BAnyType(TypeConstants.ANY_TNAME, EMPTY_MODULE, false));
        private static final Type TYPE_READONLY_ANY =
                toSemType(new BAnyType(TypeConstants.READONLY_ANY_TNAME, EMPTY_MODULE, true));

        private static final Type BOOLEAN_TRUE = SemTypeUtils.SemTypeBuilder.booleanSubType(true);
        private static final Type BOOLEAN_FALSE = SemTypeUtils.SemTypeBuilder.booleanSubType(false);

        private TypeCache() {
        }


    }

    public static final class StringSubtypeData {

        private boolean charsAllowed;
        private boolean nonCharsAllowed;
        private String[] chars; // Java char can't represent unicode values
        private String[] nonChars;

        public StringSubtypeData() {
            this.charsAllowed = false;
            this.nonCharsAllowed = false;
            this.chars = null;
            this.nonChars = null;
        }

        public StringSubtypeData includeNonChars(String... values) {
            return setStringState(true, values);
        }

        public StringSubtypeData excludeNonChars(String... values) {
            return setStringState(false, values);
        }

        public StringSubtypeData includeChars(String... values) {
            return setCharState(true, values);
        }

        public StringSubtypeData excludeChars(String... values) {
            return setCharState(false, values);
        }

        private StringSubtypeData setStringState(boolean nonCharsAllowed, String[] nonChars) {
            if (this.nonChars != null) {
                throw new UnsupportedOperationException("Can't include and exclude strings at the same time");
            }
            this.nonCharsAllowed = nonCharsAllowed;
            this.nonChars = nonChars;
            return this;
        }

        private StringSubtypeData setCharState(boolean charsAllowed, String[] chars) {
            if (this.chars != null) {
                throw new UnsupportedOperationException("Can't include and exclude chars at the same time");
            }
            this.charsAllowed = charsAllowed;
            this.chars = chars;
            return this;
        }
    }
}
