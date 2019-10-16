/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler.format;

/**
 * Formatting constants for keywords used in the formatting util.
 */
public class FormattingConstants {
    // Indentation whitespace values.
    public static final String SPACE_TAB = "    ";
    public static final String SINGLE_SPACE = " ";
    public static final String NEW_LINE = System.lineSeparator();
    public static final String EMPTY_SPACE = "";

    // Formatting config related properties.
    public static final String SKIP_FORMATTING = "skipFormatting";
    public static final String NEW_LINE_COUNT = "newLineCount";
    public static final String SPACE_COUNT = "spacesCount";
    public static final String START_COLUMN = "startColumn";
    public static final String DO_INDENT = "doIndent";
    public static final String INDENTED_START_COLUMN = "indentedStartColumn";
    public static final String FORMATTING_CONFIG = "formattingConfig";
    public static final String USE_PARENT_INDENTATION = "useParentIndentation";

    // Node's WS related constants.
    public static final String WS = "ws";
    public static final String TEXT = "text";
    public static final String PARENT = "parent";
    public static final String POSITION = "position";
    public static final String EXPRESSION = "expression";
    public static final String GROUPED = "grouped";
    public static final String IS_ANON_TYPE = "isAnonType";
    public static final String BODY = "body";
    public static final String STATEMENTS = "statements";
    public static final String ANON_TYPE = "anonType";
    public static final String FIELDS = "fields";
    public static final String TYPE_NODE = "typeNode";
    public static final String VALUE = "value";
    public static final String IS_EXPRESSION = "isExpression";
    public static final String VARIABLE = "variable";
    public static final String KIND = "kind";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String EXPRESSIONS = "expressions";
    public static final String WORKERS = "workers";
    public static final String IS_VAR_EXISTS = "isVarExists";
    public static final String ANNOTATION_ATTACHMENTS = "annotationAttachments";
}
