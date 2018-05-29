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

package io.ballerina.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.ballerinalang.plugins.idea.psi.BallerinaCallableUnitSignature;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaFieldReference;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaInvocationReference;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaNameReferenceReference;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaObjectFieldReference;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaObjectFunctionReference;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaTypeReference;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Completion contributor which is responsible for code completion suggestions.
 */
public class BallerinaCompletionContributor extends CompletionContributor {

    public BallerinaCompletionContributor() {
        extend(CompletionType.BASIC, isBallerinaNameReference(), new BallerinaReferenceCompletionProvider());
        extend(CompletionType.BASIC, isBallerinaTypeReference(), new BallerinaReferenceCompletionProvider());
        extend(CompletionType.BASIC, isBallerinaObjectFunctionReference(), new BallerinaReferenceCompletionProvider());
        extend(CompletionType.BASIC, isBallerinaObjectFieldReference(), new BallerinaReferenceCompletionProvider());
        extend(CompletionType.BASIC, isBallerinaFieldReference(), new BallerinaReferenceCompletionProvider());
        extend(CompletionType.BASIC, isBallerinaInvocationReference(), new BallerinaReferenceCompletionProvider());
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaNameReference() {
        return psiElement().withReference(BallerinaNameReferenceReference.class);
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaTypeReference() {
        return psiElement().withReference(BallerinaTypeReference.class);
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaObjectFunctionReference() {
        return psiElement().withReference(BallerinaObjectFunctionReference.class);
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaObjectFieldReference() {
        return psiElement().withReference(BallerinaObjectFieldReference.class);
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaFieldReference() {
        return psiElement().withReference(BallerinaFieldReference.class);
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaInvocationReference() {
        return psiElement().withReference(BallerinaInvocationReference.class);
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        // To ignore auto popup when typing :: in function signature.
        if (position.getParent() instanceof BallerinaCallableUnitSignature) {
            return false;
        }
        if (position instanceof LeafPsiElement) {
            if (((LeafPsiElement) position).getElementType() == BallerinaTypes.SUB && typeChar == '>') {
                return true;
            }
        }
        return typeChar == ':' || typeChar == '@';
    }
}
