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

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PackageNameReference extends BallerinaElementReference {

    public PackageNameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof PsiDirectory || def instanceof PackageNameNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> results = new ArrayList<>();
        // Get the current element. This element will be an identifier element.
        PsiElement identifierElement = getElement();
        if (identifierElement == null) {
            return new ResolveResult[0];
        }
        AliasNode aliasNode = PsiTreeUtil.getParentOfType(identifierElement, AliasNode.class);
        if (aliasNode != null) {
            // Return the results.
            return results.toArray(new ResolveResult[results.size()]);
        }

        // Check whether this is in a import statement.
        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(identifierElement,
                ImportDeclarationNode.class);
        if (importDeclarationNode != null) {
            // If this is an import declaration, resolve the directory.
            PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory(identifierElement);
            for (PsiDirectory directory : directories) {
                results.add(new PsiElementResolveResult(directory));
            }
            // Return the results.
            return results.toArray(new ResolveResult[results.size()]);
        }

        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.getParentOfType(identifierElement,
                PackageDeclarationNode.class);
        if (packageDeclarationNode != null) {
            // If this is a package declaration, resolve the directory.
            PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory(identifierElement);

            for (PsiDirectory directory : directories) {
                results.add(new PsiElementResolveResult(directory));
            }
            // Return the results.
            return results.toArray(new ResolveResult[results.size()]);
        }

        // If this package node is not in an import declaration or a package declaration, that means the current
        // packageName is in a NameReferenceNode. So we need to resolve the package name to the corresponding import
        // declaration and from there, resolve to the correct directory.

        // Get all imported packages in the file.
        List<PsiElement> packages =
                BallerinaPsiImplUtil.getImportedPackagesInCurrentFile(identifierElement.getContainingFile());
        for (PsiElement pack : packages) {
            // For all packages, check whether the identifier is equal to the package name. If they are equal,
            // that means that the current element is what we are looking for.
            if (identifierElement.getText().equals(pack.getText())) {
                // Get the identifier of the package name from the import declaration.
                PsiElement[] children = pack.getChildren();
                if (children.length == 0) {
                    continue;
                }
                // Resolve the directory. Ideally this should return only one element because imports are unique.
                // But this can happen if two or more imported  packages have the same. In that case, we suggest
                // all matching packages.
                PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory(children[0]);
                for (PsiDirectory directory : directories) {
                    results.add(new PsiElementResolveResult(directory));
                }
            }
        }

        packages = BallerinaPsiImplUtil.getPackagesImportedAsAliasInCurrentFile(identifierElement.getContainingFile());
        for (PsiElement pack : packages) {
            // For all packages, check whether the identifier is equal to the package name. If they are equal,
            // that means that the current element is what we are looking for.
            if (identifierElement.getText().equals(pack.getText())) {
                results.add(new PsiElementResolveResult(pack));
            }
        }
        // Return the results.
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent())) {
            definitionElement = definitionElement.getParent();
        }
        // Check whether the definition element is a definition node (PsiDirectory).
        if (isDefinitionNode(definitionElement)) {
            AliasNode aliasNode = PsiTreeUtil.getParentOfType(definitionElement, AliasNode.class);
            if (aliasNode != null) {
                ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(myElement,
                        ImportDeclarationNode.class);
                if (importDeclarationNode != null) {
                    return false;
                }
                String defName = definitionElement.getText();
                String refName = myElement.getText();
                return (defName != null) && refName.equals(defName);
            } else {
                PsiReference reference = myElement.getReference();
                if (reference == null) {
                    return false;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
                    return false;
                }
                if (!resolvedElement.equals(definitionElement)) {
                    return false;
                }
                String defName;
                // If the definitionElement is a instanceof PsiJavaDirectoryImpl, the directory name will be taken as
                // defName. Otherwise the text of the node is taken as the defName.
                if (definitionElement instanceof PsiJavaDirectoryImpl) {
                    defName = ((PsiJavaDirectoryImpl) definitionElement).getName();
                } else {
                    defName = definitionElement.getText();
                }
                // Check whether the refName and defName are equal to find a match.
                String refName = myElement.getName();
                return (refName != null) && (defName != null) && refName.equals(defName);
            }
        }
        return false;
    }
}
