/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.psi.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class ActionInvocationReference extends BallerinaElementReference {

    public ActionInvocationReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof ActionDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
//        ResolveResult[] resolveResults = multiResolve(false);
//        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();

        return super.resolve();
    }

//    @NotNull
//    @Override
//    public ResolveResult[] multiResolve(boolean incompleteCode) {
//        List<PsiElement> actions = BallerinaPsiImplUtil.resolveAction(getElement());
//        List<ResolveResult> results = actions.stream()
//                .map(PsiElementResolveResult::new)
//                .collect(Collectors.toList());
//        return results.toArray(new ResolveResult[results.size()]);
//    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent())) {
            definitionElement = definitionElement.getParent();
        }
        if (isDefinitionNode(definitionElement)) {
            PsiReference reference = myElement.getReference();
            if (reference == null) {
                return false;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return false;
            }
            return definitionElement.equals(resolvedElement.getParent());
        }
        return false;
    }
}
