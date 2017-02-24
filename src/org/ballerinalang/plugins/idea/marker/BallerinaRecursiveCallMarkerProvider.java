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
import org.ballerinalang.plugins.idea.psi.CallableUnitNameNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BallerinaRecursiveCallMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        List<PsiElement> callableUnitNameNodes = new ArrayList<>();
        // Iterate through all elements and get CallableUnitNameNodes.
        for (PsiElement element : elements) {
            if (element instanceof CallableUnitNameNode) {
                callableUnitNameNodes.add(element);
            }
        }
        // Iterate through all elements to identify recursive calls.
        for (PsiElement element : elements) {
            // We need to check FunctionNodes only.
            if (!(element instanceof FunctionNode)) {
                continue;
            }
            // Check each FunctionNode against all callableUnitNameNodes to identify recursive calls.
            for (PsiElement node : callableUnitNameNodes) {
                // Get the SimpleTypeNode child element.
                SimpleTypeNode simpleTypeNode = PsiTreeUtil.getChildOfType(node, SimpleTypeNode.class);
                if (simpleTypeNode == null) {
                    continue;
                }
                // Get the identifier.
                PsiElement identifier = simpleTypeNode.getNameIdentifier();
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
                // Find the common context.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(node, element);
                // If the resolved element is same as the current function in the iteration(element), and the
                // common context is also the same function, that means it is a recursive call.
                if (element == resolvedElement.getParent() && element == commonContext) {
                    result.add(new RecursiveMethodCallMarkerInfo(identifier));
                }
            }
        }
    }

    private static class RecursiveMethodCallMarkerInfo extends LineMarkerInfo<PsiElement> {
        private RecursiveMethodCallMarkerInfo(@NotNull PsiElement methodCall) {
            super(methodCall, methodCall.getTextRange(), AllIcons.Gutter.RecursiveMethod, Pass.LINE_MARKERS,
                    FunctionUtil.constant("Recursive call"), null, GutterIconRenderer.Alignment.RIGHT);
        }
    }
}
