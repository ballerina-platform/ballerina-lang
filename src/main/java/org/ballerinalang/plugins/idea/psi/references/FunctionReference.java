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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaAutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.completion.ParenthesisInsertHandler;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
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
        PsiDirectory psiDirectory = containingFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(psiDirectory);
        for (PsiElement function : functions) {
            if (function.getText().equals(identifier.getText())) {
                return function;
            }
        }
        return null;
    }

    @Nullable
    private PsiElement resolveInPackage(@NotNull PackageNameNode packageNameNode,
                                        @NotNull IdentifierPSINode identifier) {
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return null;
        }
        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(psiDirectory);
        for (PsiElement function : functions) {
            String functionName = function.getText();
            if (functionName == null || functionName.isEmpty()) {
                continue;
            }
            if (functionName.equals(identifier.getText())) {
                return function;
            }
        }
        return null;
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

        if (containingPackage != null) {
            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions,
                    ParenthesisInsertHandler.INSTANCE));
        }

        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        results.addAll(packages);
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
        if (resolvedElement == null) {
            return results;
        }
        PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(resolvedPackage);
        results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));
        return results;
    }
}
