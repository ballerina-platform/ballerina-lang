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

import java.util.List;

import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.*;

public class BallerinaTypes {

    private BallerinaTypes() {

    }

    public static final List<RuleIElementType> ruleIElementTypes =
            PSIElementTypeFactory.getRuleIElementTypes(BallerinaLanguage.INSTANCE);

    public static final List<TokenIElementType> tokenIElementTypes =
            PSIElementTypeFactory.getTokenIElementTypes(BallerinaLanguage.INSTANCE);

    public static final TokenIElementType IDENTIFIER = tokenIElementTypes.get(BallerinaLexer.Identifier);
    public static final TokenIElementType COMMENT_STATEMENT = tokenIElementTypes.get(BallerinaLexer.LINE_COMMENT);
    public static final TokenIElementType QUOTED_STRING = tokenIElementTypes.get(BallerinaLexer.QuotedStringLiteral);
    public static final TokenIElementType BACKTICKED_STRING = tokenIElementTypes.get(BallerinaLexer
            .BacktickStringLiteral);
    public static final TokenIElementType ERRCHAR = tokenIElementTypes.get(BallerinaLexer.ERRCHAR);
    public static final TokenIElementType SINGLE_QUOTE = tokenIElementTypes.get(BallerinaLexer.SINGLEQUOTE);
    public static final TokenIElementType DOUBLE_QUOTE = tokenIElementTypes.get(BallerinaLexer.DOUBLEQUOTE);
    public static final TokenIElementType BACK_TICK = tokenIElementTypes.get(BallerinaLexer.BACKTICK);

    public static final RuleIElementType STATEMENT = ruleIElementTypes.get(RULE_statement);

    public static final RuleIElementType FUNCTION_BODY = ruleIElementTypes.get(RULE_callableUnitBody);
    public static final RuleIElementType CONNECTOR_BODY = ruleIElementTypes.get(RULE_connectorBody);
    public static final RuleIElementType SERVICE_BODY = ruleIElementTypes.get(RULE_serviceBody);
    public static final RuleIElementType STRUCT_BODY = ruleIElementTypes.get(RULE_structBody);
    public static final RuleIElementType ANNOTATION_BODY = ruleIElementTypes.get(RULE_annotationBody);

    public static final RuleIElementType FUNCTION_DEFINITION = ruleIElementTypes.get(RULE_functionDefinition);
    public static final RuleIElementType SERVICE_DEFINITION = ruleIElementTypes.get(RULE_serviceDefinition);
    public static final RuleIElementType RESOURCE_DEFINITION = ruleIElementTypes.get(RULE_resourceDefinition);
    public static final RuleIElementType CONNECTOR_DEFINITION =
            ruleIElementTypes.get(RULE_connectorDefinition);
    public static final RuleIElementType ACTION_DEFINITION = ruleIElementTypes.get(RULE_actionDefinition);
    public static final RuleIElementType STRUCT_DEFINITION = ruleIElementTypes.get(RULE_structDefinition);
    public static final RuleIElementType TYPE_MAPPER_DEFINTION =
            ruleIElementTypes.get(RULE_typeMapperDefinition);

