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
import io.ballerina.plugins.idea.psi.BallerinaFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFiniteType;
import io.ballerina.plugins.idea.psi.BallerinaFiniteTypeUnit;
import io.ballerina.plugins.idea.psi.BallerinaObjectBody;
import io.ballerina.plugins.idea.psi.BallerinaObjectFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaRecordFieldDefinitionList;
import io.ballerina.plugins.idea.psi.BallerinaRecordTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypeName;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing object fields.
 */
public class BallerinaObjectFieldProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaObjectFieldProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                         boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaTypeDefinition;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            BallerinaFiniteType ballerinaFiniteType = PsiTreeUtil.getChildOfType(element, BallerinaFiniteType.class);
            if (ballerinaFiniteType == null) {
                return true;
            }

            PsiElement firstChild = ballerinaFiniteType.getFirstChild();
            if (firstChild instanceof BallerinaObjectBody) {
                processObjectDefinition(((BallerinaObjectBody) firstChild));
                return true;
            }
            PsiElement owner = ((BallerinaTypeDefinition) element).getIdentifier();
            if (owner == null) {
                return true;
            }

            List<BallerinaFiniteTypeUnit> finiteTypeUnitList = ballerinaFiniteType.getFiniteTypeUnitList();
            for (BallerinaFiniteTypeUnit ballerinaFiniteTypeUnit : finiteTypeUnitList) {
                BallerinaTypeName typeName = ballerinaFiniteTypeUnit.getTypeName();
                if (!(typeName instanceof BallerinaRecordTypeName)) {
                    continue;
                }
                BallerinaRecordFieldDefinitionList fieldDefinitionList =
                        ((BallerinaRecordTypeName) typeName).getRecordFieldDefinitionList();
                List<BallerinaFieldDefinition> fieldList = fieldDefinitionList.getFieldDefinitionList();
                for (BallerinaFieldDefinition ballerinaFieldDefinition : fieldList) {
                    PsiElement identifier = ballerinaFieldDefinition.getIdentifier();
                    if (myResult != null) {
                        BallerinaTypeName fieldTypeName = ballerinaFieldDefinition.getTypeName();
                        String type;
                        if (fieldTypeName instanceof BallerinaRecordTypeName) {
                            type = "record {}";
                        } else {
                            type = fieldTypeName.getText();
                        }
                        myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(identifier, owner,
                                type, BallerinaPsiImplUtil.getObjectFieldDefaultValue(ballerinaFieldDefinition),
                                null, false, false));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                }
            }
        }
        return true;
    }

    // Todo - Merge with function in BallerinaBlockProcessor
    private void processObjectDefinition(@NotNull BallerinaObjectBody ballerinaObjectBody) {
        BallerinaTypeDefinition ballerinaTypeDefinition = PsiTreeUtil.getParentOfType(ballerinaObjectBody,
                BallerinaTypeDefinition.class);
        if (ballerinaTypeDefinition == null || ballerinaTypeDefinition.getIdentifier() == null) {
            return;
        }

        List<BallerinaObjectFieldDefinition> objectFieldDefinitionList =
                ballerinaObjectBody.getObjectFieldDefinitionList();
        processObjectFields(ballerinaTypeDefinition.getIdentifier(), objectFieldDefinitionList);
    }

    // Todo - Merge with function in BallerinaBlockProcessor
    private void processObjectFields(@NotNull PsiElement typeName,
                                     @NotNull List<BallerinaObjectFieldDefinition> fieldDefinitionList) {
        for (BallerinaObjectFieldDefinition ballerinaFieldDefinition : fieldDefinitionList) {
            PsiElement identifier = ballerinaFieldDefinition.getIdentifier();
            if (identifier == null) {
                return;
            }
            if (myResult != null) {
                myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(identifier, typeName,
                        ballerinaFieldDefinition.getTypeName().getText(),
                        BallerinaPsiImplUtil.getObjectFieldDefaultValue(ballerinaFieldDefinition), null,
                        ballerinaFieldDefinition.getPublic() == null, ballerinaFieldDefinition.getPrivate() == null));
            } else if (myElement.getText().equals(identifier.getText())) {
                add(identifier);
            }
            if (!isCompletion() && getResult() != null) {
                return;
            }
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
