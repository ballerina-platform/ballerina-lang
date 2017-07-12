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
import org.ballerinalang.plugins.idea.completion.BallerinaAutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.MapStructKeyNode;
import org.ballerinalang.plugins.idea.psi.MapStructKeyValueNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
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

public class StructValueReference extends BallerinaElementReference {

    public StructValueReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return super.resolve();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();

        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        // Todo -  Add functions, global variables, constants in current package from name reference


        // Todo - use util
        //                List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(containingFile);
        //        for (PsiElement importedPackage : importedPackages) {
        //            PsiReference reference = importedPackage.findReferenceAt(0);
        //            if (reference == null) {
        //                continue;
        //            }
        //            PsiElement resolvedElement = reference.resolve();
        //            if (resolvedElement == null) {
        //                continue;
        //            }
        //            PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
        //            LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement(resolvedPackage,
        //                    PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        //            results.add(lookupElement);
        //        }
        //
        //        List<PsiDirectory> unImportedPackages = BallerinaPsiImplUtil.getAllUnImportedPackages
        //                (containingFile);
        //        for (PsiDirectory unImportedPackage : unImportedPackages) {
        //            LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement
        // (unImportedPackage,
        //                    BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        //            results.add(lookupElement);
        //        }
        List<LookupElement> packages = BallerinaPsiImplUtil.getPackagesAsLookups(originalFile, true,
                PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP, true,
                BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        results.addAll(packages);

        // Todo - improve suggesting elements from a package in StructValueNode since it will not be identified properly
        MapStructKeyValueNode mapStructKeyValueNode = PsiTreeUtil.getParentOfType(identifier,
                MapStructKeyValueNode.class);

        if (mapStructKeyValueNode != null) {

            MapStructKeyNode mapStructKeyNode = PsiTreeUtil.getChildOfType(mapStructKeyValueNode,
                    MapStructKeyNode.class);

            if (mapStructKeyNode != null) {
                //            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parent);
                //            if (prevVisibleLeaf != null && ":".equals(prevVisibleLeaf.getText())) {
                //                PsiElement packageName = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                //                if (packageName != null) {


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

                        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(resolvedPackage);
                        results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

                        List<PsiElement> globalVariables =
                                BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(resolvedPackage);
                        results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

                        List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(resolvedPackage);
                        results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
                    }
                }
            }
        }

        PsiDirectory currentPackage = originalFile.getParent();

        if (currentPackage != null) {
            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(currentPackage);
            results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));


            List<PsiElement> globalVariables =
                    BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(currentPackage);
            results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(currentPackage);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        }

        // Todo - use a util method
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {

            int caretOffset = identifier.getStartOffset();

            List<PsiElement> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                    caretOffset);
            results.addAll(BallerinaCompletionUtils.createVariableLookupElements(variables));

            List<PsiElement> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                    caretOffset);
            results.addAll(BallerinaCompletionUtils.createParameterLookupElements(parameters));

            List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope
                    (scope);
            results.addAll(BallerinaCompletionUtils.createGlobalVariableLookupElements(globalVariables));

            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
            results.addAll(BallerinaCompletionUtils.createConstantLookupElements(constants));
        }


        //        IdentifierPSINode identifier = getElement();
        //        PsiElement parent = identifier.getParent();
        //
        //        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        //        if (packageNameNode == null) {
        //            results.addAll(getVariantsFromCurrentPackage());
        //        } else {
        //            results.addAll(getVariantsFromPackage(packageNameNode));
        //        }
        return results.toArray(new LookupElement[results.size()]);
    }

    @NotNull
    private List<LookupElement> getVariantsFromCurrentPackage() {
        List<LookupElement> results = new LinkedList<>();

        IdentifierPSINode identifier = getElement();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();

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

        if (containingPackage != null) {

            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
            results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

        }


        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                VariableDefinitionNode.class);
        if (variableDefinitionNode == null) {

        } else {
            TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(variableDefinitionNode, TypeNameNode.class);
            if (typeNameNode == null) {
                // Todo - resolve variable to definition
            } else {
                PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
                if (reference == null) {
                    return results;
                }

                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return results;
                }

                PsiElement resolvedElementParent = resolvedElement.getParent();
                if (resolvedElementParent instanceof StructDefinitionNode) {

                    // Todo - use an util method?
                    Collection<FieldDefinitionNode> fieldDefinitionNodes =
                            PsiTreeUtil.findChildrenOfType(resolvedElementParent, FieldDefinitionNode.class);
                    for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
                        IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode,
                                IdentifierPSINode.class);
                        TypeNameNode fieldType = PsiTreeUtil.getChildOfType(fieldDefinitionNode, TypeNameNode.class);
                        if (fieldName == null || fieldType == null) {
                            continue;
                        }
                        LookupElement lookupElement = BallerinaCompletionUtils.createFieldLookupElement(fieldName,
                                fieldType, (IdentifierPSINode) resolvedElement);
                        results.add(lookupElement);
                    }

                }
            }
        }
        return results;
    }

    @NotNull
    private List<LookupElement> getVariantsFromPackage(@NotNull PackageNameNode packageNameNode) {
        return new LinkedList<>();
    }
}
