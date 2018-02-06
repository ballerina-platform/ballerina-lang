/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea.codeInsight.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ThreeState;
import org.ballerinalang.plugins.idea.psi.ForEachStatementNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.jetbrains.annotations.NotNull;

public class SkipAutoPopupInForEach extends CompletionConfidence {

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        VariableReferenceListNode variableReferenceListNode = PsiTreeUtil.getParentOfType(contextElement,
                VariableReferenceListNode.class);
        ForEachStatementNode forEachStatementNode = PsiTreeUtil.getParentOfType(contextElement,
                ForEachStatementNode.class);
        if (variableReferenceListNode != null && forEachStatementNode != null) {
            return ThreeState.YES;
        }
        return ThreeState.UNSURE;
    }
}
