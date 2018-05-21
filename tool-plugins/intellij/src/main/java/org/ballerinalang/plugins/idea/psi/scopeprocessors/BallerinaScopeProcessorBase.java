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

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.OrderedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for scope processors.
 */
public abstract class BallerinaScopeProcessorBase extends BallerinaScopeProcessor {

    @NotNull
    private final OrderedSet<PsiElement> myResult = new OrderedSet<>();
    @NotNull
    protected final PsiElement myOrigin;
    @NotNull
    private final PsiElement myRequestedNameElement; // Todo - Remove
    protected final boolean myIsCompletion;

    public BallerinaScopeProcessorBase(@NotNull PsiElement origin) {
        this(origin, origin, false);
    }

    public BallerinaScopeProcessorBase(@NotNull PsiElement origin, boolean completion) {
        this(origin, origin, completion);
    }

    public BallerinaScopeProcessorBase(@NotNull PsiElement requestedNameElement, @NotNull PsiElement origin,
                                       boolean completion) {
        myRequestedNameElement = requestedNameElement;
        myOrigin = origin;
        myIsCompletion = completion;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        return false;
    }

    protected boolean add(@NotNull PsiElement psiElement) {
        return !myResult.add(psiElement);
    }

    @Nullable
    public PsiElement getResult() {
        return ContainerUtil.getFirstItem(myResult);
    }

    @NotNull
    public List<PsiElement> getVariants() {
        return myResult;
    }

    protected abstract boolean crossOff(@NotNull PsiElement e);
}
