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

package org.ballerinalang.plugins.idea;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;

import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.ballerinalang.plugins.idea.lexer.BallerinaLexerAdapter;
import org.ballerinalang.plugins.idea.parser.BallerinaParser;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

import static org.ballerinalang.plugins.idea.psi.BallerinaTypes.*;

public class BallerinaParserDefinition implements ParserDefinition {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(BallerinaTypes.LINE_COMMENT);

    public static final TokenSet STRING_LITERALS = TokenSet.create(QUOTEDSTRINGLITERAL, VALIDBACKTICKSTRING);
    public static final TokenSet NUMBERS = TokenSet.create(INTEGERLITERAL, FLOATINGPOINTLITERAL);
    public static final TokenSet KEYWORDS = TokenSet.create(
            ACTION, BREAK, CATCH, CONNECTOR, CONST, ELSE, FORK, FUNCTION, IF, IMPORT, ITERATE, JOIN, NEW, PACKAGE,
            REPLY, RESOURCE, RETURN, SERVICE, THROW, THROWS, TRY, TYPE, TYPECONVERTOR, WHILE, WORKER, BACKTICK,
            VERSION, PUBLIC, ANY, ALL, AS, TIMEOUT, SENDARROW, RECEIVEARROW, NULLLITERAL, BOOLEANLITERAL);
    public static final TokenSet OPERATORS = TokenSet.create(
            ASSIGN, GT, LT, BANG, TILDE, QUESTION, COLON, EQUAL, LE, GE, NOTEQUAL, AND, OR, ADD, SUB, MUL, DIV,
            BITAND, BITOR, CARET, MOD, DOLLAR_SIGN, AT);

    public static final IFileElementType FILE =
            new IFileElementType(Language.<BallerinaLanguage>findInstance(BallerinaLanguage.class));

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new BallerinaLexerAdapter();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new BallerinaParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new BallerinaFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return BallerinaTypes.Factory.createElement(node);
    }
}
