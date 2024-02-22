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

import io.ballerina.runtime.api.types.Type;

/**
 * This class contains predefined types used in ballerina runtime.
 *
 * @since 2.0.0
 */
public class PredefinedTypes {

    public static final Type TYPE_INT = TypeBuilder.intType();
    public static final Type TYPE_INT_SIGNED_8 = TypeBuilder.INT_SIGNED_8;
    public static final Type TYPE_INT_SIGNED_16 = TypeBuilder.INT_SIGNED_16;
    public static final Type TYPE_INT_SIGNED_32 = TypeBuilder.INT_SIGNED_32;
    public static final Type TYPE_INT_UNSIGNED_8 = TypeBuilder.INT_UNSIGNED_8;
    public static final Type TYPE_INT_UNSIGNED_16 = TypeBuilder.INT_UNSIGNED_16;
    public static final Type TYPE_INT_UNSIGNED_32 = TypeBuilder.INT_UNSIGNED_32;
    public static final Type TYPE_BOOLEAN = TypeBuilder.booleanType();
    public static final Type TYPE_BYTE = TypeBuilder.byteType();
    public static final Type TYPE_FLOAT = TypeBuilder.floatType();
    public static final Type TYPE_DECIMAL = TypeBuilder.decimalType();
    public static final Type TYPE_STRING = TypeBuilder.stringType();
    public static final Type TYPE_STRING_CHAR = TypeBuilder.STRING_CHAR;

    public static final Type TYPE_READONLY = TypeBuilder.readonlyType();
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

    public static final Type TYPE_READONLY_XML = TypeBuilder.TypeCache.TYPE_READONLY_XML;
    // FIXME:
    public static final Type TYPE_XML_ELEMENT_SEQUENCE = TypeBuilder.TypeCache.TYPE_XML_ELEMENT_SEQUENCE;
    public static final Type TYPE_XML_COMMENT_SEQUENCE = TypeBuilder.TypeCache.TYPE_XML_COMMENT_SEQUENCE;
    public static final Type TYPE_XML_PI_SEQUENCE = TypeBuilder.TypeCache.TYPE_XML_PI_SEQUENCE;
    public static final Type TYPE_XML_TEXT_SEQUENCE = TypeBuilder.TypeCache.TYPE_XML_TEXT_SEQUENCE;

    public static final Type TYPE_ANY = TypeBuilder.anyType();
    public static final Type TYPE_READONLY_ANY = TypeBuilder.intersect(TYPE_ANY, TYPE_READONLY);
    public static final Type TYPE_TYPEDESC = TypeBuilder.typedescType();
    public static final Type TYPE_MAP = TypeBuilder.typeMap();
    public static final Type TYPE_FUTURE = TypeBuilder.typeFuture();
    public static final Type TYPE_NULL = TypeBuilder.nilType();
    public static final Type TYPE_NEVER = TypeBuilder.neverType();
    public static final Type TYPE_XML_ATTRIBUTES = TypeBuilder.TypeCache.TYPE_XML_ATTRIBUTES;
    public static final Type TYPE_ITERATOR = TypeBuilder.TypeCache.TYPE_ITERATOR;
    public static final Type TYPE_ANY_SERVICE = TypeBuilder.TypeCache.TYPE_ANY_SERVICE;
    public static final Type TYPE_HANDLE = TypeBuilder.typeHandle();
    public static final Type TYPE_STREAM = TypeBuilder.typeStream();

    public static final Type TYPE_JSON = TypeBuilder.jsonType();
    public static final Type TYPE_READONLY_JSON = TypeBuilder.intersect(TYPE_JSON, TYPE_READONLY);
    public static final Type TYPE_JSON_ARRAY = TypeBuilder.TypeCache.TYPE_JSON_ARRAY;
    public static final Type TYPE_ANYDATA = TypeBuilder.anydataType();
    public static final Type TYPE_READONLY_ANYDATA = TypeBuilder.intersect(TYPE_ANYDATA, TYPE_READONLY);
    public static final Type TYPE_ANYDATA_ARRAY = TypeBuilder.TypeCache.TYPE_ANYDATA_ARRAY;
    public static final Type TYPE_DETAIL = TypeBuilder.TypeCache.TYPE_DETAIL;
    public static final Type TYPE_ERROR_DETAIL = TypeBuilder.TypeCache.TYPE_ERROR_DETAIL;
    public static final Type TYPE_ERROR = TypeBuilder.typeError();
    public static final Type TYPE_CLONEABLE = TypeBuilder.TypeCache.TYPE_CLONEABLE;

    public static final Type TYPE_JSON_DECIMAL = TypeBuilder.TypeCache.TYPE_JSON_DECIMAL;
    public static final Type TYPE_JSON_FLOAT = TypeBuilder.TypeCache.TYPE_JSON_FLOAT;

    public static final Type STRING_ITR_NEXT_RETURN_TYPE = TypeBuilder.TypeCache.STRING_ITR_NEXT_RETURN_TYPE;

    public static final Type ANY_AND_READONLY_TYPE = TypeBuilder.TypeCache.ANY_AND_READONLY_TYPE;
    public static final Type ANY_AND_READONLY_OR_ERROR_TYPE = TypeBuilder.TypeCache.ANY_AND_READONLY_OR_ERROR_TYPE;

    private PredefinedTypes() {}

}
