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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.completion.ParenthesisInsertHandler;
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

public class FunctionReference extends BallerinaElementReference {

    public FunctionReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return resolveInCurrentPackage(identifier);
        } else {
            return resolveInPackage(packageNameNode, identifier);
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
    private PsiElement resolveInCurrentPackage(@NotNull IdentifierPSINode identifier) {
        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory psiDirectory = originalFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        PsiElement element = BallerinaPsiImplUtil.resolveFunctionInPackage(identifier, true, true, true);
        if (element != null) {
            return element;
        }
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, false, false, false,
                false, false, true, true);
    }

    @Nullable
    private PsiElement resolveInPackage(@NotNull PackageNameNode packageNameNode,
                                        @NotNull IdentifierPSINode identifier) {
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return null;
        }
        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, false, false, false,
                false, false, false, false);
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

        if (containingPackage != null) {
            List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage,
                    true, true);
            results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions,
                    ParenthesisInsertHandler.INSTANCE));
        }

        // Todo - use a util method
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {
            int caretOffset = identifier.getStartOffset();

            List<IdentifierPSINode> variables =
                    BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope, caretOffset);
            for (IdentifierPSINode variable : variables) {
                if (BallerinaPsiImplUtil.isALambdaFunction(variable)) {
                    results.add(BallerinaCompletionUtils.createVariableLookupElement(variable));
                }
            }

            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                    caretOffset);
            for (IdentifierPSINode parameter : parameters) {
                if (BallerinaPsiImplUtil.isALambdaFunction(parameter)) {
                    results.add(BallerinaCompletionUtils.createParameterLookupElement(parameter));
                }
            }

            List<IdentifierPSINode> globalVariables =
                    BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
            for (IdentifierPSINode variable : globalVariables) {
                if (BallerinaPsiImplUtil.isALambdaFunction(variable)) {
                    results.add(BallerinaCompletionUtils.createGlobalVariableLookupElement(variable));
                }
            }
        }

        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        results.addAll(packages);
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode) {
        List<LookupElement> results = new LinkedList<>();
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return results;
        }
        PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
        List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(resolvedPackage, false,
                false);
        results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));
        return results;
    }
}
