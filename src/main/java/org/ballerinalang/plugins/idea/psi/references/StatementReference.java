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
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class StatementReference extends BallerinaElementReference {

    public StatementReference(@NotNull IdentifierPSINode element) {
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

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        // Todo - do we need to check the text to be equal to ":" ?
        if (prevVisibleLeaf != null) {

            PsiElement packageName = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
            if (packageName != null) {


                PsiReference reference = packageName.findReferenceAt(0);
                if (reference == null) {
                    return new Object[0];
                }

                PsiElement resolvedElement = reference.resolve();
                if (!(resolvedElement instanceof PsiDirectory)) {
                    return new Object[0];
                }

                PsiDirectory containingPackage = (PsiDirectory) resolvedElement;


                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
                results.addAll(BallerinaCompletionUtils.createFunctionsLookupElements(functions));

                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
                results.addAll(BallerinaCompletionUtils.createConnectorLookupElements(connectors,
                        AddSpaceInsertHandler.INSTANCE));

                List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
                results.addAll(BallerinaCompletionUtils.createStructLookupElements(structs));


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

            }


        }

        return results.toArray(new LookupElement[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // WARNING: If find usage shows an error message, verify that the super.resolve() returns an Identifier node.
        //        ResolveResult[] resolveResults = multiResolve(false);
        //        return resolveResults.length != 0 ? resolveResults[0].getElement() : super.resolve();

        IdentifierPSINode identifier = getElement();

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(identifier);
        // Todo - do we need to check the text to be equal to ":" ?
        if (prevVisibleLeaf != null) {
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


                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);
                for (PsiElement function : functions) {
                    if (identifier.getText().equals(function.getText())) {
                        return function;
                    }
                }

                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(containingPackage);
                for (PsiElement connector : connectors) {
                    if (identifier.getText().equals(connector.getText())) {
                        return connector;
                    }
                }

                List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(containingPackage);
                for (PsiElement struct : structs) {
                    if (identifier.getText().equals(struct.getText())) {
                        return struct;
                    }
                }


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

            }
        }
        return null;
    }
}