    public static final RuleIElementType IF_ELSE_STATEMENT = ruleIElementTypes.get(RULE_ifElseStatement);
    public static final RuleIElementType ITERATE_STATEMENT = ruleIElementTypes.get(RULE_iterateStatement);
    public static final RuleIElementType WHILE_STATEMENT = ruleIElementTypes.get(RULE_whileStatement);
    public static final RuleIElementType TRY_CATCH_STATEMENT = ruleIElementTypes.get(RULE_tryCatchStatement);
    public static final RuleIElementType TYPE_MAPPER_BODY = ruleIElementTypes.get(RULE_typeMapperBody);
    public static final RuleIElementType WORKER_DECLARATION = ruleIElementTypes.get(RULE_workerDeclaration);
    public static final RuleIElementType WORKER_BODY = ruleIElementTypes.get(RULE_workerBody);
    public static final RuleIElementType FORK_JOIN_STATEMENT = ruleIElementTypes.get(RULE_forkJoinStatement);
    public static final RuleIElementType JOIN_CONDITIONS = ruleIElementTypes.get(RULE_joinConditions);
    public static final RuleIElementType TRANSACTION_STATEMENT = ruleIElementTypes.get(RULE_transactionStatement);
    public static final RuleIElementType TRANSFORM_STATEMENT = ruleIElementTypes.get(RULE_transformStatement);
    public static final RuleIElementType TRANSFORM_STATEMENT_BODY =
            ruleIElementTypes.get(RULE_transformStatementBody);
    public static final RuleIElementType XML_LOCAL_NAME = ruleIElementTypes.get(RULE_xmlLocalName);
    public static final RuleIElementType NAME_REFERENCE = ruleIElementTypes.get(RULE_nameReference);
    public static final RuleIElementType VARIABLE_REFERENCE = ruleIElementTypes.get(RULE_variableReference);
    public static final RuleIElementType FUNCTION_REFERENCE = ruleIElementTypes.get(RULE_functionReference);
    public static final RuleIElementType CONNECTOR_REFERENCE = ruleIElementTypes.get(RULE_connectorReference);
    public static final RuleIElementType ANNOTATION_REFERENCE = ruleIElementTypes.get(RULE_annotationReference);

    public static final RuleIElementType INDEX = ruleIElementTypes.get(RULE_index);
    public static final RuleIElementType FIELD = ruleIElementTypes.get(RULE_field);
    public static final RuleIElementType XML_ATTRIB = ruleIElementTypes.get(RULE_xmlAttrib);

    public static final RuleIElementType PARAMETER_LIST = ruleIElementTypes.get(RULE_parameterList);
    public static final RuleIElementType ANNOTATION_ATTACHMENT =
            ruleIElementTypes.get(RULE_annotationAttachment);
    public static final RuleIElementType ATTACHMENT_POINT = ruleIElementTypes.get(RULE_attachmentPoint);
    public static final RuleIElementType ANNOTATION_ATTRIBUTE_LIST =
            ruleIElementTypes.get(RULE_annotationAttributeList);
    public static final RuleIElementType MAP_STRUCT_LITERAL = ruleIElementTypes.get(RULE_mapStructLiteral);
    public static final RuleIElementType MAP_STRUCT_KEY_VALUE = ruleIElementTypes.get(RULE_mapStructKeyValue);

    public static final RuleIElementType EXPRESSION_LIST = ruleIElementTypes.get(RULE_expressionList);
    public static final RuleIElementType RETURN_TYPE_LIST = ruleIElementTypes.get(RULE_returnTypeList);
    public static final RuleIElementType TYPE_NAME = ruleIElementTypes.get(RULE_typeName);
    public static final RuleIElementType EXPRESSION = ruleIElementTypes.get(RULE_expression);
    public static final RuleIElementType SIMPLE_EXPRESSION = ruleIElementTypes.get(RULE_simpleExpression);
    public static final RuleIElementType RETURN_PARAMETERS = ruleIElementTypes.get(RULE_returnParameters);
    public static final RuleIElementType VALUE_TYPE_NAME = ruleIElementTypes.get(RULE_valueTypeName);
    public static final RuleIElementType ACTION_INVOCATION = ruleIElementTypes.get(RULE_actionInvocation);
    public static final RuleIElementType XML_NAMESPACE_NAME = ruleIElementTypes.get(RULE_xmlNamespaceName);
    public static final RuleIElementType SOURCE_NOTATION = ruleIElementTypes.get(RULE_sourceNotation);
    public static final RuleIElementType PACKAGE_NAME = ruleIElementTypes.get(RULE_packageName);

