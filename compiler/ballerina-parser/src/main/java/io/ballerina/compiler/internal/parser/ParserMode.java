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
    DEFAULT,
    IMPORT,
    TEMPLATE,
    INTERPOLATION,
    INTERPOLATION_BRACED_CONTENT,
    DOCUMENTATION_INIT,
    DOCUMENTATION,
    DOCUMENTATION_INTERNAL,
    DOCUMENTATION_PARAMETER,
    DOCUMENTATION_REFERENCE_TYPE,
    DOCUMENTATION_BACKTICK_CONTENT,
    DOCUMENTATION_BACKTICK_EXPR,
    XML_CONTENT,
    XML_ELEMENT_START_TAG,
    XML_ELEMENT_END_TAG,
    XML_TEXT,
    XML_ATTRIBUTES,
    XML_COMMENT,
    XML_PI,
    XML_PI_DATA,
    XML_SINGLE_QUOTED_STRING,
    XML_DOUBLE_QUOTED_STRING
}
