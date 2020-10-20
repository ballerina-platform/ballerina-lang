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

import io.ballerina.runtime.IteratorUtils;
import io.ballerina.runtime.TypeChecker;
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
import io.ballerina.runtime.api.types.JSONType;
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
import io.ballerina.runtime.api.types.XMLAttributesType;
import io.ballerina.runtime.api.types.XMLType;
import io.ballerina.runtime.types.BAnyType;
import io.ballerina.runtime.types.BAnydataType;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BBooleanType;
import io.ballerina.runtime.types.BByteType;
import io.ballerina.runtime.types.BDecimalType;
import io.ballerina.runtime.types.BErrorType;
import io.ballerina.runtime.types.BFiniteType;
import io.ballerina.runtime.types.BFloatType;
import io.ballerina.runtime.types.BFutureType;
import io.ballerina.runtime.types.BHandleType;
import io.ballerina.runtime.types.BIntegerType;
import io.ballerina.runtime.types.BIteratorType;
import io.ballerina.runtime.types.BJSONType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BNeverType;
import io.ballerina.runtime.types.BNullType;
import io.ballerina.runtime.types.BReadonlyType;
import io.ballerina.runtime.types.BServiceType;
import io.ballerina.runtime.types.BStreamType;
import io.ballerina.runtime.types.BStringType;
import io.ballerina.runtime.types.BType;
import io.ballerina.runtime.types.BTypedescType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.types.BXMLAttributesType;
import io.ballerina.runtime.types.BXMLType;
import io.ballerina.runtime.values.ReadOnlyUtils;