    // Keywords
    public static final TokenIElementType ALL = tokenIElementTypes.get(BallerinaLexer.ALL);
    public static final TokenIElementType ABORTED = tokenIElementTypes.get(BallerinaLexer.ABORTED);
    public static final TokenIElementType ACTION = tokenIElementTypes.get(BallerinaLexer.ACTION);
    public static final TokenIElementType ANNOTATION = tokenIElementTypes.get(BallerinaLexer.ANNOTATION);
    public static final TokenIElementType ANY = tokenIElementTypes.get(BallerinaLexer.ANY);
    public static final TokenIElementType AS = tokenIElementTypes.get(BallerinaLexer.AS);
    public static final TokenIElementType ATTACH = tokenIElementTypes.get(BallerinaLexer.ATTACH);
    public static final TokenIElementType BREAK = tokenIElementTypes.get(BallerinaLexer.BREAK);
    public static final TokenIElementType CATCH = tokenIElementTypes.get(BallerinaLexer.CATCH);
    public static final TokenIElementType COMMITTED = tokenIElementTypes.get(BallerinaLexer.COMMITTED);
    public static final TokenIElementType CONNECTOR = tokenIElementTypes.get(BallerinaLexer.CONNECTOR);
    public static final TokenIElementType CONST = tokenIElementTypes.get(BallerinaLexer.CONST);
    public static final TokenIElementType CREATE = tokenIElementTypes.get(BallerinaLexer.CREATE);
    public static final TokenIElementType ELSE = tokenIElementTypes.get(BallerinaLexer.ELSE);
    public static final TokenIElementType FINALLY = tokenIElementTypes.get(BallerinaLexer.FINALLY);
    public static final TokenIElementType FORK = tokenIElementTypes.get(BallerinaLexer.FORK);
    public static final TokenIElementType FUNCTION = tokenIElementTypes.get(BallerinaLexer.FUNCTION);
    public static final TokenIElementType IF = tokenIElementTypes.get(BallerinaLexer.IF);
    public static final TokenIElementType IMPORT = tokenIElementTypes.get(BallerinaLexer.IMPORT);
    public static final TokenIElementType ITERATE = tokenIElementTypes.get(BallerinaLexer.ITERATE);
    public static final TokenIElementType JOIN = tokenIElementTypes.get(BallerinaLexer.JOIN);
    public static final TokenIElementType JSON = tokenIElementTypes.get(BallerinaLexer.JSON);
    public static final TokenIElementType MAP = tokenIElementTypes.get(BallerinaLexer.MAP);
    public static final TokenIElementType MESSAGE = tokenIElementTypes.get(BallerinaLexer.MESSAGE);
    public static final TokenIElementType NATIVE = tokenIElementTypes.get(BallerinaLexer.NATIVE);
    public static final TokenIElementType PACKAGE = tokenIElementTypes.get(BallerinaLexer.PACKAGE);
    public static final TokenIElementType REPLY = tokenIElementTypes.get(BallerinaLexer.REPLY);
    public static final TokenIElementType RESOURCE = tokenIElementTypes.get(BallerinaLexer.RESOURCE);
    public static final TokenIElementType RETURN = tokenIElementTypes.get(BallerinaLexer.RETURN);
    public static final TokenIElementType SERVICE = tokenIElementTypes.get(BallerinaLexer.SERVICE);
    public static final TokenIElementType STRUCT = tokenIElementTypes.get(BallerinaLexer.STRUCT);
    public static final TokenIElementType THROW = tokenIElementTypes.get(BallerinaLexer.THROW);
    public static final TokenIElementType TIMEOUT = tokenIElementTypes.get(BallerinaLexer.TIMEOUT);
    public static final TokenIElementType TRANSACTION = tokenIElementTypes.get(BallerinaLexer.TRANSACTION);
    public static final TokenIElementType TRY = tokenIElementTypes.get(BallerinaLexer.TRY);
    public static final TokenIElementType TYPEMAPPER = tokenIElementTypes.get(BallerinaLexer.TYPEMAPPER);
    public static final TokenIElementType VAR = tokenIElementTypes.get(BallerinaLexer.VAR);
    public static final TokenIElementType WHILE = tokenIElementTypes.get(BallerinaLexer.WHILE);
    public static final TokenIElementType WORKER = tokenIElementTypes.get(BallerinaLexer.WORKER);
    public static final TokenIElementType XMLNS = tokenIElementTypes.get(BallerinaLexer.XMLNS);
    public static final TokenIElementType XML = tokenIElementTypes.get(BallerinaLexer.XML);
    public static final TokenIElementType XML_DOCUMENT = tokenIElementTypes.get(BallerinaLexer.XML_DOCUMENT);

