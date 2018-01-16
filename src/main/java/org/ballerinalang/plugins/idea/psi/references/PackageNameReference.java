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

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.SourceNotationNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PackageNameReference extends BallerinaElementReference implements PsiPolyVariantReference {

    public PackageNameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode = PsiTreeUtil.getParentOfType(identifier,
                FullyQualifiedPackageNameNode.class);

        if (fullyQualifiedPackageNameNode != null) {
            return new LookupElement[0];
        }
        List<LookupElement> results = new ArrayList<>();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        // We don't need to add ':' at the end of the package name in SourceNotationNode.
        InsertHandler<LookupElement> importedPackagesInsertHandler =
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
        InsertHandler<LookupElement> unImportedPackagesInsertHandler =
                AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP;
        SourceNotationNode sourceNotationNode = PsiTreeUtil.getParentOfType(identifier, SourceNotationNode.class);
        if (sourceNotationNode != null) {
            importedPackagesInsertHandler = null;
            unImportedPackagesInsertHandler = AutoImportInsertHandler.INSTANCE;
        }

        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                importedPackagesInsertHandler, true, unImportedPackagesInsertHandler);
        results.addAll(packages);
        return results.toArray(new LookupElement[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        if (resolveResults.length != 0) {
            return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
        }

        IdentifierPSINode identifier = getElement();
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope == null) {
            return null;
        }
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
        return null;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        IdentifierPSINode identifier = getElement();
        if (identifier == null) {
            return new ResolveResult[0];
        }
        AliasNode aliasNode = PsiTreeUtil.getParentOfType(identifier, AliasNode.class);
        if (aliasNode != null) {
            return new ResolveResult[0];
        }

        List<ResolveResult> results = new ArrayList<>();
        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(identifier,
                ImportDeclarationNode.class);
        if (importDeclarationNode != null) {
            List<PsiDirectory> directories = BallerinaPsiImplUtil.resolveDirectory(identifier);
            for (PsiDirectory directory : directories) {
                results.add(new PsiElementResolveResult(directory));
            }
            // Return the results.
            return results.toArray(new ResolveResult[results.size()]);
        }

        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.getParentOfType(identifier,
                PackageDeclarationNode.class);
        if (packageDeclarationNode != null) {
            // If this is a package declaration, resolve the directory.
            List<PsiDirectory> directories = BallerinaPsiImplUtil.resolveDirectory(identifier);

            for (PsiDirectory directory : directories) {
                results.add(new PsiElementResolveResult(directory));
            }
            // Return the results.
            return results.toArray(new ResolveResult[results.size()]);
        }

        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return new ResolveResult[0];
        }

        List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackagesInCurrentFile(containingFile);
        for (PsiElement importedPackage : importedPackages) {
            String packageName = importedPackage.getText();
            if (packageName == null || packageName.isEmpty()) {
                continue;
            }
            if (packageName.equals(identifier.getText())) {
                PsiReference reference = importedPackage.findReferenceAt(0);
                if (reference != null) {
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement != null) {
                        results.add(new PsiElementResolveResult(resolvedElement));
                    }
                }
            }
        }

        importedPackages = BallerinaPsiImplUtil.getPackagesImportedAsAliasInCurrentFile(containingFile);
        for (PsiElement importedPackage : importedPackages) {
            String packageName = importedPackage.getText();
            if (packageName == null || packageName.isEmpty()) {
                continue;
            }
            if (packageName.equals(identifier.getText())) {
                IdentifierPSINode nameNode = PsiTreeUtil.findChildOfType(importedPackage, IdentifierPSINode.class);
                if (nameNode != null) {
                    results.add(new PsiElementResolveResult(nameNode));
                }
            }
        }
        return results.toArray(new ResolveResult[results.size()]);
    }
}
