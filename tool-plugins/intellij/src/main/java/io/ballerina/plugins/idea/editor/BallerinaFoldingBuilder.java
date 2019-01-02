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
import io.ballerina.plugins.idea.psi.BallerinaObjectBody;
import io.ballerina.plugins.idea.psi.BallerinaObjectTypeName;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.BallerinaRecordFieldDefinitionList;
import io.ballerina.plugins.idea.psi.BallerinaRecordTypeName;
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
        if (!(root instanceof BallerinaFile)) {
            return;
        }
        buildImportFoldingRegion(descriptors, root);
        buildObjectFoldingRegions(descriptors,root);
        buildRecordFoldingRegions(descriptors,root);
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

    private void buildObjectFoldingRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        Collection<BallerinaObjectTypeName> objectDefinitions = PsiTreeUtil.findChildrenOfType(root,
                BallerinaObjectTypeName.class);
        for (BallerinaObjectTypeName objectDefinition : objectDefinitions) {
            // Get the object body. This is used to calculate the start offset.
            BallerinaObjectBody objectBody = PsiTreeUtil.getChildOfType(objectDefinition, BallerinaObjectBody
                    .class);
            if (objectBody == null) {
                continue;
            }
            // Add folding descriptor.
            addFoldingDescriptor(descriptors, objectDefinition, objectBody);
        }
    }

    private void buildRecordFoldingRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        Collection<BallerinaRecordTypeName> recordDefinitions = PsiTreeUtil.findChildrenOfType(root,
                BallerinaRecordTypeName.class);
        for (BallerinaRecordTypeName recordDefinition : recordDefinitions) {
            // Get the record body. This is used to calculate the start offset.
            BallerinaRecordFieldDefinitionList recordBody = PsiTreeUtil.getChildOfType(recordDefinition,
                    BallerinaRecordFieldDefinitionList.class);
            if (recordBody == null) {
                continue;
            }
            // Add folding descriptor.
            addFoldingDescriptor(descriptors, recordDefinition, recordBody);
        }
    }

    private void addFoldingDescriptor(@NotNull List<FoldingDescriptor> descriptors, PsiElement node,
            PsiElement bodyNode) {
        // Sometimes the body node might start with a comment node.
        PsiElement prevSibling = bodyNode.getPrevSibling();
        while (prevSibling != null && (prevSibling instanceof PsiComment
                || prevSibling instanceof PsiWhiteSpace)) {
            prevSibling = prevSibling.getPrevSibling();
        }
        if (prevSibling != null) {
            // Calculate the start and end offsets.
            int startOffset = prevSibling.getTextRange().getStartOffset();
            int endOffset = node.getTextRange().getEndOffset();
            // Add the new folding descriptor.
            descriptors.add(new NamedFoldingDescriptor(node, startOffset, endOffset, null, "{...}"));
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
