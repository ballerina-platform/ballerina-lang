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
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.BallerinaAssignmentStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaBlock;
import org.ballerinalang.plugins.idea.psi.BallerinaCallableUnitSignature;
import org.ballerinalang.plugins.idea.psi.BallerinaDefaultableParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaFieldDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaForeachStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaFormalParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaLambdaFunction;
import org.ballerinalang.plugins.idea.psi.BallerinaNameReference;
import org.ballerinalang.plugins.idea.psi.BallerinaNamespaceDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaNamespaceDeclarationStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectBody;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectCallableUnitSignature;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectDefaultableParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectInitializer;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaParameterWithType;
import org.ballerinalang.plugins.idea.psi.BallerinaPrivateObjectFields;
import org.ballerinalang.plugins.idea.psi.BallerinaPublicObjectFields;
import org.ballerinalang.plugins.idea.psi.BallerinaResourceDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaResourceParameterList;
import org.ballerinalang.plugins.idea.psi.BallerinaRestParameter;
import org.ballerinalang.plugins.idea.psi.BallerinaServiceBody;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleVariableReference;
import org.ballerinalang.plugins.idea.psi.BallerinaStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaTupleDestructuringStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableDefinitionStatement;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableReference;
import org.ballerinalang.plugins.idea.psi.BallerinaVariableReferenceList;
import org.ballerinalang.plugins.idea.psi.BallerinaWorkerDefinition;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing definitions in code blocks.
 */
