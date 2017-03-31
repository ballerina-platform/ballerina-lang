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
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.xpath.XPath;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NameReference extends BallerinaElementReference {

    public NameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return (def instanceof FunctionNode) || (def instanceof ConnectorNode) || (def instanceof StructDefinitionNode)
                || (def instanceof VariableDefinitionNode) || (def instanceof AnnotationDefinitionNode);
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
        // Get the NameReferenceNode parent. This is to resolve functions from other packages. Ex- system:println().
        PsiElement parentElement = PsiTreeUtil.getParentOfType(getElement(), NameReferenceNode.class);
        if (parentElement == null) {
            // Get the TypeNameNode parent. This is to resolve Connectors from packages. Ex- http:ClientConnector.
            parentElement = PsiTreeUtil.getParentOfType(getElement(), TypeNameNode.class);
        }

        // Get the PackagePath node. We need the package path to resolve the corresponding Function/Connector. We use
        // XPath.findAll() here because the PackagePath node might not be a direct child of the parentElement.
        Collection<? extends PsiElement> packagePath =
                XPath.findAll(BallerinaLanguage.INSTANCE, parentElement, "//nameReference");

        // Check whether a packagePath is found.
        if (!packagePath.iterator().hasNext()) {
            return new ResolveResult[0];
        }
        // There cannot be multiple packagePath nodes. So we get the fist package path.
        PsiElement packagePathNode = packagePath.iterator().next();
        if (packagePathNode == null) {
            return new ResolveResult[0];
        }
        // Get the PackageNameNode. We need this to resolve the package.
        PackageNameNode[] packageNameNodes =
                PsiTreeUtil.getChildrenOfType(packagePathNode, PackageNameNode.class);
        if (packageNameNodes == null) {
            return new ResolveResult[0];
        }
        // Get the last PackageNameNode because we only need to resolve the corresponding package.
        PackageNameNode lastPackage = packageNameNodes[packageNameNodes.length - 1];
        if (lastPackage == null) {
            return new ResolveResult[0];
        }
        // Get the identifier from the last package name.
        PsiElement nameIdentifier = lastPackage.getNameIdentifier();
        if (nameIdentifier == null) {
            return new ResolveResult[0];
        }
        // Get the reference.
        PsiReference reference = nameIdentifier.getReference();
        if (reference == null) {
            return new ResolveResult[0];
        }
        // Multi resolve the reference.
        ResolveResult[] resolveResults = ((PackageNameReference) reference).multiResolve(false);
        // Create a new List to save resolved elements.
        List<ResolveResult> results = new ArrayList<>();
        // Iterate through all resolve results.
        for (ResolveResult resolveResult : resolveResults) {
            // Get the element from the resolve result.
            PsiElement element = resolveResult.getElement();
            // Get all functions in the package.
            List<PsiElement> allFunctions =
                    BallerinaPsiImplUtil.getAllFunctionsInPackage((PsiDirectory) element);
            // Add matching functions to results.
            for (PsiElement psiElement : allFunctions) {
                if (getElement().getText().equals(psiElement.getText())) {
                    results.add(new PsiElementResolveResult(psiElement));
                }
            }
            // Get all connectors in the package.
            List<PsiElement> allConnectors =
                    BallerinaPsiImplUtil.getAllConnectorsInPackage((PsiDirectory) element);
            // Add matching functions to results.
            for (PsiElement psiElement : allConnectors) {
                if (getElement().getText().equals(psiElement.getText())) {
                    results.add(new PsiElementResolveResult(psiElement));
                }
            }
            // Get all annotations in the package.
            List<PsiElement> allAnnotations =
                    BallerinaPsiImplUtil.getAllAnnotationsInPackage((PsiDirectory) element);
            // Add matching functions to results.
            for (PsiElement psiElement : allAnnotations) {
                if (getElement().getText().equals(psiElement.getText())) {
                    results.add(new PsiElementResolveResult(psiElement));
                }
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
