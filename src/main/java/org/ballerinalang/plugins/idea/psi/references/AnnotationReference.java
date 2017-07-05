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
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class AnnotationReference extends BallerinaElementReference {

    public AnnotationReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return false;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        PsiFile containingFile = parent.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            PsiDirectory currentPackage = originalFile.getParent();
            if (currentPackage == null) {
                return null;
            }
            List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(currentPackage);
            for (PsiElement annotation : annotations) {
                String text = annotation.getText();
                if (text != null && text.equals(identifier.getText())) {
                    return annotation;
                }
            }
        } else {
            PsiReference reference = packageNameNode.findReferenceAt(0);
            if (reference == null) {
                return null;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return null;
            }
            PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
            List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(psiDirectory);
            for (PsiElement annotation : annotations) {
                String text = annotation.getText();
                if (text != null && text.equals(identifier.getText())) {
                    return annotation;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        PsiFile containingFile = parent.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        List<LookupElement> results = new LinkedList<>();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            results.addAll(getLocalAnnotations(originalFile));
        } else {
            results.addAll(getAnnotationsFromPackage(packageNameNode));
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @NotNull
    private List<LookupElement> getLocalAnnotations(@NotNull PsiFile originalFile) {
        List<LookupElement> results = new LinkedList<>();
        List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(originalFile);
        for (PsiElement importedPackage : importedPackages) {
            PsiReference reference = importedPackage.findReferenceAt(0);
            if (reference == null) {
                continue;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                continue;
            }
            PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
            LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement(resolvedPackage,
                    PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            results.add(lookupElement);
        }
        return results;
    }

    private List<LookupElement> getAnnotationsFromPackage(@NotNull PackageNameNode packageNameNode) {
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
        List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(resolvedPackage);
        for (PsiElement annotation : annotations) {
            LookupElement lookupElement = BallerinaCompletionUtils.createAnnotationLookupElement(annotation);
            results.add(lookupElement);
        }
        return results;
    }
}
