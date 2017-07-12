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
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.BallerinaAutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        //        // Todo - use util method
        //        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CallableUnitBodyNode.class,
        //                ServiceBodyNode.class, ConnectorBodyNode.class, ServiceDefinitionNode.class,
        //                ConnectorDefinitionNode.class);
        //        if (scope != null) {
        //
        //            int caretOffset = identifier.getStartOffset();
        //
        //            List<PsiElement> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
        // caretOffset);
        //            for (PsiElement variable : variables) {
        //                if (identifier.getText().equals(variable.getText())) {
        //                    return variable;
        //                }
        //            }
        //
        //            List<PsiElement> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope);
        //            for (PsiElement parameter : parameters) {
        //                if (identifier.getText().equals(parameter.getText())) {
        //                    return parameter;
        //                }
        //            }
        //
        //            List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope
        // (scope);
        //            for (PsiElement variable : globalVariables) {
        //                if (identifier.getText().equals(variable.getText())) {
        //                    return variable;
        //                }
        //            }
        //
        //            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
        //            for (PsiElement constant : constants) {
        //                if (identifier.getText().equals(constant.getText())) {
        //                    return constant;
        //                }
        //            }
        //        }

        PsiElement elementInScope = BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true);
        if (elementInScope != null) {
            return elementInScope;
        }

        PsiFile containingFile = identifier.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory psiDirectory = originalFile.getParent();
        if (psiDirectory == null) {
            return null;
        }
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true, true, true, true);
    }

    @Nullable
    private PsiElement resolveInPackage(@NotNull PackageNameNode packageNameNode) {
        IdentifierPSINode identifier = getElement();
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (!(resolvedElement instanceof PsiDirectory)) {
            return null;
        }
        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
        return BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true, true, true, true);
    }


    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

        if (containingPackage != null) {


            List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                    PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                    BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            results.addAll(packages);

            // Todo - use util?
            //            List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(containingFile);
            //            for (PsiElement importedPackage : importedPackages) {
            //                PsiReference reference = importedPackage.findReferenceAt(0);
            //                if (reference == null) {
            //                    continue;
            //                }
            //                PsiElement resolvedElement = reference.resolve();
            //                if (resolvedElement == null) {
            //                    continue;
            //                }
            //                PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
            //                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement
            // (resolvedPackage,
            //                        PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            //                results.add(lookupElement);
            //            }
            //
            //            List<PsiDirectory> unImportedPackages = BallerinaPsiImplUtil.getAllUnImportedPackages
            //                    (containingFile);
            //            for (PsiDirectory unImportedPackage : unImportedPackages) {
            //                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement
            // (unImportedPackage,
            //                        BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            //                results.add(lookupElement);
            //            }

            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);

            //            GlobalVariableDefinitionNode globalVariableDefinitionNode = PsiTreeUtil.getParentOfType
            // (identifier,
            //                    GlobalVariableDefinitionNode.class);
            //            if (globalVariableDefinitionNode == null) {


            ANTLRPsiNode definitionParent = PsiTreeUtil.getParentOfType(identifier, CallableUnitBodyNode.class,
                    ServiceBodyNode.class, ConnectorBodyNode.class);
            if (definitionParent != null || prevVisibleLeaf != null && !";".equals(prevVisibleLeaf.getText())) {
                //            if (prevVisibleLeaf != null && !";".equals(prevVisibleLeaf.getText())) {
                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
                results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));
                //            }
            }


            //            }

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

            // Todo - use a util method
            ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                    TopLevelDefinition.class, LowerLevelDefinition.class);
            if (scope != null) {

                int caretOffset = identifier.getStartOffset();

                List<PsiElement> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                        caretOffset);
                results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

                List<PsiElement> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

                List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
                results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
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

        // Todo - use a util method
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                AddSpaceInsertHandler.INSTANCE));

        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));

        List<PsiElement> globalVariables =
                BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

        List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(containingPackage);
        results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));

        return results;
    }
}
