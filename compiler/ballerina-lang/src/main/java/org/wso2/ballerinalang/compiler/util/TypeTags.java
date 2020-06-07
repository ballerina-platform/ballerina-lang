/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util;

/**
 * @since 0.94
 */
public class TypeTags {

    public static final int INT = 1;
    public static final int BYTE = INT + 1;
    public static final int FLOAT = BYTE + 1;
    public static final int DECIMAL = FLOAT + 1;
    public static final int STRING = DECIMAL + 1;
    public static final int BOOLEAN = STRING + 1;
    // All the above types are values type
    public static final int JSON = BOOLEAN + 1;
    public static final int XML = JSON + 1;
    public static final int TABLE = XML + 1;
    public static final int NIL = TABLE + 1;
    public static final int ANYDATA = NIL + 1;
    public static final int RECORD = ANYDATA + 1;
    public static final int TYPEDESC = RECORD + 1;
    public static final int STREAM = TYPEDESC + 1;
    public static final int MAP = STREAM + 1;
    public static final int INVOKABLE = MAP + 1;
    // All the above types are branded types
    public static final int ANY = INVOKABLE + 1;
    public static final int ENDPOINT = ANY + 1;
    public static final int ARRAY = ENDPOINT + 1;
    public static final int UNION = ARRAY + 1;
    public static final int INTERSECTION = UNION + 1;
    public static final int PACKAGE = INTERSECTION + 1;
    public static final int NONE = PACKAGE + 1;
    public static final int VOID = NONE + 1;
    public static final int XMLNS = VOID + 1;
    public static final int ANNOTATION = XMLNS + 1;
    public static final int SEMANTIC_ERROR = ANNOTATION + 1;
    public static final int ERROR = SEMANTIC_ERROR + 1;
    public static final int ITERATOR = ERROR + 1;
    public static final int TUPLE = ITERATOR + 1;
    public static final int FUTURE = TUPLE + 1;
    public static final int FINITE = FUTURE + 1;
    public static final int OBJECT = FINITE + 1;
    public static final int SERVICE = OBJECT;
    public static final int BYTE_ARRAY = OBJECT + 1;
    public static final int FUNCTION_POINTER = BYTE_ARRAY + 1;
    public static final int HANDLE = FUNCTION_POINTER + 1;
    public static final int READONLY = HANDLE + 1;

    // Subtypes
    public static final int SIGNED32_INT = READONLY + 1;
    public static final int SIGNED16_INT = SIGNED32_INT + 1;
    public static final int SIGNED8_INT = SIGNED16_INT + 1;
    public static final int UNSIGNED32_INT = SIGNED8_INT + 1;
    public static final int UNSIGNED16_INT = UNSIGNED32_INT + 1;
    public static final int UNSIGNED8_INT = UNSIGNED16_INT + 1;
    public static final int CHAR_STRING = UNSIGNED8_INT + 1;
    public static final int XML_ELEMENT = CHAR_STRING + 1;
    public static final int XML_PI = XML_ELEMENT + 1;
    public static final int XML_COMMENT = XML_PI + 1;
    public static final int XML_TEXT = XML_COMMENT + 1;
    public static final int NEVER = XML_TEXT + 1;

    public static final int NULL_SET = XML_TEXT + 1;

    private TypeTags() {

    }

    public static boolean isIntegerTypeTag(int tag) {

        // TODO : Fix byte type. Ideally, byte belongs to here. But we have modeled it differently.
        switch (tag) {
            case INT:
            case SIGNED32_INT:
            case SIGNED16_INT:
            case SIGNED8_INT:
            case UNSIGNED32_INT:
            case UNSIGNED16_INT:
            case UNSIGNED8_INT:
                return true;
        }
        return false;
    }

    public static boolean isSignedIntegerTypeTag(int tag) {

        switch (tag) {
            case INT:
            case SIGNED32_INT:
            case SIGNED16_INT:
            case SIGNED8_INT:
                return true;
        }
        return false;
    }

    public static boolean isXMLTypeTag(int tag) {
        switch (tag) {
            case XML:
            case XML_ELEMENT:
            case XML_PI:
            case XML_COMMENT:
            case XML_TEXT:
                return true;
        }
        return false;
    }

    public static boolean isXMLNonSequenceType(int tag) {
        switch (tag) {
            case XML_ELEMENT:
            case XML_PI:
            case XML_COMMENT:
            case XML_TEXT:
                return true;
        }
        return false;
    }

    public static boolean isStringTypeTag(int tag) {

        switch (tag) {
            case STRING:
            case CHAR_STRING:
                return true;
        }
        return false;
    }
}
