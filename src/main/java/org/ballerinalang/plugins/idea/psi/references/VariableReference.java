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
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VariableReference extends BallerinaElementReference {

    public VariableReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof VariableDefinitionNode || def instanceof ParameterNode
                || def instanceof ConstantDefinitionNode || def instanceof FieldDefinitionNode
                || def instanceof GlobalVariableDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // WARNING: If find usage shows an error message, verify that the super.resolve() returns an Identifier node.
//        ResolveResult[] resolveResults = multiResolve(false);
//        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();
        return super.resolve();
    }

//    @NotNull
//    @Override
//    public ResolveResult[] multiResolve(boolean incompleteCode) {
//        // We resolve struct fields using the multiResolve().
//
//        // For struct fields, PSI tree will be 'variableReference.variableReference'. The first variableReference
//        // will resolve to the corresponding struct variable definition. This is already resolvable. So when we
//        // want to resolve the second variable definition, we get the first variableReference and then resolve it.
//        // Most likely, it will resolve into some Node like variableDefinition. Then we get the struct type from
//        // that and resolve and the struct definition node. From there, we resolve the second variableReference
//        // (struct field).
//        PsiElement parent = myElement.getParent();
//        if (parent == null) {
//            return new ResolveResult[0];
//        }
//        PsiElement superParent = parent.getParent();
//        if (superParent == null) {
//            return new ResolveResult[0];
//        }
//        // For struct fields, super parent is also a VariableReferenceNode.
//        if (superParent instanceof VariableReferenceNode) {
//            // Get the previous VariableReferenceNode.
//            VariableReferenceNode prevSibling =
//                    PsiTreeUtil.getPrevSiblingOfType(parent, VariableReferenceNode.class);
//            if (prevSibling == null) {
//                return new ResolveResult[0];
//            }
//            // Get the identifier.
//            PsiElement nameIdentifier = prevSibling.getNameIdentifier();
//            if (nameIdentifier == null) {
//                return new ResolveResult[0];
//            }
//            // Get references.
//            PsiReference[] references = nameIdentifier.getReferences();
//            // This list will be used to store resolved elements.
//            List<ResolveResult> results = new ArrayList<>();
//            // Iterate through all references
//            for (PsiReference reference : references) {
//                // Resolve the reference. This will most likely to resolve to a definition like variableDefinition.
//                PsiElement resolvedElement = reference.resolve();
//                if (resolvedElement == null) {
//                    continue;
//                }
//                // Now we need to get the struct type. So we get the parent element.
//                PsiElement parentNode = resolvedElement.getParent();
//                if (parentNode == null || !(parentNode instanceof VariableDefinitionNode
//                        || parentNode instanceof ParameterNode)) {
//                    continue;
//                }
//                // In a definition, the first child will be the type.
//                PsiElement firstChild = parentNode.getFirstChild();
//                // Todo - Update conditions
//                if (firstChild == null || !(firstChild instanceof TypeNameNode)) {
//                    continue;
//                }
//                // But there can be other children within the first child as well. So we need to navigate into each
//                // child until an IdentifierPSINode is found.
//                while (firstChild != null && !(firstChild instanceof IdentifierPSINode)) {
//                    firstChild = firstChild.getFirstChild();
//                }
//                if (firstChild == null) {
//                    continue;
//                }
//                // Get reference of struct.
//                PsiReference structReference = firstChild.getReference();
//                if (structReference == null) {
//                    continue;
//                }
//                // Resolve the struct.
//                PsiElement resolvedStruct = structReference.resolve();
//                if (resolvedStruct == null || !(resolvedStruct.getParent() instanceof StructDefinitionNode)) {
//                    continue;
//                }
//                // Resolve the field within the struct.
//                PsiElement resolveField = ((StructDefinitionNode) resolvedStruct.getParent()).resolve(myElement);
//                if (resolveField == null) {
//                    continue;
//                }
//                // Add to the result list.
//                results.add(new PsiElementResolveResult(resolveField));
//            }
//            // Return the results.
//            return results.toArray(new ResolveResult[results.size()]);
//        }
//        return new ResolveResult[0];
//    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        return false;
    }
}
