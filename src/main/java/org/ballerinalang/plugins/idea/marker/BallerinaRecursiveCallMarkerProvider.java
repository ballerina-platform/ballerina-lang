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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.FunctionUtil;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class BallerinaRecursiveCallMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        // Check whether the element is an instance of IdentifierPSINode since recursion can only happen for those
        // (function name and function invocation both are instance of IdentifierPSINode).
        if (element instanceof IdentifierPSINode) {
            // If it is a function invocation, there should be a parent of NameReferenceNode.
            NameReferenceNode parent = PsiTreeUtil.getParentOfType(element, NameReferenceNode.class);
            if (parent == null) {
                return null;
            }
            // Get the SimpleTypeNode child because the function name is inside the SimpleTypeNode child.
            SimpleTypeNode simpleTypeNode = PsiTreeUtil.getChildOfType(parent, SimpleTypeNode.class);
            if (simpleTypeNode == null) {
                return null;
            }
            // Get the identifier.
            PsiElement identifier = simpleTypeNode.getNameIdentifier();
            if (identifier == null) {
                return null;
            }
            // Get the reference.
            PsiReference reference = identifier.getReference();
            if (reference == null) {
                return null;
            }
            // Resolve the reference.
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return null;
            }
            // Find the common context. For a recursive call, the common context should be a FunctionNode.
            PsiElement commonContext = PsiTreeUtil.findCommonContext(parent, resolvedElement);
            if (commonContext instanceof FunctionNode) {
                // Return a new line marker.
                return new RecursiveMethodCallMarkerInfo(element);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }

    private static class RecursiveMethodCallMarkerInfo extends LineMarkerInfo<PsiElement> {
        private RecursiveMethodCallMarkerInfo(@NotNull PsiElement methodCall) {
            super(methodCall, methodCall.getTextRange(), AllIcons.Gutter.RecursiveMethod, Pass.LINE_MARKERS,
                    FunctionUtil.constant("Recursive call"), null, GutterIconRenderer.Alignment.RIGHT);
        }
    }
}
