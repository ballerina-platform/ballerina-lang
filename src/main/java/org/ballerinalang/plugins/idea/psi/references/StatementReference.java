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
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.MapStructKeyValueNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperBodyNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StatementReference extends BallerinaElementReference {

    public StatementReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return def instanceof PackageNameNode || def instanceof VariableDefinitionNode || def instanceof ParameterNode
                || def instanceof ConstantDefinitionNode || def instanceof TypeNameNode
                || def instanceof FunctionDefinitionNode || def instanceof ConnectorDefinitionNode
                || def instanceof StructDefinitionNode || def instanceof GlobalVariableDefinitionNode
                || def instanceof ConstantDefinitionNode;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // WARNING: If find usage shows an error message, verify that the super.resolve() returns an Identifier node.
//        ResolveResult[] resolveResults = multiResolve(false);
//        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();
        return super.resolve();
    }

//    @NotNull
//    @Override
//    public ResolveResult[] multiResolve(boolean incompleteCode) {
//        List<ResolveResult> results = new ArrayList<>();
//        // This is used to resolve elements which cannot be parsed properly. We assume that the element is a
//        // reference to a definition.
//        CallableUnitBodyNode bodyNode = PsiTreeUtil.getParentOfType(getElement(), CallableUnitBodyNode.class);
//        if (bodyNode == null) {
//            return results.toArray(new ResolveResult[results.size()]);
//        }
//        PsiFile file = myElement.getContainingFile();
//
//        List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(file);
//        for (PsiElement importedPackage : importedPackages) {
//            if (myElement.getText().equals(importedPackage.getText())) {
//                if (!(importedPackage instanceof PackageNameNode)) {
//                    continue;
//                }
//                PsiElement nameIdentifier = ((PackageNameNode) importedPackage).getNameIdentifier();
//                if (nameIdentifier == null) {
//                    continue;
//                }
//                PsiReference reference = nameIdentifier.getReference();
//                if (reference == null) {
//                    continue;
//                }
//                PsiElement resolvedElement = reference.resolve();
//                if (resolvedElement == null) {
//                    continue;
//                }
//                results.add(new PsiElementResolveResult(resolvedElement));
//            }
//        }
//
//        // First we get all the definitions in the callable unit body.
//        Collection<VariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(bodyNode,
//                VariableDefinitionNode.class);
//        // Check and add each result.
//        for (VariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
//            PsiElement nameIdentifier = variableDefinitionNode.getNameIdentifier();
//            if (nameIdentifier == null) {
//                continue;
//            }
//            if (myElement.getText().equals(nameIdentifier.getText())) {
//                results.add(new PsiElementResolveResult(variableDefinitionNode));
//            }
//        }
//
//        // Check parameters
//        Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(bodyNode.getParent(),
//                ParameterNode.class);
//        // Check and add each result.
//        for (ParameterNode parameterNode : parameterNodes) {
//            PsiElement nameIdentifier = parameterNode.getNameIdentifier();
//            if (nameIdentifier == null) {
//                continue;
//            }
//            if (myElement.getText().equals(nameIdentifier.getText())) {
//                results.add(new PsiElementResolveResult(parameterNode));
//            }
//        }
//
//        PsiElement previousElement = BallerinaCompletionUtils.getPreviousNonEmptyElement(file,
//                myElement.getTextOffset());
//        if (previousElement instanceof LeafPsiElement) {
//            IElementType elementType = ((LeafPsiElement) previousElement).getElementType();
//            if (elementType == BallerinaTypes.COLON) {
//                PsiElement packageNode = file.findElementAt(previousElement.getTextOffset() - 2);
//                if (packageNode != null) {
//                    PsiReference reference = packageNode.getReference();
//                    if (reference != null) {
//                        PsiElement resolvedElement = reference.resolve();
//                        if (resolvedElement != null && resolvedElement instanceof PsiDirectory) {
//                            List<PsiElement> functions =
//                                    BallerinaPsiImplUtil.getAllFunctionsFromPackage((PsiDirectory) resolvedElement);
//                            for (PsiElement function : functions) {
//                                if (myElement.getText().startsWith(function.getText())) {
//                                    results.add(new PsiElementResolveResult(function));
//                                }
//                            }
//
//                            List<PsiElement> connectors =
//                                    BallerinaPsiImplUtil.getAllConnectorsInPackage((PsiDirectory) resolvedElement);
//                            for (PsiElement connector : connectors) {
//                                if (myElement.getText().startsWith(connector.getText())) {
//                                    results.add(new PsiElementResolveResult(connector));
//                                }
//                            }
//
//                            List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(
//                                    (PsiDirectory) resolvedElement);
//                            for (PsiElement variable : globalVariables) {
//                                if (myElement.getText().startsWith(variable.getText())) {
//                                    results.add(new PsiElementResolveResult(variable));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        List<PsiElement> connectorsInCurrentPackage =
//                BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(file.getParent());
//        for (PsiElement connector : connectorsInCurrentPackage) {
//            if (!(connector instanceof IdentifierPSINode)) {
//                continue;
//            }
//            PsiElement nameIdentifier = ((IdentifierPSINode) connector).getNameIdentifier();
//            if (nameIdentifier == null) {
//                continue;
//            }
//            if (myElement.getText().equals(nameIdentifier.getText())) {
//                results.add(new PsiElementResolveResult(nameIdentifier));
//            }
//        }
//
//        List<PsiElement> globalVariablesInCurrentPackage =
//                BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(file.getParent());
//        for (PsiElement variable : globalVariablesInCurrentPackage) {
//            if (!(variable instanceof IdentifierPSINode)) {
//                continue;
//            }
//            PsiElement nameIdentifier = ((IdentifierPSINode) variable).getNameIdentifier();
//            if (nameIdentifier == null) {
//                continue;
//            }
//            if (myElement.getText().equals(nameIdentifier.getText())) {
//                results.add(new PsiElementResolveResult(nameIdentifier));
//            }
//        }
//
//        List<PsiElement> constants =
//                BallerinaPsiImplUtil.getAllConstantsFromPackage(file.getParent());
//        for (PsiElement constant : constants) {
//            if (!(constant instanceof IdentifierPSINode)) {
//                continue;
//            }
//            PsiElement nameIdentifier = ((IdentifierPSINode) constant).getNameIdentifier();
//            if (nameIdentifier == null) {
//                continue;
//            }
//            if (myElement.getText().equals(nameIdentifier.getText())) {
//                results.add(new PsiElementResolveResult(nameIdentifier));
//            }
//        }
//
//        // Return results.
//        return results.toArray(new ResolveResult[results.size()]);
//    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        String refName = myElement.getName();
        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent())) {
            definitionElement = definitionElement.getParent();
        }
        if (isDefinitionNode(definitionElement)) {
            if (definitionElement instanceof FunctionDefinitionNode
                    || definitionElement instanceof ConnectorDefinitionNode
                    || definitionElement instanceof StructDefinitionNode
                    || definitionElement instanceof ConstantDefinitionNode
                    || definitionElement instanceof GlobalVariableDefinitionNode) {
                PsiDirectory myDirectory = myElement.getContainingFile().getContainingDirectory();
                PsiDirectory definitionDirectory = definitionElement.getContainingFile().getContainingDirectory();
                if (myDirectory.equals(definitionDirectory)) {
                    if (myElement.getParent() instanceof NameReferenceNode) {
                        PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(myElement.getParent(),
                                PackageNameNode.class);
                        if (packageNameNode != null) {
                            return false;
                        }
                    }
                    return isValid((PsiNameIdentifierOwner) definitionElement, refName);
                } else {
                    Map<String, String> allImports =
                            BallerinaPsiImplUtil.getAllImportsInAFile(myElement.getContainingFile());
                    if (!allImports.containsKey(definitionDirectory.getName())) {
                        return false;
                    }
                    if (myElement.getParent() instanceof NameReferenceNode) {
                        PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(myElement.getParent(),
                                PackageNameNode.class);
                        if (packageNameNode == null) {
                            return false;
                        }
                        PsiReference reference = packageNameNode.getReference();
                        if (reference != null) {
                            PsiElement resolvedElement = reference.resolve();
                            if (!(resolvedElement instanceof PsiDirectory)) {
                                return false;
                            }
                        }
                    }
                }
                return isValid((PsiNameIdentifierOwner) definitionElement, refName);
            } else if (definitionElement instanceof ParameterNode) {

                if (!(myElement.getParent() instanceof NameReferenceNode)) {
                    return false;
                }
                // If the common context is file, that means the myElement is not in the scope where the
                // definitionElement is defined in.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof FunctionDefinitionNode || commonContext instanceof ResourceDefinitionNode
                        || commonContext instanceof ConnectorDefinitionNode
                        || commonContext instanceof ActionDefinitionNode
                        || commonContext instanceof TypeMapperNode)) {
                    return false;
                }

                VariableReferenceNode variableReferenceNode = PsiTreeUtil.getParentOfType(myElement,
                        VariableReferenceNode.class);

                if (variableReferenceNode == null) {
                    return false;
                }

                PsiElement prevSibling = variableReferenceNode.getPrevSibling();
                if (prevSibling != null) {

                    if (prevSibling instanceof LeafPsiElement) {
                        IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                        if (elementType == BallerinaTypes.DOT) {
                            return false;
                        }
                    }
                }

                ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(myElement, ExpressionNode.class);
                if (expressionNode != null) {
                    if (expressionNode.getParent() instanceof MapStructKeyValueNode) {
                        return false;
                    }
                }

                return isValid((PsiNameIdentifierOwner) definitionElement, refName);
            } else if (definitionElement instanceof VariableDefinitionNode) {
                // If the common context is file, that means the myElement is not in the scope where the
                // definitionElement is defined in.
                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
                if (!(commonContext instanceof CallableUnitBodyNode || commonContext instanceof ConnectorBodyNode
                        || commonContext instanceof TypeMapperBodyNode)) {
                    return false;
                }
                VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(myElement,
                        VariableDefinitionNode.class);
                if (variableDefinitionNode != null) {
                    if (definitionElement.equals(variableDefinitionNode)) {
                        return false;
                    }
                }
                return isValid((PsiNameIdentifierOwner) definitionElement, refName);
            }
        }
        return false;
    }

    private boolean isValid(PsiNameIdentifierOwner definitionElement, String refName) {
        PsiElement id = definitionElement.getNameIdentifier();
        String defName = id != null ? id.getText() : null;
        return refName != null && defName != null && refName.equals(defName);
    }
}
