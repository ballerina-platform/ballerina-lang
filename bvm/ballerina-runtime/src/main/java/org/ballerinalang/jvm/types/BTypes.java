/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.ReadOnlyUtils;

import java.util.Arrays;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.INT_LANG_LIB;
import static org.ballerinalang.jvm.util.BLangConstants.STRING_LANG_LIB;
import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
 *
 * @since 0.995.0
 */
public class BTypes {
    public static BType typeInt = new BIntegerType(TypeConstants.INT_TNAME, new BPackage(null, null, null));
    public static BType typeIntSigned32 = new BIntegerType(TypeConstants.SIGNED32,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null), TypeTags.SIGNED32_INT_TAG);
    public static BType typeIntSigned16 = new BIntegerType(TypeConstants.SIGNED16,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null), TypeTags.SIGNED16_INT_TAG);
    public static BType typeIntSigned8 = new BIntegerType(TypeConstants.SIGNED8,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null), TypeTags.SIGNED8_INT_TAG);
    public static BType typeIntUnsigned32 = new BIntegerType(TypeConstants.UNSIGNED32,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null), TypeTags.UNSIGNED32_INT_TAG);
    public static BType typeIntUnsigned16 = new BIntegerType(TypeConstants.UNSIGNED16,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null), TypeTags.UNSIGNED16_INT_TAG);
    public static BType typeIntUnsigned8 = new BIntegerType(TypeConstants.UNSIGNED8,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, INT_LANG_LIB, null), TypeTags.UNSIGNED8_INT_TAG);

    public static BType typeReadonly = new BReadonlyType(TypeConstants.READONLY_TNAME, new BPackage(null, null, null));
    public static BType typeElement =
            new BXMLType(TypeConstants.XML_ELEMENT, new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                         TypeTags.XML_ELEMENT_TAG, false);
    public static BType typeReadonlyElement = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(typeElement);

    public static BType typeProcessingInstruction =
            new BXMLType(TypeConstants.XML_PI, new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                         TypeTags.XML_PI_TAG, false);
    public static BType typeReadonlyProcessingInstruction =
            ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(typeProcessingInstruction);;

    public static BType typeComment =
            new BXMLType(TypeConstants.XML_COMMENT, new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null),
                         TypeTags.XML_COMMENT_TAG, false);
    public static BType typeReadonlyComment = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(typeComment);

    public static BType typeText = new BXMLType(TypeConstants.XML_TEXT,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB, null), TypeTags.XML_TEXT_TAG, true);

    public static BType typeByte = new BByteType(TypeConstants.BYTE_TNAME, new BPackage(null, null, null));
    public static BType typeFloat = new BFloatType(TypeConstants.FLOAT_TNAME, new BPackage(null, null, null));
    public static BType typeDecimal = new BDecimalType(TypeConstants.DECIMAL_TNAME, new BPackage(null, null, null));
    public static BType typeString = new BStringType(TypeConstants.STRING_TNAME, new BPackage(null, null, null));
    public static BType typeStringChar = new BStringType(TypeConstants.CHAR,
            new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, STRING_LANG_LIB, null), TypeTags.CHAR_STRING_TAG);
    public static BType typeBoolean = new BBooleanType(TypeConstants.BOOLEAN_TNAME, new BPackage(null, null, null));
    public static BType typeXML = new BXMLType(TypeConstants.XML_TNAME,
            new BUnionType(Arrays.asList(typeElement, typeComment, typeProcessingInstruction, typeText)),
            new BPackage(null, null, null));
    public static BType typeJSON = new BJSONType(TypeConstants.JSON_TNAME, new BPackage(null, null, null), false);
    public static BType typeReadonlyJSON = new BJSONType(TypeConstants.READONLY_JSON_TNAME,
                                                         new BPackage(null, null, null), true);
    public static BType typeAny = new BAnyType(TypeConstants.ANY_TNAME, new BPackage(null, null, null), false);
    public static BType typeReadonlyAny = new BAnyType(TypeConstants.READONLY_ANY_TNAME, new BPackage(null, null, null),
                                                       true);
    public static BType typeAnydata = new BAnydataType(TypeConstants.ANYDATA_TNAME, new BPackage(null, null, null),
                                                       false);
    public static BType typeReadonlyAnydata = new BAnydataType(TypeConstants.READONLY_ANYDATA_TNAME,
                                                               new BPackage(null, null, null), true);
    public static BType typeStream = new BStreamType(TypeConstants.STREAM_TNAME, typeAny, new BPackage(null,
            null, null));
    public static BType typeTypedesc = new BTypedescType(TypeConstants.TYPEDESC_TNAME, new BPackage(null,
            null, null));
    public static BType typeMap = new BMapType(TypeConstants.MAP_TNAME, typeAny, new BPackage(null, null, null));
    public static BType typeFuture = new BFutureType(TypeConstants.FUTURE_TNAME,
            new BPackage(null, null, null));
    public static BType typeNull = new BNullType(TypeConstants.NULL_TNAME, new BPackage(null, null, null));
    public static BType typeNever = new BNeverType(new BPackage(null, null, null));
    public static BType typeXMLAttributes = new BXMLAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME,
                                                                   new BPackage(null, null, null));
    public static BType typeIterator = new BIteratorType(TypeConstants.ITERATOR_TNAME, new BPackage(null,
            null, null));
    // public static BType typeChannel = new BChannelType(TypeConstants.CHANNEL, null);
    public static BType typeAnyService = new BServiceType(TypeConstants.SERVICE, new BPackage(null, null, null), 0);
    public static BType typeHandle = new BHandleType(TypeConstants.HANDLE_TNAME, new BPackage(null, null, null));
    public static BType anydataOrReadonly = new BUnionType(Arrays.asList(typeAnydata, typeReadonly));
    public static BMapType typeErrorDetail = new BMapType(TypeConstants.MAP_TNAME, anydataOrReadonly,
            new BPackage(null, null, null));
    public static BErrorType typeError = new BErrorType(TypeConstants.ERROR, new BPackage(null, null, null),
            typeErrorDetail);

    public static BRecordType stringItrNextReturnType = IteratorUtils.createIteratorNextReturnType(BTypes.typeString);
    public static BRecordType xmlItrNextReturnType = IteratorUtils
            .createIteratorNextReturnType(new BUnionType(Arrays.asList(BTypes.typeString, BTypes.typeXML)));

    private BTypes() {
    }

    public static boolean isValueType(BType type) {
        if (type == BTypes.typeInt || type == BTypes.typeByte || type == BTypes.typeFloat ||
                type == BTypes.typeDecimal || type == BTypes.typeString || type == BTypes.typeBoolean) {
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

    public static BType getTypeFromName(String typeName) {
        switch (typeName) {
            case TypeConstants.INT_TNAME:
                return typeInt;
            case TypeConstants.BYTE_TNAME:
                return typeByte;
            case TypeConstants.FLOAT_TNAME:
                return typeFloat;
            case TypeConstants.DECIMAL_TNAME:
                return typeDecimal;
            case TypeConstants.STRING_TNAME:
                return typeString;
            case TypeConstants.BOOLEAN_TNAME:
                return typeBoolean;
            case TypeConstants.JSON_TNAME:
                return typeJSON;
            case TypeConstants.XML_TNAME:
                return typeXML;
            case TypeConstants.MAP_TNAME:
                return typeMap;
            case TypeConstants.FUTURE_TNAME:
                return typeFuture;
             case TypeConstants.STREAM_TNAME:
                return typeStream;
            // case TypeConstants.CHANNEL:
            // return typeChannel;
            case TypeConstants.ANY_TNAME:
                return typeAny;
            case TypeConstants.TYPEDESC_TNAME:
                return typeTypedesc;
            case TypeConstants.NULL_TNAME:
                return typeNull;
            case TypeConstants.XML_ATTRIBUTES_TNAME:
                return typeXMLAttributes;
            case TypeConstants.ERROR:
                return typeError;
            case TypeConstants.ANYDATA_TNAME:
                return typeAnydata;
            default:
                throw new IllegalStateException("Unknown type name");
        }
    }

    public static BType fromString(String typeName) {
        if (typeName.endsWith("[]")) {
            String elementTypeName = typeName.substring(0, typeName.length() - 2);
            BType elemType = fromString(elementTypeName);
            return new BArrayType(elemType);
        }
        return getTypeFromName(typeName);
    }

}
