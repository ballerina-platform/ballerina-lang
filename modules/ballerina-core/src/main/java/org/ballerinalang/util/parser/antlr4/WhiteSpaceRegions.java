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
}
