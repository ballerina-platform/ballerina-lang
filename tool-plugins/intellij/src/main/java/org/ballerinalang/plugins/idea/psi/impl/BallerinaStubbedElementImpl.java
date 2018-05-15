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

package org.ballerinalang.plugins.idea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import org.ballerinalang.plugins.idea.psi.BallerinaCompositeElement;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation class of the stubbed element class.
 *
 * @param <T> stub base
 */
public abstract class BallerinaStubbedElementImpl<T extends StubBase<?>> extends StubBasedPsiElementBase<T>
        implements BallerinaCompositeElement {

    public BallerinaStubbedElementImpl(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public BallerinaStubbedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return getElementType().toString();
    }

    @Nullable
    @Override
    public String getText() {
        return super.getText();
    }

    @Override
    public PsiElement getParent() {
        return getParentByStub();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        return BallerinaCompositeElementImpl.processDeclarationsDefault(this, processor, state, lastParent, place);
    }

    @NotNull
    @Override
    public BallerinaFile getContainingFile() {
        return (BallerinaFile) super.getContainingFile();
    }

    @Override
    public boolean shouldGoDeeper() {
        return true;
    }
}
