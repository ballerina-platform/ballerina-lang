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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.jetbrains.annotations.NotNull;

import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addFileLevelKeywordsAsLookups;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addTypeNamesAsLookups;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.addValueTypesAsLookups;

public class BallerinaKeywordsCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();

        if (parent.getPrevSibling() == null) {

            GlobalVariableDefinitionNode globalVariableDefinitionNode = PsiTreeUtil.getParentOfType(element,
                    GlobalVariableDefinitionNode.class);
            if (globalVariableDefinitionNode != null) {
                PsiElement definitionNode = globalVariableDefinitionNode.getParent();

                PackageDeclarationNode prevPackageDeclarationNode = PsiTreeUtil.getPrevSiblingOfType(definitionNode,
                        PackageDeclarationNode.class);

                ImportDeclarationNode prevImportDeclarationNode = PsiTreeUtil.getPrevSiblingOfType(definitionNode,
                        ImportDeclarationNode.class);

                ConstantDefinitionNode prevConstantDefinitionNode = PsiTreeUtil.getPrevSiblingOfType(definitionNode,
                        ConstantDefinitionNode.class);

                DefinitionNode prevDefinitionNode =
                        PsiTreeUtil.getPrevSiblingOfType(definitionNode, DefinitionNode.class);

                GlobalVariableDefinitionNode prevGlobalVariableDefinition =
                        PsiTreeUtil.findChildOfType(prevDefinitionNode, GlobalVariableDefinitionNode.class);

                if (prevPackageDeclarationNode == null && prevImportDeclarationNode == null
                        && prevConstantDefinitionNode == null && prevGlobalVariableDefinition == null) {
                    addFileLevelKeywordsAsLookups(result, true, true);
                } else if ((prevPackageDeclarationNode != null || prevImportDeclarationNode != null)
                        && prevConstantDefinitionNode == null && prevGlobalVariableDefinition == null) {
                    addFileLevelKeywordsAsLookups(result, false, true);
                } else {
                    addFileLevelKeywordsAsLookups(result, false, false);
                }


                addTypeNamesAsLookups(result);
            }
        }

        if (parent instanceof ConstantDefinitionNode) {
            PsiElement prevVisibleSibling = PsiTreeUtil.prevVisibleLeaf(element);
            if (prevVisibleSibling != null && "const".equals(prevVisibleSibling.getText())) {
                addValueTypesAsLookups(result);
            }
        }
    }
}

