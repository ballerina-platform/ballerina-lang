/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.highlighter;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Contains colors for syntax highlighting.
 */
public class BallerinaSyntaxHighlightingColors {

    public static final TextAttributesKey LINE_COMMENT = createTextAttributesKey("BALLERINA_LINE_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("BALLERINA_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING = createTextAttributesKey("BALLERINA_STRING",
            DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("BALLERINA_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey OPERATOR = createTextAttributesKey("BALLERINA_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("BALLERINA_IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("BALLERINA_BAD_TOKEN",
            HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey ANNOTATION = createTextAttributesKey("BALLERINA_ANNOTATION",
            DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey CONSTANT = createTextAttributesKey("BALLERINA_CONSTANT",
            DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey GLOBAL_VARIABLE = createTextAttributesKey("BALLERINA_GLOBAL_VARIABLE",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey VALID_STRING_ESCAPE = createTextAttributesKey("BALLERINA_VALID_STRING_ESCAPE",
            DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey INVALID_STRING_ESCAPE = createTextAttributesKey(
            "BALLERINA_INVALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    public static final TextAttributesKey PACKAGE = createTextAttributesKey("BALLERINA_PACKAGE",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey STATIC_FIELD = createTextAttributesKey("BALLERINA_STATIC_FIELD",
            DefaultLanguageHighlighterColors.STATIC_FIELD);
    public static final TextAttributesKey DOC = createTextAttributesKey("BALLERINA_DOC",
            DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey DOC_COMMENT_TAG = createTextAttributesKey("BALLERINA_DOC_COMMENT_TAG",
            DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
    public static final TextAttributesKey DOC_TAG_VALUE = createTextAttributesKey("BALLERINA_DOC_TAG_VALUE",
            DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE);
    public static final TextAttributesKey DOC_COMMENT_MARKUP = createTextAttributesKey("BALLERINA_DOC_COMMENT_MARKUP",
            DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
    public static final TextAttributesKey TEMPLATE_LANGUAGE_COLOR = createTextAttributesKey
            ("BALLERINA_TEMPLATE_LANGUAGE_COLOR", DefaultLanguageHighlighterColors.INSTANCE_METHOD);

    private BallerinaSyntaxHighlightingColors() {
    }
}
