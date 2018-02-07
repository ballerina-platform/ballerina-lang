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

package org.ballerinalang.plugins.idea.codeinsight.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ThreeState;
import org.ballerinalang.plugins.idea.psi.FloatingPointLiteral;
import org.jetbrains.annotations.NotNull;

public class SkipAutoPopupInFloatingPointLiterals extends CompletionConfidence {

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        PsiElement elementAtOffset = psiFile.findElementAt(offset);
        if (elementAtOffset == null) {
            if (offset > 0) {
                elementAtOffset = psiFile.findElementAt(offset - 1);
                if (elementAtOffset instanceof FloatingPointLiteral) {
                    return ThreeState.YES;
                }
            }
        } else {
            PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(elementAtOffset);
            if (prevVisibleLeaf instanceof FloatingPointLiteral) {
                return ThreeState.YES;
            }
        }
        return ThreeState.UNSURE;
    }
}
