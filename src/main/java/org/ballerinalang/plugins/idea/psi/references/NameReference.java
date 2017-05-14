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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.xpath.XPath;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionStatementNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
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
        return (def instanceof FunctionDefinitionNode) || (def instanceof ConnectorDefinitionNode)
                || (def instanceof StructDefinitionNode) || (def instanceof VariableDefinitionNode)
                || (def instanceof AnnotationDefinitionNode) || (def instanceof GlobalVariableDefinitionStatementNode);
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
        // Create a new List to save resolved elements.
        List<ResolveResult> results = new ArrayList<>();
        // Get the PackageNameNode. We need this to resolve the package.
        PackageNameNode[] packageNameNodes =
                PsiTreeUtil.getChildrenOfType(packagePathNode, PackageNameNode.class);

        if (packageNameNodes == null || isDotOperatorAtEnd()) {
            PsiFile file = myElement.getContainingFile();
            if (file == null) {
                return new ResolveResult[0];
            }
            PsiElement prevSibling = myElement.getPrevSibling();
            if (prevSibling instanceof LeafPsiElement) {

                IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();

                if (elementType == BallerinaTypes.COLON) {

                    PsiElement packageNode = file.findElementAt(prevSibling.getTextOffset() - 2);

                    if (packageNode != null) {
                        PsiReference reference = packageNode.getReference();
                        if (reference != null) {
                            PsiElement resolvedElement = reference.resolve();
                            if (resolvedElement != null) {
                                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInPackage
                                        (((PsiDirectory) resolvedElement));
                                // Add matching annotations to results.
                                for (PsiElement connector : connectors) {
                                    if (getElement().getText().equals(connector.getText())) {
                                        results.add(new PsiElementResolveResult(connector));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Even though the package name is null, the name reference node might be an annotation node. So we
                // need to get all annotations in the current.
                PsiDirectory parentDirectory = file.getParent();
                if (parentDirectory == null) {
                    return new ResolveResult[0];
                }
                List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInCurrentPackage(parentDirectory);
                // Add matching annotations to results.
                for (PsiElement annotation : annotations) {
                    if (getElement().getText().equals(annotation.getText())) {
                        results.add(new PsiElementResolveResult(annotation));
                    }
                }
                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(parentDirectory);
                // Add matching annotations to results.
                for (PsiElement connector : connectors) {
                    if (getElement().getText().equals(connector.getText())) {
                        results.add(new PsiElementResolveResult(connector));
                    }
                }

                List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage
                        (parentDirectory);
                // Add matching annotations to results.
                for (PsiElement variable : globalVariables) {
                    if (getElement().getText().equals(variable.getText())) {
                        results.add(new PsiElementResolveResult(variable));
                    }
                }
            }
            return results.toArray(new ResolveResult[results.size()]);
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
        // Iterate through all resolve results.
        for (ResolveResult resolveResult : resolveResults) {
            // Get the element from the resolve result.
            PsiElement element = resolveResult.getElement();
            if (element == null) {
                continue;
            }
            // Get all functions in the package.
            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage((PsiDirectory) element);
            // Add matching functions to results.
            for (PsiElement psiElement : functions) {
                if (getElement().getText().equals(psiElement.getText())) {
                    results.add(new PsiElementResolveResult(psiElement));
                }
            }
            // Get all connectors in the package.
            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInPackage((PsiDirectory) element);
            // Add matching connectors to results.
            for (PsiElement connector : connectors) {
                if (getElement().getText().equals(connector.getText())) {
                    results.add(new PsiElementResolveResult(connector));
                }
            }
            // Get all annotations in the package.
            List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage((PsiDirectory) element);
            // Add matching annotations to results.
            for (PsiElement annotation : annotations) {
                if (getElement().getText().equals(annotation.getText())) {
                    results.add(new PsiElementResolveResult(annotation));
                }
            }
            // Get all structs in the package.
            List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage((PsiDirectory) element);
            // Add matching structs to results.
            for (PsiElement struct : structs) {
                if (getElement().getText().equals(struct.getText())) {
                    results.add(new PsiElementResolveResult(struct));
                }
            }
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    private boolean isDotOperatorAtEnd() {
        int offset = myElement.getTextOffset() + myElement.getTextLength();
        PsiElement element = myElement.getContainingFile().findElementAt(offset);
        if (element != null && element instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) element).getElementType();
            if (elementType == BallerinaTypes.DOT) {
                return true;
            }
        }
        return false;
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