import java.util.Arrays;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.INT_LANG_LIB;
import static io.ballerina.runtime.util.BLangConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.util.BLangConstants.XML_LANG_LIB;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
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
    public static final XMLType TYPE_ELEMENT = new BXMLType(TypeConstants.XML_ELEMENT,
                                                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                       null), TypeTags.XML_ELEMENT_TAG, false);
    public static final Type TYPE_READONLY_ELEMENT = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ELEMENT);

    public static final XMLType TYPE_PROCESSING_INSTRUCTION = new BXMLType(TypeConstants.XML_PI,
                                                                           new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                                      XML_LANG_LIB, null),
                                                                           TypeTags.XML_PI_TAG, false);
    public static final Type TYPE_READONLY_PROCESSING_INSTRUCTION =
            ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_PROCESSING_INSTRUCTION);

    public static final XMLType TYPE_COMMENT = new BXMLType(TypeConstants.XML_COMMENT,
                                                            new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                       null), TypeTags.XML_COMMENT_TAG, false);
    public static final Type TYPE_READONLY_COMMENT = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_COMMENT);

    public static final XMLType TYPE_TEXT = new BXMLType(TypeConstants.XML_TEXT,
                                                         new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                                                         TypeTags.XML_TEXT_TAG, true);

    public static final Type TYPE_XML = new BXMLType(TypeConstants.XML_TNAME,
                                                     new BUnionType(Arrays.asList(TYPE_ELEMENT, TYPE_COMMENT,
                                                                                  TYPE_PROCESSING_INSTRUCTION,
                                                                                  TYPE_TEXT)), EMPTY_MODULE);
    public static final JSONType TYPE_JSON = new BJSONType(TypeConstants.JSON_TNAME, EMPTY_MODULE, false);
    public static final ArrayType TYPE_JSON_ARRAY = new BArrayType(TYPE_JSON);
    public static final JSONType TYPE_READONLY_JSON = new BJSONType(TypeConstants.READONLY_JSON_TNAME, EMPTY_MODULE,
                                                                    true);
    public static final AnyType TYPE_ANY = new BAnyType(TypeConstants.ANY_TNAME, EMPTY_MODULE, false);
    public static final AnyType TYPE_READONLY_ANY = new BAnyType(TypeConstants.READONLY_ANY_TNAME, EMPTY_MODULE, true);
    public static final AnydataType TYPE_ANYDATA =
            new BAnydataType(TypeConstants.ANYDATA_TNAME, EMPTY_MODULE, false);
    public static final AnydataType TYPE_READONLY_ANYDATA = new BAnydataType(TypeConstants.READONLY_ANYDATA_TNAME,
                                                                             EMPTY_MODULE, true);
    public static final StreamType TYPE_STREAM = new BStreamType(TypeConstants.STREAM_TNAME, TYPE_ANY, EMPTY_MODULE);
    public static final TypedescType TYPE_TYPEDESC = new BTypedescType(TypeConstants.TYPEDESC_TNAME, EMPTY_MODULE);
    public static final MapType TYPE_MAP = new BMapType(TypeConstants.MAP_TNAME, TYPE_ANY, EMPTY_MODULE);
    public static final FutureType TYPE_FUTURE = new BFutureType(TypeConstants.FUTURE_TNAME, EMPTY_MODULE);
    public static final NullType TYPE_NULL = new BNullType(TypeConstants.NULL_TNAME, EMPTY_MODULE);
    public static final NeverType TYPE_NEVER = new BNeverType(EMPTY_MODULE);
    public static final XMLAttributesType TYPE_XML_ATTRIBUTES =
            new BXMLAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME, EMPTY_MODULE);
    public static final IteratorType TYPE_ITERATOR = new BIteratorType(TypeConstants.ITERATOR_TNAME, EMPTY_MODULE);
    public static final ServiceType TYPE_ANY_SERVICE = new BServiceType(TypeConstants.SERVICE, EMPTY_MODULE, 0);
    public static final HandleType TYPE_HANDLE = new BHandleType(TypeConstants.HANDLE_TNAME, EMPTY_MODULE);
    public static final UnionType ANYDATA_OR_READONLY = new BUnionType(Arrays.asList(TYPE_ANYDATA, TYPE_READONLY));
    public static final MapType TYPE_ERROR_DETAIL = new BMapType(TypeConstants.MAP_TNAME, ANYDATA_OR_READONLY,
                                                                 EMPTY_MODULE);
    public static final ErrorType TYPE_ERROR = new BErrorType(TypeConstants.ERROR, EMPTY_MODULE, TYPE_ERROR_DETAIL);

    public static final RecordType STRING_ITR_NEXT_RETURN_TYPE =
            IteratorUtils.createIteratorNextReturnType(PredefinedTypes.TYPE_STRING);
    public static final RecordType XML_ITR_NEXT_RETURN_TYPE = IteratorUtils
            .createIteratorNextReturnType(
                    new BUnionType(Arrays.asList(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_XML)));

    private PredefinedTypes() {
    }

    public static boolean isValueType(Type type) {
        if (type == PredefinedTypes.TYPE_INT || type == PredefinedTypes.TYPE_BYTE ||
                type == PredefinedTypes.TYPE_FLOAT ||
                type == PredefinedTypes.TYPE_DECIMAL || type == PredefinedTypes.TYPE_STRING ||
                type == PredefinedTypes.TYPE_BOOLEAN) {
            return true;
        }

        if (type != null && type.getTag() == TypeTags.FINITE_TYPE_TAG) {
            // All the types in value space should be value types.
            for (Object value : ((BFiniteType) type).valueSpace) {
                if (!isValueType(TypeChecker.getType(value))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Type getTypeFromName(String typeName) {
        switch (typeName) {
            case TypeConstants.INT_TNAME:
                return TYPE_INT;
            case TypeConstants.BYTE_TNAME:
                return TYPE_BYTE;
            case TypeConstants.FLOAT_TNAME:
                return TYPE_FLOAT;
            case TypeConstants.DECIMAL_TNAME:
                return TYPE_DECIMAL;
            case TypeConstants.STRING_TNAME:
                return TYPE_STRING;
            case TypeConstants.BOOLEAN_TNAME:
                return TYPE_BOOLEAN;
            case TypeConstants.JSON_TNAME:
                return TYPE_JSON;
            case TypeConstants.XML_TNAME:
                return TYPE_XML;
            case TypeConstants.MAP_TNAME:
                return TYPE_MAP;
            case TypeConstants.FUTURE_TNAME:
                return TYPE_FUTURE;
            case TypeConstants.STREAM_TNAME:
                return TYPE_STREAM;
            // case TypeConstants.CHANNEL:
            // return typeChannel;
            case TypeConstants.ANY_TNAME:
                return TYPE_ANY;
            case TypeConstants.TYPEDESC_TNAME:
                return TYPE_TYPEDESC;
            case TypeConstants.NULL_TNAME:
                return TYPE_NULL;
            case TypeConstants.XML_ATTRIBUTES_TNAME:
                return TYPE_XML_ATTRIBUTES;
            case TypeConstants.ERROR:
                return TYPE_ERROR;
            case TypeConstants.ANYDATA_TNAME:
                return TYPE_ANYDATA;
            default:
                throw new IllegalStateException("Unknown type name");
        }
    }

    public static Type fromString(String typeName) {
        if (typeName.endsWith("[]")) {
            String elementTypeName = typeName.substring(0, typeName.length() - 2);
            Type elemType = fromString(elementTypeName);
            return new BArrayType(elemType);
        }
        return getTypeFromName(typeName);
    }

}
