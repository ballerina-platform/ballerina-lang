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

package io.ballerina.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.completion.inserthandlers.SmartParenthesisInsertHandler;
import io.ballerina.plugins.idea.psi.BallerinaArrayTypeName;
import io.ballerina.plugins.idea.psi.BallerinaAttachedObject;
import io.ballerina.plugins.idea.psi.BallerinaBuiltInReferenceTypeName;
import io.ballerina.plugins.idea.psi.BallerinaField;
import io.ballerina.plugins.idea.psi.BallerinaFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFieldDefinitionList;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.BallerinaMapArrayVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaNullableTypeName;
import io.ballerina.plugins.idea.psi.BallerinaObjectFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaRecordTypeName;
import io.ballerina.plugins.idea.psi.BallerinaSimpleTypeName;
import io.ballerina.plugins.idea.psi.BallerinaSimpleVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypeName;
import io.ballerina.plugins.idea.psi.BallerinaUnionTypeName;
import io.ballerina.plugins.idea.psi.BallerinaVariableReference;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Responsible for resolving and completing object fields.
 */
public class BallerinaFieldProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaFieldProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                   boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element.getParent() instanceof BallerinaField;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            PsiElement parent = element.getParent();
            PsiElement prevSibling = parent.getPrevSibling();
            if (!(prevSibling instanceof BallerinaVariableReference)) {
                return true;
            }

            // Note - This check should occur before resolving the type. Otherwise there will be a performance issue
            // when the "self" is tried to resolve to a definition.
            if (prevSibling instanceof BallerinaSimpleVariableReference && "self".equals(prevSibling.getText())) {
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
                if (resolvedElement == null || !(resolvedElement.getParent() instanceof BallerinaTypeDefinition)) {
                    return true;
                }
                return processTypeDefinition(((BallerinaTypeDefinition) resolvedElement.getParent()));
            } else if (prevSibling instanceof BallerinaMapArrayVariableReference) {
                prevSibling = ((BallerinaMapArrayVariableReference) prevSibling).getVariableReference();
            }

            PsiElement type = ((BallerinaVariableReference) prevSibling).getType();

            // Todo - Refactor and remove duplication in BallerinaInvocationProcessor
            if (type != null) {
                // Anonymous objects.
                if (type instanceof BallerinaRecordTypeName) {
                    PsiElement definition = type.getParent();
                    BallerinaIdentifier identifier = PsiTreeUtil.getChildOfType(definition, BallerinaIdentifier.class);
                    if (identifier != null) {
                        BallerinaFieldDefinitionList fieldDefinitionList = PsiTreeUtil.findChildOfType(type,
                                BallerinaFieldDefinitionList.class);
                        List<BallerinaFieldDefinition> fieldDefinitions =
                                PsiTreeUtil.getChildrenOfTypeAsList(fieldDefinitionList,
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
                                        definitionIdentifier, identifier, typeName, null, null, false));
                            } else if (myElement.getText().equals(definitionIdentifier.getText())) {
                                add(definitionIdentifier);
                            }
                        }
                    }
                    return false;
                } else if (type instanceof BallerinaNullableTypeName) {
                    BallerinaTypeName nillableType =
                            BallerinaPsiImplUtil.getTypeNameFromNillableType(((BallerinaNullableTypeName) type));
                    PsiElement identifier = BallerinaPsiImplUtil.resolveTypeToDefinition(nillableType);
                    if (identifier != null && identifier.getParent() instanceof BallerinaTypeDefinition) {
                        processTypeDefinition(((BallerinaTypeDefinition) identifier.getParent()), identifier);
                    }
                } else if (type instanceof BallerinaUnionTypeName) {
                    BallerinaTypeName typeName =
                            BallerinaPsiImplUtil.liftErrorAndGetType(((BallerinaUnionTypeName) type));
                    if (typeName != null) {
                        PsiElement identifier = BallerinaPsiImplUtil.resolveTypeToDefinition(typeName);
                        if (identifier != null && identifier.getParent() instanceof BallerinaTypeDefinition) {
                            processTypeDefinition(((BallerinaTypeDefinition) identifier.getParent()), identifier);
                        }
                    }
                } else if (BallerinaPsiImplUtil.isConstraintableType(type)) {
                    PsiElement identifier = BallerinaPsiImplUtil.getConstrainedType(type);
                    if (identifier != null && identifier.getParent() instanceof BallerinaTypeDefinition) {
                        processTypeDefinition(((BallerinaTypeDefinition) identifier.getParent()), identifier);
                    }
                }

                PsiElement ballerinaTypeDefinition = type.getParent();
                if (ballerinaTypeDefinition instanceof BallerinaTypeDefinition) {
                    BallerinaObjectFieldProcessor ballerinaFieldProcessor = new BallerinaObjectFieldProcessor(myResult,
                            myElement, isCompletion());
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
                } else if (type instanceof BallerinaSimpleTypeName) {
                    PsiElement definition =
                            BallerinaPsiImplUtil.resolveTypeToDefinition((BallerinaTypeName) type);
                    if (definition == null) {
                        List<BallerinaFunctionDefinition> functionDefinitions =
                                BallerinaPsiImplUtil.suggestBuiltInFunctions(type);
                        for (BallerinaFunctionDefinition functionDefinition : functionDefinitions) {
                            PsiElement identifier = functionDefinition.getIdentifier();
                            if (identifier != null) {
                                if (myResult != null) {
                                    // Todo - Conside oncommit, onabort, etc and set the insert handler
                                    // Note - Child is passed here instead of identifier because it is is top level
                                    // definition.
                                    myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement(
                                            functionDefinition, SmartParenthesisInsertHandler.INSTANCE));
                                } else if (myElement.getText().equals(identifier.getText())) {
                                    add(identifier);
                                }
                            }
                        }
                        List<BallerinaTypeDefinition> typeDefinitions = BallerinaPsiImplUtil.suggestBuiltInTypes(type);
                        for (BallerinaTypeDefinition typeDefinition : typeDefinitions) {
                            PsiElement identifier = typeDefinition.getIdentifier();
                            if (identifier != null && type.getText().equals(identifier.getText())) {
                                processTypeDefinition(typeDefinition, identifier);
                            }
                        }
                    } else {
                        if (definition.getParent() instanceof BallerinaTypeDefinition) {
                            processTypeDefinition(((BallerinaTypeDefinition) definition.getParent()), definition);
                        }
                    }
                } else if (type instanceof BallerinaArrayTypeName) {
                    if (element.getParent().getPrevSibling() instanceof BallerinaMapArrayVariableReference) {
                        List<BallerinaFunctionDefinition> functionDefinitions =
                                BallerinaPsiImplUtil.suggestBuiltInFunctions(((BallerinaArrayTypeName) type)
                                        .getTypeName());

                        for (BallerinaFunctionDefinition functionDefinition : functionDefinitions) {
                            PsiElement identifier = functionDefinition.getIdentifier();
                            if (identifier != null) {
                                if (myResult != null) {
                                    // Todo - Conside oncommit, onabort, etc and set the insert handler
                                    // Note - Child is passed here instead of identifier because it is is top level
                                    // definition.
                                    myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement(
                                            functionDefinition, SmartParenthesisInsertHandler.INSTANCE));
                                } else if (myElement.getText().equals(identifier.getText())) {
                                    add(identifier);
                                }
                            }
                        }
                    }
                } else if (type.getParent().getParent() instanceof BallerinaBuiltInReferenceTypeName) {
                    // Note - Built-in functions for xml literals in var assignments are handled from here.
                    List<BallerinaFunctionDefinition> functionDefinitions =
                            BallerinaPsiImplUtil.suggestBuiltInFunctions(type);

                    for (BallerinaFunctionDefinition functionDefinition : functionDefinitions) {
                        PsiElement identifier = functionDefinition.getIdentifier();
                        if (identifier != null) {
                            if (myResult != null) {
                                // Todo - Conside oncommit, onabort, etc and set the insert handler
                                // Note - Child is passed here instead of identifier because it is is top level
                                // definition.
                                myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement(
                                        functionDefinition, SmartParenthesisInsertHandler.INSTANCE));
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    }
                } else if (type.getParent().getParent() instanceof BallerinaSimpleTypeName) {
                    // Note - Built-in functions for string literals in var assignments are handled from here.
                    List<BallerinaFunctionDefinition> functionDefinitions =
                            BallerinaPsiImplUtil.suggestBuiltInFunctions(type);

                    for (BallerinaFunctionDefinition functionDefinition : functionDefinitions) {
                        PsiElement identifier = functionDefinition.getIdentifier();
                        if (identifier != null) {
                            if (myResult != null) {
                                // Todo - Conside oncommit, onabort, etc and set the insert handler
                                // Note - Child is passed here instead of identifier because it is is top level
                                // definition.
                                myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement(
                                        functionDefinition, SmartParenthesisInsertHandler.INSTANCE));
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void processTypeDefinition(@NotNull BallerinaTypeDefinition typeDefinition,
                                       @NotNull PsiElement identifier) {
        BallerinaFieldDefinitionList fieldDefinitionList =
                PsiTreeUtil.findChildOfType(typeDefinition, BallerinaFieldDefinitionList.class);
        List<BallerinaFieldDefinition> fieldDefinitions =
                PsiTreeUtil.getChildrenOfTypeAsList(fieldDefinitionList,
                        BallerinaFieldDefinition.class);

        for (BallerinaFieldDefinition fieldDefinition : fieldDefinitions) {
            PsiElement definitionIdentifier = fieldDefinition.getIdentifier();
            String typeName = "Type";
            PsiElement typeNameFromField = BallerinaPsiImplUtil.getTypeNameFromField
                    (fieldDefinition);
            if (typeNameFromField != null) {
                typeName = typeNameFromField.getText();
            }
            if (myResult != null) {
                // Todo - Conside oncommit, onabort, etc and set the insert handler
                // Note - Child is passed here instead of identifier because it is is top level
                // definition.
                myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(
                        definitionIdentifier, identifier, typeName, null, null, false));
            } else if (myElement.getText().equals(definitionIdentifier.getText())) {
                add(definitionIdentifier);
            }
        }

        Collection<BallerinaObjectFunctionDefinition> functionDefinitions =
                PsiTreeUtil.findChildrenOfType(typeDefinition, BallerinaObjectFunctionDefinition.class);
        for (BallerinaObjectFunctionDefinition functionDefinition : functionDefinitions) {
            if (myResult != null) {
                myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement(
                        functionDefinition, identifier, SmartParenthesisInsertHandler.INSTANCE));
            } else if (myElement.getText().equals(identifier.getText())) {
                add(identifier);
            }
        }
    }

    private boolean processTypeDefinition(@NotNull BallerinaTypeDefinition ballerinaTypeDefinition) {
        BallerinaObjectFieldProcessor ballerinaFieldProcessor =
                new BallerinaObjectFieldProcessor(myResult, myElement, isCompletion());
        ballerinaFieldProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
        PsiElement result = ballerinaFieldProcessor.getResult();
        if (!isCompletion() && result != null) {
            add(result);
            return false;
        }

        // Note - This is needed for code completion.
        // Todo - Remove duplication in BallerinaInvocationProcessor
        BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor
                = new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
        ballerinaObjectFunctionProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
        result = ballerinaObjectFunctionProcessor.getResult();
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
