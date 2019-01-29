/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.marker;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.MethodCellRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.FunctionUtil;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.psi.BallerinaAttachedObject;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Provides support to mark object type functions.
 */
public class BallerinaTypeFunctionMarker extends LineMarkerProviderDescriptor {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        // This is used to prevent adding multiple line markers to the same line.
        Set<Integer> lines = ContainerUtil.newHashSet();
        for (PsiElement implementation : elements) {

            if (!(implementation instanceof BallerinaIdentifier)) {
                continue;
            }

            PsiElement parent = implementation.getParent();
            if (parent instanceof BallerinaCallableUnitSignature) {
                BallerinaFunctionDefinition ballerinaFunctionDefinition = PsiTreeUtil.getParentOfType(parent,
                        BallerinaFunctionDefinition.class);
                if (ballerinaFunctionDefinition == null) {
                    continue;
                }
                BallerinaAttachedObject attachedObject = ballerinaFunctionDefinition.getAttachedObject();
                if (attachedObject == null) {
                    continue;
                }

                PsiReference reference = implementation.getReference();
                if (reference == null) {
                    continue;
                }
                PsiElement definition = reference.resolve();
                if (definition == null) {
                    continue;
                }
                addMarker(result, lines, definition, implementation);

            }
        }
    }

    private void addMarker(@NotNull Collection<LineMarkerInfo> result, @NotNull Set<Integer> lines,
                           @NotNull PsiElement definition, @NotNull PsiElement implementation) {
        // Get the document manager;
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(definition.getProject());
        // Get the document.
        Document document = documentManager.getDocument(implementation.getContainingFile());
        if (document == null) {
            return;
        }

        // Get the offset of the current element.
        int textOffset = implementation.getTextOffset();
        // Get the line number of the current element.
        int lineNumber = document.getLineNumber(textOffset);

        if (!lines.contains(lineNumber)) {
            // Add the number to the set.
            lines.add(lineNumber);
            // Return a new line marker.

            result.add(new BallerinaImplementedFunctionMarkerInfo(definition, implementation));
            result.add(new BallerinaImplementingFunctionMarkerInfo(implementation, definition));
        }
    }

    @Nullable
    @Override
    public String getName() {
        return "Ballerina Line Markers";
    }

    private static class BallerinaImplementedFunctionMarkerInfo extends LineMarkerInfo<PsiElement> {
        private BallerinaImplementedFunctionMarkerInfo(@NotNull PsiElement definition,
                                                       @NotNull PsiElement implementation) {
            super(definition,
                    definition.getTextRange(),
                    AllIcons.Gutter.ImplementedMethod,
                    Pass.LINE_MARKERS,
                    FunctionUtil.constant("Implemented function"),
                    (e, elt) -> navigateToOverridingMethod(e, ((NavigatablePsiElement) implementation)),
                    GutterIconRenderer.Alignment.RIGHT
            );
        }
    }

    private static class BallerinaImplementingFunctionMarkerInfo extends LineMarkerInfo<PsiElement> {
        private BallerinaImplementingFunctionMarkerInfo(@NotNull PsiElement implementation,
                                                        @NotNull PsiElement definition) {
            super(implementation,
                    implementation.getTextRange(),
                    AllIcons.Gutter.ImplementingMethod,
                    Pass.LINE_MARKERS,
                    FunctionUtil.constant("Implementing function"),
                    (e, elt) -> navigateToOverridingMethod(e, ((NavigatablePsiElement) definition)),
                    GutterIconRenderer.Alignment.RIGHT
            );
        }
    }

    private static void navigateToOverridingMethod(MouseEvent e, @NotNull NavigatablePsiElement method/*, boolean
    acceptSelf*/) {
        PsiElementListNavigator.openTargets(e, new NavigatablePsiElement[]{method},
                DaemonBundle.message("navigation.title.super.method", method.getText()),
                DaemonBundle.message("navigation.findUsages.title.super.method", method.getText()),
                new MethodCellRenderer(true));
    }
}
