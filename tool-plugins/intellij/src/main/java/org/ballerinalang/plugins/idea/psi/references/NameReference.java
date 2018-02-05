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

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class NameReference extends BallerinaElementReference {

    public NameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return resolveInCurrentPackage();
        } else {
            return resolveInPackage(packageNameNode);
        }
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            results.addAll(getVariantsFromCurrentPackage());
        } else {
            results.addAll(getVariantsFromPackage(packageNameNode));
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @Nullable
    private PsiElement resolveInCurrentPackage() {
        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory psiDirectory = originalFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        // If the next element is '(', that means we are at a function invocation node. So match any matching
        // function first. If no matching function found, match with variables since this can be a lambda function.
        // If the next element is not '(', we match variables first.
        PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(identifier);
        if (nextVisibleLeaf != null && "(".equals(nextVisibleLeaf.getText())) {
            PsiElement element = BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true,
                    true, true, true, true, true, true);
            if (element != null) {
                return element;
            }
            return BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true, true);
        } else {
            PsiElement elementInScope = BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true,
                    true, true);
            if (elementInScope != null) {
                return elementInScope;
            }
            PsiElement resolvedElement;
            TypeNameNode typeNameNode = PsiTreeUtil.getParentOfType(identifier, TypeNameNode.class);
            if (typeNameNode == null) {
                resolvedElement = BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true,
                        true, true, true, true, true, true);
            } else {
                resolvedElement = BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, false, true,
                        true, true, false, false, true, true);
            }
            if (resolvedElement != null) {
                return resolvedElement;
            }
            return BallerinaPsiImplUtil.getNamespaceDefinition(identifier);
        }
    }

    @Nullable
    private PsiElement resolveInPackage(@NotNull PackageNameNode packageNameNode) {
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return null;
        }
        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        IdentifierPSINode identifier = getElement();
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true, true, true, true,
                true, false, false);
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

        AnnotationAttachmentNode attachmentNode = PsiTreeUtil.getParentOfType(identifier,
                AnnotationAttachmentNode.class);
        if (attachmentNode != null && containingFile instanceof BallerinaFile) {
            ScopeNode scope = (BallerinaFile) containingFile;
            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        } else if (containingPackage != null) {

            List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                    PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                    AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            results.addAll(packages);

            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
            ANTLRPsiNode definitionParent = PsiTreeUtil.getParentOfType(identifier, CallableUnitBodyNode.class,
                    ServiceBodyNode.class, ResourceDefinitionNode.class, ConnectorBodyNode.class);
            TypeNameNode typeNameNode = PsiTreeUtil.getParentOfType(identifier, TypeNameNode.class);
            if ((definitionParent != null && !(definitionParent instanceof ResourceDefinitionNode)) ||
                    prevVisibleLeaf != null && (!";".equals(prevVisibleLeaf.getText()) && typeNameNode == null ||
                            prevVisibleLeaf.getText().matches("[{}]"))) {

                List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage
                        (containingPackage, true, true);
                results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));

                // Todo - use a util method
                ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                        TopLevelDefinition.class, LowerLevelDefinition.class);
                if (scope != null) {
                    int caretOffset = identifier.getStartOffset();

                    List<IdentifierPSINode> variables =
                            BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope, caretOffset);
                    results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

                    List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                            caretOffset);
                    results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

                    List<IdentifierPSINode> globalVariables =
                            BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
                    results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                    List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
                    results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));

                    List<PsiElement> namespaces = BallerinaPsiImplUtil.getAllXmlNamespacesInResolvableScope(scope,
                            caretOffset);
                    results.addAll(BallerinaCompletionUtils.createNamespaceLookupElements(namespaces));

                    List<IdentifierPSINode> endpoints = BallerinaPsiImplUtil.getAllEndpointsInResolvableScope(scope,
                            caretOffset);
                    results.addAll(BallerinaCompletionUtils.createEndpointLookupElements(endpoints));
                } else {
                    ConstantDefinitionNode constantDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                            ConstantDefinitionNode.class);
                    GlobalVariableDefinitionNode globalVariableDefinitionNode = PsiTreeUtil.getParentOfType
                            (identifier, GlobalVariableDefinitionNode.class);
                    if (constantDefinitionNode != null || globalVariableDefinitionNode != null) {
                        scope = PsiTreeUtil.getParentOfType(constantDefinitionNode, BallerinaFile.class);
                    }
                    if (globalVariableDefinitionNode != null) {
                        scope = PsiTreeUtil.getParentOfType(globalVariableDefinitionNode, BallerinaFile.class);
                    }
                    if (scope != null) {
                        int caretOffset = identifier.getStartOffset();
                        List<IdentifierPSINode> globalVars =
                                BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope, caretOffset);
                        results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVars));
                        List<IdentifierPSINode> constants =
                                BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope, caretOffset);
                        results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
                    }
                }
            }

            List<IdentifierPSINode> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage,
                    true, true);
            results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                    AddSpaceInsertHandler.INSTANCE));

            List<IdentifierPSINode> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage, true,
                    true);
            results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

            List<IdentifierPSINode> enums = BallerinaPsiImplUtil.getAllEnumsFromPackage(containingPackage, true, true);
            results.addAll(BallerinaCompletionUtils.createEnumLookupElements(enums, null));
            return results;
        }

        // Try to get fields from an anonymous struct.
        PsiElement structDefinitionNode = BallerinaPsiImplUtil.resolveAnonymousStruct(identifier);
        if (structDefinitionNode == null || !(structDefinitionNode instanceof StructDefinitionNode)) {
            return results;
        }
        IdentifierPSINode structNameNode = PsiTreeUtil.getChildOfType(structDefinitionNode,
                IdentifierPSINode.class);
        if (structNameNode == null) {
            return results;
        }
        Collection<FieldDefinitionNode> fieldDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
        results = BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                structNameNode, PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode) {
        List<LookupElement> results = new LinkedList<>();
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedElement == null || !(resolvedElement instanceof PsiDirectory)) {
            return results;
        }

        PsiDirectory containingPackage = (PsiDirectory) resolvedElement;
        AnnotationAttachmentNode attachmentNode = PsiTreeUtil.getParentOfType(packageNameNode,
                AnnotationAttachmentNode.class);
        if (attachmentNode != null) {
            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(containingPackage,
                    false, false);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        } else {
            // Todo - use a util method
            List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage,
                    false, false);
            results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));

            List<IdentifierPSINode> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage,
                    false, false);
            results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                    AddSpaceInsertHandler.INSTANCE));

            List<IdentifierPSINode> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage,
                    false, false);
            results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

            List<IdentifierPSINode> enums = BallerinaPsiImplUtil.getAllEnumsFromPackage(containingPackage, true, false);
            results.addAll(BallerinaCompletionUtils.createEnumLookupElements(enums, null));

            List<IdentifierPSINode> globalVariables =
                    BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(containingPackage, false, false);
            results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(containingPackage,
                    false, false);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        }
        return results;
    }
}
