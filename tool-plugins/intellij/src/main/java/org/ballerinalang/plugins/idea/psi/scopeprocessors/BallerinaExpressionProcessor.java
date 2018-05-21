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

package org.ballerinalang.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.BallerinaMatchExpressionPatternClause;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeName;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for resolving and completing definitions in statements.
 */
public class BallerinaExpressionProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaExpressionProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                        boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaMatchExpressionPatternClause;
    }

    @Override
    public boolean execute(@NotNull PsiElement scopeElement, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(scopeElement)) {
            if (scopeElement instanceof BallerinaMatchExpressionPatternClause) {
                PsiElement identifier = ((BallerinaMatchExpressionPatternClause) scopeElement).getIdentifier();
                if (identifier != null) {
                    if (myResult != null) {
                        BallerinaTypeName typeName = ((BallerinaMatchExpressionPatternClause) scopeElement)
                                .getTypeName();
                        myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                BallerinaPsiImplUtil.formatBallerinaTypeName(typeName)));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
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
