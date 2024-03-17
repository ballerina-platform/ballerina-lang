/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.api;

import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.IteratorUtils;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BHandleType;
import io.ballerina.runtime.internal.types.BIteratorType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BReadonlyType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlAttributesType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.types.semtype.BSemType;
import io.ballerina.runtime.internal.types.semtype.BSubType;
import io.ballerina.runtime.internal.types.semtype.SemTypeUtils;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.TypeBuilder.toSemType;
import static io.ballerina.runtime.api.TypeBuilder.union;
import static io.ballerina.runtime.api.TypeBuilder.unionFrom;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VALUE_LANG_LIB;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_DECIMAL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_INT;

/**
 * This class contains predefined types used in ballerina runtime.
 *
 * @since 2.0.0
 */
public class PredefinedTypes {

    private static final Module EMPTY_MODULE = new Module(null, null, null);
    public static final Type TYPE_INT = TypeBuilder.intType();
    public static final Type TYPE_INT_SIGNED_8 = TypeBuilder.intSubType(SIGNED8_MIN_VALUE, SIGNED8_MAX_VALUE);
    public static final Type TYPE_INT_SIGNED_16 = TypeBuilder.intSubType(SIGNED16_MIN_VALUE, SIGNED16_MAX_VALUE);
    public static final Type TYPE_INT_SIGNED_32 = TypeBuilder.intSubType(SIGNED32_MIN_VALUE, SIGNED32_MAX_VALUE);
    public static final Type TYPE_INT_UNSIGNED_8 = TypeBuilder.intSubType(0, UNSIGNED8_MAX_VALUE);
    public static final Type TYPE_INT_UNSIGNED_16 = TypeBuilder.intSubType(0, UNSIGNED16_MAX_VALUE);
    public static final Type TYPE_INT_UNSIGNED_32 = TypeBuilder.intSubType(0, UNSIGNED32_MAX_VALUE);

    public static final Type TYPE_BOOLEAN = TypeBuilder.booleanType();
    public static final Type TYPE_BYTE = TypeBuilder.byteType();
    public static final Type TYPE_FLOAT = TypeBuilder.floatType();
    public static final Type TYPE_DECIMAL = TypeBuilder.decimalType();
    public static final Type TYPE_STRING = TypeBuilder.stringType();
    public static final Type TYPE_STRING_CHAR =
            TypeBuilder.stringSubType(new TypeBuilder.StringSubtypeData().includeNonChars().excludeChars());

