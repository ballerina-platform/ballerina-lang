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

package io.ballerina.plugins.idea.psi.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.impl.BallerinaElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for resolving package names.
 */
public class BallerinaPackageNameReference extends BallerinaCachedReference<BallerinaIdentifier> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaPackageNameReference.class);
    private PsiReference reference;

    public BallerinaPackageNameReference(@NotNull BallerinaIdentifier element, PsiReference reference) {
        super(element);
        this.reference = reference;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        // Todo - Update rename logic to consider only the last part of the name (after the last ".").
        myElement.replace(BallerinaElementFactory.createIdentifierFromText(myElement.getProject(), newElementName));
        return myElement;
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
        if (reference != null) {
            try {
                // This try catch is added to handle any issues which occur due to user repo changes.
                return reference.resolve();
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
