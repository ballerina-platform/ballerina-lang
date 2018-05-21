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
 *
 */

package org.ballerinalang.plugins.idea.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.lexer.BallerinaLexerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors.BAD_CHARACTER;
import static org.ballerinalang.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors.KEYWORD;
import static org.ballerinalang.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors.LINE_COMMENT;
import static org.ballerinalang.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors.NUMBER;
import static org.ballerinalang.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors.STRING;

/**
 * Provides syntax highlighting support.
 */
public class BallerinaSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        fillMap(ATTRIBUTES, BallerinaParserDefinition.COMMENTS, LINE_COMMENT);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.KEYWORDS, KEYWORD);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.OPERATORS, KEYWORD);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.STRINGS, STRING);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.NUMBERS, NUMBER);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.BAD_CHARACTER, BAD_CHARACTER);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new BallerinaLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}