    public static final Type TYPE_READONLY = toSemType(new BReadonlyType(TypeConstants.READONLY_TNAME, EMPTY_MODULE));
    public static final Type TYPE_ELEMENT = TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_ELEMENT_RW);
    public static final Type TYPE_READONLY_ELEMENT = TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_ELEMENT_RO);

    public static final Type TYPE_PROCESSING_INSTRUCTION =
            TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_PI_RW);
    public static final Type TYPE_READONLY_PROCESSING_INSTRUCTION =
            TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_PI_RO);

    public static final Type TYPE_COMMENT = TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_COMMENT_RW);
    public static final Type TYPE_READONLY_COMMENT = TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_COMMENT_RO);

    public static final Type TYPE_TEXT = TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_TEXT);

    public static final Type TYPE_XML_NEVER = TypeBuilder.xmlSimpleSubType(TypeBuilder.XML_PRIMITIVE_NEVER);

    public static final Type TYPE_XML = TypeBuilder.xmlType();

    public static final Type TYPE_READONLY_XML =
            toSemType(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(toSemType(new BXmlType(
                    TypeConstants.XML_TNAME,
                    union(TYPE_ELEMENT, TYPE_COMMENT, TYPE_PROCESSING_INSTRUCTION, TYPE_TEXT), EMPTY_MODULE))));
    public static final Type TYPE_XML_ELEMENT_SEQUENCE =
            toSemType(new BXmlType(TYPE_ELEMENT, false));
    public static final Type TYPE_XML_COMMENT_SEQUENCE =
            toSemType(new BXmlType(TYPE_COMMENT, false));
    public static final Type TYPE_XML_PI_SEQUENCE =
            toSemType(new BXmlType(TYPE_PROCESSING_INSTRUCTION, false));
    public static final Type TYPE_XML_TEXT_SEQUENCE =
            toSemType(new BXmlType(TYPE_TEXT, false));

    public static final Type TYPE_ANY = toSemType(new BAnyType(TypeConstants.ANY_TNAME, EMPTY_MODULE, false));
    public static final Type TYPE_READONLY_ANY = TypeBuilder.intersect(TYPE_ANY, TYPE_READONLY);
    public static final Type TYPE_TYPEDESC =
            toSemType(new BTypedescType(TypeConstants.TYPEDESC_TNAME, EMPTY_MODULE));
    public static final Type TYPE_MAP =
            toSemType(new BMapType(TypeConstants.MAP_TNAME, TYPE_ANY, EMPTY_MODULE));
    public static final Type TYPE_FUTURE = toSemType(new BFutureType(TypeConstants.FUTURE_TNAME, EMPTY_MODULE));
    public static final Type TYPE_NULL = TypeBuilder.nilType();
    public static final Type TYPE_NEVER = TypeBuilder.neverType();
    public static final Type TYPE_XML_ATTRIBUTES =
            toSemType(new BXmlAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME, EMPTY_MODULE));
    public static final Type TYPE_ITERATOR =
            toSemType(new BIteratorType(TypeConstants.ITERATOR_TNAME, EMPTY_MODULE));
    public static final Type TYPE_ANY_SERVICE = toSemType(new BServiceType(TypeConstants.SERVICE, EMPTY_MODULE, 0));
    public static final Type TYPE_HANDLE = toSemType(new BHandleType(TypeConstants.HANDLE_TNAME, EMPTY_MODULE));
    public static final Type TYPE_STREAM = TypeBuilder.streamType();

    public static final Type TYPE_JSON;
    public static final Type TYPE_READONLY_JSON;
    public static final Type TYPE_JSON_ARRAY;

    // type json = ()|boolean|int|float|decimal|string|json[]|map<json>
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NIL));
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_BOOLEAN));
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_INT));
        members.add(TYPE_FLOAT);
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_DECIMAL));
        members.add(TYPE_STRING);
        BSemType jsonType = unionFrom(TypeConstants.JSON_TNAME, EMPTY_MODULE, members);
        jsonType.setBTypeClass(BSubType.BTypeClass.BJson);
        jsonType.setReadonly(false);
        Type internalJsonMap = new BMapType(TypeConstants.MAP_TNAME, jsonType, EMPTY_MODULE);
        Type internalJsonArray = new BArrayType(jsonType);
        jsonType.addCyclicMembers(List.of(internalJsonArray, internalJsonMap));
        TYPE_JSON = toSemType(jsonType);
        TYPE_JSON_ARRAY = toSemType(new BArrayType(TYPE_JSON));
        BSemType jsonROType = unionFrom(TypeConstants.READONLY_JSON_TNAME, EMPTY_MODULE, members);
        jsonROType.setBTypeClass(BSubType.BTypeClass.BJson);
        jsonROType.setReadonly(true);
        Type internalRoJsonMap = new BMapType(TypeConstants.MAP_TNAME, jsonROType, EMPTY_MODULE, true);
        Type internalRoJsonArray = new BArrayType(jsonROType, true);
        jsonROType.addCyclicMembers(List.of(internalRoJsonArray, internalRoJsonMap));
        TYPE_READONLY_JSON = jsonROType;
    }

    public static final Type TYPE_ANYDATA;
    public static final Type TYPE_READONLY_ANYDATA;
    public static final Type TYPE_ANYDATA_ARRAY;

    // type anydata =  ()|boolean|int|float|decimal|string|xml|anydata[]|map<anydata>|table<map<anydata>>
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NIL));
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_BOOLEAN));
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_INT));
        members.add(TYPE_FLOAT);
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_DECIMAL));
        members.add(TYPE_STRING);
        members.add(TYPE_XML);
        TYPE_ANYDATA = getAnydataType(members, TypeConstants.ANYDATA_TNAME, false);
        TYPE_READONLY_ANYDATA = getAnydataType(members, TypeConstants.READONLY_ANYDATA_TNAME, true);
        TYPE_ANYDATA_ARRAY = toSemType(new BArrayType(TYPE_ANYDATA));
    }

    private static Type getAnydataType(List<Type> members, String typeName, boolean readonly) {
        BSemType anydataType = unionFrom(typeName, EMPTY_MODULE, members);
        anydataType.setBTypeClass(BSubType.BTypeClass.BAnyData);
        anydataType.setReadonly(readonly);
        Type internalAnydataMap = new BMapType(TypeConstants.MAP_TNAME, anydataType, EMPTY_MODULE, readonly);
        Type internalAnydataArray = new BArrayType(anydataType, readonly);
        Type internalAnydataMapTable = new BTableType(internalAnydataMap, readonly);
        anydataType.addCyclicMembers(List.of(internalAnydataArray, internalAnydataMap, internalAnydataMapTable));
        return toSemType(anydataType);
    }

    public static final Type TYPE_DETAIL;
    public static final Type TYPE_ERROR_DETAIL;
    public static final Type TYPE_ERROR;
    public static final Type TYPE_CLONEABLE;

    public static final Type TYPE_JSON_DECIMAL;
    public static final Type TYPE_JSON_FLOAT;

    public static final Type STRING_ITR_NEXT_RETURN_TYPE =
            toSemType(IteratorUtils.createIteratorNextReturnType(TYPE_STRING_CHAR));

    public static final Type ANY_AND_READONLY_TYPE =
            toSemType(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ANY));
    public static final Type ANY_AND_READONLY_OR_ERROR_TYPE;

    private PredefinedTypes() {}

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
        TYPE_CLONEABLE = toSemType(cloneable);
        TYPE_DETAIL = toSemType(new BMapType(TypeConstants.MAP_TNAME, TYPE_CLONEABLE, EMPTY_MODULE));
        TYPE_ERROR_DETAIL = toSemType(ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_DETAIL));
        TYPE_ERROR = toSemType(new BErrorType(TypeConstants.ERROR, EMPTY_MODULE, TYPE_DETAIL));
        BSemType anyAndReadonlyType = toSemType(union(ANY_AND_READONLY_TYPE, TYPE_ERROR));
        anyAndReadonlyType.setReadonly(true);
        ANY_AND_READONLY_OR_ERROR_TYPE = anyAndReadonlyType;
    }

    // public type JsonDecimal ()|boolean|string|decimal|JsonDecimal[]|map<JsonDecimal>;
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NIL));
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_BOOLEAN));
        members.add(TYPE_STRING);
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_DECIMAL));
        var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
        BSemType jsonDecimal = unionFrom(TypeConstants.JSON_DECIMAL_TNAME, valueModule, members);
        MapType internalJsonDecimalMap = new BMapType(TypeConstants.MAP_TNAME, jsonDecimal, valueModule);
        ArrayType internalJsonDecimalArray = new BArrayType(jsonDecimal);
        jsonDecimal.addCyclicMembers(List.of(internalJsonDecimalArray, internalJsonDecimalMap));
        TYPE_JSON_DECIMAL = toSemType(jsonDecimal);
    }

    // public type JsonFloat ()|boolean|string|float|JsonFloat[]|map<JsonFloat>;
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(SemTypeUtils.SemTypeBuilder.from(SemTypeUtils.BasicTypeCodes.BT_NIL));
        members.add(SemTypeUtils.SemTypeBuilder.from(BT_BOOLEAN));
        members.add(TYPE_STRING);
        members.add(TYPE_FLOAT);
        var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
        BUnionType jsonFloat = new BUnionType(TypeConstants.JSON_FLOAT_TNAME, valueModule, members, false);
        jsonFloat.isCyclic = true;
        MapType internalJsonFloatMap = new BMapType(TypeConstants.MAP_TNAME, jsonFloat, valueModule);
        ArrayType internalJsonFloatArray = new BArrayType(jsonFloat);
        jsonFloat.addMembers(internalJsonFloatArray, internalJsonFloatMap);
        TYPE_JSON_FLOAT = toSemType(jsonFloat);
    }
}
