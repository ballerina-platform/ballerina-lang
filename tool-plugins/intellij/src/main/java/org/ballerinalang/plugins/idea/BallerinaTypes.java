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

import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_actionDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationAttachment;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationAttributeList;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationAttributeValue;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_anyIdentifierName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_attachmentPoint;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_callableUnitBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_catchClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_catchClauses;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_codeBlockBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_connectorBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_connectorDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_connectorReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_elseClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_elseIfClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_endpointBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_endpointDeclaration;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_enumFieldList;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_expression;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_expressionList;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_failedClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_field;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_finallyClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_foreachStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_forkJoinStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_functionDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_functionReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_ifClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_ifElseStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_index;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_invocation;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_joinClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_joinConditions;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_nameReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_packageName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_parameterList;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_recordKeyValue;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_recordLiteral;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_resourceDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_returnParameters;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_serviceBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_serviceDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_simpleExpression;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_sourceNotation;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_statement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_structBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_structDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_timeoutClause;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_transactionPropertyInitStatementList;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_transactionStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_transformerInvocation;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_tryCatchStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_typeList;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_typeName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_valueTypeName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_variableReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_whileStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_workerBody;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_workerDeclaration;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_xmlAttrib;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_xmlLocalName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_xmlNamespaceName;

/**
 * Contains Ballerina lexer and parser rules. These are needed for formatting, etc.
 */
public class BallerinaTypes {

    private BallerinaTypes() {

    }

    private static final List<RuleIElementType> ruleIElementTypes =
            PSIElementTypeFactory.getRuleIElementTypes(BallerinaLanguage.INSTANCE);

    private static final List<TokenIElementType> tokenIElementTypes =
            PSIElementTypeFactory.getTokenIElementTypes(BallerinaLanguage.INSTANCE);

    public static final TokenIElementType IDENTIFIER = tokenIElementTypes.get(BallerinaLexer.Identifier);
    public static final TokenIElementType COMMENT_STATEMENT = tokenIElementTypes.get(BallerinaLexer.LINE_COMMENT);
    public static final TokenIElementType QUOTED_STRING = tokenIElementTypes.get(BallerinaLexer.QuotedStringLiteral);
    public static final TokenIElementType FLOATING_POINT = tokenIElementTypes.get(BallerinaLexer.FloatingPointLiteral);
    public static final TokenIElementType INTEGER_LITERAL = tokenIElementTypes.get(BallerinaLexer.IntegerLiteral);
    public static final TokenIElementType ERRCHAR = tokenIElementTypes.get(BallerinaLexer.ERRCHAR);
    public static final TokenIElementType DOUBLE_QUOTE = tokenIElementTypes.get(BallerinaLexer.DOUBLEQUOTE);
    public static final TokenIElementType BACK_TICK = tokenIElementTypes.get(BallerinaLexer.BACKTICK);

    public static final TokenIElementType STRING_TEMPLATE_LITERAL_START =
            tokenIElementTypes.get(BallerinaLexer.StringTemplateLiteralStart);
    public static final TokenIElementType STRING_TEMPLATE_EXPRESSION_START =
            tokenIElementTypes.get(BallerinaLexer.StringTemplateExpressionStart);
    public static final TokenIElementType STRING_TEMPLATE_TEXT =
            tokenIElementTypes.get(BallerinaLexer.StringTemplateText);

    public static final TokenIElementType XML_START = tokenIElementTypes.get(BallerinaLexer.XMLStart);
    public static final TokenIElementType XML_EXPRESSION_START =
            tokenIElementTypes.get(BallerinaLexer.XMLExpressionStart);
    public static final TokenIElementType XML_TEXT = tokenIElementTypes.get(BallerinaLexer.XMLText);

    public static final TokenIElementType EXPRESSION_END =
            tokenIElementTypes.get(BallerinaLexer.ExpressionEnd);

    public static final TokenIElementType DOCUMENTATION_TEMPLATE_START =
            tokenIElementTypes.get(BallerinaLexer.DocumentationTemplateStart);
    public static final TokenIElementType DOCUMENTATION_TEMPLATE_ATTRIBUTE_END =
            tokenIElementTypes.get(BallerinaLexer.DocumentationTemplateAttributeEnd);
    public static final TokenIElementType INLINE_CODE = tokenIElementTypes.get(BallerinaLexer.InlineCode);

