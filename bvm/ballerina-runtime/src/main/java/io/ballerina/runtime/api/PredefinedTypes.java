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
import io.ballerina.runtime.api.types.AnyType;
import io.ballerina.runtime.api.types.AnydataType;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.BooleanType;
import io.ballerina.runtime.api.types.ByteType;
import io.ballerina.runtime.api.types.DecimalType;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.FloatType;
import io.ballerina.runtime.api.types.FutureType;
import io.ballerina.runtime.api.types.HandleType;
import io.ballerina.runtime.api.types.IntegerType;
import io.ballerina.runtime.api.types.IteratorType;
import io.ballerina.runtime.api.types.JsonType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.NeverType;
import io.ballerina.runtime.api.types.NullType;
import io.ballerina.runtime.api.types.ReadonlyType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.StringType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.types.XmlAttributesType;
import io.ballerina.runtime.api.types.XmlType;
import io.ballerina.runtime.internal.IteratorUtils;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BBooleanType;
import io.ballerina.runtime.internal.types.BByteType;
import io.ballerina.runtime.internal.types.BDecimalType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BFloatType;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BHandleType;
import io.ballerina.runtime.internal.types.BIntegerType;
import io.ballerina.runtime.internal.types.BIteratorType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BNeverType;
import io.ballerina.runtime.internal.types.BNullType;
import io.ballerina.runtime.internal.types.BReadonlyType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BStringType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlAttributesType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VALUE_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.XML_LANG_LIB;

/**
 * This class contains predefined types used in ballerina runtime.
 *
 * @since 2.0.0
 */
public class PredefinedTypes {

    private static final Module EMPTY_MODULE = new Module(null, null, null);

