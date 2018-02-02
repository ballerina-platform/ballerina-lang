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
import com.intellij.psi.tree.IElementType;
import org.antlr.jetbrains.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;
import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BallerinaSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        fillMap(ATTRIBUTES, BallerinaParserDefinition.COMMENTS, BallerinaSyntaxHighlightingColors.LINE_COMMENT);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.KEYWORDS, BallerinaSyntaxHighlightingColors.KEYWORD);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.STRING_LITERALS, BallerinaSyntaxHighlightingColors.STRING);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.NUMBER, BallerinaSyntaxHighlightingColors.NUMBER);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.BRACES_AND_OPERATORS, BallerinaSyntaxHighlightingColors.KEYWORD);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.BAD_CHARACTER, BallerinaSyntaxHighlightingColors.BAD_CHARACTER);
        fillMap(ATTRIBUTES, BallerinaParserDefinition.IDENTIFIER, BallerinaSyntaxHighlightingColors.IDENTIFIER);
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        BallerinaLexer lexer = new BallerinaLexer(null);
        return new ANTLRLexerAdaptor(BallerinaLanguage.INSTANCE, lexer);
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (!(tokenType instanceof TokenIElementType)) {
            return EMPTY_KEYS;
        }
        TokenIElementType myType = (TokenIElementType) tokenType;
        return pack(ATTRIBUTES.get(myType));
    }
}
