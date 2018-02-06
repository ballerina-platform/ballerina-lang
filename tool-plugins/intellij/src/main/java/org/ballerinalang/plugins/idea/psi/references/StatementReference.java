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

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class StatementReference extends BallerinaElementReference {

    public StatementReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        if (prevVisibleLeaf != null && ":".equals(prevVisibleLeaf.getText())) {
            PsiElement packageNameNode = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (packageNameNode == null) {
                return null;
            }
            PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
            if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
                return null;
            }
            PsiDirectory containingPackage = (PsiDirectory) resolvedElement;
            return BallerinaPsiImplUtil.resolveElementInPackage(containingPackage, identifier, true, true, true,
                    true, true, true, false, false);
        } else {
            return BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true, true);
        }
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        if (prevVisibleLeaf == null) {
            return new LookupElement[0];
        }
        if (":".equals(prevVisibleLeaf.getText())) {
            PsiElement packageNameNode = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (packageNameNode == null || !(packageNameNode instanceof IdentifierPSINode)) {
                return new LookupElement[0];
            }
            results.addAll(getVariantsFromPackage(packageNameNode));
        } else if (".".equals(prevVisibleLeaf.getText())) {
            // Todo - suggest length field
            PsiElement previousField = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (previousField == null) {
                return new LookupElement[0];
            }
            PsiReference reference = previousField.findReferenceAt(0);
            if (reference == null) {
                return new Object[0];
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return new LookupElement[0];
            }

            // Todo - use util method
            PsiElement resolvedElementParent = resolvedElement.getParent();
            StructDefinitionNode structDefinitionNode = null;
            // Resolve the corresponding resolvedElementParent to get the struct definition.
            if (resolvedElementParent instanceof VariableDefinitionNode) {
                structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode
                        (((VariableDefinitionNode) resolvedElementParent));
            } else if (resolvedElementParent instanceof FieldDefinitionNode) {
                structDefinitionNode =
                        BallerinaPsiImplUtil.resolveTypeNodeStruct((resolvedElementParent));
            }
            if (structDefinitionNode == null) {
                return results.toArray(new LookupElement[results.size()]);
            }
            Collection<FieldDefinitionNode> fieldDefinitionNodes =
                    PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);

            List<LookupElement> fields = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                    (IdentifierPSINode) resolvedElement, PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            results.addAll(fields);
        } else {
            results.addAll(getVariantsFromCurrentPackage());
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();

        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        results.addAll(packages);

        // Todo - use a util method
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {
            int caretOffset = identifier.getStartOffset();

            List<IdentifierPSINode> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                    caretOffset);
            results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                    caretOffset);
            results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

            List<IdentifierPSINode> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope
                    (scope);
            results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        }

        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage != null) {
            List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage,
                    true, true);
            results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));
        }
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PsiElement packageNameNode) {
        List<LookupElement> results = new LinkedList<>();

        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return results;
        }
        PsiDirectory containingPackage = (PsiDirectory) resolvedElement;

        // Todo - add util method
        List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage,
                false, false);
        functions = functions.stream()
                .filter(function -> !BallerinaPsiImplUtil.isAttachedFunction(function))
                .collect(Collectors.toList());
        results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));

        List<IdentifierPSINode> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage,
                false, false);
        results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                AddSpaceInsertHandler.INSTANCE));

        List<IdentifierPSINode> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage, false,
                false);
        results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

        List<IdentifierPSINode> globalVariables =
                BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(containingPackage, false, false);
        results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

        List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(containingPackage,
                false, false);
        results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));

        List<IdentifierPSINode> enums = BallerinaPsiImplUtil.getAllEnumsFromPackage(containingPackage,
                false, false);
        results.addAll(BallerinaCompletionUtils.createEnumLookupElements(enums, AddSpaceInsertHandler.INSTANCE));

        return results;
    }
}
