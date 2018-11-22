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
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.psi.BallerinaCatchClause;
import io.ballerina.plugins.idea.psi.BallerinaExpression;
import io.ballerina.plugins.idea.psi.BallerinaFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaJoinClause;
import io.ballerina.plugins.idea.psi.BallerinaNamedPattern;
import io.ballerina.plugins.idea.psi.BallerinaRecordFieldDefinitionList;
import io.ballerina.plugins.idea.psi.BallerinaRecordKey;
import io.ballerina.plugins.idea.psi.BallerinaRecordKeyValue;
import io.ballerina.plugins.idea.psi.BallerinaRecordLiteralExpression;
import io.ballerina.plugins.idea.psi.BallerinaRecordTypeName;
import io.ballerina.plugins.idea.psi.BallerinaServiceBody;
import io.ballerina.plugins.idea.psi.BallerinaStatement;
import io.ballerina.plugins.idea.psi.BallerinaTimeoutClause;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypeName;
import io.ballerina.plugins.idea.psi.BallerinaVariableDefinitionStatement;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing definitions in statements.
 */
public class BallerinaStatementProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaStatementProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                       boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaStatement || element instanceof BallerinaVariableDefinitionStatement;
    }

    @Override
    public boolean execute(@NotNull PsiElement scopeElement, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(scopeElement)) {
            if (scopeElement instanceof BallerinaStatement) {
                BallerinaStatement statement = (BallerinaStatement) scopeElement;

                // This is to suggest fields in when the caret is in a record literal.
                BallerinaRecordKey ballerinaRecordKey = PsiTreeUtil.getParentOfType(myElement,
                        BallerinaRecordKey.class);

                // Todo - Check for record literal expression to find other types.
                if (ballerinaRecordKey != null) {
                    BallerinaRecordLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(ballerinaRecordKey,
                            BallerinaRecordLiteralExpression.class);
                    if (literalExpression != null && literalExpression.getParent() instanceof BallerinaRecordKeyValue) {

                        BallerinaRecordKeyValue expressionParent =
                                (BallerinaRecordKeyValue) literalExpression.getParent();
                        BallerinaRecordKey parentRecordKey = expressionParent.getRecordKey();
                        BallerinaExpression expression = parentRecordKey.getExpression();
                        if (expression == null) {
                            return true;
                        }
                        PsiElement resolvedElement = BallerinaPsiImplUtil.getBallerinaTypeFromExpression(expression);
                        if (resolvedElement != null) {
                            PsiElement parent = resolvedElement.getParent();
                            if (resolvedElement instanceof BallerinaRecordTypeName) {
                                BallerinaRecordFieldDefinitionList fieldDefinitionList =
                                        PsiTreeUtil.findChildOfType(parent, BallerinaRecordFieldDefinitionList.class);
                                List<BallerinaFieldDefinition> fieldDefinitions =
                                        PsiTreeUtil.getChildrenOfTypeAsList(fieldDefinitionList,
                                                BallerinaFieldDefinition.class);
                                for (BallerinaFieldDefinition ballerinaFieldDefinition : fieldDefinitions) {
                                    PsiElement identifier = ballerinaFieldDefinition.getIdentifier();
                                    if (myResult != null) {
                                        BallerinaTypeName fieldTypeName = ballerinaFieldDefinition.getTypeName();
                                        String type;
                                        if (fieldTypeName instanceof BallerinaRecordTypeName) {
                                            type = "record {}";
                                        } else {
                                            type = fieldTypeName.getText();
                                        }
                                        myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement
                                                (identifier, resolvedElement, type,
                                                        BallerinaPsiImplUtil.getObjectFieldDefaultValue
                                                                (ballerinaFieldDefinition), null, false, false));
                                    } else if (myElement.getText().equals(identifier.getText())) {
                                        add(identifier);
                                    }
                                }

                            } else if (parent instanceof BallerinaTypeDefinition) {
                                BallerinaObjectFieldProcessor ballerinaFieldProcessor = new
                                        BallerinaObjectFieldProcessor
                                        (myResult, myElement, isCompletion());
                                ballerinaFieldProcessor.execute(parent, ResolveState.initial());
                                PsiElement result = ballerinaFieldProcessor.getResult();
                                if (!isCompletion() && result != null) {
                                    add(result);
                                    return false;
                                }
                            }
                        }
                        return false;

                    }

                    if (statement.getFirstChild() instanceof BallerinaVariableDefinitionStatement) {
                        PsiElement resolvedElement = BallerinaPsiImplUtil.resolveBallerinaType((
                                (BallerinaVariableDefinitionStatement) statement.getFirstChild()));
                        if (resolvedElement != null && resolvedElement.getParent() instanceof BallerinaTypeDefinition) {
                            BallerinaObjectFieldProcessor ballerinaFieldProcessor = new BallerinaObjectFieldProcessor
                                    (myResult, myElement, isCompletion());
                            ballerinaFieldProcessor.execute(resolvedElement.getParent(), ResolveState.initial());
                            PsiElement result = ballerinaFieldProcessor.getResult();
                            if (!isCompletion() && result != null) {
                                add(result);
                                return false;
                            }
                        }
                    }
                    // We don't want to suggest any other completion since we are currently at the key of a record
                    // literal.
                    return false;
                }

                BallerinaNamedPattern ballerinaNamedPattern = PsiTreeUtil.getParentOfType(statement,
                        BallerinaNamedPattern.class);
                while (ballerinaNamedPattern != null) {
                    PsiElement identifier = ballerinaNamedPattern.getIdentifier();
                    if (myResult != null) {
                        myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                BallerinaPsiImplUtil.formatBallerinaTypeName(ballerinaNamedPattern.getTypeName())));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                    if (!isCompletion() && getResult() != null) {
                        return false;
                    }
                    ballerinaNamedPattern = PsiTreeUtil.getParentOfType(ballerinaNamedPattern,
                            BallerinaNamedPattern.class);
                }

                // Process catch clause variable.
                BallerinaCatchClause ballerinaCatchClause = PsiTreeUtil.getParentOfType(statement,
                        BallerinaCatchClause.class);
                while (ballerinaCatchClause != null) {
                    PsiElement identifier = ballerinaCatchClause.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                    BallerinaPsiImplUtil.formatBallerinaTypeName(ballerinaCatchClause.getTypeName())));
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                    if (!isCompletion() && getResult() != null) {
                        return false;
                    }
                    ballerinaCatchClause = PsiTreeUtil.getParentOfType(ballerinaCatchClause,
                            BallerinaCatchClause.class);
                }

                // Process join clause variable.
                BallerinaJoinClause ballerinaJoinClause = PsiTreeUtil.getParentOfType(statement,
                        BallerinaJoinClause.class);
                while (ballerinaJoinClause != null) {
                    PsiElement identifier = ballerinaJoinClause.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                    BallerinaPsiImplUtil.formatBallerinaTypeName(ballerinaJoinClause.getTypeName())));
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                    if (!isCompletion() && getResult() != null) {
                        return false;
                    }
                    ballerinaJoinClause = PsiTreeUtil.getParentOfType(ballerinaJoinClause, BallerinaJoinClause.class);
                }

                // Process timeout clause variables.
                BallerinaTimeoutClause ballerinaTimeoutClause = PsiTreeUtil.
                        getParentOfType(statement, BallerinaTimeoutClause.class);
                while (ballerinaTimeoutClause != null) {
                    PsiElement identifier = ballerinaTimeoutClause.getIdentifier();
                    if (identifier != null) {
                        if (myResult != null) {
                            myResult.addElement(BallerinaCompletionUtils.createVariableLookupElement(identifier,
                                    BallerinaPsiImplUtil.
                                            formatBallerinaTypeName(ballerinaTimeoutClause.getTypeName())));
                        } else if (myElement.getText().equals(identifier.getText())) {
                            add(identifier);
                        }
                    }
                    if (!isCompletion() && getResult() != null) {
                        return false;
                    }
                    ballerinaTimeoutClause = PsiTreeUtil.getParentOfType(ballerinaTimeoutClause,
                            BallerinaTimeoutClause.class);
                }

            } else if (scopeElement instanceof BallerinaVariableDefinitionStatement) {
                BallerinaVariableDefinitionStatement statement = (BallerinaVariableDefinitionStatement) scopeElement;
                BallerinaServiceBody ballerinaServiceBody = PsiTreeUtil.getParentOfType(myElement,
                        BallerinaServiceBody.class);
                if (ballerinaServiceBody != null) {
                    List<BallerinaVariableDefinitionStatement> definitionStatements =
                            ballerinaServiceBody.getVariableDefinitionStatementList();
                    for (BallerinaVariableDefinitionStatement definitionStatement : definitionStatements) {
                        PsiElement identifier = null;
                        if (definitionStatement.getVariableDefinitionStatementWithAssignment() != null) {
                            identifier = definitionStatement.getVariableDefinitionStatementWithAssignment()
                                    .getBindingPattern().getIdentifier();
                        } else if (definitionStatement.getVariableDefinitionStatementWithoutAssignment() != null) {
                            identifier = definitionStatement.getVariableDefinitionStatementWithoutAssignment()
                                    .getIdentifier();
                        }
                        if (identifier != null) {
                            int statementEndOffset = definitionStatement.getTextRange().getEndOffset();
                            if (statementEndOffset >= statement.getTextRange().getEndOffset()) {
                                continue;
                            }
                            if (myElement.getText().equals(identifier.getText())) {
                                add(identifier);
                            }
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
}
