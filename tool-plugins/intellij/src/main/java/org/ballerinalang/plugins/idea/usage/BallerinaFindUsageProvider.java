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
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_annotationDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_constantDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_endpointDeclaration;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_enumDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_enumField;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_fieldDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_functionDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_globalVariableDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_nameReference;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_namespaceDeclaration;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_packageName;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_parameter;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_serviceDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_structDefinition;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_variableDefinitionStatement;
import static org.ballerinalang.plugins.idea.grammar.BallerinaParser.RULE_workerDeclaration;

/**
 * Find usage provider for Ballerina.
 */
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
        if (!(element.getParent() instanceof ANTLRPsiNode)) {
            return "";
        }
        ANTLRPsiNode parent = (ANTLRPsiNode) element.getParent();
        RuleIElementType elType = (RuleIElementType) parent.getNode().getElementType();
        // Todo - Add more types
        switch (elType.getRuleIndex()) {
            case RULE_functionDefinition:
                return "Function";
            case RULE_serviceDefinition:
                return "Service";
            case RULE_variableDefinitionStatement:
                return "Variable";
            case RULE_parameter:
                return "Parameter";
            case RULE_packageName:
                return "Package";
            case RULE_structDefinition:
                return "Struct";
            case RULE_constantDefinition:
                return "Constant";
            case RULE_fieldDefinition:
                return "Field";
            case RULE_annotationDefinition:
                return "Annotation";
            case RULE_nameReference:
                PsiElement parentNode = PsiTreeUtil.getParentOfType(element, FunctionInvocationNode.class);
                if (parentNode != null) {
                    return "Function";
                }
                parentNode = PsiTreeUtil.getParentOfType(element, ActionInvocationNode.class);
                if (parentNode != null) {
                    return "Action";
                }
                parentNode = PsiTreeUtil.getParentOfType(element, AssignmentStatementNode.class);
                if (parentNode != null) {
                    return "Variable";
                }
                break;
            case RULE_globalVariableDefinition:
                return "Global Variable";
            case RULE_namespaceDeclaration:
                return "Namespace";
            case RULE_workerDeclaration:
                return "Worker";
            case RULE_enumDefinition:
                return "Enum";
            case RULE_enumField:
                return "Enum Field";
            case RULE_endpointDeclaration:
                return "Endpoint";
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