    public static final RuleIElementType STATEMENT = ruleIElementTypes.get(RULE_statement);

    public static final RuleIElementType FUNCTION_BODY = ruleIElementTypes.get(RULE_callableUnitBody);
    public static final RuleIElementType CONNECTOR_BODY = ruleIElementTypes.get(RULE_connectorBody);
    public static final RuleIElementType SERVICE_BODY = ruleIElementTypes.get(RULE_serviceBody);
    public static final RuleIElementType STRUCT_BODY = ruleIElementTypes.get(RULE_structBody);
    public static final RuleIElementType ANNOTATION_BODY = ruleIElementTypes.get(RULE_annotationBody);

    public static final RuleIElementType FUNCTION_DEFINITION = ruleIElementTypes.get(RULE_functionDefinition);
    public static final RuleIElementType SERVICE_DEFINITION = ruleIElementTypes.get(RULE_serviceDefinition);
    public static final RuleIElementType RESOURCE_DEFINITION = ruleIElementTypes.get(RULE_resourceDefinition);
    public static final RuleIElementType CONNECTOR_DEFINITION = ruleIElementTypes.get(RULE_connectorDefinition);
    public static final RuleIElementType ACTION_DEFINITION = ruleIElementTypes.get(RULE_actionDefinition);
    public static final RuleIElementType STRUCT_DEFINITION = ruleIElementTypes.get(RULE_structDefinition);

    public static final RuleIElementType IF_ELSE_STATEMENT = ruleIElementTypes.get(RULE_ifElseStatement);
    public static final RuleIElementType IF_CLAUSE = ruleIElementTypes.get(RULE_ifClause);
    public static final RuleIElementType ELSE_IF_CLAUSE = ruleIElementTypes.get(RULE_elseIfClause);
    public static final RuleIElementType ELSE_CLAUSE = ruleIElementTypes.get(RULE_elseClause);
    public static final RuleIElementType CODE_BLOCK_BODY = ruleIElementTypes.get(RULE_codeBlockBody);
    public static final RuleIElementType FOREACH_STATEMENT = ruleIElementTypes.get(RULE_foreachStatement);
    public static final RuleIElementType WHILE_STATEMENT = ruleIElementTypes.get(RULE_whileStatement);
    public static final RuleIElementType TRY_CATCH_STATEMENT = ruleIElementTypes.get(RULE_tryCatchStatement);
    public static final RuleIElementType CATCH_CLAUSE = ruleIElementTypes.get(RULE_catchClause);
    public static final RuleIElementType CATCH_CLAUSES = ruleIElementTypes.get(RULE_catchClauses);
    public static final RuleIElementType FINALLY_CLAUSE = ruleIElementTypes.get(RULE_finallyClause);
    public static final RuleIElementType WORKER_DECLARATION = ruleIElementTypes.get(RULE_workerDeclaration);
    public static final RuleIElementType WORKER_BODY = ruleIElementTypes.get(RULE_workerBody);
    public static final RuleIElementType FORK_JOIN_STATEMENT = ruleIElementTypes.get(RULE_forkJoinStatement);
    public static final RuleIElementType JOIN_CLAUSE = ruleIElementTypes.get(RULE_joinClause);
    public static final RuleIElementType TIMEOUT_CLAUSE = ruleIElementTypes.get(RULE_timeoutClause);
    public static final RuleIElementType JOIN_CONDITIONS = ruleIElementTypes.get(RULE_joinConditions);
    public static final RuleIElementType TRANSACTION_STATEMENT = ruleIElementTypes.get(RULE_transactionStatement);
    public static final RuleIElementType FAILED_CLAUSE = ruleIElementTypes.get(RULE_failedClause);
    public static final RuleIElementType XML_LOCAL_NAME = ruleIElementTypes.get(RULE_xmlLocalName);
    public static final RuleIElementType NAME_REFERENCE = ruleIElementTypes.get(RULE_nameReference);
    public static final RuleIElementType VARIABLE_REFERENCE = ruleIElementTypes.get(RULE_variableReference);
    public static final RuleIElementType FUNCTION_REFERENCE = ruleIElementTypes.get(RULE_functionReference);
    public static final RuleIElementType CONNECTOR_REFERENCE = ruleIElementTypes.get(RULE_connectorReference);
    public static final RuleIElementType ANNOTATION_REFERENCE = ruleIElementTypes.get(RULE_annotationReference);
    public static final RuleIElementType ENDPOINT_DECLARATION = ruleIElementTypes.get(RULE_endpointDeclaration);
    public static final RuleIElementType ENDPOINT_BODY = ruleIElementTypes.get(RULE_endpointBody);
    public static final RuleIElementType TRANSFORMER_INVOCATION = ruleIElementTypes.get(RULE_transformerInvocation);

