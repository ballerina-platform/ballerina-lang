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

import static org.ballerinalang.plugins.idea.BallerinaTypes.*;

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
                .after(ACTION).spaceIf(true)
                .after(ALL).spaceIf(true)
                .before(ANY).spaceIf(false)
                .after(ANY).spaceIf(true)
                .around(AS).spaceIf(true)
                .around(BREAK).spaceIf(false)
                .around(CATCH).spaceIf(true)
                .around(FAILED).spaceIf(true)
                .around(FINALLY).spaceIf(true)
                .after(CONNECTOR).spaceIf(true)
                .after(CONST).spaceIf(true)
                .after(CREATE).spaceIf(true)
                .around(ELSE).spaceIf(true)
                .between(FORK, LBRACE).spaceIf(true)
                .between(EXPRESSION, LBRACE).spaceIf(true)
                .between(FORK, SEMI).spaceIf(false)
                .after(FUNCTION).spaceIf(true)
                .after(IF).spaceIf(true)
                .after(IMPORT).spaceIf(true)
                .after(FOREACH).spaceIf(true)
                .around(IN).spaceIf(true)
                .around(JOIN).spaceIf(true)
                .after(NATIVE).spaceIf(true)
                .after(PACKAGE).spaceIf(true)
                .after(RETRIES).spaceIf(false)
                .after(TRANSACTION_PROPERTY_INIT_STATEMENT_LIST).spaceIf(true)
                .after(RESOURCE).spaceIf(true)
                .between(SERVICE, SOURCE_NOTATION).spaceIf(false)
                .between(SERVICE, IDENTIFIER).spaceIf(true)
                .after(STRUCT).spaceIf(true)
                .after(THROW).spaceIf(true)
                .around(TIMEOUT).spaceIf(true)
                .after(TRY).spaceIf(true)
                .after(TYPE).spaceIf(true)
                .after(VAR).spaceIf(true)
                .after(WHILE).spaceIf(true)
                .after(WORKER).spaceIf(true)
                .around(WITH).spaceIf(true)
                .after(XMLNS).spaceIf(true)
                .after(TYPEOF).spaceIf(true)
                .after(LENGTHOF).spaceIf(true)
                .after(LOCK).spaceIf(true)
                .before(SEMI).spaceIf(false)
                .around(DOT).spaceIf(false)
                .around(PUBLIC).spaceIf(true)
                .between(LPAREN, RPAREN).spaceIf(false)
                .between(RPAREN, LBRACE).spaceIf(true)
                .around(COLON).spaceIf(false)
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
                .around(ATTACH).spaceIf(true)
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
                .after(ANNOTATION_REFERENCE).spaceIf(true)
                .after(FUNCTION_REFERENCE).spaceIf(false)
                .after(CONNECTOR_REFERENCE).spaceIf(false)
                .around(ANNOTATION_ATTRIBUTE_LIST).spaceIf(false)
                .around(ANNOTATION_ATTRIBUTE_VALUE).spaceIf(false)
                .around(NAME_REFERENCE).spaceIf(false)
                .between(VALUE_TYPE_NAME, IDENTIFIER).spaceIf(true)
                .between(IDENTIFIER, LBRACE).spaceIf(true)
                .between(ATTACHMENT_POINT, LBRACE).spaceIf(true)
                .around(RECORD_LITERAL).spaceIf(false)
                .around(RECORD_KEY_VALUE).spaceIf(false)
                .between(XML, LT).spaceIf(false)
                .around(XML_NAMESPACE_NAME).spaceIf(false)
                .around(EXPRESSION_LIST).spaceIf(false)
                .aroundInside(INTEGER_LITERAL, JOIN_CONDITIONS).spaceIf(true)
                .aroundInside(PACKAGE_NAME, SOURCE_NOTATION).spaceIf(false)
                .between(SOURCE_NOTATION, IDENTIFIER).spaceIf(true)
                .withinPairInside(IDENTIFIER, LBRACE, FUNCTION_DEFINITION).spaceIf(true)
                .withinPairInside(IDENTIFIER, LBRACE, SERVICE_DEFINITION).spaceIf(true)
                .withinPairInside(IDENTIFIER, LBRACE, RESOURCE_DEFINITION).spaceIf(true)
                .withinPairInside(IDENTIFIER, LPAREN, CONNECTOR_DEFINITION).spaceIf(true)
                .withinPairInside(IDENTIFIER, LPAREN, ACTION_DEFINITION).spaceIf(true)
                .before(INVOCATION).spaceIf(false)
                .afterInside(IDENTIFIER, INVOCATION).spaceIf(false)
                .after(BIND).spaceIf(true)
                .between(LT, CONNECTOR_REFERENCE).spaceIf(false)
                .between(CONNECTOR_REFERENCE, GT).spaceIf(false)
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
