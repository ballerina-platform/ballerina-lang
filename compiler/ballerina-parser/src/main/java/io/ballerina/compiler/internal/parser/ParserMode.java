/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.internal.parser;

/**
 * Modes of parsing.
 * 
 * @since 1.3.0
 */
public enum ParserMode {

    // Ballerina Parser
    DEFAULT,
    IMPORT,
    TEMPLATE,
    INTERPOLATION,
    INTERPOLATION_BRACED_CONTENT,

    // Documentation Parser
    DOC_LINE_START_HASH,
    DOC_LINE_DIFFERENTIATOR,
    DOC_INTERNAL,
    DOC_PARAMETER,
    DOC_REFERENCE_TYPE,
    DOC_SINGLE_BACKTICK_CONTENT,
    DOC_DOUBLE_BACKTICK_CONTENT,
    DOC_TRIPLE_BACKTICK_CONTENT,
    DOC_CODE_REF_END,
    DOC_CODE_LINE_START_HASH,

    // XML Parser
    XML_CONTENT,
    XML_ELEMENT_START_TAG,
    XML_ELEMENT_END_TAG,
    XML_TEXT,
    XML_ATTRIBUTES,
    XML_COMMENT,
    XML_PI,
    XML_PI_DATA,
    XML_SINGLE_QUOTED_STRING,
    XML_DOUBLE_QUOTED_STRING,
    XML_CDATA_SECTION,

    // RegExp Parser
    RE_DISJUNCTION,
    RE_FLAG_EXPRESSION,
    RE_UNICODE_PROP_ESCAPE,
    RE_UNICODE_PROPERTY_VALUE,
    RE_ESCAPE,
}