    // Other tokens
    public static final TokenIElementType SENDARROW = tokenIElementTypes.get(BallerinaLexer.SENDARROW);
    public static final TokenIElementType RECEIVEARROW = tokenIElementTypes.get(BallerinaLexer.RECEIVEARROW);

    public static final TokenIElementType LPAREN = tokenIElementTypes.get(BallerinaLexer.LPAREN);
    public static final TokenIElementType RPAREN = tokenIElementTypes.get(BallerinaLexer.RPAREN);
    public static final TokenIElementType LBRACE = tokenIElementTypes.get(BallerinaLexer.LBRACE);
    public static final TokenIElementType RBRACE = tokenIElementTypes.get(BallerinaLexer.RBRACE);
    public static final TokenIElementType LBRACK = tokenIElementTypes.get(BallerinaLexer.LBRACK);
    public static final TokenIElementType RBRACK = tokenIElementTypes.get(BallerinaLexer.RBRACK);
    public static final TokenIElementType SEMI = tokenIElementTypes.get(BallerinaLexer.SEMI);
    public static final TokenIElementType COMMA = tokenIElementTypes.get(BallerinaLexer.COMMA);
    public static final TokenIElementType DOT = tokenIElementTypes.get(BallerinaLexer.DOT);

    public static final TokenIElementType ASSIGN = tokenIElementTypes.get(BallerinaLexer.ASSIGN);
    public static final TokenIElementType GT = tokenIElementTypes.get(BallerinaLexer.GT);
    public static final TokenIElementType LT = tokenIElementTypes.get(BallerinaLexer.LT);
    public static final TokenIElementType BANG = tokenIElementTypes.get(BallerinaLexer.BANG);
    public static final TokenIElementType TILDE = tokenIElementTypes.get(BallerinaLexer.TILDE);
    public static final TokenIElementType COLON = tokenIElementTypes.get(BallerinaLexer.COLON);
    public static final TokenIElementType EQUAL = tokenIElementTypes.get(BallerinaLexer.EQUAL);
    public static final TokenIElementType LE = tokenIElementTypes.get(BallerinaLexer.LE);
    public static final TokenIElementType GE = tokenIElementTypes.get(BallerinaLexer.GE);
    public static final TokenIElementType NOTEQUAL = tokenIElementTypes.get(BallerinaLexer.NOTEQUAL);
    public static final TokenIElementType AND = tokenIElementTypes.get(BallerinaLexer.AND);
    public static final TokenIElementType OR = tokenIElementTypes.get(BallerinaLexer.OR);
    public static final TokenIElementType ADD = tokenIElementTypes.get(BallerinaLexer.ADD);
    public static final TokenIElementType SUB = tokenIElementTypes.get(BallerinaLexer.SUB);
    public static final TokenIElementType MUL = tokenIElementTypes.get(BallerinaLexer.MUL);
    public static final TokenIElementType DIV = tokenIElementTypes.get(BallerinaLexer.DIV);
    public static final TokenIElementType BITAND = tokenIElementTypes.get(BallerinaLexer.BITAND);
    public static final TokenIElementType BITOR = tokenIElementTypes.get(BallerinaLexer.BITOR);
    public static final TokenIElementType CARET = tokenIElementTypes.get(BallerinaLexer.CARET);
    public static final TokenIElementType MOD = tokenIElementTypes.get(BallerinaLexer.MOD);
    public static final TokenIElementType AT = tokenIElementTypes.get(BallerinaLexer.AT);

    public static final TokenIElementType INTEGER_LITERAL = tokenIElementTypes.get(BallerinaLexer.IntegerLiteral);

    public static final TokenSet OPERATORS = TokenSet.create(ASSIGN, EQUAL, LE, GE, NOTEQUAL, AND, OR, MUL, DIV,
            BITAND, BITOR, CARET, MOD);
}