public class BallerinaBlockProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaBlockProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                   boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaBlock;
    }

    @Override
    public boolean execute(@NotNull PsiElement scopeElement, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(scopeElement)) {
            BallerinaBlock block = (BallerinaBlock) scopeElement;

            while (block != null) {
                // Check for workers in all blocks.
                List<BallerinaWorkerDefinition> ballerinaWorkerDefinitions = PsiTreeUtil.getChildrenOfTypeAsList(block,
                        BallerinaWorkerDefinition.class);
                for (BallerinaWorkerDefinition ballerinaWorkerDefinition : ballerinaWorkerDefinitions) {
                    PsiElement identifier = ballerinaWorkerDefinition.getIdentifier();
                    if (identifier == null) {
                        continue;
                    }
                    if (myResult != null) {
                        myResult.addElement(BallerinaCompletionUtils.createWorkerLookupElement(identifier));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                }
                // Check endpoints in the current block.
                List<BallerinaEndpointDefinition> ballerinaEndpointDefinitions = PsiTreeUtil.getChildrenOfTypeAsList
                        (block, BallerinaEndpointDefinition.class);
                for (BallerinaEndpointDefinition ballerinaEndpointDefinition : ballerinaEndpointDefinitions) {
                    PsiElement identifier = ballerinaEndpointDefinition.getIdentifier();
                    if (identifier == null) {
                        continue;
                    }
                    if (myResult != null) {
                        myResult.addElement(BallerinaCompletionUtils.createEndpointLookupElement(identifier));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                }

                // Check for enclosing lambda function.
                BallerinaLambdaFunction ballerinaLambdaFunction = PsiTreeUtil.getParentOfType(block,
                        BallerinaLambdaFunction.class);
                if (ballerinaLambdaFunction != null) {
                    BallerinaFormalParameterList formalParameterList = ballerinaLambdaFunction.getFormalParameterList();
                    if (formalParameterList != null) {
                        processFormalParameterList(formalParameterList);
                    }
                }

                // Check for enclosing foreach statements.
                BallerinaForeachStatement ballerinaForeachStatement = PsiTreeUtil.getParentOfType(block,
                        BallerinaForeachStatement.class);
                if (ballerinaForeachStatement != null) {
                    BallerinaVariableReferenceList variableReferenceList = ballerinaForeachStatement
                            .getVariableReferenceList();
                    if (variableReferenceList != null) {
                        List<BallerinaVariableReference> referenceList = variableReferenceList
                                .getVariableReferenceList();

                        for (int i = 0; i < referenceList.size(); i++) {
                            String type = null;
                            if (referenceList.size() == 2 && i == 0) {

                                type = "int";
                                BallerinaVariableReference ballerinaVariableReference = referenceList.get(0);
                                BallerinaNameReference nameReference = ((BallerinaSimpleVariableReference)
                                        ballerinaVariableReference).getNameReference();
                                PsiElement identifier = nameReference.getIdentifier();
                                if (myResult != null) {
                                    myResult.addElement(BallerinaCompletionUtils
                                            .createVariableLookupElement(identifier, type));
                                } else if (myElement.getText().equals(identifier.getText())) {
                                    add(identifier);
                                    return false;
                                }
                                continue;
                            }

                            BallerinaVariableReference ballerinaVariableReference = referenceList.get(i);
                            if (ballerinaVariableReference instanceof BallerinaSimpleVariableReference) {
                                PsiElement ballerinaVariableReferenceType = ballerinaVariableReference.getType();
                                if (ballerinaVariableReferenceType != null) {
                                    type = ballerinaVariableReferenceType.getText();
                                }

                                BallerinaNameReference nameReference = ((BallerinaSimpleVariableReference)
                                        ballerinaVariableReference).getNameReference();
                                PsiElement identifier = nameReference.getIdentifier();
                                if (myResult != null) {
                                    myResult.addElement(BallerinaCompletionUtils
                                            .createVariableLookupElement(identifier, type));
                                } else if (myElement.getText().equals(identifier.getText())) {
                                    add(identifier);
                                    return false;
                                }
                            }
                        }
                    }
                }

                // Get variable definitions from statements.
                List<BallerinaStatement> statements = block.getStatementList();
                for (BallerinaStatement statement : statements) {
                    int statementEndOffset = statement.getTextRange().getEndOffset();
                    if (statementEndOffset >= myElement.getTextRange().getEndOffset()) {
                        continue;
                    }

                    PsiElement firstChild = statement.getFirstChild();
                    if (firstChild == null) {
                        continue;
                    }
                    if (firstChild instanceof BallerinaVariableDefinitionStatement) {
                        BallerinaVariableDefinitionStatement definitionStatement =
                                (BallerinaVariableDefinitionStatement) firstChild;
                        PsiElement identifier = definitionStatement.getIdentifier();
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                    BallerinaPsiImplUtil.formatBallerinaTypeName(definitionStatement.getTypeName())));
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }

                    } else if (firstChild instanceof BallerinaAssignmentStatement) {
                        BallerinaAssignmentStatement assignmentStatement = (BallerinaAssignmentStatement) firstChild;
                        BallerinaVariableReference variableReference = assignmentStatement.getVariableReference();
                        if (variableReference instanceof BallerinaSimpleVariableReference
                                && assignmentStatement.getVar() != null) {
                            BallerinaNameReference nameReference = ((BallerinaSimpleVariableReference)
                                    variableReference).getNameReference();
                            PsiElement identifier = nameReference.getIdentifier();
                            if (myResult != null) {
                                myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                        "")); // Todo - Get type
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    } else if (firstChild instanceof BallerinaTupleDestructuringStatement) {
                        BallerinaVariableReferenceList variableReferenceList =
                                ((BallerinaTupleDestructuringStatement) firstChild).getVariableReferenceList();
                        if (variableReferenceList != null) {
                            List<BallerinaVariableReference> referenceList =
                                    variableReferenceList.getVariableReferenceList();

                            for (BallerinaVariableReference ballerinaVariableReference : referenceList) {
                                if (ballerinaVariableReference instanceof BallerinaSimpleVariableReference) {
                                    BallerinaNameReference nameReference = ((BallerinaSimpleVariableReference)
                                            ballerinaVariableReference).getNameReference();
                                    PsiElement identifier = nameReference.getIdentifier();
                                    if (myResult != null) {
                                        myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement
                                                (identifier, "")); // Todo - Get type
                                    } else if (myElement.getText().equals(identifier.getText())) {
                                        add(identifier);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!isCompletion() && getResult() != null) {
                    return false;
                }

                block = PsiTreeUtil.getParentOfType(block, BallerinaBlock.class);
            }

            // Todo - check return value and continue only if needed
            processObjectInitializer(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processObjectFunctions(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processObjectFields(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processFunctionSignature(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processObjectFunctionSignature(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processServiceBody(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processResourceSignature(scopeElement);
        }
        return true;
    }

    private void processObjectFunctions(@NotNull PsiElement scopeElement) {
        BallerinaTypeDefinition ballerinaTypeDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaTypeDefinition.class);
        if (ballerinaTypeDefinition != null) {
            BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor =
                    new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
            ballerinaObjectFunctionProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
            PsiElement result = ballerinaObjectFunctionProcessor.getResult();
            if (!isCompletion() && result != null) {
                add(result);
            }
        }
    }

    private void processObjectInitializer(@NotNull PsiElement scopeElement) {
        BallerinaObjectInitializer ballerinaObjectInitializer = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaObjectInitializer.class);
        if (ballerinaObjectInitializer == null) {
            return;
        }

        // Add parameters which are not object parameters to the suggestions
        BallerinaObjectParameterList ballerinaObjectParameterList =
                PsiTreeUtil.findChildOfType(ballerinaObjectInitializer, BallerinaObjectParameterList.class);
        if (ballerinaObjectParameterList != null) {
            List<BallerinaObjectDefaultableParameter> objectDefaultableParameterList =
                    ballerinaObjectParameterList.getObjectDefaultableParameterList();
            for (BallerinaObjectDefaultableParameter parameter : objectDefaultableParameterList) {
                BallerinaObjectParameter objectParameter = parameter.getObjectParameter();
                PsiElement identifier = objectParameter.getIdentifier();
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier,
                            BallerinaPsiImplUtil.formatBallerinaTypeName(objectParameter.getTypeName()),
                            BallerinaPsiImplUtil.formatParameterDefaultValue(parameter.getExpression())));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }

        BallerinaObjectBody ballerinaObjectBody = PsiTreeUtil.getParentOfType(ballerinaObjectInitializer,
                BallerinaObjectBody.class);
        if (ballerinaObjectBody == null) {
            return;
        }
        processObjectDefinition(ballerinaObjectBody);
    }

    private void processObjectFields(@NotNull PsiElement scopeElement) {
        BallerinaObjectFunctionDefinition ballerinaObjectFunctionDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaObjectFunctionDefinition.class);
        if (ballerinaObjectFunctionDefinition == null) {
            return;
        }

        processObjectFunctionDefinition(ballerinaObjectFunctionDefinition);
    }

    // Todo - Move to util and merge with duplicate method in BallerinaObjectFieldProcessor
    private void processObjectFunctionDefinition(@NotNull BallerinaObjectFunctionDefinition definition) {
        BallerinaObjectBody ballerinaObjectBody = PsiTreeUtil.getParentOfType(definition,
                BallerinaObjectBody.class);
        if (ballerinaObjectBody == null) {
            return;
        }

        processObjectDefinition(ballerinaObjectBody);
    }

    private void processObjectDefinition(@NotNull BallerinaObjectBody ballerinaObjectBody) {
        BallerinaTypeDefinition ballerinaTypeDefinition = PsiTreeUtil.getParentOfType(ballerinaObjectBody,
                BallerinaTypeDefinition.class);
        if (ballerinaTypeDefinition == null || ballerinaTypeDefinition.getIdentifier() == null) {
            return;
        }

        BallerinaPublicObjectFields publicObjectFields = ballerinaObjectBody.getPublicObjectFields();
        if (publicObjectFields != null) {
            processObjectFields(ballerinaTypeDefinition.getIdentifier(), publicObjectFields.getFieldDefinitionList(),
                    true);
        }
        BallerinaPrivateObjectFields privateObjectFields = ballerinaObjectBody.getPrivateObjectFields();
        if (privateObjectFields != null) {
            processObjectFields(ballerinaTypeDefinition.getIdentifier(), privateObjectFields.getFieldDefinitionList(),
                    false);
        }
    }

    private void processObjectFields(@Nullable PsiElement typeName,
                                     @NotNull List<BallerinaFieldDefinition> fieldDefinitionList,
                                     boolean isPublic) {
        if (typeName == null) {
            return;
        }
        for (BallerinaFieldDefinition ballerinaFieldDefinition : fieldDefinitionList) {
            PsiElement identifier = ballerinaFieldDefinition.getIdentifier();
            if (myResult != null) {
                myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(identifier, typeName,
                        ballerinaFieldDefinition.getTypeName().getText(),
                        BallerinaPsiImplUtil.getObjectFieldDefaultValue(ballerinaFieldDefinition), null, isPublic));
            } else if (myElement.getText().equals(identifier.getText())) {
                add(identifier);
            }
        }
    }

    private void processFunctionSignature(@NotNull PsiElement scopeElement) {
        BallerinaFunctionDefinition ballerinaFunctionDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaFunctionDefinition.class);
        if (ballerinaFunctionDefinition == null) {
            return;
        }

        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
        if (callableUnitSignature == null) {
            return;
        }

        // Todo - Needed here? Cannot access without self?
        //        BallerinaAttachedObject attachedObject = ballerinaFunctionDefinition.getAttachedObject();
        //        if (attachedObject != null) {
        //            PsiElement identifier = callableUnitSignature.getIdentifier();
        //            PsiReference reference = identifier.getReference();
        //            if (reference != null) {
        //                PsiElement resolvedElement = reference.resolve();
        //                if (resolvedElement != null) {
        //                    BallerinaObjectFunctionDefinition objectFunctionDefinition =
        //                            PsiTreeUtil.getParentOfType(resolvedElement, BallerinaObjectFunctionDefinition
        // .class);
        //                    if (objectFunctionDefinition != null) {
        //                        processObjectFunctionDefinition(objectFunctionDefinition);
        //                    }
        //                }
        //            }
        //        }

        BallerinaFormalParameterList formalParameterList = callableUnitSignature.getFormalParameterList();
        if (formalParameterList == null) {
            return;
        }
        processFormalParameterList(formalParameterList);
    }

    private void processObjectFunctionSignature(@NotNull PsiElement scopeElement) {
        BallerinaObjectFunctionDefinition objectFunctionDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaObjectFunctionDefinition.class);
        if (objectFunctionDefinition == null) {
            return;
        }

        BallerinaObjectCallableUnitSignature callableUnitSignature =
                objectFunctionDefinition.getObjectCallableUnitSignature();
        if (callableUnitSignature == null) {
            return;
        }

        BallerinaFormalParameterList formalParameterList = callableUnitSignature.getFormalParameterList();
        if (formalParameterList == null) {
            return;
        }
        processFormalParameterList(formalParameterList);
    }

    private void processServiceBody(@NotNull PsiElement scopeElement) {
        BallerinaServiceBody serviceBody = PsiTreeUtil.getParentOfType(scopeElement, BallerinaServiceBody.class);
        if (serviceBody == null) {
            return;
        }

        List<BallerinaVariableDefinitionStatement> definitions = serviceBody.getVariableDefinitionStatementList();
        for (BallerinaVariableDefinitionStatement definition : definitions) {
            PsiElement identifier = definition.getIdentifier();
            if (identifier == null) {
                continue;
            }
            if (myResult != null) {
                String type = "";
                PsiElement definitionType = BallerinaPsiImplUtil.getType(definition);
                if (definitionType != null) {
                    type = definitionType.getText();
                }
                myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier, type));
            } else if (myElement.getText().equals(identifier.getText())) {
                add(identifier);
            }
        }

        List<BallerinaNamespaceDeclarationStatement> namespaces = serviceBody.getNamespaceDeclarationStatementList();
        for (BallerinaNamespaceDeclarationStatement namespace : namespaces) {
            BallerinaNamespaceDeclaration namespaceDeclaration = namespace.getNamespaceDeclaration();
            if (namespaceDeclaration == null) {
                continue;
            }
            PsiElement identifier = namespaceDeclaration.getIdentifier();
            if (identifier == null) {
                continue;
            }
            if (myResult != null) {
                myResult.addElement(BallerinaCompletionUtils.createNamespaceLookupElement(identifier));
            } else if (myElement.getText().equals(identifier.getText())) {
                add(identifier);
            }
        }
    }

    private void processFormalParameterList(@NotNull BallerinaFormalParameterList formalParameterList) {
        processParameterList(formalParameterList.getParameterList());

        List<BallerinaDefaultableParameter> defaultableParameterList =
                formalParameterList.getDefaultableParameterList();
        for (BallerinaDefaultableParameter ballerinaDefaultableParameter : defaultableParameterList) {
            BallerinaParameter parameter = ballerinaDefaultableParameter.getParameter();
            List<BallerinaParameterWithType> parameterWithTypeList = parameter
                    .getParameterWithTypeList();
            for (BallerinaParameterWithType ballerinaParameterWithType : parameterWithTypeList) {
                PsiElement identifier = ballerinaParameterWithType.getIdentifier();
                if (identifier == null) {
                    continue;
                }
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier, null, null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }

        BallerinaRestParameter restParameter = formalParameterList.getRestParameter();
        if (restParameter != null) {
            PsiElement identifier = restParameter.getIdentifier();
            if (identifier != null) {
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier, null, null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }
    }

    private void processParameterList(@NotNull List<BallerinaParameter> parameterList) {
        for (BallerinaParameter parameter : parameterList) {
            List<BallerinaParameterWithType> parameterWithTypeList = parameter.getParameterWithTypeList();
            for (BallerinaParameterWithType ballerinaParameterWithType : parameterWithTypeList) {
                PsiElement identifier = ballerinaParameterWithType.getIdentifier();
                if (identifier == null) {
                    continue;
                }
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier,
                            BallerinaPsiImplUtil.formatBallerinaTypeName(ballerinaParameterWithType.getTypeName()),
                            null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }
    }

    private void processResourceSignature(@NotNull PsiElement scopeElement) {
        BallerinaResourceDefinition ballerinaResourceDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaResourceDefinition.class);
        if (ballerinaResourceDefinition == null) {
            return;
        }
        BallerinaResourceParameterList resourceParameterList = ballerinaResourceDefinition.getResourceParameterList();
        if (resourceParameterList == null) {
            return;
        }
        BallerinaEndpointParameter endpointParameter = resourceParameterList.getEndpointParameter();
        if (endpointParameter != null) {
            PsiElement identifier = endpointParameter.getIdentifier();
            if (identifier != null) {
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier, "Endpoint",
                            null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
            BallerinaParameterList parameterList = endpointParameter.getParameterList();
            if (parameterList == null) {
                return;
            }
            processParameterList(parameterList.getParameterList());
        }

        BallerinaParameterList parameterList = resourceParameterList.getParameterList();
        if (parameterList != null) {
            processParameterList(parameterList.getParameterList());
        }
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