    public static final RuleIElementType INDEX = ruleIElementTypes.get(RULE_index);
    public static final RuleIElementType FIELD = ruleIElementTypes.get(RULE_field);
    public static final RuleIElementType XML_ATTRIB = ruleIElementTypes.get(RULE_xmlAttrib);

    public static final RuleIElementType PARAMETER_LIST = ruleIElementTypes.get(RULE_parameterList);
    public static final RuleIElementType ANNOTATION_ATTACHMENT = ruleIElementTypes.get(RULE_annotationAttachment);
    public static final RuleIElementType ATTACHMENT_POINT = ruleIElementTypes.get(RULE_attachmentPoint);
    public static final RuleIElementType ANNOTATION_ATTRIBUTE_LIST =
            ruleIElementTypes.get(RULE_annotationAttributeList);
    public static final RuleIElementType ANNOTATION_ATTRIBUTE_VALUE =
            ruleIElementTypes.get(RULE_annotationAttributeValue);
    public static final RuleIElementType RECORD_LITERAL = ruleIElementTypes.get(RULE_recordLiteral);
    public static final RuleIElementType RECORD_KEY_VALUE = ruleIElementTypes.get(RULE_recordKeyValue);

    public static final RuleIElementType EXPRESSION_LIST = ruleIElementTypes.get(RULE_expressionList);
    public static final RuleIElementType TYPE_LIST = ruleIElementTypes.get(RULE_typeList);
    public static final RuleIElementType TYPE_NAME = ruleIElementTypes.get(RULE_typeName);
    public static final RuleIElementType EXPRESSION = ruleIElementTypes.get(RULE_expression);
    public static final RuleIElementType SIMPLE_EXPRESSION = ruleIElementTypes.get(RULE_simpleExpression);
    public static final RuleIElementType RETURN_PARAMETERS = ruleIElementTypes.get(RULE_returnParameters);
    public static final RuleIElementType VALUE_TYPE_NAME = ruleIElementTypes.get(RULE_valueTypeName);
    public static final RuleIElementType XML_NAMESPACE_NAME = ruleIElementTypes.get(RULE_xmlNamespaceName);
    public static final RuleIElementType SOURCE_NOTATION = ruleIElementTypes.get(RULE_sourceNotation);
    public static final RuleIElementType PACKAGE_NAME = ruleIElementTypes.get(RULE_packageName);
    public static final RuleIElementType INVOCATION = ruleIElementTypes.get(RULE_invocation);
    public static final RuleIElementType ANY_IDENTIFIER_NAME = ruleIElementTypes.get(RULE_anyIdentifierName);
    public static final RuleIElementType ENUM_FIELD_LIST = ruleIElementTypes.get(RULE_enumFieldList);
    public static final RuleIElementType TRANSACTION_PROPERTY_INIT_STATEMENT_LIST =
            ruleIElementTypes.get(RULE_transactionPropertyInitStatementList);

