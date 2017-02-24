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

package org.ballerinalang.plugins.idea.usage;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.*;

public class BallerinaFindUsageProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof IdentifierPSINode;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        ANTLRPsiNode parent = (ANTLRPsiNode) element.getParent();
        RuleIElementType elType = (RuleIElementType) parent.getNode().getElementType();
        // Todo - Add more types
        switch (elType.getRuleIndex()) {
            case RULE_function:
                return "Function";
            case RULE_nativeFunction:
                return "Native Function";
            case RULE_connector:
                return "Connector";
            case RULE_nativeConnector:
                return "Native Connector";
            case RULE_action:
                return "Action";
            case RULE_nativeAction:
                return "Native Action";
            case RULE_serviceDefinition:
                return "Service";
            case RULE_variableDefinitionStatement:
                return "Variable";
            case RULE_parameter:
                return "Parameter";
            case RULE_packageName:
                return "Package";
            case RULE_namedParameter:
                return "Named Parameter";
            case RULE_typeMapperInput:
                return "Type Mapper Input";
            case RULE_structDefinition:
                return "Struct";
            case RULE_constantDefinition:
                return "Constant";
            case RULE_simpleType:
                // Todo - Resolve the SimpleType element and return the correct type.
        }
        return "";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        return element.getText();
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        String text = element.getText();
        return text;
    }
}