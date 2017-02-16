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

package org.ballerinalang.plugins.idea.psi;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StatementReference extends BallerinaElementReference {

    public StatementReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof PackageNameNode || def instanceof VariableDefinitionNode || def instanceof ParameterNode
                || def instanceof ConstantDefinitionNode || def instanceof SimpleTypeNode
                || def instanceof StructDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        List results = new ArrayList<>();
        String text = getElement().getText();

        PsiElement prevSibling = getElement().getParent().getPrevSibling();
        if (prevSibling != null && prevSibling.getPrevSibling() != null) {
            text = prevSibling.getPrevSibling().getText();
        }

        if (text.endsWith(":")) {
            List<PsiElement> allImportedPackages = BallerinaPsiImplUtil.getAllImportedPackages(getElement());

            for (PsiElement importedPackage : allImportedPackages) {
                if (text.equals(importedPackage.getText() + ":")) {
                    PsiElement packageIdentifier = ((IdentifierDefSubtree) importedPackage).getNameIdentifier();

                    ResolveResult[] resolveResults = ((PackageNameReference) packageIdentifier.getReference())
                            .multiResolve(false);

                    for (ResolveResult resolveResult : resolveResults) {

                        List<PsiElement> allMatchingElementsFromPackage = BallerinaPsiImplUtil
                                .getAllMatchingElementsFromPackage((PsiDirectory) resolveResult.getElement(),
                                        "//functionDefinition");
                        for (PsiElement psiElement : allMatchingElementsFromPackage) {
                            results.add(psiElement);
                        }
                    }
                }
            }
        }
        return results.toArray();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> results = new ArrayList<>();

        PsiElement prevSibling = getElement().getParent().getPrevSibling();
        if (prevSibling != null && prevSibling.getPrevSibling() != null) {
            String text = prevSibling.getPrevSibling().getText();
            if (text.endsWith(":")) {
                List<PsiElement> allImportedPackages = BallerinaPsiImplUtil.getAllImportedPackages(getElement());

                for (PsiElement importedPackage : allImportedPackages) {
                    if (text.equals(importedPackage.getText() + ":")) {
                        PsiElement packageIdentifier = ((IdentifierDefSubtree) importedPackage).getNameIdentifier();

                        PsiReference packageReference = packageIdentifier.getReference();
                        PsiElement resolved = packageReference.resolve();

                        List<PsiElement> allMatchingElementsFromPackage = BallerinaPsiImplUtil
                                .getAllMatchingElementsFromPackage((PsiDirectory) resolved,
                                        "//functionDefinition");
                        for (PsiElement psiElement : allMatchingElementsFromPackage) {
                            results.add(new PsiElementResolveResult(psiElement));
                        }
                    }
                }
            }
        }
        List<PsiElement> functions = BallerinaPsiImplUtil.resolveConnector(getElement());
        for (PsiElement function : functions) {
            results.add(new PsiElementResolveResult(function));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }
}