    // Keywords
    public static final TokenIElementType ALL = tokenIElementTypes.get(BallerinaLexer.ALL);
    public static final TokenIElementType ACTION = tokenIElementTypes.get(BallerinaLexer.ACTION);
    public static final TokenIElementType ANNOTATION = tokenIElementTypes.get(BallerinaLexer.ANNOTATION);
    public static final TokenIElementType ANY = tokenIElementTypes.get(BallerinaLexer.TYPE_ANY);
    public static final TokenIElementType AS = tokenIElementTypes.get(BallerinaLexer.AS);
    public static final TokenIElementType ATTACH = tokenIElementTypes.get(BallerinaLexer.ATTACH);
    public static final TokenIElementType BIND = tokenIElementTypes.get(BallerinaLexer.BIND);
    public static final TokenIElementType BREAK = tokenIElementTypes.get(BallerinaLexer.BREAK);
    public static final TokenIElementType CATCH = tokenIElementTypes.get(BallerinaLexer.CATCH);
    public static final TokenIElementType CONNECTOR = tokenIElementTypes.get(BallerinaLexer.CONNECTOR);
    public static final TokenIElementType CONST = tokenIElementTypes.get(BallerinaLexer.CONST);
    public static final TokenIElementType CREATE = tokenIElementTypes.get(BallerinaLexer.CREATE);
    public static final TokenIElementType ELSE = tokenIElementTypes.get(BallerinaLexer.ELSE);
    public static final TokenIElementType ENDPOINT = tokenIElementTypes.get(BallerinaLexer.ENDPOINT);
    public static final TokenIElementType FAILED = tokenIElementTypes.get(BallerinaLexer.FAILED);
    public static final TokenIElementType FINALLY = tokenIElementTypes.get(BallerinaLexer.FINALLY);
    public static final TokenIElementType FORK = tokenIElementTypes.get(BallerinaLexer.FORK);
    public static final TokenIElementType FUNCTION = tokenIElementTypes.get(BallerinaLexer.FUNCTION);
    public static final TokenIElementType IF = tokenIElementTypes.get(BallerinaLexer.IF);
    public static final TokenIElementType IMPORT = tokenIElementTypes.get(BallerinaLexer.IMPORT);
    public static final TokenIElementType FOREACH = tokenIElementTypes.get(BallerinaLexer.FOREACH);
    public static final TokenIElementType IN = tokenIElementTypes.get(BallerinaLexer.IN);
    public static final TokenIElementType JOIN = tokenIElementTypes.get(BallerinaLexer.JOIN);
    public static final TokenIElementType JSON = tokenIElementTypes.get(BallerinaLexer.TYPE_JSON);
    public static final TokenIElementType LENGTHOF = tokenIElementTypes.get(BallerinaLexer.LENGTHOF);
    public static final TokenIElementType LOCK = tokenIElementTypes.get(BallerinaLexer.LOCK);
    public static final TokenIElementType MAP = tokenIElementTypes.get(BallerinaLexer.TYPE_MAP);
    public static final TokenIElementType NATIVE = tokenIElementTypes.get(BallerinaLexer.NATIVE);
    public static final TokenIElementType PACKAGE = tokenIElementTypes.get(BallerinaLexer.PACKAGE);
    public static final TokenIElementType PUBLIC = tokenIElementTypes.get(BallerinaLexer.PUBLIC);
    public static final TokenIElementType RETRIES = tokenIElementTypes.get(BallerinaLexer.RETRIES);
    public static final TokenIElementType RESOURCE = tokenIElementTypes.get(BallerinaLexer.RESOURCE);
    public static final TokenIElementType RETURN = tokenIElementTypes.get(BallerinaLexer.RETURN);
    public static final TokenIElementType RETURNS = tokenIElementTypes.get(BallerinaLexer.RETURNS);
    public static final TokenIElementType SERVICE = tokenIElementTypes.get(BallerinaLexer.SERVICE);
    public static final TokenIElementType STRUCT = tokenIElementTypes.get(BallerinaLexer.STRUCT);
    public static final TokenIElementType THROW = tokenIElementTypes.get(BallerinaLexer.THROW);
    public static final TokenIElementType TIMEOUT = tokenIElementTypes.get(BallerinaLexer.TIMEOUT);
    public static final TokenIElementType TRANSACTION = tokenIElementTypes.get(BallerinaLexer.TRANSACTION);
    public static final TokenIElementType TRANSFORMER = tokenIElementTypes.get(BallerinaLexer.TRANSFORMER);
    public static final TokenIElementType TRY = tokenIElementTypes.get(BallerinaLexer.TRY);
    public static final TokenIElementType TYPE = tokenIElementTypes.get(BallerinaLexer.TYPE_TYPE);
    public static final TokenIElementType TYPEOF = tokenIElementTypes.get(BallerinaLexer.TYPEOF);
    public static final TokenIElementType VAR = tokenIElementTypes.get(BallerinaLexer.VAR);
    public static final TokenIElementType WHILE = tokenIElementTypes.get(BallerinaLexer.WHILE);
    public static final TokenIElementType WORKER = tokenIElementTypes.get(BallerinaLexer.WORKER);
    public static final TokenIElementType WITH = tokenIElementTypes.get(BallerinaLexer.WITH);
    public static final TokenIElementType XMLNS = tokenIElementTypes.get(BallerinaLexer.XMLNS);
    public static final TokenIElementType XML = tokenIElementTypes.get(BallerinaLexer.TYPE_XML);

