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

package org.ballerinalang.plugins.idea.formatter;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.ballerinalang.plugins.idea.BallerinaTypes.ADD;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ALL;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANNOTATION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANNOTATION_ATTACHMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANY_IDENTIFIER_NAME;
import static org.ballerinalang.plugins.idea.BallerinaTypes.AS;
import static org.ballerinalang.plugins.idea.BallerinaTypes.AT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ATTACHMENT_POINT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.BANG;
import static org.ballerinalang.plugins.idea.BallerinaTypes.BIND;
import static org.ballerinalang.plugins.idea.BallerinaTypes.BREAK;
import static org.ballerinalang.plugins.idea.BallerinaTypes.BY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CATCH;
import static org.ballerinalang.plugins.idea.BallerinaTypes.COLON;
import static org.ballerinalang.plugins.idea.BallerinaTypes.COMMA;
import static org.ballerinalang.plugins.idea.BallerinaTypes.COMPOUND_OPERATOR;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CONST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.DELETE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.DOT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.DOUBLE_COLON;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ELSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ENDPOINT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.EXPRESSION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.EXPRESSION_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FIELD;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FINALLY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FLOATING_POINT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FOLLOWED;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FOR;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FOREACH;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FORK;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FROM;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FUNCTION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FUNCTION_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FUNCTION_REFERENCE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.GROUP;
import static org.ballerinalang.plugins.idea.BallerinaTypes.GT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.HAVING;
import static org.ballerinalang.plugins.idea.BallerinaTypes.IDENTIFIER;
import static org.ballerinalang.plugins.idea.BallerinaTypes.IF;
import static org.ballerinalang.plugins.idea.BallerinaTypes.IMPORT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.IN;
import static org.ballerinalang.plugins.idea.BallerinaTypes.INDEX;
import static org.ballerinalang.plugins.idea.BallerinaTypes.INSERT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.INTEGER_LITERAL;
import static org.ballerinalang.plugins.idea.BallerinaTypes.INTO;
import static org.ballerinalang.plugins.idea.BallerinaTypes.INVOCATION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.JOIN;
import static org.ballerinalang.plugins.idea.BallerinaTypes.JOIN_CONDITIONS;
import static org.ballerinalang.plugins.idea.BallerinaTypes.JSON;
import static org.ballerinalang.plugins.idea.BallerinaTypes.LBRACE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.LBRACK;
import static org.ballerinalang.plugins.idea.BallerinaTypes.LENGTHOF;
import static org.ballerinalang.plugins.idea.BallerinaTypes.LOCK;
import static org.ballerinalang.plugins.idea.BallerinaTypes.LPAREN;
import static org.ballerinalang.plugins.idea.BallerinaTypes.LT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.MAP;
import static org.ballerinalang.plugins.idea.BallerinaTypes.MATCH;
import static org.ballerinalang.plugins.idea.BallerinaTypes.NAME_REFERENCE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.NATIVE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.NEW;
import static org.ballerinalang.plugins.idea.BallerinaTypes.OBJECT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ON;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ONABORT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ONCOMMIT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ONRETRY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.OPERATORS;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ORDER;
import static org.ballerinalang.plugins.idea.BallerinaTypes.PACKAGE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.PARAMETER;
import static org.ballerinalang.plugins.idea.BallerinaTypes.PARAMETER_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.PUBLIC;
import static org.ballerinalang.plugins.idea.BallerinaTypes.QUERY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.QUESTION_MARK;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RBRACE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RECEIVEARROW;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RECORD_KEY_VALUE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RESOURCE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RESOURCE_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RETRIES;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RETURN;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RETURNS;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RETURN_PARAMETERS;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RPAREN;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SELECT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SEMI;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SENDARROW;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SERVICE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SERVICE_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SET;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SIMPLE_EXPRESSION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.STREAMLET;
import static org.ballerinalang.plugins.idea.BallerinaTypes.STRUCT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SUB;
import static org.ballerinalang.plugins.idea.BallerinaTypes.THROW;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TILDE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TIMEOUT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRANSACTION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRANSACTION_PROPERTY_INIT_STATEMENT_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRANSFORMER;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRANSFORMER_INVOCATION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TYPE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TYPEOF;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TYPE_AGGREGATION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TYPE_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TYPE_NAME;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TYPE_STREAM;
import static org.ballerinalang.plugins.idea.BallerinaTypes.UNTAINT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.UPDATE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.VALUE_TYPE_NAME;
import static org.ballerinalang.plugins.idea.BallerinaTypes.VAR;
import static org.ballerinalang.plugins.idea.BallerinaTypes.VARIABLE_REFERENCE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WHERE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WHILE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WINDOW;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WITH;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WORKER;
import static org.ballerinalang.plugins.idea.BallerinaTypes.XML;
import static org.ballerinalang.plugins.idea.BallerinaTypes.XMLNS;
import static org.ballerinalang.plugins.idea.BallerinaTypes.XML_ATTRIB;
import static org.ballerinalang.plugins.idea.BallerinaTypes.XML_LOCAL_NAME;
import static org.ballerinalang.plugins.idea.BallerinaTypes.XML_NAMESPACE_NAME;

