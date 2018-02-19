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
import org.ballerinalang.plugins.idea.psi.EnumDefinitionNode;
import org.ballerinalang.plugins.idea.psi.EnumFieldNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an enum reference.
 */
public class EnumFieldReference extends BallerinaElementReference {

    public EnumFieldReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement dot = PsiTreeUtil.prevVisibleLeaf(identifier);
        if (dot == null || !".".equals(dot.getText())) {
            return null;
        }
        PsiElement enumReferenceNode = PsiTreeUtil.prevVisibleLeaf(dot);
        if (enumReferenceNode == null) {
            return null;
        }
        PsiReference reference = enumReferenceNode.findReferenceAt(enumReferenceNode.getTextLength());
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        PsiElement resolvedElementParent = resolvedElement.getParent();
        if (!(resolvedElementParent instanceof EnumDefinitionNode)) {
            return null;
        }
        return ((EnumDefinitionNode) resolvedElementParent).resolve(identifier);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement dot = PsiTreeUtil.prevVisibleLeaf(identifier);
        if (dot == null || !".".equals(dot.getText())) {
            return results.toArray(new LookupElement[results.size()]);
        }
        PsiElement enumReferenceNode = PsiTreeUtil.prevVisibleLeaf(dot);
        if (enumReferenceNode == null) {
            return results.toArray(new LookupElement[results.size()]);
        }
        PsiReference reference = enumReferenceNode.findReferenceAt(enumReferenceNode.getTextLength());
        if (reference == null) {
            return results.toArray(new LookupElement[results.size()]);
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return results.toArray(new LookupElement[results.size()]);
        }
        PsiElement resolvedElementParent = resolvedElement.getParent();
        if (!(resolvedElementParent instanceof EnumDefinitionNode)) {
            return results.toArray(new LookupElement[results.size()]);
        }
        Collection<EnumFieldNode> fieldDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(resolvedElementParent, EnumFieldNode.class);
        results.addAll(BallerinaCompletionUtils.createEnumFieldLookupElements(fieldDefinitionNodes,
                (IdentifierPSINode) resolvedElement));
        return results.toArray(new LookupElement[results.size()]);
    }
}
