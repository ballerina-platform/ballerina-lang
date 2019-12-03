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

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.util.Flags;

import java.util.Arrays;
import java.util.HashMap;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
 *
 * @since 0.995.0
 */
public class BTypes {
    public static BType typeInt = new BIntegerType(TypeConstants.INT_TNAME, new BPackage(null, null, null));
    public static BType typeByte = new BByteType(TypeConstants.BYTE_TNAME, new BPackage(null, null, null));
    public static BType typeFloat = new BFloatType(TypeConstants.FLOAT_TNAME, new BPackage(null, null, null));
    public static BType typeDecimal = new BDecimalType(TypeConstants.DECIMAL_TNAME, new BPackage(null, null, null));
    public static BType typeString = new BStringType(TypeConstants.STRING_TNAME, new BPackage(null, null, null));
    public static BType typeBoolean = new BBooleanType(TypeConstants.BOOLEAN_TNAME, new BPackage(null, null, null));
    public static BType typeXML = new BXMLType(TypeConstants.XML_TNAME, new BPackage(null, null, null));
    public static BType typeJSON = new BJSONType(TypeConstants.JSON_TNAME, new BPackage(null, null, null));
    public static BType typeAny = new BAnyType(TypeConstants.ANY_TNAME, new BPackage(null, null, null));
    public static BType typeAnydata = new BAnydataType(TypeConstants.ANYDATA_TNAME, new BPackage(null, null, null));
    public static BType typeStream = new BStreamType(TypeConstants.STREAM_TNAME, typeAny, new BPackage(null,
            null, null));
    public static BType typeTypedesc = new BTypedescType(TypeConstants.TYPEDESC_TNAME, new BPackage(null,
            null, null));
    public static BType typeMap = new BMapType(TypeConstants.MAP_TNAME, typeAny, new BPackage(null, null, null));
    public static BType typeTable = new BTableType(TypeConstants.TABLE_TNAME, typeAnydata,
            new BPackage(null, null, null));
    public static BType typeFuture = new BFutureType(TypeConstants.FUTURE_TNAME,
            new BPackage(null, null, null));
    public static BType typeNull = new BNullType(TypeConstants.NULL_TNAME, new BPackage(null, null, null));
    public static BType typeXMLAttributes = new BXMLAttributesType(TypeConstants.XML_ATTRIBUTES_TNAME,
                                                                   new BPackage(null, null, null));
    public static BType typeIterator = new BIteratorType(TypeConstants.ITERATOR_TNAME, new BPackage(null,
            null, null));
    // public static BType typeChannel = new BChannelType(TypeConstants.CHANNEL, null);
    public static BType typeAnyService = new BServiceType(TypeConstants.SERVICE, new BPackage(null, null, null), 0);
    public static BRecordType typeErrorDetail = new BRecordType(TypeConstants.DETAIL_TYPE, new BPackage(null, null,
            null), 0, false, TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE));
    public static BErrorType typeError = new BErrorType(TypeConstants.ERROR, new BPackage(null,
            null, null), typeString, typeErrorDetail);
    public static BType typePureType = new BUnionType(Arrays.asList(typeAnydata, typeError));
    public static BType typeAllType = new BUnionType(Arrays.asList(typeAny, typeError));
    public static BType typeHandle = new BHandleType(TypeConstants.HANDLE_TNAME, new BPackage(null, null, null));

    static {
        HashMap<String, BField> fields = new HashMap<>();
        fields.put(TypeConstants.DETAIL_MESSAGE, new BField(typeString, TypeConstants.DETAIL_MESSAGE,
                Flags.OPTIONAL + Flags.PUBLIC));
        fields.put(TypeConstants.DETAIL_CAUSE, new BField(typeError, TypeConstants.DETAIL_CAUSE,
                Flags.OPTIONAL + Flags.PUBLIC));
        typeErrorDetail.setFields(fields);
        BType[] restFieldType = new BType[2];
        restFieldType[0] = BTypes.typeAnydata;
        restFieldType[1] = BTypes.typeError;
        typeErrorDetail.restFieldType = new BUnionType(Arrays.asList(restFieldType));
    }
    
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
