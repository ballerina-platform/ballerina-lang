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
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class NameReference extends BallerinaElementReference {

    public NameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        boolean isDefinition = def instanceof FunctionDefinitionNode || def instanceof ConnectorDefinitionNode
                || def instanceof StructDefinitionNode || def instanceof VariableDefinitionNode
                || def instanceof AnnotationDefinitionNode || def instanceof GlobalVariableDefinitionNode
                || def instanceof ConstantDefinitionNode || def instanceof ParameterNode
                || def instanceof AssignmentStatementNode;
        if (isDefinition) {
            return true;
        }
        if (def instanceof IdentifierPSINode) {
            AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(def,
                    AssignmentStatementNode.class);
            if (assignmentStatementNode != null) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();

        List<LookupElement> results = new LinkedList<>();

        if (containingFile != null) {

            PsiDirectory containingPackage = containingFile.getParent();

            if (containingPackage != null) {
                // Todo - Get all functions, connectors, structs, variables
                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);

                for (PsiElement function : functions) {
                    LookupElement lookupElement = BallerinaCompletionUtils.createFunctionsLookupElement(function);
                    results.add(lookupElement);
                }
            }
        }

        return results.toArray(new LookupElement[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {

        // Todo - check package
        return super.resolve();

        //        ResolveResult[] resolveResults = multiResolve(false);
        //        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();
    }

    //    // Todo - remove
    //    @NotNull
    //    @Override
    //    public ResolveResult[] multiResolve(boolean incompleteCode) {
    //        //        // Get the NameReferenceNode parent. This is to resolve functions from other packages. Ex-
    //        // system:println().
    //        //        PsiElement parentElement = PsiTreeUtil.getParentOfType(getElement(), NameReferenceNode.class);
    //        //        if (parentElement == null) {
    //        //            // Get the TypeNameNode parent. This is to resolve Connectors from packages. Ex-
    //        // http:ClientConnector.
    //        //            parentElement = PsiTreeUtil.getParentOfType(getElement(), TypeNameNode.class);
    //        //        }
    //        //
    //        //        // Get the PackagePath node. We need the package path to resolve the corresponding
    //        // Function/Connector. We use
    //        //        // XPath.findAll() here because the PackagePath node might not be a direct child of the
    // parentElement.
    //        //        Collection<? extends PsiElement> packagePath =
    //        //                XPath.findAll(BallerinaLanguage.INSTANCE, parentElement, "//nameReference");
    //        //
    //        //        // Check whether a packagePath is found.
    //        //        if (!packagePath.iterator().hasNext()) {
    //        //            return new ResolveResult[0];
    //        //        }
    //        //        // There cannot be multiple packagePath nodes. So we get the fist package path.
    //        //        PsiElement packagePathNode = packagePath.iterator().next();
    //        //        if (packagePathNode == null) {
    //        //            return new ResolveResult[0];
    //        //        }
    //        //        // Create a new List to save resolved elements.
    //        //        List<ResolveResult> results = new ArrayList<>();
    //        //        // Get the PackageNameNode. We need this to resolve the package.
    //        //        PackageNameNode[] packageNameNodes =
    //        //                PsiTreeUtil.getChildrenOfType(packagePathNode, PackageNameNode.class);
    //        //
    //        //        if (packageNameNodes == null || isDotOperatorAtEnd()) {
    //        //            PsiFile file = myElement.getContainingFile();
    //        //            if (file == null) {
    //        //                return new ResolveResult[0];
    //        //            }
    //        //            PsiElement prevSibling = myElement.getPrevSibling();
    //        //            if (prevSibling instanceof LeafPsiElement) {
    //        //                IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
    //        //                if (elementType != BallerinaTypes.COLON) {
    //        //                    return results.toArray(new ResolveResult[results.size()]);
    //        //                }
    //        //                PsiElement packageNode = file.findElementAt(prevSibling.getTextOffset() - 2);
    //        //                if (packageNode == null) {
    //        //                    return results.toArray(new ResolveResult[results.size()]);
    //        //                }
    //        //                PsiReference reference = packageNode.getReference();
    //        //                if (reference == null) {
    //        //                    return results.toArray(new ResolveResult[results.size()]);
    //        //                }
    //        //                PsiElement resolvedElement = reference.resolve();
    //        //                if (resolvedElement == null) {
    //        //                    return results.toArray(new ResolveResult[results.size()]);
    //        //                }
    //        //                if (resolvedElement instanceof PackageNameNode) {
    //        //                    reference = resolvedElement.getReference();
    //        //                    if (reference == null) {
    //        //                        return results.toArray(new ResolveResult[results.size()]);
    //        //                    }
    //        //                    resolvedElement = reference.resolve();
    //        //                    if (resolvedElement == null) {
    //        //                        return results.toArray(new ResolveResult[results.size()]);
    //        //                    }
    //        //                }
    //        //                if (!(resolvedElement instanceof PsiDirectory)) {
    //        //                    return results.toArray(new ResolveResult[results.size()]);
    //        //                }
    //        //                List<PsiElement> connectors =
    //        //                        BallerinaPsiImplUtil.getAllConnectorsInPackage(((PsiDirectory)
    // resolvedElement));
    //        //                // Add matching annotations to results.
    //        //                for (PsiElement connector : connectors) {
    //        //                    if (getElement().getText().equals(connector.getText())) {
    //        //                        results.add(new PsiElementResolveResult(connector));
    //        //                    }
    //        //                }
    //        //            }
    //        ////            else if (prevSibling == null) {
    //        ////                PsiReference reference = myElement.getReference();
    //        ////                if (reference != null) {
    //        ////                    PsiElement resolvedResult = reference.resolve();
    //        ////                    if (resolvedResult != null) {
    //        ////                        results.add(new PsiElementResolveResult(resolvedResult));
    //        ////                    }
    //        ////                }
    //        ////            }
    //        //            return results.toArray(new ResolveResult[results.size()]);
    //        //        }
    //        //        // Get the last PackageNameNode because we only need to resolve the corresponding package.
    //        //        PackageNameNode lastPackage = packageNameNodes[packageNameNodes.length - 1];
    //        //        if (lastPackage == null) {
    //        //            return new ResolveResult[0];
    //        //        }
    //        //        // Get the identifier from the last package name.
    //        //        PsiElement nameIdentifier = lastPackage.getNameIdentifier();
    //        //        if (nameIdentifier == null) {
    //        //            return new ResolveResult[0];
    //        //        }
    //        //        // Get the reference.
    //        //        PsiReference reference = nameIdentifier.getReference();
    //        //        if (reference == null) {
    //        //            return new ResolveResult[0];
    //        //        }
    //        //        // Multi resolve the reference.
    //        //        ResolveResult[] resolveResults = ((PackageNameReference) reference).multiResolve(false);
    //        //        // Iterate through all resolve results.
    //        //        for (ResolveResult resolveResult : resolveResults) {
    //        //            // Get the element from the resolve result.
    //        //            PsiElement element = resolveResult.getElement();
    //        //            if (element == null) {
    //        //                continue;
    //        //            }
    //        //            PsiDirectory psiDirectory = null;
    //        //            if (element instanceof PackageNameNode) {
    //        //
    //        //                PsiElement packageNameIdentifier = ((PackageNameNode) element).getNameIdentifier();
    //        //                if (packageNameIdentifier == null) {
    //        //                    return new ResolveResult[0];
    //        //                }
    //        //
    //        //                // Check whether this is in a import statement.
    //        //                ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(element,
    //        //                        ImportDeclarationNode.class);
    //        //                if (importDeclarationNode != null) {
    //        //                    // If this is an import declaration, resolve the directory.
    //        //                    PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory
    // (packageNameIdentifier);
    //        //                    for (PsiDirectory directory : directories) {
    //        //                        psiDirectory = directory;
    //        //                        break;
    //        //                    }
    //        //                }
    //        //            } else {
    //        //                // Get all functions in the package.
    //        //                psiDirectory = (PsiDirectory) element;
    //        //            }
    //        //
    //        //
    //        //            if (psiDirectory == null) {
    //        //                return new ResolveResult[0];
    //        //            }
    //        //
    //        //            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(psiDirectory);
    //        //            // Add matching functions to results.
    //        //            for (PsiElement psiElement : functions) {
    //        //                if (getElement().getText().equals(psiElement.getText())) {
    //        //                    results.add(new PsiElementResolveResult(psiElement));
    //        //                }
    //        //            }
    //        //            // Get all connectors in the package.
    //        //            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInPackage(psiDirectory);
    //        //            // Add matching connectors to results.
    //        //            for (PsiElement connector : connectors) {
    //        //                if (getElement().getText().equals(connector.getText())) {
    //        //                    results.add(new PsiElementResolveResult(connector));
    //        //                }
    //        //            }
    //        //            // Get all annotations in the package.
    //        //            List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage
    // (psiDirectory);
    //        //            // Add matching annotations to results.
    //        //            for (PsiElement annotation : annotations) {
    //        //                if (getElement().getText().equals(annotation.getText())) {
    //        //                    results.add(new PsiElementResolveResult(annotation));
    //        //                }
    //        //            }
    //        //            // Get all structs in the package.
    //        //            List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(psiDirectory);
    //        //            // Add matching structs to results.
    //        //            for (PsiElement struct : structs) {
    //        //                if (getElement().getText().equals(struct.getText())) {
    //        //                    results.add(new PsiElementResolveResult(struct));
    //        //                }
    //        //            }
    //        //            // Get all constants in the package.
    //        //            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(psiDirectory);
    //        //            // Add matching constants to results.
    //        //            for (PsiElement constant : constants) {
    //        //                if (getElement().getText().equals(constant.getText())) {
    //        //                    results.add(new PsiElementResolveResult(constant));
    //        //                }
    //        //            }
    //        //            // Get all global variables in the package.
    //        //            List<PsiElement> globalVariables =
    //        //                    BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(psiDirectory);
    //        //            // Add matching global variables to results.
    //        //            for (PsiElement variable : globalVariables) {
    //        //                if (getElement().getText().equals(variable.getText())) {
    //        //                    results.add(new PsiElementResolveResult(variable));
    //        //                }
    //        //            }
    //        //        }
    //        //        return results.toArray(new ResolveResult[results.size()]);
    //        return new ResolveResult[0];
    //    }

    //    private boolean isDotOperatorAtEnd() {
    //        int offset = myElement.getTextOffset() + myElement.getTextLength();
    //        PsiElement element = myElement.getContainingFile().findElementAt(offset);
    //        if (element != null && element instanceof LeafPsiElement) {
    //            IElementType elementType = ((LeafPsiElement) element).getElementType();
    //            if (elementType == BallerinaTypes.DOT) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

    @Override
    public boolean isReferenceTo(PsiElement definitionElement) {
        //        String refName = myElement.getName();
        //        if (definitionElement instanceof IdentifierPSINode && isDefinitionNode(definitionElement.getParent
        // ())) {
        //            definitionElement = definitionElement.getParent();
        //        }
        //        if (isDefinitionNode(definitionElement)) {
        //            if (definitionElement instanceof FunctionDefinitionNode
        //                    || definitionElement instanceof ConnectorDefinitionNode
        //                    || definitionElement instanceof StructDefinitionNode
        //                    || definitionElement instanceof ConstantDefinitionNode
        //                    || definitionElement instanceof VariableDefinitionNode
        //                    || definitionElement instanceof GlobalVariableDefinitionNode
        //                    || definitionElement instanceof AnnotationDefinitionNode
        //                    || definitionElement instanceof IdentifierPSINode
        //                    || definitionElement instanceof FieldNode) {
        //                PsiFile containingFile = myElement.getContainingFile();
        //                PsiDirectory myDirectory = containingFile.getContainingDirectory();
        //                PsiDirectory definitionDirectory = definitionElement.getContainingFile()
        // .getContainingDirectory();
        //                if (myDirectory.equals(definitionDirectory)) {
        //                    if (myElement.getParent() instanceof NameReferenceNode) {
        //                        PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(myElement.getParent(),
        //                                PackageNameNode.class);
        //                        if (packageNameNode != null) {
        //                            return false;
        //                        }
        //                    }
        //                } else {
        //                    List<PsiElement> importedAsAlias =
        //                            BallerinaPsiImplUtil.getPackagesImportedAsAliasInCurrentFile(containingFile);
        //                    boolean matchFound = false;
        //                    for (PsiElement packageNameNode : importedAsAlias) {
        //                        IdentifierPSINode resolvedIdentifier = BallerinaPsiImplUtil.resolveAlias
        // (packageNameNode);
        //                        if (resolvedIdentifier == null) {
        //                            continue;
        //                        }
        //
        //                        // If this is an import declaration, resolve the directory.
        //                        PsiDirectory[] directories = BallerinaPsiImplUtil.resolveDirectory
        // (resolvedIdentifier);
        //                        for (PsiDirectory directory : directories) {
        //                            if (definitionDirectory.getName().equals(directory.getName())) {
        //                                matchFound = true;
        //                                break;
        //                            }
        //                        }
        //                    }
        //
        //                    Map<String, String> allImports =
        //                            BallerinaPsiImplUtil.getAllImportsInAFile(containingFile);
        //                    if (!allImports.containsKey(definitionDirectory.getName()) && !matchFound) {
        //                        return false;
        //                    }
        //                    if (myElement.getParent() instanceof NameReferenceNode) {
        //                        PackageNameNode packageNameNode = PsiTreeUtil.findChildOfType(myElement.getParent(),
        //                                PackageNameNode.class);
        //                        if (packageNameNode == null) {
        //                            return false;
        //                        }
        //                        PsiReference reference = packageNameNode.getReference();
        //                        if (reference != null) {
        //                            PsiElement resolvedElement = reference.resolve();
        //                            if (!(resolvedElement instanceof PsiDirectory)) {
        //                                return false;
        //                            }
        //                        }
        //                    }
        //                    return isValid((PsiNameIdentifierOwner) definitionElement, refName);
        //                }
        //            }
        //
        //            if (definitionElement instanceof ParameterNode) {
        //
        //                if (!(myElement.getParent() instanceof NameReferenceNode)) {
        //                    return false;
        //                }
        //                // If the common context is file, that means the myElement is not in the scope where the
        //                // definitionElement is defined in.
        //                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
        //                if (!(commonContext instanceof FunctionDefinitionNode || commonContext instanceof
        // ResourceDefinitionNode
        //                        || commonContext instanceof ConnectorDefinitionNode
        //                        || commonContext instanceof ActionDefinitionNode
        //                        || commonContext instanceof TypeMapperNode)) {
        //                    return false;
        //                }
        //
        //                VariableReferenceNode variableReferenceNode = PsiTreeUtil.getParentOfType(myElement,
        //                        VariableReferenceNode.class);
        //                if (variableReferenceNode == null) {
        //                    return false;
        //                }
        //                while (variableReferenceNode != null) {
        //                    PsiElement prevSibling = variableReferenceNode.getPrevSibling();
        //                    if (prevSibling != null) {
        //                        if (prevSibling instanceof LeafPsiElement) {
        //                            IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
        //                            if (elementType == BallerinaTypes.DOT) {
        //                                return false;
        //                            }
        //                        }
        //                    }
        //                    PsiElement variableReferenceNodeParent = variableReferenceNode.getParent();
        //                    if (variableReferenceNodeParent instanceof VariableReferenceNode) {
        //                        variableReferenceNode = ((VariableReferenceNode) variableReferenceNodeParent);
        //                    } else {
        //                        variableReferenceNode = null;
        //                    }
        //                }
        //                boolean isStructField = BallerinaPsiImplUtil.isStructField(myElement);
        //                if (!isStructField) {
        //                    return isValid((PsiNameIdentifierOwner) definitionElement, refName);
        //                }
        //            } else if (definitionElement instanceof VariableDefinitionNode) {
        //                // If the common context is file, that means the myElement is not in the scope where the
        //                // definitionElement is defined in.
        //                PsiElement commonContext = PsiTreeUtil.findCommonContext(definitionElement, myElement);
        //
        //                // If the definition node is in a ResourceDefinitionNode, common context should be
        //                // CallableUnitBodyNode for the references to be on the same context.
        //                ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(definitionElement,
        //                        ResourceDefinitionNode.class);
        //                if (resourceDefinitionNode != null && !(commonContext instanceof CallableUnitBodyNode)) {
        //                    return false;
        //                }
        //
        //                // If the definition node is in a ActionDefinitionNode, common context should be
        //                // CallableUnitBodyNode for the references to be on the same context.
        //                ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(definitionElement,
        //                        ActionDefinitionNode.class);
        //                if (actionDefinitionNode != null && !(commonContext instanceof CallableUnitBodyNode)) {
        //                    return false;
        //                }
        //
        //                // Otherwise, the common context can be
        //                // 1) CallableUnitBodyNode - for functions
        //                // 2) ConnectorBodyNode - for variables defined in connector body
        //                // 3) ServiceBodyNode - for variables defined in service body
        //                // 4) TypeMapperBodyNode - for variables defined in typemapper body
        //                if (!(commonContext instanceof CallableUnitBodyNode || commonContext instanceof
        // ConnectorBodyNode
        //                        || commonContext instanceof ServiceBodyNode || commonContext instanceof
        // TypeMapperBodyNode)) {
        //                    return false;
        //                }
        //                // Variable definition node should not be listed as a usage. So we check the parent
        //                // VariableDefinitionNode if one available.
        //                VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(myElement,
        //                        VariableDefinitionNode.class);
        //                if (variableDefinitionNode != null) {
        //                    if (definitionElement.equals(variableDefinitionNode)) {
        //                        return false;
        //                    }
        //                }
        //                FunctionInvocationNode functionInvocationNode = PsiTreeUtil.getParentOfType(myElement,
        //                        FunctionInvocationNode.class);
        //                if (functionInvocationNode != null) {
        //                    ExpressionListNode expressionListNode = PsiTreeUtil.getParentOfType(myElement,
        //                            ExpressionListNode.class);
        //                    // Without this check, the parameters in the function invocation in the "assignment +
        // function
        //                    // invocation" statements will fail.
        //                    // Eg: nameString = jsons:getString(jsonMsg, "$.name");
        //                    // In here, selecting the "jsonMsg" will not highlight the definition.
        //                    if (expressionListNode == null) {
        //                        return false;
        //                    }
        //                }
        //                // Don't check struct field references.
        //                boolean isStructField = BallerinaPsiImplUtil.isStructField(myElement);
        //                if (!isStructField) {
        //                    return isValid((PsiNameIdentifierOwner) definitionElement, refName);
        //                }
        //            } else if (definitionElement instanceof FieldDefinitionNode) {
        //                PsiReference reference = myElement.getReference();
        //                if (reference == null) {
        //                    return false;
        //                }
        //                PsiElement resolvedElement = reference.resolve();
        //                if (resolvedElement == null) {
        //                    return false;
        //                }
        //                return resolvedElement.getParent().equals(definitionElement);
        //            } else if (definitionElement instanceof GlobalVariableDefinitionNode
        //                    || definitionElement instanceof ConstantDefinitionNode) {
        //                boolean isStructField = BallerinaPsiImplUtil.isStructField(myElement);
        //                if (!isStructField) {
        //                    return isValid((PsiNameIdentifierOwner) definitionElement, refName);
        //                }
        //            } else if (definitionElement instanceof FunctionDefinitionNode
        //                    || definitionElement instanceof AnnotationDefinitionNode) {
        //                return isValid((PsiNameIdentifierOwner) definitionElement, refName);
        //            } else if (definitionElement instanceof IdentifierPSINode) {
        //                if (myElement.equals(definitionElement)) {
        //                    return false;
        //                }
        //                return true;
        //            }
        //        }
        //        return false;

        String defName = definitionElement.getText();
        String refName = myElement.getText();
        return defName != null && refName.equals(defName);
        //        return true;
    }

    //    private boolean isValid(PsiNameIdentifierOwner definitionElement, String refName) {
    //        PsiElement id = definitionElement.getNameIdentifier();
    //        String defName = id != null ? id.getText() : null;
    //        return refName != null && defName != null && refName.equals(defName);
    //    }
}
