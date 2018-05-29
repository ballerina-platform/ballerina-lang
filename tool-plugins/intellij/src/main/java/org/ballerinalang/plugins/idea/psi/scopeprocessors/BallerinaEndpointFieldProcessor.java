/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.completion.inserthandlers.ColonInsertHandler;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointType;
import org.ballerinalang.plugins.idea.psi.BallerinaExpression;
import org.ballerinalang.plugins.idea.psi.BallerinaFieldDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaRecordKey;
import org.ballerinalang.plugins.idea.psi.BallerinaRecordKeyValue;
import org.ballerinalang.plugins.idea.psi.BallerinaRecordLiteralExpression;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Responsible for resolving and completing endpoint fields.
 */
public class BallerinaEndpointFieldProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaEndpointFieldProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                           boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaEndpointDefinition;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            BallerinaRecordLiteralExpression recordLiteralExpression = PsiTreeUtil.getParentOfType(myElement,
                    BallerinaRecordLiteralExpression.class);
            PsiElement ownerName;
            BallerinaTypeDefinition typeDefinition;
            if (recordLiteralExpression != null) {
                BallerinaRecordKeyValue recordKeyValue = PsiTreeUtil.getParentOfType(recordLiteralExpression,
                        BallerinaRecordKeyValue.class);
                if (recordKeyValue == null) {
                    return true;
                } else {
                    BallerinaRecordKey recordKey = recordKeyValue.getRecordKey();
                    BallerinaExpression expression = recordKey.getExpression();
                    if (expression == null) {
                        return true;
                    }
                    PsiReference reference = expression.findReferenceAt(expression.getTextLength());
                    if (reference == null) {
                        return true;
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null || !(resolvedElement.getParent() instanceof BallerinaFieldDefinition)) {
                        return true;
                    }
                    BallerinaFieldDefinition fieldDefinition = (BallerinaFieldDefinition) resolvedElement.getParent();
                    PsiElement type = BallerinaPsiImplUtil.getTypeNameFromField(fieldDefinition);
                    if (type == null) {
                        return true;
                    }
                    if (type.getParent() instanceof BallerinaTypeDefinition) {
                        ownerName = type;
                        typeDefinition = (BallerinaTypeDefinition) type.getParent();
                    } else {
                        PsiReference typeReference = type.findReferenceAt(type.getTextLength());
                        if (typeReference == null) {
                            return true;
                        }
                        ownerName = typeReference.resolve();
                        if (ownerName == null || !(ownerName.getParent() instanceof BallerinaTypeDefinition)) {
                            return true;
                        }
                        typeDefinition = (BallerinaTypeDefinition) ownerName.getParent();
                    }
                }
            } else {
                BallerinaEndpointType endpointType = ((BallerinaEndpointDefinition) element).getEndpointType();
                if (endpointType == null) {
                    return true;
                }
                PsiElement endpointTypeIdentifier = endpointType.getNameReference().getIdentifier();
                PsiReference endpointTypeReference = endpointTypeIdentifier.getReference();
                if (endpointTypeReference == null) {
                    return true;
                }
                PsiElement resolvedElement = endpointTypeReference.resolve();
                if (resolvedElement == null) {
                    return true;
                }
                PsiElement parent = resolvedElement.getParent();
                if (!(parent instanceof BallerinaTypeDefinition)) {
                    return true;
                }

                ownerName = BallerinaPsiImplUtil.getConfigTypeDefinitionFromListener((BallerinaTypeDefinition) parent);
                if (ownerName == null || !(ownerName.getParent() instanceof BallerinaTypeDefinition)) {
                    return true;
                }
                typeDefinition = (BallerinaTypeDefinition) ownerName.getParent();
            }

            Collection<BallerinaFieldDefinition> fieldDefinitions = PsiTreeUtil.findChildrenOfType(typeDefinition,
                    BallerinaFieldDefinition.class);
            for (BallerinaFieldDefinition fieldDefinition : fieldDefinitions) {
                PsiElement definitionIdentifier = fieldDefinition.getIdentifier();
                String typeName = "Type";
                PsiElement typeNameFromField = BallerinaPsiImplUtil.getTypeNameFromField(fieldDefinition);
                if (typeNameFromField != null) {
                    typeName = typeNameFromField.getText();
                }
                if (myResult != null) {
                    // Todo - Conside oncommit, onabort, etc and set the insert handler
                    // Note - Child is passed here instead of identifier because it is is top level
                    // definition.
                    myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(
                            definitionIdentifier, ownerName, typeName, null, ColonInsertHandler.INSTANCE_WITH_SPACE,
                            true));
                } else if (myElement.getText().equals(definitionIdentifier.getText())) {
                    add(definitionIdentifier);
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCompletion() {
        return myIsCompletion;
    }

    @Override
    protected boolean crossOff(@NotNull PsiElement e) {
        return false;
    }
}
