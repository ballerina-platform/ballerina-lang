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
 *
 */

package org.ballerinalang.plugins.idea.psi.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaAttachedObject;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaIdentifier;
import org.ballerinalang.plugins.idea.psi.scopeprocessors.BallerinaObjectFunctionProcessor;
import org.ballerinalang.plugins.idea.psi.scopeprocessors.BallerinaScopeProcessor;
import org.ballerinalang.plugins.idea.psi.scopeprocessors.BallerinaScopeProcessorBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for resolving object functions.
 */
public class BallerinaObjectFunctionReference extends BallerinaCachedReference<BallerinaIdentifier> {

    public BallerinaObjectFunctionReference(@NotNull BallerinaIdentifier element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
        BallerinaScopeProcessorBase processor = new BallerinaObjectFunctionProcessor(null, myElement, false);
        processResolveVariants(processor);
        return processor.getResult();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    public boolean processResolveVariants(@NotNull BallerinaScopeProcessor processor) {
        BallerinaFunctionDefinition ballerinaFunctionDefinition = PsiTreeUtil.getParentOfType(myElement,
                BallerinaFunctionDefinition.class);

        if (ballerinaFunctionDefinition == null) {
            return true;
        }

        BallerinaAttachedObject attachedObject = ballerinaFunctionDefinition.getAttachedObject();
        if (attachedObject == null) {
            return true;
        }

        PsiElement identifier = attachedObject.getIdentifier();

        PsiReference reference = identifier.getReference();
        if (reference == null) {
            return true;
        }

        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return true;
        }

        // Get suggestions from current file.
        return processor.execute(resolvedElement.getParent(), ResolveState.initial());
    }
}
