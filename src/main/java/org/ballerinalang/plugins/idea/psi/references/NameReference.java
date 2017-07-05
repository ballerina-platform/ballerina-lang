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
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class NameReference extends BallerinaElementReference {

    public NameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return false;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        List<LookupElement> results = new LinkedList<>();

        PsiDirectory containingPackage = originalFile.getParent();

        if (containingPackage != null) {

            //            List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(containingFile);
            //            for (PsiElement importedPackage : importedPackages) {
            //                PsiReference reference = importedPackage.findReferenceAt(0);
            //                if (reference == null) {
            //                    continue;
            //                }
            //                PsiElement resolvedElement = reference.resolve();
            //                if (resolvedElement == null) {
            //                    continue;
            //                }
            //                PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
            //                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement
            // (resolvedPackage,
            //                        PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiDirectory> unImportedPackages = BallerinaPsiImplUtil.getAllUnImportedPackages
            // (containingFile);
            //            for (PsiDirectory unImportedPackage : unImportedPackages) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement
            // (unImportedPackage,
            //                        BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage
            // (containingPackage);
            //            for (PsiElement function : functions) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createFunctionsLookupElement
            // (function);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage
            // (containingPackage);
            //            for (PsiElement connector : connectors) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createConnectorLookupElement
            // (connector,
            //                        AddSpaceInsertHandler.INSTANCE);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
            //            for (PsiElement struct : structs) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createStructLookupElement(struct);
            //                results.add(lookupElement);
            //            }

            // Todo - Add all variables
        }

        return results.toArray(new LookupElement[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return resolveLocalElement(identifier);
        } else {
            return resolveElementInPackage(packageNameNode, identifier);
        }
    }

    @Nullable
    private PsiElement resolveLocalElement(IdentifierPSINode identifier) {
        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiDirectory psiDirectory = containingFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        return findMatchingElement(psiDirectory, identifier);
    }

    @Nullable
    private PsiElement resolveElementInPackage(@NotNull PackageNameNode packageNameNode,
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
        return findMatchingElement(psiDirectory, identifier);
    }

    @Nullable
    private PsiElement findMatchingElement(@NotNull PsiDirectory directory,
                                           @NotNull IdentifierPSINode identifier) {
        // Todo - Add all variables

        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage
                (directory);
        for (PsiElement function : functions) {
            if (identifier.getText().equals(function.getText())) {
                return function;
            }
        }

        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage
                (directory);
        for (PsiElement connector : connectors) {
            if (identifier.getText().equals(connector.getText())) {
                return connector;
            }
        }

        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(directory);
        for (PsiElement struct : structs) {
            if (identifier.getText().equals(struct.getText())) {
                return struct;
            }
        }
        return null;
    }
}
