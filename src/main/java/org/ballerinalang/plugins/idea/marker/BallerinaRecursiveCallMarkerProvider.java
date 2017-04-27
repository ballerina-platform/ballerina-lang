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

package org.ballerinalang.plugins.idea.marker;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.FunctionUtil;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BallerinaRecursiveCallMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        // This is used to prevent adding multiple line markers to the same line.
        Set<Integer> lines = ContainerUtil.newHashSet();
        for (PsiElement element : elements) {
            // Check whether the element is an instance of IdentifierPSINode since recursion can only happen for those
            // (function name and function invocation both are instance of IdentifierPSINode).
            PsiElement parent = element.getParent();
            PsiElement superParent = null;
            if (parent != null) {
                superParent = parent.getParent();
            }
            if (element instanceof IdentifierPSINode && (parent instanceof FunctionInvocationStatementNode ||
                    (parent instanceof NameReferenceNode && superParent instanceof ExpressionNode))) {
                // If it is a function invocation, there should be a parent of NameReferenceNode.
                NameReferenceNode nameReferenceNode = PsiTreeUtil.getParentOfType(element, NameReferenceNode.class);
                if (nameReferenceNode == null) {
                    continue;
                }
                // Get the identifier.
                PsiElement identifier = nameReferenceNode.getNameIdentifier();
                if (identifier == null) {
                    continue;
                }
                // Get the reference.
                PsiReference reference = identifier.getReference();
                if (reference == null) {
                    continue;
                }
                // Resolve the reference.
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    continue;
                }
                // Get the document manager;
                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
                // Get the document.
                Document document = documentManager.getDocument(element.getContainingFile());
                if (document == null) {
                    continue;
                }
                // Get the offset of the current element.
                int textOffset = element.getTextOffset();
                // Get the line number of the current element.
                int lineNumber = document.getLineNumber(textOffset);
                // Find the common context. For a recursive call, the common context should be a FunctionNode.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(nameReferenceNode, resolvedElement);
                if (commonContext instanceof FunctionNode && !lines.contains(lineNumber)) {
                    // Add the number to the set.
                    lines.add(lineNumber);
                    // Return a new line marker.
                    result.add(new RecursiveMethodCallMarkerInfo(element));
                }
            }
        }
    }

    private static class RecursiveMethodCallMarkerInfo extends LineMarkerInfo<PsiElement> {
        private RecursiveMethodCallMarkerInfo(@NotNull PsiElement methodCall) {
            super(methodCall, methodCall.getTextRange(), BallerinaIcons.RECURSIVE, Pass.LINE_MARKERS,
                    FunctionUtil.constant("Recursive call"), null, GutterIconRenderer.Alignment.RIGHT);
        }
    }
}
