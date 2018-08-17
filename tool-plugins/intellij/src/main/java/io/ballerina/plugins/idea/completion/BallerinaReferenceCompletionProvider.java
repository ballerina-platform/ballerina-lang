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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ProcessingContext;
import io.ballerina.plugins.idea.psi.reference.BallerinaFieldReference;
import io.ballerina.plugins.idea.psi.reference.BallerinaInvocationReference;
import io.ballerina.plugins.idea.psi.reference.BallerinaNameReferenceReference;
import io.ballerina.plugins.idea.psi.reference.BallerinaObjectFieldReference;
import io.ballerina.plugins.idea.psi.reference.BallerinaObjectFunctionReference;
import io.ballerina.plugins.idea.psi.reference.BallerinaTypeReference;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaActionInvocationProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaAnnotationFieldProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaAnonymousServiceConfigProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaBlockProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaEndpointFieldProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaFieldProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaInvocationProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaObjectFieldProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaObjectFunctionProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaPackageNameProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaStatementProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaTopLevelScopeProcessor;
import io.ballerina.plugins.idea.psi.scopeprocessors.BallerinaTypeProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * Ballerina reference provider which provides completions.
 */
public class BallerinaReferenceCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                  @NotNull CompletionResultSet result) {

        PsiElement element = parameters.getPosition();

        PsiReference reference = element.getReference();
        if (reference == null) {
            return;
        }
        // Todo - Refactor
        if (reference instanceof BallerinaNameReferenceReference) {
            BallerinaNameReferenceReference nameReferenceReference = (BallerinaNameReferenceReference) reference;
            if (!nameReferenceReference.processResolveVariants(new BallerinaAnonymousServiceConfigProcessor(result,
                    element, true))) {
                return;
            }
            if (!nameReferenceReference.processResolveVariants(new BallerinaAnnotationFieldProcessor(result, element,
                    true))) {
                return;
            }
            if (!nameReferenceReference.processResolveVariants(new BallerinaEndpointFieldProcessor(result, element,
                    true))) {
                return;
            }
            if (!nameReferenceReference.processResolveVariants(new BallerinaActionInvocationProcessor(result,
                    element, true))) {
                return;
            }
            nameReferenceReference.processResolveVariants(new BallerinaPackageNameProcessor(result, element, true));
            if (!nameReferenceReference.processResolveVariants(new BallerinaStatementProcessor(result, element,
                    true))) {
                return;
            }
            if (!nameReferenceReference.processResolveVariants(new BallerinaBlockProcessor(result, element, true))) {
                return;
            }
            nameReferenceReference.processResolveVariants(new BallerinaTopLevelScopeProcessor(result, element, true));

        } else if (reference instanceof BallerinaTypeReference) {
            BallerinaTypeReference ballerinaTypeReference = (BallerinaTypeReference) reference;
            ballerinaTypeReference.processResolveVariants(new BallerinaTypeProcessor(result, element));
        } else if (reference instanceof BallerinaObjectFunctionReference) {
            BallerinaObjectFunctionReference objectFunctionReference = (BallerinaObjectFunctionReference) reference;
            objectFunctionReference.processResolveVariants(new BallerinaObjectFunctionProcessor(result, element,
                    false));
        } else if (reference instanceof BallerinaObjectFieldReference) {
            BallerinaObjectFieldReference objectFieldReference = (BallerinaObjectFieldReference) reference;
            objectFieldReference.processResolveVariants(new BallerinaObjectFieldProcessor(result, element, false));
        } else if (reference instanceof BallerinaFieldReference) {
            BallerinaFieldReference fieldReference = (BallerinaFieldReference) reference;
            fieldReference.processResolveVariants(new BallerinaFieldProcessor(result, element, false));
        } else if (reference instanceof BallerinaInvocationReference) {
            BallerinaInvocationReference fieldReference = (BallerinaInvocationReference) reference;
            fieldReference.processResolveVariants(new BallerinaInvocationProcessor(result, element, false));
        }
    }
}
