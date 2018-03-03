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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.AnnotationReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.getPackageAsLookups;

/**
 * Provides code completion support.
 */
public class BallerinaCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        // The file will be loaded to memory and and will be edited. parameters.getOriginalFile()
        // contains the original file. parameters.getPosition().getContainingFile() will return null
        // because it only exists in the memory. So use parameters.getOriginalFile().getContainingFile()
        // if you want to get the details like containing directory, etc.
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();

        if (parent instanceof PackageNameNode) {
            handlePackageNameNode(parameters, result);
        } else if (parent instanceof ImportDeclarationNode) {
            handleImportDeclarationNode(parameters, result);
        }

        // We only show all annotations in a package if the user entered extended completion(Ctrl+Space twice or more).
        // Otherwise we only suggest attachable annotations only.
        if (parameters.isExtendedCompletion()) {
            PsiReference reference = element.findReferenceAt(0);
            if (reference instanceof AnnotationReference) {
                List<LookupElement> variants = ((AnnotationReference) reference).getVariants(true);
                result.addAllElements(variants);
                result.addLookupAdvertisement("Showing all annotations in the package");
            }
        }
    }

    /**
     * Add lookups for package declarations.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    private void handlePackageNameNode(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement superParent = parent.getParent();
        // Check whether we are in a package declaration node
        if (superParent.getParent() instanceof PackageDeclarationNode) {
            // If we are in a package declaration node, suggest packages.
            addPackageSuggestions(resultSet, element);
        } else if (superParent.getParent() instanceof ImportDeclarationNode &&
                !(superParent instanceof AliasNode)) {
            // If the parent is not an AliasNode and is inside the ImportDeclarationNode, we need to suggest
            // packages.
            addImportSuggestions(resultSet, element);
        }
    }

    /**
     * Adds package suggestions in the package declaration nodes.
     *
     * @param resultSet      result list which is used to add lookups
     * @param identifierNode node which contains the package name
     */
    private void addPackageSuggestions(@NotNull CompletionResultSet resultSet, @NotNull PsiElement identifierNode) {
        // If we are in a package declaration, we only need to suggest the path to the current file.
        PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestCurrentPackagePath(identifierNode);
        for (PsiDirectory directory : psiDirectories) {
            InsertHandler<LookupElement> insertHandler;
            // If the package does have any sub packages, we need to add the insert handler with auto popup
            // enabled with will add '.' at the end of the package and show the sub packages in the popup.
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                // If the package does not have sub packages, we need to add the ';' at the end.
                insertHandler = StatementCompletionInsertHandler.INSTANCE;
            }
            // Add directories as lookup elements.
            resultSet.addElement(getPackageAsLookups(directory, insertHandler));
        }
    }

    /**
     * Adds package suggestions in the import declaration nodes.
     *
     * @param resultSet      result list which is used to add lookups
     * @param identifierNode node which contains the package name
     */
    private void addImportSuggestions(@NotNull CompletionResultSet resultSet, @NotNull PsiElement identifierNode) {
        // Suggest import packages.
        PsiDirectory[] packageDirectories = BallerinaPsiImplUtil.suggestImportPackages(identifierNode);
        // Get names of all imported packages.
        List<String> allImportedPackages =
                BallerinaPsiImplUtil.getImportedPackages(identifierNode.getContainingFile()).stream()
                        .map(PsiElement::getText)
                        .collect(Collectors.toList());

        // Add each directory as lookup elements.
        for (PsiDirectory directory : packageDirectories) {
            InsertHandler<LookupElement> insertHandler;
            // If the package has sub packages, use the auto popup insert handler.
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                // If there are no sub packages and the current package name is already imported, use the
                // AliasCompletionInsertHandler.
                if (allImportedPackages.contains(directory.getName())) {
                    insertHandler = AliasCompletionInsertHandler.INSTANCE;
                } else {
                    // If the current package is not imported previously, use StatementCompletionInsertHandler.
                    insertHandler = StatementCompletionInsertHandler.INSTANCE;
                }
            }
            resultSet.addElement(getPackageAsLookups(directory, insertHandler));
        }
    }

    /**
     * Add lookups for import declarations.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleImportDeclarationNode(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestImportPackages(element);
        for (PsiDirectory directory : psiDirectories) {
            InsertHandler<LookupElement> insertHandler;
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                insertHandler = StatementCompletionInsertHandler.INSTANCE;
            }
            resultSet.addElement(LookupElementBuilder.create(directory).withInsertHandler(insertHandler));
        }
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return typeChar == ':' || typeChar == '@';
    }
}
