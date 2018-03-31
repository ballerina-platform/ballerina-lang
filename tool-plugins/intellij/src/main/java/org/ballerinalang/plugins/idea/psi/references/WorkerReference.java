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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.JoinClauseNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a worker reference.
 */
public class WorkerReference extends BallerinaElementReference {

    public WorkerReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        ScopeNode scopeNode = PsiTreeUtil.getParentOfType(identifier, ScopeNode.class);
        if (scopeNode == null) {
            return null;
        }
        if (scopeNode instanceof JoinClauseNode) {
            scopeNode = (ScopeNode) scopeNode.getParent();
        }
        List<WorkerDeclarationNode> workerDeclarations = BallerinaPsiImplUtil.getWorkerDeclarationsInScope(scopeNode);
        for (WorkerDeclarationNode workerDeclaration : workerDeclarations) {
            IdentifierPSINode workerName = PsiTreeUtil.getChildOfType(workerDeclaration, IdentifierPSINode.class);
            if (workerName == null) {
                continue;
            }
            if (identifier.getText().equals(workerName.getText())) {
                return workerName;
            }
        }
        return super.resolve();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        ScopeNode scopeNode = PsiTreeUtil.getParentOfType(identifier, ScopeNode.class);
        if (scopeNode == null) {
            return results.toArray(new LookupElement[results.size()]);
        }
        if (scopeNode instanceof JoinClauseNode) {
            scopeNode = (ScopeNode) scopeNode.getParent();
        }
        List<WorkerDeclarationNode> workerDeclarations = BallerinaPsiImplUtil.getWorkerDeclarationsInScope(scopeNode);
        results.addAll(BallerinaCompletionUtils.createWorkerLookupElements(workerDeclarations));
        return results.toArray(new LookupElement[results.size()]);
    }
}