/**
 * Builds the Ballerina file formatting model.
 */
public class BallerinaFormattingModelBuilder implements FormattingModelBuilder {

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        BallerinaBlock rootBlock = new BallerinaBlock(
                element.getNode(), null, Indent.getNoneIndent(), null, settings, createSpaceBuilder(settings)
        );
        return FormattingModelProvider.createFormattingModelForPsiFile(
                element.getContainingFile(), rootBlock, settings
        );
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, BallerinaLanguage.INSTANCE)
                .around(OPERATORS).spaceIf(true)
                .between(TYPE_NAME, GT).spaceIf(false)
                .between(LT, TYPE_NAME).spaceIf(false)
                .between(IDENTIFIER, LT).spaceIf(false)
                .between(GT, SIMPLE_EXPRESSION).spaceIf(false)
                .between(RPAREN, SIMPLE_EXPRESSION).spaceIf(false)
                .aroundInside(LT, EXPRESSION).spaceIf(true)
                .aroundInside(GT, EXPRESSION).spaceIf(true)
                .between(ADD, SIMPLE_EXPRESSION).spaceIf(false)
                .between(SUB, SIMPLE_EXPRESSION).spaceIf(false)
                .between(ADD, INTEGER_LITERAL).spaceIf(false)
                .between(SUB, INTEGER_LITERAL).spaceIf(false)
                .between(ADD, FLOATING_POINT).spaceIf(false)
                .between(SUB, FLOATING_POINT).spaceIf(false)
                .between(BANG, SIMPLE_EXPRESSION).spaceIf(false)
                .around(ADD).spaceIf(true)
                .around(SUB).spaceIf(true)
                .before(ALL).spaceIf(false)
                .after(ALL).spaceIf(true)
                .before(ANY).spaceIf(false)
                .after(ANY).spaceIf(true)
                .around(AS).spaceIf(true)
                .around(BIND).spaceIf(true)
                .around(BY).spaceIf(true)
                .around(BREAK).spaceIf(false)
                .around(CATCH).spaceIf(true)
                .around(COMPOUND_OPERATOR).spaceIf(true)
                .around(ONABORT).spaceIf(true)
                .around(ONCOMMIT).spaceIf(true)
                .around(ONRETRY).spaceIf(true)
                .around(DOUBLE_COLON).spaceIf(false)
                .around(FINALLY).spaceIf(true)
                .around(GROUP).spaceIf(true)
                .after(CONST).spaceIf(true)
                .around(DELETE).spaceIf(true)
                .after(ENDPOINT).spaceIf(true)
                .around(ELSE).spaceIf(true)
                .between(FORK, LBRACE).spaceIf(true)
                .around(FOLLOWED).spaceIf(true)
                .around(FOR).spaceIf(true)
                .around(FROM).spaceIf(true)
                .between(EXPRESSION, LBRACE).spaceIf(true)
                .between(FORK, SEMI).spaceIf(false)
                .after(FUNCTION).spaceIf(true)
                .after(IF).spaceIf(true)
                .around(INSERT).spaceIf(true)
                .around(INTO).spaceIf(true)
                .after(HAVING).spaceIf(true)
                .after(IMPORT).spaceIf(true)
                .after(FOREACH).spaceIf(true)
                .around(IN).spaceIf(true)
                .around(JOIN).spaceIf(true)
                .after(MATCH).spaceIf(true)
                .after(NATIVE).spaceIf(true)
                .after(NEW).spaceIf(true)
                .around(OBJECT).spaceIf(true)
                .around(ON).spaceIf(true)
                .around(ORDER).spaceIf(true)
                .after(PACKAGE).spaceIf(true)
                .around(QUERY).spaceIf(true)
                .after(RETRIES).spaceIf(false)
                .after(TRANSACTION_PROPERTY_INIT_STATEMENT_LIST).spaceIf(true)
                .after(RESOURCE).spaceIf(true)
                .around(SELECT).spaceIf(true)
                .around(SET).spaceIf(true)
                .between(SERVICE, LT).spaceIf(false)
                .between(SERVICE, IDENTIFIER).spaceIf(true)
                .after(STREAMLET).spaceIf(true)
                .after(STRUCT).spaceIf(true)
                .after(THROW).spaceIf(true)
                .around(TIMEOUT).spaceIf(true)
                .after(TRY).spaceIf(true)
                .after(TYPE).spaceIf(true)
                .around(TYPE_AGGREGATION).spaceIf(true)
                .around(TYPE_STREAM).spaceIf(true)
                .after(UNTAINT).spaceIf(true)
                .around(UPDATE).spaceIf(true)
                .after(VAR).spaceIf(true)
                .around(WHERE).spaceIf(true)
                .after(WHILE).spaceIf(true)
                .around(WINDOW).spaceIf(true)
                .after(WORKER).spaceIf(true)
                .around(WITH).spaceIf(true)
                .after(XMLNS).spaceIf(true)
                .after(TYPEOF).spaceIf(true)
                .after(LENGTHOF).spaceIf(true)
                .after(LOCK).spaceIf(true)
                .before(SEMI).spaceIf(false)
                .around(DOT).spaceIf(false)
                .between(TYPE_NAME, QUESTION_MARK).spaceIf(false)
                .around(QUESTION_MARK).spaceIf(true)
                .around(PUBLIC).spaceIf(true)
                .between(LPAREN, RPAREN).spaceIf(false)
                .between(RPAREN, LBRACE).spaceIf(true)
                .aroundInside(COLON, EXPRESSION).spaceIf(true)
                .around(COLON).spaceIf(false)
                .between(COMMA, PARAMETER).spaceIf(true)
                .around(PARAMETER).spaceIf(false)
                .between(COMMA, PARAMETER_LIST).spaceIf(true)
                .around(PARAMETER_LIST).spaceIf(false)
                .around(TYPE_LIST).spaceIf(false)
                .before(COMMA).spaceIf(false)
                .after(COMMA).spaceIf(true)
                .between(TYPE_NAME, IDENTIFIER).spaceIf(true)
                .around(LBRACK).spaceIf(false)
                .between(LBRACE, RBRACE).spaceIf(false)
                .before(RPAREN).spaceIf(false)
                .after(LPAREN).spaceIf(false)
                .between(RPAREN, LPAREN).spaceIf(true)
                .after(AT).spaceIf(false)
                .between(BIND, EXPRESSION).spaceIf(true)
                .around(EXPRESSION).spaceIf(false)
                .around(RETURN_PARAMETERS).spaceIf(true)
                .around(SENDARROW).spaceIf(true)
                .around(RECEIVEARROW).spaceIf(true)
                .before(TILDE).spaceIf(false)
                .between(RETURN, EXPRESSION_LIST).spaceIf(true)
                .around(RETURNS).spaceIf(true)
                .after(ANNOTATION).spaceIf(true)
                .between(ANNOTATION_ATTACHMENT, TYPE_NAME).spaceIf(true)
                .between(VALUE_TYPE_NAME, IDENTIFIER).spaceIf(true)
                .between(ANNOTATION_ATTACHMENT, TYPE_NAME).spaceIf(true)
                .around(TYPE_NAME).spaceIf(false)
                .after(TRANSACTION).spaceIf(true)
                .between(MAP, LT).spaces(0)
                .between(JSON, LT).spaceIf(false)
                .between(XML, LT).spaceIf(false)
                .between(LT, LBRACE).spaceIf(false)
                .before(LBRACE).spaceIf(true)
                .between(RBRACE, GT).spaceIf(false)
                .between(RBRACE, XML_LOCAL_NAME).spaceIf(true)
                .between(XML_LOCAL_NAME, GT).spaceIf(false)
                .between(NAME_REFERENCE, LBRACE).spaceIf(true)
                .between(VARIABLE_REFERENCE, INDEX).spaceIf(false)
                .between(VARIABLE_REFERENCE, FIELD).spaceIf(false)
                .between(VARIABLE_REFERENCE, XML_ATTRIB).spaceIf(false)
                .between(VARIABLE_REFERENCE, LPAREN).spaceIf(false)
                .after(FUNCTION_REFERENCE).spaceIf(false)
                .around(NAME_REFERENCE).spaceIf(false)
                .between(VALUE_TYPE_NAME, IDENTIFIER).spaceIf(true)
                .between(IDENTIFIER, LBRACE).spaceIf(true)
                .between(ATTACHMENT_POINT, LBRACE).spaceIf(true)
                .around(RECORD_KEY_VALUE).spaceIf(false)
                .between(XML, LT).spaceIf(false)
                .around(XML_NAMESPACE_NAME).spaceIf(false)
                .around(EXPRESSION_LIST).spaceIf(false)
                .aroundInside(INTEGER_LITERAL, JOIN_CONDITIONS).spaceIf(true)
                .between(LT, IDENTIFIER).spaceIf(true)
                .withinPairInside(IDENTIFIER, LBRACE, FUNCTION_DEFINITION).spaceIf(true)
                .withinPairInside(IDENTIFIER, LBRACE, SERVICE_DEFINITION).spaceIf(true)
                .withinPairInside(IDENTIFIER, LBRACE, RESOURCE_DEFINITION).spaceIf(true)
                .before(INVOCATION).spaceIf(false)
                .afterInside(IDENTIFIER, INVOCATION).spaceIf(false)
                .around(ANY_IDENTIFIER_NAME).spaceIf(false)
                .after(BIND).spaceIf(true)
                .after(ENDPOINT).spaceIf(false)
                .between(GT, IDENTIFIER).spaceIf(true)
                .after(TRANSFORMER).spaceIf(true)
                .around(TRANSFORMER_INVOCATION).spaceIf(false);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }
}
