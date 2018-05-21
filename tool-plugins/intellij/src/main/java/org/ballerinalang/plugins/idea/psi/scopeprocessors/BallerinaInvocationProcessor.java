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
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.inserthandlers.SmartParenthesisInsertHandler;
import org.ballerinalang.plugins.idea.psi.BallerinaAnyIdentifierName;
import org.ballerinalang.plugins.idea.psi.BallerinaAttachedObject;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleTypeName;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleVariableReference;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableReference;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing invocations.
 */
public class BallerinaInvocationProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaInvocationProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                        boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element.getParent() instanceof BallerinaAnyIdentifierName;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            PsiElement parent = element.getParent();
            if (parent == null) {
                return true;
            }
            PsiElement superParent = parent.getParent();
            if (superParent == null) {
                return true;
            }
            PsiElement prevSibling = superParent.getPrevSibling();
            if (prevSibling != null && prevSibling instanceof BallerinaVariableReference) {
                PsiElement type = ((BallerinaVariableReference) prevSibling).getType();

                // Todo - Refactor and remove duplication in BallerinaFieldProcessor
                if (type != null) {
                    // Resolve to built-in package.
                    if (type instanceof BallerinaSimpleTypeName || type instanceof LeafPsiElement) {

                        if (BallerinaPsiImplUtil.hasBuiltInDefinitions(type)) {
                            List<BallerinaFunctionDefinition> definitions =
                                    BallerinaPsiImplUtil.suggestBuiltInFunctions(type);
                            for (BallerinaFunctionDefinition definition : definitions) {
                                PsiElement identifier = definition.getIdentifier();
                                if (identifier != null) {
                                    if (myResult != null) {
                                        myResult.addElement(BallerinaCompletionUtils
                                                .createFunctionLookupElement(definition,
                                                        SmartParenthesisInsertHandler.INSTANCE));
                                    } else if (myElement.getText().equals(identifier.getText())) {
                                        add(identifier);
                                    }
                                }
                            }
                        }
                    }

                    PsiElement ballerinaTypeDefinition = type.getParent();
                    if (ballerinaTypeDefinition instanceof BallerinaTypeDefinition) {
                        BallerinaObjectFieldProcessor ballerinaFieldProcessor = new BallerinaObjectFieldProcessor
                                (myResult, myElement, isCompletion());
                        ballerinaFieldProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
                        PsiElement result = ballerinaFieldProcessor.getResult();
                        if (!isCompletion() && result != null) {
                            add(result);
                            return false;
                        }

                        BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor
                                = new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
                        ballerinaObjectFunctionProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
                        result = ballerinaObjectFunctionProcessor.getResult();
                        if (!isCompletion() && result != null) {
                            add(result);
                            return false;
                        }
                    }
                } else {
                    if (prevSibling instanceof BallerinaSimpleVariableReference
                            && "self".equals(prevSibling.getText())) {
                        BallerinaTypeDefinition ballerinaTypeDefinition = PsiTreeUtil.getParentOfType(prevSibling,
                                BallerinaTypeDefinition.class);
                        if (ballerinaTypeDefinition != null) {
                            if (!processTypeDefinition(ballerinaTypeDefinition)) {
                                return false;
                            }
                        }

                        BallerinaFunctionDefinition functionDefinition = PsiTreeUtil.getParentOfType(prevSibling,
                                BallerinaFunctionDefinition.class);
                        if (functionDefinition == null || functionDefinition.getAttachedObject() == null) {
                            return true;
                        }
                        BallerinaAttachedObject attachedObject = functionDefinition.getAttachedObject();
                        if (attachedObject == null) {
                            return true;
                        }
                        PsiElement identifier = attachedObject.getIdentifier();
                        PsiReference reference = identifier.getReference();
                        if (reference == null) {
                            return true;
                        }
                        PsiElement resolvedElement = reference.resolve();
                        if (resolvedElement == null || !(resolvedElement.getParent() instanceof
                                BallerinaTypeDefinition)) {
                            return true;
                        }
                        return processTypeDefinition(((BallerinaTypeDefinition) resolvedElement.getParent()));
                    }
                }
            }
        }
        return true;
    }

    private boolean processTypeDefinition(@NotNull BallerinaTypeDefinition ballerinaTypeDefinition) {
        //        BallerinaObjectFieldProcessor ballerinaFieldProcessor =
        //                new BallerinaObjectFieldProcessor(myResult, myElement, isCompletion());
        //        ballerinaFieldProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
        //        PsiElement result = ballerinaFieldProcessor.getResult();
        //        if (!isCompletion() && result != null) {
        //            add(result);
        //            return false;
        //        }

        // Todo - Remove duplication in BallerinaFieldProcessor
        BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor
                = new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
        ballerinaObjectFunctionProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
        PsiElement result = ballerinaObjectFunctionProcessor.getResult();
        if (!isCompletion() && result != null) {
            add(result);
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
