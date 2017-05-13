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
package org.ballerinalang.util.parser.antlr4;

/**
 * Contains region ids of possible whitespace regions within each language construct according to grammar.
 *
 * @see WhiteSpaceUtil
 * @since 0.9.0
 */
public class WhiteSpaceRegions {
    // whitespace regions related to BFile
    public static final int BFILE_START = 0;
    public static final int BFILE_PKG_KEYWORD_TO_PKG_NAME_START = 1;
    public static final int BFILE_PKG_NAME_END_TO_SEMICOLON = 2;
    public static final int BFILE_PKG_DEC_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a import declaration
    public static final int IMPORT_DEC_IMPORT_KEYWORD_TO_PKG_NAME_START = 0;
    public static final int IMPORT_DEC_PKG_NAME_END_TO_NEXT = 1;
    public static final int IMPORT_DEC_AS_KEYWORD_TO_IDENTIFIER = 2;
    public static final int IMPORT_DEC_IDENTIFIER_TO_IMPORT_DEC_END = 3;
    public static final int IMPORT_DEC_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a service definition
    public static final int SERVICE_DEF_SERVICE_KEYWORD_TO_IDENTIFIER = 0;
    public static final int SERVICE_DEF_IDENTIFIER_TO_BODY_START = 1;
    public static final int SERVICE_DEF_BODY_START_TO_FIRST_CHILD = 2;
    public static final int SERVICE_DEF_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a resource definition
    public static final int RESOURCE_DEF_RESOURCE_KEYWORD_TO_IDENTIFIER = 0;
    public static final int RESOURCE_DEF_IDENTIFIER_TO_PARAM_LIST_START = 1;
    public static final int RESOURCE_DEF_PARAM_LIST_START_TO_FIRST_PARAM = 2;
    public static final int RESOURCE_DEF_PARAM_LIST_END_TO_BODY_START = 3;
    public static final int RESOURCE_DEF_BODY_START_TO_FIRST_CHILD = 4;
    public static final int RESOURCE_DEF_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in a annotation attachment
    public static final int ANNOTATION_ATCHMNT_AT_KEYWORD_TO_IDENTIFIER = 0;
    public static final int ANNOTATION_ATCHMNT_IDENTIFIER_TO_ATTRIB_LIST_START = 1;
    public static final int ANNOTATION_ATCHMNT_ATTRIB_LIST_START_TO_FIRST_ATTRIB = 2;
    public static final int ANNOTATION_ATCHMNT_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a annotation attribute
    public static final int ANNOTATION_ATTRIB_KEY_START_TO_LAST_TOKEN = 0;
    public static final int ANNOTATION_ATTRIB_KEY_TO_COLON = 1;
    public static final int ANNOTATION_ATTRIB_COLON_TO_VALUE_START = 2;
    public static final int ANNOTATION_ATTRIB_VALUE_START_TO_LAST_TOKEN = 3;
    public static final int ANNOTATION_ATTRIB_VALUE_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a function definition
    public static final int FUNCTION_DEF_NATIVE_KEYWORD_TO_FUNCTION_KEYWORD = 0;
    public static final int FUNCTION_DEF_FUNCTION_KEYWORD_TO_IDENTIFIER_START = 1;
    public static final int FUNCTION_DEF_IDENTIFIER_TO_PARAM_LIST_START = 2;
    public static final int FUNCTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START = 3;
    public static final int FUNCTION_DEF_RETURN_PARAM_END_TO_THROWS_KEYWORD = 4;
    public static final int FUNCTION_DEF_THROWS_KEYWORD_TO_EXCEPTION_KEYWORD = 5;
    public static final int FUNCTION_DEF_BODY_START_TO_LAST_TOKEN = 6;
    public static final int FUNCTION_DEF_BODY_END_TO_NEXT_TOKEN = 7;

    // whitespace regions in a connector definition
    public static final int CONNECTOR_DEF_CONNECTOR_KEYWORD_TO_IDENTIFIER = 0;
    public static final int CONNECTOR_DEF_IDENTIFIER_TO_PARAM_LIST_START = 1;
    public static final int CONNECTOR_DEF_PARAM_LIST_END_TO_BODY_START = 2;
    public static final int CONNECTOR_DEF_BODY_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in a struct definition
    public static final int STRUCT_DEF_STRUCT_KEYWORD_TO_IDENTIFIER = 0;
    public static final int STRUCT_DEF_IDENTIFIER_TO_BODY_START = 1;
    public static final int STRUCT_DEF_BODY_END_TO_NEXT_TOKEN = 2;

    // whitespace regions in a type mapper definition
    public static final int TYPE_MAP_DEF_NATIVE_KEYWORD_TO_SIGNATURE_START = 0;
    public static final int TYPE_MAP_DEF_TYPEMAPPER_KEYWORD_TO_IDENTIFIER = 1;
    public static final int TYPE_MAP_DEF_IDENTIFIER_PARAM_WRAPPER_START = 2;
    public static final int TYPE_MAP_DEF_PARAM_WRAPPER_END_TO_RETURN_TYPE_WRAPPER_START = 3;
    public static final int TYPE_MAP_DEF_RETURN_TYPE_WRAPPER_TO_BODY_START = 4;
    public static final int TYPE_MAP_DEF_BODY_END_TO_NEXT_TOKEN = 5;

    // whitespace regions in a constant definition
    public static final int CONST_DEF_CONST_KEYWORD_TO_VAL_TYPE = 0;
    public static final int CONST_DEF_VAL_TYPE_TO_IDENTIFIER = 1;
    public static final int CONST_DEF_IDENTIFIER_TO_EQUAL_OPERATOR = 2;
    public static final int CONST_DEF_EQUAL_OPERATOR_TO_LITERAL_START = 3;
    public static final int CONST_DEF_END_TO_NEXT_TOKEN = 4;

    // whitespace regions in a annotation definition
    public static final int ANNOTATION_DEF_ANNOTATION_KEYWORD_TO_IDENTIFIER = 0;
    public static final int ANNOTATION_DEF_IDENTIFIER_TO_ATTACH_KEYWORD = 1;
    public static final int ANNOTATION_DEF_BODY_START_TO_LAST_TOKEN = 2;
    public static final int ANNOTATION_DEF_BODY_END_TO_NEXT_TOKEN = 3;

    // whitespace regions in an annotation attachment point
    public static final int ANNOTATION_ATTACHMENT_POINT_PRECEDING_WS = 0;
    public static final int ANNOTATION_ATTACHMENT_POINT_TAILING_WS = 1;
}
