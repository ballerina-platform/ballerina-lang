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

package org.ballerinalang.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.BallerinaLexerAdapter;
import org.ballerinalang.BallerinaParserDefinition;
import org.ballerinalang.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BallerinaSyntaxHighlighter extends SyntaxHighlighterBase {
    //    public static final TextAttributesKey SEPARATOR =
    //            createTextAttributesKey("SIMPLE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    //    public static final TextAttributesKey KEY =
    //            createTextAttributesKey("SIMPLE_KEY", DefaultLanguageHighlighterColors.KEYWORD);
    //    public static final TextAttributesKey VALUE =
    //            createTextAttributesKey("SIMPLE_VALUE", DefaultLanguageHighlighterColors.STRING);
    //    public static final TextAttributesKey COMMENT =
    //            createTextAttributesKey("SIMPLE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    //    public static final TextAttributesKey BAD_CHARACTER =
    //            createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    //
    //    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    //    private static final TextAttributesKey[] SEPARATOR_KEYS = new TextAttributesKey[]{SEPARATOR};
    //    private static final TextAttributesKey[] KEY_KEYS = new TextAttributesKey[]{KEY};
    //    private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
    //    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    //    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    //
    //    @NotNull
    //    @Override
    //    public Lexer getHighlightingLexer() {
    //        return new BallerinaLexerAdapter();
    //    }
    //
    //    @NotNull
    //    @Override
    //    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    //        if (tokenType.equals(SimpleTypes.SEPARATOR)) {
    //            return SEPARATOR_KEYS;
    //        } else if (tokenType.equals(SimpleTypes.KEY)) {
    //            return KEY_KEYS;
    //        } else if (tokenType.equals(SimpleTypes.VALUE)) {
    //            return VALUE_KEYS;
    //        } else if (tokenType.equals(SimpleTypes.COMMENT)) {
    //            return COMMENT_KEYS;
    //        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
    //            return BAD_CHAR_KEYS;
    //        } else {
    //            return EMPTY_KEYS;
    //        }
    //    }

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType,
            TextAttributesKey>();

    static {
        fillMap(ATTRIBUTES, BallerinaSyntaxHighlightingColors.LINE_COMMENT, BallerinaTypes.LINE_COMMENT);
        //        fillMap(ATTRIBUTES, PARENTHESES, GoTypes.LPAREN, GoTypes.RPAREN);
        //        fillMap(ATTRIBUTES, BRACES, GoTypes.LBRACE, GoTypes.RBRACE);
        //        fillMap(ATTRIBUTES, BRACKETS, GoTypes.LBRACK, GoTypes.RBRACK);
        fillMap(ATTRIBUTES, BallerinaSyntaxHighlightingColors.BAD_CHARACTER, TokenType.BAD_CHARACTER);
        //        fillMap(ATTRIBUTES, IDENTIFIER, GoTypes.IDENTIFIER);
        //        fillMap(ATTRIBUTES, DOT, GoTypes.DOT, GoTypes.TRIPLE_DOT);
        //        fillMap(ATTRIBUTES, COLON, GoTypes.COLON);
        //        fillMap(ATTRIBUTES, SEMICOLON, GoTypes.SEMICOLON);
        //        fillMap(ATTRIBUTES, COMMA, GoTypes.COMMA);
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
