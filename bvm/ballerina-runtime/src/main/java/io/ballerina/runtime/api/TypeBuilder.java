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
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.MaybeRoType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.IteratorUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BByteType;
import io.ballerina.runtime.internal.types.BDecimalType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BFloatType;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BHandleType;
import io.ballerina.runtime.internal.types.BIntegerType;
import io.ballerina.runtime.internal.types.BIteratorType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BNeverType;
import io.ballerina.runtime.internal.types.BReadonlyType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BStringType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlAttributesType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.types.semType.BSemType;
import io.ballerina.runtime.internal.types.semType.BSubTypeData;
import io.ballerina.runtime.internal.types.semType.SemTypeUtils;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VALUE_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.XML_LANG_LIB;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BOOLEAN;

public final class TypeBuilder {

    public static final BSemType PURE_B_TYPE = SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.UniformTypeCodes.UT_BTYPE);
    private TypeBuilder() {
    }

    public static <T extends BType> T expectBType(Type type) {
        return (T) type;
    }

    public static BSemType wrap(Type type) {
        if (type instanceof BSemType semType) {
            return semType;
        }
        if (type instanceof BUnionType unionType) {
            // TODO: not sure if this needs ordered members
            BSemType semType = unionFrom(unionType.getName(), unionType.getPackage(), unionType.getMemberTypes());
//            if (unionType.isCyclic()) {
//                semType.addCyclicMembers(List.of());
//            }
            return semType;
        }
        if (type instanceof BTypeReferenceType referenceType) {
            return wrap(referenceType.getReferredType());
        }
//        if (type instanceof BIntersectionType intersectionType) {
//            return wrap(intersectionType.getEffectiveType());
//        }
        return SemTypeUtils.SemTypeBuilder.from((BType) type);
    }

    public static <T extends Type> T unwrap(Type type) {
        Type innerType = type;
        while (innerType instanceof BSemType semType) {
            innerType = semType.getBType();
        }
        return (T) innerType;
    }

    // TODO: move these to predefinedTypes
    // Then actual constructors must check the type cache for these fields
    static final Type INT_UNSIGNED_8 = unsignedIntSubType(255);
    static final Type INT_UNSIGNED_16 = unsignedIntSubType(65535);
    static final Type INT_UNSIGNED_32 = unsignedIntSubType(4294967295L);

    static final Type INT_SIGNED_8 = intSubType(-128, 127);
    static final Type INT_SIGNED_16 = intSubType(-32768, 32767);
    static final Type INT_SIGNED_32 = intSubType(-2147483648, 2147483647);
    static final Type STRING_CHAR = stringSubType(new StringSubtypeData().includeStrings().excludeChars());

    // Basic Types
    public static Type neverType() {
        return TypeCache.TYPE_NEVER;
    }

    public static Type nilType() {
        return TypeCache.TYPE_NULL;
    }

    public static Type intType() {
        return TypeCache.TYPE_INT;
    }

    public static Type decimalType() {
        return TypeCache.TYPE_DECIMAL;
    }

    public static Type booleanType() {
        return TypeCache.TYPE_BOOLEAN;
    }

    public static Type readonlyType() {
        return TypeCache.TYPE_READONLY;
    }

    public static Type anyType() {
        return TypeCache.TYPE_ANY;
    }

    public static Type anydataType() {
        return TypeCache.TYPE_ANYDATA;
    }

    public static Type typedescType() {
        return TypeCache.TYPE_TYPEDESC;
    }

    public static Type typeMap() {
        return TypeCache.TYPE_MAP;
    }

    public static Type typeFuture() {
        return TypeCache.TYPE_FUTURE;
    }

    public static Type typeHandle() {
        return TypeCache.TYPE_HANDLE;
    }

    public static Type typeStream() {
        return TypeCache.TYPE_STREAM;
    }

    public static Type typeError() {
        return TypeCache.TYPE_ERROR;
    }

    public static Type listType() {
        // TODO: list is any|error[] (we need union operation here)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static Type jsonType() {
        // TODO: json is a union type not a basic type
        return TypeCache.TYPE_JSON;
    }

    // Type operations
    public static Type intersect(Type type1, Type type2) {
        if (isReadonly(type1) || isReadonly(type2)) {
            Type other = isReadonly(type1) ? type2 : type1;
            return toReadonlyType(other);
        }
        throw new UnsupportedOperationException("unsupported intersection");
    }

    public static Type union(Type type1, Type type2, Identifier identifier) {
        if (type1 instanceof BSemType semType1 && type2 instanceof BSemType semType2) {
            BSemType result = SemTypeUtils.TypeOperation.union(semType1, semType2);
            // TODO: avoid creating new modules instead use some sort of flyweight here
            String name = identifier.name;
            if (name != null && !name.isEmpty()) {
                result.setIdentifiers(name, new Module(identifier.org, identifier.pkgName, identifier.version));
            } else {
                result.setOrderedUnionMembers(new Type[]{type1, type2});
            }
            return result;
        }
        throw new RuntimeException("unexpected named union");
    }

    // TODO: next we need to make sure all the predefined union types could be build using this
    public static Type union(Type type1, Type type2) {
        if (type1 instanceof BSemType semType1 && type2 instanceof BSemType semType2) {
            BSemType result = SemTypeUtils.TypeOperation.union(semType1, semType2);
            result.setOrderedUnionMembers(new Type[]{type1, type2});
            return result;
        }
        return union(wrap(type1), wrap(type2));
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

        // Set the ordering so we can get proper string output
        BSemType semType = (BSemType) result;
        Type[] orderedMembers = new Type[rest.length + 2];
        orderedMembers[0] = type1;
        orderedMembers[1] = type2;
        System.arraycopy(rest, 0, orderedMembers, 2, rest.length);
        semType.setOrderedUnionMembers(orderedMembers);

        return result;
    }

    static BSemType unionFrom(String name, Module module, Collection<Type> members) {
        Iterator<Type> it = members.iterator();
        if (!it.hasNext()) {
            return (BSemType) TypeCache.TYPE_NEVER;
        }
        Type first = it.next();
        if (!it.hasNext()) {
            return wrap(first);
        }
        Type second = it.next();
        List<Type> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        return (BSemType) union(name, module, first, second, rest.toArray(new Type[0]));
    }

    public static Type union(String name, Module module, Type type1, Type type2, Type... rest) {
        Type result = union(type1, type2);
        for (Type type : rest) {
            result = union(result, type);
        }
        // TODO: it is better if we can avoid casting may be make union base method return a BSemType (need to update
        // compiler side for this to work)
        BSemType semType = (BSemType) result;
        if (name != null && !name.isEmpty()) {
            semType.setIdentifiers(name, module);
        } else {
            // TODO: common code
            Type[] orderedMembers = new Type[rest.length + 2];
            orderedMembers[0] = type1;
            orderedMembers[1] = type2;
            System.arraycopy(rest, 0, orderedMembers, 2, rest.length);
            semType.setOrderedUnionMembers(orderedMembers);
        }
        return result;
    }

    // TODO: we need to do intersection before doing json and xml (represent them as intersection of their non readonly
    // type and readonly type)
    // FIXME: this is a hack to support byte since it has a different BType. Once we wrap it in SemType this too can
    // use intSubType
    @Deprecated
    public static Type byteType() {
        return TypeCache.TYPE_BYTE;
    }

    public static Type floatType() {
        return TypeCache.TYPE_FLOAT;
    }

    public static Type stringType() {
        return TypeCache.TYPE_STRING;
    }

    public static Type xmlType() {
        return TypeCache.TYPE_XML;
    }

    // With SemType we will accept a Bdd instead of type
    public static Type xmlSubType(Type constraint) {
        return wrap(new BXmlType(constraint, constraint.isReadOnly()));
    }

    public static Type xmlSimpleSubType(int primitives) {
        List<Type> constraints = new ArrayList<>();
        boolean isReadonly = true;
        for (int primitive : XML_PRIMITIVES) {
            if ((primitives & primitive) != 0) {
                constraints.add(XmlPrimitiveType(primitive));
                if (primitive >= XML_PRIMITIVE_ELEMENT_RW) {
                    isReadonly = false;
                }
            }
        }
        if (constraints.size() == 1) {
            return constraints.get(0);
        }
        // TODO: if all rw primitives are set also return the top type
        return wrap(new BXmlType(unionFrom(constraints), isReadonly));
    }

    private static Type XmlPrimitiveType(int primitive) {
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

    // BTypeHack: this is because we treat BTuple and BArray types are separate types. This will be replaced by
    // listSubType method
    @Deprecated
    public static Type tupleSubType(Type[] memberTypes, int fixedLength, Type restType) {
        List<Type> memberTypeList = new ArrayList<>(fixedLength);
        boolean setAnydataFlag = true;
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
            return wrap(new BTupleType(memberTypeList, null, flags, false, false));
        }
        // TODO: how to detect if the type is cyclic?
        // - Look in to type ref
        return wrap(new BTupleType(memberTypeList, restType, flags, false, false));
    }

    private static Type toReadonlyType(Type type) {
        if (TypeChecker.isSameType(type, TypeCache.TYPE_ANY)) {
            return TypeCache.TYPE_READONLY_ANY;
        }
        if (TypeChecker.isSameType(type, TypeCache.TYPE_ANYDATA)) {
            return TypeCache.TYPE_READONLY_ANYDATA;
        }
        if (TypeChecker.isSameType(type, TypeCache.TYPE_JSON)) {
            return TypeCache.TYPE_READONLY_JSON;
        }
        Type inner = unwrap(type);
        if (inner instanceof MaybeRoType maybeRoType) {
            return maybeRoType.toReadonlyType();
        }
        throw new UnsupportedOperationException("Can't convert " + type + " to readonly type");
    }

    public static Type listSubType(Type[] memberTypes, int fixedLength, Type restType) {
        if (fixedLength != 0) {
            return wrap(new BArrayType(memberTypes[0], fixedLength));
        }
        return wrap(new BArrayType(restType));
    }

    private static boolean isNever(Type type) {
        // TODO:
        return type == TypeCache.TYPE_NEVER;
    }

    private static boolean isAnydata(Type type) {
        return TypeChecker.checkIsType(type, TypeCache.TYPE_ANYDATA);
    }

    private static boolean isReadonly(Type type) {
        return TypeChecker.checkIsType(type, TypeCache.TYPE_READONLY);
    }

    private static boolean isXml(Type type) {
        return TypeChecker.checkIsType(type, TypeCache.TYPE_XML);
    }

    public static Type intSubType(long min, long max) {
        if (min == 0) {
            return unsignedIntSubType(max);
        }
        if (min == -128 && max == 127) {
            return TypeCache.TYPE_INT_SIGNED_8;
        }
        if (min == -32768 && max == 32767) {
            return TypeCache.TYPE_INT_SIGNED_16;
        }
        if (min == -2147483648 && max == 2147483647) {
            return TypeCache.TYPE_INT_SIGNED_32;
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static Type unsignedIntSubType(long max) {
        if (max == 255) {
            return TypeCache.TYPE_INT_UNSIGNED_8;
        }
        if (max == 65535) {
            return TypeCache.TYPE_INT_UNSIGNED_16;
        }
        if (max == 4294967295L) {
            return TypeCache.TYPE_INT_UNSIGNED_32;
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static Type floatSubType(boolean allowed, double... values) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static Type decimalSubType(boolean allowed, BigDecimal... values) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static Type booleanSubType(boolean value) {
        return value ? TypeCache.BOOLEAN_TRUE : TypeCache.BOOLEAN_FALSE;
    }

    public static Type stringSubType(StringSubtypeData data) {
        if (data.stringPositive && data.strings.length == 0 && !(data.includingChars) && data.chars.length == 0) {
            return TypeCache.TYPE_STRING_CHAR;
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // TODO: consider
    // 1. Passing in a full module (we are any way have to create one every time this is used)
    // 2. Create an instance of Type builder for each module (again not sure good we are creating far too many instances)
    public record Identifier(String name, String org, String pkgName, String version) {

    }

    protected static class TypeCache {

        private static final Module EMPTY_MODULE = new Module(null, null, null);

        public static final Type TYPE_INT = wrap(new BIntegerType(TypeConstants.INT_TNAME, EMPTY_MODULE));
        public static final Type TYPE_INT_SIGNED_8 =
                wrap(new BIntegerType(TypeConstants.SIGNED8,
                        new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                        TypeTags.SIGNED8_INT_TAG));
        public static final Type TYPE_INT_SIGNED_16 =
                wrap(new BIntegerType(TypeConstants.SIGNED16,
                        new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                        TypeTags.SIGNED16_INT_TAG));
        public static final Type TYPE_INT_SIGNED_32 =
                wrap(new BIntegerType(TypeConstants.SIGNED32,
                        new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                        TypeTags.SIGNED32_INT_TAG));
        public static final Type TYPE_INT_UNSIGNED_8 =
                wrap(new BIntegerType(TypeConstants.UNSIGNED8,
                        new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                        TypeTags.UNSIGNED8_INT_TAG));
        public static final Type TYPE_INT_UNSIGNED_16 =
                wrap(new BIntegerType(TypeConstants.UNSIGNED16,
                        new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                        TypeTags.UNSIGNED16_INT_TAG));
        public static final Type TYPE_INT_UNSIGNED_32 =
                wrap(new BIntegerType(TypeConstants.UNSIGNED32,
                        new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                        TypeTags.UNSIGNED32_INT_TAG));
        public static final Type TYPE_BOOLEAN = SemTypeUtils.SemTypeBuilder.from(UT_BOOLEAN);
        public static final Type TYPE_BYTE = wrap(new BByteType(TypeConstants.BYTE_TNAME, EMPTY_MODULE));
        public static final Type TYPE_FLOAT = wrap(new BFloatType(TypeConstants.FLOAT_TNAME, EMPTY_MODULE));
        public static final Type TYPE_DECIMAL = wrap(new BDecimalType(TypeConstants.DECIMAL_TNAME, EMPTY_MODULE));
        public static final Type TYPE_STRING = wrap(new BStringType(TypeConstants.STRING_TNAME, EMPTY_MODULE));
        public static final Type TYPE_STRING_CHAR = wrap(new BStringType(TypeConstants.CHAR,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                        STRING_LANG_LIB, null),
                TypeTags.CHAR_STRING_TAG));

        public static final Type TYPE_READONLY = wrap(new BReadonlyType(TypeConstants.READONLY_TNAME, EMPTY_MODULE));
        public static final Type TYPE_ELEMENT = wrap(new BXmlType(TypeConstants.XML_ELEMENT,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                        null), TypeTags.XML_ELEMENT_TAG, false));
        public static final Type TYPE_READONLY_ELEMENT =
                wrap(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ELEMENT));

        public static final Type TYPE_PROCESSING_INSTRUCTION = wrap(new BXmlType(TypeConstants.XML_PI,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                        XML_LANG_LIB, null),
                TypeTags.XML_PI_TAG, false));
        public static final Type TYPE_READONLY_PROCESSING_INSTRUCTION =
                wrap(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_PROCESSING_INSTRUCTION));

        public static final Type TYPE_COMMENT = wrap(new BXmlType(TypeConstants.XML_COMMENT,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                        null), TypeTags.XML_COMMENT_TAG, false));
        public static final Type TYPE_READONLY_COMMENT =
                wrap(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_COMMENT));

        public static final Type TYPE_TEXT = wrap(new BXmlType(TypeConstants.XML_TEXT,
                new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                TypeTags.XML_TEXT_TAG, true));

        public static final Type TYPE_XML_NEVER =
                wrap(new BXmlType(TypeConstants.XML_TNAME, new BNeverType(EMPTY_MODULE),
                        EMPTY_MODULE, true));

        public static final Type TYPE_XML = wrap(new BXmlType(TypeConstants.XML_TNAME,
                union(TYPE_ELEMENT, TYPE_COMMENT, TYPE_PROCESSING_INSTRUCTION, TYPE_TEXT), EMPTY_MODULE));

        public static final Type TYPE_READONLY_XML = wrap(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_XML));

        public static final Type TYPE_XML_ELEMENT_SEQUENCE = wrap(new BXmlType(PredefinedTypes.TYPE_ELEMENT, false));
        public static final Type TYPE_XML_COMMENT_SEQUENCE = wrap(new BXmlType(PredefinedTypes.TYPE_COMMENT, false));
        public static final Type TYPE_XML_PI_SEQUENCE =
                wrap(new BXmlType(PredefinedTypes.TYPE_PROCESSING_INSTRUCTION, false));
        public static final Type TYPE_XML_TEXT_SEQUENCE = wrap(new BXmlType(PredefinedTypes.TYPE_TEXT, false));

        public static final Type TYPE_ANY = wrap(new BAnyType(TypeConstants.ANY_TNAME, EMPTY_MODULE, false));
        public static final Type TYPE_READONLY_ANY =
                wrap(new BAnyType(TypeConstants.READONLY_ANY_TNAME, EMPTY_MODULE, true));
        public static final Type TYPE_TYPEDESC = wrap(new BTypedescType(TypeConstants.TYPEDESC_TNAME, EMPTY_MODULE));
        public static final Type TYPE_MAP = wrap(new BMapType(TypeConstants.MAP_TNAME, TYPE_ANY, EMPTY_MODULE));
        public static final Type TYPE_FUTURE = wrap(new BFutureType(TypeConstants.FUTURE_TNAME, EMPTY_MODULE));

        public static final Type TYPE_NULL = SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.UniformTypeCodes.UT_NIL);
        public static final Type TYPE_NEVER = SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.UniformTypeCodes.UT_NEVER);
        public static final Type TYPE_XML_ATTRIBUTES =
                wrap(new BXmlAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME, EMPTY_MODULE));
        public static final Type TYPE_ITERATOR = wrap(new BIteratorType(TypeConstants.ITERATOR_TNAME, EMPTY_MODULE));
        public static final Type TYPE_ANY_SERVICE = wrap(new BServiceType(TypeConstants.SERVICE, EMPTY_MODULE, 0));
        public static final Type TYPE_HANDLE = wrap(new BHandleType(TypeConstants.HANDLE_TNAME, EMPTY_MODULE));
        public static final Type TYPE_STREAM = wrap(new BStreamType(TypeConstants.STREAM_TNAME, TYPE_ANY, TYPE_NULL,
                EMPTY_MODULE));

        public static final Type TYPE_JSON;
        public static final Type TYPE_READONLY_JSON;
        public static final Type TYPE_JSON_ARRAY;
        public static final Type TYPE_ANYDATA;
        public static final Type TYPE_READONLY_ANYDATA;
        public static final Type TYPE_ANYDATA_ARRAY;
        public static final Type TYPE_DETAIL;
        public static final Type TYPE_ERROR_DETAIL;
        public static final Type TYPE_ERROR;
        public static final Type TYPE_CLONEABLE;

        public static final Type TYPE_JSON_DECIMAL;
        public static final Type TYPE_JSON_FLOAT;

        public static final Type STRING_ITR_NEXT_RETURN_TYPE =
                wrap(IteratorUtils.createIteratorNextReturnType(TYPE_STRING_CHAR));

        public static final Type ANY_AND_READONLY_TYPE =
                wrap(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ANY));
        public static final Type ANY_AND_READONLY_OR_ERROR_TYPE;
        public static final Type BOOLEAN_TRUE = SemTypeUtils.SemTypeBuilder.booleanSubType(true);
        public static final Type BOOLEAN_FALSE = SemTypeUtils.SemTypeBuilder.booleanSubType(false);

        private TypeCache() {
        }

        // type anydata =  ()|boolean|int|float|decimal|string|xml|anydata[]|map<anydata>|table<map<anydata>>
        static {
            ArrayList<Type> members = new ArrayList<>();
            members.add(TYPE_NULL);
            members.add(TYPE_BOOLEAN);
            members.add(TYPE_INT);
            members.add(TYPE_FLOAT);
            members.add(TYPE_DECIMAL);
            members.add(TYPE_STRING);
            members.add(TYPE_XML);
            TYPE_ANYDATA = getAnydataType(members, TypeConstants.ANYDATA_TNAME, false);
            TYPE_READONLY_ANYDATA = getAnydataType(members, TypeConstants.READONLY_ANYDATA_TNAME, true);
            TYPE_ANYDATA_ARRAY = wrap(new BArrayType(TYPE_ANYDATA));
        }

        private static Type getAnydataType(List<Type> members, String typeName, boolean readonly) {
            BSemType anydataType = unionFrom(typeName, EMPTY_MODULE, members);
            anydataType.setBTypeClass(BSubTypeData.BTypeClass.BAnyData);
            anydataType.setReadonly(readonly);
            Type internalAnydataMap = new BMapType(TypeConstants.MAP_TNAME, anydataType, EMPTY_MODULE, readonly);
            Type internalAnydataArray = new BArrayType(anydataType, readonly);
            Type internalAnydataMapTable = new BTableType(internalAnydataMap, readonly);
            anydataType.addCyclicMembers(List.of(internalAnydataArray, internalAnydataMap, internalAnydataMapTable));
            return wrap(anydataType);
        }

        // type json = ()|boolean|int|float|decimal|string|json[]|map<json>
        static {
            ArrayList<Type> members = new ArrayList<>();
            members.add(TYPE_NULL);
            members.add(TYPE_BOOLEAN);
            members.add(TYPE_INT);
            members.add(TYPE_FLOAT);
            members.add(TYPE_DECIMAL);
            members.add(TYPE_STRING);
            BSemType jsonType = unionFrom(TypeConstants.JSON_TNAME, EMPTY_MODULE, members);
            jsonType.setBTypeClass(BSubTypeData.BTypeClass.BJson);
            jsonType.setReadonly(false);
            Type internalJsonMap = new BMapType(TypeConstants.MAP_TNAME, jsonType, EMPTY_MODULE);
            Type internalJsonArray = new BArrayType(jsonType);
            jsonType.addCyclicMembers(List.of(internalJsonArray, internalJsonMap));
            TYPE_JSON = wrap(jsonType);
            TYPE_JSON_ARRAY = wrap(new BArrayType(TYPE_JSON));
//            TYPE_READONLY_JSON = wrap(new BJsonType(unwrap(jsonType), TypeConstants.READONLY_JSON_TNAME, true));
            // FIXME: this is not setting the readonly properly
            BSemType jsonROType = unionFrom(TypeConstants.READONLY_JSON_TNAME, EMPTY_MODULE, members);
            jsonROType.setBTypeClass(BSubTypeData.BTypeClass.BJson);
            jsonROType.setReadonly(true);
            Type internalRoJsonMap = new BMapType(TypeConstants.MAP_TNAME, jsonROType, EMPTY_MODULE, true);
            Type internalRoJsonArray = new BArrayType(jsonROType, true);
            jsonROType.addCyclicMembers(List.of(internalRoJsonArray, internalRoJsonMap));
            TYPE_READONLY_JSON = jsonROType;
        }

        // public type Cloneable readonly|xml|Cloneable[]|map<Cloneable>|table<map<Cloneable>>
        static {
            ArrayList<Type> members = new ArrayList<>();
            members.add(TYPE_XML);
            members.add(TYPE_READONLY);
            var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
            BSemType cloneable = unionFrom(TypeConstants.CLONEABLE_TNAME, valueModule, members);
            MapType internalCloneableMap = new BMapType(TypeConstants.MAP_TNAME, cloneable, valueModule);
            ArrayType internalCloneableArray = new BArrayType(cloneable);
            BTableType internalCloneableMapTable = new BTableType(internalCloneableMap, false);
            cloneable.addCyclicMembers(
                    List.of(internalCloneableMap, internalCloneableArray, internalCloneableMapTable));
            TYPE_CLONEABLE = wrap(cloneable);
            TYPE_DETAIL = wrap(new BMapType(TypeConstants.MAP_TNAME, TYPE_CLONEABLE, EMPTY_MODULE));
            TYPE_ERROR_DETAIL = wrap(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_DETAIL));
            TYPE_ERROR = wrap(new BErrorType(TypeConstants.ERROR, EMPTY_MODULE, TYPE_DETAIL));
            BSemType anyAndReadonlyType = wrap(union(ANY_AND_READONLY_TYPE, TYPE_ERROR));
            anyAndReadonlyType.setReadonly(true);
            ANY_AND_READONLY_OR_ERROR_TYPE = anyAndReadonlyType;
        }

        // public type JsonDecimal ()|boolean|string|decimal|JsonDecimal[]|map<JsonDecimal>;
        static {
            ArrayList<Type> members = new ArrayList<>();
            members.add(TYPE_NULL);
            members.add(TYPE_BOOLEAN);
            members.add(TYPE_STRING);
            members.add(TYPE_DECIMAL);
            var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
            BSemType jsonDecimal = unionFrom(TypeConstants.JSON_DECIMAL_TNAME, valueModule, members);
            MapType internalJsonDecimalMap = new BMapType(TypeConstants.MAP_TNAME, jsonDecimal, valueModule);
            ArrayType internalJsonDecimalArray = new BArrayType(jsonDecimal);
            jsonDecimal.addCyclicMembers(List.of(internalJsonDecimalArray, internalJsonDecimalMap));
            TYPE_JSON_DECIMAL = wrap(jsonDecimal);
        }

        // public type JsonFloat ()|boolean|string|float|JsonFloat[]|map<JsonFloat>;
        static {
            ArrayList<Type> members = new ArrayList<>();
            members.add(TYPE_NULL);
            members.add(TYPE_BOOLEAN);
            members.add(TYPE_STRING);
            members.add(TYPE_FLOAT);
            // FIXME:
            var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
            BUnionType jsonFloat = new BUnionType(TypeConstants.JSON_FLOAT_TNAME, valueModule, members, false);
            jsonFloat.isCyclic = true;
            MapType internalJsonFloatMap = new BMapType(TypeConstants.MAP_TNAME, jsonFloat, valueModule);
            ArrayType internalJsonFloatArray = new BArrayType(jsonFloat);
            jsonFloat.addMembers(internalJsonFloatArray, internalJsonFloatMap);
            TYPE_JSON_FLOAT = wrap(jsonFloat);
        }
    }

    public final static class StringSubtypeData {

        // If true chars are the set of chars included in the set else it's the set of chars excluded from the set
        private boolean includingChars;
        private boolean stringPositive;
        private String[] chars; // Java char can't represent unicode values
        private String[] strings;

        public StringSubtypeData() {
            this.includingChars = false;
            this.stringPositive = false;
            this.chars = null;
            this.strings = null;
        }

        public StringSubtypeData includeStrings(String... values) {
            return setStringState(true, values);
        }

        public StringSubtypeData excludeStrings(String... values) {
            return setStringState(false, values);
        }

        public StringSubtypeData includeChars(String... values) {
            return setCharState(true, values);
        }

        public StringSubtypeData excludeChars(String... values) {
            return setCharState(false, values);
        }

        private StringSubtypeData setStringState(boolean includingStrings, String[] strings) {
            if (this.strings != null) {
                throw new UnsupportedOperationException("Can't include and exclude strings at the same time");
            }
            this.stringPositive = includingStrings;
            this.strings = strings;
            return this;
        }

        private StringSubtypeData setCharState(boolean includingChars, String[] chars) {
            if (this.chars != null) {
                throw new UnsupportedOperationException("Can't include and exclude chars at the same time");
            }
            this.includingChars = includingChars;
            this.chars = chars;
            return this;
        }

        // TODO: this will have a method to get ProperSubtypeData
    }
}