    public static final IntegerType TYPE_INT = new BIntegerType(TypeConstants.INT_TNAME, EMPTY_MODULE);
    public static final IntegerType TYPE_INT_SIGNED_8 =
            new BIntegerType(TypeConstants.SIGNED8, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.SIGNED8_INT_TAG);
    public static final IntegerType TYPE_INT_SIGNED_16 =
            new BIntegerType(TypeConstants.SIGNED16, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.SIGNED16_INT_TAG);
    public static final IntegerType TYPE_INT_SIGNED_32 =
            new BIntegerType(TypeConstants.SIGNED32, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.SIGNED32_INT_TAG);
    public static final IntegerType TYPE_INT_UNSIGNED_8 =
            new BIntegerType(TypeConstants.UNSIGNED8, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.UNSIGNED8_INT_TAG);
    public static final IntegerType TYPE_INT_UNSIGNED_16 =
            new BIntegerType(TypeConstants.UNSIGNED16, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.UNSIGNED16_INT_TAG);
    public static final IntegerType TYPE_INT_UNSIGNED_32 =
            new BIntegerType(TypeConstants.UNSIGNED32, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.UNSIGNED32_INT_TAG);
    public static final BooleanType TYPE_BOOLEAN = new BBooleanType(TypeConstants.BOOLEAN_TNAME, EMPTY_MODULE);
    public static final ByteType TYPE_BYTE = new BByteType(TypeConstants.BYTE_TNAME, EMPTY_MODULE);
    public static final FloatType TYPE_FLOAT = new BFloatType(TypeConstants.FLOAT_TNAME, EMPTY_MODULE);
    public static final DecimalType TYPE_DECIMAL = new BDecimalType(TypeConstants.DECIMAL_TNAME, EMPTY_MODULE);
    public static final StringType TYPE_STRING = new BStringType(TypeConstants.STRING_TNAME, EMPTY_MODULE);
    public static final StringType TYPE_STRING_CHAR = new BStringType(TypeConstants.CHAR,
                                                                      new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                                 STRING_LANG_LIB, null),
                                                                      TypeTags.CHAR_STRING_TAG);

    public static final ReadonlyType TYPE_READONLY = new BReadonlyType(TypeConstants.READONLY_TNAME, EMPTY_MODULE);
    public static final XmlType TYPE_ELEMENT = new BXmlType(TypeConstants.XML_ELEMENT,
                                                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                       null), TypeTags.XML_ELEMENT_TAG, false);
    public static final Type TYPE_READONLY_ELEMENT = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ELEMENT);

    public static final XmlType TYPE_PROCESSING_INSTRUCTION = new BXmlType(TypeConstants.XML_PI,
                                                                           new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                                      XML_LANG_LIB, null),
                                                                           TypeTags.XML_PI_TAG, false);
    public static final Type TYPE_READONLY_PROCESSING_INSTRUCTION =
            ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_PROCESSING_INSTRUCTION);

    public static final XmlType TYPE_COMMENT = new BXmlType(TypeConstants.XML_COMMENT,
                                                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                       null), TypeTags.XML_COMMENT_TAG, false);
    public static final Type TYPE_READONLY_COMMENT = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_COMMENT);

    public static final XmlType TYPE_TEXT = new BXmlType(TypeConstants.XML_TEXT,
                                                         new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                                                         TypeTags.XML_TEXT_TAG, true);

    public static final Type TYPE_XML_NEVER = new BXmlType(TypeConstants.XML_TNAME, new BNeverType(EMPTY_MODULE),
                                              EMPTY_MODULE, true);

    public static final Type TYPE_XML = new BXmlType(TypeConstants.XML_TNAME,
                                                     new BUnionType(Arrays.asList(TYPE_ELEMENT, TYPE_COMMENT,
                                                                                  TYPE_PROCESSING_INSTRUCTION,
                                                                                  TYPE_TEXT)), EMPTY_MODULE);

    public static final Type TYPE_READONLY_XML = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_XML);
    public static final Type TYPE_XML_ELEMENT_SEQUENCE = new BXmlType(PredefinedTypes.TYPE_ELEMENT, false);
    public static final Type TYPE_XML_COMMENT_SEQUENCE = new BXmlType(PredefinedTypes.TYPE_COMMENT, false);
    public static final Type TYPE_XML_PI_SEQUENCE = new BXmlType(PredefinedTypes.TYPE_PROCESSING_INSTRUCTION, false);
    public static final Type TYPE_XML_TEXT_SEQUENCE = new BXmlType(PredefinedTypes.TYPE_TEXT, false);

    public static final AnyType TYPE_ANY = new BAnyType(TypeConstants.ANY_TNAME, EMPTY_MODULE, false);
    public static final AnyType TYPE_READONLY_ANY = new BAnyType(TypeConstants.READONLY_ANY_TNAME, EMPTY_MODULE, true);
    public static final TypedescType TYPE_TYPEDESC = new BTypedescType(TypeConstants.TYPEDESC_TNAME, EMPTY_MODULE);
    public static final MapType TYPE_MAP = new BMapType(TypeConstants.MAP_TNAME, TYPE_ANY, EMPTY_MODULE);
    public static final FutureType TYPE_FUTURE = new BFutureType(TypeConstants.FUTURE_TNAME, EMPTY_MODULE);
    public static final NullType TYPE_NULL = new BNullType(TypeConstants.NULL_TNAME, EMPTY_MODULE);
    public static final NeverType TYPE_NEVER = new BNeverType(EMPTY_MODULE);
    public static final XmlAttributesType TYPE_XML_ATTRIBUTES =
            new BXmlAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME, EMPTY_MODULE);
    public static final IteratorType TYPE_ITERATOR = new BIteratorType(TypeConstants.ITERATOR_TNAME, EMPTY_MODULE);
    public static final ServiceType TYPE_ANY_SERVICE = new BServiceType(TypeConstants.SERVICE, EMPTY_MODULE, 0);
    public static final HandleType TYPE_HANDLE = new BHandleType(TypeConstants.HANDLE_TNAME, EMPTY_MODULE);
    public static final StreamType TYPE_STREAM = new BStreamType(TypeConstants.STREAM_TNAME, TYPE_ANY, TYPE_NULL,
            EMPTY_MODULE);
    public static final ArrayType TYPE_ANY_ARRAY = new BArrayType(TYPE_ANY);

    public static final JsonType TYPE_JSON;
    public static final JsonType TYPE_READONLY_JSON;
    public static final ArrayType TYPE_JSON_ARRAY;
    public static final AnydataType TYPE_ANYDATA;
    public static final AnydataType TYPE_READONLY_ANYDATA;
    public static final ArrayType TYPE_ANYDATA_ARRAY;
    public static final MapType TYPE_DETAIL;
    public static final Type TYPE_ERROR_DETAIL;
    public static final ErrorType TYPE_ERROR;
    public static final UnionType TYPE_CLONEABLE;

    public static final UnionType TYPE_JSON_DECIMAL;
    public static final UnionType TYPE_JSON_FLOAT;

    public static final RecordType STRING_ITR_NEXT_RETURN_TYPE =
            IteratorUtils.createIteratorNextReturnType(PredefinedTypes.TYPE_STRING_CHAR);

    public static final Type ANY_AND_READONLY_TYPE = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ANY);
    public static final Type ANY_AND_READONLY_OR_ERROR_TYPE;

    private PredefinedTypes() {}

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
        TYPE_ANYDATA_ARRAY = new BArrayType(TYPE_ANYDATA);
    }

    private static BAnydataType getAnydataType(List<Type> members, String typeName, boolean readonly) {
        BAnydataType anydataType = new BAnydataType(new BUnionType(TypeConstants.ANYDATA_TNAME, EMPTY_MODULE, members
                , readonly), typeName, readonly);
        anydataType.isCyclic = true;
        MapType internalAnydataMap = new BMapType(TypeConstants.MAP_TNAME, anydataType, EMPTY_MODULE, readonly);
        ArrayType internalAnydataArray = new BArrayType(anydataType, readonly);
        BTableType internalAnydataMapTable = new BTableType(internalAnydataMap, readonly);
        anydataType.addMembers(internalAnydataArray, internalAnydataMap, internalAnydataMapTable);
        return anydataType;
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
        BJsonType jsonType = new BJsonType(new BUnionType(TypeConstants.JSON_TNAME, EMPTY_MODULE, members, false),
                TypeConstants.JSON_TNAME, false);
        jsonType.isCyclic = true;
        MapType internalJsonMap = new BMapType(TypeConstants.MAP_TNAME, jsonType, EMPTY_MODULE);
        ArrayType internalJsonArray = new BArrayType(jsonType);
        jsonType.addMembers(internalJsonArray, internalJsonMap);
        TYPE_JSON = jsonType;
        TYPE_JSON_ARRAY = new BArrayType(TYPE_JSON);
        TYPE_READONLY_JSON = new BJsonType(jsonType, TypeConstants.READONLY_JSON_TNAME, true);
    }

    // public type Cloneable readonly|xml|Cloneable[]|map<Cloneable>|table<map<Cloneable>>
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(TYPE_XML);
        members.add(TYPE_READONLY);
        var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
        BUnionType cloneable = new BUnionType(TypeConstants.CLONEABLE_TNAME, valueModule, members, false);
        cloneable.isCyclic = true;
        MapType internalCloneableMap = new BMapType(TypeConstants.MAP_TNAME, cloneable, valueModule);
        ArrayType internalCloneableArray = new BArrayType(cloneable);
        BTableType internalCloneableMapTable = new BTableType(internalCloneableMap, false);
        cloneable.addMembers(internalCloneableMap, internalCloneableArray, internalCloneableMapTable);
        TYPE_CLONEABLE = cloneable;
        TYPE_DETAIL = new BMapType(TypeConstants.MAP_TNAME, TYPE_CLONEABLE, EMPTY_MODULE);
        TYPE_ERROR_DETAIL = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_DETAIL);
        TYPE_ERROR = new BErrorType(TypeConstants.ERROR, EMPTY_MODULE, TYPE_DETAIL);
        ANY_AND_READONLY_OR_ERROR_TYPE = new BUnionType(Arrays.asList(ANY_AND_READONLY_TYPE, TYPE_ERROR));
    }

    // public type JsonDecimal ()|boolean|string|decimal|JsonDecimal[]|map<JsonDecimal>;
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(TYPE_NULL);
        members.add(TYPE_BOOLEAN);
        members.add(TYPE_STRING);
        members.add(TYPE_DECIMAL);
        var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
        BUnionType jsonDecimal = new BUnionType(TypeConstants.JSON_DECIMAL_TNAME, valueModule, members, false);
        jsonDecimal.isCyclic = true;
        MapType internalJsonDecimalMap = new BMapType(TypeConstants.MAP_TNAME, jsonDecimal, valueModule);
        ArrayType internalJsonDecimalArray = new BArrayType(jsonDecimal);
        jsonDecimal.addMembers(internalJsonDecimalArray, internalJsonDecimalMap);
        TYPE_JSON_DECIMAL = jsonDecimal;
    }

    // public type JsonFloat ()|boolean|string|float|JsonFloat[]|map<JsonFloat>;
    static {
        ArrayList<Type> members = new ArrayList<>();
        members.add(TYPE_NULL);
        members.add(TYPE_BOOLEAN);
        members.add(TYPE_STRING);
        members.add(TYPE_FLOAT);
        var valueModule = new Module(BALLERINA_BUILTIN_PKG_PREFIX, VALUE_LANG_LIB, null);
        BUnionType jsonFloat = new BUnionType(TypeConstants.JSON_FLOAT_TNAME, valueModule, members, false);
        jsonFloat.isCyclic = true;
        MapType internalJsonFloatMap = new BMapType(TypeConstants.MAP_TNAME, jsonFloat, valueModule);
        ArrayType internalJsonFloatArray = new BArrayType(jsonFloat);
        jsonFloat.addMembers(internalJsonFloatArray, internalJsonFloatMap);
        TYPE_JSON_FLOAT = jsonFloat;
    }
}
