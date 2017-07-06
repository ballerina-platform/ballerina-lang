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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.BallerinaAutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class NameReference extends BallerinaElementReference {

    public NameReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Override
    public boolean isDefinitionNode(PsiElement def) {
        return false;
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

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

        if (containingPackage != null) {

            List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(containingFile);
            for (PsiElement importedPackage : importedPackages) {
                PsiReference reference = importedPackage.findReferenceAt(0);
                if (reference == null) {
                    continue;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    continue;
                }
                PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement(resolvedPackage,
                        PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                results.add(lookupElement);
            }

            List<PsiDirectory> unImportedPackages = BallerinaPsiImplUtil.getAllUnImportedPackages
                    (containingFile);
            for (PsiDirectory unImportedPackage : unImportedPackages) {
                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement(unImportedPackage,
                        BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                results.add(lookupElement);
            }

            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                    AddSpaceInsertHandler.INSTANCE));

            List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

            //            List<PsiElement> variables = BallerinaPsiImplUtil.getAllVariablesInResolvableScope
            // (identifier,
            //                    identifier.getParent().getContext());
            //            for (PsiElement variable : variables) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createVariableLookupElement
            // (variable);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiElement> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope
            // (identifier,
            //                    identifier.getParent().getContext());
            //            for (PsiElement parameter : parameters) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createParameterLookupElement
            // (parameter);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage
            // (containingPackage);
            //            for (PsiElement globalVariable : globalVariables) {
            //                LookupElement lookupElement =
            //                        BallerinaCompletionUtils.createGlobalVariableLookupElement(globalVariable);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage
            // (containingPackage);
            //            for (PsiElement constant : constants) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createConstantLookupElement
            // (constant);
            //                results.add(lookupElement);
            //            }

            ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CallableUnitBodyNode.class,
                    ServiceBodyNode.class, ConnectorBodyNode.class, ServiceDefinitionNode.class,
                    ConnectorDefinitionNode.class);
            if (scope != null) {

                int caretOffset = identifier.getStartOffset();

                List<PsiElement> variables = getAllLocalVariablesInResolvableScope(scope, caretOffset);
                for (PsiElement variable : variables) {
                    LookupElement lookupElement = BallerinaCompletionUtils.createVariableLookupElement(variable);
                    results.add(lookupElement);
                }

                List<PsiElement> parameters = getAllParametersInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

                List<PsiElement> globalVariables = getAllGlobalVariablesInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                List<PsiElement> constants = getAllConstantsInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
            }
        }
        return results;
    }


    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode) {
        List<LookupElement> results = new LinkedList<>();
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return results;
        }
        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return results;
        }

        PsiDirectory containingPackage = (PsiDirectory) resolvedElement;

        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                AddSpaceInsertHandler.INSTANCE));

        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

        return results;
    }

    @NotNull
    private List<PsiElement> getAllLocalVariablesInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof CallableUnitBodyNode || scope instanceof ServiceBodyNode
                || scope instanceof ConnectorBodyNode) {
            results.addAll(getAllLocalVariablesInScope(scope, caretOffset));
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllLocalVariablesInResolvableScope(context, caretOffset));
            }
        } else if (scope instanceof ResourceDefinitionNode || scope instanceof ActionDefinitionNode) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllLocalVariablesInResolvableScope(context, caretOffset));
            }
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllLocalVariablesInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        Collection<VariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                VariableDefinitionNode.class);
        for (VariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement elementAtCaret = scope.getContainingFile().findElementAt(caretOffset);
            if (elementAtCaret != null) {
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(elementAtCaret);

                // Todo - temp fix
                if (prevVisibleLeaf != null && !";".equals(prevVisibleLeaf.getText())) {

                    PsiElement prevVisibleLeafParent = PsiTreeUtil.getParentOfType(prevVisibleLeaf,
                            VariableDefinitionNode.class);
                    if (prevVisibleLeafParent != null) {
                        PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                        if (identifier != null && identifier.getTextOffset() < caretOffset
                                && !variableDefinitionNode.equals(prevVisibleLeafParent)) {
                            results.add(identifier);
                        }
                    } else {
                        PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                        if (identifier != null && identifier.getTextOffset() < caretOffset) {
                            results.add(identifier);
                        }
                    }
                } else {
                    PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                    if (identifier != null && identifier.getTextOffset() < caretOffset) {
                        results.add(identifier);
                    }
                }
            }
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllParametersInResolvableScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof CallableUnitBodyNode || scope instanceof ServiceBodyNode
                || scope instanceof ConnectorBodyNode) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllParametersInResolvableScope(context));
            }
        } else if (scope instanceof FunctionDefinitionNode || scope instanceof ResourceDefinitionNode
                || scope instanceof ActionDefinitionNode || scope instanceof ConnectorDefinitionNode) {
            results.addAll(getAllParametersInScope(scope));
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllParametersInResolvableScope(context));
            }
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllParametersInScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(scope,
                ParameterNode.class);
        for (ParameterNode parameter : parameterNodes) {
            PsiElement identifier = parameter.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllGlobalVariablesInResolvableScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof CallableUnitBodyNode || scope instanceof ServiceBodyNode
                || scope instanceof ConnectorBodyNode || scope instanceof FunctionDefinitionNode
                || scope instanceof ResourceDefinitionNode || scope instanceof ActionDefinitionNode
                || scope instanceof ServiceDefinitionNode || scope instanceof ConnectorDefinitionNode) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllGlobalVariablesInResolvableScope(context));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllGlobalVariablesInScope(scope));
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllGlobalVariablesInScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        Collection<GlobalVariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                GlobalVariableDefinitionNode.class);
        for (GlobalVariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }
        PsiFile originalFile = ((BallerinaFile) scope).getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage != null) {
            PsiFile[] files = containingPackage.getFiles();
            for (PsiFile file : files) {
                // Do't check the current file again.
                if (file.equals(originalFile)) {
                    continue;
                }
                variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(file, GlobalVariableDefinitionNode.class);
                for (GlobalVariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
                    PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                    if (identifier != null) {
                        results.add(identifier);
                    }
                }
            }
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllConstantsInResolvableScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof CallableUnitBodyNode || scope instanceof ServiceBodyNode
                || scope instanceof ConnectorBodyNode || scope instanceof FunctionDefinitionNode
                || scope instanceof ResourceDefinitionNode || scope instanceof ActionDefinitionNode
                || scope instanceof ServiceDefinitionNode || scope instanceof ConnectorDefinitionNode) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllConstantsInResolvableScope(context));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllConstantsInScope(scope));
        }
        return results;
    }

    @NotNull
    private List<PsiElement> getAllConstantsInScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        Collection<ConstantDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                ConstantDefinitionNode.class);
        for (ConstantDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }
        PsiFile originalFile = ((BallerinaFile) scope).getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage != null) {
            PsiFile[] files = containingPackage.getFiles();
            for (PsiFile file : files) {
                // Do't check the current file again.
                if (file.equals(originalFile)) {
                    continue;
                }
                variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(file, ConstantDefinitionNode.class);
                for (ConstantDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
                    PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                    if (identifier != null) {
                        results.add(identifier);
                    }
                }
            }
        }
        return results;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement parent = identifier.getParent();

        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return resolveLocalElement(identifier);
        } else {
            return resolveElementInPackage(packageNameNode, identifier);
        }
    }

    @Nullable
    private PsiElement resolveLocalElement(IdentifierPSINode identifier) {
        // Todo - Resolve variables
        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory psiDirectory = originalFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        return findMatchingElement(psiDirectory, identifier);
    }

    @Nullable
    private PsiElement resolveElementInPackage(@NotNull PackageNameNode packageNameNode,
                                               @NotNull IdentifierPSINode identifier) {
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return null;
        }

        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return null;
        }

        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        return findMatchingElement(psiDirectory, identifier);
    }

    @Nullable
    private PsiElement findMatchingElement(@NotNull PsiDirectory directory,
                                           @NotNull IdentifierPSINode identifier) {
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage
                (directory);
        for (PsiElement function : functions) {
            if (identifier.getText().equals(function.getText())) {
                return function;
            }
        }

        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage
                (directory);
        for (PsiElement connector : connectors) {
            if (identifier.getText().equals(connector.getText())) {
                return connector;
            }
        }

        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(directory);
        for (PsiElement struct : structs) {
            if (identifier.getText().equals(struct.getText())) {
                return struct;
            }
        }

        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CallableUnitBodyNode.class,
                ServiceBodyNode.class, ConnectorBodyNode.class, ServiceDefinitionNode.class,
                ConnectorDefinitionNode.class);
        if (scope != null) {

            int caretOffset = identifier.getStartOffset();

            List<PsiElement> variables = getAllLocalVariablesInResolvableScope(scope, caretOffset);
            for (PsiElement variable : variables) {
                if (identifier.getText().equals(variable.getText())) {
                    return variable;
                }
            }

            List<PsiElement> parameters = getAllParametersInResolvableScope(scope);
            for (PsiElement parameter : parameters) {
                if (identifier.getText().equals(parameter.getText())) {
                    return parameter;
                }
            }

            List<PsiElement> globalVariables = getAllGlobalVariablesInResolvableScope(scope);
            for (PsiElement variable : globalVariables) {
                if (identifier.getText().equals(variable.getText())) {
                    return variable;
                }
            }

            List<PsiElement> constants = getAllConstantsInResolvableScope(scope);
            for (PsiElement constant : constants) {
                if (identifier.getText().equals(constant.getText())) {
                    return constant;
                }
            }
        }

        // Todo - Resolve global variables, constants
        return null;
    }
}
