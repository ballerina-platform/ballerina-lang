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

    private BallerinaTypes() {

    }

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
    public static RuleIElementType STRUCT_BODY = ruleIElementTypes.get(BallerinaParser.RULE_structBody);
    public static RuleIElementType ANNOTATION_BODY = ruleIElementTypes.get(BallerinaParser.RULE_annotationBody);

    public static RuleIElementType FUNCTION_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_functionDefinition);
    public static RuleIElementType SERVICE_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_serviceDefinition);
    public static RuleIElementType RESOURCE_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_resourceDefinition);
    public static RuleIElementType CONNECTOR_DEFINITION =
            ruleIElementTypes.get(BallerinaParser.RULE_connectorDefinition);
    public static RuleIElementType ACTION_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_actionDefinition);
    public static RuleIElementType STRUCT_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_structDefinition);
    public static RuleIElementType TYPE_MAPPER_DEFINTION =
            ruleIElementTypes.get(BallerinaParser.RULE_typeMapperDefinition);

    public static RuleIElementType IF_ELSE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_ifElseStatement);
    public static RuleIElementType ITERATE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_iterateStatement);
    public static RuleIElementType WHILE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_whileStatement);
    public static RuleIElementType TRY_CATCH_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_tryCatchStatement);
    public static RuleIElementType TYPE_MAPPER_BODY = ruleIElementTypes.get(BallerinaParser.RULE_typeMapperBody);
    public static RuleIElementType WORKER_DECLARATION = ruleIElementTypes.get(BallerinaParser.RULE_workerDeclaration);
    public static RuleIElementType WORKER_BODY = ruleIElementTypes.get(BallerinaParser.RULE_workerBody);
    public static RuleIElementType FORK_JOIN_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_forkJoinStatement);
    public static RuleIElementType JOIN_CONDITIONS = ruleIElementTypes.get(BallerinaParser.RULE_joinConditions);
    public static RuleIElementType TRANSACTION_STATEMENT = ruleIElementTypes.get(BallerinaParser
            .RULE_transactionStatement);
    public static RuleIElementType TRANSFORM_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_transformStatement);
    public static RuleIElementType TRANSFORM_STATEMENT_BODY =
            ruleIElementTypes.get(BallerinaParser.RULE_transformStatementBody);
    public static RuleIElementType XML_LOCAL_NAME = ruleIElementTypes.get(BallerinaParser.RULE_xmlLocalName);
    public static RuleIElementType NAME_REFERENCE = ruleIElementTypes.get(BallerinaParser.RULE_nameReference);

    public static RuleIElementType PARAMETER_LIST = ruleIElementTypes.get(BallerinaParser.RULE_parameterList);
    public static RuleIElementType ANNOTATION_ATTACHMENT =
            ruleIElementTypes.get(BallerinaParser.RULE_annotationAttachment);
    public static RuleIElementType ATTACHMENT_POINT = ruleIElementTypes.get(BallerinaParser.RULE_attachmentPoint);
    public static RuleIElementType ANNOTATION_ATTRIBUTE_LIST =
            ruleIElementTypes.get(BallerinaParser.RULE_annotationAttributeList);
    public static RuleIElementType MAP_STRUCT_LITERAL = ruleIElementTypes.get(BallerinaParser.RULE_mapStructLiteral);
    public static RuleIElementType MAP_STRUCT_KEY_VALUE = ruleIElementTypes.get(BallerinaParser.RULE_mapStructKeyValue);

    public static RuleIElementType EXPRESSION_LIST = ruleIElementTypes.get(BallerinaParser.RULE_expressionList);
    public static RuleIElementType RETURN_TYPE_LIST = ruleIElementTypes.get(BallerinaParser.RULE_returnTypeList);
    public static RuleIElementType TYPE_NAME = ruleIElementTypes.get(BallerinaParser.RULE_typeName);
    public static RuleIElementType EXPRESSION = ruleIElementTypes.get(BallerinaParser.RULE_expression);
    public static RuleIElementType SIMPLE_EXPRESSION = ruleIElementTypes.get(BallerinaParser.RULE_simpleExpression);
    public static RuleIElementType RETURN_PARAMETERS = ruleIElementTypes.get(BallerinaParser.RULE_returnParameters);
    public static RuleIElementType VALUE_TYPE_NAME = ruleIElementTypes.get(BallerinaParser.RULE_valueTypeName);
    public static RuleIElementType ACTION_INVOCATION = ruleIElementTypes.get(BallerinaParser.RULE_actionInvocation);
    public static RuleIElementType XML_NAMESPACE_NAME = ruleIElementTypes.get(BallerinaParser.RULE_xmlNamespaceName);
    public static RuleIElementType SOURCE_NOTATION = ruleIElementTypes.get(BallerinaParser.RULE_sourceNotation);
    public static RuleIElementType PACKAGE_NAME = ruleIElementTypes.get(BallerinaParser.RULE_packageName);

    // Keywords
    public static TokenIElementType ALL = tokenIElementTypes.get(BallerinaLexer.ALL);
    public static TokenIElementType ABORTED = tokenIElementTypes.get(BallerinaLexer.ABORTED);
    public static TokenIElementType ACTION = tokenIElementTypes.get(BallerinaLexer.ACTION);
    public static TokenIElementType ANNOTATION = tokenIElementTypes.get(BallerinaLexer.ANNOTATION);
    public static TokenIElementType ANY = tokenIElementTypes.get(BallerinaLexer.ANY);
    public static TokenIElementType AS = tokenIElementTypes.get(BallerinaLexer.AS);
    public static TokenIElementType ATTACH = tokenIElementTypes.get(BallerinaLexer.ATTACH);
    public static TokenIElementType BREAK = tokenIElementTypes.get(BallerinaLexer.BREAK);
    public static TokenIElementType CATCH = tokenIElementTypes.get(BallerinaLexer.CATCH);
    public static TokenIElementType COMMITTED = tokenIElementTypes.get(BallerinaLexer.COMMITTED);
    public static TokenIElementType CONNECTOR = tokenIElementTypes.get(BallerinaLexer.CONNECTOR);
    public static TokenIElementType CONST = tokenIElementTypes.get(BallerinaLexer.CONST);
    public static TokenIElementType CREATE = tokenIElementTypes.get(BallerinaLexer.CREATE);
    public static TokenIElementType ELSE = tokenIElementTypes.get(BallerinaLexer.ELSE);
    public static TokenIElementType FINALLY = tokenIElementTypes.get(BallerinaLexer.FINALLY);
    public static TokenIElementType FORK = tokenIElementTypes.get(BallerinaLexer.FORK);
    public static TokenIElementType FUNCTION = tokenIElementTypes.get(BallerinaLexer.FUNCTION);
    public static TokenIElementType IF = tokenIElementTypes.get(BallerinaLexer.IF);
    public static TokenIElementType IMPORT = tokenIElementTypes.get(BallerinaLexer.IMPORT);
    public static TokenIElementType ITERATE = tokenIElementTypes.get(BallerinaLexer.ITERATE);
    public static TokenIElementType JOIN = tokenIElementTypes.get(BallerinaLexer.JOIN);
    public static TokenIElementType JSON = tokenIElementTypes.get(BallerinaLexer.JSON);
    public static TokenIElementType MAP = tokenIElementTypes.get(BallerinaLexer.MAP);
    public static TokenIElementType MESSAGE = tokenIElementTypes.get(BallerinaLexer.MESSAGE);
    public static TokenIElementType NATIVE = tokenIElementTypes.get(BallerinaLexer.NATIVE);
    public static TokenIElementType PACKAGE = tokenIElementTypes.get(BallerinaLexer.PACKAGE);
    public static TokenIElementType REPLY = tokenIElementTypes.get(BallerinaLexer.REPLY);
    public static TokenIElementType RESOURCE = tokenIElementTypes.get(BallerinaLexer.RESOURCE);
    public static TokenIElementType RETURN = tokenIElementTypes.get(BallerinaLexer.RETURN);
    public static TokenIElementType SERVICE = tokenIElementTypes.get(BallerinaLexer.SERVICE);
    public static TokenIElementType STRUCT = tokenIElementTypes.get(BallerinaLexer.STRUCT);
    public static TokenIElementType THROW = tokenIElementTypes.get(BallerinaLexer.THROW);
    public static TokenIElementType TIMEOUT = tokenIElementTypes.get(BallerinaLexer.TIMEOUT);
    public static TokenIElementType TRANSACTION = tokenIElementTypes.get(BallerinaLexer.TRANSACTION);
    public static TokenIElementType TRY = tokenIElementTypes.get(BallerinaLexer.TRY);
    public static TokenIElementType TYPEMAPPER = tokenIElementTypes.get(BallerinaLexer.TYPEMAPPER);
    public static TokenIElementType VAR = tokenIElementTypes.get(BallerinaLexer.VAR);
    public static TokenIElementType WHILE = tokenIElementTypes.get(BallerinaLexer.WHILE);
    public static TokenIElementType WORKER = tokenIElementTypes.get(BallerinaLexer.WORKER);
    public static TokenIElementType XML = tokenIElementTypes.get(BallerinaLexer.XML);
    public static TokenIElementType XML_DOCUMENT = tokenIElementTypes.get(BallerinaLexer.XML_DOCUMENT);

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

    public static TokenIElementType INTEGER_LITERAL = tokenIElementTypes.get(BallerinaLexer.IntegerLiteral);

    public static final TokenSet OPERATORS = TokenSet.create(ASSIGN, EQUAL, LE, GE, NOTEQUAL, AND, OR, MUL, DIV,
            BITAND, BITOR, CARET, MOD);
}
