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
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an annotation reference.
 */
public class AnnotationReference extends BallerinaElementReference {

    public AnnotationReference(@NotNull IdentifierPSINode element) {
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
            results.addAll(getVariantsFromCurrentPackage(false));
        } else {
            results.addAll(getVariantsFromPackage(packageNameNode, false));
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @NotNull
    public List<LookupElement> getVariants(boolean allAnnotations) {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            results.addAll(getVariantsFromCurrentPackage(allAnnotations));
        } else {
            results.addAll(getVariantsFromPackage(packageNameNode, allAnnotations));
        }
        return results;
    }

    @Nullable
    private PsiElement resolveInCurrentPackage() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        PsiFile containingFile = parent.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory currentPackage = originalFile.getParent();

        if (currentPackage == null) {
            return null;
        }
        return findMatchingAnnotationDefinition(currentPackage, true);
    }

    @Nullable
    private PsiElement resolveInPackage(@NotNull PackageNameNode packageNameNode) {
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return null;
        }
        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        return findMatchingAnnotationDefinition(psiDirectory, false);
    }

    @Nullable
    private PsiElement findMatchingAnnotationDefinition(@NotNull PsiDirectory currentPackage, boolean includePrivate) {
        IdentifierPSINode identifier = getElement();
        List<IdentifierPSINode> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(currentPackage,
                includePrivate, true);
        for (IdentifierPSINode annotation : annotations) {
            String text = annotation.getText();
            if (text.equals(identifier.getText())) {
                return annotation;
            }
        }
        return null;
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage(boolean allAnnotations) {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();
        PsiFile containingFile = parent.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        results.addAll(packages);

        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage == null) {
            return results;
        }
        List<IdentifierPSINode> annotations;
        if (allAnnotations) {
            annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(containingPackage, true, true);
        } else {
            String attachmentType = BallerinaPsiImplUtil.getAttachmentType(identifier);
            if (attachmentType == null) {
                return results;
            }
            annotations = BallerinaPsiImplUtil.getAllAnnotationAttachmentsForType(containingPackage, attachmentType,
                    true, true);
        }
        results.addAll(BallerinaCompletionUtils.createAnnotationLookupElements(annotations));
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode,
                                                       boolean allAnnotations) {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return results;
        }
        PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
        List<IdentifierPSINode> annotations;
        if (allAnnotations) {
            annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(resolvedPackage, false, true);
        } else {
            String attachmentType = BallerinaPsiImplUtil.getAttachmentType(identifier);
            if (attachmentType == null) {
                return results;
            }
            annotations = BallerinaPsiImplUtil.getAllAnnotationAttachmentsForType(resolvedPackage, attachmentType,
                    false, true);
        }
        results.addAll(BallerinaCompletionUtils.createAnnotationLookupElements(annotations));
        return results;
    }
}
