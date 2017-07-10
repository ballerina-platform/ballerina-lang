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
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.PackageCompletionInsertHandler;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StatementReference extends BallerinaElementReference {

    public StatementReference(@NotNull IdentifierPSINode element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        IdentifierPSINode identifier = getElement();

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        if (prevVisibleLeaf != null && ":".equals(prevVisibleLeaf.getText())) {
            PsiElement packageName = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (packageName != null) {
                PsiReference reference = packageName.findReferenceAt(0);
                if (reference == null) {
                    return null;
                }

                PsiElement resolvedElement = reference.resolve();
                if (!(resolvedElement instanceof PsiDirectory)) {
                    return null;
                }

                PsiDirectory containingPackage = (PsiDirectory) resolvedElement;


                //                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage
                // (containingPackage);
                //                for (PsiElement function : functions) {
                //                    if (identifier.getText().equals(function.getText())) {
                //                        return function;
                //                    }
                //                }
                //
                //                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage
                // (containingPackage);
                //                for (PsiElement connector : connectors) {
                //                    if (identifier.getText().equals(connector.getText())) {
                //                        return connector;
                //                    }
                //                }
                //
                //                List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage
                // (containingPackage);
                //                for (PsiElement struct : structs) {
                //                    if (identifier.getText().equals(struct.getText())) {
                //                        return struct;
                //                    }
                //                }


                // Todo - Add global variables and constants

                //                List<PsiElement> globalVariables = getAllGlobalVariablesInResolvableScope(scope);
                //                for (PsiElement variable : globalVariables) {
                //                    LookupElement lookupElement = BallerinaCompletionUtils
                // .createGlobalVariableLookupElement
                //                            (variable);
                //                    results.add(lookupElement);
                //                }
                //
                //                List<PsiElement> constants = getAllConstantsInResolvableScope(scope);
                //                for (PsiElement constant : constants) {
                //                    LookupElement lookupElement = BallerinaCompletionUtils
                // .createConstantLookupElement(constant);
                //                    results.add(lookupElement);
                //                }


                return BallerinaPsiImplUtil.resolveElementInPackage(containingPackage, identifier, true, true, true,
                        true, true);
            }
        } else {
            return BallerinaPsiImplUtil.resolveElementInScope(identifier, true, true, true, true);
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        List<LookupElement> results = new LinkedList<>();
        IdentifierPSINode identifier = getElement();
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);

        if (prevVisibleLeaf == null) {
            return new LookupElement[0];
        }

        if (":".equals(prevVisibleLeaf.getText())) {
            PsiElement packageName = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (packageName == null) {
                return new LookupElement[0];
            }
            PsiReference reference = packageName.findReferenceAt(0);
            if (reference == null) {
                return new Object[0];
            }
            PsiElement resolvedElement = reference.resolve();
            if (!(resolvedElement instanceof PsiDirectory)) {
                return new Object[0];
            }
            PsiDirectory containingPackage = (PsiDirectory) resolvedElement;
            results.addAll(getVariantsInPackage(containingPackage));
        } else if (".".equals(prevVisibleLeaf.getText())) {
            // Todo - suggest length field
            PsiElement previousField = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (previousField == null) {
                return new LookupElement[0];
            }
            PsiReference reference = previousField.findReferenceAt(0);
            if (reference == null) {
                return new Object[0];
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return new LookupElement[0];
            }

            // Todo - use util method
            PsiElement resolvedElementParent = resolvedElement.getParent();
            StructDefinitionNode structDefinitionNode = null;
            // Resolve the corresponding resolvedElementParent to get the struct definition.
            if (resolvedElementParent instanceof VariableDefinitionNode) {
                structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode
                        (((VariableDefinitionNode) resolvedElementParent));
            } else if (resolvedElementParent instanceof FieldDefinitionNode) {
                structDefinitionNode =
                        BallerinaPsiImplUtil.resolveField(((FieldDefinitionNode) resolvedElementParent));
            }
            if (structDefinitionNode == null) {
                return results.toArray(new LookupElement[results.size()]);
            }

            Collection<FieldDefinitionNode> fieldDefinitionNodes =
                    PsiTreeUtil.findChildrenOfType(structDefinitionNode, FieldDefinitionNode.class);
            //                        for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
            //                            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType
            // (fieldDefinitionNode,
            //                                    IdentifierPSINode.class);
            //                            if (fieldName == null) {
            //                                continue;
            //                            }
            //                            TypeNameNode fieldType = PsiTreeUtil.getChildOfType
            // (fieldDefinitionNode,
            //                                    TypeNameNode.class);
            //                            if (fieldType == null) {
            //                                continue;
            //                            }
            //                            LookupElement lookupElement =
            //                                    BallerinaCompletionUtils.createFieldLookupElement
            // (fieldName, fieldType,
            //                                            (IdentifierPSINode) resolvedElement);
            //                            results.add(lookupElement);
            //                        }

            List<LookupElement> fields =
                    BallerinaCompletionUtils.createFieldLookupElements(fieldDefinitionNodes,
                            (IdentifierPSINode) resolvedElement,
                            PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            results.addAll(fields);
        }
        return results.toArray(new LookupElement[results.size()]);
    }

    private List<LookupElement> getVariantsInPackage(@NotNull PsiDirectory containingPackage) {

        List<LookupElement> results = new LinkedList<>();

        // Todo - add util method
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
