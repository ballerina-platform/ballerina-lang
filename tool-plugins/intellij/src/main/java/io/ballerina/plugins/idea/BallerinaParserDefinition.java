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

package io.ballerina.plugins.idea;

import com.intellij.lang.ASTNode;
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
import io.ballerina.plugins.idea.lexer.BallerinaLexerAdapter;
import io.ballerina.plugins.idea.parser.BallerinaParser;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

import static io.ballerina.plugins.idea.psi.BallerinaTypes.ABORT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ABORTED;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ABSTRACT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ANNOTATION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ANY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ANYDATA;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BINARY_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BOOLEAN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BOOLEAN_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BREAK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BYTE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CATCH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CHANNEL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CHECK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CHECKPANIC;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CLIENT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.COMMITTED;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CONST;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CONTINUE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DECIMAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DECIMAL_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ELSE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ELVIS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.EQUAL_GT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ERROR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.EXTERNAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FAIL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FINAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FINALLY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FLOAT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FLOATING_POINT_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FLUSH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FOREVER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FORK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FROM;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FUNCTION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FUTURE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.HANDLE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.HEX_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IF;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IMPORT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.INT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.JSON;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LARROW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LINE_COMMENT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LISTENER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LOCK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.MATCH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.NEW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.NULL_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OBJECT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OBJECT_INIT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OCTAL_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ON;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ONRETRY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.PANIC;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.PARAMETER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.PRIVATE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.PUBLIC;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.QUOTED_STRING_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RARROW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RECORD;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.REMOTE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RESOURCE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RETRIES;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RETRY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RETURN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RETURNS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SELECT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SERVICE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SOURCE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.START;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.STREAM;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.STRING;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SYNCRARROW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TABLE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.THROW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TRANSACTION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TRAP;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TRY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TYPE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TYPEDESC;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TYPEOF;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.VAR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.VERSION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WAIT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WHILE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WITH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WORKER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.XML;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.XMLNS;

/**
 * Parser definition.
 */
public class BallerinaParserDefinition implements ParserDefinition {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(LINE_COMMENT);

    public static final TokenSet STRINGS = TokenSet.create(QUOTED_STRING_LITERAL);

    public static final TokenSet NUMBERS = TokenSet
            .create(DECIMAL_INTEGER_LITERAL, HEX_INTEGER_LITERAL, OCTAL_INTEGER_LITERAL, BINARY_INTEGER_LITERAL,
                    FLOATING_POINT_LITERAL);

    // excluding keywords "foreach" and "map", which are also used as Iterable operations.
    public static final TokenSet KEYWORDS = TokenSet
            .create(ABORT, ABORTED, ABSTRACT, ANNOTATION, ANY, ANYDATA, BOOLEAN, BREAK, BYTE, CATCH, CHANNEL,
                    CHECK, CHECKPANIC, CLIENT, COMMITTED, CONST, CONTINUE, DECIMAL, ELSE, ERROR, EXTERNAL,
                    FAIL, FINAL, FINALLY, FLOAT, FLUSH, FORK, FUNCTION, FUTURE, HANDLE, IF, IMPORT, IN, INT, IS,
                    JSON, LISTENER, LOCK, MATCH, NEW, OBJECT, OBJECT_INIT, ONRETRY, PARAMETER, PANIC, PRIVATE, PUBLIC,
                    RECORD, REMOTE, RESOURCE, RETRIES, RETRY, RETURN, RETURNS, SERVICE, SOURCE, START, STREAM, STRING,
                    TABLE, TRANSACTION, TRY, TYPE, TYPEDESC, TYPEOF, TRAP, THROW, WAIT, WHILE, WITH, WORKER, VAR,
                    VERSION, XML, XMLNS, BOOLEAN_LITERAL, NULL_LITERAL, FROM, ON, SELECT, FOREVER);

    public static final TokenSet OPERATORS = TokenSet.create(ELVIS, EQUAL_GT, LARROW, RARROW, SYNCRARROW);

    public static final TokenSet BAD_CHARACTER = TokenSet.create(TokenType.BAD_CHARACTER);

    public static final TokenSet ERROR_ELEMENT = TokenSet.create(TokenType.ERROR_ELEMENT);

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
        return BallerinaFileElementType.INSTANCE;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new BallerinaFile(viewProvider);
    }

    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return BallerinaTypes.Factory.createElement(node);
    }
}
