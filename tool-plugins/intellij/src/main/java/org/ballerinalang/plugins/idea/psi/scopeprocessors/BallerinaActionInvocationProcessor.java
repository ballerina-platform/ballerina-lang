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
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointType;
import org.ballerinalang.plugins.idea.psi.BallerinaNameReference;
import org.ballerinalang.plugins.idea.psi.BallerinaServiceDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaServiceEndpointAttachments;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing action invocations.
 */
public class BallerinaActionInvocationProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaActionInvocationProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                              boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return true;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(myElement);
            if (prevVisibleLeaf != null && prevVisibleLeaf instanceof LeafPsiElement) {
                if (((LeafPsiElement) prevVisibleLeaf).getElementType() == BallerinaTypes.RARROW) {
                    PsiElement connector = PsiTreeUtil.prevVisibleLeaf(prevVisibleLeaf);
                    if (connector == null) {
                        return true;
                    }
                    PsiReference reference = connector.getReference();
                    if (reference == null) {
                        return true;
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null) {
                        return true;
                    }
                    PsiElement definition = resolvedElement.getParent();
                    if (definition instanceof BallerinaEndpointParameter) {
                        BallerinaServiceDefinition ballerinaServiceDefinition =
                                PsiTreeUtil.getParentOfType(resolvedElement, BallerinaServiceDefinition.class);
                        if (ballerinaServiceDefinition == null) {
                            return true;
                        }
                        PsiElement parent = null;

                        BallerinaServiceEndpointAttachments serviceEndpointAttachments =
                                ballerinaServiceDefinition.getServiceEndpointAttachments();
                        if (serviceEndpointAttachments != null) {
                            List<BallerinaNameReference> nameReferenceList =
                                    serviceEndpointAttachments.getNameReferenceList();
                            if (!nameReferenceList.isEmpty()) {
                                BallerinaNameReference ballerinaNameReference = nameReferenceList.get(0);
                                PsiElement type = BallerinaPsiImplUtil.getCachedType(ballerinaNameReference);
                                if (type == null) {
                                    return true;
                                }
                                parent = type.getParent();
                            }
                        }
                        if (parent == null) {
                            BallerinaNameReference nameReference = ballerinaServiceDefinition.getNameReference();
                            if (nameReference == null) {
                                return true;
                            }
                            PsiElement typeDefinition = BallerinaPsiImplUtil.getCachedType(nameReference);
                            if (typeDefinition instanceof BallerinaTypeDefinition) {
                                BallerinaTypeDefinition typeName = (BallerinaTypeDefinition) typeDefinition;
                                parent = BallerinaPsiImplUtil.getReturnTypeFromObjectFunction(typeName, "getEndpoint");
                            }
                        }
                        if (parent == null) {
                            return true;
                        }
                        if (parent instanceof BallerinaTypeDefinition) {
                            // Todo - Remove duplicate below
                            BallerinaTypeDefinition clientConnector =
                                    BallerinaPsiImplUtil.getReturnTypeFromObjectFunction((BallerinaTypeDefinition)
                                            parent, "getCallerActions");
                            if (clientConnector != null) {
                                BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor
                                        = new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
                                ballerinaObjectFunctionProcessor.execute(clientConnector, ResolveState.initial());
                                PsiElement result = ballerinaObjectFunctionProcessor.getResult();
                                if (!isCompletion() && result != null) {
                                    add(result);
                                }
                                return false;
                            }
                        }
                    } else if (definition instanceof BallerinaEndpointDefinition) {
                        BallerinaEndpointType endpointType = ((BallerinaEndpointDefinition) definition)
                                .getEndpointType();
                        if (endpointType == null) {
                            return true;
                        }
                        PsiElement identifier = endpointType.getNameReference().getIdentifier();
                        PsiReference identifierReference = identifier.getReference();
                        if (identifierReference == null) {
                            return true;
                        }
                        PsiElement resolvedType = identifierReference.resolve();
                        if (resolvedType == null || !(resolvedType.getParent() instanceof BallerinaTypeDefinition)) {
                            return true;
                        }

                        // Todo - Remove duplicate above
                        BallerinaTypeDefinition clientConnector =
                                BallerinaPsiImplUtil.getReturnTypeFromObjectFunction((BallerinaTypeDefinition)
                                        resolvedType.getParent(), "getCallerActions");
                        if (clientConnector != null) {
                            BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor
                                    = new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
                            ballerinaObjectFunctionProcessor.execute(clientConnector, ResolveState.initial());
                            PsiElement result = ballerinaObjectFunctionProcessor.getResult();
                            if (!isCompletion() && result != null) {
                                add(result);
                            }
                            return false;
                        }
                        return false;
                    }
                }
            }
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
