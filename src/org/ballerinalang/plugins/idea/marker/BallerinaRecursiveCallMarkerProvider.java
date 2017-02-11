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
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.FunctionUtil;
import com.intellij.util.containers.ContainerUtil;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.psi.CallableUnitNameNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

        Set<Integer> lines = ContainerUtil.newHashSet();

        List<PsiElement> callableUnitNameNodes = new ArrayList<>();
        for (PsiElement element : elements) {
            if (element instanceof CallableUnitNameNode) {
                callableUnitNameNodes.add(element);
            }
        }

        for (PsiElement element : elements) {
            if (element instanceof FunctionDefinitionNode) {
                for (PsiElement node : callableUnitNameNodes) {
                    // Get the identifier.
                    PsiElement identifier = ((IdentifierDefSubtree) node).getNameIdentifier();
                    // Get the reference.
                    PsiReference reference = identifier.getReference();
                    // Resolve the reference.
                    PsiElement resolvedElement = reference.resolve();
                    // Find the common context.
                    PsiElement commonContext = PsiTreeUtil.findCommonContext(node, element);
                    // If the resolved element is same as the current function in the iteration(element), and the
                    // common context is also the same function, that means it is a recursive call.
                    if (element == resolvedElement && element == commonContext) {
                        PsiDocumentManager instance = PsiDocumentManager.getInstance(node.getProject());
                        Document document = instance.getDocument(node.getContainingFile());
                        int textOffset = node.getTextOffset();
                        if (document == null) {
                            continue;
                        }
                        int lineNumber = document.getLineNumber(textOffset);
                        if (!lines.contains(lineNumber)) {
                            result.add(new RecursiveMethodCallMarkerInfo(node));
                        }
                        lines.add(lineNumber);
                    }
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
