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
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class NameReference extends BallerinaElementReference {

    public NameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return resolveInCurrentPackage();
        } else {
            return resolveInPackage(packageNameNode);
        }
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            results.addAll(getVariantsFromCurrentPackage());
        } else {
            results.addAll(getVariantsFromPackage(packageNameNode));
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @Nullable
    private PsiElement resolveInCurrentPackage() {
        IdentifierPSINode identifier = getElement();
        PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(identifier);
        // Don't match function invocations to variables
        if (nextVisibleLeaf == null || !"(".equals(nextVisibleLeaf.getText())) {
            PsiElement elementInScope = BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true);
            if (elementInScope != null) {
                return elementInScope;
            }
        }
        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory psiDirectory = originalFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true, true, true, true);
    }

    @Nullable
    private PsiElement resolveInPackage(@NotNull PackageNameNode packageNameNode) {
        IdentifierPSINode identifier = getElement();
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement instanceof PackageNameNode) {
            reference = resolvedElement.findReferenceAt(0);
            if (reference == null) {
                return null;
            }
            resolvedElement = reference.resolve();
        }
        if (!(resolvedElement instanceof PsiDirectory)) {
            return null;
        }
        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true, true, true, true);
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

        if (containingPackage != null) {

            List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                    PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                    AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            results.addAll(packages);

            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
            ANTLRPsiNode definitionParent = PsiTreeUtil.getParentOfType(identifier, CallableUnitBodyNode.class,
                    ServiceBodyNode.class, ResourceDefinitionNode.class, ConnectorBodyNode.class);
            TypeNameNode typeNameNode = PsiTreeUtil.getParentOfType(identifier, TypeNameNode.class);
            if ((definitionParent != null && !(definitionParent instanceof ResourceDefinitionNode)) ||
                    (prevVisibleLeaf != null && !";".equals(prevVisibleLeaf.getText())) && typeNameNode == null) {

                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
                results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

                // Todo - use a util method
                ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                        TopLevelDefinition.class, LowerLevelDefinition.class);
                if (scope != null) {
                    int caretOffset = identifier.getStartOffset();

                    List<PsiElement> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                            caretOffset);
                    results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

                    List<PsiElement> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                            caretOffset);
                    results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

                    List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope
                            (scope);
                    results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                    List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
                    results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
                }
            }

            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                    AddSpaceInsertHandler.INSTANCE));

            List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));
        }
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode) {
        List<LookupElement> results = new LinkedList<>();
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return results;
        }
        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return results;
        }

        PsiDirectory containingPackage = (PsiDirectory) resolvedElement;

        // Todo - use a util method
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                AddSpaceInsertHandler.INSTANCE));

        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

        List<PsiElement> globalVariables =
                BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

        List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));

        return results;
    }
}
