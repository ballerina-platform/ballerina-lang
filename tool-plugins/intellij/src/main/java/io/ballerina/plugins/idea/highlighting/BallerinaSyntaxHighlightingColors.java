/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.highlighting;

import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOC_COMMENT;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOC_COMMENT_TAG;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.METADATA;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Syntax highlighting colors.
 */
public class BallerinaSyntaxHighlightingColors {

    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("BALLERINA_BAD_TOKEN",
            HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey LINE_COMMENT = createTextAttributesKey("BALLERINA_LINE_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("BALLERINA_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING = createTextAttributesKey("BALLERINA_STRING",
            DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("BALLERINA_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey ANNOTATION = createTextAttributesKey("BALLERINA_ANNOTATION",
            METADATA);
    public static final TextAttributesKey PACKAGE = createTextAttributesKey("BALLERINA_PACKAGE",
            FUNCTION_DECLARATION);
    public static final TextAttributesKey TEMPLATE_LANGUAGE_COLOR = createTextAttributesKey
            ("BALLERINA_TEMPLATE_LANGUAGE_COLOR", INSTANCE_METHOD);
    public static final TextAttributesKey DOCUMENTATION = createTextAttributesKey("BALLERINA_DOCUMENTATION",
            DOC_COMMENT);
    public static final TextAttributesKey DOCUMENTATION_VARIABLE =
            createTextAttributesKey("BALLERINA_DOCUMENTATION_VARIABLE", DOC_COMMENT_TAG);
    public static final TextAttributesKey DOCUMENTATION_INLINE_CODE =
            createTextAttributesKey("BALLERINA_DOCUMENTATION_INLINE_CODE", DOC_COMMENT_TAG_VALUE);
    public static final TextAttributesKey GLOBAL_VARIABLE = createTextAttributesKey("BALLERINA_GLOBAL_VARIABLE",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey RECORD_KEY = createTextAttributesKey("BALLERINA_RECORD_KEY",
            CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES);
}
