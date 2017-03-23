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

import com.intellij.psi.tree.TokenSet;
import org.antlr.jetbrains.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
import org.ballerinalang.plugins.idea.grammar.BallerinaParser;

import java.util.List;

public class BallerinaTypes {

    public static List<RuleIElementType> ruleIElementTypes =
            PSIElementTypeFactory.getRuleIElementTypes(BallerinaLanguage.INSTANCE);

    public static List<TokenIElementType> tokenIElementTypes =
            PSIElementTypeFactory.getTokenIElementTypes(BallerinaLanguage.INSTANCE);

    public static TokenIElementType IDENTIFIER = tokenIElementTypes.get(BallerinaLexer.Identifier);
    public static TokenIElementType COMMENT_STATEMENT = tokenIElementTypes.get(BallerinaLexer.LINE_COMMENT);
    public static TokenIElementType QUOTED_STRING = tokenIElementTypes.get(BallerinaLexer.QuotedStringLiteral);
    public static TokenIElementType BACKTICKED_STRING = tokenIElementTypes.get(BallerinaLexer.BacktickStringLiteral);
    public static TokenIElementType ERRCHAR = tokenIElementTypes.get(BallerinaLexer.ERRCHAR);
    public static TokenIElementType SINGLE_QUOTE = tokenIElementTypes.get(BallerinaLexer.SINGLEQUOTE);
    public static TokenIElementType DOUBLE_QUOTE = tokenIElementTypes.get(BallerinaLexer.DOUBLEQUOTE);
    public static TokenIElementType BACK_TICK = tokenIElementTypes.get(BallerinaLexer.BACKTICK);

