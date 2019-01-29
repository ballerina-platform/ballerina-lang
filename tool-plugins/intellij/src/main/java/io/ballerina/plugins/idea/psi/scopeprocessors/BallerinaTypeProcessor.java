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
import com.intellij.psi.ResolveState;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.psi.BallerinaDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing types.
 */
public class BallerinaTypeProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaTypeProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element) {
        super(element);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaFile;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            List<BallerinaDefinition> definitions = ((BallerinaFile) element).getDefinitions();
            for (BallerinaDefinition definition : definitions) {
                ProgressManager.checkCanceled();
                PsiElement lastChild = definition.getLastChild();
                if (lastChild instanceof BallerinaDefinition) {
                    lastChild = lastChild.getLastChild();
                }
                if (lastChild instanceof BallerinaTypeDefinition) {
                    BallerinaTypeDefinition child = (BallerinaTypeDefinition) lastChild;
                    PsiElement identifier = child.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createTypeLookupElement(child));
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
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
