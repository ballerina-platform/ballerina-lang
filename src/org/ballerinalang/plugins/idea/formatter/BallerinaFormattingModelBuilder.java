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

public class BallerinaFormattingModelBuilder implements FormattingModelBuilder {

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        BallerinaBlock rootBlock = new BallerinaBlock(element.getNode(), null, Indent.getNoneIndent(), null,
                settings, createSpaceBuilder(settings)
        );
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), rootBlock, settings
        );
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, BallerinaLanguage.INSTANCE)

                //{ function body }
                //                //Todo: Add code style settings provider
                //                .before(COMMA).spaceIf(false)
                //                .after(COMMA).spaceIf(true)
                //                .around(ASSIGN).spaceIf(true)
                //                .around(GT).spaceIf(true)
                //                .around(LT).spaceIf(true)
                //                .around(EQUAL).spaceIf(true)
                //                .around(LE).spaceIf(true)
                //                .around(GE).spaceIf(true)
                //                .around(NOTEQUAL).spaceIf(true)
                //                .around(AND).spaceIf(true)
                //                .around(OR).spaceIf(true)
                //                .around(ADD).spaceIf(true)
                //                .around(SUB).spaceIf(true)
                //                .around(MUL).spaceIf(true)
                //                .around(DIV).spaceIf(true)
                //                .around(BITAND).spaceIf(true)
                //                .around(BITOR).spaceIf(true)
                //                .around(MOD).spaceIf(true)
                //                .before(SEMI).spaceIf(false)
                //                .around(THROWS).spaceIf(true)
                //                .around(MAP_INIT_KEY_VALUE).spaceIf(false)
                //                .around(MAP_INIT_KEY_VALUE_LIST).spaceIf(false)
                //                .after(IF).spaceIf(true)
                //                .between(RPAREN, LBRACE).spaceIf(true)
                //                .around(ELSE_IF_CLAUSE).spaceIf(true)
                //                .around(ELSE_CLAUSE).spaceIf(true)
                //                .between(ELSE, IF).spaceIf(true)
                //                .between(ELSE, LBRACE).spaceIf(true)
                //                .after(WHILE).spaceIf(true)
                //                .between(RPAREN, TYPE_NAME).spaceIf(true)
                //                .after(NEW).spaceIf(true)
                //                .after(RETURN).spaceIf(true)
                //                .after(SERVICE).spaceIf(true)
                //                .after(RESOURCE).spaceIf(true)
                //                .after(FUNCTION).spaceIf(true)
                //                .after(CONNECTOR).spaceIf(true)
                //                .after(REPLY).spaceIf(true)
                //                .after(TYPECONVERTOR).spaceIf(true)
                //                .between(LBRACK, RBRACK).spaceIf(false)
                //                .before(LBRACK).spaceIf(false)
                //                .between(PACKAGE_NAME, COLON).spaceIf(false)
                //                .between(COLON, IDENTIFIER).spaceIf(false)
                //                .between(IDENTIFIER, DOT).spaceIf(false)
                //                .between(DOT, IDENTIFIER).spaceIf(false)
                //                .between(LPAREN, ELEMENT_VALUE).spaceIf(false)
                //                .between(ELEMENT_VALUE, RPAREN).spaceIf(false)
                //                .between(IDENTIFIER, LBRACE).spaceIf(true)
                //                .before(EXPRESSION_LIST).spaceIf(false)
                //                .after(EXPRESSION_LIST).spaceIf(false)
                //                .around(EXPRESSION).spaceIf(false)
                //                .around(RETURN_TYPE_LIST).spaceIf(false)
                //                .around(RETURN_PARAMETERS).spaceIf(true)
                //                .after(PACKAGE).spaceIf(true)
                //                .after(IMPORT).spaceIf(true)
                //                .after(ANNOTATION_NAME).spaceIf(true)
                //                .between(AT, ANNOTATION_NAME).spaceIf(false)
                //                .between(TYPE_NAME, IDENTIFIER).spaceIf(true)
                //                .between(IDENTIFIER, LPAREN).spaceIf(true)
                //                .after(PARAMETER_LIST).spaceIf(false)
                //                .before(PARAMETER_LIST).spaceIf(false)
                //                .after(PACKAGE_DECLARATION).blankLines(1)
                //                .before(FUNCTION_DEFINITION).blankLines(1)
                //                .after(FUNCTION_DEFINITION).blankLines(1)
                //                .before(SERVICE_DEFINITION).blankLines(1)
                //                .after(SERVICE_DEFINITION).blankLines(1)
                //                .before(RESOURCE_DEFINITION).blankLines(1)
                //                .after(RESOURCE_DEFINITION).blankLines(1)
                //                .before(SERVICE_BODY_DECLARATION).blankLines(1)
                ;
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }
}
