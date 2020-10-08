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
package io.ballerina.jvm.api;

import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.TypeChecker;
import io.ballerina.jvm.api.runtime.Module;
import io.ballerina.jvm.api.types.ErrorType;
import io.ballerina.jvm.api.types.MapType;
import io.ballerina.jvm.api.types.RecordType;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.types.BAnyType;
import io.ballerina.jvm.types.BAnydataType;
import io.ballerina.jvm.types.BArrayType;
import io.ballerina.jvm.types.BBooleanType;
import io.ballerina.jvm.types.BByteType;
import io.ballerina.jvm.types.BDecimalType;
import io.ballerina.jvm.types.BErrorType;
import io.ballerina.jvm.types.BFiniteType;
import io.ballerina.jvm.types.BFloatType;
import io.ballerina.jvm.types.BFutureType;
import io.ballerina.jvm.types.BHandleType;
import io.ballerina.jvm.types.BIntegerType;
import io.ballerina.jvm.types.BIteratorType;
import io.ballerina.jvm.types.BJSONType;
import io.ballerina.jvm.types.BMapType;
import io.ballerina.jvm.types.BNeverType;
import io.ballerina.jvm.types.BNullType;
import io.ballerina.jvm.types.BReadonlyType;
import io.ballerina.jvm.types.BServiceType;
import io.ballerina.jvm.types.BStreamType;
import io.ballerina.jvm.types.BStringType;
import io.ballerina.jvm.types.BType;
import io.ballerina.jvm.types.BTypedescType;
import io.ballerina.jvm.types.BUnionType;
import io.ballerina.jvm.types.BXMLAttributesType;
import io.ballerina.jvm.types.BXMLType;
import io.ballerina.jvm.values.ReadOnlyUtils;

import java.util.Arrays;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.INT_LANG_LIB;
import static org.ballerinalang.jvm.util.BLangConstants.STRING_LANG_LIB;
import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
 *
 * @since 2.0.0
 */
public class Types {

