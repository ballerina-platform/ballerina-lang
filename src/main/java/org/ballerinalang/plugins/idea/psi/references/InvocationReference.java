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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.ConnectorDeclarationStatementNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InvocationReference extends BallerinaElementReference {

    public InvocationReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        PsiElement prevSibling = parent.getPrevSibling();
        if (prevSibling == null) {
            return null;
        }
        PsiReference reference = prevSibling.findReferenceAt(prevSibling.getTextLength());
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        PsiElement definitionNode = resolvedElement.getParent();
        if ((definitionNode instanceof ConnectorDeclarationStatementNode)) {
            //            return new ActionInvocationReference(this);
        } else if (definitionNode instanceof VariableDefinitionNode || definitionNode instanceof ParameterNode
                || definitionNode instanceof GlobalVariableDefinitionNode) {
            ConnectorDefinitionNode connectorDefinitionNode =
                    BallerinaPsiImplUtil.resolveConnectorFromVariableDefinitionNode(definitionNode);
            if (connectorDefinitionNode != null) {
                //                return new ActionInvocationReference(this);
            }
            StructDefinitionNode structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode
                    (definitionNode);
            if (structDefinitionNode != null) {
                //                return new LambdaFunctionReference(this);

                List<IdentifierPSINode> attachedFunctions =
                        BallerinaPsiImplUtil.getAttachedFunctions(structDefinitionNode);
                for (IdentifierPSINode attachedFunction : attachedFunctions) {
                    if (identifier.getText().equals(attachedFunction.getText())) {
                        return attachedFunction;
                    }
                }
            }
        }

        return super.resolve();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        StructDefinitionNode structDefinition = getStructDefinitionNode();
        if (structDefinition == null) {
            return new LookupElement[0];
        }
        List<IdentifierPSINode> attachedFunctions = BallerinaPsiImplUtil.getAttachedFunctions(structDefinition);
        List<LookupElement> results = BallerinaCompletionUtils.createAttachedFunctionsLookupElements(attachedFunctions);
        return results.toArray(new LookupElement[results.size()]);
    }

    private StructDefinitionNode getStructDefinitionNode() {
        IdentifierPSINode identifier = getElement();
        PsiElement structReference = identifier.getPrevSibling();
        if (".".equals(structReference.getText())) {
            structReference = PsiTreeUtil.prevVisibleLeaf(structReference);
        }
        if (structReference == null) {
            return null;
        }
        PsiReference reference = structReference.findReferenceAt(structReference.getTextLength());
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null || !(resolvedElement instanceof IdentifierPSINode)) {
            return null;
        }
        return BallerinaPsiImplUtil.findStructDefinition((IdentifierPSINode) resolvedElement);
    }
}
