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

package org.ballerinalang.plugins.idea.psi.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationReferenceNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Represents an annotation attribute reference.
 */
public class AnnotationAttributeReference extends BallerinaElementReference {

    public AnnotationAttributeReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(identifier,
                AnnotationAttachmentNode.class);
        if (annotationAttachmentNode == null) {
            return null;
        }
        AnnotationReferenceNode annotationReferenceNode = PsiTreeUtil.getChildOfType(annotationAttachmentNode,
                AnnotationReferenceNode.class);
        if (annotationReferenceNode == null) {
            return null;
        }
        int textLength = annotationReferenceNode.getTextLength();
        PsiReference reference = annotationReferenceNode.findReferenceAt(textLength);
        if (reference == null) {
            return null;
        }
        PsiElement annotationName = reference.resolve();
        if (annotationName == null || !(annotationName instanceof IdentifierPSINode)) {
            return null;
        }
        PsiElement annotationDefinition = annotationName.getParent();
        Collection<FieldDefinitionNode> fieldDefinitionNodes = PsiTreeUtil.findChildrenOfType(annotationDefinition,
                FieldDefinitionNode.class);
        return BallerinaPsiImplUtil.resolveReference(fieldDefinitionNodes, identifier);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(identifier,
                AnnotationAttachmentNode.class);
        if (annotationAttachmentNode == null) {
            return new Object[0];
        }
        AnnotationReferenceNode annotationReferenceNode = PsiTreeUtil.getChildOfType(annotationAttachmentNode,
                AnnotationReferenceNode.class);
        if (annotationReferenceNode == null) {
            return new Object[0];
        }
        int textLength = annotationReferenceNode.getTextLength();
        PsiReference reference = annotationReferenceNode.findReferenceAt(textLength);
        if (reference == null) {
            return new Object[0];
        }
        PsiElement annotationName = reference.resolve();
        if (annotationName == null || !(annotationName instanceof IdentifierPSINode)) {
            return new Object[0];
        }
        PsiElement annotationDefinition = annotationName.getParent();
        Collection<FieldDefinitionNode> fieldDefinitionNodes = PsiTreeUtil.findChildrenOfType(annotationDefinition,
                FieldDefinitionNode.class);
        List<LookupElement> results = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                (IdentifierPSINode) annotationName, PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        return results.toArray(new LookupElement[results.size()]);
    }
}
