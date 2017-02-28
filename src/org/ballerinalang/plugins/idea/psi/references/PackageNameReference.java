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
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PackageNameReference extends BallerinaElementReference {

    public PackageNameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        // Todo - Check whether the node is the last node in the packagePath. This should be done in isReferenceTo()
        // of reference class
        return def instanceof PackageNameNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
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

        // Check whether this is in a import statement.
        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(identifierElement,
                ImportDeclarationNode.class);
        if (importDeclarationNode == null) {
            // If this is not in an import statement, we need to resolve the package name to the corresponding import
            // declaration.
            // Get all imported packages in the file.
            List<PsiElement> packages =
                    BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(identifierElement.getContainingFile());
            // Todo - Show packages which are not imported as well.
            for (PsiElement pack : packages) {
                // For all packages, check whether the identifier is equal to the package name. If they are equal,
                // that means that the current element is what we are looking for.
                if (identifierElement.getText().equals(pack.getText())) {
                    // Get the identifier of the package name from the import declaration.
                    PsiElement[] children = pack.getChildren();
                    if (children.length == 0) {
                        continue;
                    }
                    // Resolve the directory. Ideally this should return only on element because imports are unique.
                    // But this can happen if two or more imported  packages have the same. In that case, we suggest
                    // all matching packages.
                    PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory(children[0]);
                    for (PsiDirectory directory : directories) {
                        results.add(new PsiElementResolveResult(directory));
                    }
                }
            }
        } else {
            // If the identifier is in an import declaration,resolve the directory.
            PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory(identifierElement);

            for (PsiDirectory directory : directories) {
                results.add(new PsiElementResolveResult(directory));
            }
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        String refName = myElement.getName();
        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent())) {
            definitionElement = definitionElement.getParent();
        }
        if (isDefinitionNode(definitionElement)) {
            PsiElement id = ((PsiNameIdentifierOwner) definitionElement).getNameIdentifier();
            String defName = id != null ? id.getText() : null;

            return refName != null && defName != null && refName.equals(defName);
        }
        return false;
    }
}
