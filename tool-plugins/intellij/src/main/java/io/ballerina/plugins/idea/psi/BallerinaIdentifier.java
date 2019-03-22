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

package io.ballerina.plugins.idea.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import io.ballerina.plugins.idea.psi.impl.BallerinaElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an identifier.
 */
public class BallerinaIdentifier extends LeafPsiElement implements PsiNameIdentifierOwner, NavigatablePsiElement {

    public BallerinaIdentifier(@NotNull IElementType type, CharSequence text) {
        super(type, text);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        replace(BallerinaElementFactory.createIdentifierFromText(getProject(), name));
        return this;
    }

    @Override
    public PsiReference getReference() {
        // Note - Don't need to return references for definitions.
        return null;
    }
}