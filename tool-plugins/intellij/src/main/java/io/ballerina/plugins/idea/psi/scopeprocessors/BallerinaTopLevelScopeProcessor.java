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

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.completion.inserthandlers.ParenthesisInsertHandler;
import io.ballerina.plugins.idea.completion.inserthandlers.SmartParenthesisInsertHandler;
import io.ballerina.plugins.idea.psi.BallerinaAnnotationAttachment;
import io.ballerina.plugins.idea.psi.BallerinaAnnotationDefinition;
import io.ballerina.plugins.idea.psi.BallerinaChannelDefinition;
import io.ballerina.plugins.idea.psi.BallerinaCompositeElement;
import io.ballerina.plugins.idea.psi.BallerinaDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaGlobalVariable;
import io.ballerina.plugins.idea.psi.BallerinaGlobalVariableDefinition;
import io.ballerina.plugins.idea.psi.BallerinaMatchExpressionPatternClause;
import io.ballerina.plugins.idea.psi.BallerinaNameReference;
import io.ballerina.plugins.idea.psi.BallerinaNamespaceDeclaration;
import io.ballerina.plugins.idea.psi.BallerinaOnCommitStatement;
import io.ballerina.plugins.idea.psi.BallerinaOnRetryClause;
import io.ballerina.plugins.idea.psi.BallerinaPackageReference;
import io.ballerina.plugins.idea.psi.BallerinaServiceDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing definitions in top level scopes.
 */
public class BallerinaTopLevelScopeProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    private boolean lookupElementsFound;

    public BallerinaTopLevelScopeProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                           boolean isCompletion) {
        super(element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaFile;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            BallerinaFile file = (BallerinaFile) element;
            List<BallerinaDefinition> definitions = file.getDefinitions();

            PsiElement parent = myElement.getParent();
            PsiElement superParent = parent.getParent();
            if (superParent instanceof BallerinaAnnotationAttachment) {
                if (!(myElement.getPrevSibling() instanceof BallerinaPackageReference)) {
                    List<BallerinaAnnotationDefinition> annotationDefinitions =
                            BallerinaPsiImplUtil.suggestBuiltInAnnotations(element);
                    for (BallerinaAnnotationDefinition definition : annotationDefinitions) {
                        ProgressManager.checkCanceled();
                        PsiElement identifier = definition.getIdentifier();
                        if (identifier != null) {
                            if (myResult != null) {
                                myResult.addElement(BallerinaCompletionUtils.createAnnotationLookupElement(identifier));
                                lookupElementsFound = true;
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    }
                }
                for (BallerinaDefinition definition : definitions) {
                    ProgressManager.checkCanceled();
                    PsiElement lastChild = definition.getLastChild();
                    if (lastChild instanceof BallerinaDefinition) {
                        lastChild = lastChild.getLastChild();
                    }
                    if (lastChild instanceof BallerinaAnnotationDefinition) {
                        BallerinaAnnotationDefinition child = (BallerinaAnnotationDefinition) lastChild;
                        PsiElement identifier = child.getIdentifier();
                        if (identifier != null) {
                            if (myResult != null) {
                                myResult.addElement(BallerinaCompletionUtils.createAnnotationLookupElement(identifier));
                                lookupElementsFound = true;
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    }
                }
                if (isCompletion() && getResult() != null) {
                    return false;
                }
                return true;
            }

            List<BallerinaTypeDefinition> ballerinaTypeDefinitions =
                    BallerinaPsiImplUtil.suggestBuiltInTypes(element);
            if (!ballerinaTypeDefinitions.isEmpty()) {
                for (BallerinaTypeDefinition definition : ballerinaTypeDefinitions) {
                    ProgressManager.checkCanceled();
                    PsiElement identifier = definition.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {

                            InsertHandler<LookupElement> insertHandler = AddSpaceInsertHandler.INSTANCE;
                            BallerinaNameReference nameReference = PsiTreeUtil.getParentOfType(myElement,
                                    BallerinaNameReference.class);
                            if (nameReference != null) {
                                if (nameReference.getParent() instanceof BallerinaServiceDefinition) {
                                    insertHandler = null;
                                }
                            }

                            String publicFieldsOnly = state.get(BallerinaCompletionUtils.PUBLIC_DEFINITIONS_ONLY);
                            if (publicFieldsOnly != null) {
                                if (definition.isPublic()) {
                                    myResult.addElement(BallerinaCompletionUtils.createTypeLookupElement(definition,
                                            insertHandler));
                                    lookupElementsFound = true;
                                }
                            } else {
                                myResult.addElement(BallerinaCompletionUtils.createTypeLookupElement(definition,
                                        insertHandler));
                                lookupElementsFound = true;
                            }
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                }

                if (isCompletion() && getResult() != null) {
                    return false;
                }
            }

            for (BallerinaDefinition definition : definitions) {
                ProgressManager.checkCanceled();
                PsiElement lastChild = definition.getLastChild();
                if (lastChild instanceof BallerinaDefinition) {
                    lastChild = lastChild.getLastChild();
                }
                if (lastChild instanceof BallerinaFunctionDefinition) {
                    BallerinaFunctionDefinition child = (BallerinaFunctionDefinition) lastChild;
                    if (child.getAttachedObject() == null) {
                        PsiElement identifier = child.getIdentifier();
                        if (identifier != null) {
                            if (myResult != null) {
                                // Todo - Conside oncommit, onabort, etc and set the insert handler
                                // Note - Child is passed here instead of identifier because it is is top level
                                // definition.
                                String publicFieldsOnly = state.get(BallerinaCompletionUtils.PUBLIC_DEFINITIONS_ONLY);
                                InsertHandler<LookupElement> insertHandler = SmartParenthesisInsertHandler.INSTANCE;
                                BallerinaCompositeElement compositeElement = PsiTreeUtil.getParentOfType(myElement,
                                        BallerinaOnCommitStatement.class, BallerinaOnRetryClause.class);
                                if (compositeElement != null) {
                                    insertHandler = ParenthesisInsertHandler.INSTANCE;
                                }
                                // Todo - Fix the transaction properties grammar.
                                PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(myElement);
                                if (nextVisibleLeaf instanceof LeafPsiElement) {
                                    IElementType elementType = ((LeafPsiElement) nextVisibleLeaf).getElementType();
                                    if (elementType == BallerinaTypes.COMMA
                                            || elementType == BallerinaTypes.LEFT_BRACE) {
                                        insertHandler = ParenthesisInsertHandler.INSTANCE;
                                    }
                                }
                                BallerinaMatchExpressionPatternClause patternClause =
                                        PsiTreeUtil.getParentOfType(myElement,
                                                BallerinaMatchExpressionPatternClause.class);
                                if (publicFieldsOnly != null) {
                                    if (child.isPublic()) {
                                        myResult.addElement(BallerinaCompletionUtils
                                                .createFunctionLookupElementWithSemicolon(child, insertHandler,
                                                        patternClause == null));
                                        lookupElementsFound = true;
                                    }
                                } else {

                                    myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement(child,
                                            insertHandler));
                                    lookupElementsFound = true;
                                }
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    }
                } else if (lastChild instanceof BallerinaGlobalVariable) {
                    PsiElement definitionType = lastChild.getFirstChild();
                    if (definitionType instanceof BallerinaGlobalVariableDefinition) {
                        BallerinaGlobalVariableDefinition child = (BallerinaGlobalVariableDefinition) definitionType;
                        PsiElement identifier = child.getIdentifier();
                        if (identifier != null) {
                            if (myResult != null) {
                                String publicFieldsOnly = state.get(BallerinaCompletionUtils.PUBLIC_DEFINITIONS_ONLY);
                                if (publicFieldsOnly != null) {
                                    if (child.isPublic()) {
                                        myResult.addElement(BallerinaCompletionUtils
                                                .createGlobalVariableLookupElement(child, BallerinaPsiImplUtil
                                                        .formatBallerinaTypeName(child.getTypeName())));
                                        lookupElementsFound = true;
                                    }
                                } else {
                                    myResult.addElement(BallerinaCompletionUtils
                                            .createGlobalVariableLookupElement(child, BallerinaPsiImplUtil
                                                    .formatBallerinaTypeName(child.getTypeName())));
                                    lookupElementsFound = true;
                                }
                            } else if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
                        }
                    } else if (definitionType instanceof BallerinaChannelDefinition) {
                        BallerinaChannelDefinition child = (BallerinaChannelDefinition) definitionType;
                        PsiElement identifier = child.getIdentifier();
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils
                                    .createChannelVariableLookupElement(child, null));
                            lookupElementsFound = true;
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                } else if (lastChild instanceof BallerinaTypeDefinition) {
                    BallerinaTypeDefinition child = (BallerinaTypeDefinition) lastChild;
                    PsiElement identifier = child.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            InsertHandler<LookupElement> insertHandler = AddSpaceInsertHandler.INSTANCE;
                            BallerinaNameReference nameReference = PsiTreeUtil.getParentOfType(myElement,
                                    BallerinaNameReference.class);
                            if (nameReference != null) {
                                if (nameReference.getParent() instanceof BallerinaServiceDefinition) {
                                    insertHandler = null;
                                }
                            }
                            String publicFieldsOnly = state.get(BallerinaCompletionUtils.PUBLIC_DEFINITIONS_ONLY);
                            if (publicFieldsOnly != null) {
                                if (child.isPublic()) {
                                    myResult.addElement(BallerinaCompletionUtils.createTypeLookupElement(child,
                                            insertHandler));
                                    lookupElementsFound = true;
                                }
                            } else {
                                myResult.addElement(BallerinaCompletionUtils.createTypeLookupElement(child,
                                        insertHandler));
                                lookupElementsFound = true;
                            }
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                } else if (lastChild instanceof BallerinaServiceDefinition) {
                    BallerinaServiceDefinition child = (BallerinaServiceDefinition) lastChild;
                    PsiElement identifier = child.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createServiceLookupElement(identifier));
                            lookupElementsFound = true;
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                }
                if (!isCompletion() && getResult() != null) {
                    return false;
                }
            }

            // Check in global namespace declarations.
            List<BallerinaNamespaceDeclaration> namespaceDeclarations =
                    PsiTreeUtil.getChildrenOfTypeAsList(file, BallerinaNamespaceDeclaration.class);
            for (PsiElement definition : namespaceDeclarations) {
                if (definition instanceof BallerinaNamespaceDeclaration) {
                    PsiElement identifier = ((BallerinaNamespaceDeclaration) definition).getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createNamespaceLookupElement(identifier));
                            lookupElementsFound = true;
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
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

    public boolean isLookupElementsFound() {
        return lookupElementsFound;
    }
}