    public static final Type TYPE_INT
            = new BIntegerType(TypeConstants.INT_TNAME, new Module(null, null, null));
    public static final Type TYPE_INT_SIGNED_8 =
            new BIntegerType(TypeConstants.SIGNED8, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.SIGNED8_INT_TAG);
    public static final Type TYPE_INT_SIGNED_16 =
            new BIntegerType(TypeConstants.SIGNED16, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.SIGNED16_INT_TAG);
    public static final Type TYPE_INT_SIGNED_32 =
            new BIntegerType(TypeConstants.SIGNED32, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.SIGNED32_INT_TAG);
    public static final Type TYPE_INT_UNSIGNED_8 =
            new BIntegerType(TypeConstants.UNSIGNED8, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.UNSIGNED8_INT_TAG);
    public static final Type TYPE_INT_UNSIGNED_16 =
            new BIntegerType(TypeConstants.UNSIGNED16, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null),
                             TypeTags.UNSIGNED16_INT_TAG);
    public static final Type TYPE_INT_UNSIGNED_32 =
            new BIntegerType(TypeConstants.UNSIGNED32, new Module(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null)
                    , TypeTags.UNSIGNED32_INT_TAG);

    public static final Type TYPE_READONLY =
            new BReadonlyType(TypeConstants.READONLY_TNAME, new Module(null, null, null));
    public static final Type TYPE_ELEMENT =
            new BXMLType(TypeConstants.XML_ELEMENT, new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                         TypeTags.XML_ELEMENT_TAG, false);
    public static final Type TYPE_READONLY_ELEMENT = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_ELEMENT);

    public static final Type TYPE_PROCESSING_INSTRUCTION =
            new BXMLType(TypeConstants.XML_PI, new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                         TypeTags.XML_PI_TAG, false);
    public static final Type TYPE_READONLY_PROCESSING_INSTRUCTION =
            ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_PROCESSING_INSTRUCTION);

    public static final Type TYPE_COMMENT = new BXMLType(TypeConstants.XML_COMMENT,
                                                         new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                    null), TypeTags.XML_COMMENT_TAG, false);
    public static final Type TYPE_READONLY_COMMENT = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(TYPE_COMMENT);

    public static final Type TYPE_TEXT = new BXMLType(TypeConstants.XML_TEXT,
                                                      new Module(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                                                      TypeTags.XML_TEXT_TAG, true);

    public static final Type TYPE_BYTE = new BByteType(TypeConstants.BYTE_TNAME, new Module(null, null, null));
    public static final Type TYPE_FLOAT = new BFloatType(TypeConstants.FLOAT_TNAME, new Module(null, null, null));
    public static final Type TYPE_DECIMAL =
            new BDecimalType(TypeConstants.DECIMAL_TNAME, new Module(null, null, null));
    public static final Type TYPE_STRING = new BStringType(TypeConstants.STRING_TNAME, new Module(null, null, null));
    public static final Type TYPE_STRING_CHAR = new BStringType(TypeConstants.CHAR,
                                                                new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                          STRING_LANG_LIB, null),
                                                                TypeTags.CHAR_STRING_TAG);
    public static final Type TYPE_BOOLEAN =
            new BBooleanType(TypeConstants.BOOLEAN_TNAME, new Module(null, null, null));
    public static final Type TYPE_XML = new BXMLType(TypeConstants.XML_TNAME,
                                                     new BUnionType(Arrays.asList(TYPE_ELEMENT, TYPE_COMMENT,
                                                                                  TYPE_PROCESSING_INSTRUCTION,
                                                                                  TYPE_TEXT)),
                                                     new Module(null, null, null));
    public static final Type TYPE_JSON = new BJSONType(TypeConstants.JSON_TNAME, new Module(null, null, null), false);
    public static final Type TYPE_JSON_ARRAY = new BArrayType(TYPE_JSON);
    public static final Type TYPE_READONLY_JSON = new BJSONType(TypeConstants.READONLY_JSON_TNAME,
                                                                new Module(null, null, null), true);
    public static final Type TYPE_ANY = new BAnyType(TypeConstants.ANY_TNAME, new Module(null, null, null), false);
    public static final Type TYPE_READONLY_ANY = new BAnyType(TypeConstants.READONLY_ANY_TNAME,
                                                              new Module(null, null, null), true);
    public static final Type TYPE_ANYDATA =
            new BAnydataType(TypeConstants.ANYDATA_TNAME, new Module(null, null, null), false);
    public static final Type TYPE_READONLY_ANYDATA = new BAnydataType(TypeConstants.READONLY_ANYDATA_TNAME,
                                                                      new Module(null, null, null), true);
    public static final Type TYPE_STREAM = new BStreamType(TypeConstants.STREAM_TNAME, TYPE_ANY,
                                                           new Module(null, null, null));
    public static final Type TYPE_TYPEDESC = new BTypedescType(TypeConstants.TYPEDESC_TNAME,
                                                               new Module(null, null, null));
    public static final Type TYPE_MAP = new BMapType(TypeConstants.MAP_TNAME, TYPE_ANY, new Module(null, null, null));
    public static final Type TYPE_FUTURE = new BFutureType(TypeConstants.FUTURE_TNAME, new Module(null, null, null));
    public static final Type TYPE_NULL = new BNullType(TypeConstants.NULL_TNAME, new Module(null, null, null));
    public static final Type TYPE_NEVER = new BNeverType(new Module(null, null, null));
    public static final Type TYPE_XML_ATTRIBUTES = new BXMLAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME,
                                                                          new Module(null, null, null));
    public static final Type TYPE_ITERATOR = new BIteratorType(TypeConstants.ITERATOR_TNAME,
                                                               new Module(null, null, null));
    // public static final Type typeChannel = new BChannelType(TypeConstants.CHANNEL, null);
    public static final Type TYPE_ANY_SERVICE =
            new BServiceType(TypeConstants.SERVICE, new Module(null, null, null), 0);
    public static final Type TYPE_HANDLE = new BHandleType(TypeConstants.HANDLE_TNAME, new Module(null, null, null));
    public static final Type ANYDATA_OR_READONLY = new BUnionType(Arrays.asList(TYPE_ANYDATA, TYPE_READONLY));
    public static final MapType TYPE_ERROR_DETAIL = new BMapType(TypeConstants.MAP_TNAME, ANYDATA_OR_READONLY,
                                                                 new Module(null, null, null));
    public static final ErrorType TYPE_ERROR = new BErrorType(TypeConstants.ERROR,
                                                              new Module(null, null, null), TYPE_ERROR_DETAIL);

    public static final RecordType STRING_ITR_NEXT_RETURN_TYPE =
            IteratorUtils.createIteratorNextReturnType(Types.TYPE_STRING);
    public static final RecordType XML_ITR_NEXT_RETURN_TYPE = IteratorUtils
            .createIteratorNextReturnType(new BUnionType(Arrays.asList(Types.TYPE_STRING, Types.TYPE_XML)));

    private Types() {
    }

    public static boolean isValueType(Type type) {
        if (type == Types.TYPE_INT || type == Types.TYPE_BYTE || type == Types.TYPE_FLOAT ||
                type == Types.TYPE_DECIMAL || type == Types.TYPE_STRING || type == Types.TYPE_BOOLEAN) {
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
