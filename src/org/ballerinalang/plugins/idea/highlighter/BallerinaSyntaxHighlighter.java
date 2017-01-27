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

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.lexer.BallerinaLexerAdapter;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BallerinaSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES =
            new HashMap<IElementType, TextAttributesKey>();

    static {
        fillMap(ATTRIBUTES, BallerinaSyntaxHighlightingColors.LINE_COMMENT, BallerinaTypes.LINE_COMMENT);
        fillMap(ATTRIBUTES, BallerinaSyntaxHighlightingColors.BAD_CHARACTER, TokenType.BAD_CHARACTER);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.OPERATORS, BallerinaSyntaxHighlightingColors.OPERATOR);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.KEYWORDS, BallerinaSyntaxHighlightingColors.KEYWORD);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.NUMBERS, BallerinaSyntaxHighlightingColors.NUMBER);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.STRING_LITERALS, BallerinaSyntaxHighlightingColors.STRING);
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new BallerinaLexerAdapter();
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}
