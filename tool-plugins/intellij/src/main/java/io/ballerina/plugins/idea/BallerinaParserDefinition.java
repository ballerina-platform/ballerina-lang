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
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ABSTRACT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ALL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ANNOTATION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ANY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ANYDATA;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ASCENDING;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BINARY_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BOOLEAN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BOOLEAN_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BREAK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BUT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.BYTE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CATCH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CHANNEL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CHECK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CLIENT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CONST;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.CONTINUE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DAY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DAYS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DECIMAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DECIMAL_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DEPRECATED;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DESCENDING;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DONE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.DOUBLE_COLON;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ELSE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ELVIS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ENUM;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.EQUAL_GT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ERROR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.EVENTS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.EVERY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.EXTERN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FAIL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FINAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FINALLY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FIRST;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FLOAT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FLOATING_POINT_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FOLLOWED;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FOR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FOREVER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FORK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FROM;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FULL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FUNCTION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.FUTURE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.GROUP;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.HAVING;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.HEX_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.HOUR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.HOURS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IF;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IMPORT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.INNER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.INT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.IS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.JOIN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.JSON;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LARROW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LAST;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LEFT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LENGTHOF;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LIMIT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LINE_COMMENT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LISTENER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.LOCK;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.MATCH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.MINUTE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.MINUTES;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.MONTH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.MONTHS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.NEW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.NULL_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OBJECT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OBJECT_INIT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OCTAL_INTEGER_LITERAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ON;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ONABORT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ONCOMMIT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ONRETRY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.ORDER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OUTER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.OUTPUT;
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
import static io.ballerina.plugins.idea.psi.BallerinaTypes.RIGHT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SECOND;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SECONDS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SELECT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SERVICE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SET;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.SNAPSHOT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.START;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.STREAM;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.STRING;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TABLE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.THROW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TRANSACTION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TRAP;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TRY;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TYPE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.TYPEDESC;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.UNIDIRECTIONAL;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.UNTAINT;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.VAR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.VERSION;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WHERE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WHILE;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WINDOW;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WITH;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WITHIN;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.WORKER;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.XML;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.XMLNS;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.YEAR;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.YEARS;

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
    // Todo - Annotate ReservedWord in the runtime to prevent highlighting as keywords.
    public static final TokenSet KEYWORDS = TokenSet
            .create(ABORT, ABSTRACT, ALL, ANNOTATION, ANY, ANYDATA, BOOLEAN, BREAK, BUT, BYTE, CATCH, CHANNEL,
                    CHECK, CLIENT, CONST, CONTINUE, DEPRECATED, DECIMAL, DONE, ELSE, ENUM, ERROR, EXTERN, FAIL, FINAL,
                    FINALLY, FLOAT, FORK, FUNCTION, FUTURE, IF, IMPORT, IN, INT, IS, JOIN, JSON, LENGTHOF, LISTENER,
                    LOCK, MATCH, NEW, OBJECT, OBJECT_INIT, ONABORT, ONCOMMIT, ONRETRY, PARAMETER, PANIC, PRIVATE,
                    PUBLIC, RECORD, REMOTE, RESOURCE, RETRIES, RETRY, RETURN, RETURNS, SERVICE, START, STREAM,
                    STRING, TABLE, TRANSACTION, TRY, TYPE, TYPEDESC, TRAP, THROW, UNTAINT, WHILE, WITH, WORKER,
                    VAR, VERSION, XML, XMLNS, BOOLEAN_LITERAL, NULL_LITERAL, FROM, ON, SELECT, GROUP, BY, HAVING, ORDER,
                    WHERE, FOLLOWED, SET, FOR, WINDOW, EVENTS, EVERY, WITHIN, LAST, FIRST, SNAPSHOT, OUTPUT, INNER,
                    OUTER, RIGHT, LEFT, FULL, UNIDIRECTIONAL, SECOND, SECONDS, MINUTE, MINUTES, HOUR, HOURS, DAY, DAYS,
                    MONTH, MONTHS, YEAR, YEARS, FOREVER, LIMIT, ASCENDING, DESCENDING);

    public static final TokenSet OPERATORS = TokenSet.create(DOUBLE_COLON, ELVIS, EQUAL_GT, LARROW, RARROW);

    public static final TokenSet BAD_CHARACTER = TokenSet.create(TokenType.BAD_CHARACTER);

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

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return BallerinaTypes.Factory.createElement(node);
    }
}
