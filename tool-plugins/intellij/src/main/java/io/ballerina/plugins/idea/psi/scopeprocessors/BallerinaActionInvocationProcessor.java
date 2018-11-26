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

package io.ballerina.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for resolving and completing action invocations.
 */
public class BallerinaActionInvocationProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaActionInvocationProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
            boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return true;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(myElement);
            if (prevVisibleLeaf != null && prevVisibleLeaf instanceof LeafPsiElement) {
                if (((LeafPsiElement) prevVisibleLeaf).getElementType() == BallerinaTypes.RARROW) {
                    PsiElement connector = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                    if (connector == null) {
                        return true;
                    }
                    PsiReference reference = connector.getReference();
                    if (reference == null) {
                        return true;
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null) {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isCompletion() {
        return myIsCompletion;
    }

    @Override
    protected boolean crossOff(@NotNull PsiElement e) {
        return false;
    }
}
