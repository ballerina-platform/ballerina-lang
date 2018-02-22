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
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a namespace reference.
 */
public class NameSpaceReference extends BallerinaElementReference {

    public NameSpaceReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {
            int caretOffset = identifier.getStartOffset();
            List<PsiElement> namespaces = BallerinaPsiImplUtil.getAllXmlNamespacesInResolvableScope(scope, caretOffset);
            for (PsiElement namespace : namespaces) {
                if (namespace == null || namespace.getText().isEmpty()) {
                    continue;
                }
                if (namespace.getText().equals(identifier.getText())) {
                    return namespace;
                }
            }
        }
        return BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true, true);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                    TopLevelDefinition.class, LowerLevelDefinition.class);
            if (scope != null) {
                int caretOffset = identifier.getStartOffset();
                List<PsiElement> namespaces = BallerinaPsiImplUtil.getAllXmlNamespacesInResolvableScope(scope,
                        caretOffset);
                results.addAll(BallerinaCompletionUtils.createNamespaceLookupElements(namespaces));

                List<IdentifierPSINode> variables =
                        BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope, caretOffset);
                results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

                List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                        caretOffset);
                results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

                List<IdentifierPSINode> globalVariables =
                        BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
            }
        }
        return results.toArray(new LookupElement[results.size()]);
    }
}
