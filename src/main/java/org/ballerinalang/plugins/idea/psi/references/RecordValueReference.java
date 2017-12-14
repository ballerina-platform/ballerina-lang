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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.EnumFieldNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.RecordKeyNode;
import org.ballerinalang.plugins.idea.psi.RecordKeyValueNode;
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

public class RecordValueReference extends BallerinaElementReference {

    public RecordValueReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();
        PsiElement prevSibling = identifier.getPrevSibling();
        if (prevSibling != null && prevSibling.getPrevSibling() != null &&
                prevSibling.getPrevSibling() instanceof PackageNameNode) {
            PsiReference reference = prevSibling.getPrevSibling().findReferenceAt(0);
            if (reference != null) {
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement instanceof PsiDirectory) {
                    PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
                    // First we try to resolve the reference to following definitions.
                    PsiElement element = BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true,
                            true,
                            true, true, true, true, true, true);
                    if (element != null) {
                        return element;
                    }
                }
            }
        } else {
            PsiFile containingFile = identifier.getContainingFile();
            if (containingFile == null) {
                return null;
            }
            PsiFile originalFile = containingFile.getOriginalFile();
            PsiDirectory psiDirectory = originalFile.getParent();
            if (psiDirectory == null) {
                return null;
            }
            // First we try to resolve the reference to following definitions.
            PsiElement element = BallerinaPsiImplUtil.resolveElementInPackage(psiDirectory, identifier, true, true,
                    true, true, true, true, true, true);
            if (element != null) {
                return element;
            }
        }
        // If the reference is not resolved to an above definition, we try to resolve it to below definition.
        return BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true, true);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();

        PsiElement prevSibling = identifier.getPrevSibling();
        if (prevSibling != null && prevSibling.getPrevSibling() != null &&
                prevSibling.getPrevSibling() instanceof PackageNameNode) {
            PsiReference reference = prevSibling.getPrevSibling().findReferenceAt(0);
            if (reference != null) {
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement instanceof PsiDirectory) {
                    PsiDirectory currentPackage = (PsiDirectory) resolvedElement;
                    List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(currentPackage,
                            true, true);
                    results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));

                    List<IdentifierPSINode> enums = BallerinaPsiImplUtil.getAllEnumsFromPackage(currentPackage, true,
                            true);
                    results.addAll(BallerinaCompletionUtils.createEnumLookupElements(enums, null));

                    List<IdentifierPSINode> globalVariables =
                            BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(currentPackage, true, true);
                    results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                    List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(currentPackage,
                            true, true);
                    results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));

                    if (!results.isEmpty()) {
                        return results.toArray(new LookupElement[results.size()]);
                    }
                }
            }
        }

        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        results.addAll(packages);

        // Todo - improve suggesting elements from a package in StructValueNode since it will not be identified properly
        RecordKeyValueNode recordKeyValueNode = PsiTreeUtil.getParentOfType(identifier,
                RecordKeyValueNode.class);
        if (recordKeyValueNode != null)

        {
            RecordKeyNode mapStructKeyNode = PsiTreeUtil.getChildOfType(recordKeyValueNode, RecordKeyNode.class);
            if (mapStructKeyNode != null) {

                // Todo - use util?
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
                    if (mapStructKeyNode.getText().equals(resolvedPackage.getName())) {

                        List<IdentifierPSINode> functions =
                                BallerinaPsiImplUtil.getAllFunctionsFromPackage(resolvedPackage, false, false);
                        results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));

                        Collection<EnumFieldNode> enums = PsiTreeUtil.findChildrenOfType(resolvedPackage,
                                EnumFieldNode.class);
                        results.addAll(BallerinaCompletionUtils.createEnumFieldLookupElements(enums,
                                (IdentifierPSINode) resolvedElement));

                        List<IdentifierPSINode> globalVariables =
                                BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(resolvedPackage, false, false);
                        results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                        List<IdentifierPSINode> constants =
                                BallerinaPsiImplUtil.getAllConstantsFromPackage(resolvedPackage, false, false);
                        results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
                    }
                }
            }
        }

        PsiDirectory currentPackage = originalFile.getParent();
        if (currentPackage != null)

        {
            List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(currentPackage,
                    true, true);
            results.addAll(BallerinaCompletionUtils.createFunctionLookupElements(functions));

            List<IdentifierPSINode> enums = BallerinaPsiImplUtil.getAllEnumsFromPackage(currentPackage, true, true);
            results.addAll(BallerinaCompletionUtils.createEnumLookupElements(enums, null));

            List<IdentifierPSINode> globalVariables =
                    BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(currentPackage, true, true);
            results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(currentPackage,
                    true, true);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        }

        // Todo - use a util method
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null)

        {
            int caretOffset = identifier.getStartOffset();

            List<IdentifierPSINode> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                    caretOffset);
            results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                    caretOffset);
            results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

            List<IdentifierPSINode> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope
                    (scope);
            results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        }
        return results.toArray(new LookupElement[results.size()]);
    }
}