    public static RuleIElementType STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_statement);

    public static RuleIElementType FUNCTION_BODY = ruleIElementTypes.get(BallerinaParser.RULE_callableUnitBody);
    public static RuleIElementType CONNECTOR_BODY = ruleIElementTypes.get(BallerinaParser.RULE_connectorBody);
    public static RuleIElementType SERVICE_BODY = ruleIElementTypes.get(BallerinaParser.RULE_serviceBody);
    public static RuleIElementType STRUCT_BODY = ruleIElementTypes.get(BallerinaParser.RULE_structDefinitionBody);

    public static RuleIElementType FUNCTION_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_functionDefinition);
    public static RuleIElementType CONNECTOR_DEFINITION =
            ruleIElementTypes.get(BallerinaParser.RULE_connectorDefinition);
    public static RuleIElementType SERVICE_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_serviceDefinition);
    public static RuleIElementType STRUCT_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_structDefinition);

    public static RuleIElementType IF_ELSE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_ifElseStatement);
    public static RuleIElementType ITERATE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_iterateStatement);
    public static RuleIElementType WHILE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_whileStatement);
    public static RuleIElementType JOIN_CLAUS = ruleIElementTypes.get(BallerinaParser.RULE_joinClause);
    public static RuleIElementType TIMEOUT_CLAUSE = ruleIElementTypes.get(BallerinaParser.RULE_timeoutClause);
    public static RuleIElementType TRY_CATCH_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_tryCatchStatement);
    public static RuleIElementType TYPE_MAPPER_BODY = ruleIElementTypes.get(BallerinaParser.RULE_typeMapperBody);
    public static RuleIElementType WORKER_DECLARATION = ruleIElementTypes.get(BallerinaParser.RULE_workerDeclaration);

    public static RuleIElementType ARGUMENT_LIST = ruleIElementTypes.get(BallerinaParser.RULE_argumentList);
    public static RuleIElementType PARAMETER_LIST = ruleIElementTypes.get(BallerinaParser.RULE_parameterList);
    public static RuleIElementType EXPRESSION_LIST = ruleIElementTypes.get(BallerinaParser.RULE_expressionList);
    public static RuleIElementType RETURN_TYPE_LIST = ruleIElementTypes.get(BallerinaParser.RULE_returnTypeList);
    public static RuleIElementType TYPE_NAME = ruleIElementTypes.get(BallerinaParser.RULE_typeName);
    public static RuleIElementType ANNOTATION_NAME = ruleIElementTypes.get(BallerinaParser.RULE_annotationName);
    public static RuleIElementType ANNOTATION = ruleIElementTypes.get(BallerinaParser.RULE_annotation);
    public static RuleIElementType EXPRESSION = ruleIElementTypes.get(BallerinaParser.RULE_expression);
    public static RuleIElementType RETURN_PARAMETERS = ruleIElementTypes.get(BallerinaParser.RULE_returnParameters);
    public static RuleIElementType TYPE_MAPPER_INPUT = ruleIElementTypes.get(BallerinaParser.RULE_typeMapperInput);
    public static RuleIElementType TYPE_MAPPER_TYPE = ruleIElementTypes.get(BallerinaParser.RULE_typeMapperType);

    public static RuleIElementType ELEMENT_VALUE_PAIRS = ruleIElementTypes.get(BallerinaParser.RULE_elementValuePairs);
    public static RuleIElementType ELEMENT_VALUE_PAIR = ruleIElementTypes.get(BallerinaParser.RULE_elementValuePair);

    // Keywords
    public static TokenIElementType ALL = tokenIElementTypes.get(BallerinaLexer.ALL);
    public static TokenIElementType ANY = tokenIElementTypes.get(BallerinaLexer.ANY);
    public static TokenIElementType AS = tokenIElementTypes.get(BallerinaLexer.AS);
    public static TokenIElementType BREAK = tokenIElementTypes.get(BallerinaLexer.BREAK);
    public static TokenIElementType CATCH = tokenIElementTypes.get(BallerinaLexer.CATCH);
    public static TokenIElementType CONNECTOR = tokenIElementTypes.get(BallerinaLexer.CONNECTOR);
    public static TokenIElementType CONST = tokenIElementTypes.get(BallerinaLexer.CONST);
    public static TokenIElementType CREATE = tokenIElementTypes.get(BallerinaLexer.CREATE);
    public static TokenIElementType ELSE = tokenIElementTypes.get(BallerinaLexer.ELSE);
    public static TokenIElementType FORK = tokenIElementTypes.get(BallerinaLexer.FORK);
    public static TokenIElementType FUNCTION = tokenIElementTypes.get(BallerinaLexer.FUNCTION);
    public static TokenIElementType IF = tokenIElementTypes.get(BallerinaLexer.IF);
    public static TokenIElementType IMPORT = tokenIElementTypes.get(BallerinaLexer.IMPORT);
    public static TokenIElementType ITERATE = tokenIElementTypes.get(BallerinaLexer.ITERATE);
    public static TokenIElementType JOIN = tokenIElementTypes.get(BallerinaLexer.JOIN);
    public static TokenIElementType NULL = tokenIElementTypes.get(BallerinaLexer.NULL);
    public static TokenIElementType PACKAGE = tokenIElementTypes.get(BallerinaLexer.PACKAGE);
    public static TokenIElementType REPLY = tokenIElementTypes.get(BallerinaLexer.REPLY);
    public static TokenIElementType RESOURCE = tokenIElementTypes.get(BallerinaLexer.RESOURCE);
    public static TokenIElementType RETURN = tokenIElementTypes.get(BallerinaLexer.RETURN);
    public static TokenIElementType SERVICE = tokenIElementTypes.get(BallerinaLexer.SERVICE);
    public static TokenIElementType STRUCT = tokenIElementTypes.get(BallerinaLexer.STRUCT);
    public static TokenIElementType THROW = tokenIElementTypes.get(BallerinaLexer.THROW);
    public static TokenIElementType THROWS = tokenIElementTypes.get(BallerinaLexer.THROWS);
    public static TokenIElementType TIMEOUT = tokenIElementTypes.get(BallerinaLexer.TIMEOUT);
    public static TokenIElementType TRY = tokenIElementTypes.get(BallerinaLexer.TRY);
    public static TokenIElementType TYPEMAPPER = tokenIElementTypes.get(BallerinaLexer.TYPEMAPPER);
    public static TokenIElementType WHILE = tokenIElementTypes.get(BallerinaLexer.WHILE);
    public static TokenIElementType WORKER = tokenIElementTypes.get(BallerinaLexer.WORKER);

    // Other tokens
    public static TokenIElementType SENDARROW = tokenIElementTypes.get(BallerinaLexer.SENDARROW);
    public static TokenIElementType RECEIVEARROW = tokenIElementTypes.get(BallerinaLexer.RECEIVEARROW);

    public static TokenIElementType LPAREN = tokenIElementTypes.get(BallerinaLexer.LPAREN);
    public static TokenIElementType RPAREN = tokenIElementTypes.get(BallerinaLexer.RPAREN);
    public static TokenIElementType LBRACE = tokenIElementTypes.get(BallerinaLexer.LBRACE);
    public static TokenIElementType RBRACE = tokenIElementTypes.get(BallerinaLexer.RBRACE);
    public static TokenIElementType LBRACK = tokenIElementTypes.get(BallerinaLexer.LBRACK);
    public static TokenIElementType RBRACK = tokenIElementTypes.get(BallerinaLexer.RBRACK);
    public static TokenIElementType SEMI = tokenIElementTypes.get(BallerinaLexer.SEMI);
    public static TokenIElementType COMMA = tokenIElementTypes.get(BallerinaLexer.COMMA);
    public static TokenIElementType DOT = tokenIElementTypes.get(BallerinaLexer.DOT);

    public static TokenIElementType ASSIGN = tokenIElementTypes.get(BallerinaLexer.ASSIGN);
    public static TokenIElementType GT = tokenIElementTypes.get(BallerinaLexer.GT);
    public static TokenIElementType LT = tokenIElementTypes.get(BallerinaLexer.LT);
    public static TokenIElementType BANG = tokenIElementTypes.get(BallerinaLexer.BANG);
    public static TokenIElementType TILDE = tokenIElementTypes.get(BallerinaLexer.TILDE);
    public static TokenIElementType COLON = tokenIElementTypes.get(BallerinaLexer.COLON);
    public static TokenIElementType EQUAL = tokenIElementTypes.get(BallerinaLexer.EQUAL);
    public static TokenIElementType LE = tokenIElementTypes.get(BallerinaLexer.LE);
    public static TokenIElementType GE = tokenIElementTypes.get(BallerinaLexer.GE);
    public static TokenIElementType NOTEQUAL = tokenIElementTypes.get(BallerinaLexer.NOTEQUAL);
    public static TokenIElementType AND = tokenIElementTypes.get(BallerinaLexer.AND);
    public static TokenIElementType OR = tokenIElementTypes.get(BallerinaLexer.OR);
    public static TokenIElementType ADD = tokenIElementTypes.get(BallerinaLexer.ADD);
    public static TokenIElementType SUB = tokenIElementTypes.get(BallerinaLexer.SUB);
    public static TokenIElementType MUL = tokenIElementTypes.get(BallerinaLexer.MUL);
    public static TokenIElementType DIV = tokenIElementTypes.get(BallerinaLexer.DIV);
    public static TokenIElementType BITAND = tokenIElementTypes.get(BallerinaLexer.BITAND);
    public static TokenIElementType BITOR = tokenIElementTypes.get(BallerinaLexer.BITOR);
    public static TokenIElementType CARET = tokenIElementTypes.get(BallerinaLexer.CARET);
    public static TokenIElementType MOD = tokenIElementTypes.get(BallerinaLexer.MOD);
    public static TokenIElementType AT = tokenIElementTypes.get(BallerinaLexer.AT);

    public static final TokenSet OPERATORS = TokenSet.create(ASSIGN, GT, LT, EQUAL, LE, GE, NOTEQUAL, AND, OR, ADD,
            SUB, MUL, DIV, BITAND, BITOR, CARET, MOD);
}
