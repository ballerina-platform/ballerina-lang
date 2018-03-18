/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea.lang;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.XmlAttribNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaElementFactory;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Optimizes Ballerina imports.
 */
public class BallerinaImportOptimizer implements ImportOptimizer {

    @Override
    public boolean supports(PsiFile file) {
        return file instanceof BallerinaFile;
    }

    @NotNull
    @Override
    public Runnable processFile(PsiFile file) {
        if (!(file instanceof BallerinaFile)) {
            return EmptyRunnable.getInstance();
        }

        // Get all imported packages in the file.
        List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(file);
        // If there are no imported packages, we don't need to optimize imports.
        if (importedPackages.isEmpty()) {
            return EmptyRunnable.getInstance();
        }

        return new CollectingInfoRunnable() {

            private int removedImports;

            @Override
            public void run() {
                // Get all of used imports in the file.
                List<ImportDeclarationNode> usedImportDeclarations = getUsedImportDeclarations(file);
                // Iterate through all of the imports in the file.
                for (PsiElement importedPackage : importedPackages) {
                    // Get the ImportDeclarationNode which corresponds to the import.
                    ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(importedPackage,
                            ImportDeclarationNode.class);
                    // If an ImportDeclarationNode is not found, continue with the next import.
                    if (importDeclarationNode == null) {
                        continue;
                    }
                    // If ImportDeclarationNode is found, delete it since it is not used.
                    if (!usedImportDeclarations.contains(importDeclarationNode)) {
                        importDeclarationNode.delete();
                    }
                }

                // Sort the used imports.
                Collections.sort(usedImportDeclarations);

                // Get the first element of the list. We start to insert elements after the 1st element. So we
                // consider this element to be the first element which we added to the tree.
                PsiElement addedNode = usedImportDeclarations.get(0);
                for (int i = 1; i < usedImportDeclarations.size(); i++) {
                    // Get an element from the list.
                    ImportDeclarationNode importDeclarationNode = usedImportDeclarations.get(i);
                    // Add it after the element which we have previously added to the tree. Keep the returned value
                    // in a temporary variable.
                    PsiElement temp = addedNode.getParent().addAfter(importDeclarationNode, addedNode);
                    // Add a new line after the previously added node. Otherwise all imports will be in one line. We
                    // need to do it in this order, otherwise the next import will be added to the end of the file.
                    addedNode.getParent().addAfter(BallerinaElementFactory.createNewLine(file.getProject()), addedNode);
                    // Update the added node with the temporary created variable value.
                    addedNode = temp;
                }

                // Now we need to delete old imports.
                for (int i = 1; i < usedImportDeclarations.size(); i++) {
                    usedImportDeclarations.get(i).delete();
                }
                // Calculate removed imports size.
                removedImports = importedPackages.size() - BallerinaPsiImplUtil.getImportedPackages(file).size();
            }

            @Nullable
            @Override
            public String getUserNotificationInfo() {
                if (removedImports > 0) {
                    return "removed " + removedImports + " import" + (removedImports > 1 ? "s" : "");
                }
                return null;
            }
        };
    }

    /**
     * Used to get used imports in a file.
     *
     * @param file a {@link PsiFile} element
     * @return list of used {@link ImportDeclarationNode} in the file.
     */
    private List<ImportDeclarationNode> getUsedImportDeclarations(@NotNull PsiFile file) {
        // This is used to track all packages used in the file.
        List<String> usedPackages = new LinkedList<>();
        Collection<PackageNameNode> packageNameNodes = PsiTreeUtil.findChildrenOfType(file, PackageNameNode.class);
        for (PackageNameNode packageNameNode : packageNameNodes) {
            ProgressManager.checkCanceled();
            if (packageNameNode == null) {
                continue;
            }
            PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.getParentOfType(packageNameNode,
                    PackageDeclarationNode.class);
            if (packageDeclarationNode != null) {
                continue;
            }
            ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(packageNameNode,
                    ImportDeclarationNode.class);
            if (importDeclarationNode != null) {
                continue;
            }
            XmlAttribNode xmlAttribNode = PsiTreeUtil.getParentOfType(packageNameNode, XmlAttribNode.class);
            if (xmlAttribNode != null) {
                continue;
            }

            PsiElement nameIdentifier = packageNameNode.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            usedPackages.add(nameIdentifier.getText());
        }

        List<ImportDeclarationNode> usedImportDeclarations = new LinkedList<>();
        List<String> fullyQualifiedImportedPackages = new LinkedList<>();
        List<String> importedPackages = new LinkedList<>();

        Collection<ImportDeclarationNode> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        for (ImportDeclarationNode importDeclarationNode : importDeclarationNodes) {
            ProgressManager.checkCanceled();
            if (importDeclarationNode == null) {
                continue;
            }
            // Check unused imports. No need to check for fully qualified path since we cant import packages of same
            // name.
            List<PackageNameNode> packageNames = new ArrayList<>(PsiTreeUtil.findChildrenOfType(importDeclarationNode,
                    PackageNameNode.class));
            PackageNameNode lastPackage = ContainerUtil.getLastItem(packageNames);
            if (lastPackage == null) {
                continue;
            }
            String lastPackageName = lastPackage.getText();
            if (!usedPackages.contains(lastPackageName)) {
                continue;
            }

            // Check conflicting imports (which ends with same package name).
            if (importedPackages.contains(lastPackageName)) {
                continue;
            }
            importedPackages.add(lastPackageName);

            // Check redeclared imports.
            FullyQualifiedPackageNameNode fullyQualifiedPackageName = PsiTreeUtil.getChildOfType(importDeclarationNode,
                    FullyQualifiedPackageNameNode.class);
            if (fullyQualifiedPackageName == null) {
                continue;
            }
            if (fullyQualifiedImportedPackages.contains(fullyQualifiedPackageName.getText())) {
                continue;
            }
            fullyQualifiedImportedPackages.add(fullyQualifiedPackageName.getText());
            usedImportDeclarations.add(importDeclarationNode);
        }
        return usedImportDeclarations;
    }
}
