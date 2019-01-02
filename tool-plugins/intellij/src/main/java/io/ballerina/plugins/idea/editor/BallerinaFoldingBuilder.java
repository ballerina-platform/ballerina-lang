/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;

import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaImportDeclaration;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Provides support to fold code snippets in Ballerina.
 */
public class BallerinaFoldingBuilder extends CustomFoldingBuilder implements DumbAware {

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root,
            @NotNull Document document, boolean quick) {
        if (root instanceof BallerinaFile) {
            return;
        }
        buildImportFoldingRegion(descriptors, root);
    }

    private void buildImportFoldingRegion(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        Collection<BallerinaImportDeclaration> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(root,
                BallerinaImportDeclaration.class);
        if (!importDeclarationNodes.isEmpty()) {
            BallerinaImportDeclaration[] importDeclarationNodesArray =
                    importDeclarationNodes.toArray(new BallerinaImportDeclaration[importDeclarationNodes.size()]);
            BallerinaImportDeclaration firstImport = importDeclarationNodesArray[0];
            BallerinaImportDeclaration lastImport = importDeclarationNodesArray[importDeclarationNodes.size() - 1];

            BallerinaOrgName firstOrgName = PsiTreeUtil.findChildOfType(firstImport, BallerinaOrgName.class);
            if (firstOrgName == null || firstOrgName.getText().isEmpty()) {
                return;
            }
            int startOffset = firstOrgName.getTextRange().getStartOffset();
            int endOffset = lastImport.getTextRange().getEndOffset();
            descriptors.add(new NamedFoldingDescriptor(firstImport, startOffset, endOffset, null, "..."));
        }
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return "...";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
