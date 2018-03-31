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

package org.ballerinalang.plugins.idea.psi.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.DocumentationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a variable reference in documentations.
 */
public class DocVariableReference extends BallerinaElementReference {

    public DocVariableReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        //Todo - Implement properly in the new plugin
        //        IdentifierPSINode identifier = getElement();
        //        // Resource grammar is different. So need to handle it separately.
        //        ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
        //                ResourceDefinitionNode.class);
        //        if (resourceDefinitionNode != null) {
        //            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getParameters(resourceDefinitionNode);
        //            for (IdentifierPSINode parameter : parameters) {
        //                if (identifier.getText().equals(parameter.getText())) {
        //                    return parameter;
        //                }
        //            }
        //        } else {
        //            DocumentationAttachmentNode documentationAttachmentNode = PsiTreeUtil.getParentOfType(identifier,
        //                    DocumentationAttachmentNode.class);
        //            if (documentationAttachmentNode == null) {
        //                return null;
        //            }
        //            PsiElement nextSibling = PsiTreeUtil.skipSiblingsForward(documentationAttachmentNode,
        // PsiWhiteSpace.class,
        //                    PsiComment.class);
        //            if (nextSibling == null) {
        //                return null;
        //            }
        //            if (nextSibling instanceof DefinitionNode) {
        //
        //            }
        //        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                ResourceDefinitionNode.class);
        if (resourceDefinitionNode != null) {
            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getParameters(resourceDefinitionNode);
            results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));
        } else {
            DocumentationAttachmentNode documentationAttachmentNode = PsiTreeUtil.getParentOfType(identifier,
                    DocumentationAttachmentNode.class);
            if (documentationAttachmentNode == null) {
                return results.toArray(new LookupElement[results.size()]);
            }
            PsiElement nextSibling = documentationAttachmentNode.getNextSibling();
            if (nextSibling == null) {
                return results.toArray(new LookupElement[results.size()]);
            }
        }
        return results.toArray(new LookupElement[results.size()]);
    }
}