    // Other tokens
    public static final TokenIElementType SENDARROW = tokenIElementTypes.get(BallerinaLexer.RARROW);
    public static final TokenIElementType RECEIVEARROW = tokenIElementTypes.get(BallerinaLexer.LARROW);

    public static final TokenIElementType LPAREN = tokenIElementTypes.get(BallerinaLexer.LEFT_PARENTHESIS);
    public static final TokenIElementType RPAREN = tokenIElementTypes.get(BallerinaLexer.RIGHT_PARENTHESIS);
    public static final TokenIElementType LBRACE = tokenIElementTypes.get(BallerinaLexer.LEFT_BRACE);
    public static final TokenIElementType RBRACE = tokenIElementTypes.get(BallerinaLexer.RIGHT_BRACE);
    public static final TokenIElementType LBRACK = tokenIElementTypes.get(BallerinaLexer.LEFT_BRACKET);
    public static final TokenIElementType RBRACK = tokenIElementTypes.get(BallerinaLexer.RIGHT_BRACKET);
    public static final TokenIElementType SEMI = tokenIElementTypes.get(BallerinaLexer.SEMICOLON);
    public static final TokenIElementType COMMA = tokenIElementTypes.get(BallerinaLexer.COMMA);
    public static final TokenIElementType DOT = tokenIElementTypes.get(BallerinaLexer.DOT);

    public static final TokenIElementType ASSIGN = tokenIElementTypes.get(BallerinaLexer.ASSIGN);
    public static final TokenIElementType GT = tokenIElementTypes.get(BallerinaLexer.GT);
    public static final TokenIElementType LT = tokenIElementTypes.get(BallerinaLexer.LT);
    public static final TokenIElementType BANG = tokenIElementTypes.get(BallerinaLexer.NOT);
    public static final TokenIElementType TILDE = tokenIElementTypes.get(BallerinaLexer.TILDE);
    public static final TokenIElementType COLON = tokenIElementTypes.get(BallerinaLexer.COLON);
    public static final TokenIElementType EQUAL = tokenIElementTypes.get(BallerinaLexer.EQUAL);
    public static final TokenIElementType LE = tokenIElementTypes.get(BallerinaLexer.LT_EQUAL);
    public static final TokenIElementType GE = tokenIElementTypes.get(BallerinaLexer.GT_EQUAL);
    public static final TokenIElementType NOTEQUAL = tokenIElementTypes.get(BallerinaLexer.NOT_EQUAL);
    public static final TokenIElementType AND = tokenIElementTypes.get(BallerinaLexer.AND);
    public static final TokenIElementType OR = tokenIElementTypes.get(BallerinaLexer.OR);
    public static final TokenIElementType ADD = tokenIElementTypes.get(BallerinaLexer.ADD);
    public static final TokenIElementType SUB = tokenIElementTypes.get(BallerinaLexer.SUB);
    public static final TokenIElementType MUL = tokenIElementTypes.get(BallerinaLexer.MUL);
    public static final TokenIElementType DIV = tokenIElementTypes.get(BallerinaLexer.DIV);
    public static final TokenIElementType BITAND = tokenIElementTypes.get(BallerinaLexer.BITAND);
    public static final TokenIElementType BITOR = tokenIElementTypes.get(BallerinaLexer.BITOR);
    public static final TokenIElementType CARET = tokenIElementTypes.get(BallerinaLexer.POW);
    public static final TokenIElementType MOD = tokenIElementTypes.get(BallerinaLexer.MOD);
    public static final TokenIElementType AT = tokenIElementTypes.get(BallerinaLexer.AT);

    public static final TokenSet OPERATORS = TokenSet.create(ASSIGN, EQUAL, LE, GE, NOTEQUAL, AND, OR, MUL, DIV,
            BITAND, BITOR, CARET, MOD);
}
